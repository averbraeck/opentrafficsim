package org.opentrafficsim.road.gtu.generator.od;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.djunits.value.vdouble.scalar.Frequency;
import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.core.gtu.GTUCharacteristics;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.gtu.TemplateGTUType;
import org.opentrafficsim.core.gtu.Try;
import org.opentrafficsim.core.gtu.animation.GTUColorer;
import org.opentrafficsim.core.idgenerator.IdGenerator;
import org.opentrafficsim.core.network.LinkType;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.core.network.route.Route;
import org.opentrafficsim.road.gtu.animation.DefaultSwitchableGTUColorer;
import org.opentrafficsim.road.gtu.generator.CFBARoomChecker;
import org.opentrafficsim.road.gtu.generator.GeneratorPositions.LaneBias;
import org.opentrafficsim.road.gtu.generator.GeneratorPositions.LaneBiases;
import org.opentrafficsim.road.gtu.generator.LaneBasedGTUGenerator.RoomChecker;
import org.opentrafficsim.road.gtu.generator.MarkovCorrelation;
import org.opentrafficsim.road.gtu.generator.characteristics.LaneBasedGTUCharacteristics;
import org.opentrafficsim.road.gtu.generator.headway.ArrivalsHeadwayGenerator.HeadwayRandomization;
import org.opentrafficsim.road.gtu.lane.tactical.following.IDMPlusFactory;
import org.opentrafficsim.road.gtu.lane.tactical.lmrs.DefaultLMRSPerceptionFactory;
import org.opentrafficsim.road.gtu.lane.tactical.lmrs.LMRSFactory;
import org.opentrafficsim.road.gtu.strategical.LaneBasedStrategicalPlannerFactory;
import org.opentrafficsim.road.gtu.strategical.od.Categorization;
import org.opentrafficsim.road.gtu.strategical.od.Category;
import org.opentrafficsim.road.gtu.strategical.route.LaneBasedStrategicalRoutePlannerFactory;
import org.opentrafficsim.road.network.lane.Lane;

import nl.tudelft.simulation.jstats.streams.StreamInterface;
import nl.tudelft.simulation.language.Throw;

