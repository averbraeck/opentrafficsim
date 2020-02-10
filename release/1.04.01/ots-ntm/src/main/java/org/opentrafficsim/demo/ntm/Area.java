package org.opentrafficsim.demo.ntm;

import java.awt.geom.Path2D;
import java.rmi.RemoteException;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.media.j3d.Bounds;
import javax.vecmath.Point3d;

import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;
import org.opentrafficsim.demo.ntm.NTMNode.TrafficBehaviourType;

import nl.tudelft.simulation.dsol.animation.Locatable;
import nl.tudelft.simulation.language.d3.BoundingBox;
import nl.tudelft.simulation.language.d3.DirectedPoint;

/**
 * The area contains the following information:
 * 
 * <pre>
 * the_geom class com.vividsolutions.jts.geom.MultiPolygon MULTIPOLYGON (((81816.4228569232, ...
 * AREANR class java.lang.Long 15127
 * NAME class java.lang.String 70 Oostduinen
 * CENTROIDNR class java.lang.Long 1
 * NAMENR class java.lang.Long 175
 * GEMEENTE_N class java.lang.String S Gravenhage
 * GEMEENTEVM class java.lang.String sGravenhage
 * GEBIEDSNAA class java.lang.String Studiegebied
 * REGIO class java.lang.String Den_Haag
 * MATCOMPRES class java.lang.String Scheveningen
 * DHB class java.lang.Double 70.0
 * PARKEERTAR class java.lang.String 
 * AREATAG class java.lang.String
 * </pre>
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author$,
 * initial version Sep 12, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.citg.tudelft.nl">Guus Tamminga</a>
 */
public class Area extends GeoObject implements Locatable
{

    /**
     * <p>
     * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * <p>
     * $LastChangedDate$, @version $Revision$, by $Author$,
     * initial version 10 Oct 2014 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://Hansvanlint.weblog.tudelft.nl">Hans van Lint</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.citg.tudelft.nl">Guus Tamminga</a>
     * @author <a href="http://www.citg.tudelft.nl">Yufei Yuan</a>
     */

    /** AREANR class java.lang.Long 15127 */
    private final String centroidNr;

    /** NAME class java.lang.String 70 Oostduinen */
    private final String name;

    /** GEMEENTEVM class java.lang.String sGravenhage */
    private final String gemeente;

    /** GEBIEDSNAA class java.lang.String Studiegebied */
    private final String gebied;

    /** REGIO class java.lang.String Den_Haag */
    private final String regio;

    /** DHB class java.lang.Double 70.0 */
    private final double dhb;

    /** Centroid as a Coordinate. */
    private final Coordinate centroid;

    /** */
    private Speed averageSpeed;

    /** */
    private Speed currentSpeed;

    /** */
    private Length roadLength;

    /** */
    private ParametersNTM parametersNTM;

    /** Polygon for drawing relative to centroid */
    private Set<Path2D> polygons = null;

    /** */
    private TrafficBehaviourType trafficBehaviourType;

    private double increaseDemandByFactor;

    /** The parameters for the NFD. */

    /**
     * <p>
     * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * <p>
     * $LastChangedDate$, @version $Revision$, by $Author$,
     * initial version 7 Oct 2014 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://Hansvanlint.weblog.tudelft.nl">Hans van Lint</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.citg.tudelft.nl">Guus Tamminga</a>
     * @author <a href="http://www.citg.tudelft.nl">Yufei Yuan</a>
     */

    /** The number of cars within this Area. */
    private double accumulatedCars;

    /**
     * @param geometry Geometry; the_geom class com.vividsolutions.jts.geom.MultiPolygon MULTIPOLYGON (((81816.4228569232 ...
     * @param centroidNr String;
     * @param name String; NAME class java.lang.String 70 Oostduinen
     * @param gemeente String; GEMEENTEVM class java.lang.String sGravenhage
     * @param gebied String; GEBIEDSNAA class java.lang.String Studiegebied
     * @param regio String; REGIO class java.lang.String Den_Haag
     * @param dhb double; DHB class java.lang.Double 70.0
     * @param centroid Coordinate; Centroid as a Point
     * @param trafficBehaviourType TrafficBehaviourType;
     * @param roadLength Length;
     * @param averageSpeed Speed;
     * @param increaseDemandByFactor double;
     */
    public Area(final Geometry geometry, final String centroidNr, final String name, final String gemeente, final String gebied,
            final String regio, final double dhb, final Coordinate centroid, final TrafficBehaviourType trafficBehaviourType,
            Length roadLength, Speed averageSpeed, double increaseDemandByFactor, ParametersNTM parametersNTM)
    {
        super(geometry);
        this.centroidNr = centroidNr;
        this.name = name;
        this.gemeente = gemeente;
        this.gebied = gebied;
        this.regio = regio;
        this.dhb = dhb;
        this.centroid = centroid;
        this.trafficBehaviourType = trafficBehaviourType;
        this.roadLength = roadLength;
        this.averageSpeed = averageSpeed;
        this.setIncreaseDemandByFactor(increaseDemandByFactor);
        this.setParametersNTM(parametersNTM);
    }

