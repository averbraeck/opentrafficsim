package org.opentrafficsim.core.geometry;

/**
 * Test for another offsetLine problem. This one is for a reference line and offset that makes the offsetLinePK method hang
 * (i.e. never return).
 * <p>
 * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public final class Test3
{

    /** This class should never be instantiated. */
    private Test3()
    {
        // This class cannot be instantiated.
    }

    /**
     * Test for another offsetLine problem; program entry point.
     * @param args String[]; command line arguments (not used)
     * @throws OtsGeometryException should not happen
     */
    public static void main(final String[] args) throws OtsGeometryException
    {
        OtsOffsetLinePk.setDebugOffsetLine(true);
        OtsLine3d referenceLine = new OtsLine3d(new OtsPoint3d(-10.000, -10.000, 0.000), new OtsPoint3d(0.000, -10.000, 0.000),
                new OtsPoint3d(0.900, -9.960, 0.000), new OtsPoint3d(1.700, -9.800, 0.000),
                new OtsPoint3d(2.600, -9.700, 0.000), new OtsPoint3d(3.400, -9.400, 0.000),
                new OtsPoint3d(4.200, -9.100, 0.000), new OtsPoint3d(5.000, -8.700, 0.000),
                new OtsPoint3d(5.700, -8.200, 0.000), new OtsPoint3d(9.960, -0.900, 0.000),
                new OtsPoint3d(10.000, 0.000, 0.000), new OtsPoint3d(10.000, 10.000, 0.000));
        OtsLine3d offsetLine = referenceLine.offsetLine(7.5);
        System.out.println("offset line has " + offsetLine.size() + " points");
    }
}
