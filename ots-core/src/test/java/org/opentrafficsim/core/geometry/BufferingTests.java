package org.opentrafficsim.core.geometry;

import static org.junit.Assert.fail;

import org.djutils.draw.point.Point2d;
import org.junit.Test;

/**
 * Test the alternative offset line classes. <br>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 */
public class BufferingTests
{

    /**
     * Test the offsetLine method that takes relative fraction and offsets arrays.
     * @throws OtsGeometryException if that happens uncaught; this test has failed
     */
    @Test
    public void fractionalOffsetLineTest() throws OtsGeometryException
    {
        System.out.println("Hier komt ie");
        OtsLine2d referenceLine = new OtsLine2d(
                new Point2d[] {new Point2d(10, 20), new Point2d(20, 20), new Point2d(30, 30), new Point2d(30, 40)});
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
        catch (OtsGeometryException oge)
        {
            // Ignore expected exception
        }

        try
        {
            referenceLine.offsetLine(new double[] {0.5}, new double[] {2});
            fail("offsetLine with too few fractions should have thrown an exception");
        }
        catch (OtsGeometryException oge)
        {
            // Ignore expected exception
        }

        //OtsLine2d offsetLine = referenceLine.offsetLine(relativeFractions, offsets);
        //System.out.println(offsetLine.toPlot());
    }

}
