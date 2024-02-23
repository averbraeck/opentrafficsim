package org.opentrafficsim.road.network.lane.object.detector;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.djunits.value.vdouble.scalar.Length;
import org.djutils.draw.bounds.Bounds2d;
import org.djutils.draw.line.PolyLine2d;
import org.djutils.draw.line.Ray2d;
import org.djutils.draw.point.OrientedPoint2d;
import org.djutils.draw.point.Point2d;
import org.djutils.event.Event;
import org.djutils.event.EventListener;
import org.djutils.event.EventType;
import org.djutils.event.LocalEventProducer;
import org.djutils.exceptions.Throw;
import org.djutils.metadata.MetaData;
import org.djutils.metadata.ObjectDescriptor;
import org.opentrafficsim.core.dsol.OtsSimulatorInterface;
import org.opentrafficsim.core.geometry.OtsGeometryException;
import org.opentrafficsim.core.geometry.OtsLine2d;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.core.gtu.RelativePosition.TYPE;
import org.opentrafficsim.core.network.Network;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;
import org.opentrafficsim.road.network.lane.Lane;

/**
 * This traffic light reports whether any GTUs are within its area. The area is two sub-sections on one or two lanes. This
 * traffic does <b>not</b> report the total number of GTUs within the area; only whether that number is zero or non-zero. This
 * class does not derive from {@code Detector} as it concerns an area, not a cross-section. All sides of the 2 areas are managed
 * by 4 {@code Detector}s to capture GTU longitudinal movement, and by listening to events to capture lane changes, vehicle
 * generation, and vehicle destruction.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class TrafficLightDetector extends LocalEventProducer implements EventListener, Detector
{
    /** */
    private static final long serialVersionUID = 20161103L;

    /** Id of this TrafficLightDetector. */
    private final String id;

    /** Unique id for network. */
    private final String uniqueId;

    /** The detector that detects when a GTU enters the detector area at point A. */
    private final StartEndDetector entryA;

    /** The detector that detects when a GTU exits the detector area at point B. */
    private final StartEndDetector exitB;

    /** GTUs detected by the entrance detectors, but not yet removed by the exit detectors. */
    private final Set<LaneBasedGtu> currentGTUs = new LinkedHashSet<>();

    /** The lanes that the detector (partly) covers. */
    private final Set<Lane> lanes = new LinkedHashSet<>();

    /** The OTS network. */
    private final Network network;

    /** Type. */
    private final DetectorType type;

    /** Center location. */
    private final OrientedPoint2d location;

    /** Geometry of the detector. */
    private final PolyLine2d geometry;

    /**
     * The <b>timed</b> event type for pub/sub indicating the triggering of the entry of a NonDirectionalOccupancyDetector. <br>
     * Payload: Object[] {String detectorId}
     */
    public static final EventType TRAFFIC_LIGHT_DETECTOR_TRIGGER_ENTRY_EVENT =
            new EventType("TRAFFICLIGHTDETECTOR.TRIGGER.ENTRY",
                    new MetaData("Traffic light detector entty", "Traffic light detector was entered",
                            new ObjectDescriptor("Detector id", "Traffic light detector id", String.class)));

    /**
     * The <b>timed</b> event type for pub/sub indicating the triggering of the exit of an NonDirectionalOccupancyDetector. <br>
     * Payload: Object[] {String detectorId}
     */
    public static final EventType TRAFFIC_LIGHT_DETECTOR_TRIGGER_EXIT_EVENT = new EventType("TRAFFICLIGHTDETECTOR.TRIGGER.EXIT",
            new MetaData("Traffic light detector exit", "Traffic light detector was exited",
                    new ObjectDescriptor("Detector id", "Traffic light detector id", String.class)));

    /**
     * Construct a new traffic light detector.<br>
     * TODO Possibly provide the GtuTypes that trigger the detector as an argument for the constructor
     * @param id String; id of this detector
     * @param laneA Lane; the lane of the A detection point of this traffic light detector
     * @param positionA Length; the position of the A detection point of this traffic light detector
     * @param laneB Lane; the lane of the B detection point of this traffic light detector
     * @param positionB Length; the position of the B detection point of this traffic light detector
     * @param intermediateLanes List&lt;Lane&gt;; list of intermediate lanes
     * @param entryPosition TYPE; the position on the GTUs that trigger the entry events
     * @param exitPosition TYPE; the position on the GTUs that trigger the exit events
     * @param simulator OtsSimulatorInterface; the simulator
     * @param detectorType DetectorType; detector type.
     * @throws NetworkException when the network is inconsistent.
     */
    @SuppressWarnings("checkstyle:parameternumber")
    public TrafficLightDetector(final String id, final Lane laneA, final Length positionA, final Lane laneB,
            final Length positionB, final List<Lane> intermediateLanes, final TYPE entryPosition, final TYPE exitPosition,
            final OtsSimulatorInterface simulator, final DetectorType detectorType) throws NetworkException
    {
        Throw.whenNull(id, "id may not be null");
        this.id = id;
        this.uniqueId = UUID.randomUUID().toString() + "_" + id;
        this.type = detectorType;
        this.entryA = new StartEndDetector(id + ".entryA", laneA, positionA, entryPosition, simulator, detectorType);
        this.exitB = new StartEndDetector(id + ".exitB", laneB, positionB, exitPosition, simulator, detectorType);
        // Set up detection of GTUs that enter or leave the detector laterally or appear due to a generator or disappear due to
        // a sink
        this.lanes.add(laneA);
        this.network = laneA.getLink().getNetwork();
        if (null != intermediateLanes)
        {
            this.lanes.addAll(intermediateLanes);
        }
        this.lanes.add(laneB);
        for (Lane lane : this.lanes)
        {
            lane.addListener(this, Lane.GTU_ADD_EVENT);
            lane.addListener(this, Lane.GTU_REMOVE_EVENT);
        }
        try
        {
            OtsLine2d path;
            if (this.lanes.size() == 1)
            {
                path = laneA.getCenterLine().extract(positionA, positionB);
            }
            else
            {
                List<Point2d> pathPoints = new ArrayList<>();
                pathPoints.addAll(Arrays.asList(laneA.getCenterLine().extract(positionA, laneA.getLength()).getPoints()));
                for (Lane intermediateLane : intermediateLanes)
                {
                    pathPoints.addAll(Arrays.asList(intermediateLane.getCenterLine().getPoints()));
                }
                pathPoints.addAll(Arrays.asList(laneB.getCenterLine().extract(Length.ZERO, positionB).getPoints()));
                path = OtsLine2d.createAndCleanOtsLine2d(pathPoints);
            }
            OtsLine2d left = path.offsetLine(0.5);
            OtsLine2d right = path.offsetLine(-0.5);
            Ray2d ray = path.getLine2d().getLocationFraction(0.5);
            double dx = ray.x;
            double dy = ray.y;
            this.location = new OrientedPoint2d(dx, dy);
            List<Point2d> geometryPoints = new ArrayList<>();
            geometryPoints.add(new Point2d(right.get(0).x - dx, right.get(0).y - dy));
            for (Point2d p : left.getPoints())
            {
                geometryPoints.add(new Point2d(p.x - dx, p.y - dy));
            }
            for (Point2d p : right.reverse().getPoints())
            {
                geometryPoints.add(new Point2d(p.x - dx, p.y - dy));
            }
            this.geometry = new PolyLine2d(geometryPoints);
        }
        catch (OtsGeometryException exception)
        {
            throw new NetworkException("Points A and B may be the same.", exception);
        }
        this.network.addObject(this);
    }

    /**
     * Add a GTU to the set.
     * @param gtu LaneBasedGtu; the GTU that must be added
     */
    protected final void addGtu(final LaneBasedGtu gtu)
    {
        if (this.currentGTUs.add(gtu) && this.currentGTUs.size() == 1)
        {
            fireTimedEvent(TrafficLightDetector.TRAFFIC_LIGHT_DETECTOR_TRIGGER_ENTRY_EVENT, new Object[] {getId()},
                    getSimulator().getSimulatorTime());
        }
    }

    /**
     * Remove a GTU from the set.
     * @param gtu LaneBasedGtu; the GTU that must be removed
     */
    protected final void removeGtu(final LaneBasedGtu gtu)
    {
        if (this.currentGTUs.remove(gtu) && this.currentGTUs.size() == 0)
        {
            fireTimedEvent(TrafficLightDetector.TRAFFIC_LIGHT_DETECTOR_TRIGGER_EXIT_EVENT, new Object[] {getId()},
                    getSimulator().getSimulatorTime());
        }
    }

    /** {@inheritDoc} */
    @Override
    public final void notify(final Event event) throws RemoteException
    {
        String gtuId = (String) ((Object[]) event.getContent())[0];
        LaneBasedGtu gtu = (LaneBasedGtu) this.network.getGTU(gtuId);
        if (Lane.GTU_REMOVE_EVENT.equals(event.getType()))
        {
            if (!this.currentGTUs.contains(gtu))
            {
                return; // GTU is not currently detected; nothing to do
            }
            try
            {
                // If the detector covers only (part of) one lane, this must have triggered this event, GTU on longer on det.
                if (this.lanes.size() == 1)
                {
                    removeGtu(gtu);
                    return;
                }

                Lane lane = null;
                String laneId = (String) ((Object[]) event.getContent())[4];
                String linkId = (String) ((Object[]) event.getContent())[5];
                for (Lane detectorLane : this.lanes)
                {
                    if (detectorLane.getId().equals(laneId) && detectorLane.getLink().getId().equals(linkId))
                    {
                        lane = detectorLane;
                        break;
                    }
                }

                Set<Lane> remainingLanes = gtu.positions(gtu.getRelativePositions().get(RelativePosition.CENTER)).keySet();
                remainingLanes.retainAll(this.lanes);
                remainingLanes.remove(lane); // still in positions during this event
                if (remainingLanes.isEmpty())
                {
                    removeGtu(gtu);
                }
                // else: GTU is still in one of our lanes and we will get another GTU_REMOVE_EVENT or the GTU will trigger one
                // of our exit flank detectors or when the GTU leaves this detector laterally
                return;
            }
            catch (GtuException exception)
            {
                System.err.println("Caught GTU exception trying to get the a position");
                exception.printStackTrace();
            }
        }
        else if (Lane.GTU_ADD_EVENT.equals(event.getType()))
        {
            if (this.currentGTUs.contains(gtu))
            {
                return; // GTU is already detected; nothing to do
            }
            // Determine whether the GTU is in our range
            try
            {
                // If the detector covers only (part of) one lane, this must have triggered this event, check position on it
                if (this.lanes.size() == 1)
                {
                    Lane lane = this.lanes.iterator().next();
                    Length frontPos = gtu.position(lane, gtu.getRelativePositions().get(this.entryA.getPositionType()));
                    Length rearPos = gtu.position(lane, gtu.getRelativePositions().get(this.exitB.getPositionType()));
                    if (frontPos.gt(this.entryA.getLongitudinalPosition()) && rearPos.lt(this.exitB.getLongitudinalPosition()))
                    {
                        addGtu(gtu);
                    }
                    return;
                }

                Lane lane = null;
                String laneId = (String) ((Object[]) event.getContent())[2];
                String linkId = (String) ((Object[]) event.getContent())[3];
                for (Lane detectorLane : this.lanes)
                {
                    if (detectorLane.getId().equals(laneId) && detectorLane.getLink().getId().equals(linkId))
                    {
                        lane = detectorLane;
                    }
                }

                // If the triggering lane neither contains A nor B, it is an intermediate lane, so the GTU is on the detector
                if (!this.entryA.getLane().equals(lane) && !this.exitB.getLane().equals(lane))
                {
                    addGtu(gtu);
                    return;
                }

                // If triggering lane contains A, detector is triggered if front is beyond A (remainder of lane is all detector)
                if (this.entryA.getLane().equals(lane))
                {
                    Length frontPos =
                            gtu.position(this.entryA.getLane(), gtu.getRelativePositions().get(this.entryA.getPositionType()));
                    if (frontPos.gt(this.entryA.getLongitudinalPosition()))
                    {
                        addGtu(gtu);
                    }
                    return;
                }

                // If triggering lane contains B, detector is triggered if the rear is before B (before on lane is all detector)
                if (this.exitB.getLane().equals(lane))
                {
                    Length rearPos =
                            gtu.position(this.exitB.getLane(), gtu.getRelativePositions().get(this.exitB.getPositionType()));
                    if (rearPos.lt(this.exitB.getLongitudinalPosition()))
                    {
                        addGtu(gtu);
                    }
                    return;
                }

                throw new RuntimeException("Traffic light detector was notified that "
                        + "a GTU was added to a lane, but could not figure out what to do with it.");
            }
            catch (GtuException exception)
            {
                System.err.println("Caught GTU exception trying to get a position");
                exception.printStackTrace();
            }
        }
        else
        {
            System.err.println("Unexpected event: " + event);
        }
    }

    /** @return the relative position type of the vehicle (e.g., FRONT, BACK) that triggers the detector. */
    public final TYPE getPositionTypeEntry()
    {
        return this.entryA.getPositionType();
    }

    /** @return the relative position type of the vehicle (e.g., FRONT, BACK) that triggers the detector. */
    public final TYPE getPositionTypeExit()
    {
        return this.exitB.getPositionType();
    }

    /**
     * Return the A position of this NonDirectionalOccupancyDetector.
     * @return Length; the lane and position on the lane where GTU entry is detected
     */
    public final Length getLanePositionA()
    {
        return this.entryA.getLongitudinalPosition();
    }

    /**
     * Return the B position of this NonDirectionalOccupancyDetector.
     * @return Length; the lane and position on the lane where GTU exit is detected
     */
    public final Length getLanePositionB()
    {
        return this.exitB.getLongitudinalPosition();
    }

    /**
     * One of our start/end detectors has triggered.
     * @param detector StartEndDetector; the detector that was triggered
     * @param gtu LaneBasedGtu; the gtu that triggered the flank detector
     */
    public final void signalDetection(final StartEndDetector detector, final LaneBasedGtu gtu)
    {
        if (this.entryA.equals(detector))// || this.entryB == detector)
        {
            addGtu(gtu);
        }
        else if (this.exitB.equals(detector))// || this.exitA == detector)
        {
            removeGtu(gtu);
        }
    }

    /**
     * Returns the id.
     * @return The id of the detector.
     */
    @Override
    public final String getId()
    {
        return this.id;
    }

    /**
     * Returns the simulator.
     * @return The simulator.
     */
    public final OtsSimulatorInterface getSimulator()
    {
        return this.entryA.getSimulator();
    }

    /** {@inheritDoc} */
    @Override
    public final OrientedPoint2d getLocation()
    {
        return this.location;
    }

    /** {@inheritDoc} */
    @Override
    public final Bounds2d getBounds()
    {
        return this.geometry.getBounds();
    }

    /**
     * Return the state of this traffic light detector.
     * @return boolean; true if one or more GTUs are currently detected; false of no GTUs are currently detected
     */
    public final boolean getOccupancy()
    {
        return this.currentGTUs.size() > 0;
    }

    /** {@inheritDoc} */
    @Override
    public PolyLine2d getGeometry()
    {
        return this.geometry;
    }

    /** {@inheritDoc} */
    @Override
    public Length getHeight()
    {
        return Length.ZERO;
    }

    /** {@inheritDoc} */
    @Override
    public String getFullId()
    {
        return this.uniqueId;
    }

    /** {@inheritDoc} */
    @Override
    public DetectorType getType()
    {
        return this.type;
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "TrafficLightDetector [id=" + this.id + ", entryA=" + this.entryA + ", exitB=" + this.exitB + ", currentGTUs="
                + this.currentGTUs + ", lanes=" + this.lanes + ", geometry=" + this.geometry + "]";
    }

    /**
     * Embedded detectors used by a TrafficLightDetector.
     * <p>
     * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
     * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
     * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
     */
    public class StartEndDetector extends LaneDetector
    {
        /** */
        private static final long serialVersionUID = 20161104L;

        /**
         * Construct a new StartEndDetector.
         * @param id String; the name of the new StartEndDetector
         * @param lane Lane; the lane of the new StartEndDetector
         * @param longitudinalPosition Length; the longitudinal position of the new StartEndDetector
         * @param positionType TYPE; the position on the GTUs that triggers the new StartEndDetector
         * @param simulator OtsSimulatorInterface; the simulator engine
         * @param detectorType DetectorType; detector type.
         * @throws NetworkException when the network is inconsistent
         */
        public StartEndDetector(final String id, final Lane lane, final Length longitudinalPosition, final TYPE positionType,
                final OtsSimulatorInterface simulator, final DetectorType detectorType) throws NetworkException
        {
            super(id, lane, longitudinalPosition, positionType, simulator, detectorType);
        }

        /** {@inheritDoc} */
        @Override
        protected final void triggerResponse(final LaneBasedGtu gtu)
        {
            TrafficLightDetector.this.signalDetection(this, gtu);
        }

        /** {@inheritDoc} */
        @Override
        public final String toString()
        {
            return "StartEndDetector [parent=" + TrafficLightDetector.this.getId() + "]";
        }

        /**
         * Returns the parent TrafficLightDetector.
         * @return TrafficLightDetector; parent.
         */
        public TrafficLightDetector getParent()
        {
            return TrafficLightDetector.this;
        }

    }

}
