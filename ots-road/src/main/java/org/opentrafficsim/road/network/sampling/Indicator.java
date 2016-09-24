package org.opentrafficsim.road.network.sampling;

import org.djunits.unit.Unit;
import org.djunits.value.vdouble.scalar.DoubleScalar;

/**
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Sep 21, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 * @param <U>
 * @param <T>
 */
// TODO standard deviation, percentiles, min/max
// XXX think about using Tally and Persistent for some of the indicators. Maybe extend Indicator to TallyIndicator?
// XXX Persistent is already a time-weighed indicator that calculates mean, std, min, max, and confidence interval.
public interface Indicator<U extends Unit<U>, T extends DoubleScalar<U>>
{
    
    /**
     * Calculate value for given query.
     * @param query query
     * @return value for given query
     */
    T calculate(final Query query);
    
}
