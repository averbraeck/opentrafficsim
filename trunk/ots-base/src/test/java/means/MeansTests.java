package means;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.opentrafficsim.base.means.ArithmeticMean;
import org.opentrafficsim.base.means.GeometricMean;
import org.opentrafficsim.base.means.HarmonicMean;

/**
 * Test the classes in the means package
 * <p>
 * Copyright (c) 2013-2018 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Oct 26, 2018 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class MeansTests
{
    /** Some double values. */
    private static Double[] testValues = { 10.0, Math.PI, Math.E, 1234. };

    /** Some weights. */
    private static Double[] testWeights = { 1.0, 2.0, Math.PI, 1.0 };

    /**
     * Test the Mean classes using unity weights.
     */
    @Test
    public final void testMeansWithUnityWeights()
    {
        ArithmeticMean<Double, Double> am = new ArithmeticMean<Double, Double>();
        double sum = 0;
        for (int i = 0; i < testValues.length; i++)
        {
            double testValue = testValues[i];
            assertEquals("add returns object for method chaining", am, am.add(testValue));
            sum += testValue;
            assertEquals("sum", sum, am.getSum(), sum / 99999999);
            assertEquals("arithmetic mean", sum / (i + 1), am.getMean(), sum / (i + 1) / 99999999);
        }
        am = new ArithmeticMean<Double, Double>();
        assertEquals("add returns object for method chaining", am, am.add(testValues));
        assertEquals("arithmetic mean", sum / testValues.length, am.getMean(), sum / testValues.length / 99999999);

        am = new ArithmeticMean<Double, Double>();
        assertEquals("add returns object for method chaining", am, am.add(new ArrayList<Double>(Arrays.asList(testValues))));
        assertEquals("arithmetic mean", sum / testValues.length, am.getMean(), sum / testValues.length / 99999999);
    }

    /**
     * Test the Mean classes using varying weights.
     */
    @Test
    public final void testMeansWithWeights()
    {
        ArithmeticMean<Double, Double> am = new ArithmeticMean<Double, Double>();
        assertEquals("Initial sum is 0", 0, am.getSum(), 0.00000);
        assertEquals("Initial sum of weights is 0", 0, am.getSumOfWeights(), 0.00000);
        HarmonicMean<Double, Double> hm = new HarmonicMean<Double, Double>();
        assertEquals("Initial sum is 0", 0, hm.getSum(), 0.00000);
        assertEquals("Initial sum of weights is 0", 0, hm.getSumOfWeights(), 0.00000);
        GeometricMean<Double, Double> gm = new GeometricMean<Double, Double>();
        assertEquals("Initial sum is 0", 0, gm.getSum(), 0.00000);
        assertEquals("Initial sum of weights is 0", 0, gm.getSumOfWeights(), 0.00000);
        double sum = 0;
        double sumWeights = 0;
        double recipSum = 0;
        double product = 1;
        double geometricMean = 0;
        Map<Double, Double> map = new HashMap<>();
        for (int i = 0; i < testValues.length; i++)
        {
            double testValue = testValues[i];
            double testWeight = testWeights[i];
            map.put(testValue, testWeight); // There are no duplicates in testValues
            assertEquals("add returns object for method chaining", am, am.add(testValue, testWeight));
            hm.add(testValue, testWeight);
            gm.add(testValue, testWeight);
            sum += testValue * testWeight;
            recipSum += testWeight / testValue;
            product *= Math.pow(testValue, testWeight);
            sumWeights += testWeight;
            if (0 == i)
            {
                assertEquals("mean of one value equals value", testValue, am.getMean(), testValue / 99999999);
                assertEquals("mean of one value equals value", testValue, hm.getMean(), testValue / 99999999);
                assertEquals("mean of one value equals value", testValue, gm.getMean(), testValue / 99999999);
            }
            assertEquals("sum", sum, am.getSum(), sum / 99999999);
            assertEquals("sum of weights", sumWeights, am.getSumOfWeights(), sumWeights / 99999999);
            assertEquals("arithmetic mean", sum / sumWeights, am.getMean(), sum / sumWeights / 99999999);
            assertEquals("sum", recipSum, hm.getSum(), recipSum / 99999999);
            assertEquals("sum of weights", sumWeights, hm.getSumOfWeights(), sumWeights / 99999999);
            assertEquals("harmonic mean", sumWeights / recipSum, hm.getMean(), sumWeights / recipSum / 999999999);
            geometricMean = Math.pow(product, 1 / sumWeights);
            assertEquals("check with alternative way to compute geometric mean", geometricMean, gm.getMean(),
                    geometricMean / 99999999);
        }
        System.out.println("arithmetic mean=" + am.getMean() + ", harmonic mean=" + hm.getMean() + ", geometric mean="
                + gm.getMean());
        am = new ArithmeticMean<Double, Double>();
        hm = new HarmonicMean<Double, Double>();
        gm = new GeometricMean<Double, Double>();
        am.add(testValues[0], 123.456);
        hm.add(testValues[0], 123.456);
        gm.add(testValues[0], 123.456);
        assertEquals("One value, any weight has mean equal to value", testValues[0], am.getMean(), testValues[0] / 99999999);
        assertEquals("One value, any weight has mean equal to value", testValues[0], hm.getMean(), testValues[0] / 99999999);
        assertEquals("One value, any weight has mean equal to value", testValues[0], gm.getMean(), testValues[0] / 99999999);
        am = new ArithmeticMean<Double, Double>();
        hm = new HarmonicMean<Double, Double>();
        gm = new GeometricMean<Double, Double>();
        assertEquals("add returns object for method chaining", am, am.add(testValues, testWeights));
        assertEquals("arithmetic mean", sum / sumWeights, am.getMean(), sum / sumWeights / 99999999);
        hm.add(testValues, testWeights);
        assertEquals("harmonic mean", sumWeights / recipSum, hm.getMean(), sumWeights / recipSum / 999999999);
        gm.add(testValues, testWeights);
        assertEquals("geometric mean", geometricMean, gm.getMean(), geometricMean / 99999999);
        Double[] shortArray = Arrays.copyOfRange(testValues, 0, 2);
        try
        {
            am.add(shortArray, testWeights);
            fail("Short array of values should have thrown an IllegalArgumentException");
        }
        catch (IllegalArgumentException iae)
        {
            // Ignore expected exception
        }
        shortArray = Arrays.copyOfRange(testWeights, 0, 2);
        try
        {
            am.add(testValues, shortArray);
            fail("Short array of weights should have thrown an IllegalArgumentException");
        }
        catch (IllegalArgumentException iae)
        {
            // Ignore expected exception
        }
        am = new ArithmeticMean<Double, Double>();
        hm = new HarmonicMean<Double, Double>();
        gm = new GeometricMean<Double, Double>();
        assertEquals("add returns object for method chaining", am,
                am.add(new ArrayList<Double>(Arrays.asList(testValues)), new ArrayList<Double>(Arrays.asList(testWeights))));
        assertEquals("arithmetic mean", sum / sumWeights, am.getMean(), sum / sumWeights / 99999999);
        hm.add(new ArrayList<Double>(Arrays.asList(testValues)), new ArrayList<Double>(Arrays.asList(testWeights)));
        assertEquals("harmonic mean", sumWeights / recipSum, hm.getMean(), sumWeights / recipSum / 999999999);
        gm.add(new ArrayList<Double>(Arrays.asList(testValues)), new ArrayList<Double>(Arrays.asList(testWeights)));
        assertEquals("geometric mean", geometricMean, gm.getMean(), geometricMean / 99999999);

        List<Double> shortList = new ArrayList<Double>(Arrays.asList(testValues));
        shortList.remove(2);
        try
        {
            am.add(shortList, new ArrayList<Double>(Arrays.asList(testWeights)));
            fail("Short list of values should have thrown an IllegalArgumentException");
        }
        catch (IllegalArgumentException iae)
        {
            // Ignore expected exception
        }
        shortList = new ArrayList<Double>(Arrays.asList(testWeights));
        shortList.remove(2);
        try
        {
            am.add(new ArrayList<Double>(Arrays.asList(testValues)), shortList);
            fail("Short list of weights should have thrown an IllegalArgumentException");
        }
        catch (IllegalArgumentException iae)
        {
            // Ignore expected exception
        }

        am = new ArithmeticMean<Double, Double>();
        hm = new HarmonicMean<Double, Double>();
        gm = new GeometricMean<Double, Double>();
        assertEquals("add returns object for method chaining", am, am.add(map));
        assertEquals("arithmetic mean", sum / sumWeights, am.getMean(), sum / sumWeights / 99999999);
        hm.add(map);
        assertEquals("harmonic mean", sumWeights / recipSum, hm.getMean(), sumWeights / recipSum / 999999999);
        gm.add(map);
        assertEquals("geometric mean", geometricMean, gm.getMean(), geometricMean / 99999999);

        am = new ArithmeticMean<Double, Double>();
        hm = new HarmonicMean<Double, Double>();
        gm = new GeometricMean<Double, Double>();
        assertEquals("add returns object for method chaining", am,
                am.add(new ArrayList<Double>(Arrays.asList(testValues)), (Double v) -> map.get(v)));
        assertEquals("arithmetic mean", sum / sumWeights, am.getMean(), sum / sumWeights / 99999999);
        hm.add(new ArrayList<Double>(Arrays.asList(testValues)), (Double v) -> map.get(v));
        assertEquals("harmonic mean", sumWeights / recipSum, hm.getMean(), sumWeights / recipSum / 999999999);
        gm.add(new ArrayList<Double>(Arrays.asList(testValues)), (Double v) -> map.get(v));
        assertEquals("geometric mean", geometricMean, gm.getMean(), geometricMean / 99999999);

        am = new ArithmeticMean<Double, Double>();
        hm = new HarmonicMean<Double, Double>();
        gm = new GeometricMean<Double, Double>();
        Integer[] indices = new Integer[] { 0, 1, 2, 3 };
        List<Integer> indexList = new ArrayList<>(Arrays.asList(indices));
        assertEquals("add returns object for method chaining", am,
                am.add(indexList, (Integer i) -> testValues[i], (Integer i) -> testWeights[i]));
        assertEquals("arithmetic mean", sum / sumWeights, am.getMean(), sum / sumWeights / 99999999);
        hm.add(indexList, (Integer i) -> testValues[i], (Integer i) -> testWeights[i]);
        assertEquals("harmonic mean", sumWeights / recipSum, hm.getMean(), sumWeights / recipSum / 999999999);
        gm.add(indexList, (Integer i) -> testValues[i], (Integer i) -> testWeights[i]);
        assertEquals("geometric mean", geometricMean, gm.getMean(), geometricMean / 99999999);

        assertTrue("toString method returns something descriptive", am.toString().contains("ArithmeticMean"));
        assertTrue("toString method returns something descriptive", hm.toString().contains("HarmonicMean"));
        assertTrue("toString method returns something descriptive", gm.toString().contains("GeometricMean"));
    }

}
