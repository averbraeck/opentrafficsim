package org.opentrafficsim.road.gtu.generator.characteristics;

import java.util.function.Supplier;

import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djutils.exceptions.Throw;
import org.opentrafficsim.core.gtu.GtuTemplate;
import org.opentrafficsim.core.gtu.GtuType;
import org.opentrafficsim.core.network.route.Route;
import org.opentrafficsim.road.gtu.lane.VehicleModel;
import org.opentrafficsim.road.gtu.strategical.LaneBasedStrategicalPlannerFactory;

/**
 * Generate lane based GTUs using a template.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 */
public class LaneBasedGtuTemplate extends GtuTemplate implements LaneBasedGtuCharacteristicsGenerator
{
    /** */
    private static final long serialVersionUID = 20160101L;

    /** Factory for the strategical planner. */
    private final LaneBasedStrategicalPlannerFactory<?> strategicalPlannerFactory;

    /** Route supplier. */
    private final Supplier<Route> routeSupplier;

    /**
     * Constructor.
     * @param gtuType The GtuType to make it identifiable.
     * @param lengthSupplier Supplier&lt;Length&gt; Supplier for the length of the GTU type (parallel with driving direction).
     * @param widthSupplier Supplier for the width of the GTU type (perpendicular to driving direction).
     * @param maximumSpeedSupplier Supplier for the maximum speed of the GTU type (in the driving direction).
     * @param strategicalPlannerFactory Factory for the strategical planner (e.g., route determination)
     * @param routeSupplier route Supplier
     * @throws NullPointerException when one or more parameters are null
     */
    @SuppressWarnings("checkstyle:parameternumber")
    public LaneBasedGtuTemplate(final GtuType gtuType, final Supplier<Length> lengthSupplier,
            final Supplier<Length> widthSupplier, final Supplier<Speed> maximumSpeedSupplier,
            final LaneBasedStrategicalPlannerFactory<?> strategicalPlannerFactory, final Supplier<Route> routeSupplier)
            throws NullPointerException
    {
        super(gtuType, lengthSupplier, widthSupplier, maximumSpeedSupplier);
        Throw.whenNull(strategicalPlannerFactory, "strategicalPlannerFactory is null");
        Throw.whenNull(routeSupplier, "Route Supplier is null");
        this.strategicalPlannerFactory = strategicalPlannerFactory;
        this.routeSupplier = routeSupplier;
    }

    /**
     * Generate the properties of the next GTU.
     * @return the LaneBasedGtuCharacteristics with a drawn perception, strategical planner, and initial speed.
     */
    @Override
    public final LaneBasedGtuCharacteristics draw()
    {
        return new LaneBasedGtuCharacteristics(super.get(), this.strategicalPlannerFactory, this.routeSupplier.get(), null,
                null, VehicleModel.MINMAX);
    }

    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public String toString()
    {
        return String.format("LaneBasedGtuTemplate [%s, %s]", this.strategicalPlannerFactory, super.toString());
    }

}
