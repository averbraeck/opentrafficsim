package org.opentrafficsim.road.gtu.generator;

import java.io.Serializable;

import org.djunits.unit.DurationUnit;
import org.djunits.unit.LengthUnit;
import org.djunits.unit.SpeedUnit;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.core.distributions.Generator;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.gtu.GtuType;
import org.opentrafficsim.core.network.route.Route;
import org.opentrafficsim.core.units.distributions.ContinuousDistDoubleScalar;
import org.opentrafficsim.road.gtu.strategical.LaneBasedStrategicalPlanner;
import org.opentrafficsim.road.gtu.strategical.LaneBasedStrategicalPlannerFactory;
import org.opentrafficsim.road.network.OTSRoadNetwork;
import org.opentrafficsim.road.network.lane.Lane;

import nl.tudelft.simulation.dsol.SimRuntimeException;

/**
 * Generate GTUs.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 */
public class GtuGeneratorIndividualOld extends AbstractGtuGeneratorOld implements Serializable
{
    /** */
    private static final long serialVersionUID = 20160000L;

    /** Simulator to schedule next arrival events. */
    private final OTSSimulatorInterface simulator;

    /** Distribution of the length of the GTU. */
    private final ContinuousDistDoubleScalar.Rel<Length, LengthUnit> lengthDist;

    /** Distribution of the width of the GTU. */
    private final ContinuousDistDoubleScalar.Rel<Length, LengthUnit> widthDist;

    /** Distribution of the maximum speed of the GTU. */
    private final ContinuousDistDoubleScalar.Rel<Speed, SpeedUnit> maximumSpeedDist;

    /**
     * @param name String; the name of the generator
     * @param gtuType GtuType; the type of GTU to generate
     * @param gtuClass Class&lt;?&gt;; the gtu class to instantiate
     * @param initialSpeedDist ContinuousDistDoubleScalar.Rel&lt;Speed,SpeedUnit&gt;; distribution of the initial speed of the
     *            GTU
     * @param interarrivelTimeDist ContinuousDistDoubleScalar.Rel&lt;Duration,DurationUnit&gt;; distribution of the interarrival
     *            time
     * @param maxGTUs long; maximum number of GTUs to generate
     * @param startTime Time; start time of generation (delayed start)
     * @param endTime Time; end time of generation
     * @param simulator OTSSimulatorInterface; simulator to schedule next arrival events
     * @param lengthDist ContinuousDistDoubleScalar.Rel&lt;Length,LengthUnit&gt;; distribution of the length of the GTU
     * @param widthDist ContinuousDistDoubleScalar.Rel&lt;Length,LengthUnit&gt;; distribution of the width of the GTU
     * @param maximumSpeedDist ContinuousDistDoubleScalar.Rel&lt;Speed,SpeedUnit&gt;; distribution of the maximum speed of the
     *            GTU
     * @param lane Lane; Lane on which newly GTUs are placed
     * @param position Length; position on the lane, relative to the design line of the link
     * @param strategicalPlannerFactory LaneBasedStrategicalPlannerFactory&lt;? extends LaneBasedStrategicalPlanner&gt;; the
     *            lane-based strategical planner factory to use
     * @param routeGenerator Generator&lt;Route&gt;; route generator
     * @param network OTSRoadNetwork; the network to register the GTU into
     * @throws SimRuntimeException when simulation scheduling fails
     */
    @SuppressWarnings("checkstyle:parameternumber")
    public GtuGeneratorIndividualOld(final String name, final OTSSimulatorInterface simulator, final GtuType gtuType,
            final Class<?> gtuClass, final ContinuousDistDoubleScalar.Rel<Speed, SpeedUnit> initialSpeedDist,
            final ContinuousDistDoubleScalar.Rel<Duration, DurationUnit> interarrivelTimeDist,
            final ContinuousDistDoubleScalar.Rel<Length, LengthUnit> lengthDist,
            final ContinuousDistDoubleScalar.Rel<Length, LengthUnit> widthDist,
            final ContinuousDistDoubleScalar.Rel<Speed, SpeedUnit> maximumSpeedDist, final long maxGTUs, final Time startTime,
            final Time endTime, final Lane lane, final Length position,
            final LaneBasedStrategicalPlannerFactory<? extends LaneBasedStrategicalPlanner> strategicalPlannerFactory,
            final Generator<Route> routeGenerator, final OTSRoadNetwork network) throws SimRuntimeException
    {
        super(name, simulator, gtuType, gtuClass, initialSpeedDist, interarrivelTimeDist, maxGTUs, startTime, endTime, lane,
                position, strategicalPlannerFactory, routeGenerator, network);
        this.simulator = simulator;
        this.lengthDist = lengthDist;
        this.widthDist = widthDist;
        this.maximumSpeedDist = maximumSpeedDist;
    }

    /** {@inheritDoc} */
    @Override
    public final OTSSimulatorInterface getSimulator()
    {
        return this.simulator;
    }

    /**
     * @return lengthDist.
     */
    @Override
    public final ContinuousDistDoubleScalar.Rel<Length, LengthUnit> getLengthDist()
    {
        return this.lengthDist;
    }

    /**
     * @return widthDist.
     */
    @Override
    public final ContinuousDistDoubleScalar.Rel<Length, LengthUnit> getWidthDist()
    {
        return this.widthDist;
    }

    /**
     * @return maximumSpeedDist.
     */
    @Override
    public final ContinuousDistDoubleScalar.Rel<Speed, SpeedUnit> getMaximumSpeedDist()
    {
        return this.maximumSpeedDist;
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "GtuGeneratorIndividual [lengthDist=" + this.lengthDist + ", widthDist=" + this.widthDist + ", maximumSpeedDist="
                + this.maximumSpeedDist + "]";
    }

    /** {@inheritDoc} */
    @Override
    public Serializable getSourceId()
    {
        return getName();
    }

}
