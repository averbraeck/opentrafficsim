package org.opentrafficsim.core.geometry;

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
        OTSLine3D referenceLine =
                new OTSLine3D(new OTSPoint3D(5, 2.5), new OTSPoint3D(4.8, 2.5), new OTSPoint3D(4.6, 2.7), new OTSPoint3D(2.2,
                        2.7), new OTSPoint3D(2.2, 5));
        System.out.println(OTSGeometry.printCoordinates("#reference line: \nc1,0,0\n#", referenceLine, "\n    "));
        //OTSLine3D.debugOffsetLine = true;
        OTSLine3D left = referenceLine.offsetLine(2.0);
        System.out.println(OTSGeometry.printCoordinates("#left: \nc0,1,0\n#", left, "\n   "));
        OTSLine3D right = referenceLine.offsetLine(-2.0);
        System.out.println(OTSGeometry.printCoordinates("#right: \nc0,1,0\n#", right, "\n   "));
        */

        /*-
        OTSLine3D otsLine = new OTSLine3D(new OTSPoint3D(0, 0, 0), new OTSPoint3D(10, 5, 0), new OTSPoint3D(20, 0, 0));
        System.out.println(OTSGeometry.printCoordinates("#reference line: \nc1,0,0\n#", otsLine, "\n    "));
        OTSLine3D left = otsLine.offsetLine(2.0);
        System.out.println(OTSGeometry.printCoordinates("#left: \nc0,1,0\n#", left, "\n   "));
        OTSLine3D right = otsLine.offsetLine(-2.0);
        System.out.println(OTSGeometry.printCoordinates("#buffer: \nc0,1,0\n#", right, "\n   "));
        */

        /*-
        OTSLine3D referenceLine = new OTSLine3D(new OTSPoint3D(0, 0, 0), new OTSPoint3D(200, 100, 0), new OTSPoint3D(1000, 0, 0));
        System.out.println(OTSGeometry.printCoordinates("#reference line: \nc1,0,0\n#", referenceLine, "\n    "));
        OTSLine3D centerLine = referenceLine.offsetLine(-8, -5);
        System.out.println(OTSGeometry.printCoordinates("#center line: \nc0,1,0\n#", centerLine, "\n   "));
        for (int i = 1; i < centerLine.size(); i++)
        {
            OTSPoint3D from = centerLine.get(i - 1);
            OTSPoint3D to = centerLine.get(i);
            double angle = Math.atan2(to.y - from.y, to.x - from.x);
            System.out.println("#Direction in segment " + i + " is " + Math.toDegrees(angle));
        }
        OTSLine3D leftEdge = centerLine.offsetLine(1.5, 2);
        System.out.println(OTSGeometry.printCoordinates("#left edge: \nc0,0,1\n#", leftEdge, "\n   "));
        OTSLine3D rightEdge = centerLine.offsetLine(-1.5, -2);
        System.out.println(OTSGeometry.printCoordinates("#right edge: \nc0,0,1\n#", rightEdge, "\n   "));
        */

        /*-
        OTSLine3D reference =
                new OTSLine3D(new OTSPoint3D(0, 0, 0), new OTSPoint3D(20, 10, 0), new OTSPoint3D(21, 10, 0), new OTSPoint3D(22,
                        9.5, 0), new OTSPoint3D(30, 0, 0));
        System.out.println(OTSGeometry.printCoordinates("#reference: \nc1,0,0\n#", reference, "\n    "));
        OTSLine3D offset = reference.offsetLine(-3);
        System.out.println(OTSGeometry.printCoordinates("#offset: \nc0,1,0\n#", offset, "\n    "));
         */

        
        OTSPoint3D[] designLinePoints = new OTSPoint3D[8];
        double radius = 10;
        double angleStep = Math.PI / 1000;
        double initialAngle = Math.PI / 4;
        for (int i = 0; i < designLinePoints.length; i++)
        {
            double angle = initialAngle + i * angleStep;
            designLinePoints[i] = new OTSPoint3D(radius * Math.cos(angle), radius * Math.sin(angle) - radius, 0);
        }
        //OTSLine3D.debugOffsetLine = true;
        OTSLine3D reference = new OTSLine3D(designLinePoints);
        System.out.println(OTSGeometry.printCoordinates("#reference:\nc1,0,0\n#", reference, "\n    "));
        OTSLine3D centerLine = reference.offsetLine(5);
        System.out.println(OTSGeometry.printCoordinates("#center:\nc0,1,0\n#", centerLine, "\n    "));
        for (int i = 1; i < centerLine.size() - 1; i++)
        {
            double distance =
                    OTSPoint3D.distanceLineSegmentToPoint(centerLine.get(0), centerLine.get(centerLine.size() - 1),
                            centerLine.get(i));
            System.out.println("#distance of intermediate point " + i + " to overall line is " + distance);
        }
        OTSLine3D right = centerLine.offsetLine(-2);
        System.out.println(OTSGeometry.printCoordinates("#right:\nc0,0,1\n#", right, "\n    "));
        OTSLine3D left = centerLine.offsetLine(2);
        System.out.println(OTSGeometry.printCoordinates("#left:\nc0,0,1\n#", left, "\n    "));
        
    }

}
