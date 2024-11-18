package org.opentrafficsim.core.geometry;

import java.util.function.Function;

import org.djunits.value.vdouble.scalar.Direction;
import org.djunits.value.vdouble.scalar.Length;
import org.djutils.draw.line.PolyLine2d;
import org.djutils.draw.point.OrientedPoint2d;
import org.djutils.exceptions.Throw;

/**
 * A continuous line defines a line in an exact manner, from which numerical polylines can be derived. The continuous definition
 * is useful to accurately connect different lines, e.g. based on the direction of the point where they meet. Moreover, this
 * direction may be accurately be determined by either of the lines. For example, an arc can be defined up to a certain angle.
 * Whatever the angle of the last line segment in a polyline for the arc may be, the continuous line contains the final
 * direction exactly. The continuous definition is also useful to define accurate offset lines, which depend on accurate
 * directions especially at the line end points.
 * <p>
 * Copyright (c) 2023-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public interface ContinuousLine
{

    /**
     * Start point.
     * @return start point
     */
    OrientedPoint2d getStartPoint();

    /**
     * End point.
     * @return end point
     */
    OrientedPoint2d getEndPoint();

    /**
     * Start direction.
     * @return start point
     */
    default Direction getStartDirection()
    {
        return Direction.instantiateSI(getStartPoint().dirZ);
    }

    /**
     * End direction.
     * @return end point
     */
    default Direction getEndDirection()
    {
        return Direction.instantiateSI(getEndPoint().dirZ);
    }

    /**
     * Start curvature.
     * @return start curvature
     */
    double getStartCurvature();

    /**
     * End curvature.
     * @return end curvature
     */
    double getEndCurvature();

    /**
     * Start radius.
     * @return start radius
     */
    default double getStartRadius()
    {
        return 1.0 / getStartCurvature();
    }

    /**
     * End radius.
     * @return end radius
     */
    default double getEndRadius()
    {
        return 1.0 / getEndCurvature();
    }

    /**
     * Flatten continuous line in to a polyline. Implementations should use the flattener when relevant and possible.
     * @param flattener flattener
     * @return flattened line
     */
    PolyLine2d flatten(Flattener flattener);

    /**
     * Flatten continuous line offset in to a polyline. Implementations should use the flattener when relevant and possible.
     * @param offset offset data
     * @param flattener flattener
     * @return flattened line
     */
    PolyLine2d flattenOffset(OffsetFunction offset, Flattener flattener);

    /**
     * Return the length of the line.
     * @return length of the line
     */
    double getLength();

    /**
     * Temporary function implementation with {@code getDerivative()} and {@code getKnots()} method.
     */
    interface OffsetFunction extends Function<Double, Double>
    {
        /**
         * Returns the derivative of the data with respect to fractional length.
         * @param fractionalLength fractional length, may be outside range [0 ... 1]
         * @return derivative of the data with respect to fractional length
         */
        double getDerivative(double fractionalLength);
        
        /**
         * Returns knots in the function.
         * @return knots in the function
         */
        double[] getKnots();
    }
    
    /**
     * Temporary implementation of {@code OffsetFunction} that provides offset data based on {@code PiecewiseLinearLength}.
     */
    class OffsetFunctionLength implements OffsetFunction
    {
        /** Length function for offsets. */
        private final PiecewiseLinearLength lengthFunction;

        /** Length to normalize. */
        private final Length length;

        /**
         * Constructor.
         * @param lengthFunction length function for offsets
         * @param length length to normalize
         */
        public OffsetFunctionLength(final Function<Length, Length> lengthFunction, final Length length)
        {
            Throw.when(!(lengthFunction instanceof PiecewiseLinearLength), IllegalArgumentException.class,
                    "Length function must be of type PiecewiseLinearLength.");
            this.lengthFunction = (PiecewiseLinearLength) lengthFunction;
            this.length = length;
        }

        @Override
        public Double apply(final Double t)
        {
            return this.lengthFunction.apply(this.length.times(t)).si;
        }

        @Override
        public double getDerivative(final double fractionalLength)
        {
            return this.lengthFunction.getDerivative(fractionalLength);
        }

        @Override
        public double[] getKnots()
        {
            return this.lengthFunction.getKnots();
        }
    }

    /**
     * Temporary implementation of {@code Function<Length, Length>} to be used for offset and width data. 
     */
    class PiecewiseLinearLength implements Function<Length, Length>
    {
        /** Fractional length data. */
        private final FractionalLengthData linearData;

        /** Length to normalize. */
        private final Length length;

        /**
         * Constructor.
         * @param linearData fractional length data
         * @param length length to normalize
         */
        public PiecewiseLinearLength(final FractionalLengthData linearData, final Length length)
        {
            this.linearData = linearData;
            this.length = length;
        }

        @Override
        public Length apply(final Length t)
        {
            return Length.instantiateSI(this.linearData.get(t.si / this.length.si));
        }
        
        /**
         * Returns the value at fraction.
         * @param fractionalLength fractional length
         * @return value at fraction
         */
        public double get(final double fractionalLength)
        {
            return this.linearData.get(fractionalLength);
        }

        /**
         * Returns the derivative.
         * @param fractionalLength fractional length
         * @return derivative
         */
        public double getDerivative(final double fractionalLength)
        {
            return this.linearData.getDerivative(fractionalLength);
        }
        
        /**
         * Return knots.
         * @return knots
         */
        public double[] getKnots()
        {
            return this.linearData.getFractionalLengthsAsArray();
        }
    }
}
