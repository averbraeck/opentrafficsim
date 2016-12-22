package org.opentrafficsim.road.gtu.lane.perception;

import java.io.Serializable;

import org.opentrafficsim.core.gtu.GTU;

/**
 * Defines a type of object that can be observed in perception. 
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version May 4, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 * @param <T> the type of the perceived object 
 */
public class PerceivedObjectType<T> implements Serializable
{
    /** */
    private static final long serialVersionUID = 20160505L;

    private final String id;
    
    public static final PerceivedObjectType<GTU> PERCEIVED_GTU = new PerceivedObjectType<>("gtu");
    
    /**
     * @param id the id to 
     */
    public PerceivedObjectType(final String id)
    {
        this.id = id;
    }

    /**
     * @return the id of the perceivable object type. 
     */
    public final String getId()
    {
        return this.id;
    }

    /** {@inheritDoc} */
    @SuppressWarnings("checkstyle:designforextension")
    @Override
    public String toString()
    {
        return "PerceivedObjectType [id=" + this.id + "]";
    }

}

