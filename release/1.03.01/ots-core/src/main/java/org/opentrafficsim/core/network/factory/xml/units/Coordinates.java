package org.opentrafficsim.core.network.factory.xml.units;

import org.opentrafficsim.core.geometry.OTSPoint3D;

/**
 * <p>
 * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Jul 23, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public final class Coordinates
{
    /** Utility class. */
    private Coordinates()
    {
        // do not instantiate
    }

    /**
     * Parse a group of coordinates with (x,y) or (x,y,z).
     * @param cs String; the string containing the coordinates
     * @return OTSPoint3D[] containing the x,y or x,y,z values.
     */
    public static OTSPoint3D[] parseCoordinates(final String cs)
    {
        String cs1 = cs.replaceAll("\\s+", "");
        String c = cs1.replace(")(", ")split(");
        String[] cc = c.split("split");
        OTSPoint3D[] coords = new OTSPoint3D[cc.length];
        int i = 0;
        for (String coord : cc)
        {
            coords[i] = parseCoordinate(coord);
            i++;
        }
        return coords;

    }

    /**
     * Parse a coordinate with (x,y) or (x,y,z).
     * @param cs String; the string containing the coordinate
     * @return OTSPoint3D containing the x,y or x,y,z values
     */
    public static OTSPoint3D parseCoordinate(final String cs)
    {
        String c = cs.replace("(", "");
        c = c.replace(")", "");
        String[] cc = c.split(",");
        double x = Double.parseDouble(cc[0]);
        double y = Double.parseDouble(cc[1]);
        double z = cc.length > 2 ? Double.parseDouble(cc[2]) : 0.0;
        return new OTSPoint3D(x, y, z);
    }

}
