package org.opentrafficsim.kpi.sampling.impl;

import java.util.PriorityQueue;

import org.djunits.value.vdouble.scalar.Duration;

/**
 * Test simulator.
 */
public class TestSimulator
{

    /** Event queue. */
    private final PriorityQueue<Event> queue = new PriorityQueue<>();

    /** Current time. */
    private Duration now = Duration.ZERO;

    /**
     * Constructor.
     */
    public TestSimulator()
    {
        //
    }

    /**
     * Add event.
     * @param sampler sampler
     * @param time time
     * @param lane lane
     * @param start start or stop recording
     */
    public void addEvent(final TestSampler sampler, final Duration time, final TestLaneData lane, final boolean start)
    {
        this.queue.add(new Event(sampler, time, lane, start));
    }

    /**
     * Executes all events until time (inclusive).
     * @param time time
     */
    public void executeUntil(final Duration time)
    {
        while (!this.queue.isEmpty())
        {
            Event event = this.queue.peek();
            if (event.time().gt(time))
            {
                return;
            }
            this.queue.remove();
            this.now = event.time;
            event.execute();
        }
    }

    /**
     * Set time.
     * @param time time
     */
    public void setTime(final Duration time)
    {
        this.now = time;
    }

    /**
     * Returns the current time.
     * @return time
     */
    public Duration getTime()
    {
        return this.now;
    }

    /**
     * Event.
     * @param sampler sampler
     * @param time time
     * @param lane lane
     * @param start start or stop recording
     */
    private record Event(TestSampler sampler, Duration time, TestLaneData lane, boolean start) implements Comparable<Event>
    {
        /**
         * Execute event.
         */
        public void execute()
        {
            if (this.start)
            {
                this.sampler.startRecording(this.lane);
            }
            else
            {
                this.sampler.stopRecording(this.lane);
            }
        }

        @Override
        public int compareTo(final Event o)
        {
            return this.time.compareTo(o.time);
        }
    };

}
