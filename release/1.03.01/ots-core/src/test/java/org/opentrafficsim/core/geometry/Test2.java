package org.opentrafficsim.core.geometry;

import org.opentrafficsim.core.network.NetworkException;

/**
 * <p>
 * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Dec 4, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public final class Test2
{

    /** This class should never be instantiated. */
    private Test2()
    {
        // This class cannot be instantiated.
    }

    /**
     * @param args String[]; args
     * @throws NetworkException on error
     * @throws OTSGeometryException on error
     */
    public static void main(final String[] args) throws NetworkException, OTSGeometryException
    {
        OTSLine3D reference = new OTSLine3D(new OTSPoint3D(5, -1, 1), new OTSPoint3D(5, -2, 1), new OTSPoint3D(4.9, -2.01, 1),
                new OTSPoint3D(5.1, -2.03, 1), new OTSPoint3D(5, -2.04, 1), new OTSPoint3D(5, -6, 1),
                new OTSPoint3D(4.9, -6.01, 1), new OTSPoint3D(5.1, -6.03, 1), new OTSPoint3D(5, -6.04, 1),
                new OTSPoint3D(5, -7.04, 1));

        System.out.println("#reference: " + reference.toString());
        OTSLine3D right = OTSOffsetLinePK.offsetLine(reference, -2);
        System.out.println("#right: " + right.toString());
        OTSLine3D left = reference.offsetLine(2);
        System.out.println("#left: " + left.toString());

        reference = new OTSLine3D(new OTSPoint3D(10, 0.5, 0), new OTSPoint3D(10, -2, 0), new OTSPoint3D(9.9, -2.01, 0),
                new OTSPoint3D(10.1, -2.03, 0), new OTSPoint3D(10, -2.04, 0), new OTSPoint3D(10, -6, 0),
                new OTSPoint3D(9.9, -6.01, 0), new OTSPoint3D(10.1, -6.03, 0), new OTSPoint3D(10, -6.04, 0),
                new OTSPoint3D(10, -8.54, 0));

        System.out.println("#reference: " + reference.toString());
        right = reference.offsetLine(-2);
        System.out.println("#right: " + right.toString());
        left = reference.offsetLine(2);
        System.out.println("#left: " + left.toString());

    }

}
