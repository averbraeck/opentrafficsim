package org.opentrafficsim.trafficcontrol;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Time;
import org.djutils.event.EventInterface;
import org.djutils.exceptions.Throw;
import org.djutils.immutablecollections.Immutable;
import org.djutils.immutablecollections.ImmutableArrayList;
import org.djutils.immutablecollections.ImmutableHashSet;
import org.djutils.immutablecollections.ImmutableList;
import org.djutils.immutablecollections.ImmutableMap;
import org.djutils.immutablecollections.ImmutableSet;
import org.opentrafficsim.base.Identifiable;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.network.Network;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.object.InvisibleObjectInterface;
import org.opentrafficsim.road.network.lane.object.trafficlight.TrafficLight;
import org.opentrafficsim.road.network.lane.object.trafficlight.TrafficLightColor;

import nl.tudelft.simulation.dsol.SimRuntimeException;

/**
 * Fixed time traffic light control.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 21 feb. 2019 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class FixedTimeController extends AbstractTrafficController
{

    /** */
    private static final long serialVersionUID = 20190221L;

    /** Cycle time. */
    private final Duration cycleTime;

    /** Offset. */
    private final Duration offset;

    /** Signal groups, for cloning. */
    private final Set<SignalGroup> signalGroups;

    /**
     * Constructor for fixed time traffic controller.
     * @param id String; id
     * @param simulator OTSSimulatorInterface; simulator
     * @param network Network; network
     * @param offset Duration; off set from simulation start time
     * @param cycleTime Duration; cycle time
     * @param signalGroups Set&lt;SignalGroup&gt;; signal groups
     * @throws SimRuntimeException simulator is past zero time
     */
    public FixedTimeController(final String id, final OTSSimulatorInterface simulator, final Network network,
            final Duration cycleTime, final Duration offset, final Set<SignalGroup> signalGroups) throws SimRuntimeException
    {
        super(id, simulator);
        Throw.whenNull(simulator, "Simulator may not be null.");
        Throw.whenNull(network, "Network may not be null.");
        Throw.whenNull(cycleTime, "Cycle time may not be null.");
        Throw.whenNull(offset, "Offset may not be null.");
        Throw.whenNull(signalGroups, "Signal groups may not be null.");
        Throw.when(cycleTime.le0(), IllegalArgumentException.class, "Cycle time must be positive.");
        // Throw.when(signalGroups.isEmpty(), IllegalArgumentException.class, "Signal groups may not be empty.");
        /*- This is no longer considered an error
        for (SignalGroup signalGroup1 : signalGroups)
        {
            for (SignalGroup signalGroup2 : signalGroups)
            {
                if (!signalGroup1.equals(signalGroup2))
                {
                    Throw.when(!ImmutableCollections.disjoint(signalGroup1.trafficLightIds, signalGroup2.trafficLightIds),
                            IllegalArgumentException.class, "A traffic light is in both signal group %s and signal group %s.",
                            signalGroup1.getId(), signalGroup2.getId());
                }
            }
        }
        */
        this.cycleTime = cycleTime;
        this.offset = offset;
        this.signalGroups = new LinkedHashSet<>(signalGroups); // make a copy so we can modify it.
        // Identify traffic lights that are present in more than one signal group
        Map<String, List<SignalGroup>> signalGroupsOfTrafficLight = new LinkedHashMap<>();
        for (SignalGroup sg : this.signalGroups)
        {
            for (String trafficLightId : sg.getTrafficLightIds())
            {
                List<SignalGroup> sgList = signalGroupsOfTrafficLight.get(trafficLightId);
                if (null == sgList)
                {
                    sgList = new ArrayList<>();
                    signalGroupsOfTrafficLight.put(trafficLightId, sgList);
                }
                sgList.add(sg);
            }
        }
        // Collect all flanks that persist for nonzero duration
        int nextNumber = 0;
        for (String trafficLightId : signalGroupsOfTrafficLight.keySet())
        {
            List<SignalGroup> sgList = signalGroupsOfTrafficLight.get(trafficLightId);
            if (sgList.size() > 1)
            {
                // System.out.println("Signal " + trafficLightId + " is used in multiple signal groups");
                // Check for overlapping or adjacent green phases
                List<Flank> flanks = new ArrayList<>();
                for (SignalGroup sg : sgList)
                {
                    double sgOffset = sg.getOffset().si;
                    double preGreenDuration = sg.getPreGreen().si;
                    if (preGreenDuration > 0)
                    {
                        flanks.add(new Flank(sgOffset % this.cycleTime.si, TrafficLightColor.PREGREEN));
                        sgOffset += preGreenDuration;
                    }
                    flanks.add(new Flank(sgOffset % this.cycleTime.si, TrafficLightColor.GREEN));
                    sgOffset += sg.getGreen().si;
                    double yellowDuration = sg.getYellow().si;
                    if (yellowDuration > 0)
                    {
                        flanks.add(new Flank(sgOffset % this.cycleTime.si, TrafficLightColor.YELLOW));
                        sgOffset += yellowDuration;
                    }
                    flanks.add(new Flank(sgOffset % this.cycleTime.si, TrafficLightColor.RED));
                }
                Collections.sort(flanks);
                /*-
                System.out.println("Collected " + flanks.size() + " flanks:");
                for (Flank flank : flanks)
                {
                    System.out.println(flank);
                }
                */
                boolean combined = false;
                int greenCount = 0;
                for (int index = 0; index < flanks.size(); index++)
                {
                    Flank flank = flanks.get(index);
                    TrafficLightColor nextColor = flank.getTrafficLightColor();
                    if (TrafficLightColor.GREEN == nextColor)
                    {
                        greenCount++;
                        if (greenCount > 1)
                        {
                            flanks.remove(index);
                            index--;
                            combined = true;
                            continue;
                        }
                    }
                    else if (TrafficLightColor.YELLOW == nextColor)
                    {
                        if (greenCount > 1)
                        {
                            flanks.remove(index);
                            index--;
                            continue;
                        }
                    }
                    else if (TrafficLightColor.RED == nextColor)
                    {
                        greenCount--;
                        if (greenCount > 0)
                        {
                            flanks.remove(index);
                            index--;
                            continue;
                        }
                    }
                }
                /*-
                System.out.println("Reduced " + flanks.size() + " flanks:");
                for (Flank flank : flanks)
                {
                    System.out.println(flank);
                }
                */
                if (combined)
                {
                    // Traffic light has adjacent or overlapping green realizations.
                    String newSignalGroupName = "CombinedSignalGroups_";
                    // Remove the traffic light from the current signal groups that it is part of
                    for (SignalGroup sg : sgList)
                    {
                        // System.out.println("Reducing " + sg);
                        newSignalGroupName = newSignalGroupName + "_" + sg.getId();
                        Set<String> trafficLightIds = new LinkedHashSet<>();
                        for (String tlId : sg.getTrafficLightIds())
                        {
                            if (!tlId.equals(trafficLightId))
                            {
                                trafficLightIds.add(tlId);
                            }
                        }
                        this.signalGroups.remove(sg);
                        if (trafficLightIds.size() > 0)
                        {
                            SignalGroup newSignalGroup = new SignalGroup(sg.getId(), trafficLightIds, sg.getOffset(),
                                    sg.getPreGreen(), sg.getGreen(), sg.getYellow());
                            // System.out.println("reduced signal group " + newSignalGroup);
                            this.signalGroups.add(newSignalGroup);
                        }
                        /*-
                        else
                        {
                            System.out.println("Signal group became empty");
                        }
                        */
                    }
                    // Create new signal group(s) for each green realization of the traffic light
                    Duration sgOffset = null;
                    Duration preGreen = Duration.ZERO;
                    Duration green = null;
                    Duration yellow = Duration.ZERO;
                    double cumulativeOffset = 0;
                    for (int index = 0; index < flanks.size(); index++)
                    {
                        Flank flank = flanks.get(index);
                        if (null == sgOffset)
                        {
                            sgOffset = Duration.instantiateSI(flank.getOffset());
                        }
                        if (TrafficLightColor.GREEN == flank.getTrafficLightColor())
                        {
                            preGreen = Duration.instantiateSI(flank.getOffset() - sgOffset.si);
                        }
                        if (TrafficLightColor.YELLOW == flank.getTrafficLightColor())
                        {
                            green = Duration.instantiateSI(flank.getOffset() - cumulativeOffset);
                        }
                        if (TrafficLightColor.RED == flank.getTrafficLightColor())
                        {
                            nextNumber++;
                            yellow = Duration.instantiateSI(flank.getOffset() - cumulativeOffset);
                            Set<String> trafficLightIds = new LinkedHashSet<>(1);
                            trafficLightIds.add(trafficLightId);
                            SignalGroup newSignalGroup = new SignalGroup(newSignalGroupName + "_" + nextNumber, trafficLightIds,
                                    sgOffset, preGreen, green, yellow);
                            this.signalGroups.add(newSignalGroup);
                            // System.out.println("Created signal group " + newSignalGroup);
                        }
                        cumulativeOffset = flank.getOffset();
                    }
                }
            }
        }
        // Schedule setup at time == 0 (when the network should be fully created and all traffic lights have been constructed)
        simulator.scheduleEventAbs(Time.ZERO, this, this, "setup", new Object[] { simulator, network });
    }

    /**
     * Initiates all traffic control events.
     * @param simulator OTSSimulatorInterface; simulator
     * @param network Network; network
     * @throws SimRuntimeException when traffic light does not exist in the network
     */
    @SuppressWarnings("unused")
    private void setup(final OTSSimulatorInterface simulator, final Network network) throws SimRuntimeException
    {
        for (SignalGroup signalGroup : this.signalGroups)
        {
            signalGroup.startup(this.offset, this.cycleTime, simulator, network);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void notify(final EventInterface event) throws RemoteException
    {
        // nothing
    }

    /** {@inheritDoc} */
    @Override
    public InvisibleObjectInterface clone(final OTSSimulatorInterface newSimulator, final Network newNetwork)
            throws NetworkException
    {
        Set<SignalGroup> signalGroupsCloned = new LinkedHashSet<>();
        for (SignalGroup signalGroup : this.signalGroups)
        {
            signalGroupsCloned.add(signalGroup.clone());
        }
        try
        {
            return new FixedTimeController(getId(), newSimulator, newNetwork, this.cycleTime, this.offset, signalGroupsCloned);
        }
        catch (SimRuntimeException exception)
        {
            throw new RuntimeException("Cloning using a simulator that is not at time 0.");
        }
    }

    /** {@inheritDoc} */
    @Override
    public String getFullId()
    {
        return getId();
    }

    /**
     * @return cycleTime.
     */
    public final Duration getCycleTime()
    {
        return this.cycleTime;
    }

    /**
     * @return offset.
     */
    public final Duration getOffset()
    {
        return this.offset;
    }

    /**
     * @return signalGroups.
     */
    public final Set<SignalGroup> getSignalGroups()
    {
        return this.signalGroups;
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "FixedTimeController [cycleTime=" + this.cycleTime + ", offset=" + this.offset + ", signalGroups="
                + this.signalGroups + ", full id=" + this.getFullId() + "]";
    }

    /**
     * Fixed time signal group.
     * <p>
     * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
     * <p>
     * @version $Revision$, $LastChangedDate$, by $Author$, initial version 21 feb. 2019 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
     */
    public static class SignalGroup implements Identifiable
    {

        /** Id. */
        private final String id;

        /** Traffic light ids. */
        private final ImmutableSet<String> trafficLightIds;

        /** Offset from start of cycle. */
        private final Duration offset;

        /** Pre-green duration. */
        private final Duration preGreen;

        /** Green duration. */
        private final Duration green;

        /** Yellow duration. */
        private final Duration yellow;

        /** Current color (according to <b>this</b> SignalGroup). */
        private TrafficLightColor currentColor = TrafficLightColor.RED;

        // The following properties are remembered for the updates after the startup

        /** Traffic light objects. */
        private List<TrafficLight> trafficLights;

        /** Simulator. */
        private OTSSimulatorInterface simulator;

        /** Red time. */
        private Duration red;

        /**
         * Constructor without pre-green duration.
         * @param id String; id
         * @param trafficLightIds Set&lt;String&gt;; traffic light ids
         * @param offset Duration; offset from start of cycle
         * @param green Duration; green duration
         * @param yellow Duration; yellow duration
         */
        public SignalGroup(final String id, final Set<String> trafficLightIds, final Duration offset, final Duration green,
                final Duration yellow)
        {
            this(id, trafficLightIds, offset, Duration.ZERO, green, yellow);
        }

        /**
         * Constructor with pre-green duration.
         * @param id String; id
         * @param trafficLightIds Set&lt;String&gt;; traffic light ids
         * @param offset Duration; offset from start of cycle
         * @param preGreen Duration; pre-green duration
         * @param green Duration; green duration
         * @param yellow Duration; yellow duration
         */
        public SignalGroup(final String id, final Set<String> trafficLightIds, final Duration offset, final Duration preGreen,
                final Duration green, final Duration yellow)
        {
            Throw.whenNull(id, "Id may not be null.");
            Throw.whenNull(trafficLightIds, "Traffic light ids may not be null.");
            Throw.whenNull(offset, "Offset may not be null.");
            Throw.whenNull(preGreen, "Pre-green may not be null.");
            Throw.when(preGreen.lt(Duration.ZERO), IllegalArgumentException.class, "Pre green duration may not be negative");
            Throw.whenNull(green, "Green may not be null.");
            Throw.when(green.lt(Duration.ZERO), IllegalArgumentException.class, "Green duration may not be negative");
            Throw.whenNull(yellow, "Yellow may not be null.");
            Throw.when(yellow.lt(Duration.ZERO), IllegalArgumentException.class, "Yellow duration may not be negative");
            Throw.when(trafficLightIds.isEmpty(), IllegalArgumentException.class, "Traffic light ids may not be empty.");
            this.id = id;
            this.trafficLightIds = new ImmutableHashSet<>(trafficLightIds, Immutable.COPY);
            this.offset = offset;
            this.preGreen = preGreen;
            this.green = green;
            this.yellow = yellow;
        }

        /**
         * Retrieve the id of this signal group.
         * @return String
         */
        @Override
        public String getId()
        {
            return this.id;
        }

        /**
         * Connect to the traffic lights in the network, initialize the traffic lights to their initial color and schedule the
         * first transitions.
         * @param controllerOffset Duration;
         * @param cycleTime Duration;
         * @param theSimulator OTSSimulatorInterface;
         * @param network Network;
         * @throws SimRuntimeException when traffic light does not exist in the network
         */
        public void startup(final Duration controllerOffset, final Duration cycleTime, final OTSSimulatorInterface theSimulator,
                final Network network) throws SimRuntimeException
        {
            this.simulator = theSimulator;
            double totalOffsetSI = this.offset.si + controllerOffset.si;
            while (totalOffsetSI < 0.0)
            {
                totalOffsetSI += cycleTime.si;
            }
            Duration totalOffset = Duration.instantiateSI(totalOffsetSI % cycleTime.si);
            this.red = cycleTime.minus(this.preGreen).minus(this.green).minus(this.yellow);
            Throw.when(this.red.lt0(), IllegalArgumentException.class, "Cycle time shorter than sum of non-red times.");

            this.trafficLights = new ArrayList<>();
            ImmutableMap<String, TrafficLight> trafficLightObjects = network.getObjectMap(TrafficLight.class);
            for (String trafficLightId : this.trafficLightIds)
            {
                TrafficLight trafficLight = trafficLightObjects.get(trafficLightId);
                if (null == trafficLight) // Traffic light not found using id; try to find it by full id
                {
                    // TODO: networkId.trafficLightId? Shouldn't that be linkId.trafficLightId?
                    trafficLight = trafficLightObjects.get(network.getId() + "." + trafficLightId);
                }
                Throw.when(trafficLight == null, SimRuntimeException.class, "Traffic light \"" + trafficLightId
                        + "\" in fixed time controller could not be found in network " + network.getId() + ".");
                this.trafficLights.add(trafficLight);
            }
            // System.out.println("Starting up " + this);
            Duration inCycleTime = Duration.ZERO.minus(totalOffset);
            while (inCycleTime.si < 0)
            {
                inCycleTime = inCycleTime.plus(cycleTime);
            }
            Duration duration = null;
            if (inCycleTime.ge(this.preGreen.plus(this.green).plus(this.yellow)))
            {
                this.currentColor = TrafficLightColor.RED; // redundant; it is already RED
                duration = cycleTime.minus(inCycleTime);
            }
            else if (inCycleTime.lt(this.preGreen))
            {
                this.currentColor = TrafficLightColor.PREGREEN;
                duration = this.preGreen.minus(inCycleTime);
            }
            else if (inCycleTime.lt(this.preGreen.plus(this.green)))
            {
                this.currentColor = TrafficLightColor.GREEN;
                duration = this.preGreen.plus(this.green).minus(inCycleTime);
            }
            else if (inCycleTime.lt(this.preGreen.plus(this.green).plus(this.yellow)))
            {
                this.currentColor = TrafficLightColor.YELLOW;
                duration = this.preGreen.plus(this.green).plus(this.yellow).minus(inCycleTime);
            }
            else
            {
                throw new SimRuntimeException("Cannot determine initial state of signal group " + this);
            }
            // System.out.println("Initial color is " + this.currentColor + " next flank after " + duration);
            setTrafficLights(this.currentColor);
            this.simulator.scheduleEventRel(duration, this, this, "updateColors", null);
        }

        /**
         * Updates the color of the traffic lights.
         */
        @SuppressWarnings("unused")
        private void updateColors()
        {
            try
            {
                Duration duration = Duration.ZERO;
                TrafficLightColor color = this.currentColor;
                while (duration.le0())
                {
                    switch (color)
                    {
                        case PREGREEN:
                            color = TrafficLightColor.GREEN;
                            duration = this.green;
                            break;
                        case GREEN:
                            color = TrafficLightColor.YELLOW;
                            duration = this.yellow;
                            break;
                        case YELLOW:
                            color = TrafficLightColor.RED;
                            duration = this.red;
                            break;
                        case RED:
                            color = TrafficLightColor.PREGREEN;
                            duration = this.preGreen;
                            break;
                        default:
                            throw new RuntimeException("Cannot happen.");
                    }
                }
                setTrafficLights(color);
                this.simulator.scheduleEventRel(duration, this, this, "updateColors", null);
            }
            catch (SimRuntimeException exception)
            {
                // cannot happen; we check all durations for consistency
                throw new RuntimeException(exception);
            }
        }

        /**
         * Change the color of our traffic lights.
         * @param trafficLightColor TrafficLightColor; the new traffic light color to show
         */
        private void setTrafficLights(final TrafficLightColor trafficLightColor)
        {
            this.currentColor = trafficLightColor;
            for (TrafficLight trafficLight : this.trafficLights)
            {
                trafficLight.setTrafficLightColor(trafficLightColor);
            }
        }

        /**
         * Clones the object for a cloned simulation.
         */
        @Override
        public SignalGroup clone()
        {
            return new SignalGroup(getId(), this.trafficLightIds.toSet(), this.offset, this.preGreen, this.green, this.yellow);
        }

        /** {@inheritDoc} */
        @Override
        public int hashCode()
        {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((this.id == null) ? 0 : this.id.hashCode());
            return result;
        }

        /** {@inheritDoc} */
        @Override
        public boolean equals(final Object obj)
        {
            if (this == obj)
            {
                return true;
            }
            if (obj == null)
            {
                return false;
            }
            if (getClass() != obj.getClass())
            {
                return false;
            }
            SignalGroup other = (SignalGroup) obj;
            if (this.id == null)
            {
                if (other.id != null)
                {
                    return false;
                }
            }
            else if (!this.id.equals(other.id))
            {
                return false;
            }
            return true;
        }

        /**
         * @return trafficLights.
         */
        public final ImmutableList<TrafficLight> getTrafficLights()
        {
            return new ImmutableArrayList<>(this.trafficLights);
        }

        /**
         * @return red.
         */
        public final Duration getRed()
        {
            return this.red;
        }

        /**
         * @return trafficLightIds.
         */
        public final ImmutableSet<String> getTrafficLightIds()
        {
            return this.trafficLightIds;
        }

        /**
         * @return offset.
         */
        public final Duration getOffset()
        {
            return this.offset;
        }

        /**
         * @return preGreen.
         */
        public final Duration getPreGreen()
        {
            return this.preGreen;
        }

        /**
         * @return green.
         */
        public final Duration getGreen()
        {
            return this.green;
        }

        /**
         * @return yellow.
         */
        public final Duration getYellow()
        {
            return this.yellow;
        }

        /**
         * Retrieve the current color of this SignalGroup.
         * @return TrafficLightColor; the current color of this signal group.
         */
        public TrafficLightColor getCurrentColor()
        {
            return currentColor;
        }

        /** {@inheritDoc} */
        @Override
        public String toString()
        {
            return "SignalGroup [id=" + this.id + ", trafficLightIds=" + this.trafficLightIds + ", offset=" + this.offset
                    + ", preGreen=" + this.preGreen + ", green=" + this.green + ", yellow=" + this.yellow + "currentColor="
                    + this.currentColor + "]";
        }

    }

    /**
     * Storage of an offset within a cycle and the new traffic light color. Used to sort the flanks.
     */
    class Flank implements Comparable<Flank>
    {
        /** When (in the cycle time is this transition. */
        private final double offset;

        /** What is the color after this transition. */
        private final TrafficLightColor newColor;

        /**
         * Construct a new Flank.
         * @param offset double; offset within the cycle time
         * @param newColor TrafficLightColor; color to show after this transition
         */
        Flank(final double offset, final TrafficLightColor newColor)
        {
            this.offset = offset;
            this.newColor = newColor;
        }

        /**
         * Retrieve the offset.
         * @return double; the offset
         */
        public double getOffset()
        {
            return this.offset;
        }

        /**
         * Retrieve the color after this transition.
         * @return TrafficLightColor; the color after this transition
         */
        public TrafficLightColor getTrafficLightColor()
        {
            return this.newColor;
        }

        @Override
        public String toString()
        {
            return "Flank [offset=" + offset + ", newColor=" + newColor + "]";
        }

        /** Cumulative rounding errors are less than this value and traffic light transitions are spaced further apart. */
        private static final double COMPARE_MARGIN = 0.01;

        @Override
        public int compareTo(final Flank o)
        {
            double deltaOffset = this.offset - o.offset;
            if (Math.abs(deltaOffset) < COMPARE_MARGIN)
            {
                deltaOffset = 0;
            }
            if (deltaOffset > 0)
            {
                return 1;
            }
            if (deltaOffset < 0)
            {
                return -1;
            }
            if (TrafficLightColor.GREEN == this.newColor && TrafficLightColor.GREEN != o.newColor)
            {
                return -1;
            }
            if (TrafficLightColor.GREEN == o.newColor && TrafficLightColor.GREEN != this.newColor)
            {
                return 1;
            }
            return 0;
        }

    }

    /** {@inheritDoc} */
    @Override
    public Serializable getSourceId()
    {
        return getId();
    }

}
