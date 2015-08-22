package nl.grontmij.smarttraffic.lane;

import java.awt.Color;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.naming.NamingException;

import nl.tudelft.simulation.dsol.SimRuntimeException;

import org.opentrafficsim.core.car.LaneBasedIndividualCar;
import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.core.gtu.animation.DefaultCarAnimation;
import org.opentrafficsim.core.gtu.animation.IDGTUColorer;
import org.opentrafficsim.core.gtu.following.IDMPlus;
import org.opentrafficsim.core.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.core.gtu.lane.changing.Egoistic;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.lane.AbstractSensor;
import org.opentrafficsim.core.network.lane.Lane;
import org.opentrafficsim.core.network.route.CompleteLaneBasedRouteNavigator;
import org.opentrafficsim.core.network.route.CompleteRoute;
import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.unit.SpeedUnit;
import org.opentrafficsim.core.unit.TimeUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;

/**
 * This is a sensor that detects GTU's on a lane, and is able to pass information by sending a pulse with a time stamp.
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Jan 1, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class StopLineLane extends AbstractSensor
{
    /** */
    private static final long serialVersionUID = 20141231L;

    /** color of the stop line, with the default color GREEN. */
    private Color colorTrafficLight = Color.GREEN;

    private HashMap<DoubleScalar.Abs<TimeUnit>, Long> mapStopTrafficState = new HashMap<DoubleScalar.Abs<TimeUnit>, Long>();

    /** The blocking car. */
    private LaneBasedIndividualCar stopGTU = null;

    private TrafficLight trafficLight;

    private OTSDEVSSimulatorInterface simulator;

    /**
     * Place a sensor that is triggered with the back of the GTU one ulp (see <code>Math.ulp(double d)</code>) before the end of
     * the lane to make sure it will always be triggered, independent of the algorithm used to move the GTU.
     * @param lane The lane for which this is a sensor.
     * @param longitudinalPositionFromEnd longitudinal position from the end TODO change for position, not from the end.
     */
    public StopLineLane(final Lane lane, final DoubleScalar.Rel<LengthUnit> longitudinalPositionFromEnd,
        final OTSSimulatorInterface simulator)
    {
        super(lane, longitudinalPositionFromEnd, RelativePosition.FRONT, "STOPLINE@" + lane.toString(), simulator);
    }

    public StopLineLane(String name,final Lane lane, final DoubleScalar.Rel<LengthUnit> longitudinalPosition, 
        OTSDEVSSimulatorInterface simulator)
    {
        super(lane, longitudinalPosition, RelativePosition.FRONT, name, simulator);
        try
        {
            this.trafficLight = new TrafficLight(name, this.getLane(), this.getLongitudinalPosition(), simulator);
        }
        catch (RemoteException | GTUException | NetworkException | NamingException e)
        {
            e.printStackTrace();
        }

    }

    /**
     * {@inheritDoc} <br>
     * For this method, we assume that the right sensor triggered this method. In this case the sensor that indicates the front
     * of the GTU. The code triggering the sensor therefore has to do the checking for sensor type.
     */
    @Override
    public final void trigger(final LaneBasedGTU gtu)
    {
        try
        {
            System.out.println(gtu.getSimulator().getSimulatorTime().get() + ": detecting " + gtu
                + " passing stop line at lane " + getLane());
        }
        catch (RemoteException exception)
        {
            exception.printStackTrace();
        }
    }

    /**
     * Set up the block.
     * @param simulator the simulator for the stop GTU
     * @throws RemoteException on communications failure
     */
    protected final void createStopGTU(final OTSDEVSSimulatorInterface simulator) throws RemoteException
    {
        /** Type of all GTUs. */
        try
        {
            Map<Lane, DoubleScalar.Rel<LengthUnit>> initialPositions =
                new LinkedHashMap<Lane, DoubleScalar.Rel<LengthUnit>>();
            initialPositions.put(this.getLane(), this.getLongitudinalPosition());
            this.stopGTU =
                new LaneBasedIndividualCar("999999", GTUType.makeGTUType("CAR"), new IDMPlus(), new Egoistic(),
                    initialPositions, new DoubleScalar.Abs<SpeedUnit>(0, SpeedUnit.KM_PER_HOUR),
                    new DoubleScalar.Rel<LengthUnit>(1, LengthUnit.METER), new DoubleScalar.Rel<LengthUnit>(1.8,
                        LengthUnit.METER), new DoubleScalar.Abs<SpeedUnit>(0, SpeedUnit.KM_PER_HOUR),
                    new CompleteLaneBasedRouteNavigator(new CompleteRoute("")), simulator, DefaultCarAnimation.class,
                    new IDGTUColorer());
        }
        catch (RemoteException | SimRuntimeException | NamingException | NetworkException | GTUException exception)
        {
            exception.printStackTrace();
        }
    }

    public TrafficLight getTrafficLight()
    {
        return trafficLight;
    }

    public HashMap<DoubleScalar.Abs<TimeUnit>, Long> getMapStopTrafficState()
    {
        return mapStopTrafficState;
    }

    public void setMapStopTrafficState(HashMap<DoubleScalar.Abs<TimeUnit>, Long> mapStopTrafficState)
    {
        this.mapStopTrafficState = mapStopTrafficState;
    }

    public void addMapStopTrafficState(DoubleScalar.Abs<TimeUnit> timeNow, long status)
    {
        this.mapStopTrafficState.put(timeNow, status);
    }

    /**
     * Remove the block.
     */
    protected final void removeStopGTU()
    {
        this.stopGTU.destroy();
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public String toString()
    {
        return "StopLineAtLane [getLane()=" + this.getLane() + ", getLongitudinalPosition()="
            + this.getLongitudinalPosition() + ", getPositionType()=" + this.getPositionType() + "]";
    }

    public Color getColorTrafficLight()
    {
        return colorTrafficLight;
    }

    public void setColorTrafficLight(Color colorTrafficLight)
    {
        this.colorTrafficLight = colorTrafficLight;
    }

}
