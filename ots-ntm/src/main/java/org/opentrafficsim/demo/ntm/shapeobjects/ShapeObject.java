package org.opentrafficsim.demo.ntm.shapeobjects;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.vividsolutions.jts.geom.Geometry;

/**
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version 13 Nov 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://Hansvanlint.weblog.tudelft.nl">Hans van Lint</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.citg.tudelft.nl">Guus Tamminga</a>
 * @author <a href="http://www.citg.tudelft.nl">Yufei Yuan</a>
 */
public class ShapeObject
{
    /** */
    private Geometry geometry;
    /** */
    private ArrayList<String> values;
    
    /**
     * @param theGeom
     * @param values
     */
    public ShapeObject(Geometry geometry, ArrayList<String> values)
    {
        super();
        this.geometry = geometry;
        this.values = values;
    }
    /**
     * @return theGeom.
     */
    public Geometry getGeometry()
    {
        return this.geometry;
    }
    /**
     * @param theGeom set theGeom.
     */
    public void setGeometry(Geometry geometry)
    {
        this.geometry = geometry;
    }
    /**
     * @return table.
     */
    public ArrayList<String> getValues()
    {
        return this.values;
    }
    /**
     * @param table set table.
     */
    public void setValues(ArrayList<String> values)
    {
        this.values = values;
    }
    
    
}
