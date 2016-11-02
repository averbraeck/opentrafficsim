package org.opentrafficsim.road.network.lane.object.sensor;

import java.util.HashSet;
import java.util.Set;

import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.network.lane.CrossSectionElement;
import org.opentrafficsim.road.network.lane.Lane;

import nl.tudelft.simulation.language.Throw;

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

    /** The distance between the up and down sensor. */
    private final Length length;
    
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
        this.length = length;
    }

    // TODO figure out how to detect GTUs that leave the sensor sideways

    /**
     * Clone the TrafficLightSensor for e.g., copying a network.
     * @param newCSE the new cross section element to which the clone belongs
     * @param newSimulator the new simulator for this network
     * @param animation whether to (re)create animation or not
     * @return a clone of this object
     * @throws NetworkException in case the cloning fails
     */
    @SuppressWarnings("checkstyle:designforextension")
    public TrafficLightSensor clone(final CrossSectionElement newCSE, final OTSSimulatorInterface newSimulator,
            final boolean animation) throws NetworkException
    {
        Throw.when(!(newCSE instanceof Lane), NetworkException.class, "sensors can only be cloned for Lanes");
        Throw.when(!(newSimulator instanceof OTSDEVSSimulatorInterface), NetworkException.class,
                "simulator should be a DEVSSimulator");
        String newId = this.upSensor.getId().substring(0, this.upSensor.getId().length() - 4);
        return new TrafficLightSensor(newId, (Lane) newCSE, this.upSensor.getLongitudinalPosition(), this.length,
                (OTSDEVSSimulatorInterface) newSimulator);
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "TrafficLightSensor [upSensor=" + this.upSensor + ", downSensor=" + this.downSensor + ", length=" + this.length
                + ", currentGTUs=" + this.currentGTUs + "]";
    }

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

    /** {@inheritDoc} */
    @Override
    public FlankSensor clone(final CrossSectionElement newCSE, final OTSSimulatorInterface newSimulator,
            final boolean animation) throws NetworkException
    {
        Throw.when(!(newCSE instanceof Lane), NetworkException.class, "sensors can only be cloned for Lanes");
        Throw.when(!(newSimulator instanceof OTSDEVSSimulatorInterface), NetworkException.class,
                "simulator should be a DEVSSimulator");
        return new FlankSensor(getId(), (Lane) newCSE, getLongitudinalPosition(), (OTSDEVSSimulatorInterface) newSimulator,
                this.up, new HashSet<LaneBasedGTU>());
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "FlankSensor [currentGTUs=" + this.currentGTUs + ", up=" + this.up + "]";
    }

}
