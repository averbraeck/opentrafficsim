package org.opentrafficsim.road.network.lane;

import java.util.HashSet;
import java.util.Set;

import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;

/**
 * This traffic light sensor reports whether it whether any GTUs are within its area. The area is a sub-section of a Lane. This
 * traffic sensor does <b>not</b> report the total number of GTUs within the area; only whether that number is zero or non-zero.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Oct 27, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class TrafficLightSensor
{
    /** The sensor that detects when the front of a GTU starts to cover the sensor area. */
    private final FlankSensor upSensor;

    /** The sensor that detects when the rear of a GTU leaves the sensor area. */
    private final FlankSensor downSensor;

    /** GTUs detected by the upSensor, but not yet removed by the downSensor. */
    private final Set<LaneBasedGTU> currentGTUs = new HashSet<>();

    /**
     * @param id String; id of this sensor
     * @param lane Lane; the lane of this sensor
     * @param position Length; the position where the front of LaneBasedGTUs is detected by this sensor
     * @param length Length; the distance after position where the rear of LaneBasedGTUs is detected by this sensor
     * @param simulator OTSDEVSSimulatorInterface; the simulator
     * @throws NetworkException when the network is inconsistent.
     */
    public TrafficLightSensor(final String id, final Lane lane, final Length position, final Length length,
            final OTSDEVSSimulatorInterface simulator) throws NetworkException
    {
        this.upSensor = new FlankSensor(id + ".UP", lane, position, simulator, true, this.currentGTUs);
        this.downSensor = new FlankSensor(id + ".DN", lane, position.plus(length), simulator, false, this.currentGTUs);
    }

    // TODO figure out how to detect GTUs that leave the sensor sideways

}

/**
 * Sub-sensor of a traffic light sensor.
 */
class FlankSensor extends AbstractSensor
{
    /** */
    private static final long serialVersionUID = 20161027L;

    /** The current set of GTUs covering the sensor. */
    private final Set<LaneBasedGTU> currentGTUs;

    /** If true; this sensor triggers on the front of a GTU; if false; it triggers on the rear of a GTU. */
    private final boolean up;

    /**
     * Construct a new FlankSensor.
     * @param id String; name of the sensor
     * @param lane Lane; lane on which the sensor is positioned
     * @param position Length; position from the start of the lane
     * @param simulator OTSDEVSSimulatorInterface; the simulator
     * @param up boolean; if true; this sensor will sense the front of GTUs, if false; this sensor will sens the rear of GTUs
     * @param currentGTUs Set&lt;LaneBasedGTU&gt;; Set where the current set of GTUs covering the sensor is administrated
     * @throws NetworkException if the network is inconsistent
     */
    FlankSensor(final String id, final Lane lane, final Length position, final OTSDEVSSimulatorInterface simulator,
            final boolean up, final Set<LaneBasedGTU> currentGTUs) throws NetworkException
    {
        super(id, lane, position, up ? RelativePosition.FRONT : RelativePosition.REAR, simulator);
        this.currentGTUs = currentGTUs;
        this.up = up;
    }

    /** {@inheritDoc} */
    @Override
    protected void triggerResponse(final LaneBasedGTU gtu)
    {
        if (this.up)
        {
            if (this.currentGTUs.size() == 0)
            {
                // TODO fire a sensor becomes occupied event
            }
            this.currentGTUs.add(gtu);
        }
        else
        {
            this.currentGTUs.remove(gtu);
            if (this.currentGTUs.size() == 0)
            {
                // TODO fire a sensor becomes unoccupied event
            }
        }
    }

}
