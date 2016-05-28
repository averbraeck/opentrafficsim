package org.opentrafficsim.road.gtu.lane;

import java.util.Set;

import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.core.Throw;
import org.opentrafficsim.core.distributions.Generator;
import org.opentrafficsim.core.distributions.ProbabilityException;
import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.gtu.TemplateGTUType;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.ParameterException;
import org.opentrafficsim.core.idgenerator.IdGenerator;
import org.opentrafficsim.core.network.OTSNetwork;
import org.opentrafficsim.road.gtu.lane.perception.LanePerceptionFull;
import org.opentrafficsim.road.gtu.strategical.LaneBasedStrategicalPlanner;
import org.opentrafficsim.road.network.lane.DirectedLanePosition;

/**
 * Generate lane based GTUs using a template.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
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

    /** Generator for lane perception. */
    private final Generator<LanePerceptionFull> perceptionGenerator;

    /** Generator for the strategical planner. */
    private final Generator<LaneBasedStrategicalPlanner> strategicalPlannerGenerator;

    /** Generator for the initial speed of the next GTU. */
    private Generator<Speed> initialSpeedGenerator;

    /** Initial longitudinal positions of all generated GTUs. */
    private Set<DirectedLanePosition> initialLongitudinalPositions;

    /**
     * @param gtuType The GTUType to make it identifiable.
     * @param idGenerator IdGenerator; the id generator used to generate names for GTUs constructed using this TemplateGTUType.
     *            Provide null to use the default id generator of AbstractGTU.
     * @param lengthGenerator Generator&lt;Length&gt; generator for the length of the GTU type (parallel with driving
     *            direction).
     * @param widthGenerator Generator&lt;Length&gt;; generator for the width of the GTU type (perpendicular to driving
     *            direction).
     * @param maximumSpeedGenerator Generator&lt;Speed&gt;; generator for the maximum speed of the GTU type (in the driving
     *            direction).
     * @param simulator the simulator.
     * @param strategicalPlannerGenerator Generator&lt;LaneBasedStrategicalPlanner&gt;; generator for the strategical planner
     *            (e.g., route determination)
     * @param perceptionGenerator Generator&lt;LanePerceptionFull&gt;; generator for the lane-based perception model of
     *            generated GTUs
     * @param initialLongitudinalPositions Set&lt;DirectedLanePosition&gt;; the initial lanes, directions and positions of
     *            generated GTUs
     * @param initialSpeedGenerator Generator&lt;Speed&gt;; the generator for the initial speed of generated GTUs
     * @param network OTSNetwork; the network that all generated GTUs are registered in
     * @throws NullPointerException when one or more parameters are null
     */
    @SuppressWarnings("checkstyle:parameternumber")
    public LaneBasedTemplateGTUType(final GTUType gtuType, final IdGenerator idGenerator,
            final Generator<Length> lengthGenerator, final Generator<Length> widthGenerator,
            final Generator<Speed> maximumSpeedGenerator, final OTSDEVSSimulatorInterface simulator,
            final Generator<LaneBasedStrategicalPlanner> strategicalPlannerGenerator,
            final Generator<LanePerceptionFull> perceptionGenerator,
            final Set<DirectedLanePosition> initialLongitudinalPositions, final Generator<Speed> initialSpeedGenerator,
            final OTSNetwork network) throws NullPointerException
    {
        super(gtuType, idGenerator, lengthGenerator, widthGenerator, maximumSpeedGenerator, simulator, network);
        Throw.whenNull(strategicalPlannerGenerator, "strategicalPlannerGenerator is null");
        Throw.whenNull(perceptionGenerator, "perceptionGenerator is null");
        Throw.whenNull(initialLongitudinalPositions, "initialLongitudinalPositions is null");
        Throw.whenNull(initialSpeedGenerator, "initialSpeedGenerator is null");
        this.strategicalPlannerGenerator = strategicalPlannerGenerator;
        this.perceptionGenerator = perceptionGenerator;
        this.initialLongitudinalPositions = initialLongitudinalPositions;
        this.initialSpeedGenerator = initialSpeedGenerator;
    }

    /**
     * Generate the properties of the next GTU.
     * @return the LaneBasedGTUCharacteristics with a drawn perception, strategical planner, and initial speed.
     * @throws ProbabilityException when a generator is improperly configured
     * @throws ParameterException in case of a parameter problem.
     */
    public final LaneBasedGTUCharacteristics draw() throws ProbabilityException, ParameterException
    {
        return new LaneBasedGTUCharacteristics(super.draw(), this.perceptionGenerator.draw(),
                this.strategicalPlannerGenerator.draw(), this.initialSpeedGenerator.draw(), this.initialLongitudinalPositions);
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public String toString()
    {
        return String.format("LaneBasedGTUTemplate [%s, %s, %s, %s, %s]", this.initialLongitudinalPositions,
                this.perceptionGenerator, this.strategicalPlannerGenerator, this.initialSpeedGenerator, super.toString());
    }

}
