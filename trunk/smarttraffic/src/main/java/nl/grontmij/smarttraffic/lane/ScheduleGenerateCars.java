package nl.grontmij.smarttraffic.lane;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.naming.NamingException;

import nl.tudelft.simulation.dsol.SimRuntimeException;

import org.opentrafficsim.core.car.LaneBasedIndividualCar;
import org.opentrafficsim.core.car.LaneBasedIndividualCar.LaneBasedIndividualCarBuilder;
import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.core.gtu.animation.DefaultCarAnimation;
import org.opentrafficsim.core.gtu.animation.GTUColorer;
import org.opentrafficsim.core.gtu.animation.IDGTUColorer;
import org.opentrafficsim.core.gtu.following.GTUFollowingModel;
import org.opentrafficsim.core.gtu.following.HeadwayGTU;
import org.opentrafficsim.core.gtu.following.IDMPlus;
import org.opentrafficsim.core.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.core.gtu.lane.changing.Egoistic;
import org.opentrafficsim.core.gtu.lane.changing.LaneChangeModel;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.lane.Lane;
import org.opentrafficsim.core.network.route.CompleteLaneBasedRouteNavigator;
import org.opentrafficsim.core.network.route.CompleteRoute;
import org.opentrafficsim.core.network.route.LaneBasedRouteNavigator;
import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.unit.SpeedUnit;
import org.opentrafficsim.core.unit.TimeUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;

public class ScheduleGenerateCars
{
    /** The type of GTUs generated. */
    final GTUType<String> gtuType;

    /** The GTU following model used by all generated GTUs. */
    final GTUFollowingModel gtuFollowingModel;

    /** The lane change model used by all generated GTUs. */
    final LaneChangeModel laneChangeModel;

    final int generateCar;

    final double lengthCar;

    /** Initial speed of the generated GTUs. */
    DoubleScalar.Abs<SpeedUnit> initialSpeed;

    /** The GTU colorer that will be linked to each generated GTU. */
    final GTUColorer gtuColorer;

    /** The simulator that controls everything. */
    final OTSDEVSSimulatorInterface simulator;

    Map<String, GenerateSensor> mapSensor;

    /** Number of GTUs created. */
    int carsCreated = 0;

    /** the routes. A and B. */
    private Map<String, CompleteRoute> routes;

    public ScheduleGenerateCars(GTUType<String> gtuType, OTSDEVSSimulatorInterface simulator,
        Map<String, GenerateSensor> mapSensor, int generateCar, Map<String, CompleteRoute> routes)
    {
        this.gtuType = gtuType;
        this.gtuFollowingModel = new IDMPlus();
        this.laneChangeModel = new Egoistic();
        this.gtuColorer = new IDGTUColorer();
        this.simulator = simulator;
        this.mapSensor = mapSensor;
        this.generateCar = generateCar;
        this.lengthCar = 4.5;
        this.routes = routes;
        generateVehiclesFromDetector();
    }

    /**
     * Schedule generation of the next GTU.
     */
    public void generateVehiclesFromDetector()
    {
        for (Entry<String, GenerateSensor> entry : this.mapSensor.entrySet())
        {
            entry.getKey();
            GenerateSensor sensor = entry.getValue();
            HashMap<DoubleScalar.Abs<TimeUnit>, Integer> pulses = sensor.getStatusByTime();
            for (Entry<DoubleScalar.Abs<TimeUnit>, Integer> entryPulse : pulses.entrySet())
            {
                Lane<?, ?> lane = sensor.getLane();
                DoubleScalar.Rel<LengthUnit> initialPosition = sensor.getLongitudinalPosition();
                this.initialSpeed = lane.getSpeedLimit(); // TODO: what is the initial speed?
                if (entryPulse.getValue() == this.generateCar)
                {
                    DoubleScalar.Abs<TimeUnit> when = entryPulse.getKey();
                    try
                    {
                        this.simulator.scheduleEventAbs(when, this, this, "generateCar", new Object[]{lane, initialPosition});
                    }
                    catch (RemoteException | SimRuntimeException exception)
                    {
                        exception.printStackTrace();
                        System.exit(-1);
                    }
                }
            }

        }
    }

