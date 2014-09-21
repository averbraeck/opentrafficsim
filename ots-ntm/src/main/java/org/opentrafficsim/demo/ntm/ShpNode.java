package org.opentrafficsim.demo.ntm;

import java.rmi.RemoteException;

import javax.media.j3d.Bounds;
import javax.vecmath.Point3d;

import nl.tudelft.simulation.dsol.animation.LocatableInterface;
import nl.tudelft.simulation.language.d3.BoundingBox;
import nl.tudelft.simulation.language.d3.DirectedPoint;

import com.vividsolutions.jts.geom.Point;

/**
 * A node contains the following information:
 * 
 * <pre>
 * the_geom class com.vividsolutions.jts.geom.Point POINT (190599 325650)
 * NODENR class java.lang.Long 18
 * NAME class java.lang.String 
 * X class java.lang.Double 190599.0
 * Y class java.lang.Double 325650.0
 * JUNCTYPE class java.lang.Long 0
 * RALANES class java.lang.Long 0
 * CALIBRATIO class java.lang.Double 0.0
 * KRUISPUNTT class java.lang.String 
 * TYPENR class java.lang.Long 0
 * VCRATIO class java.lang.Double 0.0
 * WVCRATIO class java.lang.Double 0.0
 * DELAY class java.lang.Double 0.0
 * BACKOFQUEU class java.lang.Double 0.0
 * LOS class java.lang.Double 0.0
 * CALCCYCLET class java.lang.Long 0
 * CONFLICTRA class java.lang.Double 0.0
 * USEDSIGNAL class java.lang.Long 0
 * VCRATIO_2 class java.lang.Double 0.0
 * WVCRATIO_2 class java.lang.Double 0.0
 * DELAY_2 class java.lang.Double 0.0
 * BACKOFQU_2 class java.lang.Double 0.0
 * LOS_2 class java.lang.Double 0.0
 * CALCCYCL_2 class java.lang.Long 0
 * CONFLICT_2 class java.lang.Double 0.0
 * USEDSIGN_2 class java.lang.Long 0
 * TOEGEVOEGD class java.lang.String 
 * KRUISPUNTL class java.lang.String 
 * NAMENR class java.lang.Long 0
 * PROGNOSEAA class java.lang.String 
 * AREATYPES class java.lang.String
 * </pre>
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version Sep 12, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.citg.tudelft.nl">Guus Tamminga</a>
 */
public class ShpNode implements LocatableInterface
{
    /** the_geom class com.vividsolutions.jts.geom.Point POINT (190599 325650) */
    private final Point point;

    /** NODENR class java.lang.Long 18 */
    private final long nr;

    /** X class java.lang.Double 190599.0 */
    private final double x;

    /** Y class java.lang.Double 325650.0 */
    private final double y;

    /**
     * @param point
     * @param nr
     * @param x
     * @param y
     */
    public ShpNode(Point point, long nr, double x, double y)
    {
        super();
        this.point = point;
        this.nr = nr;
        this.x = x;
        this.y = y;
    }

    /** {@inheritDoc} */
    @Override
    public DirectedPoint getLocation() throws RemoteException
    {
        return new DirectedPoint(new double[]{this.x, this.y, 0.0d});
    }

    /** {@inheritDoc} */
    @Override
    public Bounds getBounds() throws RemoteException
    {
        return new BoundingBox(new Point3d(-1.0d, -1.0d, 0.0d), new Point3d(1.0d, 1.0d, 0.0d));
    }

    /**
     * @return point
     */
    public Point getPoint()
    {
        return this.point;
    }

    /**
     * @return nr
     */
    public long getNr()
    {
        return this.nr;
    }

    /**
     * @return x
     */
    public double getX()
    {
        return this.x;
    }

    /**
     * @return y
     */
    public double getY()
    {
        return this.y;
    }

}
