package org.opentrafficsim.kpi.sampling.impl;

import java.util.ArrayList;
import java.util.List;

import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.kpi.interfaces.LinkData;

/**
 * Test LinkData class.
 * @param getId id
 * @param getLength length
 * @param getLanes lanes
 */
public record TestLinkData(String getId, Length getLength, List<TestLaneData> getLanes) implements LinkData<TestLaneData>
{

    /**
     * Constructor.
     * @param id id
     * @param length length
     */
    public TestLinkData(final String id, final Length length)
    {
        this(id, length, new ArrayList<>());
    }

    /**
     * Adds lane.
     * @param lane lane
     */
    void addLane(final TestLaneData lane)
    {
        getLanes().add(lane);
    }

}
