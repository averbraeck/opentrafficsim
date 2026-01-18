package org.opentrafficsim.road.gtu.generator.characteristics;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import org.djutils.exceptions.Throw;
import org.djutils.exceptions.Try;
import org.opentrafficsim.core.definitions.Defaults;
import org.opentrafficsim.core.definitions.DefaultsNl;
import org.opentrafficsim.core.gtu.GtuCharacteristics;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.core.gtu.GtuTemplate;
import org.opentrafficsim.core.gtu.GtuType;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.core.network.route.Route;
import org.opentrafficsim.road.gtu.lane.VehicleModel;
import org.opentrafficsim.road.gtu.lane.VehicleModelFactory;
import org.opentrafficsim.road.gtu.lane.tactical.lmrs.LmrsFactory;
import org.opentrafficsim.road.gtu.strategical.LaneBasedStrategicalPlannerFactory;
import org.opentrafficsim.road.gtu.strategical.LaneBasedStrategicalRoutePlannerFactory;
import org.opentrafficsim.road.od.Categorization;
import org.opentrafficsim.road.od.Category;

import nl.tudelft.simulation.jstats.streams.StreamInterface;

/**
 * Default generator for {@code LaneBasedGtuCharacteristics} in a context with OD information.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public final class DefaultLaneBasedGtuCharacteristicsGeneratorOd implements LaneBasedGtuCharacteristicsGeneratorOd
{
    /** GTU type generator. */
    private Supplier<GtuType> gtuTypeGenerator = null;

    /** Templates. */
    private final Map<GtuType, GtuTemplate> templates = new LinkedHashMap<>();

    /** Supplies a strategical factory. */
    private final LaneBasedStrategicalPlannerFactory<?> factory;

    /** Vehicle factory. */
    private VehicleModelFactory vehicleModelFactory = VehicleModelFactory.MINMAX;

    /** Template function. */
    private BiFunction<GtuType, StreamInterface, Optional<GtuTemplate>> templateFunction;

    /**
     * Constructor using route supplier, provided GTU templates and provided strategical planner factory supplier.
     * @param gtuTypeGenerator GTU type supplier
     * @param templates templates
     * @param factory strategical factory supplier
     * @param vehicleModelFactory vehicle model factory
     * @param templateFunction template function
     */
    private DefaultLaneBasedGtuCharacteristicsGeneratorOd(final Supplier<GtuType> gtuTypeGenerator,
            final Set<GtuTemplate> templates, final LaneBasedStrategicalPlannerFactory<?> factory,
            final VehicleModelFactory vehicleModelFactory,
            final BiFunction<GtuType, StreamInterface, Optional<GtuTemplate>> templateFunction)
    {
        Throw.whenNull(factory, "Strategical planner factory may not be null.");
        this.gtuTypeGenerator = gtuTypeGenerator;
        if (templates != null)
        {
            for (GtuTemplate template : templates)
            {
                this.templates.put(template.getGtuType(), template);
            }
        }
        this.factory = factory;
        if (vehicleModelFactory == null)
        {
            this.vehicleModelFactory = VehicleModelFactory.MINMAX;
        }
        else
        {
            this.vehicleModelFactory = vehicleModelFactory;
        }
        this.templateFunction = templateFunction;
    }

    @Override
    public LaneBasedGtuCharacteristics draw(final Node origin, final Node destination, final Category category,
            final StreamInterface randomStream) throws GtuException
    {
        Categorization categorization = category.getCategorization();
        // GTU characteristics
        GtuType gtuType;
        if (categorization.entails(GtuType.class))
        {
            gtuType = category.get(GtuType.class);
        }
        else if (this.gtuTypeGenerator != null)
        {
            gtuType = Try.assign(() -> this.gtuTypeGenerator.get(), GtuException.class, "Parameter while drawing GTU type.");
        }
        else
        {
            gtuType = DefaultsNl.CAR;
        }
        GtuCharacteristics gtuCharacteristics;
        if (this.templates.containsKey(gtuType))
        {
            gtuCharacteristics =
                    Try.assign(() -> this.templates.get(gtuType).get(), "Exception while drawing GTU characteristics.");
        }
        else
        {
            gtuCharacteristics = this.templateFunction.apply(gtuType, randomStream).get().get();
        }
        Route route = categorization.entails(Route.class) ? category.get(Route.class) : null;
        VehicleModel vehicleModel = this.vehicleModelFactory.create(gtuType);

        return new LaneBasedGtuCharacteristics(gtuCharacteristics, this.factory, route, origin, destination, vehicleModel);
    }

    /**
     * Returns a strategical model factory for a standard LMRS model, to be used in {@code Factory}.
     * @param stream random number stream.
     * @return factory for a standard LMRS model.
     */
    public static LaneBasedStrategicalRoutePlannerFactory defaultLmrs(final StreamInterface stream)
    {
        return new LaneBasedStrategicalRoutePlannerFactory(new LmrsFactory.Factory().withDefaultIncentives().build(stream));
    }

    /**
     * Factory for {@code DefaultGtuCharacteristicsGeneratorOD}.
     * <p>
     * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
     * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
     * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
     */
    @SuppressWarnings("hiddenfield")
    public static class Factory
    {
        /** GTU type. */
        private Supplier<GtuType> gtuTypeGenerator = null;

        /** Templates. */
        private Set<GtuTemplate> templates = new LinkedHashSet<>();

        /** Supplies a strategical factory. */
        private final LaneBasedStrategicalPlannerFactory<?> factory;

        /** Vehicle factory. */
        private VehicleModelFactory vehicleModelFactory = VehicleModelFactory.MINMAX;

        /** Default templates. */
        private BiFunction<GtuType, StreamInterface, Optional<GtuTemplate>> templateFunction = Defaults.NL;

        /**
         * Constructor.
         * @param factory set factorySupplier.
         */
        public Factory(final LaneBasedStrategicalPlannerFactory<?> factory)
        {
            this.factory = factory;
        }

        /**
         * Set GTU type generator.
         * @param gtuTypeGenerator set gtuTypeGenerator.
         * @return this factory for method chaining
         */
        public Factory setGtuTypeGenerator(final Supplier<GtuType> gtuTypeGenerator)
        {
            this.gtuTypeGenerator = gtuTypeGenerator;
            return this;
        }

        /**
         * Set templates.
         * @param templates set templates.
         * @return this factory for method chaining
         */
        public Factory setTemplates(final Set<GtuTemplate> templates)
        {
            this.templates = templates;
            return this;
        }

        /**
         * Set vehicle model generator.
         * @param vehicleModelFactory set vehicleModelFactory.
         * @return this factory for method chaining
         */
        public Factory setVehicleModelGenerator(final VehicleModelFactory vehicleModelFactory)
        {
            this.vehicleModelFactory = vehicleModelFactory;
            return this;
        }

        /**
         * Set GTU template function.
         * @param templateFunction template function
         * @return this factory for method chaining
         */
        public Factory setGtuTemplateFunction(
                final BiFunction<GtuType, StreamInterface, Optional<GtuTemplate>> templateFunction)
        {
            this.templateFunction = templateFunction;
            return this;
        }

        /**
         * Creates the default GTU characteristics generator based on OD information.
         * @return default GTU characteristics generator based on OD information
         */
        @SuppressWarnings("synthetic-access")
        public DefaultLaneBasedGtuCharacteristicsGeneratorOd create()
        {
            return new DefaultLaneBasedGtuCharacteristicsGeneratorOd(this.gtuTypeGenerator, this.templates, this.factory,
                    this.vehicleModelFactory, this.templateFunction);
        }
    }

}
