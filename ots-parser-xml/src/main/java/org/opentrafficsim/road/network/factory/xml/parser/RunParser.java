package org.opentrafficsim.road.network.factory.xml.parser;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.naming.NamingException;

import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.core.dsol.OTSReplication;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.road.network.OTSRoadNetwork;
import org.opentrafficsim.road.network.factory.xml.XmlParserException;
import org.opentrafficsim.road.network.factory.xml.utils.StreamInformation;
import org.opentrafficsim.xml.generated.RANDOMSTREAM;
import org.opentrafficsim.xml.generated.RANDOMSTREAM.REPLICATION;
import org.opentrafficsim.xml.generated.RUN;

import nl.tudelft.simulation.dsol.experiment.Experiment;
import nl.tudelft.simulation.dsol.experiment.Treatment;
import nl.tudelft.simulation.jstats.streams.StreamInterface;

/**
 * RunParser parses the XML nodes of the RUN tag, including the RANDOMSTREAM tags. <br>
 * <br>
 * Copyright (c) 2003-2018 Delft University of Technology, Jaffalaan 5, 2628 BX Delft, the Netherlands. All rights reserved. See
 * for project information <a href="https://www.simulation.tudelft.nl/" target="_blank">www.simulation.tudelft.nl</a>. The
 * source code and binary code of this software is proprietary information of Delft University of Technology.
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
     * @param run the RUN tag
     * @param otsNetwork the network
     * @param streamMap the map with the random streams to be filled
     * @param simulator the simulator to defined the experiment for
     * @return experiment on the basis of the information in the RUN tag
     * @throws XmlParserException on parsing error
     */
    public static Experiment.TimeDoubleUnit<OTSSimulatorInterface> parseRun(final OTSRoadNetwork otsNetwork, final RUN run,
            final Map<String, StreamInformation> streamMap, final OTSSimulatorInterface simulator) throws XmlParserException
    {
        int numberReplications = run.getNUMBERREPLICATIONS() == null ? 1 : run.getNUMBERREPLICATIONS().intValue();
        Time startTime = run.getSTARTTIME() == null ? Time.ZERO : run.getSTARTTIME();
        Duration warmupPeriod = run.getWARMUPPERIOD() == null ? Duration.ZERO : run.getWARMUPPERIOD();
        Duration runLength = run.getRUNLENGTH() == null ? Duration.ZERO : run.getRUNLENGTH();

        Map<String, StreamInterface> streams = new LinkedHashMap<>();
        if (run.getRANDOMSTREAMS() == null)
        {
            for (String streamId : new String[] {"default", "generation"})
            {
                Map<Integer, Long> seedMap = new LinkedHashMap<>();
                for (int rep = 1; rep <= numberReplications; rep++)
                {
                    seedMap.put(rep, (long) streamId.hashCode() + rep);
                }
                StreamInformation streamInformation = new StreamInformation(streamId, seedMap);
                streamMap.put(streamId, streamInformation);
                streams.put(streamId, streamInformation.getStream());
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
                StreamInformation streamInformation = new StreamInformation(streamId, seedMap);
                streamMap.put(streamId, streamInformation);
                streams.put(streamId, streamInformation.getStream());
            }
        }

        Experiment.TimeDoubleUnit<OTSSimulatorInterface> experiment = new Experiment.TimeDoubleUnit<>();

        Treatment.TimeDoubleUnit treatment = new Treatment.TimeDoubleUnit(experiment, "Treatment for " + otsNetwork.getId(),
                startTime, warmupPeriod, runLength);
        experiment.setTreatment(treatment);
        for (

                int replicationNr = 1; replicationNr <= numberReplications; replicationNr++)
        {
            try
            {
                OTSReplication replication = new OTSReplication("Replication " + replicationNr, experiment);
                replication.setStreams(streams);
                // TODO: the seeds need to be updated with the start of each new replication. Maybe change in DSOL.
            }
            catch (NamingException exception)
            {
                throw new XmlParserException("Problems generating replicaton", exception);
            }
        }

        return experiment;
    }
}
