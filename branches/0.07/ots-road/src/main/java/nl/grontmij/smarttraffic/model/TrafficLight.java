package nl.grontmij.smarttraffic.model;

import javax.naming.NamingException;

import nl.tudelft.simulation.dsol.SimRuntimeException;

import org.djunits.value.vdouble.scalar.Length.Rel;
import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.OTSNetwork;
import org.opentrafficsim.road.gtu.lane.object.AbstractTrafficLight;
import org.opentrafficsim.road.network.lane.Lane;

/**
 */
public class TrafficLight extends AbstractTrafficLight
{
    /** */
    private static final long serialVersionUID = 1L;

    /**
     * @param name
     * @param lane
     * @param position
     * @param simulator
     * @param network
     * @throws GTUException
     * @throws NetworkException
     * @throws NamingException
     * @throws SimRuntimeException
     * @throws OTSGeometryException
     */
    public TrafficLight(String name, Lane lane, Rel position, OTSDEVSSimulatorInterface simulator, OTSNetwork network)
        throws GTUException, NetworkException, NamingException, SimRuntimeException, OTSGeometryException
    {
        super(name, lane, position, simulator, network);
    }

}
