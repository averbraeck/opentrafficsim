package org.opentrafficsim.road.gtu.lane.perception;

import java.util.TreeSet;

import org.opentrafficsim.road.gtu.lane.perception.headway.Headway;

/**
 * Simple class implementing a SortedSet. This is mainly for backwards compatibility. Methods that determine the elements 1-by-1
 * are much preferred for efficiency.
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 19 feb. 2018 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 * @param <H> headway type
 */
public class SortedSetPerceptionIterable<H extends Headway> extends TreeSet<H> implements PerceptionIterable<H>
{

    /** */
    private static final long serialVersionUID = 20180219L;
    
}
