package org.opentrafficsim.road.gtu.generator.od;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.djutils.exceptions.Throw;
import org.djutils.exceptions.Try;
import org.opentrafficsim.core.definitions.DefaultsNl;
import org.opentrafficsim.core.distributions.Generator;
import org.opentrafficsim.core.gtu.GtuCharacteristics;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.core.gtu.GtuType;
import org.opentrafficsim.core.gtu.TemplateGtuType;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.core.network.route.Route;
import org.opentrafficsim.road.gtu.generator.characteristics.LaneBasedGtuCharacteristics;
import org.opentrafficsim.road.gtu.lane.VehicleModel;
import org.opentrafficsim.road.gtu.lane.VehicleModelFactory;
import org.opentrafficsim.road.gtu.strategical.LaneBasedStrategicalPlannerFactory;
import org.opentrafficsim.road.gtu.strategical.od.Categorization;
import org.opentrafficsim.road.gtu.strategical.od.Category;
import org.opentrafficsim.road.gtu.strategical.route.RouteGeneratorOd;

import nl.tudelft.simulation.jstats.streams.StreamInterface;

/**
 * Default generator for {@code LaneBasedGtuCharacteristics}.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public final class DefaultGtuCharacteristicsGeneratorOd implements GtuCharacteristicsGeneratorOd
{
    /** GTU type generator. */
    private Generator<GtuType> gtuTypeGenerator = null;

    /** Templates. */
    private final Map<GtuType, TemplateGtuType> templates = new LinkedHashMap<>();

    /** Route generator. */
    private final RouteGeneratorOd routeGenerator;

    /** Supplies a strategical factory. */
    private final StrategicalPlannerFactorySupplierOd factorySupplier;

    /** Vehicle factory. */
    private VehicleModelFactory vehicleModelFactory = VehicleModelFactory.MINMAX;

    /**
     * Constructor using null-routes, default GTU characteristics and LMRS.
     * @param truck GtuType; truck GTU type.
     */
    public DefaultGtuCharacteristicsGeneratorOd(final GtuType truck)
    {
        this(null, RouteGeneratorOd.NULL, new LinkedHashSet<>(), StrategicalPlannerFactorySupplierOd.lmrs(truck));
    }

    /**
     * Constructor using route generator, default GTU characteristics and LMRS.
     * @param routeGenerator RouteGeneratorOD; route generator
     * @param truck GtuType; truck GTU type.
     */
    public DefaultGtuCharacteristicsGeneratorOd(final RouteGeneratorOd routeGenerator, final GtuType truck)
    {
        this(null, routeGenerator, new LinkedHashSet<>(), StrategicalPlannerFactorySupplierOd.lmrs(truck));
    }

    /**
     * Constructor using route supplier, provided GTU templates and LMRS.
     * @param routeSupplier RouteGeneratorOD; route supplier
     * @param templates Set&lt;TemplateGTUType&gt;; templates
     * @param truck GtuType; truck GTU type.
     */
    public DefaultGtuCharacteristicsGeneratorOd(final RouteGeneratorOd routeSupplier, final Set<TemplateGtuType> templates,
            final GtuType truck)
    {
        this(null, routeSupplier, templates, StrategicalPlannerFactorySupplierOd.lmrs(truck));
    }

    /**
     * Constructor using route supplier, default GTU characteristics and provided strategical planner factory supplier.
     * @param routeGenerator RouteGeneratorOD; route generator
     * @param factorySupplier StrategicalPlannerFactorySupplierOD; strategical factory supplier
     */
    public DefaultGtuCharacteristicsGeneratorOd(final RouteGeneratorOd routeGenerator,
            final StrategicalPlannerFactorySupplierOd factorySupplier)
    {
        this(null, routeGenerator, new LinkedHashSet<>(), factorySupplier);
    }

    /**
     * Constructor using route supplier, provided GTU templates and provided strategical planner factory supplier.
     * @param gtuTypeGenerator Generator&lt;GtuType&gt;; GTU type generator
     * @param routeGenerator RouteGeneratorOD; route generator
     * @param templates Set&lt;TemplateGTUType&gt;; templates
     * @param factorySupplier StrategicalPlannerFactorySupplierOD; strategical factory supplier
     */
    public DefaultGtuCharacteristicsGeneratorOd(final Generator<GtuType> gtuTypeGenerator,
            final RouteGeneratorOd routeGenerator, final Set<TemplateGtuType> templates,
            final StrategicalPlannerFactorySupplierOd factorySupplier)
    {
        Throw.whenNull(factorySupplier, "Strategical factory supplier may not be null.");
        this.gtuTypeGenerator = gtuTypeGenerator;
        if (routeGenerator == null)
        {
            this.routeGenerator = RouteGeneratorOd.NULL;
        }
        else
        {
            this.routeGenerator = routeGenerator;
        }
        if (templates != null)
        {
            for (TemplateGtuType template : templates)
            {
                this.templates.put(template.getGtuType(), template);
            }
        }
        this.factorySupplier = factorySupplier;
    }

    /**
     * Constructor using null-routes, provided GTU templates and LMRS.
     * @param templates Set&lt;TemplateGTUType&gt;; templates
     * @param truck GtuType; truck GTU type.
     */
    public DefaultGtuCharacteristicsGeneratorOd(final Set<TemplateGtuType> templates, final GtuType truck)
    {
        this(null, RouteGeneratorOd.NULL, templates, StrategicalPlannerFactorySupplierOd.lmrs(truck));
    }

    /**
     * Constructor using null-routes, provided GTU templates and provided strategical planner factory supplier.
     * @param templates Set&lt;TemplateGTUType&gt;; templates
     * @param factorySupplier StrategicalPlannerFactorySupplierOD; strategical factory supplier
     */
    public DefaultGtuCharacteristicsGeneratorOd(final Set<TemplateGtuType> templates,
            final StrategicalPlannerFactorySupplierOd factorySupplier)
    {
        this(null, RouteGeneratorOd.NULL, templates, factorySupplier);
    }

    /**
     * Constructor using using null-routes, default GTU characteristics and provided GTU templates and provided strategical
     * planner factory supplier.
     * @param factorySupplier StrategicalPlannerFactorySupplierOD; strategical factory supplier
     */
    public DefaultGtuCharacteristicsGeneratorOd(final StrategicalPlannerFactorySupplierOd factorySupplier)
    {
        this(null, RouteGeneratorOd.NULL, new LinkedHashSet<>(), factorySupplier);
    }

    // TODO: remove above constructors and use factory always

    /**
     * Constructor using route supplier, provided GTU templates and provided strategical planner factory supplier.
     * @param gtuTypeGenerator Generator&lt;GtuType&gt;; GTU type generator
     * @param routeSupplier RouteGeneratorOD; route supplier
     * @param templates Set&lt;TemplateGTUType&gt;; templates
     * @param factorySupplier StrategicalPlannerFactorySupplierOD; strategical factory supplier
     * @param vehicleModelFactory VehicleModelFactory; vehicle model factory
     */
    private DefaultGtuCharacteristicsGeneratorOd(final Generator<GtuType> gtuTypeGenerator,
            final RouteGeneratorOd routeSupplier, final Set<TemplateGtuType> templates,
            final StrategicalPlannerFactorySupplierOd factorySupplier, final VehicleModelFactory vehicleModelFactory)
    {
        Throw.whenNull(factorySupplier, "Strategical factory supplier may not be null.");
        this.gtuTypeGenerator = gtuTypeGenerator;
        if (routeSupplier == null)
        {
            this.routeGenerator = RouteGeneratorOd.NULL;
        }
        else
        {
            this.routeGenerator = routeSupplier;
        }
        if (templates != null)
        {
            for (TemplateGtuType template : templates)
            {
                this.templates.put(template.getGtuType(), template);
            }
        }
        this.factorySupplier = factorySupplier;
        if (vehicleModelFactory == null)
        {
            this.vehicleModelFactory = VehicleModelFactory.MINMAX;
        }
        else
        {
            this.vehicleModelFactory = vehicleModelFactory;
        }
    }

    /** {@inheritDoc} */
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
            gtuType = Try.assign(() -> this.gtuTypeGenerator.draw(), GtuException.class, "Parameter while drawing GTU type.");
        }
        else
        {
            gtuType = DefaultsNl.CAR;
        }
        GtuCharacteristics gtuCharacteristics;
        if (this.templates.containsKey(gtuType))
        {
            gtuCharacteristics =
                    Try.assign(() -> this.templates.get(gtuType).draw(), "Exception while drawing GTU characteristics.");
        }
        else
        {
            gtuCharacteristics = Try.assign(() -> GtuType.defaultCharacteristics(gtuType, origin.getNetwork(), randomStream),
                    "Exception while applying default GTU characteristics.");
        }
        // strategical factory
        LaneBasedStrategicalPlannerFactory<?> laneBasedStrategicalPlannerFactory =
                this.factorySupplier.getFactory(origin, destination, category, randomStream);
        // route
        Route route;
        if (categorization.entails(Route.class))
        {
            route = category.get(Route.class);
        }
        else
        {
            // get route from supplier
            // XXX typically gets the route from RouteGeneratorOD.getRoute(...)
            route = this.routeGenerator.getRoute(origin, destination, gtuType);
        }
        // vehicle model
        VehicleModel vehicleModel = this.vehicleModelFactory.create(gtuType);

        return new LaneBasedGtuCharacteristics(gtuCharacteristics, laneBasedStrategicalPlannerFactory, route, origin,
                destination, vehicleModel);
    }

    /**
     * Factory for {@code DefaultGtuCharacteristicsGeneratorOD}.
     * <p>
     * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * <p>
     * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
     * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
     * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
     */
    @SuppressWarnings("hiddenfield")
    public static class Factory
    {
        /** GTU type. */
        private Generator<GtuType> gtuTypeGenerator = null;

        /** Templates. */
        private Set<TemplateGtuType> templates = new LinkedHashSet<>();

        /** Route supplier. */
        private RouteGeneratorOd routeGenerator = RouteGeneratorOd.NULL;

        /** Supplies a strategical factory. */
        private StrategicalPlannerFactorySupplierOd factorySupplier = StrategicalPlannerFactorySupplierOd.lmrs(null);

        /** Vehicle factory. */
        private VehicleModelFactory vehicleModelFactory = VehicleModelFactory.MINMAX;

        /**
         * @param gtuTypeGenerator Generator&lt;GtuType&gt;; set gtuTypeGenerator.
         * @return Factory; this factory for method chaining
         */
        public Factory setGtuTypeGenerator(final Generator<GtuType> gtuTypeGenerator)
        {
            this.gtuTypeGenerator = gtuTypeGenerator;
            return this;
        }

        /**
         * @param templates Set&lt;TemplateGTUType&gt;; set templates.
         * @return Factory; this factory for method chaining
         */
        public Factory setTemplates(final Set<TemplateGtuType> templates)
        {
            this.templates = templates;
            return this;
        }

        /**
         * @param routeSupplier RouteGeneratorOD; set routeSupplier.
         * @return Factory; this factory for method chaining
         */
        public Factory setRouteSupplier(final RouteGeneratorOd routeSupplier)
        {
            this.routeGenerator = routeSupplier;
            return this;
        }

        /**
         * @param factorySupplier StrategicalPlannerFactorySupplierOD; set factorySupplier.
         * @return Factory; this factory for method chaining
         */
        public Factory setFactorySupplier(final StrategicalPlannerFactorySupplierOd factorySupplier)
        {
            this.factorySupplier = factorySupplier;
            return this;
        }

        /**
         * @param vehicleModelFactory VehicleModelFactory; set vehicleModelFactory.
         * @return Factory; this factory for method chaining
         */
        public Factory setVehicleModelGenerator(final VehicleModelFactory vehicleModelFactory)
        {
            this.vehicleModelFactory = vehicleModelFactory;
            return this;
        }

        /**
         * Creates the default GTU characteristics generator based on OD information.
         * @return default GTU characteristics generator based on OD information
         */
        @SuppressWarnings("synthetic-access")
        public DefaultGtuCharacteristicsGeneratorOd create()
        {
            return new DefaultGtuCharacteristicsGeneratorOd(this.gtuTypeGenerator, this.routeGenerator, this.templates,
                    this.factorySupplier, this.vehicleModelFactory);
        }
    }

}
