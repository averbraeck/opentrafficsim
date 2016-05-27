package org.opentrafficsim.road.network.factory.opendrive.communicationRTI;

import java.util.Set;

import javax.naming.NamingException;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.language.d3.DirectedPoint;

import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.OTSNetwork;
import org.opentrafficsim.road.gtu.lane.LaneBasedIndividualGTU;
import org.opentrafficsim.road.gtu.lane.perception.LanePerceptionFull;
import org.opentrafficsim.road.gtu.strategical.LaneBasedStrategicalPlanner;
import org.opentrafficsim.road.network.lane.DirectedLanePosition;

/** */
public class RTICars extends LaneBasedIndividualGTU
{

    /** */
    private static final long serialVersionUID = 1L;

    /** */
    DirectedPoint current = new DirectedPoint(0, 0, 0, 0, 0, 0);

    /**
     * @param valueOf car id
     * @param carType GTU type
     * @param lanepositionSet lanes for registration
     * @param speed initial car speed
     * @param carLength car length
     * @param width car width
     * @param maxSpeed maximum velocity of the car
     * @param simulator the simulator
     * @param sPlanner the strategical planner unit
     * @param perception the perception unit
     * @param network the network on which the car will be registered
     * @throws GTUException on GTU inconsistency
     * @throws SimRuntimeException when operational plan event cannot be scheduled
     * @throws NetworkException on network inconsistency
     * @throws NamingException when animation cannot be registered
     * @throws OperationalPlanException when operational plan cannot be constructed or executed
     * @throws OTSGeometryException when position cannot be determined
     */
    public RTICars(String valueOf, GTUType carType, Set<DirectedLanePosition> lanepositionSet, Speed speed,
        Length carLength, Length width, Speed maxSpeed, OTSDEVSSimulatorInterface simulator,
        LaneBasedStrategicalPlanner sPlanner, LanePerceptionFull perception, final OTSNetwork network)
        throws NamingException, NetworkException, SimRuntimeException, GTUException, OperationalPlanException,
        OTSGeometryException
    {
        super(valueOf, carType, lanepositionSet, speed, carLength, width, maxSpeed, simulator, sPlanner, perception,
            network);

        this.current = this.getOperationalPlan().getLocation(simulator.getSimulatorTime().getTime());
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public DirectedPoint getLocation()
    {
        double x = this.current.x + (0.01 * (Math.cos(this.current.getRotZ())));
        double y = this.current.y + (0.01 * (Math.sin(this.current.getRotZ())));

        this.current.setX(x);
        this.current.setY(y);

        return this.current;
    }
}
