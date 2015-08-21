package nl.grontmij.smarttraffic.lane;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.naming.NamingException;

import nl.tudelft.simulation.dsol.SimRuntimeException;

import org.opentrafficsim.core.car.LaneBasedIndividualCar;
import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.gtu.animation.DefaultCarAnimation;
import org.opentrafficsim.core.gtu.animation.GTUColorer;
import org.opentrafficsim.core.gtu.animation.IDGTUColorer;
import org.opentrafficsim.core.gtu.following.GTUFollowingModel;
import org.opentrafficsim.core.gtu.following.IDMPlus;
import org.opentrafficsim.core.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.core.gtu.lane.changing.Egoistic;
import org.opentrafficsim.core.gtu.lane.changing.LaneChangeModel;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.lane.Lane;
import org.opentrafficsim.core.network.route.CompleteRoute;
import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.unit.SpeedUnit;
import org.opentrafficsim.core.unit.TimeUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar.Abs;

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
        this.gtuFollowingModel = new GTMIDMPlus();
        this.laneChangeModel = new GTMLaneChangeModel();
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
                if (entryPulse.getValue() == this.generateCar)
                {
                    DoubleScalar.Abs<TimeUnit> when = entryPulse.getKey();
                    try
                    {
                        this.simulator
                            .scheduleEventAbs(when, this, this, "generateCar", new Object[]{lane, initialPosition});
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
     * Generate one car and re-schedule this method if there is no space.
     */
    protected final void generateCar(Lane<?, ?> lane, DoubleScalar.Rel<LengthUnit> initialPosition)
    {
        // is there enough space?
        Lane nextLane = lane.nextLanes().iterator().next();
        double genSpeedSI = Math.min(lane.getSpeedLimit().getSI(), nextLane.getSpeedLimit().getSI());
        if (!enoughSpace(lane, initialPosition.getSI(), this.lengthCar, genSpeedSI))
        {
            try
            {
                this.simulator.scheduleEventRel(new DoubleScalar.Rel<TimeUnit>(0.25, TimeUnit.SECOND), this, this,
                    "generateCar", new Object[]{lane, initialPosition});
                return;
            }
            catch (RemoteException | SimRuntimeException exception)
            {
                exception.printStackTrace();
            }
        }

        Map<Lane<?, ?>, DoubleScalar.Rel<LengthUnit>> initialPositions =
                new LinkedHashMap<Lane<?, ?>, DoubleScalar.Rel<LengthUnit>>();
        initialPositions.put(lane, initialPosition);
        DoubleScalar.Abs<SpeedUnit> initialSpeed = new DoubleScalar.Abs<SpeedUnit>(genSpeedSI, SpeedUnit.SI); 
        DoubleScalar.Abs<SpeedUnit> maxSpeed = new DoubleScalar.Abs<SpeedUnit>(GTM.MAXSPEED, SpeedUnit.KM_PER_HOUR);
        if (initialPosition.getSI() + this.lengthCar > lane.getLength().getSI())
        {
            // also register on next lane.
            if (lane.nextLanes().size() == 0 || lane.nextLanes().size() > 1)
            {
                System.err.println("lane.nextLanes().size() == 0 || lane.nextLanes().size() > 1");
                System.exit(-1);
            }
            DoubleScalar.Rel<LengthUnit> nextPos =
                new DoubleScalar.Rel<LengthUnit>(initialPosition.getSI() - lane.getLength().getSI(), LengthUnit.METER);
            initialPositions.put(nextLane, nextPos);
        }
        CompleteRoute straightRouteAB;
        String linkName = lane.getParentLink().getId().toString();
        if (linkName.contains("a_in") || linkName.endsWith("a"))
        {
            straightRouteAB = this.routes.get("A");
        }
        else if (linkName.contains("b_in") || linkName.endsWith("b"))
        {
            straightRouteAB = this.routes.get("B");
        }
        else
        {
            System.err.println("generateCar - lane " + lane + ", not clear whether on A or B side");
            straightRouteAB = null;
        }
        StraightRouteNavigator routeNavigatorAB = new StraightRouteNavigator(straightRouteAB, lane.getParentLink());
        try
        {
            DoubleScalar.Rel<LengthUnit> vehicleLength = new DoubleScalar.Rel<LengthUnit>(this.lengthCar, LengthUnit.METER);
            new LaneBasedIndividualCar<Integer>(++this.carsCreated, this.gtuType, this.gtuFollowingModel,
                this.laneChangeModel, initialPositions, initialSpeed, vehicleLength, new DoubleScalar.Rel<LengthUnit>(
                    2.0, LengthUnit.METER), maxSpeed, routeNavigatorAB,
                this.simulator, /*DefaultCarAnimation.class*/null, this.gtuColorer);
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
    public static final boolean enoughSpace(final Lane<?, ?> generatorLane, final double genPosSI, final double carLengthSI,
        final double genSpeedSI)
    {
        // assume a=2 m/s2
        // safety time = 1.2 sec when driving. distance = genSpeedSI * 1.2.
        // safety distance = 3 m.
        double t = genSpeedSI / 2.0; // t = V0 / a.
        double brakeDistanceSI = genSpeedSI * t - 0.5 * 2 * t * t; // xt = V0 . t - 0.5 . a . t^2
        brakeDistanceSI += Math.max(3.0, genSpeedSI * 1.2);
        
        double laneLengthSI = generatorLane.getLength().getSI();
        double frontNew = (genPosSI + carLengthSI) / laneLengthSI;
        double rearNew = genPosSI / laneLengthSI;
        
        Lane<?, ?> nextLane = generatorLane.nextLanes().iterator().next();
        double frontNextNewSI = genPosSI + carLengthSI - laneLengthSI + brakeDistanceSI;
        double rearNextNewSI = genPosSI - laneLengthSI;
        
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
                    return false;
                }
            }
            for (LaneBasedGTU<?> gtu : nextLane.getGtuList())
            {
                double frontGTU;
                frontGTU = gtu.position(nextLane, gtu.getFront()).getSI();
                double rearGTU = gtu.position(nextLane, gtu.getRear()).getSI();
                if ((frontNextNewSI >= rearGTU && frontNextNewSI <= frontGTU) || (rearNextNewSI >= rearGTU && rearNextNewSI <= frontGTU)
                    || (frontGTU >= rearNextNewSI && frontGTU <= frontNextNewSI) || (rearGTU >= rearNextNewSI && rearGTU <= frontNextNewSI))
                {
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
