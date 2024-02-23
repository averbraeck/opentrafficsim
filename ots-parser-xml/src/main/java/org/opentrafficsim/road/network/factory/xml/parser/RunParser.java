package org.opentrafficsim.road.network.factory.xml.parser;

import java.util.LinkedHashMap;
import java.util.Map;

import org.djunits.unit.DurationUnit;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Time;
import org.djutils.eval.Eval;
import org.opentrafficsim.core.dsol.OtsSimulatorInterface;
import org.opentrafficsim.road.network.factory.xml.XmlParserException;
import org.opentrafficsim.xml.generated.RandomStream;
import org.opentrafficsim.xml.generated.RandomStream.Replication;
import org.opentrafficsim.xml.generated.Run;

import nl.tudelft.simulation.dsol.experiment.ExperimentRunControl;
import nl.tudelft.simulation.dsol.experiment.StreamSeedInformation;
import nl.tudelft.simulation.jstats.streams.MersenneTwister;

/**
 * RunParser parses the XML nodes of the Run tag, including the RandomStream tags.
 * <p>
 * Copyright (c) 2003-2024 Delft University of Technology, Jaffalaan 5, 2628 BX Delft, the Netherlands. All rights reserved.
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/v2/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck" target="_blank">Alexander Verbraeck</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public final class RunParser
{
    /** */
    private RunParser()
    {
        // utility class
    }
    
    /**
     * Parse random number streams.
     * @param run Run; run tag.
     * @param eval Eval; expression evaluator.
     * @return stream information.
     */
    public static StreamSeedInformation parseStreams(final Run run, final Eval eval)
    {
        StreamSeedInformation streamInformation = new StreamSeedInformation();
        int numberReplications = run.getNumberReplications() == null ? 1 : run.getNumberReplications().get(eval);
        if (run.getRandomStreams() != null)
        {
            for (RandomStream streamTag : run.getRandomStreams().getRandomStream())
            {
                String streamId = streamTag.getId();
                Map<Integer, Long> seedMap = new LinkedHashMap<>();
                for (Replication rep : streamTag.getReplication())
                {
                    seedMap.put(rep.getId().intValue(), rep.getSeed().longValue());
                }
                if (seedMap.containsKey(0))
                {
                    streamInformation.addStream(streamId, new MersenneTwister(seedMap.get(0)));
                }
                else
                {
                    streamInformation.addStream(streamId, new MersenneTwister(10L));
                }
                streamInformation.putSeedMap(streamId, seedMap);
            }
        }
        for (String streamId : new String[] {"default", "generation"})
        {
            if (streamInformation.getSeedMap(streamId) == null)
            {
                Map<Integer, Long> seedMap = new LinkedHashMap<>();
                for (int rep = 0; rep < numberReplications; rep++)
                {
                    seedMap.put(rep, (long) streamId.hashCode() + rep);
                }
                streamInformation.addStream(streamId, new MersenneTwister(seedMap.get(0)));
                streamInformation.putSeedMap(streamId, seedMap);
            }
        }
        return streamInformation;
    }

    /**
     * Parse the Run tag in the OTS XML file.
     * @param run Run; the Run tag
     * @param networkId String; id of the network or the model
     * @param streamInformation StreamSeedInformation; the stream information that will be passed to the model
     * @param simulator OtsSimulatorInterface; the simulator to defined the experiment for
     * @param eval Eval; expression evaluator.
     * @return experiment on the basis of the information in the Run tag
     * @throws XmlParserException on parsing error
     */
    public static ExperimentRunControl<Duration> parseRun(final String networkId, final Run run,
            final StreamSeedInformation streamInformation, final OtsSimulatorInterface simulator,
            final Eval eval) throws XmlParserException
    {
        int numberReplications = run.getNumberReplications() == null ? 1 : run.getNumberReplications().get(eval);
        Time startTime = run.getStartTime() == null ? Time.ZERO : run.getStartTime().get(eval);
        Duration warmupPeriod = run.getWarmupPeriod() == null ? Duration.ZERO : run.getWarmupPeriod().get(eval);
        Duration runLength =
                run.getRunLength() == null ? new Duration(1.0, DurationUnit.HOUR) : run.getRunLength().get(eval);

        // TODO: do we want a real Time here or a Duration?
        // If it should be a Time, create an ExperimentRunControl that can take a Time as first argument
        ExperimentRunControl<Duration> runControl = new ExperimentRunControl<>("RunControl for " + networkId,
                startTime.minus(Time.ZERO), warmupPeriod, runLength, numberReplications);

        return runControl;
    }
}
