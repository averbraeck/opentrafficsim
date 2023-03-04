package org.opentrafficsim.road.od;

import java.util.LinkedHashMap;
import java.util.Map;

import org.djunits.value.vdouble.scalar.Frequency;
import org.djunits.value.vdouble.scalar.Length;
import org.djutils.exceptions.Throw;
import org.opentrafficsim.core.gtu.GtuErrorHandler;
import org.opentrafficsim.core.gtu.GtuType;
import org.opentrafficsim.core.idgenerator.IdGenerator;
import org.opentrafficsim.core.network.LinkType;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.road.gtu.generator.CfBaRoomChecker;
import org.opentrafficsim.road.gtu.generator.GeneratorPositions.LaneBiases;
import org.opentrafficsim.road.gtu.generator.LaneBasedGtuGenerator.RoomChecker;
import org.opentrafficsim.road.gtu.generator.MarkovCorrelation;
import org.opentrafficsim.road.gtu.generator.characteristics.LaneBasedGtuCharacteristicsGeneratorOd;
import org.opentrafficsim.road.gtu.generator.headway.ArrivalsHeadwayGenerator.HeadwayDistribution;
import org.opentrafficsim.road.network.lane.Lane;

/**
 * Options for vehicle generation based on an OD matrix.
 * <p>
 * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class OdOptions
{
    /** Headway randomization option. */
    public static final Option<HeadwayDistribution> HEADWAY_DIST =
            new Option<>("headway distribution", HeadwayDistribution.EXPONENTIAL);

    /** ID generator option. */
    public static final Option<IdGenerator> GTU_ID = new Option<>("gtu id", new IdGenerator(""));

    /** GTU characteristics generator option. */
    public static final Option<LaneBasedGtuCharacteristicsGeneratorOd> GTU_TYPE = new Option<>("gtu type", null);

    /** Room checker option. */
    public static final Option<RoomChecker> ROOM_CHECKER = new Option<>("room checker", new CfBaRoomChecker());

    /** Markov chain for GTU type option. */
    public static final Option<MarkovCorrelation<GtuType, Frequency>> MARKOV = new Option<>("markov", null);

    /** Initial distance over which lane changes shouldn't be performed option. */
    public static final Option<Length> NO_LC_DIST = new Option<>("no lc distance", null);

    /** Whether to perform instantaneous lane changes. */
    public static final Option<Boolean> INSTANT_LC = new Option<>("instant lc", false);

    /** Error handler when GTU exceptions occur. */
    public static final Option<GtuErrorHandler> ERROR_HANDLER = new Option<>("error handler", GtuErrorHandler.THROW);

    /** Lane bias. Default is none, i.e. uniform distribution over lanes for all GTU types. */
    public static final Option<LaneBiases> LANE_BIAS = new Option<>("lane bias", new LaneBiases());

    /** Options overall. */
    private OptionSet<Void> options = new OptionSet<>();

    /** Options per lane. */
    private OptionSet<Lane> laneOptions = new OptionSet<>();

    /** Options per node. */
    private OptionSet<Node> nodeOptions = new OptionSet<>();

    /** Options per road type. */
    private OptionSet<LinkType> linkTypeOptions = new OptionSet<>();

    /**
     * Set option value.
     * @param option Option&lt;K&gt;; option
     * @param value K; option value
     * @param <K> value type
     * @return this option set
     */
    public final <K> OdOptions set(final Option<K> option, final K value)
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
    public final <K> OdOptions set(final Lane lane, final Option<K> option, final K value)
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
    public final <K> OdOptions set(final Node node, final Option<K> option, final K value)
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
    public final <K> OdOptions set(final LinkType linkType, final Option<K> option, final K value)
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
     * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
     * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
     * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
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
     * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
     * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
     * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
     * @param <C> category type, i.e. {@code Lane}, {@code Node}, {@code LinkType} or {@code Void} (i.e. global).
     */
    private class OptionSet<C>
    {

        /** Options. */
        private Map<C, Map<Option<?>, Object>> optionsSet = new LinkedHashMap<>();

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
                map = new LinkedHashMap<>();
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
