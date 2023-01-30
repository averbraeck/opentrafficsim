package org.opentrafficsim.road.network.lane.object.sensor;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.djunits.value.vdouble.scalar.Length;
import org.djutils.event.Event;
import org.djutils.event.EventListener;
import org.djutils.event.LocalEventProducer;
import org.djutils.exceptions.Throw;
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
import org.opentrafficsim.road.network.lane.object.trafficlight.FlankSensor;

import nl.tudelft.simulation.dsol.animation.Locatable;

/**
 * This traffic light sensor reports whether it whether any GTUs are within its area. The area is a sub-section of a Lane. This
 * traffic sensor does <b>not</b> report the total number of GTUs within the area; only whether that number is zero or non-zero.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class TrafficLightSensor extends LocalEventProducer
        implements EventListener, NonDirectionalOccupancySensor, Locatable, Sensor
{
    /** */
    private static final long serialVersionUID = 20161103L;

    /** Id of this TrafficLightSensor. */
    private final String id;

    /** The sensor that detects when a GTU enters the sensor area at point A. */
    private final FlankSensor entryA;

    /** The sensor that detects when a GTU exits the sensor area at point A. */
    private final FlankSensor exitA;

    /** The sensor that detects when a GTU enters the sensor area at point B. */
    private final FlankSensor entryB;

    /** The sensor that detects when a GTU exits the sensor area at point B. */
    private final FlankSensor exitB;

    /** GTUs detected by the upSensor, but not yet removed by the downSensor. */
    private final Set<LaneBasedGtu> currentGTUs = new LinkedHashSet<>();

    /** The lanes that the detector (partly) covers. */
    private final Set<Lane> lanes = new LinkedHashSet<>();

    /** The OTS network. */
    private final OtsNetwork network;

    /** Design line of the sensor. */
    private final OtsLine3D path;

    /**
     * Construct a new traffic light sensor.<br>
     * TODO Possibly provide the GtuTypes that trigger the sensor as an argument for the constructor
     * @param id String; id of this sensor
     * @param laneA Lane; the lane of the A detection point of this traffic light sensor
     * @param positionA Length; the position of the A detection point of this traffic light sensor
     * @param laneB Lane; the lane of the B detection point of this traffic light sensor
     * @param positionB Length; the position of the B detection point of this traffic light sensor
     * @param intermediateLanes List&lt;Lane&gt;; list of intermediate lanes
     * @param entryPosition TYPE; the position on the GTUs that trigger the entry events
     * @param exitPosition TYPE; the position on the GTUs that trigger the exit events
     * @param simulator OTSSimulatorInterface; the simulator
     * @param compatible Compatible; object that checks that the detector detects a GTU.
     * @throws NetworkException when the network is inconsistent.
     */
    @SuppressWarnings("checkstyle:parameternumber")
    public TrafficLightSensor(final String id, final Lane laneA, final Length positionA, final Lane laneB,
            final Length positionB, final List<Lane> intermediateLanes, final TYPE entryPosition, final TYPE exitPosition,
            final OtsSimulatorInterface simulator, final Compatible compatible) throws NetworkException
    {
        Throw.whenNull(id, "id may not be null");
        this.id = id;
        this.entryA = new FlankSensor(id + ".entryA", laneA, positionA, entryPosition, simulator, this, compatible);
        this.exitA = new FlankSensor(id + ".exitA", laneA, positionA, exitPosition, simulator, this, compatible);
        this.entryB = new FlankSensor(id + ".entryB", laneB, positionB, entryPosition, simulator, this, compatible);
        this.exitB = new FlankSensor(id + ".exitB", laneB, positionB, exitPosition, simulator, this, compatible);
        // Set up detection of GTUs that enter or leave the sensor laterally or appear due to a generator or disappear due to a
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
        return new OtsPoint3D(point.x, point.y, point.z + SingleSensor.DEFAULT_SENSOR_ELEVATION.si);
    }

    /**
     * Add a GTU to the set.
     * @param gtu LaneBasedGtu; the GTU that must be added
     */
    protected final void addGTU(final LaneBasedGtu gtu)
    {
        if (this.currentGTUs.add(gtu) && this.currentGTUs.size() == 1)
        {
            fireTimedEvent(NonDirectionalOccupancySensor.NON_DIRECTIONAL_OCCUPANCY_SENSOR_TRIGGER_ENTRY_EVENT,
                    new Object[] {getId()}, getSimulator().getSimulatorTime());
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
            fireTimedEvent(NonDirectionalOccupancySensor.NON_DIRECTIONAL_OCCUPANCY_SENSOR_TRIGGER_EXIT_EVENT,
                    new Object[] {getId()}, getSimulator().getSimulatorTime());
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
                // of our exit flank sensors or when the GTU leaves this detector laterally
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

    /** {@inheritDoc} */
    @Override
    public final TYPE getPositionTypeEntry()
    {
        return this.entryA.getPositionType();
    }

    /** {@inheritDoc} */
    @Override
    public final TYPE getPositionTypeExit()
    {
        return this.exitA.getPositionType();
    }

    /** {@inheritDoc} */
    @Override
    public final Length getLanePositionA()
    {
        return this.entryA.getLongitudinalPosition();
    }

    /** {@inheritDoc} */
    @Override
    public final Length getLanePositionB()
    {
        return this.entryB.getLongitudinalPosition();
    }

    /**
     * One of our flank sensors has triggered.
     * @param sensor FlankSensor; the sensor that was triggered
     * @param gtu LaneBasedGtu; the gtu that triggered the flank sensor
     */
    public final void signalDetection(final FlankSensor sensor, final LaneBasedGtu gtu)
    {
        if (this.entryA == sensor || this.entryB == sensor)
        {
            addGTU(gtu);
        }
        else if (this.exitA == sensor || this.exitB == sensor)
        // Some exit sensor has triggered
        {
            removeGTU(gtu);
        }
    }

    /** {@inheritDoc} */
    @Override
    public final String getId()
    {
        return this.id;
    }

    /** {@inheritDoc} */
    @Override
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
     * Return the path of this traffic light sensor.
     * @return OTSLine3D; the path of this traffic light sensor
     */
    public final OtsLine3D getPath()
    {
        return this.path;
    }

    /**
     * Return the state of this traffic light sensor.
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
        return "TrafficLightSensor [id=" + this.id + ", entryA=" + this.entryA + ", exitA=" + this.exitA + ", entryB="
                + this.entryB + ", exitB=" + this.exitB + ", currentGTUs=" + this.currentGTUs + ", lanes=" + this.lanes
                + ", path=" + this.path + "]";
    }

}
