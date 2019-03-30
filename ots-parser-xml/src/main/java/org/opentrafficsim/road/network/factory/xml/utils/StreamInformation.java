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
    private final String id;
    private final Map<Integer, Long> seedMap;
    private final StreamInterface stream = new MersenneTwister(1L);
    
    /**
     * 
     */
    public StreamInformation(final String id, final Map<Integer, Long> seedMap)
    {
        this.id = id;
        this.seedMap = seedMap;
        this.stream.setSeed(seedMap.get(1));
    }

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

