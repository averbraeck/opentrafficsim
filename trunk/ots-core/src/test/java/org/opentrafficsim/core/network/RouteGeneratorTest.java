package org.opentrafficsim.core.network;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import org.junit.Test;
import org.opentrafficsim.core.network.geotools.NodeGeotools;
import org.opentrafficsim.core.network.route.ProbabilisticFixedRouteGenerator;
import org.opentrafficsim.core.network.route.Route;
import org.opentrafficsim.core.network.route.RouteGenerator;

import com.vividsolutions.jts.geom.Coordinate;

/**
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version 20 mrt. 2015 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class RouteGeneratorTest
{
    /**
     * Test the fixedRouteGenerator class.
     * @throws NetworkException
     */
    @Test
    public void fixedRouteGeneratorTest() throws NetworkException
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
     * @throws NetworkException
     */
    @Test
    public void probabilisticFixedRouteGeneratorTest() throws NetworkException
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
        double[] probabilities = new double[]{0.1, 0.2, 0.7};
        routeProbabilities.put(createRouteGenerator("0"), probabilities[0]);
        ProbabilisticFixedRouteGenerator pfrg = new ProbabilisticFixedRouteGenerator(routeProbabilities, 1234);
        assertNotNull("Returned ProbabilisticRouteGenerator should not be null", pfrg);
        for (int i = 0; i < 20; i++)
        {
            Route r = pfrg.generateRoute();
            assertEquals("Every generated Route should end at node \"0\"", "0", r.destinationNode().getId());
        }
        routeProbabilities.put(createRouteGenerator("1"), probabilities[1]);
        routeProbabilities.put(createRouteGenerator("2"), probabilities[2]);
        pfrg = new ProbabilisticFixedRouteGenerator(routeProbabilities, 1234);
        double[] observedCounts = new double[3];
        for (int i = 0; i < 1000; i++)
        {
            Route r = pfrg.generateRoute();
            String lastNodeName = r.destinationNode().getId().toString();
            int index = Integer.parseInt(lastNodeName);
            observedCounts[index]++;
        }
        for (int index = 0; index < observedCounts.length; index++)
        {
            double observedFraction = observedCounts[index] / 1000;
            double deviation = Math.abs(observedFraction - probabilities[index]);
            System.out.println("Observed counts for probability " + index + ": " + observedCounts[index]
                    + " deviation " + deviation);
            final double maxDeviation = 0.02;
            assertTrue("Deviation " + deviation + " (using the fixed seed) should be less than " + maxDeviation,
                    deviation < maxDeviation);
        }
        // TODO test that a negative probability/frequency throws a NetworkException
        // TODO test that the probabilities/frequencies are normalized
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
