package org.opentrafficsim.road.gtu.generator.od;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.djutils.exceptions.Throw;
import org.djutils.exceptions.Try;
import org.opentrafficsim.core.gtu.GTUCharacteristics;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.gtu.TemplateGTUType;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.core.network.route.Route;
import org.opentrafficsim.road.gtu.generator.characteristics.LaneBasedGTUCharacteristics;
import org.opentrafficsim.road.gtu.lane.VehicleModel;
import org.opentrafficsim.road.gtu.strategical.LaneBasedStrategicalPlannerFactory;
import org.opentrafficsim.road.gtu.strategical.od.Categorization;
import org.opentrafficsim.road.gtu.strategical.od.Category;
import org.opentrafficsim.road.gtu.strategical.route.RouteSupplier;

import nl.tudelft.simulation.jstats.streams.StreamInterface;

/**
 * Default generator for {@code LaneBasedGTUCharacteristics}.
 * <p>
 * Copyright (c) 2013-2018 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 10 dec. 2017 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public final class DefaultGTUCharacteristicsGeneratorOD implements GTUCharacteristicsGeneratorOD
{
    /** Templates. */
    private final Map<GTUType, TemplateGTUType> templates = new HashMap<>();

    /** Route supplier. */
    private final RouteSupplier routeSupplier;

    /** Supplies a strategical factory. */
    private final StrategicalPlannerFactorySupplierOD factorySupplier;

    /**
     * Constructor using null-routes, default GTU characteristics and LMRS.
     */
    public DefaultGTUCharacteristicsGeneratorOD()
    {
        this(RouteSupplier.NULL, new HashSet<>(), StrategicalPlannerFactorySupplierOD.LMRS);
    }

    /**
     * Constructor using route supplier, default GTU characteristics and LMRS.
     * @param routeSupplier RouteSupplier; route supplier
     */
    public DefaultGTUCharacteristicsGeneratorOD(final RouteSupplier routeSupplier)
    {
        this(routeSupplier, new HashSet<>(), StrategicalPlannerFactorySupplierOD.LMRS);
    }

    /**
     * Constructor using route supplier, provided GTU templates and LMRS.
     * @param routeSupplier RouteSupplier; route supplier
     * @param templates Set&lt;TemplateGTUType&gt;; templates
     */
    public DefaultGTUCharacteristicsGeneratorOD(final RouteSupplier routeSupplier, final Set<TemplateGTUType> templates)
    {
        this(routeSupplier, templates, StrategicalPlannerFactorySupplierOD.LMRS);
    }

    /**
     * Constructor using route supplier, default GTU characteristics and provided strategical planner factory supplier.
     * @param routeSupplier RouteSupplier; route supplier
     * @param factorySupplier StrategicalPlannerFactorySupplierOD; strategical factory supplier
     */
    public DefaultGTUCharacteristicsGeneratorOD(final RouteSupplier routeSupplier,
            final StrategicalPlannerFactorySupplierOD factorySupplier)
    {
        this(routeSupplier, new HashSet<>(), factorySupplier);
    }

    /**
     * Constructor using route supplier, provided GTU templates and provided strategical planner factory supplier.
     * @param routeSupplier RouteSupplier; route supplier
     * @param templates Set&lt;TemplateGTUType&gt;; templates
     * @param factorySupplier StrategicalPlannerFactorySupplierOD; strategical factory supplier
     */
    public DefaultGTUCharacteristicsGeneratorOD(final RouteSupplier routeSupplier, final Set<TemplateGTUType> templates,
            final StrategicalPlannerFactorySupplierOD factorySupplier)
    {
        Throw.whenNull(factorySupplier, "Strategical factory supplier may not be null.");
        if (routeSupplier == null)
        {
            this.routeSupplier = RouteSupplier.NULL;
        }
        else
        {
            this.routeSupplier = routeSupplier;
        }
        if (templates != null)
        {
            for (TemplateGTUType template : templates)
            {
                this.templates.put(template.getGTUType(), template);
            }
        }
        this.factorySupplier = factorySupplier;
    }

    /**
     * Constructor using null-routes, provided GTU templates and LMRS.
     * @param templates Set&lt;TemplateGTUType&gt;; templates
     */
    public DefaultGTUCharacteristicsGeneratorOD(final Set<TemplateGTUType> templates)
    {
        this(RouteSupplier.NULL, templates, StrategicalPlannerFactorySupplierOD.LMRS);
    }

    /**
     * Constructor using null-routes, provided GTU templates and provided strategical planner factory supplier.
     * @param templates Set&lt;TemplateGTUType&gt;; templates
     * @param factorySupplier StrategicalPlannerFactorySupplierOD; strategical factory supplier
     */
    public DefaultGTUCharacteristicsGeneratorOD(final Set<TemplateGTUType> templates,
            final StrategicalPlannerFactorySupplierOD factorySupplier)
    {
        this(RouteSupplier.NULL, templates, factorySupplier);
    }

    /**
     * Constructor using using null-routes, default GTU characteristics and provided GTU templates and provided strategical
     * planner factory supplier.
     * @param factorySupplier StrategicalPlannerFactorySupplierOD; strategical factory supplier
     */
    public DefaultGTUCharacteristicsGeneratorOD(final StrategicalPlannerFactorySupplierOD factorySupplier)
    {
        this(RouteSupplier.NULL, new HashSet<>(), factorySupplier);
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
        GTUCharacteristics gtuCharacteristics;
        if (this.templates.containsKey(gtuType))
        {
            gtuCharacteristics =
                    Try.assign(() -> this.templates.get(gtuType).draw(), "Exception while drawing GTU characteristics.");
        }
        else
        {
            gtuCharacteristics = Try.assign(() -> GTUType.defaultCharacteristics(gtuType, randomStream),
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
            route = this.routeSupplier.getRoute(origin, destination, gtuType);
        }
        return new LaneBasedGTUCharacteristics(gtuCharacteristics, laneBasedStrategicalPlannerFactory, route, origin,
                destination, VehicleModel.MINMAX);
    }
}
