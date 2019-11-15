package org.opentrafficsim.road.gtu.lane.tactical;

/**
 * Interface for tactical planners to return the control state for visualization. 
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version May 1, 2019 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
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
     * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
     * <p>
     * @version $Revision$, $LastChangedDate$, by $Author$, initial version May 1, 2019 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
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
