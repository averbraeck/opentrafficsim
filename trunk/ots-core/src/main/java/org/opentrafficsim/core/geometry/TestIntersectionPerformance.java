package org.opentrafficsim.core.geometry;

import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Measure the performance of the OTSShape intersection method.
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Apr 5, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public final class TestIntersectionPerformance
{

    /**
     * Inaccessible constructor to prevent instantiation of this class.
     */
    private TestIntersectionPerformance()
    {
        // This class cannot be instantiated
    }

    /**
     * Create and return an OTSShape.
     * @param numVertices int; the number of vertices in the constructed OTSShape
     * @param r double; double radius of the constructed OTSShape
     * @param cX double; x-coordinate of the center of the constructed OTSShape
     * @param cY double; y-coordinate of the center of the constructed OTSShape
     * @return OTSShape
     * @throws OTSGeometryException when the number of vertices is less than two, or the radius is 0;
     */
    static OTSShape makeNGon(final int numVertices, final double r, final double cX, final double cY)
            throws OTSGeometryException
    {
        OTSPoint3D[] points = new OTSPoint3D[numVertices];
        for (int i = 0; i < numVertices; i++)
        {
            double angle = 2 * Math.PI * i / numVertices;
            points[i] = new OTSPoint3D(cX + r * Math.sin(angle), cY + r * Math.cos(angle));
        }
        return new OTSShape(points);
    }

    /**
     * Perform a test.
     * @param numShapes int; number of shapes to construct
     * @param numVertices int; number of vertices in each constructed shape
     * @param desiredHitFraction double; intended fraction of shapes that overlap a randomly selected shape
     * @param numRuns int; number of runs to execute
     * @param verbose boolean; if true; print details of each run
     * @param variant int; variant of the collision tester to use
     * @return Results; collected statistics of this test
     * @throws OTSGeometryException when the number of vertices iss less than two
     */
    public static Results baseTest(final int numShapes, final int numVertices, final double desiredHitFraction,
            final int numRuns, final boolean verbose, final int variant) throws OTSGeometryException
    {
        Results results = new Results(numShapes, numVertices);
        if (verbose)
        {
            System.out.println("Counting collisions among " + numShapes + " shapes with " + numVertices + " vertices");
        }
        for (int run = 0; run < numRuns; run++)
        {
            double radius = 19;
            double dx = 6 * radius / desiredHitFraction / numShapes;
            Collection<OTSShape> shapes = new ArrayList<OTSShape>();
            OTS2DSet ots2Dset = new OTS2DSet(new Rectangle2D.Double(-20, -20, dx * numShapes / 2 + 40, 4 * radius), 1);
            for (int i = 0; i < numShapes; i++)
            {
                OTSShape shape = makeNGon(numVertices, radius, i % (numShapes / 2) * dx, i > numShapes / 2 ? radius * 1.5 : 0);
                shapes.add(shape);
                ots2Dset.add(shape);
            }
            long startMillis = System.currentTimeMillis();
            int hits = 0;
            int tests = 0;
            switch (variant)
            {
                case 0:
                    for (OTSShape ref : shapes)
                    {
                        for (OTSShape other : shapes)
                        {
                            tests++;
                            if (ref.intersects(other))
                            {
                                hits++;
                            }
                        }
                    }
                    break;

                case 1:
                    for (OTSShape ref : shapes)
                    {
                        tests += shapes.size();
                        hits += ots2Dset.intersectingShapes(ref).size();
                    }
                    break;

                default:
                    throw new Error("Bad variant: " + variant);

            }
            long endMillis = System.currentTimeMillis();
            double duration = 0.001 * (endMillis - startMillis);
            results.addResult(tests, hits, duration);
            if (verbose)
            {
                System.out.println(results.getResult(results.size() - 1));
                // System.out.println(String.format(
                // "tests %d, hits %d, fraction hits %f, time %.3fs %10.4f us/test, total time %fs", tests, hits, 1.0
                // * hits / tests, duration, 1000000 * duration / tests, duration));
            }
        }
        return results;
    }

    /**
     * Measure the performance.
     * @param args String[]; command line arguments (not used)
     * @throws OTSGeometryException ...
     * @throws IOException ...
     */
    public static void main(final String[] args) throws OTSGeometryException, IOException
    {
        System.out.println("Type return to start ...");
        System.in.read();
        final int numEdges = 8000;
        final int numRuns = 10;
        for (int variant = 0; variant <= 1; variant++)
        {
            System.out.println(Results.getHeader());
            for (int numVertices : new int[] {10, 20, 50, 100, 200, 500, 1000, 2000, 5000, 10000})
            {
                if (numEdges / numVertices > 2)
                {
                    System.out.println(
                            baseTest(numEdges / numVertices, numVertices, 0.10, numRuns, false, variant).result(true, false));
                }
            }
        }
        System.out.println("Finished");
    }

    /**
     * Storage for the results of a number of runs with identical numbers of shapes and vertices per shape.
     */
    static class Results implements Serializable
    {

        /** */
        private static final long serialVersionUID = 20160412L;

        /** Number of shapes constructed. */
        private final int numShapes;

        /** Number of vertices per shape. */
        private final int numVertices;

        /** Total execution time for all the tests performed. */
        private List<Result> stats = new ArrayList<Result>();

        /**
         * Construct a Results object.
         * @param numShapes int; number of shapes constructed
         * @param numVertices int; number of vertices per shape
         */
        Results(final int numShapes, final int numVertices)
        {
            this.numShapes = numShapes;
            this.numVertices = numVertices;
        }

        /**
         * Add the result of one run.
         * @param numTests int; number of tests executed
         * @param numHits int; number of hits detected in <cite>numTests</cite> tests
         * @param executionTime double; total execution time for <cite>numTests</cite> tests
         */
        public void addResult(final int numTests, final int numHits, final double executionTime)
        {
            if (this.stats.size() > 0)
            {
                if (numTests != this.stats.get(0).getNumTests())
                {
                    System.err.println(
                            "Number of tests per run changed from " + this.stats.get(0).getNumTests() + " to " + numTests);
                }
                if (numHits != this.stats.get(0).getNumHits())
                {
                    System.err.println(
                            "Number of hits per run changed from " + this.stats.get(0).getNumHits() + " to " + numHits);
                }
            }
            this.stats.add(new Result(numTests, numHits, executionTime));
        }

        /**
         * Retrieve the number of shapes used in the tests.
         * @return int; the number of shapes
         */
        public int getNumShapes()
        {
            return this.numShapes;
        }

        /**
         * Retrieve the number of vertices per shape.
         * @return int; the number of vertices per shape
         */
        public int getNumVertices()
        {
            return this.numVertices;
        }

        /**
         * Report number of statistics collected.
         * @return int; the number of samples stored
         */
        public final int size()
        {
            return this.stats.size();
        }

        /**
         * Retrieve the Result object at the specified index.
         * @param index int; the index of the requested result
         * @return Result
         */
        public final Result getResult(final int index)
        {
            return this.stats.get(index);
        }

        /**
         * Return the results as a String.
         * @param removeOutliers boolean; if true; remove highest and lowest values
         * @param verbose boolean; if true; print some diagnostics on the console
         * @return String; textual representation of this Results
         */
        public String result(final boolean removeOutliers, final boolean verbose)
        {
            int minimumSize = removeOutliers ? 4 : 2;
            if (this.size() <= minimumSize)
            {
                return ("Not enough results collected");
            }
            // Remove lowest and highest value
            Result lowestRunTime = this.stats.get(0);
            Result highestRunTime = lowestRunTime;
            for (Result sample : this.stats)
            {
                double runTime = sample.getExecutionTime();
                if (runTime < highestRunTime.getExecutionTime())
                {
                    highestRunTime = sample;
                }
                if (runTime > lowestRunTime.getExecutionTime())
                {
                    lowestRunTime = sample;
                }
            }
            if (verbose)
            {
                System.out.println(
                        String.format("Removing lowest (%s) and highest (%s) run times", lowestRunTime, highestRunTime));
            }
            this.stats.remove(highestRunTime);
            this.stats.remove(lowestRunTime);
            double sumRunTime = 0;
            double sumRunTimeSquared = 0;
            int totalTestsPerformed = 0;
            int totalHits = 0;
            for (Result sample : this.stats)
            {
                double runTime = sample.getExecutionTime();
                sumRunTime += runTime;
                sumRunTimeSquared += runTime * runTime;
                totalTestsPerformed += sample.getNumTests();
                totalHits += sample.getNumHits();
            }
            final double meanRunTime = sumRunTime / size();
            final double sdevRunTime = Math.sqrt(sumRunTimeSquared - sumRunTime * sumRunTime / size()) / (size() - 1);
            // System.out.println("mean " + meanRunTime + " sdev " + sdevRunTime);
            return String.format("%7d |  %5d   |%9d |%11.4f\u00b5s |%11.4f\u00b5s | " + " %5.2f%% |  %6.2f%%   |%5d |%8.1fs",
                    this.numShapes, this.numVertices, totalTestsPerformed, 1000000 * sumRunTime / totalTestsPerformed,
                    1000000 * sdevRunTime * size() / totalTestsPerformed, 100 * sdevRunTime / meanRunTime,
                    100.0 * totalHits / totalTestsPerformed, size(), sumRunTime);
        }

        /**
         * Return header string that matches the output of the <cite>result</cite> method.
         * @return String
         */
        public static String getHeader()
        {
            return "# shapes|# vertices|  # tests |mean time/test|sdev time/test|sdev/mean|hit fraction|# runs|total time";
        }

        /**
         * Storage for execution time, number of tests and number of hits.
         */
        static class Result implements Serializable
        {
            /** */
            private static final long serialVersionUID = 20160400L;

            /** Total execution time. */
            private final double executionTime;

            /** Number of tests executed in total execution timme. */
            private final int numTests;

            /** Number of hits found in <cite>numTests</cite> tests. */
            private final int numHits;

            /**
             * Construct one Result.
             * @param numTests int; number of tests executed
             * @param numHits int; number of hits detected in <cite>numTests</cite> tests
             * @param executionTime double; total execution time for <cite>numTests</cite> tests
             */
            Result(final int numTests, final int numHits, final double executionTime)
            {
                this.numTests = numTests;
                this.numHits = numHits;
                this.executionTime = executionTime;
            }

            /**
             * @return int; the number of tests executed
             */
            public final int getNumTests()
            {
                return this.numTests;
            }

            /**
             * @return int; the number of tests executed
             */
            public final int getNumHits()
            {
                return this.numHits;
            }

            /**
             * @return int; the number of tests executed
             */
            public final double getExecutionTime()
            {
                return this.executionTime;
            }

            @Override
            public final String toString()
            {
                return String.format("        |          | %8d |%11.4f\u00b5s |              |         |  %6.2f%%   |",
                        this.numTests, 1000000 * this.executionTime / this.numTests, 100.0 * this.numHits / this.numTests);
            }
        }

        /** {@inheritDoc} */
        @Override
        public final String toString()
        {
            return "Results [numShapes=" + this.numShapes + ", numVertices=" + this.numVertices + ", stats=" + this.stats + "]";
        }

    }

}