    /**
     * Generate one car and re-schedule this method.
     */
    protected final void generateCar(Lane<?, ?> lane, DoubleScalar.Rel<LengthUnit> initialPosition)
    {
        Map<Lane<?, ?>, DoubleScalar.Rel<LengthUnit>> initialPositions =
            new LinkedHashMap<Lane<?, ?>, DoubleScalar.Rel<LengthUnit>>();
        enoughSpace(lane, initialPosition.getSI(), this.lengthCar);
        initialPositions.put(lane, initialPosition);
        if (initialPosition.getSI() + this.lengthCar > lane.getLength().getSI())
        {
            // also register on next lane.
            if (lane.nextLanes().size() == 0 || lane.nextLanes().size() > 1)
            {
                System.err.println("lane.nextLanes().size() == 0 || lane.nextLanes().size() > 1");
                System.exit(-1);
            }
            Lane<?, ?> nextLane = lane.nextLanes().iterator().next();
            DoubleScalar.Rel<LengthUnit> nextPos =
                new DoubleScalar.Rel<LengthUnit>(initialPosition.getSI() - lane.getLength().getSI(), LengthUnit.METER);
            initialPositions.put(nextLane, nextPos);
        }
        CompleteRoute straightRouteAB;
        String linkName = lane.getParentLink().getId().toString();
        String ab = linkName.substring(4, 5);
        if (ab.equalsIgnoreCase("a"))
        {
            straightRouteAB = this.routes.get("A");
        }
        else if (ab.equalsIgnoreCase("b"))
        {
            straightRouteAB = this.routes.get("B");
        }
        else
        {
            System.err.println("generateCar - link " + linkName + ", not clear whether on A or B side");
            straightRouteAB = null;
        }
        StraightRouteNavigator routeNavigatorAB = new StraightRouteNavigator(straightRouteAB, lane.getParentLink());
        try
        {
            DoubleScalar.Rel<LengthUnit> vehicleLength = new DoubleScalar.Rel<LengthUnit>(this.lengthCar, LengthUnit.METER);
            new LaneBasedIndividualCar<Integer>(++this.carsCreated, this.gtuType, this.gtuFollowingModel,
                this.laneChangeModel, initialPositions, this.initialSpeed, vehicleLength, new DoubleScalar.Rel<LengthUnit>(
                    2.0, LengthUnit.METER), new DoubleScalar.Abs<SpeedUnit>(80, SpeedUnit.KM_PER_HOUR), routeNavigatorAB,
                this.simulator, DefaultCarAnimation.class, this.gtuColorer);
        }
        catch (RemoteException | SimRuntimeException | NamingException | NetworkException | GTUException exception)
        {
            exception.printStackTrace();
        }
    }

    /**
     * Check if the car to be built is not overlapping with another GTU on the same lane, and if it has enough headway to be
     * generated safely.
     * @param carBuilder the car to be generated
     * @return true if car can be safely built, false otherwise.
     * @throws RemoteException if simulator cannot be reached to calculate current position
     * @throws NetworkException if GTU does not have a position on the lane where it is registered
     */
    protected final boolean enoughSpace(final Lane<?, ?> generatorLane, final double genPosSI, final double carLengthSI)
    {
        double lengthSI = generatorLane.getLength().getSI();
        double frontNew = (genPosSI + carLengthSI) / lengthSI;
        double rearNew = genPosSI / lengthSI;

        try
        {
            // test for overlap with other GTUs
            for (LaneBasedGTU<?> gtu : generatorLane.getGtuList())
            {
                double frontGTU;
                frontGTU = gtu.fractionalPosition(generatorLane, gtu.getFront());
                double rearGTU = gtu.fractionalPosition(generatorLane, gtu.getRear());
                if ((frontNew >= rearGTU && frontNew <= frontGTU) || (rearNew >= rearGTU && rearNew <= frontGTU)
                    || (frontGTU >= rearNew && frontGTU <= frontNew) || (rearGTU >= rearNew && rearGTU <= frontNew))
                {
                    System.err.println("Generator overlap with GTU " + gtu);
                    return false;
                }
            }
        }
        catch (RemoteException | NetworkException exception)
        {
            exception.printStackTrace();
        }
        return true;
    }

}
