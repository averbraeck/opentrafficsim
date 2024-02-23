package org.opentrafficsim.road.gtu.generator.characteristics;

import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djutils.exceptions.Throw;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.core.distributions.Generator;
import org.opentrafficsim.core.distributions.ProbabilityException;
import org.opentrafficsim.core.gtu.GtuType;
import org.opentrafficsim.core.gtu.GtuTemplate;
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
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 */
public class LaneBasedGtuTemplate extends GtuTemplate implements LaneBasedGtuCharacteristicsGenerator
{
    /** */
    private static final long serialVersionUID = 20160101L;

    /** Factory for the strategical planner. */
    private final LaneBasedStrategicalPlannerFactory<?> strategicalPlannerFactory;

    /** Route Generator. */
    private final Generator<Route> routeGenerator;

    /**
     * @param gtuType GtuType; The GtuType to make it identifiable.
     * @param lengthGenerator Generator&lt;Length&gt;; Generator&lt;Length&gt; generator for the length of the GTU type
     *            (parallel with driving direction).
     * @param widthGenerator Generator&lt;Length&gt;; generator for the width of the GTU type (perpendicular to driving
     *            direction).
     * @param maximumSpeedGenerator Generator&lt;Speed&gt;; generator for the maximum speed of the GTU type (in the driving
     *            direction).
     * @param strategicalPlannerFactory LaneBasedStrategicalPlannerFactory&lt;?&gt;; Factory for the strategical planner (e.g.,
     *            route determination)
     * @param routeGenerator Generator&lt;Route&gt;; route generator
     * @throws NullPointerException when one or more parameters are null
     */
    @SuppressWarnings("checkstyle:parameternumber")
    public LaneBasedGtuTemplate(final GtuType gtuType, final Generator<Length> lengthGenerator,
            final Generator<Length> widthGenerator, final Generator<Speed> maximumSpeedGenerator,
            final LaneBasedStrategicalPlannerFactory<?> strategicalPlannerFactory, final Generator<Route> routeGenerator)
            throws NullPointerException
    {
        super(gtuType, lengthGenerator, widthGenerator, maximumSpeedGenerator);
        Throw.whenNull(strategicalPlannerFactory, "strategicalPlannerFactory is null");
        Throw.whenNull(routeGenerator, "Route generator is null");
        this.strategicalPlannerFactory = strategicalPlannerFactory;
        this.routeGenerator = routeGenerator;
    }

    /**
     * Generate the properties of the next GTU.
     * @return the LaneBasedGtuCharacteristics with a drawn perception, strategical planner, and initial speed.
     * @throws ProbabilityException when a generator is improperly configured
     * @throws ParameterException in case of a parameter problem.
     */
    @Override
    public final LaneBasedGtuCharacteristics draw() throws ProbabilityException, ParameterException
    {
        return new LaneBasedGtuCharacteristics(super.draw(), this.strategicalPlannerFactory, this.routeGenerator.draw(), null,
                null, VehicleModel.MINMAX);
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public String toString()
    {
        return String.format("LaneBasedGtuTemplate [%s, %s]", this.strategicalPlannerFactory,
                super.toString());
    }

}
