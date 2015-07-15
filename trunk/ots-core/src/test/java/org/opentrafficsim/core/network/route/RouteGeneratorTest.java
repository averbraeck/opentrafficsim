package org.opentrafficsim.core.network.route;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.SortedMap;
import java.util.TreeMap;

import org.junit.Test;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.core.network.geotools.NodeGeotools;
import org.opentrafficsim.core.network.route.ProbabilisticFixedRouteGenerator;
import org.opentrafficsim.core.network.route.Route;
import org.opentrafficsim.core.network.route.RouteGenerator;

import com.vividsolutions.jts.geom.Coordinate;

/**
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author: pknoppers
 * $, initial version 0 mrt. 2015 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class RouteGeneratorTest
{
    /**
     * Test the fixedRouteGenerator class.
     * @throws Exception when something goes wrong (should not happen)
     */
    @Test
    public void fixedRouteGeneratorTest() throws Exception
    {
        List<Node<?, ?>> nodes = new ArrayList<Node<?, ?>>();
        nodes.add(new NodeGeotools.STR("n1", new Coordinate(0, 0, 0)));
        nodes.add(new NodeGeotools.STR("n2", new Coordinate(1000, 0, 0)));
        nodes.add(new NodeGeotools.STR("n3", new Coordinate(1000, 1000, 0)));
        FixedRouteGenerator frg = new FixedRouteGenerator(nodes);
        assertNotNull("The new FixedRouteGenerator should not be null", frg);
        Route r1 = frg.generateRoute();
        assertNotNull("The FixedRouteGenerator generates non-null routes", r1);
        for (int i = 0; i < nodes.size(); i++)
        {
            assertEquals("Element i of route should match node i", nodes.get(i), r1.getNode(i));
        }
        // Generate a second one to prove that the generated routes are independent of each other
        Route r2 = frg.generateRoute();
        r1.visitNextNode();
        assertEquals("After visiting one node on r1, the lastVisitedNode on r1 should be the first node in nodes",
                nodes.get(0), r1.lastVisitedNode());
        assertNull("In r2 lastVisitedNode should be null", r2.lastVisitedNode());
    }

    /**
     * Test the ProbabilisticFixedRouteGenerator.
     * @throws Exception when something goes wrong (should not happen)
     */
    @Test
    public void probabilisticFixedRouteGeneratorTest() throws Exception
    {
        SortedMap<RouteGenerator, Double> routeProbabilities = new TreeMap<RouteGenerator, Double>();
        try
        {
            new ProbabilisticFixedRouteGenerator(routeProbabilities);
            fail("Creating a ProbabilisticFixedRouteGenerator from an empty map should have thrown a NetworkException");
        }
        catch (NetworkException e)
        {
            // Ignore expected exception
        }
        try
        {
            new ProbabilisticFixedRouteGenerator(routeProbabilities, 12345L);
            fail("Creating a ProbabilisticFixedRouteGenerator from an empty map should have thrown a NetworkException");
        }
        catch (NetworkException e)
        {
            // Ignore expected exception
        }
        double[] frequencies = new double[]{1, 2, 7};
        double sumFrequencies = 0;
        for (double frequency : frequencies)
        {
            sumFrequencies += frequency;
        }
        routeProbabilities.put(createRouteGenerator("0"), frequencies[0]);
        ProbabilisticFixedRouteGenerator pfrg = new ProbabilisticFixedRouteGenerator(routeProbabilities, 1234);
        assertNotNull("Returned ProbabilisticRouteGenerator should not be null", pfrg);
        for (int i = 0; i < 20; i++)
        {
            Route r = pfrg.generateRoute();
            assertEquals("Every generated Route should end at node \"0\"", "0", r.destinationNode().getId());
        }
        routeProbabilities.put(createRouteGenerator("1"), frequencies[1]);
        routeProbabilities.put(createRouteGenerator("2"), frequencies[2]);
        pfrg = new ProbabilisticFixedRouteGenerator(routeProbabilities, 1234);
        int[] observedCounts = new int[3];
        int samplesToTake = 10000;
        for (int i = 0; i < samplesToTake; i++)
        {
            Route r = pfrg.generateRoute();
            String lastNodeName = r.destinationNode().getId().toString();
            int index = Integer.parseInt(lastNodeName);
            observedCounts[index]++;
        }
        for (int index = 0; index < observedCounts.length; index++)
        {
            double observedFraction = 1.0 * observedCounts[index] / samplesToTake;
            double expectedFraction = frequencies[index] / sumFrequencies;
            double deviation = observedFraction - expectedFraction;
            System.out.println(String.format(Locale.US,
                    "Probability %.1f: expected/observed count %4d/%4d relative deviation % 6.4f", expectedFraction,
                    (int) (samplesToTake * expectedFraction), observedCounts[index], deviation));
            final double maxDeviation = 0.01;
            assertTrue("Deviation " + deviation + " (using the fixed seed) should be less than " + maxDeviation,
                    Math.abs(deviation) < maxDeviation);
        }
        // Add another entry with an illegal frequency
        routeProbabilities.put(createRouteGenerator("3"), -2d);
        try
        {
            new ProbabilisticFixedRouteGenerator(routeProbabilities, 1234);
            fail("Illegal probability/frequency should have thrown an exception");
        }
        catch (NetworkException e)
        {
            // Ignore expected exception
        }
    }

    /**
     * Create a FixedRouteGenerator that has a caller settable name for the final node.
     * @param endNodeName String; name of the final node in the Routes generated by the returned FixedRouteGenerator
     * @return FixedRouteGenerator
     */
    private FixedRouteGenerator createRouteGenerator(String endNodeName)
    {
        List<Node<?, ?>> nodes = new ArrayList<Node<?, ?>>();
        nodes.add(new NodeGeotools.STR("n1", new Coordinate(0, 0, 0)));
        nodes.add(new NodeGeotools.STR("n2", new Coordinate(1000, 0, 0)));
        nodes.add(new NodeGeotools.STR("n3", new Coordinate(1000, 1000, 0)));
        nodes.add(new NodeGeotools.STR(endNodeName, new Coordinate(2000, 1000, 0)));
        return new FixedRouteGenerator(nodes);
    }
}
