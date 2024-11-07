package org.opentrafficsim.kpi.sampling.impl;

import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.kpi.interfaces.LaneData;

/**
 * Test LaneData class.
 */
public class TestLaneData implements LaneData<TestLaneData>
{

    /** Id. */
    private final String id;

    /** Length. */
    private final Length length;

    /** Link. */
    private final TestLinkData link;

    /**
     * constructor.
     * @param id id
     * @param length length
     * @param link link
     */
    public TestLaneData(final String id, final Length length, final TestLinkData link)
    {
        this.id = id;
        this.length = length;
        this.link = link;
        this.link.addLane(this);
    }

    @Override
    public String getId()
    {
        return this.id;
    }

    @Override
    public Length getLength()
    {
        return this.length;
    }

    @Override
    public TestLinkData getLinkData()
    {
        return this.link;
    }

}
