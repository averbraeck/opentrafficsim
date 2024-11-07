package org.opentrafficsim.kpi.sampling.impl;

import java.util.LinkedHashSet;
import java.util.Set;

import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.kpi.sampling.Sampler;
import org.opentrafficsim.kpi.sampling.data.ExtendedDataType;
import org.opentrafficsim.kpi.sampling.filter.FilterDataType;

/**
 * Test Sampler class.
 */
public class TestSampler extends Sampler<TestGtuData, TestLaneData>
{

    /** Simulator. */
    private final TestSimulator simulator;

    /** Initialization moments. */
    private final Set<Entry> inits = new LinkedHashSet<>();

    /** Finalization moments. */
    private final Set<Entry> finals = new LinkedHashSet<>();

    /**
     * Constructor.
     * @param extendedDataTypes extended data types
     * @param filterDataTypes filter data types
     * @param simulator simulator
     */
    public TestSampler(final Set<ExtendedDataType<?, ?, ?, ? super TestGtuData>> extendedDataTypes,
            final Set<FilterDataType<?, ? super TestGtuData>> filterDataTypes, final TestSimulator simulator)
    {
        super(extendedDataTypes, filterDataTypes);
        this.simulator = simulator;
    }

    @Override
    public Time now()
    {
        return this.simulator.getTime();
    }

    @Override
    public void scheduleStartRecording(final Time time, final TestLaneData lane)
    {
        this.simulator.addEvent(this, time, lane, true);
    }

    @Override
    public void scheduleStopRecording(final Time time, final TestLaneData lane)
    {
        this.simulator.addEvent(this, time, lane, false);
    }

    @Override
    public void initRecording(final TestLaneData lane)
    {  
        this.inits.add(new Entry(now(), lane));
    }

    @Override
    public void finalizeRecording(final TestLaneData lane)
    {
        this.finals.add(new Entry(now(), lane));
    }
    
    /**
     * Returns the moments of initialization.
     * @return moments of initialization
     */
    public Set<Entry> getInits()
    {
        return this.inits;
    }

    /**
     * Returns the moments of finalization.
     * @return moments of finalization
     */
    public Set<Entry> getFinals()
    {
        return this.finals;
    }

    /**
     * Entry of initialization and finalization.
     * @param time time
     * @param lane lane
     */
    public record Entry(Time time, TestLaneData lane)
    {
    }

}
