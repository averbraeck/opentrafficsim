package org.opentrafficsim.road.gtu.lane;

import org.djunits.unit.LengthUnit;
import org.djunits.unit.SpeedUnit;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.TemplateGTUType;
import org.opentrafficsim.core.units.distributions.ContinuousDistDoubleScalar;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;
import org.opentrafficsim.road.gtu.strategical.LaneBasedStrategicalPlanner;

/**
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Nov 29, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class LaneBasedTemplateGTUType extends TemplateGTUType
{
    private final Class<LaneBasedStrategicalPlanner> strategicalPlannerClass;

    /** the strategical planner (e.g., route determination) to use. */

    private final Class<LanePerception> perceptionClass;

    /** perceptionClass the lane-based perception model of the GTU. */

    /**
     * @param id The id of the GTUType to make it identifiable.
     * @param lengthDist the length of the GTU type (parallel with driving direction).
     * @param widthDist the width of the GTU type (perpendicular to driving direction).
     * @param maximumSpeedDist the maximum speed of the GTU type (in the driving direction).
     * @param simulator the simulator.
     * @param strategicalPlannerClass the strategical planner (e.g., route determination) to use
     * @param perceptionClass the lane-based perception model of the GTU
     * @throws GTUException when GTUType defined more than once
     */
    public LaneBasedTemplateGTUType(final String id,
        final ContinuousDistDoubleScalar.Rel<Length.Rel, LengthUnit> lengthDist,
        final ContinuousDistDoubleScalar.Rel<Length.Rel, LengthUnit> widthDist,
        final ContinuousDistDoubleScalar.Rel<Speed, SpeedUnit> maximumSpeedDist,
        final OTSDEVSSimulatorInterface simulator, Class<LaneBasedStrategicalPlanner> strategicalPlannerClass,
        Class<LanePerception> perceptionClass) throws GTUException
    {
        super(id, lengthDist, widthDist, maximumSpeedDist, simulator);
        this.strategicalPlannerClass = strategicalPlannerClass;
        this.perceptionClass = perceptionClass;
    }

    /**
     * @return a new LanePerception for a GTU
     * @throws InstantiationException in case instantiation fails
     * @throws IllegalAccessException in case constructor is not public
     */
    protected LanePerception instantiatePerception() throws InstantiationException, IllegalAccessException
    {
        return this.perceptionClass.newInstance();
    }

    /**
     * @return a new LaneBasedStrategicalPlanner for a GTU
     * @throws InstantiationException in case instantiation fails
     * @throws IllegalAccessException in case constructor is not public
     */
    protected LaneBasedStrategicalPlanner instantiateStrategicalPlanner() throws InstantiationException,
        IllegalAccessException
    {
        return this.strategicalPlannerClass.newInstance();
    }

}
