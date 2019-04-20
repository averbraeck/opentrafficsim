package org.opentrafficsim.road.network.factory.rti.communication;

import javax.naming.NamingException;

import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.road.gtu.lane.LaneBasedIndividualGTU;
import org.opentrafficsim.road.network.OTSRoadNetwork;

import nl.tudelft.simulation.language.d3.DirectedPoint;

/** */
public class RTICar extends LaneBasedIndividualGTU
{

    /** */
    private static final long serialVersionUID = 1L;

    /** */
    private DirectedPoint current = new DirectedPoint(0, 0, 0, 0, 0, 0);

    /**
     * @param valueOf String; car id
     * @param carType GTUType; GTU type
     * @param carLength Length; car length
     * @param width Length; car width
     * @param maxSpeed Speed; maximum speed of the car
     * @param simulator OTSSimulatorInterface; the simulator
     * @param network OTSRoadNetwork; the network on which the car will be registered
     * @throws GTUException on GTU inconsistency
     * @throws NamingException when animation cannot be registered
     * @throws OperationalPlanException when plan retrieval fails
     */
    public RTICar(final String valueOf, final GTUType carType, final Length carLength, final Length width, final Speed maxSpeed,
            final OTSSimulatorInterface simulator, final OTSRoadNetwork network)
            throws NamingException, GTUException, OperationalPlanException
    {
        super(valueOf, carType, carLength, width, maxSpeed, carLength.multiplyBy(0.5), simulator, network);
        this.current = this.getOperationalPlan().getLocation(simulator.getSimulatorTime());
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
