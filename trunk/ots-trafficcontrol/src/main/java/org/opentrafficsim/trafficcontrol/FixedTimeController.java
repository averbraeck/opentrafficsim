package org.opentrafficsim.trafficcontrol;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.event.EventInterface;

import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Time;
import org.djutils.exceptions.Throw;
import org.djutils.immutablecollections.Immutable;
import org.djutils.immutablecollections.ImmutableArrayList;
import org.djutils.immutablecollections.ImmutableCollections;
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

/**
 * Fixed time traffic light control.
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
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
    @SuppressWarnings({"synthetic-access"})
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
        Throw.when(signalGroups.isEmpty(), IllegalArgumentException.class, "Signal groups may not be empty.");
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
        this.cycleTime = cycleTime;
        this.offset = offset;
        this.signalGroups = signalGroups;
        simulator.scheduleEventAbs(Time.ZERO, this, this, "setup", new Object[] {simulator, network});
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

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "FixedTimeController [cycleTime=" + this.cycleTime + ", offset=" + this.offset + ", signalGroups="
                + this.signalGroups + ", full id=" + this.getFullId() + "]";
    }

    /**
     * <p>
     * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
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
         * @return id.
         */
        @Override
        public String getId()
        {
            return this.id;
        }

        /**
         * @param controllerOffset Duration;
         * @param cycleTime Duration;
         * @param sim OTSSimulatorInterface;
         * @param network Network;
         * @throws SimRuntimeException when traffic light does not exist in the network
         */
        public void startup(final Duration controllerOffset, final Duration cycleTime, final OTSSimulatorInterface sim,
                final Network network) throws SimRuntimeException
        {
            this.simulator = sim;
            double totalOffsetSI = this.offset.si + controllerOffset.si;
            while (totalOffsetSI < 0.0)
            {
                totalOffsetSI += cycleTime.si;
            }
            Duration totalOffset = Duration.createSI(totalOffsetSI % cycleTime.si);
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

            Duration inCycleTime = Duration.createSI(totalOffset.si - cycleTime.si);
            while (inCycleTime.si < 0)
            {
                inCycleTime = inCycleTime.plus(cycleTime);
            }
            Duration duration;
            if (inCycleTime.si < this.preGreen.si)
            {
                setTrafficLights(TrafficLightColor.PREGREEN);
                duration = this.preGreen.minus(inCycleTime);
            }
            else if (inCycleTime.si < this.preGreen.si + this.green.si)
            {
                setTrafficLights(TrafficLightColor.GREEN);
                duration = this.preGreen.plus(this.green).minus(inCycleTime);
            }
            else if (inCycleTime.si < this.preGreen.si + this.green.si + this.yellow.si)
            {
                setTrafficLights(TrafficLightColor.YELLOW);
                duration = this.preGreen.plus(this.green).plus(this.yellow).minus(inCycleTime);
            }
            else
            {
                setTrafficLights(TrafficLightColor.RED);
                duration = cycleTime.minus(inCycleTime);
            }
            this.simulator.scheduleEventRel(duration, this, this, "updateColors", null);
        }

        /**
         * Updates the color of the traffic lights.
         */
        @SuppressWarnings("unused")
        private void updateColors()
        {
            Duration duration = Duration.ZERO;
            TrafficLightColor color = this.trafficLights.get(0).getTrafficLightColor();
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
            try
            {
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
         * @param trafficLightColor TrafficLightColor; the new traffic light color
         */
        private void setTrafficLights(final TrafficLightColor trafficLightColor)
        {
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
        public boolean equals(Object obj)
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

        /** {@inheritDoc} */
        @Override
        public String toString()
        {
            return "SignalGroup [id=" + this.id + ", trafficLightIds=" + this.trafficLightIds + ", offset=" + this.offset
                    + ", preGreen=" + this.preGreen + ", green=" + this.green + ", yellow=" + this.yellow + "]";
        }

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

}