/**
 * Options for vehicle generation based on an OD matrix.
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 6 dec. 2017 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class ODOptions
{

    /** Value for option GTU_TYPE to use the shortest route if no route is given in the Categorization. */
    public static final GTUCharacteristicsGeneratorOD SHORTEST_ROUTE = new ShortestRouteGTUCharacteristicsGeneratorOD();

    /** Headway randomization option. */
    public static final Option<HeadwayRandomization> HEADWAY_DIST =
            new Option<>("headway distribution", HeadwayRandomization.EXPONENTIAL);

    /** ID generator option. */
    public static final Option<IdGenerator> GTU_ID = new Option<>("gtu id", new IdGenerator(""));

    /** GTU colorer option. */
    public static final Option<GTUColorer> GTU_COLORER = new Option<>("gtu colorer", new DefaultSwitchableGTUColorer());

    /** GTU characteristics generator option. */
    public static final Option<GTUCharacteristicsGeneratorOD> GTU_TYPE =
            new Option<>("gtu type", new DefaultGTUCharacteristicsGeneratorOD());

    /** Room checker option. */
    public static final Option<RoomChecker> ROOM_CHECKER = new Option<>("room checker", new CFBARoomChecker());

    /** Markov chain for GTU type option. */
    public static final Option<MarkovCorrelation<GTUType, Frequency>> MARKOV = new Option<>("markov", null);

    /** Lane bias. Default is Truck: truck right (strong right, max 2 lanes), Vehicle (other): weak left. */
    public static final Option<LaneBiases> LANE_BIAS = new Option<>("lane bias",
            new LaneBiases().addBias(GTUType.TRUCK, LaneBias.TRUCK_RIGHT).addBias(GTUType.VEHICLE, LaneBias.WEAK_LEFT));

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
     * @param lane Lane; lane to obtain specific option value, may be {@null}
     * @param node Node; node to obtain specific option value, may be {@null}
     * @param linkType LinkType; link type to obtain specific option value, may be {@null}
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
     * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
     * <p>
     * @version $Revision$, $LastChangedDate$, by $Author$, initial version 6 dec. 2017 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
     * @param <K> option value type
     */
    private static final class Option<K>
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
     * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
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
        public OptionSet()
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

    /**
     * Default generator for {@code LaneBasedGTUCharacteristics}.
     * <p>
     * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
     * <p>
     * @version $Revision$, $LastChangedDate$, by $Author$, initial version 10 dec. 2017 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
     */
    private static final class DefaultGTUCharacteristicsGeneratorOD implements GTUCharacteristicsGeneratorOD
    {
        /** Constructor. */
        DefaultGTUCharacteristicsGeneratorOD()
        {
            //
        }

        /** {@inheritDoc} */
        @Override
        public LaneBasedGTUCharacteristics draw(final Node origin, final Node destination, final Category category,
                final StreamInterface randomStream) throws GTUException
        {
            Categorization categorization = category.getCategorization();
            // GTU characteristics
            GTUType gtuType;
            if (categorization.entails(GTUType.class))
            {
                gtuType = category.get(GTUType.class);
            }
            else
            {
                gtuType = GTUType.CAR;
            }
            GTUCharacteristics gtuCharacteristics = Try.assign(() -> GTUType.defaultCharacteristics(gtuType),
                    "Exception while applying default GTU characteristics.");
            // strategical factory
            LaneBasedStrategicalPlannerFactory<?> laneBasedStrategicalPlannerFactory =
                    new LaneBasedStrategicalRoutePlannerFactory(
                            new LMRSFactory(new IDMPlusFactory(randomStream), new DefaultLMRSPerceptionFactory()));
            // route
            Route route = categorization.entails(Route.class) ? category.get(Route.class) : null;
            return new LaneBasedGTUCharacteristics(gtuCharacteristics, laneBasedStrategicalPlannerFactory, route, origin,
                    destination);
        }
    }

    /**
     * Standard generator for {@code LaneBasedGTUCharacteristics} with shortest route.
     * <p>
     * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
     * <p>
     * @version $Revision$, $LastChangedDate$, by $Author$, initial version 10 dec. 2017 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
     */
    private static class ShortestRouteGTUCharacteristicsGeneratorOD implements GTUCharacteristicsGeneratorOD
    {

        /** Shortest route cache. */
        private Map<GTUType, Map<Node, Map<Node, Route>>> shortestRouteCache = new HashMap<>();

        /** Constructor. */
        ShortestRouteGTUCharacteristicsGeneratorOD()
        {
            //
        }

        /** {@inheritDoc} */
        @Override
        public LaneBasedGTUCharacteristics draw(final Node origin, final Node destination, final Category category,
                final StreamInterface randomStream) throws GTUException
        {
            Categorization categorization = category.getCategorization();
            // GTU characteristics
            GTUType gtuType;
            if (categorization.entails(GTUType.class))
            {
                gtuType = category.get(GTUType.class);
            }
            else
            {
                gtuType = GTUType.CAR;
            }
            GTUCharacteristics gtuCharacteristics = Try.assign(() -> GTUType.defaultCharacteristics(gtuType),
                    "Exception while applying default GTU characteristics.");
            // strategical factory
            LaneBasedStrategicalPlannerFactory<?> laneBasedStrategicalPlannerFactory =
                    new LaneBasedStrategicalRoutePlannerFactory(
                            new LMRSFactory(new IDMPlusFactory(randomStream), new DefaultLMRSPerceptionFactory()));
            // route
            Route route;
            if (categorization.entails(Route.class))
            {
                route = category.get(Route.class);
            }
            else
            {
                // get shortest route
                route = getShortestRoute(origin, destination, gtuType);
            }
            return new LaneBasedGTUCharacteristics(gtuCharacteristics, laneBasedStrategicalPlannerFactory, route, origin,
                    destination);
        }

        /**
         * Returns the shortest route.
         * @param origin Node; origin
         * @param destination Node; destination
         * @param gtuType GTUType; GTU type
         * @return shortest route
         */
        protected Route getShortestRoute(final Node origin, final Node destination, final GTUType gtuType)
        {
            Map<Node, Map<Node, Route>> gtuTypeCache = this.shortestRouteCache.get(gtuType);
            if (gtuTypeCache == null)
            {
                gtuTypeCache = new HashMap<>();
                this.shortestRouteCache.put(gtuType, gtuTypeCache);
            }
            Map<Node, Route> originCache = gtuTypeCache.get(origin);
            if (originCache == null)
            {
                originCache = new HashMap<>();
                gtuTypeCache.put(origin, originCache);
            }
            Route route = originCache.get(destination);
            if (route == null)
            {
                route = Try.assign(() -> origin.getNetwork().getShortestRouteBetween(gtuType, origin, destination),
                        "Could not determine the shortest route from %s to %s.", origin, destination);
                originCache.put(destination, route);
            }
            return route;
        }
    }

    /**
     * Generator of LaneBasedGTUCharacteristics with templates for vehicle properties.
     * <p>
     * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
     * <p>
     * @version $Revision$, $LastChangedDate$, by $Author$, initial version 9 mrt. 2018 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
     */
    public static class ShortestRouteRandomGTUCharacteristicsGeneratorOD extends ShortestRouteGTUCharacteristicsGeneratorOD
    {

        /** Templates. */
        private final Map<GTUType, TemplateGTUType> templates = new HashMap<>();

        /**
         * Constructor.
         * @param templates Set&lt;TemplateGTUType&gt;; templates
         */
        public ShortestRouteRandomGTUCharacteristicsGeneratorOD(final Set<TemplateGTUType> templates)
        {
            for (TemplateGTUType template : templates)
            {
                this.templates.put(template.getGTUType(), template);
            }
        }

        /** {@inheritDoc} */
        @Override
        public LaneBasedGTUCharacteristics draw(final Node origin, final Node destination, final Category category,
                final StreamInterface randomStream) throws GTUException
        {
            Categorization categorization = category.getCategorization();
            // GTU characteristics
            GTUType gtuType;
            if (categorization.entails(GTUType.class))
            {
                gtuType = category.get(GTUType.class);
            }
            else
            {
                gtuType = GTUType.CAR;
            }
            GTUCharacteristics gtuCharacteristics =
                    Try.assign(() -> this.templates.get(gtuType).draw(), "Exception while drawing GTU characteristics.");
            // strategical factory
            LaneBasedStrategicalPlannerFactory<?> laneBasedStrategicalPlannerFactory =
                    new LaneBasedStrategicalRoutePlannerFactory(
                            new LMRSFactory(new IDMPlusFactory(randomStream), new DefaultLMRSPerceptionFactory()));
            // route
            Route route;
            if (categorization.entails(Route.class))
            {
                route = category.get(Route.class);
            }
            else
            {
                // get shortest route
                route = getShortestRoute(origin, destination, gtuType);
            }
            return new LaneBasedGTUCharacteristics(gtuCharacteristics, laneBasedStrategicalPlannerFactory, route, origin,
                    destination);
        }
    }

}
