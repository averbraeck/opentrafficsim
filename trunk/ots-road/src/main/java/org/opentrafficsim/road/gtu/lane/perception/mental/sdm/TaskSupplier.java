package org.opentrafficsim.road.gtu.lane.perception.mental.sdm;

import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.gtu.lane.perception.mental.ConstantTask;
import org.opentrafficsim.road.gtu.lane.perception.mental.Task;

/**
 * Supplies a Task for within Fullers model.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 28 jun. 2018 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
@FunctionalInterface
public interface TaskSupplier
{

    /**
     * Returns a task for the given GTU.
     * @param gtu LaneBasedGTU; gtu
     * @return Task; task for given GTU
     */
    Task getTask(LaneBasedGTU gtu);

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
        public Task getTask(final LaneBasedGTU gtu)
        {
            return new ConstantTask(this.id, this.taskDemand);
        }
    }

}
