package org.opentrafficsim.kpi.sampling.impl;

import java.util.ArrayList;
import java.util.List;

import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.kpi.interfaces.LinkData;

/**
 * Test LinkData class.
 */
public class TestLinkData implements LinkData<TestLaneData>
{

    /** Id. */
    private final String id;

    /** Length. */
    private final Length length;

    /** Lanes. */
    private final List<TestLaneData> lanes = new ArrayList<>();

    /**
     * Constructor.
     * @param id id
     * @param length length
     */
    public TestLinkData(final String id, final Length length)
    {
        this.id = id;
        this.length = length;
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
    public List<TestLaneData> getLanes()
    {
        return this.lanes;
    }
    
    /**
     * Adds lane.
     * @param lane lane
     */
    void addLane(final TestLaneData lane)
    {
        this.lanes.add(lane);
    }

}
