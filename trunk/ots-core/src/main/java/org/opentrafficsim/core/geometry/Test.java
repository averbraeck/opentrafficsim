package org.opentrafficsim.core.geometry;

import java.util.ArrayList;
import java.util.List;

import org.opentrafficsim.core.network.NetworkException;

/**
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Nov 9, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public final class Test
{
    /** */
    private Test()
    {
    }

    /**
     * @param args args
     * @throws NetworkException on error
     * @throws OTSGeometryException on error
     */
    public static void main(final String[] args) throws NetworkException, OTSGeometryException
    {
        /*-
        List<OTSPoint3D> line = new ArrayList<>();
        line.add(new OTSPoint3D(5, 2.5));
        line.add(new OTSPoint3D(4.8, 2.5));
        line.add(new OTSPoint3D(4.6, 2.7));
        line.add(new OTSPoint3D(2.2, 2.7));
        line.add(new OTSPoint3D(2.2, 5));
        OTSLine3D otsLine = new OTSLine3D(line);
        System.out.println(OTSGeometry.printCoordinates("#reference line: \nc1,0,0\n#", otsLine, "\n    "));
        OTSLine3D left = OTSBuffering.offsetGeometry(otsLine, 2.0);
        System.out.println(OTSGeometry.printCoordinates("#left: \nc0,1,0\n#", left, "\n   "));
        OTSLine3D right = OTSBuffering.offsetGeometry(otsLine, -2.0);
        System.out.println(OTSGeometry.printCoordinates("#right: \nc0,1,0\n#", right, "\n   "));
        */
        
        /*-
        OTSLine3D otsLine = new OTSLine3D(new OTSPoint3D(0, 0, 0), new OTSPoint3D(10, 5, 0), new OTSPoint3D(20, 0, 0));
        System.out.println(OTSGeometry.printCoordinates("#reference line: \nc1,0,0\n#", otsLine, "\n    "));
        OTSLine3D left = OTSBuffering.offsetGeometry(otsLine, 2.0);
        System.out.println(OTSGeometry.printCoordinates("#left: \nc0,1,0\n#", left, "\n   "));
        OTSLine3D right = OTSBuffering.offsetGeometry(otsLine, -2.0);
        System.out.println(OTSGeometry.printCoordinates("#buffer: \nc0,1,0\n#", right, "\n   "));
        */

        /*-
        OTSLine3D referenceLine = new OTSLine3D(new OTSPoint3D(0, 0, 0), new OTSPoint3D(200, 100, 0), new OTSPoint3D(1000, 0, 0));
        System.out.println(OTSGeometry.printCoordinates("#reference line: \nc1,0,0\n#", referenceLine, "\n    "));
        OTSLine3D centerLine = OTSBuffering.offsetLine(referenceLine, -8, -5);
        System.out.println(OTSGeometry.printCoordinates("#center line: \nc0,1,0\n#", centerLine, "\n   "));
        for (int i = 1; i < centerLine.size(); i++)
        {
            OTSPoint3D from = centerLine.get(i - 1);
            OTSPoint3D to = centerLine.get(i);
            double angle = Math.atan2(to.y - from.y, to.x - from.x);
            System.out.println("#Direction in segment " + i + " is " + Math.toDegrees(angle));
        }
        // OTSLine3D leftEdge = OTSBuffering.offsetLine(centerLine, 1.5, 2);
        // System.out.println(OTSGeometry.printCoordinates("#left edge: \nc0,0,1\n#", leftEdge, "\n   "));
        OTSLine3D rightEdge = OTSBuffering.offsetLine(centerLine, -1.5, -2);
        System.out.println(OTSGeometry.printCoordinates("#right edge: \nc0,0,1\n#", rightEdge, "\n   "));
        */

        
        OTSLine3D reference =
                new OTSLine3D(new OTSPoint3D(0, 0, 0), new OTSPoint3D(20, 10, 0), new OTSPoint3D(21, 10, 0), new OTSPoint3D(22,
                        9.5, 0), new OTSPoint3D(30, 0, 0));
        System.out.println(OTSGeometry.printCoordinates("#reference: \nc1,0,0\n#", reference, "\n    "));
        OTSLine3D offset = OTSBuffering.offsetGeometry(reference, -3);
        System.out.println(OTSGeometry.printCoordinates("#offset: \nc0,1,0\n#", offset, "\n    "));
        
    }

}
