package org.opentrafficsim.road.gtu.lane.perception;

import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.perception.Perception;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.gtu.lane.perception.mental.Mental;

/**
 * Interface for perception in a lane-based model. The following information can be perceived:
 * <p>
 * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Jan 30, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public interface LanePerception extends Perception<LaneBasedGTU>
{

    /** {@inheritDoc} */
    @Override
    LaneBasedGTU getGtu() throws GTUException;

    /**
     * @return lane structure to perform perception
     * @throws ParameterException if parameter is not defined
     */
    LaneStructure getLaneStructure() throws ParameterException;

    /**
     * Returns the mental module of perception.
     * @return Mental; mental module of perception, may be {@code null} if not used
     */
    Mental getMental();

}
