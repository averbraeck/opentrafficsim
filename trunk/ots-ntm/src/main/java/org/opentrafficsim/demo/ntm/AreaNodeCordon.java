package org.opentrafficsim.demo.ntm;

import org.opentrafficsim.core.network.AbstractNode;

import com.vividsolutions.jts.geom.Point;

/**
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version 7 Oct 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://Hansvanlint.weblog.tudelft.nl">Hans van Lint</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.citg.tudelft.nl">Guus Tamminga</a>
 * @author <a href="http://www.citg.tudelft.nl">Yufei Yuan</a>
 */
public class AreaNodeCordon extends BoundedNode
{

    /**
     * @param centroid
     * @param nr
     * @param area
     * @param behaviourType
     */
    public AreaNodeCordon(Point centroid, String nr, Area area, TrafficBehaviourType behaviourType)
    {
        super(centroid, nr, area, behaviourType);
    }


}
