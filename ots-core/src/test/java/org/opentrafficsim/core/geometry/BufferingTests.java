package org.opentrafficsim.core.geometry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.opentrafficsim.core.network.NetworkException;

/**
 * Test the alternative offset line classes. <br>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 */
public class BufferingTests
{
    /**
     * Test some very simple cases of offset line.
     * @throws OTSGeometryException if that happens uncaught; this test has failed
     */
    @Test
    public void testVariousBufferings() throws OTSGeometryException
    {
        OTSLine3D referenceLine = new OTSLine3D(new OTSPoint3D[] {new OTSPoint3D(10, 20, 0), new OTSPoint3D(20, 20, 0),
                new OTSPoint3D(30, 30, 0), new OTSPoint3D(30, 40, 0)});
        for (String implementation : new String[] {"JTS", "PK", "main"})
        {
            for (double offset : new double[] {0, 1, 3, -2})
            {
                OTSLine3D offsetLine;
                switch (implementation)
                {
                    case "JTS":
                        offsetLine = OTSBufferingJTS.offsetLine(referenceLine, offset);
                        break;

                    case "PK":
                        offsetLine = OTSOffsetLinePK.offsetLine(referenceLine, offset);
                        break;

                    case "main":
                        offsetLine = referenceLine.offsetLine(offset);
                        break;

                    default:
                        fail("bad switch in test code");
                        continue;
                }
                System.out.print(offsetLine.toPlot());
                assertEquals("first point of offset line should have same X", referenceLine.getFirst().x,
                        offsetLine.getFirst().x, 0.1);
                assertEquals("first point of offset line should have Y + offset", referenceLine.getFirst().y + offset,
                        offsetLine.getFirst().y, 0.1);
                assertEquals("last point of offset line should have same Y", referenceLine.getLast().y, offsetLine.getLast().y,
                        0.1);
                assertEquals("last point of offset line should have X minus offset", referenceLine.getLast().x - offset,
                        offsetLine.getLast().x, 0.1);
                for (OTSPoint3D p : offsetLine.getPoints())
                {

                    OTSPoint3D closest = p.closestPointOnLine(referenceLine);
                    double distance = p.distanceSI(closest);
                    assertEquals("closest point should be offset away", Math.abs(offset), distance, 0.01);
                }
                // Varying offset
                double endOffset = offset + 0.5;
                switch (implementation)
                {
                    case "JTS":
                        offsetLine = OTSBufferingJTS.offsetLine(referenceLine, offset, endOffset);
                        break;

                    case "PK":
                        continue; // Not implemented

                    case "main":
                        offsetLine = referenceLine.offsetLine(offset, endOffset);
                        break;

                    default:
                        fail("bad switch in test code");
                        continue;
                }
                System.out.print(offsetLine.toPlot());
                assertEquals("first point of offset line should have same X", referenceLine.getFirst().x,
                        offsetLine.getFirst().x, 0.1);
                assertEquals("first point of offset line should have Y + offset", referenceLine.getFirst().y + offset,
                        offsetLine.getFirst().y, 0.1);
                assertEquals("last point of offset line should have same Y", referenceLine.getLast().y, offsetLine.getLast().y,
                        0.1);
                assertEquals("last point of offset line should have X minus offset", referenceLine.getLast().x - endOffset,
                        offsetLine.getLast().x, 0.1);
                for (OTSPoint3D p : offsetLine.getPoints())
                {

                    OTSPoint3D closest = p.closestPointOnLine(referenceLine);
                    double distance = p.distanceSI(closest);
                    // determine the approximate fractional position of the closest point
                    double pivot = 0.5; // initial guess
                    double referenceLineLength = referenceLine.getLengthSI();
                    for (double step = 0.25; step > 0.05; step *= 0.7)
                    {
                        double lowerGuess = Math.max(0, pivot - step);
                        double higherGuess = Math.min(1, pivot + step);
                        OTSPoint3D pivotPoint = new OTSPoint3D(referenceLine.getLocationSI(pivot * referenceLineLength));
                        OTSPoint3D lowerGuessPoint =
                                new OTSPoint3D(referenceLine.getLocationSI(lowerGuess * referenceLineLength));
                        OTSPoint3D higherGuessPoint =
                                new OTSPoint3D(referenceLine.getLocationSI(higherGuess * referenceLineLength));
                        double errorPivot = pivotPoint.distanceSI(closest);
                        double errorLowerGuess = lowerGuessPoint.distanceSI(closest);
                        double errorHigherGuess = higherGuessPoint.distanceSI(closest);
                        if (errorLowerGuess < errorPivot && errorLowerGuess <= errorHigherGuess)
                        {
                            pivot = lowerGuess;
                        }
                        else if (errorHigherGuess < errorPivot && errorHigherGuess < errorLowerGuess)
                        {
                            pivot = higherGuess;
                        }
                    }
                    double expectedOffset = offset + (endOffset - offset) * pivot;
                    assertEquals("closest point should be approximately offset away", Math.abs(expectedOffset), distance, 0.1);
                }

            }
        }
    }

