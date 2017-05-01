/**
 * 
 */
package org.opentrafficsim.water.demand;

import org.geotools.geometry.jts.JTSFactoryFinder;
import org.jaitools.jts.CoordinateSequence2D;
import org.opentrafficsim.water.AbstractNamed;
import org.opentrafficsim.water.RepeatableRandomStream;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;

import nl.tudelft.simulation.jstats.streams.StreamInterface;

/**
 * A region that generates demand. Examples are municipalities or (in Europe), NUTS regions.
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * </p>
 * <p>
 * Based on software from the IDVV project, which is Copyright (c) 2013 Rijkswaterstaat - Dienst Water, Verkeer en Leefomgeving
 * and licensed without restrictions to Delft University of Technology, including the right to sub-license sources and derived
 * products to third parties.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Nov 6, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class Region extends AbstractNamed implements Comparable<Region>
{
    /** */
    private static final long serialVersionUID = 1L;

    /** the region to whic this region belongs, e.g. Municipality - Province - Country. */
    private Region superRegion;

    /** the number of inhabitants in the region. */
    private double inhabitants;

    /** the number of jobs in the region. */
    private double jobs;

    /** the center of the region. */
    private Coordinate center;

    /** the shape of this region. */
    private MultiPolygon area;

    /** export ton/teu factor. */
    private double exportTonTEU;

    /** import ton/teu factor. */
    private double importTonTEU;

    /** export empty factor. */
    private double exportEmptyFactor;

    /** import empty factor. */
    private double importEmptyFactor;

    /** current Import TEU per year. */
    private double currentImportTEUperYear = 0;

    /** current Export TEU per year. */
    private double currentExportTEUperYear = 0;

    /** repeatable stream. */
    private static StreamInterface randomStream = null;

    /**
     * @param name the name of the region
     * @param superRegion the region of which this region is part; can be null
     * @param center the coordinate that denotes the center of the region
     */
    public Region(final String name, final Region superRegion, final Coordinate center)
    {
        super(name);
        this.superRegion = superRegion;
        this.center = center;

        randomStream = RepeatableRandomStream.create(this.getName());
    }

    /**
     * @return the superRegion
     */
    public final Region getSuperRegion()
    {
        return this.superRegion;
    }

    /**
     * @param jobs the jobs to set
     */
    public final void setJobs(final double jobs)
    {
        this.jobs = jobs;
    }

    /**
     * @return the jobs
     */
    public final double getJobs()
    {
        return (this.jobs > 0) ? this.jobs : 1;
    }

    /**
     * @return the center
     */
    public final Coordinate getCenter()
    {
        return this.center;
    }

    /**
     * @return the inhabitants
     */
    public final double getInhabitants()
    {
        return this.inhabitants;
    }

    /**
     * @param inhabitants set inhabitants
     */
    protected final void setInhabitants(final double inhabitants)
    {
        this.inhabitants = inhabitants;
    }

    /**
     * @return the exportTonTEU
     */
    public final double getExportTonTEU()
    {
        return this.exportTonTEU;
    }

    /**
     * @param exportTonTEU the exportTonTEU to set
     */
    public final void setExportTonTEU(final double exportTonTEU)
    {
        this.exportTonTEU = exportTonTEU;
    }

    /**
     * @return the importTonTEU
     */
    public final double getImportTonTEU()
    {
        return this.importTonTEU;
    }

    /**
     * @param importTonTEU the importTonTEU to set
     */
    public final void setImportTonTEU(final double importTonTEU)
    {
        this.importTonTEU = importTonTEU;
    }

    /**
     * @return the exportEmptyFactor
     */
    public final double getExportEmptyFactor()
    {
        return this.exportEmptyFactor;
    }

    /**
     * @param exportEmptyFactor the exportEmptyFactor to set
     */
    public final void setExportEmptyFactor(final double exportEmptyFactor)
    {
        this.exportEmptyFactor = exportEmptyFactor;
    }

    /**
     * @return the importEmptyFactor
     */
    public final double getImportEmptyFactor()
    {
        return this.importEmptyFactor;
    }

    /**
     * @param importEmptyFactor the importEmptyFactor to set
     */
    public final void setImportEmptyFactor(final double importEmptyFactor)
    {
        this.importEmptyFactor = importEmptyFactor;
    }

    /**
     * @return the currentExportTEUperYear
     */
    public final double getCurrentExportTEUperYear()
    {
        return this.currentExportTEUperYear;
    }

    /**
     * @param newExportTEUPerYear update currentExportTEUperYear
     */
    public final void setCurrentExportTEUperYear(final double newExportTEUPerYear)
    {
        this.currentExportTEUperYear = newExportTEUPerYear;
    }

    /**
     * @param newExportTEUPerYear add to currentExportTEUperYear
     */
    public final void addCurrentExportTEUperYear(final double newExportTEUPerYear)
    {
        this.currentExportTEUperYear += newExportTEUPerYear;
    }

    /**
     * @return the currentExportTEUperYear
     */
    public final double getCurrentImportTEUperYear()
    {
        return this.currentImportTEUperYear;
    }

    /**
     * @param newImportTEUPerYear update currentExportTEUperYear
     */
    public final void setCurrentImportTEUperYear(final double newImportTEUPerYear)
    {
        this.currentImportTEUperYear = newImportTEUPerYear;
    }

    /**
     * @param newImportTEUPerYear add to currentExportTEUperYear
     */
    public final void addCurrentImportTEUperYear(final double newImportTEUPerYear)
    {
        this.currentImportTEUperYear += newImportTEUPerYear;
    }

    /**
     * @return the area
     */
    public final MultiPolygon getArea()
    {
        return this.area;
    }

    /**
     * @param area the area to set
     */
    public final void setArea(final MultiPolygon area)
    {
        this.area = area;
    }

    /**
     * @return random point within a region
     */
    public final Coordinate getNextLocationInArea()
    {
        Envelope bounds = this.area.getEnvelopeInternal();

        GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();

        Point p = new Point(new CoordinateSequence2D(0, 0), geometryFactory);
        while (!isInArea(p))
        {
            double latitude = bounds.getMinX() + (randomStream.nextDouble() * ((bounds.getMaxX() - bounds.getMinX())));
            double longitude = bounds.getMinY() + (randomStream.nextDouble() * ((bounds.getMaxY() - bounds.getMinY())));

            p = new Point(new CoordinateSequence2D(latitude, longitude), geometryFactory);
        }

        return p.getCoordinate();
    }

    /**
     * @param p point
     * @return whether point is within region bounds
     */
    public final boolean isInArea(final Point p)
    {
        return this.area.contains(p);
    }

    /** {@inheritDoc} */
    @Override
    public final int compareTo(final Region other)
    {
        return this.getName().compareTo(other.getName());
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public String toString()
    {
        return "Region " + this.getName();
    }

}
