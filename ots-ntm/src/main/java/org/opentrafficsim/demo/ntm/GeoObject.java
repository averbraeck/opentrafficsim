package org.opentrafficsim.demo.ntm;

import java.util.HashSet;
import java.util.Set;

import com.vividsolutions.jts.geom.Geometry;

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
public class GeoObject
{
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
    
    
    enum TrafficBehaviourType {NTM, CORDON, FLOW, CENTROID, ROAD};

    /** */
    private Geometry geometry;
    /** touching areas. */
    private final Set<GeoObject> touchingAreas = new HashSet<>();
    /** */
    private TrafficBehaviourType behaviourType;    
    
    /**
     * @param geometry 
     * @param behaviourType 
     */
    public GeoObject(final Geometry geometry, final TrafficBehaviourType behaviourType)
    {
        super();
        this.geometry = geometry;
        this.setBehaviourType(behaviourType);
    }


    /**
     * @return geometry
     */
    public final Geometry getGeometry()
    {
        return this.geometry;
    }
    
    /**
     * @return touchingAreas
     */
    public final Set<GeoObject> getTouchingAreas()
    {
        return this.touchingAreas;
    }


    /**
     * @return behaviourType.
     */
    public final TrafficBehaviourType getBehaviourType()
    {
        return behaviourType;
    }


    /**
     * @param behaviourType set behaviourType.
     */
    public final void setBehaviourType(final TrafficBehaviourType behaviourType)
    {
        this.behaviourType = behaviourType;
    }
}
