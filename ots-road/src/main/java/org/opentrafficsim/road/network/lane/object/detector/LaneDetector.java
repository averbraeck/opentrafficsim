package org.opentrafficsim.road.network.lane.object.detector;

import org.djunits.unit.LengthUnit;
import org.djunits.value.vdouble.scalar.Length;
import org.djutils.draw.line.PolyLine2d;
import org.djutils.event.EventType;
import org.djutils.exceptions.Throw;
import org.djutils.metadata.MetaData;
import org.djutils.metadata.ObjectDescriptor;
import org.opentrafficsim.core.dsol.OtsSimulatorInterface;
import org.opentrafficsim.core.gtu.GtuType;
import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.object.AbstractLaneBasedObject;
import org.opentrafficsim.road.network.lane.object.LaneBasedObject;

/**
 * A detector is a lane-based object that can be triggered by a relative position of the GTU (e.g., front, back) when that
 * relative position passes over the detector location on the lane.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public abstract class LaneDetector extends AbstractLaneBasedObject
        implements Comparable<LaneDetector>, LaneBasedObject, Detector
{
    /** */
    private static final long serialVersionUID = 20141231L;

    /** The relative position of the vehicle that triggers the detector. */
    private final RelativePosition.Type positionType;

    /** The simulator for being able to generate an animation. */
    private final OtsSimulatorInterface simulator;

    /** Detector type. */
    private final DetectorType detectorType;

    /**
     * The <b>timed</b> event type for pub/sub indicating the triggering of a Detector on a lane. <br>
     * Payload: Object[] {String detectorId, Detector detector, LaneBasedGtu gtu, RelativePosition.TYPE relativePosition}
     */
    public static final EventType DETECTOR_TRIGGER_EVENT = new EventType("DETECTOR.TRIGGER",
            new MetaData("Detector trigger", "Detector is triggered",
                    new ObjectDescriptor("Detector id", "Id of the detector", String.class),
                    new ObjectDescriptor("Detector", "Detector itself", Detector.class),
                    new ObjectDescriptor("GTU", "Triggering GTU", LaneBasedGtu.class),
                    new ObjectDescriptor("Position", "Relative GTU position that triggered", RelativePosition.Type.class)));

    /** Default elevation of a detector; if the lane is not at elevation 0; this value is probably far off. */
    public static final Length DEFAULT_DETECTOR_ELEVATION = new Length(0.1, LengthUnit.METER);

    /**
     * Create a detector on a lane at a position on that lane.
     * @param id the id of the detector.
     * @param lane the lane for which this is a detector.
     * @param longitudinalPosition the position (between 0.0 and the length of the Lane) of the detector on the design line of
     *            the lane.
     * @param positionType RelativePosition.TYPE; the relative position type (e.g., FRONT, BACK) of the vehicle that triggers
     *            the detector.
     * @param simulator the simulator (needed to generate the animation).
     * @param line the line of the object, which provides its location and bounds as well
     * @param elevation elevation of the detector
     * @param detectorType detector type.
     * @throws NetworkException when the position on the lane is out of bounds
     */
    @SuppressWarnings("checkstyle:parameternumber")
    public LaneDetector(final String id, final Lane lane, final Length longitudinalPosition,
            final RelativePosition.Type positionType, final OtsSimulatorInterface simulator, final PolyLine2d line,
            final Length elevation, final DetectorType detectorType) throws NetworkException
    {
        super(id, lane, longitudinalPosition, line, elevation);
        Throw.whenNull(simulator, "simulator is null");
        Throw.whenNull(positionType, "positionType is null");
        Throw.whenNull(id, "id is null");
        Throw.whenNull(detectorType, "detectorType is null");
        this.positionType = positionType;
        this.simulator = simulator;
        this.detectorType = detectorType;

        init();

        getLane().addDetector(this); // Implements OTS-218
    }

    /**
     * Create a detector on a lane at a position on that lane at elevation <code>Detector.DEFAULT_DETECTOR_ELEVATION</code>.
     * @param id the id of the detector.
     * @param lane the lane for which this is a detector.
     * @param longitudinalPosition the position (between 0.0 and the length of the Lane) of the detector on the design line of
     *            the lane.
     * @param positionType RelativePosition.TYPE; the relative position type (e.g., FRONT, BACK) of the vehicle that triggers
     *            the detector.
     * @param simulator the simulator (needed to generate the animation).
     * @param line the contour of the object, which provides its location and bounds as well
     * @param detectorType detector type.
     * @throws NetworkException when the position on the lane is out of bounds
     */
    public LaneDetector(final String id, final Lane lane, final Length longitudinalPosition,
            final RelativePosition.Type positionType, final OtsSimulatorInterface simulator, final PolyLine2d line,
            final DetectorType detectorType) throws NetworkException
    {
        this(id, lane, longitudinalPosition, positionType, simulator, line, LaneDetector.DEFAULT_DETECTOR_ELEVATION,
                detectorType);
    }

    /**
     * Create a new Detector on a lane at a position on that lane at elevation <code>Detector.DEFAULT_DETECTOR_ELEVATION</code>
     * and default geometry.
     * @param id the id of the new Detector
     * @param lane the lane on which the new Detector is positioned
     * @param longitudinalPosition the position (between 0.0 and the length of the Lane) of the detector on the design line of
     *            the lane
     * @param positionType RelativePosition.TYPE; the relative position type (e.g., FRONT, BACK) of the vehicle that triggers
     *            the detector.
     * @param simulator the simulator (needed to generate the animation).
     * @param detectorType detector type.
     * @throws NetworkException when the position on the lane is out of bounds
     */
    public LaneDetector(final String id, final Lane lane, final Length longitudinalPosition,
            final RelativePosition.Type positionType, final OtsSimulatorInterface simulator, final DetectorType detectorType)
            throws NetworkException
    {
        this(id, lane, longitudinalPosition, positionType, simulator, LaneBasedObject.makeLine(lane, longitudinalPosition, 1.0),
                detectorType);
    }

    /** {@inheritDoc} */
    @Override
    public DetectorType getType()
    {
        return this.detectorType;
    }

    /**
     * Trigger an action on the GTU. Normally this is the GTU that triggered the detector. The typical call therefore is
     * <code>detector.trigger(this);</code>.
     * @param gtu the GTU for which to carry out the trigger action.
     */
    public final void trigger(final LaneBasedGtu gtu)
    {
        fireTimedEvent(LaneDetector.DETECTOR_TRIGGER_EVENT, new Object[] {getId(), this, gtu, this.positionType},
                getSimulator().getSimulatorTime());
        triggerResponse(gtu);
    }

    /**
     * Implementation of the response to a trigger of this detector by a GTU.
     * @param gtu the lane based GTU that triggered this detector.
     */
    protected abstract void triggerResponse(LaneBasedGtu gtu);

    /**
     * Returns the relative position type of the vehicle (e.g., FRONT, BACK) that triggers the detector.
     * @return the relative position type of the vehicle (e.g., FRONT, BACK) that triggers the detector.
     */
    public final RelativePosition.Type getPositionType()
    {
        return this.positionType;
    }

    /** @return The simulator. */
    public final OtsSimulatorInterface getSimulator()
    {
        return this.simulator;
    }

    /**
     * Returns whether this Detector can detector GTUs of the given type.
     * @param gtuType GTU type.
     * @return whether this Detector can detector GTUs of the given type.
     */
    public final boolean isCompatible(final GtuType gtuType)
    {
        return this.getType().isCompatible(gtuType);
    }

    /** {@inheritDoc} */
    @SuppressWarnings("checkstyle:designforextension")
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getLane() == null) ? 0 : getLane().hashCode());
        long temp;
        temp = Double.doubleToLongBits(getLongitudinalPosition().si);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        result = prime * result + ((this.positionType == null) ? 0 : this.positionType.hashCode());
        return result;
    }

    /** {@inheritDoc} */
    @SuppressWarnings({"checkstyle:needbraces", "checkstyle:designforextension"})
    @Override
    public boolean equals(final Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        LaneDetector other = (LaneDetector) obj;
        if (this.getLane() == null)
        {
            if (other.getLane() != null)
                return false;
        }
        else if (!this.getLane().equals(other.getLane()))
            return false;
        if (Double.doubleToLongBits(this.getLongitudinalPosition().si) != Double
                .doubleToLongBits(other.getLongitudinalPosition().si))
            return false;
        if (this.positionType == null)
        {
            if (other.positionType != null)
                return false;
        }
        else if (!this.positionType.equals(other.positionType))
            return false;
        return true;
    }

    /** {@inheritDoc} */
    @SuppressWarnings("checkstyle:designforextension")
    @Override
    public int compareTo(final LaneDetector o)
    {
        if (this.getLane() != o.getLane())
        {
            return this.getLane().hashCode() < o.getLane().hashCode() ? -1 : 1;
        }
        if (this.getLongitudinalPosition().si != o.getLongitudinalPosition().si)
        {
            return this.getLongitudinalPosition().si < o.getLongitudinalPosition().si ? -1 : 1;
        }
        if (!this.positionType.equals(o.getPositionType()))
        {
            return this.positionType.hashCode() < o.getPositionType().hashCode() ? -1 : 1;
        }
        if (!this.equals(o))
        {
            return this.hashCode() < o.hashCode() ? -1 : 1;
        }
        return 0;
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public String toString()
    {
        return "Detector[" + getId() + "]";
    }

}
