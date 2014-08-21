package org.opentrafficsim.graphs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.awt.event.ActionEvent;

import org.junit.Test;
import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalarAbs;

/**
 * <p>
 * Copyright (c) 2002-2014 Delft University of Technology, Jaffalaan 5, 2628 BX Delft, the Netherlands. All rights
 * reserved.
 * <p>
 * See for project information <a href="http://www.simulation.tudelft.nl/"> www.simulation.tudelft.nl</a>.
 * <p>
 * The OpenTrafficSim project is distributed under the following BSD-style license:<br>
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 * <ul>
 * <li>Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 * disclaimer.</li>
 * <li>Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
 * following disclaimer in the documentation and/or other materials provided with the distribution.</li>
 * <li>Neither the name of Delft University of Technology, nor the names of its contributors may be used to endorse or
 * promote products derived from this software without specific prior written permission.</li>
 * </ul>
 * This software is provided by the copyright holders and contributors "as is" and any express or implied warranties,
 * including, but not limited to, the implied warranties of merchantability and fitness for a particular purpose are
 * disclaimed. In no event shall the copyright holder or contributors be liable for any direct, indirect, incidental,
 * special, exemplary, or consequential damages (including, but not limited to, procurement of substitute goods or
 * services; loss of use, data, or profits; or business interruption) however caused and on any theory of liability,
 * whether in contract, strict liability, or tort (including negligence or otherwise) arising in any way out of the use
 * of this software, even if advised of the possibility of such damage.
 * @version Aug 21, 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class ContourPlotTest
{
    /** Lower bound of test distance range */
    static DoubleScalarAbs<LengthUnit> minimumDistance = new DoubleScalarAbs<LengthUnit>(1234, LengthUnit.METER);

    /** Upper bound of test distance range */
    static DoubleScalarAbs<LengthUnit> maximumDistance = new DoubleScalarAbs<LengthUnit>(12345, LengthUnit.METER);

    /**
     * Test the DensityContourPlot
     */
    @SuppressWarnings("static-method")
    @Test
    public void densityContourTest()
    {
        DensityContourPlot dcp = new DensityContourPlot("title", minimumDistance, maximumDistance);
        assertTrue("newly created DensityContourPlot should not be null", null != dcp);
        assertEquals("seriesCount should be 1", 1, dcp.getSeriesCount());
        assertTrue("SeriesKey should be \"density\"", "density".equals(dcp.getSeriesKey(0)));
        standardEmptyContourTests(dcp, 0);
    }

    /**
     * Test various properties of a ContourPlot that has no observed data added.
     * @param cp ContourPlot; the ContourPlot to test
     */
    public static void standardEmptyContourTests(ContourPlot cp, double expectedZValue)
    {
        assertEquals("seriesCount should be 1", 1, cp.getSeriesCount());
        int xBins = cp.xAxisBins();
        int yBins = cp.yAxisBins();
        int expectedXBins =
                (int) Math.ceil((DoubleScalar.minus(ContourPlot.initialUpperTimeBound,
                        ContourPlot.initialLowerTimeBound).getValueSI())
                        / ContourPlot.standardTimeGranularities[ContourPlot.standardInitialTimeGranularityIndex]);
        assertEquals("Initial xBins should be " + expectedXBins, expectedXBins, xBins);
        int expectedYBins =
                (int) Math
                        .ceil((DoubleScalar.minus(maximumDistance, minimumDistance).getValueSI())
                                / ContourPlot.standardDistanceGranularities[ContourPlot.standardInitialDistanceGranularityIndex]);
        assertEquals("yBins should be " + expectedYBins, expectedYBins, yBins);
        int bins = cp.getItemCount(0);
        assertEquals("Total bin count is product of xBins * yBins", xBins * yBins, bins);
        // Vary the x granularity
        for (double timeGranularity : ContourPlot.standardTimeGranularities)
        {
            cp.actionPerformed(new ActionEvent(cp, 0, "setTimeGranularity " + timeGranularity));
            for (double distanceGranularity : ContourPlot.standardDistanceGranularities)
            {
                cp.actionPerformed(new ActionEvent(cp, 0, "setDistanceGranularity " + distanceGranularity));
                cp.reGraph();
                expectedXBins =
                        (int) Math.ceil((DoubleScalar.minus(ContourPlot.initialUpperTimeBound,
                                ContourPlot.initialLowerTimeBound).getValueSI()) / timeGranularity);
                xBins = cp.xAxisBins();
                assertEquals("Modified xBins should be " + expectedXBins, expectedXBins, xBins);
                expectedYBins =
                        (int) Math.ceil((DoubleScalar.minus(maximumDistance, minimumDistance).getValueSI())
                                / distanceGranularity);
                yBins = cp.yAxisBins();
                assertEquals("Modified yBins should be " + expectedYBins, expectedYBins, yBins);
                bins = cp.getItemCount(0);
                assertEquals("Total bin count is product of xBins * yBins", xBins * yBins, bins);
                for (int item = 0; item < bins; item++)
                {
                    double x = cp.getXValue(0, item);
                    assertTrue("X should be >= " + ContourPlot.initialLowerTimeBound,
                            x >= ContourPlot.initialLowerTimeBound.getValueSI());
                    assertTrue("X should be <= " + ContourPlot.initialUpperTimeBound,
                            x <= ContourPlot.initialUpperTimeBound.getValueSI());
                    Number alternateX = cp.getX(0, item);
                    assertEquals("getXValue and getX should return things that have the same value", x,
                            alternateX.doubleValue(), 0.000001);
                    double y = cp.getYValue(0, item);
                    assertTrue("Y should be >= " + minimumDistance, y >= minimumDistance.getValueSI());
                    assertTrue("Y should be <= " + maximumDistance, y <= maximumDistance.getValueSI());
                    Number alternateY = cp.getY(0, item);
                    assertEquals("getYValue and getY should return things that have the same value", y,
                            alternateY.doubleValue(), 0.000001);
                    double z = cp.getZValue(0, item);
                    if (Double.isNaN(expectedZValue))
                        assertTrue("Z value should be NaN", Double.isNaN(z));
                    else
                        assertEquals("Z value should be " + expectedZValue, expectedZValue, z, 0.0001);
                }
                try
                {
                    cp.getXValue(0, -1);
                    fail("Should have thrown an Error");
                }
                catch (Error e)
                {
                    // Ignore
                }
                try
                {
                    cp.getXValue(0, bins);
                    fail("Should have thrown an Error");
                }
                catch (Error e)
                {
                    // Ignore
                }
            }
        }
        // Test some ActionEvents that ContourPlot can not handle
        try
        {
            cp.actionPerformed(new ActionEvent(cp, 0, "blabla"));
            fail("Should have thrown an Error");
        }
        catch (Error e)
        {
            // Ignore
        }
        try
        {
            cp.actionPerformed(new ActionEvent(cp, 0, "setDistanceGranularity -1"));
            fail("Should have thrown an Error");
        }
        catch (Error e)
        {
            // ignore
        }
        try
        {
            cp.actionPerformed(new ActionEvent(cp, 0, "setDistanceGranularity abc"));
            fail("Should have thrown an Error");
        }
        catch (Error e)
        {
            // ignore
        }
        try
        {
            cp.actionPerformed(new ActionEvent(cp, 0, "setDistanceGranularitIE 10")); // typo in the event name
            fail("Should have thrown an Error");
        }
        catch (Error e)
        {
            // ignore
        }
    }
}
