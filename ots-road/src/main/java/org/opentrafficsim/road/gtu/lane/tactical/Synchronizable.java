package org.opentrafficsim.road.gtu.lane.tactical;

/**
 * Interface for tactical planners that can return synchronization information for visualization.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public interface Synchronizable
{

    /**
     * Returns the synchronization state.
     * @return synchronization state
     */
    State getSynchronizationState();

    /**
     * State of synchronization.
     * <p>
     * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
     * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
     * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
     */
    enum State
    {
        /** No synchronization. */
        NONE,

        /** Subject vehicle is adjusting speed. */
        SYNCHRONIZING,

        /** Subject vehicle is adjusting speed and indicating desired lane change. */
        INDICATING,

        /** Subject vehicle is cooperating for a lane change of another GTU. */
        COOPERATING;

        /**
         * Returns whether this is NONE.
         * @return whether this is NONE
         */
        public boolean isNone()
        {
            return this == NONE;
        }

        /**
         * Returns whether this is SYNCHRONIZING.
         * @return whether this is SYNCHRONIZING
         */
        public boolean isSycnhronizing()
        {
            return this == SYNCHRONIZING;
        }

        /**
         * Returns whether this is INDICATING.
         * @return whether this is INDICATING
         */
        public boolean isIndicating()
        {
            return this == INDICATING;
        }

        /**
         * Returns whether this is COOPERATING.
         * @return whether this is COOPERATING
         */
        public boolean isCooperating()
        {
            return this == COOPERATING;
        }
    }

}
