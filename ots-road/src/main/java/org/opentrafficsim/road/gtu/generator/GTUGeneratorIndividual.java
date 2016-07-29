package org.opentrafficsim.road.gtu.generator;

import java.io.Serializable;

import nl.tudelft.simulation.dsol.SimRuntimeException;

import org.djunits.unit.LengthUnit;
import org.djunits.unit.SpeedUnit;
import org.djunits.unit.TimeUnit;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.gtu.GTUDirectionality;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.gtu.animation.GTUColorer;
import org.opentrafficsim.core.network.OTSNetwork;
import org.opentrafficsim.core.units.distributions.ContinuousDistDoubleScalar;
import org.opentrafficsim.road.gtu.lane.perceptionold.LanePerceptionFull;
import org.opentrafficsim.road.gtu.strategical.LaneBasedStrategicalPlanner;
import org.opentrafficsim.road.network.lane.Lane;

/**
 * Generate GTUs.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision: 1401 $, $LastChangedDate: 2015-09-14 01:33:02 +0200 (Mon, 14 Sep 2015) $, by $Author: averbraeck $,
 *          initial version Feb 2, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class GTUGeneratorIndividual extends AbstractGTUGenerator implements Serializable
{
    /** */
    private static final long serialVersionUID = 20160000L;

    /** Simulator to schedule next arrival events. */
    private final OTSDEVSSimulatorInterface simulator;

    /** Distribution of the length of the GTU. */
    private final ContinuousDistDoubleScalar.Rel<Length, LengthUnit> lengthDist;

    /** Distribution of the width of the GTU. */
    private final ContinuousDistDoubleScalar.Rel<Length, LengthUnit> widthDist;

    /** Distribution of the maximum speed of the GTU. */
    private final ContinuousDistDoubleScalar.Rel<Speed, SpeedUnit> maximumSpeedDist;

    /**
     * @param name the name of the generator
     * @param gtuType the type of GTU to generate
     * @param gtuClass the gtu class to instantiate
     * @param initialSpeedDist distribution of the initial speed of the GTU
     * @param interarrivelTimeDist distribution of the interarrival time
     * @param maxGTUs maximum number of GTUs to generate
     * @param startTime start time of generation (delayed start)
     * @param endTime end time of generation
     * @param simulator simulator to schedule next arrival events
     * @param lengthDist distribution of the length of the GTU
     * @param widthDist distribution of the width of the GTU
     * @param maximumSpeedDist distribution of the maximum speed of the GTU
     * @param lane Lane on which newly GTUs are placed
     * @param position position on the lane, relative to the design line of the link
     * @param direction the direction on the lane in which the GTU has to be generated (DIR_PLUS, or DIR_MINUS)
     * @param gtuColorer the GTUColorer to use
     * @param strategicalPlanner the lane-based strategical planner to use
     * @param perceptionClass the LanePerception class to use (stateful, so has to be class-based)
     * @param network the network to register the GTU into
     * @throws SimRuntimeException when simulation scheduling fails
     */
    @SuppressWarnings("checkstyle:parameternumber")
    public GTUGeneratorIndividual(final String name, final OTSDEVSSimulatorInterface simulator, final GTUType gtuType,
        final Class<?> gtuClass, final ContinuousDistDoubleScalar.Rel<Speed, SpeedUnit> initialSpeedDist,
        final ContinuousDistDoubleScalar.Rel<Duration, TimeUnit> interarrivelTimeDist,
        final ContinuousDistDoubleScalar.Rel<Length, LengthUnit> lengthDist,
        final ContinuousDistDoubleScalar.Rel<Length, LengthUnit> widthDist,
        final ContinuousDistDoubleScalar.Rel<Speed, SpeedUnit> maximumSpeedDist, final long maxGTUs,
        final Time startTime, final Time endTime, final Lane lane, final Length position,
        final GTUDirectionality direction, final GTUColorer gtuColorer,
        final LaneBasedStrategicalPlanner strategicalPlanner, final Class<LanePerceptionFull> perceptionClass,
        final OTSNetwork network) throws SimRuntimeException
    {
        super(name, simulator, gtuType, gtuClass, initialSpeedDist, interarrivelTimeDist, maxGTUs, startTime, endTime,
            lane, position, direction, gtuColorer, strategicalPlanner, perceptionClass, network);
        this.simulator = simulator;
        this.lengthDist = lengthDist;
        this.widthDist = widthDist;
        this.maximumSpeedDist = maximumSpeedDist;
    }

    /** {@inheritDoc} */
    @Override
    public final OTSDEVSSimulatorInterface getSimulator()
    {
        return this.simulator;
    }

    /**
     * @return lengthDist.
     */
    public final ContinuousDistDoubleScalar.Rel<Length, LengthUnit> getLengthDist()
    {
        return this.lengthDist;
    }

    /**
     * @return widthDist.
     */
    public final ContinuousDistDoubleScalar.Rel<Length, LengthUnit> getWidthDist()
    {
        return this.widthDist;
    }

    /**
     * @return maximumSpeedDist.
     */
    public final ContinuousDistDoubleScalar.Rel<Speed, SpeedUnit> getMaximumSpeedDist()
    {
        return this.maximumSpeedDist;
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "GTUGeneratorIndividual [lengthDist=" + this.lengthDist + ", widthDist=" + this.widthDist
                + ", maximumSpeedDist=" + this.maximumSpeedDist + "]";
    }

}
