package org.opentrafficsim.road.gtu.lane.perception;

import org.opentrafficsim.core.gtu.perception.Perception;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;

/**
 * Interface for perception in a lane-based model. The following information can be perceived:
 * <ul>
 * <li>maximum speed we can use at the current location; either time stamped or just the information</li>
 * <li>forward headway and first object (e.g., GTU) in front; either time stamped or just the information</li>
 * <li>backward headway and first object (e.g., GTU) behind; either time stamped or just the information</li>
 * <li>accessible adjacent lanes on the left or right; either time stamped or just the information</li>
 * <li>parallel objects (e.g., GTUa) on the left or right; either time stamped or just the information</li>
 * <li>Objects (e.g., GTUs) in parallel, in front and behind on the left or right neighboring lane, with their headway relative
 * to our GTU; either time stamped or just the information</li>
 * </ul>
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Jan 30, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public interface LanePerception extends Perception
{

    /**
     * @return the gtu for which this is the perception
     */
    LaneBasedGTU getGtu();
    
}
