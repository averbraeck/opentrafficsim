package org.opentrafficsim.demo.ntm;

import java.util.HashSet;
import java.util.Set;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

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
 * Copyright (c) 2002-2014 Delft University of Technology, Jaffalaan 5, 2628 BX Delft, the Netherlands. All rights
 * reserved.
 * <p>
 * See for project information <a href="http://www.simulation.tudelft.nl/"> www.simulation.tudelft.nl</a>.
 * <p>
 * The OpenTrafficSim project is distributed under the following BSD-style license:<br>
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 * <ul>
 * <li>Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 * disclaimer.</li>
 * <li>Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
 * following disclaimer in the documentation and/or other materials provided with the distribution.</li>
 * <li>Neither the name of Delft University of Technology, nor the names of its contributors may be used to endorse or
 * promote products derived from this software without specific prior written permission.</li>
 * </ul>
 * This software is provided by the copyright holders and contributors "as is" and any express or implied warranties,
 * including, but not limited to, the implied warranties of merchantability and fitness for a particular purpose are
 * disclaimed. In no event shall the copyright holder or contributors be liable for any direct, indirect, incidental,
 * special, exemplary, or consequential damages (including, but not limited to, procurement of substitute goods or
 * services; loss of use, data, or profits; or business interruption) however caused and on any theory of liability,
 * whether in contract, strict liability, or tort (including negligence or otherwise) arising in any way out of the use
 * of this software, even if advised of the possibility of such damage.
 * @version Sep 12, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.citg.tudelft.nl">Guus Tamminga</a>
 */
public class Area
{
    /** the_geom class com.vividsolutions.jts.geom.MultiPolygon MULTIPOLYGON (((81816.4228569232 ... */
    private final Geometry geometry;

    /** AREANR class java.lang.Long 15127 */
    private final long nr;

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

    /** Centroid as a Point */
    private final Point centroid;

    /** touching areas */
    private final Set<Area> touchingAreas = new HashSet<>();

    /**
     * @param geometry the_geom class com.vividsolutions.jts.geom.MultiPolygon MULTIPOLYGON (((81816.4228569232 ...
     * @param nr AREANR class java.lang.Long 15127
     * @param name NAME class java.lang.String 70 Oostduinen
     * @param gemeente GEMEENTEVM class java.lang.String sGravenhage
     * @param gebied GEBIEDSNAA class java.lang.String Studiegebied
     * @param regio REGIO class java.lang.String Den_Haag
     * @param dhb DHB class java.lang.Double 70.0
     * @param centroid Centroid as a Point
     */
    public Area(final Geometry geometry, final long nr, final String name, final String gemeente, final String gebied,
            final String regio, final double dhb, final Point centroid)
    {
        super();
        this.geometry = geometry;
        this.nr = nr;
        this.name = name;
        this.gemeente = gemeente;
        this.gebied = gebied;
        this.regio = regio;
        this.dhb = dhb;
        this.centroid = centroid;
    }

    /**
     * @return centroid
     */
    public Point getCentroid()
    {
        return this.centroid;
    }

    /**
     * @return nr
     */
    public long getNr()
    {
        return this.nr;
    }

    /**
     * @return name
     */
    public String getName()
    {
        return this.name;
    }

    /**
     * @return gemeente
     */
    public String getGemeente()
    {
        return this.gemeente;
    }

    /**
     * @return gebied
     */
    public String getGebied()
    {
        return this.gebied;
    }

    /**
     * @return regio
     */
    public String getRegio()
    {
        return this.regio;
    }

    /**
     * @return dhb
     */
    public double getDhb()
    {
        return this.dhb;
    }

    /**
     * @return geometry
     */
    public Geometry getGeometry()
    {
        return this.geometry;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return "Area [nr=" + this.nr + ", name=" + this.name + ", gemeente=" + this.gemeente + "]";
    }

    /**
     * @return touchingAreas
     */
    public Set<Area> getTouchingAreas()
    {
        return this.touchingAreas;
    }

}