    /**
     * Test the offsetLine method that takes relative fraction and offsets arrays.
     * @throws OTSGeometryException if that happens uncaught; this test has failed
     */
    @Test
    public void fractionalOffsetLineTest() throws OTSGeometryException
    {
        System.out.println("Hier komt ie");
        OTSLine3D referenceLine = new OTSLine3D(new OTSPoint3D[] {new OTSPoint3D(10, 20, 0), new OTSPoint3D(20, 20, 0),
                new OTSPoint3D(30, 30, 0), new OTSPoint3D(30, 40, 0)});
        double[] relativeFractions = new double[] {0.1, 0.2, 0.3, 0.4, 0.8};
        double[] offsets = new double[] {2, 3, -2, 4, 5};
        try
        {
            referenceLine.offsetLine(null, offsets);
            fail("offsetLine with null for fractions should have thrown an exception");
        }
        catch (NullPointerException npe)
        {
            // Ignore expected exception
        }

        try
        {
            referenceLine.offsetLine(relativeFractions, null);
            fail("offsetLine with null for offsets should have thrown an exception");
        }
        catch (NullPointerException npe)
        {
            // Ignore expected exception
        }

        try
        {
            referenceLine.offsetLine(new double[] {}, new double[] {});
            fail("offsetLine with too few fractions should have thrown an exception");
        }
        catch (OTSGeometryException oge)
        {
            // Ignore expected exception
        }

        try
        {
            referenceLine.offsetLine(new double[] {0.5}, new double[] {2});
            fail("offsetLine with too few fractions should have thrown an exception");
        }
        catch (OTSGeometryException oge)
        {
            // Ignore expected exception
        }

        OTSLine3D offsetLine = referenceLine.offsetLine(relativeFractions, offsets);
        System.out.println(offsetLine.toPlot());
    }

    /**
     * @param args String[]; args
     * @throws NetworkException on error
     * @throws OTSGeometryException on error
     */
    public static void main(final String[] args) throws NetworkException, OTSGeometryException
    {
        // OTSLine3D line =
        // new OTSLine3D(new OTSPoint3D[]{new OTSPoint3D(-579.253, 60.157, 1.568),
        // new OTSPoint3D(-579.253, 60.177, 1.568)});
        // double offset = 4.83899987;
        // System.out.println(OTSBufferingOLD.offsetGeometryOLD(line, offset));
        OTSLine3D line = new OTSLine3D(new OTSPoint3D[] {new OTSPoint3D(-579.253, 60.157, 4.710),
                new OTSPoint3D(-579.253, 60.144, 4.712), new OTSPoint3D(-579.253, 60.144, 0.000),
                new OTSPoint3D(-579.251, 60.044, 0.000), new OTSPoint3D(-579.246, 59.944, 0.000),
                new OTSPoint3D(-579.236, 59.845, 0.000), new OTSPoint3D(-579.223, 59.746, 0.000),
                new OTSPoint3D(-579.206, 59.647, 0.000), new OTSPoint3D(-579.185, 59.549, 0.000),
                new OTSPoint3D(-579.161, 59.452, 0.000), new OTSPoint3D(-579.133, 59.356, 0.000),
                new OTSPoint3D(-579.101, 59.261, 0.000), new OTSPoint3D(-579.066, 59.168, 0.000),
                new OTSPoint3D(-579.028, 59.075, 0.000), new OTSPoint3D(-578.986, 58.985, 0.000),
                new OTSPoint3D(-578.940, 58.896, 0.000), new OTSPoint3D(-578.891, 58.809, 0.000),
                new OTSPoint3D(-578.839, 58.723, 0.000), new OTSPoint3D(-578.784, 58.640, 0.000),
                new OTSPoint3D(-578.725, 58.559, 0.000), new OTSPoint3D(-578.664, 58.480, 0.000),
                new OTSPoint3D(-578.599, 58.403, 0.000), new OTSPoint3D(-578.532, 58.329, 0.000),
                new OTSPoint3D(-578.462, 58.258, 0.000), new OTSPoint3D(-578.390, 58.189, 0.000),
                new OTSPoint3D(-578.314, 58.123, 0.000), new OTSPoint3D(-578.237, 58.060, 0.000),
                new OTSPoint3D(-578.157, 58.000, 0.000), new OTSPoint3D(-578.075, 57.943, 0.000),
                new OTSPoint3D(-577.990, 57.889, 0.000), new OTSPoint3D(-577.904, 57.839, 0.000),
                new OTSPoint3D(-577.816, 57.791, 0.000), new OTSPoint3D(-577.726, 57.747, 0.000),
                new OTSPoint3D(-577.635, 57.707, 0.000), new OTSPoint3D(-577.542, 57.670, 0.000),
                new OTSPoint3D(-577.448, 57.636, 0.000), new OTSPoint3D(-577.352, 57.606, 0.000),
                new OTSPoint3D(-577.256, 57.580, 0.000), new OTSPoint3D(-577.159, 57.557, 0.000),
                new OTSPoint3D(-577.060, 57.538, 0.000), new OTSPoint3D(-576.962, 57.523, 0.000),
                new OTSPoint3D(-576.862, 57.512, 0.000), new OTSPoint3D(-576.763, 57.504, 0.000),
                new OTSPoint3D(-576.663, 57.500, 0.000), new OTSPoint3D(-576.623, 57.500, 6.278),
                new OTSPoint3D(-576.610, 57.500, 6.280), new OTSPoint3D(-567.499, 57.473, 6.280)});
        System.out.println(line.toExcel());
        System.out.println(OTSBufferingJTS.offsetLine(line, -1.831));
    }

}
