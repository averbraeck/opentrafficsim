package org.opentrafficsim.road.gtu.lane.tactical;

/**
 * Interface for tactical planners to return the control state for visualization.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
@FunctionalInterface
public interface Controllable
{

    /**
     * Returns the control state.
     * @return control state
     */
    State getControlState();

    /**
     * Control state.
     * <p>
     * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
     * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
     * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
     */
    enum State
    {

        /** GTU has no control. */
        NONE,

        /** Control is disabled. */
        DISABLED,

        /** Control is enabled. */
        ENABLED;

        /**
         * Returns whether this is NONE.
         * @return whether this is NONE
         */
        public boolean isNone()
        {
            return this.equals(NONE);
        }

        /**
         * Returns whether this is DISABLED.
         * @return whether this is DISABLED
         */
        public boolean isDisabled()
        {
            return this.equals(DISABLED);
        }

        /**
         * Returns whether this is ENABLED.
         * @return whether this is ENABLED
         */
        public boolean isEnabled()
        {
            return this.equals(ENABLED);
        }

    }

}
