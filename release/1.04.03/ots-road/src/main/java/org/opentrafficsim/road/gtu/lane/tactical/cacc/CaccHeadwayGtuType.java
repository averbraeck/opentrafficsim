package org.opentrafficsim.road.gtu.lane.tactical.cacc;

import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.gtu.lane.perception.categories.WienerProcess;
import org.opentrafficsim.road.gtu.lane.perception.categories.neighbors.HeadwayGtuType;
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayGTU;
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayGTUPerceived;
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayGTUReal;

import nl.tudelft.simulation.jstats.streams.StreamInterface;

/**
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 17 okt. 2018 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class CaccHeadwayGtuType implements HeadwayGtuType
{

    /** Error factor for headway. */
    private WienerProcess distHeadwayError;

    /**
     * Constructor.
     * @param randomStream StreamInterface; stream for random numbers
     * @param simulator OTSSimulatorInterface; simulator
     * @param parameters Parameters; parameters
     */
    public CaccHeadwayGtuType(final StreamInterface randomStream, final OTSSimulatorInterface simulator,
            final Parameters parameters)
    {
        // TODO parameterize randomness, now assumed at 5% standard deviation
        this.distHeadwayError = new WienerProcess(randomStream, 1.0, 0.05, Duration.instantiateSI(20), simulator);
    }

    /** {@inheritDoc} */
    @Override
    public HeadwayGTU createDownstreamGtu(final LaneBasedGTU perceivingGtu, final LaneBasedGTU perceivedGtu,
            final Length distance) throws GTUException, ParameterException
    {
        return createUpOrDownstreamGtu(perceivingGtu, perceivedGtu, distance);
    }

    /** {@inheritDoc} */
    @Override
    public HeadwayGTU createUpstreamGtu(final LaneBasedGTU perceivingGtu, final LaneBasedGTU perceivedGtu,
            final Length distance) throws GTUException, ParameterException
    {
        return createUpOrDownstreamGtu(perceivingGtu, perceivedGtu, distance);
    }

    /**
     * Creates a headway object from a GTU, downstream or upsteam.
     * @param perceivingGtu LaneBasedGTU; perceiving GTU
     * @param perceivedGtu LaneBasedGTU; perceived GTU
     * @param distance Length; distance
     * @return headway object from a gtu
     * @throws GTUException when headway object cannot be created
     * @throws ParameterException on invalid parameter value or missing parameter
     */
    private HeadwayGTU createUpOrDownstreamGtu(final LaneBasedGTU perceivingGtu, final LaneBasedGTU perceivedGtu,
            final Length distance) throws GTUException, ParameterException
    {
        if (perceivedGtu.getGTUType().getId().equals("CACC"))
        {
            // CACC neighbor, assume perfect knowledge
            return new HeadwayGTUReal(perceivedGtu, distance, true);
        }
        // TODO add noise
        // TODO use different Wiener process for each of the 4 surrounding neighbors?
        // let's add some noise, acceleration and desired speed are null
        double err = this.distHeadwayError.draw();
        Length d = Length.interpolate(Length.ZERO, distance, err); // distance.multiplyBy(err);
        double dv = perceivedGtu.getSpeed().si - perceivingGtu.getSpeed().si;
        Speed v = Speed.instantiateSI(perceivingGtu.getSpeed().si + dv * err * err); // TODO err^2 assumed on speed, ok?
        return new HeadwayGTUPerceived(perceivedGtu, d, v, null);
    }

    /** {@inheritDoc} */
    @Override
    public HeadwayGTU createParallelGtu(final LaneBasedGTU perceivingGtu, final LaneBasedGTU perceivedGtu,
            final Length overlapFront, final Length overlap, final Length overlapRear) throws GTUException
    {
        // for adjacent vehicles, we assume perfect knowledge
        return new HeadwayGTUReal(perceivedGtu, overlapFront, overlap, overlapRear, true);
    }

}
