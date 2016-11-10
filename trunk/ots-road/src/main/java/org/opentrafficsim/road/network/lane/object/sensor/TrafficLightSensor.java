package org.opentrafficsim.road.network.lane.object.sensor;

import java.rmi.RemoteException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nl.tudelft.simulation.event.EventInterface;
import nl.tudelft.simulation.event.EventListenerInterface;
import nl.tudelft.simulation.event.EventProducer;
import nl.tudelft.simulation.event.EventProducerInterface;
import nl.tudelft.simulation.language.Throw;

import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.gtu.GTUDirectionality;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.gtu.RelativePosition.TYPE;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.network.lane.CrossSectionElement;
import org.opentrafficsim.road.network.lane.Lane;

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
public class TrafficLightSensor extends EventProducer implements EventListenerInterface, NonDirectionalOccupancySensor,
        EventProducerInterface
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
    private final Set<LaneBasedGTU> currentGTUs = new HashSet<>();

    /** The lanes that the detector (partly) covers. */
    private final Set<Lane> lanes = new HashSet<>();

    /** Which side of the position of flank sensor A is the TrafficLightSensor. */
    private final GTUDirectionality directionalityA;

    /** Which side of the position of flank sensor B is the TrafficLightSensor. */
    private final GTUDirectionality directionalityB;

    /**
     * @param id String; id of this sensor
     * @param laneA Lane; the lane of the A detection point of this traffic light sensor
     * @param positionA Length; the position of the A detection point of this traffic light sensor
     * @param laneB Lane; the lane of the B detection point of this traffic light sensor
     * @param positionB Length; the position of the B detection point of this traffic light sensor
     * @param intermediateLanes List&lt;Lane&gt;; list of intermediate lanes
     * @param entryPosition RelativePosition; the position on the GTUs that trigger the entry events
     * @param exitPosition RelativePosition; the position on the GTUs that trigger the exit events
     * @param simulator OTSDEVSSimulatorInterface; the simulator
     * @throws NetworkException when the network is inconsistent.
     * TODO Possibly provide the GTUTypes that trigger the sensor as an argument for the constructor
     */
    public TrafficLightSensor(final String id, final Lane laneA, final Length positionA, final Lane laneB,
            final Length positionB, final List<Lane> intermediateLanes, final TYPE entryPosition, final TYPE exitPosition,
            final OTSDEVSSimulatorInterface simulator) throws NetworkException
    {
        Throw.whenNull(id, "id may not be null");
        this.id = id;
        this.entryA = new FlankSensor(id + ".entryA", laneA, positionA, entryPosition, simulator, this);
        this.exitA = new FlankSensor(id + ".exitA", laneA, positionA, exitPosition, simulator, this);
        this.entryB = new FlankSensor(id + ".entryB", laneB, positionB, entryPosition, simulator, this);
        this.exitB = new FlankSensor(id + ".exitB", laneB, positionB, exitPosition, simulator, this);
        // Set up detection of GTUs that enter or leave the sensor laterally or appear due to a generator or disappear due to a
        // sink
        this.lanes.add(laneA);
        this.lanes.add(laneB);
        if (null != intermediateLanes)
        {
            this.lanes.addAll(intermediateLanes);
        }
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
            this.directionalityA = findDirectionality(laneA);
            this.directionalityB =
                    GTUDirectionality.DIR_PLUS == findDirectionality(laneB) ? GTUDirectionality.DIR_MINUS
                            : GTUDirectionality.DIR_PLUS;
            System.out.println("Directionality on B is " + this.directionalityB);
        }
    }

    /**
     * Figure out which part of a lane is covered by the TrafficLightSensor.
     * @param lane Lane; the lane
     * @return GTUDirectionality; DIR_PLUS if the detector covers the section with higher longitudinal position; DIR_MINUS if
     *         the detector covers the section with lower longitudinal position
     * @throws NetworkException if the lane is not connected to any of the lanes in this.lanes
     */
    private GTUDirectionality findDirectionality(final Lane lane) throws NetworkException
    {
        for (Lane nextLane : lane.nextLanes(GTUType.ALL).keySet())
        {
            if (this.lanes.contains(nextLane))
            {
                return GTUDirectionality.DIR_PLUS;
            }
        }
        for (Lane prevLane : lane.prevLanes(GTUType.ALL).keySet())
        {
            if (this.lanes.contains(prevLane))
            {
                return GTUDirectionality.DIR_MINUS;
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
                    new Object[] { getId() }, getSimulator().getSimulatorTime());
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
                    new Object[] { getId() }, getSimulator().getSimulatorTime());
        }
    }

    /** {@inheritDoc} */
    @Override
    public final void notify(final EventInterface event) throws RemoteException
    {
        System.out.println("Received notification: " + event);
        LaneBasedGTU gtu = (LaneBasedGTU) ((Object[]) event.getContent())[1];
        if (Lane.GTU_REMOVE_EVENT.equals(event.getType()))
        {
            if (!this.currentGTUs.contains(gtu))
            {
                return; // GTU is not currently detected; nothing to do
            }
            try
            {
                Map<Lane, Length> frontPositions = gtu.positions(gtu.getRelativePositions().get(this.entryA.getPositionType()));
                Set<Lane> remainingLanes = new HashSet<>(frontPositions.keySet());
                remainingLanes.retainAll(this.lanes);
                if (remainingLanes.size() == 0)
                {
                    removeGTU(gtu);
                }
                // else: GTU is still in one of our lanes and we should get another GTU_REMOVE_EVENT or the GTU will trigger one
                // of our exit flank sensors
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
                Set<Lane> remainingLanes = new HashSet<>(frontPositions.keySet());
                remainingLanes.retainAll(this.lanes);
                if (remainingLanes.size() == 0)
                {
                    System.err.println("GTU is not in any or our lanes - CANNOT HAPPEN");
                }
                Map<Lane, Length> rearPositions = gtu.positions(gtu.getRelativePositions().get(this.exitA.getPositionType()));
                for (Lane remainingLane : remainingLanes)
                {
                    Length frontPosition = frontPositions.get(remainingLane);
                    Length rearPosition = rearPositions.get(remainingLane);
                    Length laneLength = remainingLane.getLength();
                    System.out.println("frontPosition " + frontPosition + ", rearPosition " + rearPosition + ", laneLength "
                            + laneLength + ", directionalityB " + this.directionalityB);
                    if (laneLength.si >= 900)
                    {
                        System.out.println("Let op");
                    }
                    if (frontPosition.lt(Length.ZERO) && rearPosition.lt(Length.ZERO) || frontPosition.gt(laneLength)
                            && rearPosition.gt(laneLength))
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
                System.err.println("Caught GTU exception tryint to get the frontPositions");
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
    final void signalDetection(final FlankSensor sensor, final LaneBasedGTU gtu)
    {
        String source =
                this.entryA == sensor ? "entryA" : this.entryB == sensor ? "entryB" : this.exitA == sensor ? "exitA"
                        : this.exitB == sensor ? "exitB" : "???";
        GTUDirectionality gtuDirection = null;
        try
        {
            gtuDirection = gtu.getDirection(sensor.getLane());
        }
        catch (GTUException exception)
        {
            exception.printStackTrace();
        }
        System.out.println("Time " + sensor.getSimulator().getSimulatorTime().get() + ": " + this.id + " " + source
                + " triggered on " + gtu + " driving direction is " + gtuDirection);
        if (this.entryA == sensor && gtuDirection == this.directionalityA || this.entryB == sensor
                && gtuDirection != this.directionalityB)
        {
            addGTU(gtu);
        }
        else if (this.exitA == sensor && gtuDirection != this.directionalityA || this.exitB == sensor
                && gtuDirection == this.directionalityB)
        // Some exit sensor has triggered
        {
            removeGTU(gtu);
        }
        else
        {
            // System.out.println("Ignoring event (GTU is driving in wrong direction)");
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
    public final OTSDEVSSimulatorInterface getSimulator()
    {
        return this.entryA.getSimulator();
    }

}

/**
 * The embedded sensors of a TrafficLightSensor.
 */
class FlankSensor extends AbstractSensor
{
    /** */
    private static final long serialVersionUID = 20161104L;

    /** The parent that must be informed of all flanks. */
    private final TrafficLightSensor parent;

    /**
     * Construct a new FlankSensor.
     * @param id String; the name of the new FlankSensor
     * @param lane Lane; the lane of the new FlankSensor
     * @param longitudinalPosition Length; the longitudinal position of the new FlankSensor
     * @param positionType TYPE; the position on the GTUs that triggers the new FlankSensor
     * @param simulator OTSDEVSSimulatorInterface; the simulator engine
     * @param parent TrafficLightSensor; the traffic light sensor that deploys this FlankSensor
     * @throws NetworkException when the network is inconsistent
     */
    FlankSensor(final String id, final Lane lane, final Length longitudinalPosition, final TYPE positionType,
            final OTSDEVSSimulatorInterface simulator, final TrafficLightSensor parent) throws NetworkException
    {
        super(id, lane, longitudinalPosition, positionType, simulator);
        this.parent = parent;
    }

    /** {@inheritDoc} */
    @Override
    protected void triggerResponse(final LaneBasedGTU gtu)
    {
        this.parent.signalDetection(this, gtu);
    }

    /** {@inheritDoc} */
    @Override
    public FlankSensor clone(final CrossSectionElement newCSE, final OTSSimulatorInterface newSimulator, final boolean animation)
            throws NetworkException
    {
        Throw.when(!(newCSE instanceof Lane), NetworkException.class, "sensors can only be cloned for Lanes");
        Throw.when(!(newSimulator instanceof OTSDEVSSimulatorInterface), NetworkException.class,
                "simulator should be a DEVSSimulator");
        // XXX should the parent of the clone be our parent??? And should the (cloned) parent not construct its own flank
        // sensors?
        return new FlankSensor(getId(), (Lane) newCSE, getLongitudinalPosition(), getPositionType(),
                (OTSDEVSSimulatorInterface) newSimulator, this.parent);
    }

}
