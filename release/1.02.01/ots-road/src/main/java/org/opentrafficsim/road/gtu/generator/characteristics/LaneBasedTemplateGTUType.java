package org.opentrafficsim.road.gtu.generator.characteristics;

import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djutils.exceptions.Throw;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.core.distributions.Generator;
import org.opentrafficsim.core.distributions.ProbabilityException;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.gtu.TemplateGTUType;
import org.opentrafficsim.core.network.route.Route;
import org.opentrafficsim.road.gtu.lane.VehicleModel;
import org.opentrafficsim.road.gtu.strategical.LaneBasedStrategicalPlannerFactory;

/**
 * Generate lane based GTUs using a template.
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Nov 29, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class LaneBasedTemplateGTUType extends TemplateGTUType implements LaneBasedGTUCharacteristicsGenerator
{
    /** */
    private static final long serialVersionUID = 20160101L;

    /** Factory for the strategical planner. */
    private final LaneBasedStrategicalPlannerFactory<?> strategicalPlannerFactory;

    /** Route Generator. */
    private final Generator<Route> routeGenerator;

    /** Generator for the initial speed of the next GTU. */
    private Generator<Speed> initialSpeedGenerator;

    /**
     * @param gtuType GTUType; The GTUType to make it identifiable.
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
    public LaneBasedTemplateGTUType(final GTUType gtuType, final Generator<Length> lengthGenerator,
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
     * @return the LaneBasedGTUCharacteristics with a drawn perception, strategical planner, and initial speed.
     * @throws ProbabilityException when a generator is improperly configured
     * @throws ParameterException in case of a parameter problem.
     */
    @Override
    public final LaneBasedGTUCharacteristics draw() throws ProbabilityException, ParameterException
    {
        return new LaneBasedGTUCharacteristics(super.draw(), this.strategicalPlannerFactory, this.routeGenerator.draw(), null,
                null, VehicleModel.MINMAX);
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public String toString()
    {
        return String.format("LaneBasedGTUTemplate [%s, %s, %s]", this.strategicalPlannerFactory, this.initialSpeedGenerator,
                super.toString());
    }

}
