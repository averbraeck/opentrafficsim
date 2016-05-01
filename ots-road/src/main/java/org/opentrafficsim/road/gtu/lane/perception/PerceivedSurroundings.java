package org.opentrafficsim.road.gtu.lane.perception;

import java.util.List;

/**
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Apr 29, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */

public interface PerceivedSurroundings
{

    /**
     * List of followers on the left lane, which is usually 0 or 1, but possibly more in case of an upstream merge with no
     * intermediate vehicle.
     * @return list of followers on the left lane.
     */
    List<HeadwayGTU> getFirstLeftFollowers();

    /**
     * List of followers on the right lane, which is usually 0 or 1, but possibly more in case of an upstream merge with no
     * intermediate vehicle.
     * @return list of followers on the right lane.
     */
    List<HeadwayGTU> getFirstRightFollowers();

    /**
     * List of leaders on the left lane, which is usually 0 or 1, but possibly more in case of a downstream split with no
     * intermediate vehicle.
     * @return list of followers on the left lane.
     */
    List<HeadwayGTU> getFirstLeftLeaders();

    /**
     * List of leaders on the right lane, which is usually 0 or 1, but possibly more in case of a downstream split with no
     * intermediate vehicle.
     * @return list of followers on the right lane.
     */
    List<HeadwayGTU> getFirstRightLeaders();
    
    
    
    /**
     * Whether there is an adjacent vehicle, i.e. with overlap, in the left lane.
     * @return whether there is an adjacent vehicle, i.e. with overlap, in the left lane.
     */
    boolean hasLeftAdjacentVehicle();
    
    /**
     * Whether there is an adjacent vehicle, i.e. with overlap, in the right lane.
     * @return whether there is an adjacent vehicle, i.e. with overlap, in the right lane.
     */
    boolean hasRightAdjacentVehicle();
    
    
    
    /**
     * 
     */

}
