package org.opentrafficsim.core.geometry;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.djutils.draw.bounds.Bounds2d;
import org.djutils.draw.line.Polygon2d;
import org.djutils.draw.point.Point2d;

/**
 * Measure the performance of the Polygon2d intersection method.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 */
public final class TestIntersectionPerformance
{

    /** */
    private TestIntersectionPerformance()
    {
        // do not instantiate test class
    }

    /**
     * Create and return an Polygon2d.
     * @param numVertices the number of vertices in the constructed Polygon2d
     * @param r double radius of the constructed Polygon2d
     * @param cX x-coordinate of the center of the constructed Polygon2d
     * @param cY y-coordinate of the center of the constructed Polygon2d
     * @return Polygon2d
     */
    static Polygon2d makeNGon(final int numVertices, final double r, final double cX, final double cY)
    {
        Point2d[] points = new Point2d[numVertices];
        for (int i = 0; i < numVertices; i++)
        {
            double angle = 2 * Math.PI * i / numVertices;
            points[i] = new Point2d(cX + r * Math.sin(angle), cY + r * Math.cos(angle));
        }
        return new Polygon2d(points);
    }

    /**
     * Perform a test.
     * @param numShapes number of shapes to construct
     * @param numVertices number of vertices in each constructed shape
     * @param desiredHitFraction intended fraction of shapes that overlap a randomly selected shape
     * @param numRuns number of runs to execute
     * @param verbose if true; print details of each run
     * @param variant variant of the collision tester to use
     * @return collected statistics of this test
     */
    public static Results baseTest(final int numShapes, final int numVertices, final double desiredHitFraction,
            final int numRuns, final boolean verbose, final int variant)
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
            Collection<Polygon2d> shapes = new ArrayList<Polygon2d>();
            Ots2dSet ots2Dset = new Ots2dSet(new Bounds2d(-20, -20, dx * numShapes / 2 + 40, 4 * radius), 1);
            for (int i = 0; i < numShapes; i++)
            {
                Polygon2d shape = makeNGon(numVertices, radius, i % (numShapes / 2) * dx, i > numShapes / 2 ? radius * 1.5 : 0);
                shapes.add(shape);
                ots2Dset.add(shape);
            }
            long startMillis = System.currentTimeMillis();
            int hits = 0;
            int tests = 0;
            switch (variant)
            {
                case 0:
                    for (Polygon2d ref : shapes)
                    {
                        for (Polygon2d other : shapes)
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
                    for (Polygon2d ref : shapes)
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
     * @param args command line arguments (not used)
     * @throws IOException ...
     */
    public static void main(final String[] args) throws IOException
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
    static class Results
    {

        /** Number of shapes constructed. */
        private final int numShapes;

        /** Number of vertices per shape. */
        private final int numVertices;

        /** Total execution time for all the tests performed. */
        private List<Result> stats = new ArrayList<Result>();

        /**
         * Construct a Results object.
         * @param numShapes number of shapes constructed
         * @param numVertices number of vertices per shape
         */
        Results(final int numShapes, final int numVertices)
        {
            this.numShapes = numShapes;
            this.numVertices = numVertices;
        }

        /**
         * Add the result of one run.
         * @param numTests number of tests executed
         * @param numHits number of hits detected in <cite>numTests</cite> tests
         * @param executionTime total execution time for <cite>numTests</cite> tests
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
         * @return the number of shapes
         */
        public int getNumShapes()
        {
            return this.numShapes;
        }

        /**
         * Retrieve the number of vertices per shape.
         * @return the number of vertices per shape
         */
        public int getNumVertices()
        {
            return this.numVertices;
        }

        /**
         * Report number of statistics collected.
         * @return the number of samples stored
         */
        public final int size()
        {
            return this.stats.size();
        }

        /**
         * Retrieve the Result object at the specified index.
         * @param index the index of the requested result
         * @return Result
         */
        public final Result getResult(final int index)
        {
            return this.stats.get(index);
        }

        /**
         * Return the results as a String.
         * @param removeOutliers if true; remove highest and lowest values
         * @param verbose if true; print some diagnostics on the console
         * @return textual representation of this Results
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
        static class Result
        {
            /** Total execution time. */
            private final double executionTime;

            /** Number of tests executed in total execution timme. */
            private final int numTests;

            /** Number of hits found in <cite>numTests</cite> tests. */
            private final int numHits;

            /**
             * Construct one Result.
             * @param numTests number of tests executed
             * @param numHits number of hits detected in <cite>numTests</cite> tests
             * @param executionTime total execution time for <cite>numTests</cite> tests
             */
            Result(final int numTests, final int numHits, final double executionTime)
            {
                this.numTests = numTests;
                this.numHits = numHits;
                this.executionTime = executionTime;
            }

            /**
             * Return number of tests.
             * @return the number of tests executed
             */
            public final int getNumTests()
            {
                return this.numTests;
            }

            /**
             * Return number of hits.
             * @return the number of tests executed
             */
            public final int getNumHits()
            {
                return this.numHits;
            }

            /**
             * Return execution time.
             * @return the number of tests executed
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

        @Override
        public final String toString()
        {
            return "Results [numShapes=" + this.numShapes + ", numVertices=" + this.numVertices + ", stats=" + this.stats + "]";
        }

    }

}
