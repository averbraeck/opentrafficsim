package org.opentrafficsim.core.geometry;

/**
 * <p>
 * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public final class IntersectionProblem
{
    /**
     * Cannot be instantiated.
     */
    private IntersectionProblem()
    {
        // Not reached
    }

    /**
     * Computed intersection is way off.
     * @param args String[]; command line arguments (not used)
     * @throws OtsGeometryException ...
     */
    public static void main(final String[] args) throws OtsGeometryException
    {
        final OtsLine3d lineA = new OtsLine3d(new OtsPoint3d(426330.98352154676, 4581381.654110057),
                new OtsPoint3d(426330.99232492544, 4581381.6609363835));
        // final OtsLine3d lineB =
        // new OtsLine3d(new OtsPoint3d(426330.9891418501, 4581381.65846816), new OtsPoint3d(426330.3763622642,
        // 4581382.44872203));
        final OtsLine3d lineB = new OtsLine3d(new OtsPoint3d(426330.98915, 4581381.65846816),
                new OtsPoint3d(426330.3763622642, 4581382.44872203));
        OtsPoint3d intersection =
                OtsPoint3d.intersectionOfLines(lineA.getFirst(), lineA.getLast(), lineB.getFirst(), lineB.getLast());
        System.out.println(
                "Determinant values are " + (lineA.getFirst().x - lineA.getLast().x) * (lineB.getFirst().y - lineB.getLast().y)
                        + " - " + (lineA.getFirst().y - lineA.getLast().y) * (lineB.getFirst().x - lineB.getLast().x));
        System.out.println("Determinant values are (" + lineA.getFirst().x + " - " + lineA.getLast().x + ") * ("
                + lineB.getFirst().y + " - " + lineB.getLast().y + ") \n\t\t- (" + lineA.getFirst().y + " - "
                + lineA.getLast().y + ") * (" + lineB.getFirst().x + " - " + lineB.getLast().x + "0");

        System.out.println("intersection of " + lineA + " and " + lineB + " is at (" + intersection.x + "," + intersection.y
                + "," + intersection.z + ")");
        intersection =
                OtsPoint3d.intersectionOfLineSegments(lineA.getFirst(), lineA.getLast(), lineB.getFirst(), lineB.getLast());
        System.out.println("intersection of " + lineA + " and " + lineB + " is at (" + intersection.x + "," + intersection.y
                + "," + intersection.z + ")");
    }
}
