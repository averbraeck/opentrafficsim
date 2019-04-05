package org.opentrafficsim.road.gtu.generator.od;

import java.util.HashMap;
import java.util.Map;

import org.djunits.value.vdouble.scalar.Frequency;
import org.djunits.value.vdouble.scalar.Length;
import org.djutils.exceptions.Throw;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.idgenerator.IdGenerator;
import org.opentrafficsim.core.network.LinkType;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.road.gtu.generator.CFBARoomChecker;
import org.opentrafficsim.road.gtu.generator.GeneratorPositions.LaneBias;
import org.opentrafficsim.road.gtu.generator.GeneratorPositions.LaneBiases;
import org.opentrafficsim.road.gtu.generator.LaneBasedGTUGenerator.RoomChecker;
import org.opentrafficsim.road.gtu.generator.MarkovCorrelation;
import org.opentrafficsim.road.gtu.generator.headway.ArrivalsHeadwayGenerator.HeadwayDistribution;
import org.opentrafficsim.road.network.RoadNetwork;
import org.opentrafficsim.road.network.lane.Lane;

/**
 * Options for vehicle generation based on an OD matrix.
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 6 dec. 2017 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class ODOptions
{
    /** Headway randomization option. */
    public static final Option<HeadwayDistribution> HEADWAY_DIST =
            new Option<>("headway distribution", HeadwayDistribution.EXPONENTIAL);

    /** ID generator option. */
    public static final Option<IdGenerator> GTU_ID = new Option<>("gtu id", new IdGenerator(""));

    /** GTU characteristics generator option. */
    public static final Option<GTUCharacteristicsGeneratorOD> GTU_TYPE =
            new Option<>("gtu type", new DefaultGTUCharacteristicsGeneratorOD());

    /** Room checker option. */
    public static final Option<RoomChecker> ROOM_CHECKER = new Option<>("room checker", new CFBARoomChecker());

    /** Markov chain for GTU type option. */
    public static final Option<MarkovCorrelation<GTUType, Frequency>> MARKOV = new Option<>("markov", null);

    /** Initial distance over which lane changes shouldn't be performed option. */
    public static final Option<Length> NO_LC_DIST = new Option<>("no lc distance", null);

    /** Options overall. */
    private OptionSet<Void> options = new OptionSet<>();

    /** Options per lane. */
    private OptionSet<Lane> laneOptions = new OptionSet<>();

    /** Options per node. */
    private OptionSet<Node> nodeOptions = new OptionSet<>();

    /** Options per road type. */
    private OptionSet<LinkType> linkTypeOptions = new OptionSet<>();

    /** cache for lane biases per network. */
    private static final Map<RoadNetwork, Option<LaneBiases>> LANE_BIAS_CACHE = new HashMap<>();

    /**
     * Lane bias. Default is Truck: truck right (strong right, max 2 lanes), Vehicle (other): weak left.
     * @param network the network for which to return the lane bias
     * @return the lane bias
     */
    public static final Option<LaneBiases> getLaneBiasOption(final RoadNetwork network)
    {
        Option<LaneBiases> laneBiases = LANE_BIAS_CACHE.get(network);
        if (laneBiases == null)
        {
            laneBiases = new Option<>("lane bias",
                    new LaneBiases().addBias(network.getGtuType(GTUType.DEFAULTS.TRUCK), LaneBias.TRUCK_RIGHT)
                            .addBias(network.getGtuType(GTUType.DEFAULTS.VEHICLE), LaneBias.WEAK_LEFT));
            LANE_BIAS_CACHE.put(network, laneBiases);
        }
        return laneBiases;
    }

    /**
     * Set option value.
     * @param option Option&lt;K&gt;; option
     * @param value K; option value
     * @param <K> value type
     * @return this option set
     */
    public final <K> ODOptions set(final Option<K> option, final K value)
    {
        this.options.set(null, option, value);
        return this;
    }

    /**
     * Set option value for lane.
     * @param lane Lane; lane
     * @param option Option&lt;K&gt;; option
     * @param value K; option value
     * @param <K> value type
     * @return this option set
     */
    public final <K> ODOptions set(final Lane lane, final Option<K> option, final K value)
    {
        this.laneOptions.set(lane, option, value);
        return this;
    }

    /**
     * Set option value for node.
     * @param node Node; node
     * @param option Option&lt;K&gt;; option
     * @param value K; option value
     * @param <K> value type
     * @return this option set
     */
    public final <K> ODOptions set(final Node node, final Option<K> option, final K value)
    {
        this.nodeOptions.set(node, option, value);
        return this;
    }

    /**
     * Set option value for link type.
     * @param linkType LinkType; link type
     * @param option Option&lt;K&gt;; option
     * @param value K; option value
     * @param <K> value type
     * @return this option set
     */
    public final <K> ODOptions set(final LinkType linkType, final Option<K> option, final K value)
    {
        this.linkTypeOptions.set(linkType, option, value);
        return this;
    }

    /**
     * Get option value. If a value is specified for a specific category, it is returned. The following order is used:
     * <ul>
     * <li>{@code Lane}</li>
     * <li>{@code Node} (origin)</li>
     * <li>{@code LinkType}</li>
     * <li>None (global option value)</li>
     * <li>Default option value</li>
     * </ul>
     * @param option Option&lt;K&gt;; option
     * @param lane Lane; lane to obtain specific option value, may be null
     * @param node Node; node to obtain specific option value, may be null
     * @param linkType LinkType; link type to obtain specific option value, may be null
     * @param <K> value type
     * @return K; option value
     */
    public final <K> K get(final Option<K> option, final Lane lane, final Node node, final LinkType linkType)
    {
        Throw.whenNull(option, "Option may not be null.");
        K value = this.laneOptions.get(lane, option);
        if (value != null)
        {
            return value;
        }
        value = this.nodeOptions.get(node, option);
        if (value != null)
        {
            return value;
        }
        value = this.linkTypeOptions.get(linkType, option);
        if (value != null)
        {
            return value;
        }
        value = this.options.get(null, option);
        if (value != null)
        {
            return value;
        }
        return option.getDefaultValue();
    }

    /**
     * Utility class to store options.
     * <p>
     * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
     * <p>
     * @version $Revision$, $LastChangedDate$, by $Author$, initial version 6 dec. 2017 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
     * @param <K> option value type
     */
    public static final class Option<K>
    {

        /** Id. */
        private final String id;

        /** Default value. */
        private final K defaultValue;

        /**
         * Constructor.
         * @param id String; id
         * @param defaultValue K; default value
         */
        Option(final String id, final K defaultValue)
        {
            this.id = id;
            this.defaultValue = defaultValue;
        }

        /**
         * Returns the default value.
         * @return default value
         */
        public K getDefaultValue()
        {
            return this.defaultValue;
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
            Option<?> other = (Option<?>) obj;
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

        /** {@inheritDoc} */
        @Override
        public String toString()
        {
            return "Option [id=" + this.id + "]";
        }

    }

    /**
     * Single set of options for a certain category, i.e. lane, node, link type or null (i.e. global).
     * <p>
     * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
     * <p>
     * @version $Revision$, $LastChangedDate$, by $Author$, initial version 11 jan. 2018 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
     * @param <C> category type, i.e. {@code Lane}, {@code Node}, {@code LinkType} or {@code Void} (i.e. global).
     */
    private class OptionSet<C>
    {

        /** Options. */
        private Map<C, Map<Option<?>, Object>> optionsSet = new HashMap<>();

        /**
         * Constructor.
         */
        OptionSet()
        {
            //
        }

        /**
         * Set value in option set.
         * @param category C; category
         * @param option Option&lt;K&gt;; option
         * @param value K; value
         * @param <K> option value type
         */
        public <K> void set(final C category, final Option<K> option, final K value)
        {
            Map<Option<?>, Object> map = this.optionsSet.get(category);
            if (map == null)
            {
                map = new HashMap<>();
                this.optionsSet.put(category, map);
            }
            map.put(option, value);
        }

        /**
         * Returns the option value for the category.
         * @param category C; category
         * @param option Option&lt;K&gt;; option
         * @return option value for the category
         * @param <K> value type
         */
        @SuppressWarnings("unchecked")
        public <K> K get(final C category, final Option<K> option)
        {
            if (!this.optionsSet.containsKey(category))
            {
                return null;
            }
            return (K) this.optionsSet.get(category).get(option);
        }

    }

}
