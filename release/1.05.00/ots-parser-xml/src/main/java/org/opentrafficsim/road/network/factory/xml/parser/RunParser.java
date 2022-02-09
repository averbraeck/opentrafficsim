package org.opentrafficsim.road.network.factory.xml.parser;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.road.network.factory.xml.XmlParserException;
import org.opentrafficsim.xml.generated.RANDOMSTREAM;
import org.opentrafficsim.xml.generated.RANDOMSTREAM.REPLICATION;
import org.opentrafficsim.xml.generated.RUN;

import nl.tudelft.simulation.dsol.experiment.ExperimentRunControl;
import nl.tudelft.simulation.dsol.experiment.StreamSeedInformation;
import nl.tudelft.simulation.jstats.streams.MersenneTwister;

/**
 * RunParser parses the XML nodes of the RUN tag, including the RANDOMSTREAM tags.
 * <p>
 * Copyright (c) 2003-2022 Delft University of Technology, Jaffalaan 5, 2628 BX Delft, the Netherlands. All rights reserved.
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/v2/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://www.tudelft.nl/averbraeck" target="_blank">Alexander Verbraeck</a>
 */
public final class RunParser
{
    /** */
    private RunParser()
    {
        // utility class
    }

    /**
     * Parse the RUN tag in the OTS XML file.
     * @param run RUN; the RUN tag
     * @param networkId String; id of the network or the model
     * @param streamInformation StreamSeedInformation; the stream information that will be passed to the model
     * @param simulator OTSSimulatorInterface; the simulator to defined the experiment for
     * @return experiment on the basis of the information in the RUN tag
     * @throws XmlParserException on parsing error
     */
    public static ExperimentRunControl.TimeDoubleUnit parseRun(final String networkId, final RUN run,
            final StreamSeedInformation streamInformation, final OTSSimulatorInterface simulator) throws XmlParserException
    {
        int numberReplications = run.getNUMBERREPLICATIONS() == null ? 1 : run.getNUMBERREPLICATIONS().intValue();
        Time startTime = run.getSTARTTIME() == null ? Time.ZERO : run.getSTARTTIME();
        Duration warmupPeriod = run.getWARMUPPERIOD() == null ? Duration.ZERO : run.getWARMUPPERIOD();
        Duration runLength = run.getRUNLENGTH() == null ? Duration.ZERO : run.getRUNLENGTH();

        if (run.getRANDOMSTREAMS() == null)
        {
            for (String streamId : new String[] {"default", "generation"})
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
        else
        {
            List<RANDOMSTREAM> streamTags = run.getRANDOMSTREAMS().getRANDOMSTREAM();
            for (RANDOMSTREAM streamTag : streamTags)
            {
                String streamId = streamTag.getID();
                Map<Integer, Long> seedMap = new LinkedHashMap<>();
                for (REPLICATION rep : streamTag.getREPLICATION())
                {
                    seedMap.put(rep.getID().intValue(), rep.getSEED().longValue());
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

        ExperimentRunControl.TimeDoubleUnit runControl = new ExperimentRunControl.TimeDoubleUnit("RunControl for " + networkId,
                startTime, warmupPeriod, runLength, numberReplications);

        return runControl;
    }
}
