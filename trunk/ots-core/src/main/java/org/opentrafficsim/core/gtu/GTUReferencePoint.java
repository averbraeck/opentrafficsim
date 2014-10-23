package org.opentrafficsim.core.gtu;

import javax.vecmath.Point3d;

/**
 * The relative x, y and z from the Location returned by the LocatableInterface. Positive x is taken to be in the driving or
 * walking direction. Positive y is left when facing forward. Positive z is up. x, y and z are relative to the vehicle
 * coordinate system.
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version Oct 22, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://Hansvanlint.weblog.tudelft.nl">Hans van Lint</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.citg.tudelft.nl">Guus Tamminga</a>
 * @author <a href="http://www.citg.tudelft.nl">Yufei Yuan</a>
 */
public class GTUReferencePoint extends Point3d
{
    /** */
    private static final long serialVersionUID = 20141022L;

    /**
     * @param x x.
     * @param y y.
     * @param z z.
     */
    public GTUReferencePoint(final double x, final double y, final double z)
    {
        super(x, y, z);
    }

    /**
     * @param array double array with x, y and z.
     */
    public GTUReferencePoint(final double[] array)
    {
        super(array);
    }
    
}
