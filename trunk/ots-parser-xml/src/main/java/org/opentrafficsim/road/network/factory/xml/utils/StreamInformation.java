package org.opentrafficsim.road.network.factory.xml.utils;

import java.util.Map;

import nl.tudelft.simulation.jstats.streams.MersenneTwister;
import nl.tudelft.simulation.jstats.streams.StreamInterface;

/**
 * StreamInformation contains information about the stream and its seeds. <br>
 * <br>
 * Copyright (c) 2003-2018 Delft University of Technology, Jaffalaan 5, 2628 BX Delft, the Netherlands. All rights reserved. See
 * for project information <a href="https://www.simulation.tudelft.nl/" target="_blank">www.simulation.tudelft.nl</a>. The
 * source code and binary code of this software is proprietary information of Delft University of Technology.
 * @author <a href="https://www.tudelft.nl/averbraeck" target="_blank">Alexander Verbraeck</a>
 */
public class StreamInformation
{
    /** the stream id. */
    private final String id;

    /** the seed map, mapping replication number to seed. */
    private final Map<Integer, Long> seedMap;

    /** the stream itself; the seed value of 1L will be updated per replication. */
    private final StreamInterface stream = new MersenneTwister(1L);

    /**
     * @param id the stream id
     * @param seedMap the seed map, mapping replication number to seed
     */
    public StreamInformation(final String id, final Map<Integer, Long> seedMap)
    {
        this.id = id;
        this.seedMap = seedMap;
        this.stream.setSeed(seedMap.get(1));
    }

    // TODO: make sure the seed map is used by the OTSExperiment / OTSReplication
    
    /**
     * @return id
     */
    public final String getId()
    {
        return this.id;
    }

    /**
     * @return stream
     */
    public final StreamInterface getStream()
    {
        return this.stream;
    }

}
