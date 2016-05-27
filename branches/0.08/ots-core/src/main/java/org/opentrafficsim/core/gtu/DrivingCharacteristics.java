package org.opentrafficsim.core.gtu;

import java.io.Serializable;

import org.djunits.value.vdouble.scalar.Frequency;

/**
 * Driving characteristics of the driver of a GTU. Sets the parameters for models that the TacticalPlanner (and other planners)
 * and perception can use. An example is: how often does a driver observe the environment? How far does a driver look forward or
 * backward for other GTUs? Etc.
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Nov 28, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public interface DrivingCharacteristics extends Serializable
{
    /** @return the average update frequency of the driver to update the Perception state. */
    Frequency getAveragePerceptionUpdateFrequency();
}