    /** {@inheritDoc} */
    @Override
    public DirectedPoint getLocation() throws RemoteException
    {
        Point c = this.getGeometry().getCentroid();
        return new DirectedPoint(new double[] {c.getX(), c.getY(), 0.0d});
    }

    /** {@inheritDoc} */
    @Override
    public Bounds getBounds() throws RemoteException
    {
        DirectedPoint d = getLocation();
        Envelope envelope = this.getGeometry().getEnvelopeInternal();
        return new BoundingBox(new Point3d(envelope.getMinX() - d.x, d.y - envelope.getMinY(), 0.0d),
                new Point3d(envelope.getMaxX() - d.x, d.y - envelope.getMaxY(), 0.0d));
    }

    /**
     * @return polygon
     * @throws RemoteException comment
     */
    public final Set<Path2D> getPolygons() throws RemoteException
    {
        // create the polygon if it did not exist before
        if (this.polygons == null)
        {
            double dx = this.getLocation().x;
            double dy = this.getLocation().y;
            this.polygons = new LinkedHashSet<Path2D>();
            for (int i = 0; i < this.getGeometry().getNumGeometries(); i++)
            {
                Path2D polygon = new Path2D.Double();
                Geometry g = this.getGeometry().getGeometryN(i);
                boolean start = true;
                for (Coordinate c : g.getCoordinates())
                {
                    if (start)
                    {
                        polygon.moveTo(c.x - dx, dy - c.y);
                        start = false;
                    }
                    else
                    {
                        polygon.lineTo(c.x - dx, dy - c.y);
                    }
                }
                polygon.closePath();
                this.polygons.add(polygon);
            }
        }
        return this.polygons;
    }

    /**
     * @return centroid
     */
    public final Coordinate getCentroid()
    {
        return this.centroid;
    }

    /**
     * @return nr
     */
    public final String getCentroidNr()
    {
        return this.centroidNr;
    }

    /**
     * @return name
     */
    public final String getName()
    {
        return this.name;
    }

    /**
     * @return gemeente
     */
    public final String getGemeente()
    {
        return this.gemeente;
    }

    /**
     * @return gebied
     */
    public final String getGebied()
    {
        return this.gebied;
    }

    /**
     * @return regio
     */
    public final String getRegio()
    {
        return this.regio;
    }

    /**
     * @return dhb
     */
    public final double getDhb()
    {
        return this.dhb;
    }

    /**
     * @return accumulatedCars.
     */
    public final double getAccumulatedCars()
    {
        return this.accumulatedCars;
    }

    /**
     * @param d double; set accumulatedCars.
     */
    public final void setAccumulatedCars(final double d)
    {
        this.accumulatedCars = d;
    }

    /**
     * @return areaType.
     */
    public final TrafficBehaviourType getTrafficBehaviourType()
    {
        return this.trafficBehaviourType;
    }

    /**
     * @param areaType TrafficBehaviourType; set areaType.
     */
    public final void setTrafficBehaviourType(final TrafficBehaviourType areaType)
    {
        this.trafficBehaviourType = areaType;
    }

    /**
     * @return roadLength.
     */
    public Length getRoadLength()
    {
        return this.roadLength;
    }

    /**
     * @param roadLength Length; set roadLength.
     */
    public void setRoadLength(Length roadLength)
    {
        this.roadLength = roadLength;
    }

    /**
     * @param rel Length;
     */
    public void addRoadLength(Length rel)
    {
        this.roadLength = this.roadLength.plus(rel);
    }

    /**
     * @return averageSpeed.
     */
    public Speed getAverageSpeed()
    {
        return this.averageSpeed;
    }

    /**
     * @param averageSpeed Speed; set averageSpeed.
     */
    public void setAverageSpeed(Speed averageSpeed)
    {
        this.averageSpeed = averageSpeed;
    }

    /**
     * @return currentSpeed.
     */
    public Speed getCurrentSpeed()
    {
        return this.currentSpeed;
    }

    /**
     * @param currentSpeed Speed; set currentSpeed.
     */
    public void setCurrentSpeed(Speed currentSpeed)
    {
        this.currentSpeed = currentSpeed;
    }

    /**
     * @return increaseDemandByFactor.
     */
    public double getIncreaseDemandByFactor()
    {
        return this.increaseDemandByFactor;
    }

    /**
     * @param increaseDemandByFactor double; set increaseDemandByFactor.
     */
    public void setIncreaseDemandByFactor(double increaseDemandByFactor)
    {
        this.increaseDemandByFactor = increaseDemandByFactor;
    }

    /**
     * @return parametersNTM.
     */
    public ParametersNTM getParametersNTM()
    {
        return this.parametersNTM;
    }

    /**
     * @param parametersNTM ParametersNTM; set parametersNTM.
     */
    public void setParametersNTM(ParametersNTM parametersNTM)
    {
        this.parametersNTM = parametersNTM;
    }

}
