package org.opentrafficsim.core.geometry;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

import org.djutils.draw.line.PolyLine2d;
import org.djutils.draw.point.Point2d;
import org.djutils.exceptions.Throw;

/**
 * Flattens a continuous line in to a polyline.
 * <p>
 * Copyright (c) 2023-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
@FunctionalInterface
public interface Flattener
{

    /**
     * Flatten continuous line in to a polyline.
     * @param line FlattableLine; line function.
     * @return PolyLine2d; flattened line.
     */
    PolyLine2d flatten(FlattableLine line);

    /**
     * Flattener based on number of segments.
     * <p>
     * Copyright (c) 2023-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
     * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
     * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
     */
    public static class NumSegments implements Flattener
    {
        /** Number of segments. */
        private final int numSegments;

        /**
         * Constructor.
         * @param numSegments int; number of segments, must be at least 1.
         */
        public NumSegments(final int numSegments)
        {
            Throw.when(numSegments < 1, IllegalArgumentException.class, "Number of segments must be at least 1.");
            this.numSegments = numSegments;
        }

        /** {@inheritDoc} */
        @Override
        public PolyLine2d flatten(final FlattableLine line)
        {
            Throw.whenNull(line, "Line function may not be null.");
            List<Point2d> points = new ArrayList<>(this.numSegments + 1);
            for (int i = 0; i <= this.numSegments; i++)
            {
                points.add(line.get(((double) i) / this.numSegments));
            }
            return new PolyLine2d(points);
        }
    }

    /**
     * Flattener based on maximum deviation.
     * <p>
     * Copyright (c) 2023-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
     * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
     * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
     */
    public static class MaxDeviation implements Flattener
    {
        /** Maximum deviation. */
        private final double maxDeviation;

        /**
         * Constructor.
         * @param maxDeviation int; maximum deviation, must be above 0.0.
         */
        public MaxDeviation(final double maxDeviation)
        {
            Throw.when(maxDeviation <= 0.0, IllegalArgumentException.class, "Maximum deviation must be above 0.0.");
            this.maxDeviation = maxDeviation;
        }

        /** {@inheritDoc} */
        @Override
        public PolyLine2d flatten(final FlattableLine line)
        {
            Throw.whenNull(line, "Line function may not be null.");
            NavigableMap<Double, Point2d> result = new TreeMap<>();
            result.put(0.0, line.get(0.0));
            result.put(1.0, line.get(1.0));

            // Walk along all point pairs and see if additional points need to be inserted
            double prevT = result.firstKey();
            Point2d prevPoint = result.get(prevT);
            Map.Entry<Double, Point2d> entry;
            while ((entry = result.higherEntry(prevT)) != null)
            {
                double nextT = entry.getKey();
                Point2d nextPoint = entry.getValue();
                double medianT = (prevT + nextT) / 2;
                Point2d medianPoint = line.get(medianT);

                // Check max deviation
                Point2d projectedPoint = medianPoint.closestPointOnSegment(prevPoint, nextPoint);
                double errorPosition = medianPoint.distance(projectedPoint);
                if (errorPosition >= this.maxDeviation)
                {
                    // We need to insert another point
                    result.put(medianT, medianPoint);
                    continue;
                }

                if (prevPoint.distance(nextPoint) > this.maxDeviation)
                {
                    // Check for an inflection point by creating additional points at one quarter and three quarters. If these
                    // are on opposite sides of the line from prevPoint to nextPoint; there must be an inflection point.
                    // https://stackoverflow.com/questions/1560492/how-to-tell-whether-a-point-is-to-the-right-or-left-side-of-a-line
                    Point2d quarter = line.get((prevT + medianT) / 2);
                    int sign1 = (int) Math.signum((nextPoint.x - prevPoint.x) * (quarter.y - prevPoint.y)
                            - (nextPoint.y - prevPoint.y) * (quarter.x - prevPoint.x));
                    Point2d threeQuarter = line.get((nextT + medianT) / 2);
                    int sign2 = (int) Math.signum((nextPoint.x - prevPoint.x) * (threeQuarter.y - prevPoint.y)
                            - (nextPoint.y - prevPoint.y) * (threeQuarter.x - prevPoint.x));
                    if (sign1 != sign2)
                    {
                        // There is an inflection point, inserting the halfway point should take care of this
                        result.put(medianT, medianPoint);
                        continue;
                    }
                }
                prevT = nextT;
                prevPoint = nextPoint;
            }
            return new PolyLine2d(result.values().iterator());
        }
    }

    /**
     * Flattener based on maximum deviation and maximum angle.
     * <p>
     * Copyright (c) 2023-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
     * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
     * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
     */
    public static class MaxDeviationAndAngle implements Flattener
    {
        /** Maximum deviation. */
        private final double maxDeviation;

        /** Maximum angle. */
        private final double maxAngle;

        /**
         * Constructor.
         * @param maxDeviation int; maximum deviation, must be above 0.0.
         * @param maxAngle int; maximum angle, must be above 0.0.
         */
        public MaxDeviationAndAngle(final double maxDeviation, final double maxAngle)
        {
            Throw.when(maxDeviation <= 0.0, IllegalArgumentException.class, "Maximum deviation must be above 0.0.");
            Throw.when(maxAngle <= 0.0, IllegalArgumentException.class, "Maximum angle must be above 0.0.");
            this.maxDeviation = maxDeviation;
            this.maxAngle = maxAngle;
        }

        /** {@inheritDoc} */
        @Override
        public PolyLine2d flatten(final FlattableLine line)
        {
            NavigableMap<Double, Point2d> result = new TreeMap<>();
            result.put(0.0, line.get(0.0));
            result.put(1.0, line.get(1.0));
            Map<Double, Double> directions = new LinkedHashMap<>();
            directions.put(0.0, line.getDirection(0.0));
            directions.put(1.0, line.getDirection(1.0));

            // Walk along all point pairs and see if additional points need to be inserted
            double prevT = result.firstKey();
            Point2d prevPoint = result.get(prevT);
            Map.Entry<Double, Point2d> entry;
            int iterationsAtSinglePoint = 0;
            while ((entry = result.higherEntry(prevT)) != null)
            {
                double nextT = entry.getKey();
                Point2d nextPoint = entry.getValue();
                double medianT = (prevT + nextT) / 2;
                Point2d medianPoint = line.get(medianT);

                // Check max deviation
                Point2d projectedPoint = medianPoint.closestPointOnSegment(prevPoint, nextPoint);
                double errorPosition = medianPoint.distance(projectedPoint);
                if (errorPosition >= this.maxDeviation)
                {
                    // We need to insert another point
                    result.put(medianT, medianPoint);
                    directions.put(medianT, line.getDirection(medianT)); // for angle checks
                    continue;
                }

                // Check max angle
                double angle = prevPoint.directionTo(nextPoint) - directions.get(prevT);
                while (angle < -Math.PI)
                {
                    angle += 2 * Math.PI;
                }
                while (angle > Math.PI)
                {
                    angle -= 2 * Math.PI;
                }
                if (Math.abs(angle) >= this.maxAngle)
                {
                    // We need to insert another point
                    result.put(medianT, medianPoint);
                    directions.put(medianT, line.getDirection(medianT));
                    iterationsAtSinglePoint++;
                    Throw.when(iterationsAtSinglePoint == 50, RuntimeException.class,
                            "Required a new point 50 times at the same point. Likely the reported direction of the point does "
                                    + "not match further points produced. Consider using the numerical approach in the "
                                    + "default getDirection(fraction) method of the FlattableLine.");
                    continue;
                }
                iterationsAtSinglePoint = 0;

                // Check for an inflection point by creating additional points at one quarter and three quarters. If these
                // are on opposite sides of the line from prevPoint to nextPoint; there must be an inflection point.
                // https://stackoverflow.com/questions/1560492/how-to-tell-whether-a-point-is-to-the-right-or-left-side-of-a-line
                Point2d quarter = line.get((prevT + medianT) / 2);
                int sign1 = (int) Math.signum((nextPoint.x - prevPoint.x) * (quarter.y - prevPoint.y)
                        - (nextPoint.y - prevPoint.y) * (quarter.x - prevPoint.x));
                Point2d threeQuarter = line.get((nextT + medianT) / 2);
                int sign2 = (int) Math.signum((nextPoint.x - prevPoint.x) * (threeQuarter.y - prevPoint.y)
                        - (nextPoint.y - prevPoint.y) * (threeQuarter.x - prevPoint.x));
                if (sign1 != sign2)
                {
                    // There is an inflection point, inserting the halfway point should take care of this
                    result.put(medianT, medianPoint);
                    directions.put(medianT, line.getDirection(medianT));
                    continue;
                }
                prevT = nextT;
                prevPoint = nextPoint;
            }
            return new PolyLine2d(result.values().iterator());
        }
    }

    /**
     * Flattener based on maximum angle.
     * <p>
     * Copyright (c) 2023-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
     * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
     * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
     */
    public static class MaxAngle implements Flattener
    {
        /** Maximum angle. */
        private final double maxAngle;

        /**
         * Constructor.
         * @param maxAngle int; maximum angle.
         */
        public MaxAngle(final double maxAngle)
        {
            Throw.when(maxAngle <= 0.0, IllegalArgumentException.class, "Maximum angle must be above 0.0.");
            this.maxAngle = maxAngle;
        }

        /** {@inheritDoc} */
        @Override
        public PolyLine2d flatten(final FlattableLine line)
        {
            NavigableMap<Double, Point2d> result = new TreeMap<>();
            result.put(0.0, line.get(0.0));
            result.put(1.0, line.get(1.0));
            Map<Double, Double> directions = new LinkedHashMap<>();
            directions.put(0.0, line.getDirection(0.0));
            directions.put(1.0, line.getDirection(1.0));

            // Walk along all point pairs and see if additional points need to be inserted
            double prevT = result.firstKey();
            Point2d prevPoint = result.get(prevT);
            Map.Entry<Double, Point2d> entry;
            int iterationsAtSinglePoint = 0;
            while ((entry = result.higherEntry(prevT)) != null)
            {
                double nextT = entry.getKey();
                Point2d nextPoint = entry.getValue();
                double medianT = (prevT + nextT) / 2;

                // Check max angle
                double angle = prevPoint.directionTo(nextPoint) - directions.get(prevT);
                while (angle < -Math.PI)
                {
                    angle += 2 * Math.PI;
                }
                while (angle > Math.PI)
                {
                    angle -= 2 * Math.PI;
                }
                if (Math.abs(angle) >= this.maxAngle)
                {
                    // We need to insert another point
                    Point2d medianPoint = line.get(medianT);
                    result.put(medianT, medianPoint);
                    directions.put(medianT, line.getDirection(medianT));
                    iterationsAtSinglePoint++;
                    Throw.when(iterationsAtSinglePoint == 50, RuntimeException.class,
                            "Required a new point 50 times at the same point. Likely the reported direction of the point does "
                                    + "not match further points produced. Consider using the numerical approach in the "
                                    + "default getDirection(fraction) method of the FlattableLine.");
                    continue;
                }
                iterationsAtSinglePoint = 0;

                // Check for an inflection point by creating additional points at one quarter and three quarters. If these
                // are on opposite sides of the line from prevPoint to nextPoint; there must be an inflection point.
                // https://stackoverflow.com/questions/1560492/how-to-tell-whether-a-point-is-to-the-right-or-left-side-of-a-line
                Point2d quarter = line.get((prevT + medianT) / 2);
                int sign1 = (int) Math.signum((nextPoint.x - prevPoint.x) * (quarter.y - prevPoint.y)
                        - (nextPoint.y - prevPoint.y) * (quarter.x - prevPoint.x));
                Point2d threeQuarter = line.get((nextT + medianT) / 2);
                int sign2 = (int) Math.signum((nextPoint.x - prevPoint.x) * (threeQuarter.y - prevPoint.y)
                        - (nextPoint.y - prevPoint.y) * (threeQuarter.x - prevPoint.x));
                if (sign1 != sign2)
                {
                    // There is an inflection point, inserting the halfway point should take care of this
                    Point2d medianPoint = line.get(medianT);
                    result.put(medianT, medianPoint);
                    directions.put(medianT, line.getDirection(medianT));
                    continue;
                }
                prevT = nextT;
                prevPoint = nextPoint;
            }
            return new PolyLine2d(result.values().iterator());
        }
    }

}
