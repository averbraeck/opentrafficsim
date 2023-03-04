package org.opentrafficsim.road.network.lane.object.detector;

import java.rmi.RemoteException;

import org.djunits.unit.LengthUnit;
import org.djunits.value.vdouble.scalar.Length;
import org.djutils.event.EventType;
import org.djutils.exceptions.Throw;
import org.djutils.metadata.MetaData;
import org.djutils.metadata.ObjectDescriptor;
import org.opentrafficsim.core.dsol.OtsSimulatorInterface;
import org.opentrafficsim.core.geometry.DirectedPoint;
import org.opentrafficsim.core.geometry.OtsGeometryException;
import org.opentrafficsim.core.geometry.OtsLine3d;
import org.opentrafficsim.core.geometry.OtsPoint3d;
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
 * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 */
public abstract class LaneDetector extends AbstractLaneBasedObject
        implements Comparable<LaneDetector>, LaneBasedObject, Detector
{
    /** */
    private static final long serialVersionUID = 20141231L;

    /** The relative position of the vehicle that triggers the detector. */
    private final RelativePosition.TYPE positionType;

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
                    new ObjectDescriptor("Position", "Relative GTU position that triggered", RelativePosition.TYPE.class)));

    /** Default elevation of a detector; if the lane is not at elevation 0; this value is probably far off. */
    public static final Length DEFAULT_DETECTOR_ELEVATION = new Length(0.1, LengthUnit.METER);

    /**
     * Create a detector on a lane at a position on that lane.
     * @param id String; the id of the detector.
     * @param lane Lane; the lane for which this is a detector.
     * @param longitudinalPosition Length; the position (between 0.0 and the length of the Lane) of the detector on the design
     *            line of the lane.
     * @param positionType RelativePosition.TYPE; the relative position type (e.g., FRONT, BACK) of the vehicle that triggers
     *            the detector.
     * @param simulator OtsSimulatorInterface; the simulator (needed to generate the animation).
     * @param geometry OtsLine3d; the geometry of the object, which provides its location and bounds as well
     * @param elevation Length; elevation of the detector
     * @param detectorType DetectorType; detector type.
     * @throws NetworkException when the position on the lane is out of bounds
     */
    @SuppressWarnings("checkstyle:parameternumber")
    public LaneDetector(final String id, final Lane lane, final Length longitudinalPosition,
            final RelativePosition.TYPE positionType, final OtsSimulatorInterface simulator, final OtsLine3d geometry,
            final Length elevation, final DetectorType detectorType) throws NetworkException
    {
        super(id, lane, longitudinalPosition, geometry, elevation);
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
     * @param id String; the id of the detector.
     * @param lane Lane; the lane for which this is a detector.
     * @param longitudinalPosition Length; the position (between 0.0 and the length of the Lane) of the detector on the design
     *            line of the lane.
     * @param positionType RelativePosition.TYPE; the relative position type (e.g., FRONT, BACK) of the vehicle that triggers
     *            the detector.
     * @param simulator OtsSimulatorInterface; the simulator (needed to generate the animation).
     * @param geometry OtsLine3d; the geometry of the object, which provides its location and bounds as well
     * @param detectorType DetectorType; detector type.
     * @throws NetworkException when the position on the lane is out of bounds
     */
    public LaneDetector(final String id, final Lane lane, final Length longitudinalPosition,
            final RelativePosition.TYPE positionType, final OtsSimulatorInterface simulator, final OtsLine3d geometry,
            final DetectorType detectorType) throws NetworkException
    {
        this(id, lane, longitudinalPosition, positionType, simulator, geometry, LaneDetector.DEFAULT_DETECTOR_ELEVATION,
                detectorType);
    }

    /**
     * Create a new Detector on a lane at a position on that lane at elevation <code>Detector.DEFAULT_DETECTOR_ELEVATION</code>
     * and default geometry.
     * @param id String; the id of the new Detector
     * @param lane Lane; the lane on which the new Detector is positioned
     * @param longitudinalPosition Length; the position (between 0.0 and the length of the Lane) of the detector on the design
     *            line of the lane
     * @param positionType RelativePosition.TYPE; the relative position type (e.g., FRONT, BACK) of the vehicle that triggers
     *            the detector.
     * @param simulator OtsSimulatorInterface; the simulator (needed to generate the animation).
     * @param detectorType DetectorType; detector type.
     * @throws NetworkException when the position on the lane is out of bounds
     */
    public LaneDetector(final String id, final Lane lane, final Length longitudinalPosition,
            final RelativePosition.TYPE positionType, final OtsSimulatorInterface simulator, final DetectorType detectorType)
            throws NetworkException
    {
        this(id, lane, longitudinalPosition, positionType, simulator, makeGeometry(lane, longitudinalPosition, 0.9),
                detectorType);
    }

    /**
     * Make a geometry perpendicular to the center line of the lane with a length of 90% of the width of the lane.
     * @param lane Lane; the lane for which to make a perpendicular geometry
     * @param longitudinalPosition Length; the position on the lane
     * @param relativeWidth double; lane width to use
     * @return an OtsLine3d that describes the line
     * @throws NetworkException in case the detector point on the center line of the lane cannot be found
     */
    protected static OtsLine3d makeGeometry(final Lane lane, final Length longitudinalPosition, final double relativeWidth)
            throws NetworkException
    {
        try
        {
            double w50 = lane.getWidth(longitudinalPosition).si * 0.5 * relativeWidth;
            DirectedPoint c = lane.getCenterLine().getLocation(longitudinalPosition);
            double a = c.getRotZ();
            OtsPoint3d p1 = new OtsPoint3d(c.x + w50 * Math.cos(a + Math.PI / 2), c.y - w50 * Math.sin(a + Math.PI / 2), c.z);
            OtsPoint3d p2 = new OtsPoint3d(c.x - w50 * Math.cos(a + Math.PI / 2), c.y + w50 * Math.sin(a + Math.PI / 2), c.z);
            return new OtsLine3d(p1, p2);
        }
        catch (OtsGeometryException exception)
        {
            throw new NetworkException(exception);
        }
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
     * @param gtu LaneBasedGtu; the GTU for which to carry out the trigger action.
     */
    public final void trigger(final LaneBasedGtu gtu)
    {
        fireTimedEvent(LaneDetector.DETECTOR_TRIGGER_EVENT, new Object[] {getId(), this, gtu, this.positionType},
                getSimulator().getSimulatorTime());
        triggerResponse(gtu);
    }

    /**
     * Implementation of the response to a trigger of this detector by a GTU.
     * @param gtu LaneBasedGtu; the lane based GTU that triggered this detector.
     */
    protected abstract void triggerResponse(LaneBasedGtu gtu);

    /**
     * Returns the relative position type of the vehicle (e.g., FRONT, BACK) that triggers the detector.
     * @return the relative position type of the vehicle (e.g., FRONT, BACK) that triggers the detector.
     */
    public final RelativePosition.TYPE getPositionType()
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
     * @param gtuType GtuType; GTU type.
     * @return boolean; whether this Detector can detector GTUs of the given type.
     */
    public final boolean isCompatible(final GtuType gtuType)
    {
        return this.getType().isCompatible(gtuType);
    }

    /** {@inheritDoc} */
    @Override
    public double getZ() throws RemoteException
    {
        return -0.0002;
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
