package org.opentrafficsim.road.gtu.lane.perception.mental.sdm;

import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;
import org.opentrafficsim.road.gtu.lane.perception.mental.ConstantTask;
import org.opentrafficsim.road.gtu.lane.perception.mental.Task;

/**
 * Supplies a Task for within Fullers model.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
@FunctionalInterface
public interface TaskSupplier
{

    /**
     * Returns a task for the given GTU.
     * @param gtu LaneBasedGtu; gtu
     * @return Task; task for given GTU
     */
    Task getTask(LaneBasedGtu gtu);

    /**
     * Class that supplies a constant task.
     */
    class Constant implements TaskSupplier
    {
        /** Id. */
        private final String id;

        /** Task demand. */
        private final double taskDemand;

        /**
         * Constructor.
         * @param id String; id
         * @param taskDemand double; task demand
         */
        public Constant(final String id, final double taskDemand)
        {
            this.id = id;
            this.taskDemand = taskDemand;
        }

        /** {@inheritDoc} */
        @Override
        public Task getTask(final LaneBasedGtu gtu)
        {
            return new ConstantTask(this.id, this.taskDemand);
        }
    }

}
