package org.opentrafficsim.road.network.lane.object.detector;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.djunits.value.vdouble.scalar.Length;
import org.djutils.event.Event;
import org.djutils.event.EventListener;
import org.djutils.event.EventType;
import org.djutils.event.LocalEventProducer;
import org.djutils.exceptions.Throw;
import org.opentrafficsim.base.Identifiable;
import org.opentrafficsim.core.compatibility.Compatible;
import org.opentrafficsim.core.dsol.OtsSimulatorInterface;
import org.opentrafficsim.core.geometry.Bounds;
import org.opentrafficsim.core.geometry.DirectedPoint;
import org.opentrafficsim.core.geometry.OtsGeometryException;
import org.opentrafficsim.core.geometry.OtsLine3D;
import org.opentrafficsim.core.geometry.OtsPoint3D;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.core.gtu.RelativePosition.TYPE;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.core.network.OtsNetwork;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;
import org.opentrafficsim.road.network.lane.Lane;

import nl.tudelft.simulation.dsol.animation.Locatable;

/**
 * This traffic light reports whether any GTUs are within its area. The area is two sub-sections on one or two lanes. This
 * traffic does <b>not</b> report the total number of GTUs within the area; only whether that number is zero or non-zero. This
 * class does not derive from {@code Detector} as it concerns an area, not a cross-section. All sides of the 2 areas are managed
 * by 4 {@code Detector}s to capture GTU longitudinal movement, and by listening to events to capture lane changes, vehicle
 * generation, and vehicle destruction.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class TrafficLightDetector extends LocalEventProducer
        implements EventListener, Locatable, DetectorAnimationToggle, Serializable, Identifiable
{
    /** */
    private static final long serialVersionUID = 20161103L;

    /** Id of this TrafficLightDetector. */
    private final String id;

    /** The detector that detects when a GTU enters the detector area at point A. */
    private final StartEndDetector entryA;

    /** The detector that detects when a GTU exits the detector area at point A. */
    private final StartEndDetector exitA;

    /** The detector that detects when a GTU enters the detector area at detectorB. */
    private final StartEndDetector entryB;

    /** The detector that detects when a GTU exits the detector area at point B. */
    private final StartEndDetector exitB;

    /** GTUs detected by the entrance detectors, but not yet removed by the exit detectors. */
    private final Set<LaneBasedGtu> currentGTUs = new LinkedHashSet<>();

    /** The lanes that the detector (partly) covers. */
    private final Set<Lane> lanes = new LinkedHashSet<>();

    /** The OTS network. */
    private final OtsNetwork network;

    /** Design line of the detector. */
    private final OtsLine3D path;

    /**
     * The <b>timed</b> event type for pub/sub indicating the triggering of the entry of a NonDirectionalOccupancyDetector. <br>
     * Payload: Object[] {String detectorId, NonDirectionalOccupancyDetector detector, LaneBasedGtu gtu, RelativePosition.TYPE
     * relativePosition}
     */
    public static final EventType TRAFFIC_LIGHT_DETECTOR_TRIGGER_ENTRY_EVENT =
            new EventType("TRAFFICLIGHTDETECTOR.TRIGGER.ENTRY");

    /**
     * The <b>timed</b> event type for pub/sub indicating the triggering of the exit of an NonDirectionalOccupancyDetector. <br>
     * Payload: Object[] {String detectorId, NonDirectionalOccupancyDetector detector, LaneBasedGtu gtu, RelativePosition.TYPE
     * relativePosition}
     */
    public static final EventType TRAFFIC_LIGHT_DETECTOR_TRIGGER_EXIT_EVENT =
            new EventType("TRAFFICLIGHTDETECTOR.TRIGGER.EXIT");

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
     * @param simulator OTSSimulatorInterface; the simulator
     * @param compatible Compatible; object that checks that the detector detects a GTU.
     * @throws NetworkException when the network is inconsistent.
     */
    @SuppressWarnings("checkstyle:parameternumber")
    public TrafficLightDetector(final String id, final Lane laneA, final Length positionA, final Lane laneB,
            final Length positionB, final List<Lane> intermediateLanes, final TYPE entryPosition, final TYPE exitPosition,
            final OtsSimulatorInterface simulator, final Compatible compatible) throws NetworkException
    {
        Throw.whenNull(id, "id may not be null");
        this.id = id;
        this.entryA = new StartEndDetector(id + ".entryA", laneA, positionA, entryPosition, simulator, compatible);
        this.exitA = new StartEndDetector(id + ".exitA", laneA, positionA, exitPosition, simulator, compatible);
        this.entryB = new StartEndDetector(id + ".entryB", laneB, positionB, entryPosition, simulator, compatible);
        this.exitB = new StartEndDetector(id + ".exitB", laneB, positionB, exitPosition, simulator, compatible);
        // Set up detection of GTUs that enter or leave the detector laterally or appear due to a generator or disappear due to
        // a
        // sink
        this.lanes.add(laneA);
        this.network = laneA.getParentLink().getNetwork();
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
        List<OtsPoint3D> outLine = new ArrayList<>();
        outLine.add(fixElevation(this.entryA.getGeometry().getCentroid()));
        if (null != intermediateLanes && intermediateLanes.size() > 0)
        {
            Lane prevLane = laneA;
            List<Lane> remainingLanes = new ArrayList<>();
            remainingLanes.addAll(intermediateLanes);
            remainingLanes.add(laneB);
            while (remainingLanes.size() > 0)
            {
                Lane continuingLane = null;
                Node node = prevLane.getParentLink().getEndNode();
                for (Lane nextLane : intermediateLanes)
                {
                    if (nextLane.getParentLink().getStartNode().equals(node))
                    {
                        continuingLane = nextLane;
                        outLine.add(fixElevation(nextLane.getCenterLine().getFirst()));
                        break;
                    }
                    else if (nextLane.getParentLink().getEndNode().equals(node))
                    {
                        continuingLane = nextLane;
                        outLine.add(fixElevation(nextLane.getCenterLine().getLast()));
                        break;
                    }
                }
                if (null == continuingLane)
                {
                    throw new NetworkException("Cannot find route from laneA to laneB using the provided intermediateLanes");
                }
                remainingLanes.remove(continuingLane);
            }
        }
        outLine.add(fixElevation(this.exitB.getGeometry().getCentroid()));
        try
        {
            this.path = OtsLine3D.createAndCleanOTSLine3D(outLine);
        }
        catch (OtsGeometryException exception)
        {
            // This happens if A and B are the same
            throw new NetworkException(exception);
        }
    }

    /**
     * Increase the elevation of an OTSPoint3D.
     * @param point OTSPoint3D; the point
     * @return OTSPoint3D
     */
    private OtsPoint3D fixElevation(final OtsPoint3D point)
    {
        return new OtsPoint3D(point.x, point.y, point.z + Detector.DEFAULT_DETECTOR_ELEVATION.si);
    }

    /**
     * Add a GTU to the set.
     * @param gtu LaneBasedGtu; the GTU that must be added
     */
    protected final void addGTU(final LaneBasedGtu gtu)
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
    protected final void removeGTU(final LaneBasedGtu gtu)
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
        // System.out.println("Received notification: " + event);
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
                Map<Lane, Length> frontPositions = gtu.positions(gtu.getRelativePositions().get(this.entryA.getPositionType()));
                Set<Lane> remainingLanes = new LinkedHashSet<>(frontPositions.keySet());
                remainingLanes.retainAll(this.lanes);
                if (remainingLanes.size() == 0)
                {
                    removeGTU(gtu);
                }
                // else: GTU is still in one of our lanes and we will get another GTU_REMOVE_EVENT or the GTU will trigger one
                // of our exit flank detectors or when the GTU leaves this detector laterally
                return;
            }
            catch (GtuException exception)
            {
                System.err.println("Caught GTU exception trying to get the frontPositions");
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
                Map<Lane, Length> frontPositions = gtu.positions(gtu.getRelativePositions().get(this.entryA.getPositionType()));
                Set<Lane> remainingLanes = new LinkedHashSet<>(frontPositions.keySet());
                remainingLanes.retainAll(this.lanes);
                if (remainingLanes.size() == 0)
                {
                    System.err.println("GTU is not in any of our lanes - CANNOT HAPPEN");
                }
                Map<Lane, Length> rearPositions = gtu.positions(gtu.getRelativePositions().get(this.exitA.getPositionType()));
                for (Lane remainingLane : remainingLanes)
                {
                    Length frontPosition = frontPositions.get(remainingLane);
                    Length rearPosition = rearPositions.get(remainingLane);
                    Length laneLength = remainingLane.getLength();
                    // System.out.println("frontPosition " + frontPosition + ", rearPosition " + rearPosition + ", laneLength "
                    // + laneLength + ", directionalityB " + this.directionalityB);

                    if (frontPosition.lt0() && rearPosition.lt0()
                            || frontPosition.gt(laneLength) && rearPosition.gt(laneLength))
                    {
                        continue; // Not detected on this lane
                    }
                    // The active part of the GTU covers some part of this lane
                    if (this.entryA.getLane() != remainingLane && this.entryB.getLane() != remainingLane)
                    {
                        // The active part covers (part of) an intermediate lane; therefore this detector detects the GTU
                        addGTU(gtu);
                        return;
                    }
                    // The GTU is on the A lane and/or the B lane; in this case the driving direction matters
                    if (this.entryA.getLane().equals(this.entryB.getLane()))
                    {
                        // A lane equals B lane; does the active part of the GTU cover the detector?
                        if (frontPosition.ge(this.entryA.getLongitudinalPosition())
                                && rearPosition.lt(this.exitB.getLongitudinalPosition()))
                        {
                            addGTU(gtu);
                            return;
                        }
                    }
                    else
                    // Lane A is not equal to lane B; the GTU is on of these
                    {
                        Length detectionPosition;
                        if (this.entryA.getLane() == remainingLane)
                        {
                            detectionPosition = this.entryA.getLongitudinalPosition();
                        }
                        else
                        {
                            detectionPosition = this.entryB.getLongitudinalPosition();
                        }
                        if (frontPosition.ge(detectionPosition))
                        {
                            addGTU(gtu);
                            return;
                        }
                    }
                }
                return;
            }
            catch (GtuException exception)
            {
                System.err.println("Caught GTU exception trying to get the frontPositions");
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
        return this.exitA.getPositionType();
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
        return this.entryB.getLongitudinalPosition();
    }

    /**
     * One of our flank detectors has triggered.
     * @param detector StartEndDetector; the detector that was triggered
     * @param gtu LaneBasedGtu; the gtu that triggered the flank detector
     */
    public final void signalDetection(final StartEndDetector detector, final LaneBasedGtu gtu)
    {
        if (this.entryA == detector || this.entryB == detector)
        {
            addGTU(gtu);
        }
        else if (this.exitA == detector || this.exitB == detector)
        // Some exit detector has triggered
        {
            removeGTU(gtu);
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
    public final DirectedPoint getLocation()
    {
        return this.path.getLocation();
    }

    /** {@inheritDoc} */
    @Override
    public final Bounds getBounds() throws RemoteException
    {
        return this.path.getBounds();
    }

    /**
     * Return the path of this traffic light detector.
     * @return OTSLine3D; the path of this traffic light detector
     */
    public final OtsLine3D getPath()
    {
        return this.path;
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
    public final String toString()
    {
        return "TrafficLightDetector [id=" + this.id + ", entryA=" + this.entryA + ", exitA=" + this.exitA + ", entryB="
                + this.entryB + ", exitB=" + this.exitB + ", currentGTUs=" + this.currentGTUs + ", lanes=" + this.lanes
                + ", path=" + this.path + "]";
    }

    /**
     * Embedded detectors used by a TrafficLightDetector.
     * <p>
     * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * <p>
     * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
     * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
     * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
     */
    public class StartEndDetector extends Detector
    {
        /** */
        private static final long serialVersionUID = 20161104L;

        /**
         * Construct a new StartEndDetector.
         * @param id String; the name of the new StartEndDetector
         * @param lane Lane; the lane of the new StartEndDetector
         * @param longitudinalPosition Length; the longitudinal position of the new StartEndDetector
         * @param positionType TYPE; the position on the GTUs that triggers the new StartEndDetector
         * @param simulator OTSSimulatorInterface; the simulator engine
         * @param compatible Compatible; object that determines if a GTU is detectable by the new StartEndDetector
         * @throws NetworkException when the network is inconsistent
         */
        public StartEndDetector(final String id, final Lane lane, final Length longitudinalPosition, final TYPE positionType,
                final OtsSimulatorInterface simulator, final Compatible compatible) throws NetworkException
        {
            super(id, lane, longitudinalPosition, positionType, simulator, compatible);
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
            return "FlankSensor [parent=" + TrafficLightDetector.this.getId() + "]";
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
