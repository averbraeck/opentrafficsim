package org.opentrafficsim.road.network.lane.object.sensor;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.media.j3d.Bounds;

import org.djunits.value.vdouble.scalar.Length;
import org.djutils.event.EventInterface;
import org.djutils.event.EventListenerInterface;
import org.djutils.event.EventProducer;
import org.djutils.event.EventProducerInterface;
import org.djutils.exceptions.Throw;
import org.opentrafficsim.core.compatibility.Compatible;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.geometry.OTSLine3D;
import org.opentrafficsim.core.geometry.OTSPoint3D;
import org.opentrafficsim.core.gtu.GTUDirectionality;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.RelativePosition.TYPE;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.core.network.OTSNetwork;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.object.trafficlight.FlankSensor;

import nl.tudelft.simulation.dsol.animation.Locatable;
import nl.tudelft.simulation.dsol.simulators.DEVSSimulatorInterface;
import nl.tudelft.simulation.language.d3.DirectedPoint;

/**
 * This traffic light sensor reports whether it whether any GTUs are within its area. The area is a sub-section of a Lane. This
 * traffic sensor does <b>not</b> report the total number of GTUs within the area; only whether that number is zero or non-zero.
 * <p>
 * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Oct 27, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class TrafficLightSensor extends EventProducer
        implements EventListenerInterface, NonDirectionalOccupancySensor, EventProducerInterface, Locatable, Sensor
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
    private final Set<LaneBasedGTU> currentGTUs = new LinkedHashSet<>();

    /** The lanes that the detector (partly) covers. */
    private final Set<Lane> lanes = new LinkedHashSet<>();
    
    /** The OTS network. */
    private final OTSNetwork network;

    /** Which side of the position of flank sensor A is the TrafficLightSensor. */
    private final GTUDirectionality directionalityA;

    /** Which side of the position of flank sensor B is the TrafficLightSensor. */
    private final GTUDirectionality directionalityB;

    /** Design line of the sensor. */
    private final OTSLine3D path;

    /**
     * Construct a new traffic light sensor.<br>
     * TODO Possibly provide the GTUTypes that trigger the sensor as an argument for the constructor
     * @param id String; id of this sensor
     * @param laneA Lane; the lane of the A detection point of this traffic light sensor
     * @param positionA Length; the position of the A detection point of this traffic light sensor
     * @param laneB Lane; the lane of the B detection point of this traffic light sensor
     * @param positionB Length; the position of the B detection point of this traffic light sensor
     * @param intermediateLanes List&lt;Lane&gt;; list of intermediate lanes
     * @param entryPosition TYPE; the position on the GTUs that trigger the entry events
     * @param exitPosition TYPE; the position on the GTUs that trigger the exit events
     * @param simulator DEVSSimulatorInterface.TimeDoubleUnit; the simulator
     * @param compatible Compatible; object that checks that the detector detects a GTU.
     * @throws NetworkException when the network is inconsistent.
     */
    @SuppressWarnings("checkstyle:parameternumber")
    public TrafficLightSensor(final String id, final Lane laneA, final Length positionA, final Lane laneB,
            final Length positionB, final List<Lane> intermediateLanes, final TYPE entryPosition, final TYPE exitPosition,
            final DEVSSimulatorInterface.TimeDoubleUnit simulator, final Compatible compatible) throws NetworkException
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
        if (laneA.equals(laneB))
        {
            this.directionalityA = positionA.le(positionB) ? GTUDirectionality.DIR_PLUS : GTUDirectionality.DIR_MINUS;
            this.directionalityB = this.directionalityA;
        }
        else
        {
            this.directionalityA = findDirectionality(laneA, intermediateLanes);
            this.directionalityB = GTUDirectionality.DIR_PLUS == findDirectionality(laneB, intermediateLanes)
                    ? GTUDirectionality.DIR_MINUS : GTUDirectionality.DIR_PLUS;
            // System.out.println("Directionality on B is " + this.directionalityB);
        }
        List<OTSPoint3D> outLine = new ArrayList<>();
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
            this.path = OTSLine3D.createAndCleanOTSLine3D(outLine);
        }
        catch (OTSGeometryException exception)
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
    private OTSPoint3D fixElevation(final OTSPoint3D point)
    {
        return new OTSPoint3D(point.x, point.y, point.z + SingleSensor.DEFAULT_SENSOR_ELEVATION.si);
    }

    /**
     * Figure out which part of a lane is covered by the TrafficLightSensor.
     * @param lane Lane; the lane
     * @param intermediateLanes List&lt;Lane&gt;; TODO
     * @return GTUDirectionality; DIR_PLUS if the detector covers the section with higher longitudinal position; DIR_MINUS if
     *         the detector covers the section with lower longitudinal position
     * @throws NetworkException if the lane is not connected to any of the lanes in this.lanes
     */
    private GTUDirectionality findDirectionality(final Lane lane, final List<Lane> intermediateLanes) throws NetworkException
    {
        Node startNode = lane.getParentLink().getStartNode();
        Node endNode = lane.getParentLink().getEndNode();
        for (Lane otherLane : intermediateLanes)
        {
            if (lane.equals(otherLane))
            {
                continue;
            }
            Node intermediateNode = otherLane.getParentLink().getStartNode();
            if (intermediateNode == startNode)
            {
                return GTUDirectionality.DIR_MINUS;
            }
            if (intermediateNode == endNode)
            {
                return GTUDirectionality.DIR_PLUS;
            }
            intermediateNode = otherLane.getParentLink().getEndNode();
            if (intermediateNode == startNode)
            {
                return GTUDirectionality.DIR_MINUS;
            }
            if (intermediateNode == endNode)
            {
                return GTUDirectionality.DIR_PLUS;
            }
        }
        throw new NetworkException("lane " + lane + " is not connected to any intermediate lane or the other lane");
    }

    /**
     * Add a GTU to the set.
     * @param gtu LaneBasedGTU; the GTU that must be added
     */
    protected final void addGTU(final LaneBasedGTU gtu)
    {
        if (this.currentGTUs.add(gtu) && this.currentGTUs.size() == 1)
        {
            fireTimedEvent(NonDirectionalOccupancySensor.NON_DIRECTIONAL_OCCUPANCY_SENSOR_TRIGGER_ENTRY_EVENT,
                    new Object[] {getId()}, getSimulator().getSimulatorTime());
        }
    }

    /**
     * Remove a GTU from the set.
     * @param gtu LaneBasedGTU; the GTU that must be removed
     */
    protected final void removeGTU(final LaneBasedGTU gtu)
    {
        if (this.currentGTUs.remove(gtu) && this.currentGTUs.size() == 0)
        {
            fireTimedEvent(NonDirectionalOccupancySensor.NON_DIRECTIONAL_OCCUPANCY_SENSOR_TRIGGER_EXIT_EVENT,
                    new Object[] {getId()}, getSimulator().getSimulatorTime());
        }
    }

    /** {@inheritDoc} */
    @Override
    public final void notify(final EventInterface event) throws RemoteException
    {
        // System.out.println("Received notification: " + event);
        String gtuId = (String) ((Object[]) event.getContent())[0];
        LaneBasedGTU gtu = (LaneBasedGTU) this.network.getGTU(gtuId);
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
            catch (GTUException exception)
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
                    GTUDirectionality drivingDirection = gtu.getDirection(remainingLane);
                    if (this.entryA.getLane().equals(this.entryB.getLane()))
                    {
                        // A lane equals B lane; does the active part of the GTU cover the detector?
                        // TODO: not handling backwards driving GTU
                        if (GTUDirectionality.DIR_PLUS == drivingDirection)
                        {
                            // GTU is driving in direction of increasing longitudinal distance
                            if (this.directionalityA == GTUDirectionality.DIR_PLUS)
                            {
                                if (frontPosition.ge(this.entryA.getLongitudinalPosition())
                                        && rearPosition.lt(this.exitB.getLongitudinalPosition()))
                                {
                                    addGTU(gtu);
                                    return;
                                }
                            }
                            else
                            {
                                if (frontPosition.le(this.entryB.getLongitudinalPosition())
                                        && rearPosition.gt(this.exitA.getLongitudinalPosition()))
                                {
                                    addGTU(gtu);
                                    return;
                                }
                            }
                        }
                        else
                        {
                            // GTU is driving in direction of decreasing longitudinal distance
                            if (this.directionalityA == GTUDirectionality.DIR_MINUS)
                            {
                                if (frontPosition.le(this.entryB.getLongitudinalPosition())
                                        && rearPosition.gt(this.entryA.getLongitudinalPosition()))
                                {
                                    addGTU(gtu);
                                    return;
                                }
                            }
                            else
                            {
                                if (frontPosition.le(this.entryB.getLongitudinalPosition())
                                        && rearPosition.gt(this.exitA.getLongitudinalPosition()))
                                {
                                    addGTU(gtu);
                                    return;
                                }
                            }
                        }
                    }
                    else
                    // Lane A is not equal to lane B; the GTU is on of these
                    {
                        Length detectionPosition;
                        GTUDirectionality detectorDirectionality;
                        if (this.entryA.getLane() == remainingLane)
                        {
                            detectionPosition = this.entryA.getLongitudinalPosition();
                            detectorDirectionality = this.directionalityA;
                        }
                        else
                        {
                            detectionPosition = this.entryB.getLongitudinalPosition();
                            detectorDirectionality = this.directionalityB;
                        }
                        if (GTUDirectionality.DIR_PLUS == drivingDirection)
                        {
                            if (GTUDirectionality.DIR_PLUS == detectorDirectionality)
                            {
                                if (frontPosition.ge(detectionPosition))
                                {
                                    addGTU(gtu);
                                    return;
                                }
                            }
                            else
                            {
                                if (rearPosition.le(detectionPosition))
                                {
                                    addGTU(gtu);
                                    return;
                                }
                            }
                        }
                        else
                        {
                            // GTU is driving in direction of decreasing longitudinal distance
                            if (GTUDirectionality.DIR_PLUS == detectorDirectionality)
                            {
                                if (rearPosition.ge(detectionPosition))
                                {
                                    addGTU(gtu);
                                    return;
                                }
                            }
                            else
                            {
                                if (frontPosition.le(detectionPosition))
                                {
                                    addGTU(gtu);
                                    return;
                                }
                            }
                        }
                    }
                }
                return;
            }
            catch (GTUException exception)
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
     * @param gtu LaneBasedGTU; the gtu that triggered the flank sensor
     */
    public final void signalDetection(final FlankSensor sensor, final LaneBasedGTU gtu)
    {
        GTUDirectionality gtuDirection = null;
        try
        {
            gtuDirection = gtu.getDirection(sensor.getLane());
        }
        catch (GTUException exception)
        {
            exception.printStackTrace();
        }
        // String source =
        // this.entryA == sensor ? "entryA" : this.entryB == sensor ? "entryB" : this.exitA == sensor ? "exitA"
        // : this.exitB == sensor ? "exitB" : "???";
        // System.out.println("Time " + sensor.getSimulator().getSimulatorTime() + ": " + this.id + " " + source
        // + " triggered on " + gtu + " driving direction is " + gtuDirection);
        if (this.entryA == sensor && gtuDirection == this.directionalityA
                || this.entryB == sensor && gtuDirection != this.directionalityB)
        {
            addGTU(gtu);
        }
        else if (this.exitA == sensor && gtuDirection != this.directionalityA
                || this.exitB == sensor && gtuDirection == this.directionalityB)
        // Some exit sensor has triggered
        {
            removeGTU(gtu);
        }
        // else
        // {
        // // System.out.println("Ignoring event (GTU is driving in wrong direction)");
        // }
    }

    /** {@inheritDoc} */
    @Override
    public final String getId()
    {
        return this.id;
    }

    /** {@inheritDoc} */
    @Override
    public final DEVSSimulatorInterface.TimeDoubleUnit getSimulator()
    {
        return this.entryA.getSimulator();
    }

    /** {@inheritDoc} */
    @Override
    public final DirectedPoint getLocation() throws RemoteException
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
    public final OTSLine3D getPath()
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
                + ", directionalityA=" + this.directionalityA + ", directionalityB=" + this.directionalityB + ", path="
                + this.path + "]";
    }

    /** {@inheritDoc} */
    @Override
    public Serializable getSourceId()
    {
        return this.id;
    }

}
