package org.opentrafficsim.core.units.distributions;

import org.djunits.value.vdouble.scalar.DOUBLE_SCALAR;

/**
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Sep 3, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public interface OTS_DOUBLE_DIST extends DOUBLE_SCALAR
{
    /** the easy access interface to the double scalar continuous distributions. */
    public interface ContinuousDistScalar extends ContinuousDistDoubleScalar
    {
        // no additional features.
    }

    /** the easy access interface to the double scalar discrete distributions. */
    public interface DiscreteDistScalar extends DiscreteDistDoubleScalar
    {
        // no additional features.
    }
}
