package org.opentrafficsim.water.role;

import org.opentrafficsim.water.AbstractNamedLocated;

import org.opentrafficsim.core.geometry.DirectedPoint;

/**
 * <p>
 * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Dec 22, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class Company extends AbstractNamedLocated
{

    /** code for the company. */
    final String code;

    /**
     * @param code String; code
     * @param name String; name
     * @param location DirectedPoint; location
     */
    public Company(final String code, final String name, final DirectedPoint location)
    {
        super(name, location);
        this.code = code;
    }

    /**
     * @return code
     */
    public final String getCode()
    {
        return this.code;
    }

}
