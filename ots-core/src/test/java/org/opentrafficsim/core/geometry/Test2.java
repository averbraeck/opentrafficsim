package org.opentrafficsim.core.geometry;

import org.opentrafficsim.core.network.NetworkException;

/**
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
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
     * @throws OtsGeometryException on error
     */
    public static void main(final String[] args) throws NetworkException, OtsGeometryException
    {
        OtsLine3D reference = new OtsLine3D(new OtsPoint3D(5, -1, 1), new OtsPoint3D(5, -2, 1), new OtsPoint3D(4.9, -2.01, 1),
                new OtsPoint3D(5.1, -2.03, 1), new OtsPoint3D(5, -2.04, 1), new OtsPoint3D(5, -6, 1),
                new OtsPoint3D(4.9, -6.01, 1), new OtsPoint3D(5.1, -6.03, 1), new OtsPoint3D(5, -6.04, 1),
                new OtsPoint3D(5, -7.04, 1));

        System.out.println("#reference: " + reference.toString());
        OtsLine3D right = OtsOffsetLinePK.offsetLine(reference, -2);
        System.out.println("#right: " + right.toString());
        OtsLine3D left = reference.offsetLine(2);
        System.out.println("#left: " + left.toString());

        reference = new OtsLine3D(new OtsPoint3D(10, 0.5, 0), new OtsPoint3D(10, -2, 0), new OtsPoint3D(9.9, -2.01, 0),
                new OtsPoint3D(10.1, -2.03, 0), new OtsPoint3D(10, -2.04, 0), new OtsPoint3D(10, -6, 0),
                new OtsPoint3D(9.9, -6.01, 0), new OtsPoint3D(10.1, -6.03, 0), new OtsPoint3D(10, -6.04, 0),
                new OtsPoint3D(10, -8.54, 0));

        System.out.println("#reference: " + reference.toString());
        right = reference.offsetLine(-2);
        System.out.println("#right: " + right.toString());
        left = reference.offsetLine(2);
        System.out.println("#left: " + left.toString());

    }

}
