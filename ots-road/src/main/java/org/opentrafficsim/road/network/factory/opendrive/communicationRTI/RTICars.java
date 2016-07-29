package org.opentrafficsim.road.network.factory.opendrive.communicationRTI;

import javax.naming.NamingException;

import nl.tudelft.simulation.language.d3.DirectedPoint;

import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.core.network.OTSNetwork;
import org.opentrafficsim.road.gtu.lane.LaneBasedIndividualGTU;

/** */
public class RTICars extends LaneBasedIndividualGTU
{

    /** */
    private static final long serialVersionUID = 1L;

    /** */
    private DirectedPoint current = new DirectedPoint(0, 0, 0, 0, 0, 0);

    /**
     * @param valueOf car id
     * @param carType GTU type
     * @param carLength car length
     * @param width car width
     * @param maxSpeed maximum speed of the car
     * @param simulator the simulator
     * @param network the network on which the car will be registered
     * @throws GTUException on GTU inconsistency
     * @throws NamingException when animation cannot be registered
     * @throws OperationalPlanException when plan retrieval fails
     */
    public RTICars(final String valueOf, final GTUType carType, final Length carLength, final Length width, final Speed maxSpeed,
            final OTSDEVSSimulatorInterface simulator, final OTSNetwork network)
            throws NamingException, GTUException, OperationalPlanException
    {
        super(valueOf, carType, carLength, width, maxSpeed, simulator, network);
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
