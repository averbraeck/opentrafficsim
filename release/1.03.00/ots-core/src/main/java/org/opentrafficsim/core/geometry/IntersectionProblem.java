package org.opentrafficsim.core.geometry;

/**
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Aug 16, 2018 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
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
     * @throws OTSGeometryException ...
     */
    public static void main(final String[] args) throws OTSGeometryException
    {
        final OTSLine3D lineA = new OTSLine3D(new OTSPoint3D(426330.98352154676, 4581381.654110057),
                new OTSPoint3D(426330.99232492544, 4581381.6609363835));
        // final OTSLine3D lineB =
        // new OTSLine3D(new OTSPoint3D(426330.9891418501, 4581381.65846816), new OTSPoint3D(426330.3763622642,
        // 4581382.44872203));
        final OTSLine3D lineB = new OTSLine3D(new OTSPoint3D(426330.98915, 4581381.65846816),
                new OTSPoint3D(426330.3763622642, 4581382.44872203));
        OTSPoint3D intersection =
                OTSPoint3D.intersectionOfLines(lineA.getFirst(), lineA.getLast(), lineB.getFirst(), lineB.getLast());
        System.out.println(
                "Determinant values are " + (lineA.getFirst().x - lineA.getLast().x) * (lineB.getFirst().y - lineB.getLast().y)
                        + " - " + (lineA.getFirst().y - lineA.getLast().y) * (lineB.getFirst().x - lineB.getLast().x));
        System.out.println("Determinant values are (" + lineA.getFirst().x + " - " + lineA.getLast().x + ") * ("
                + lineB.getFirst().y + " - " + lineB.getLast().y + ") \n\t\t- (" + lineA.getFirst().y + " - "
                + lineA.getLast().y + ") * (" + lineB.getFirst().x + " - " + lineB.getLast().x + "0");

        System.out.println("intersection of " + lineA + " and " + lineB + " is at (" + intersection.x + "," + intersection.y
                + "," + intersection.z + ")");
        intersection =
                OTSPoint3D.intersectionOfLineSegments(lineA.getFirst(), lineA.getLast(), lineB.getFirst(), lineB.getLast());
        System.out.println("intersection of " + lineA + " and " + lineB + " is at (" + intersection.x + "," + intersection.y
                + "," + intersection.z + ")");
    }
}
