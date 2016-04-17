package nl.grontmij.smarttraffic.model;

import org.djunits.value.vdouble.scalar.Length.Rel;
import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.gtu.RelativePosition.TYPE;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.network.lane.AbstractSensor;
import org.opentrafficsim.road.network.lane.Lane;

/**
 */
public class GenerateSensor extends AbstractSensor
{
    /** */
    private static final long serialVersionUID = 1L;

    /**
     * @param lane
     * @param longitudinalPosition
     * @param positionType
     * @param name
     * @param simulator
     */
    public GenerateSensor(Lane lane, Rel longitudinalPosition, TYPE positionType, String name,
        OTSDEVSSimulatorInterface simulator)
    {
        super(lane, longitudinalPosition, positionType, name, simulator);
    }

    /** {@inheritDoc} */
    @Override
    public void trigger(LaneBasedGTU gtu)
    {
        //
    }

}
