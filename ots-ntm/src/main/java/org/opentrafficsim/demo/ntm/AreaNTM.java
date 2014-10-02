package org.opentrafficsim.demo.ntm;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

/**
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version 26 Sep 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://Hansvanlint.weblog.tudelft.nl">Hans van Lint</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.citg.tudelft.nl">Guus Tamminga</a>
 * @author <a href="http://www.citg.tudelft.nl">Yufei Yuan</a>
 */
public class AreaNTM extends Area

{

    /** The movement of traffic between cells. */
    private CellBehaviourNTM cellBehaviourNTM;

    /** The number of cars within this Area. */
    private double accumulatedCars;



    /**
     * @param geometry
     * @param nr
     * @param name
     * @param gemeente
     * @param gebied
     * @param regio
     * @param dhb
     * @param centroid
     */
    public AreaNTM(final Geometry geometry, String nr, final String name, final String gemeente, final String gebied,
            final String regio, double dhb, Point centroid)
    {
        super(geometry, nr, name, gemeente, gebied, regio, dhb, centroid);
    }

    /**
     * @param geometry
     * @param nr
     * @param name
     * @param gemeente
     * @param gebied
     * @param regio
     * @param dhb
     * @param centroid
     * @param parametersNTM
     */
    public AreaNTM(final Geometry geometry, String nr, final String name, final String gemeente, final String gebied,
            final String regio, double dhb, Point centroid, final ParametersNTM parametersNTM)
    {
        super(geometry, nr, name, gemeente, gebied, regio, dhb, centroid);
        this.setCellBehaviourNTM(new CellBehaviourNTM(parametersNTM));
    }

    /**
     * @return cellBehaviourNTM.
     */
    public final CellBehaviourNTM getCellBehaviourNTM()
    {
        return this.cellBehaviourNTM;
    }

    /**
     * @param cellBehaviourNTM set cellBehaviourNTM.
     */
    public final void setCellBehaviourNTM(final CellBehaviourNTM cellBehaviourNTM)
    {
        this.cellBehaviourNTM = cellBehaviourNTM;
    }

    /**
     * @return accumulatedCars.
     */
    public final double getAccumulatedCars()
    {
        return this.accumulatedCars;
    }

    /**
     * @param d set accumulatedCars.
     */
    public final void setAccumulatedCars(final double d)
    {
        this.accumulatedCars = d;
    }



}
