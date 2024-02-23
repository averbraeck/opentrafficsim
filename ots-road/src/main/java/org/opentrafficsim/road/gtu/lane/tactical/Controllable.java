package org.opentrafficsim.road.gtu.lane.tactical;

/**
 * Interface for tactical planners to return the control state for visualization.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public interface Controllable
{

    /**
     * Returns the control state.
     * @return State; control state
     */
    State getControlState();

    /**
     * Control state.
     * <p>
     * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
     * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
     * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
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
         * @return boolean; whether this is NONE
         */
        public boolean isNone()
        {
            return this.equals(NONE);
        }

        /**
         * Returns whether this is DISABLED.
         * @return boolean; whether this is DISABLED
         */
        public boolean isDisabled()
        {
            return this.equals(DISABLED);
        }

        /**
         * Returns whether this is ENABLED.
         * @return boolean; whether this is ENABLED
         */
        public boolean isEnabled()
        {
            return this.equals(ENABLED);
        }

    }

}
