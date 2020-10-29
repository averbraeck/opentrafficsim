package org.opentrafficsim.road.gtu.lane.tactical;

/**
 * Interface for tactical planners that can return synchronization information for visualization.
 * <p>
 * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 23 feb. 2018 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public interface Synchronizable
{

    /**
     * Returns the synchronization state.
     * @return State; synchronization state
     */
    State getSynchronizationState();

    /**
     * State of synchronization.
     * <p>
     * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
     * <p>
     * @version $Revision$, $LastChangedDate$, by $Author$, initial version 23 feb. 2018 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
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
         * @return boolean; whether this is NONE
         */
        public boolean isNone()
        {
            return this == NONE;
        }
        
        /**
         * Returns whether this is SYNCHRONIZING.
         * @return boolean; whether this is SYNCHRONIZING
         */
        public boolean isSycnhronizing()
        {
            return this == SYNCHRONIZING;
        }
        
        /**
         * Returns whether this is INDICATING.
         * @return boolean; whether this is INDICATING
         */
        public boolean isIndicating()
        {
            return this == INDICATING;
        }
        
        /**
         * Returns whether this is COOPERATING.
         * @return boolean; whether this is COOPERATING
         */
        public boolean isCooperating()
        {
            return this == COOPERATING;
        }
    }

}
