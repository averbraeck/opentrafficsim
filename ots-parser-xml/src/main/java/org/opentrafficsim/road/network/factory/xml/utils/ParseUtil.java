package org.opentrafficsim.road.network.factory.xml.utils;

import java.util.ArrayList;
import java.util.List;

import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.core.parameters.InputParameters;
import org.opentrafficsim.road.gtu.generator.CfBaRoomChecker;
import org.opentrafficsim.road.gtu.generator.CfRoomChecker;
import org.opentrafficsim.road.gtu.generator.TtcRoomChecker;
import org.opentrafficsim.road.gtu.generator.LaneBasedGtuGenerator.RoomChecker;
import org.opentrafficsim.road.network.factory.xml.XmlParserException;
import org.opentrafficsim.xml.bindings.types.LengthBeginEndType.LengthBeginEnd;
import org.opentrafficsim.xml.generated.RandomStreamSource;
import org.opentrafficsim.xml.generated.RoomCheckerType;

import nl.tudelft.simulation.dsol.experiment.StreamInformation;
import nl.tudelft.simulation.jstats.streams.StreamInterface;

/**
 * Parser - Utility class for parsing using JAXB generated classes.
 * <p>
 * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck" target="_blank">Alexander Verbraeck</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public final class ParseUtil
{
    /** */
    private ParseUtil()
    {
        // utility class
    }

    /**
     * Returns all objects of given type from the list of all objects. The returned list may be altered as it is not backed by
     * the input list.
     * @param objectList List&lt;?&gt;; list of objects
     * @param clazz Class&lt;T&gt;; class of type of objects to return
     * @param <T> type
     * @return list of all objects of given type from the list of all objects
     */
    @SuppressWarnings("unchecked")
    public static <T> List<T> getObjectsOfType(final List<?> objectList, final Class<T> clazz)
    {
        List<T> list = new ArrayList<>();
        for (Object object : objectList)
        {
            if (clazz.isAssignableFrom(object.getClass()))
            {
                list.add((T) object);
            }
        }
        return list;
    }

    /**
     * Find and return the stream belonging to te streamId.
     * @param streamInformation the map with streams from the RUN tag
     * @param streamSource the stream source
     * @param inputParameters InputParameters; input parameters.
     * @return the stream belonging to te streamId
     * @throws XmlParserException when the stream could not be found
     */
    public static StreamInterface findStream(final StreamInformation streamInformation, final RandomStreamSource streamSource,
            final InputParameters inputParameters) throws XmlParserException
    {
        String streamId;
        if (streamSource == null || streamSource.getDefault() == null)
        {
            streamId = "default";
        }
        else if (streamSource.getGeneration() == null)
        {
            streamId = "generation";
        }
        else
        {
            streamId = streamSource.getDefined().get(inputParameters);
        }
        if (streamInformation.getStream(streamId) == null)
        {
            throw new XmlParserException("Could not find stream with Id=" + streamId);
        }
        return streamInformation.getStream(streamId);
    }
    
    /**
     * Parse LengthBeginEnd for a Lane.
     * @param lbe LengthBeginEnd; the begin, end, fraction, or offset from begin or end on the lane
     * @param laneLength Length; the length of the lane
     * @return the offset on the lane
     */
    public static Length parseLengthBeginEnd(final LengthBeginEnd lbe, final Length laneLength)
    {
        if (lbe.isAbsolute())
        {
            if (lbe.isBegin())
                return lbe.getOffset();
            else
                return laneLength.minus(lbe.getOffset());
        }
        else
        {
            return laneLength.times(lbe.getFraction());
        }
    }

    /**
     * Parse room checker.
     * @param roomChecker RoomCheckerType; room checker type
     * @param inputParameters InputParameters; input parameters.
     * @return RoomChecker; parsed room checker
     */
    public static RoomChecker parseRoomChecker(final RoomCheckerType roomChecker, final InputParameters inputParameters)
    {
        if (roomChecker == null || roomChecker.getCf() != null)
        {
            return new CfRoomChecker();
        }
        else if (roomChecker.getCfBa() != null)
        {
            return new CfBaRoomChecker();
        }
        return new TtcRoomChecker(roomChecker.getTtc().get(inputParameters));
    }
}
