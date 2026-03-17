package org.opentrafficsim.opendrive.parser;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.BiFunction;

import org.djunits.value.vdouble.scalar.Length;
import org.djutils.draw.curve.Arc2d;
import org.djutils.draw.curve.Curve2d;
import org.djutils.draw.curve.Flattener2d;
import org.djutils.draw.curve.Straight2d;
import org.djutils.draw.line.PolyLine2d;
import org.djutils.draw.point.DirectedPoint2d;
import org.djutils.draw.point.Point2d;
import org.opentrafficsim.opendrive.generated.EParamPoly3PRange;
import org.opentrafficsim.opendrive.generated.TRoadPlanViewGeometry;
import org.opentrafficsim.opendrive.generated.TRoadPlanViewGeometryParamPoly3;

/**
 * Design line that is a sequential combination of 2D curves.
 * <p>
 * Copyright (c) 2024-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class SegmentedCurve implements Curve2d
{

    /** Design line segments, where each is a continuous line. Double values are fractions. */
    private NavigableMap<Double, Curve2d> segments = new TreeMap<>();

    /** Knots cached, lazy calculation. */
    private Set<Double> knots;

    /**
     * Constructor.
     * @param geometry list of geometry tags
     * @param roadLength length of the road
     */
    public SegmentedCurve(final List<TRoadPlanViewGeometry> geometry, final Length roadLength)
    {
        for (TRoadPlanViewGeometry geom : geometry)
        {
            Curve2d line;
            DirectedPoint2d start = new DirectedPoint2d(geom.getX(), geom.getY(), geom.getHdg());
            if (geom.getLine() != null)
            {
                line = new Straight2d(start, geom.getLength().si);
            }
            else if (geom.getArc() != null)
            {
                double curvature = geom.getArc().getCurvature();
                double r = 1.0 / Math.abs(curvature);
                line = new Arc2d(start, r, curvature > 0.0, geom.getLength().si / r);
            }
            else if (geom.getSpiral() != null)
            {
                // TODO: use regular Clothoid2d when its fixed in djutils
                line = Clothoid2dFix.withLength(start, geom.getLength().si, geom.getSpiral().getCurvStart(),
                        geom.getSpiral().getCurvEnd());
            }
            else if (geom.getPoly3() != null)
            {
                // note that <poly3> is a deprecated tag
                throw new UnsupportedOperationException("<poly3> not supported.");
            }
            else if (geom.getParamPoly3() != null)
            {
                line = new ParamPoly3(start, geom.getParamPoly3(), geom.getLength());
            }
            else
            {
                throw new UnsupportedOperationException("TRoadPlanViewGeometry all shape tags empty.");
            }
            this.segments.put(geom.getS() / roadLength.si, line);
        }
    }

    @Override
    public Point2d getPoint(final double fraction)
    {
        return SegmentFraction.get(this, fraction, (c, f) -> c.getPoint(f));
    }

    @Override
    public DirectedPoint2d getStartPoint()
    {
        return this.segments.firstEntry().getValue().getStartPoint();
    }

    @Override
    public DirectedPoint2d getEndPoint()
    {
        return this.segments.lastEntry().getValue().getEndPoint();
    }

    @Override
    public Set<Double> getKnots()
    {
        if (this.knots == null)
        {
            this.knots = new LinkedHashSet<>();
            for (Entry<Double, Curve2d> segment : this.segments.entrySet())
            {
                double f = segment.getKey();
                if (f > 0.0)
                {
                    this.knots.add(f);
                }
                double segLength = segment.getValue().getLength();
                if (segment.getValue().getKnots() != null)
                {
                    segment.getValue().getKnots().forEach((k) -> this.knots.add(f + k / segLength));
                }
            }
        }
        return new LinkedHashSet<>(this.knots);
    }

    @Override
    public Double getDirection(final double fraction)
    {
        return SegmentFraction.get(this, fraction, (c, f) -> c.getDirection(f));
    }

    @Override
    public double getLength()
    {
        return this.segments.lastKey() + this.segments.lastEntry().getValue().getLength();
    }

    @Override
    public PolyLine2d toPolyLine(final Flattener2d flattener)
    {
        List<Point2d> list = new ArrayList<>();
        list.add(getStartPoint());
        this.segments.entrySet().stream().map((e) -> e.getValue()).forEach((c) ->
        {
            List<Point2d> flatSegment = c.toPolyLine(flattener).getPointList();
            flatSegment.remove(0);
            list.addAll(flatSegment);
        });
        return new PolyLine2d(list);
    }

    /**
     * Segment and fraction in segment.
     * @param segment segment curve
     * @param fraction fraction in segment
     */
    private record SegmentFraction(Curve2d segment, double fraction)
    {
        /**
         * Returns the segment fraction at the given fraction in the whole curve.
         * @param curve whole curve
         * @param fraction fraction in whole curve
         * @return segment fraction
         */
        private static SegmentFraction of(final SegmentedCurve curve, final double fraction)
        {
            Entry<Double, Curve2d> entry = curve.segments.floorEntry(fraction);
            Double higher = curve.segments.higherKey(entry.getKey());
            if (higher == null)
            {
                higher = 1.0;
            }
            double fInSegment = fraction == 1.0 ? 1.0 : (fraction - entry.getKey()) / (higher - entry.getKey());
            return new SegmentFraction(entry.getValue(), fInSegment);
        }

        /**
         * Returns the result of a function that is applied at a segment curve's fraction.
         * @param <T> value type
         * @param curve whole curve
         * @param fraction fraction along whole curve
         * @param function function returning value at segment curve's fraction
         * @return result of a function that is applied at a segment curve's fraction
         */
        private static <T> T get(final SegmentedCurve curve, final double fraction,
                final BiFunction<Curve2d, Double, T> function)
        {
            SegmentFraction segmentFraction = of(curve, fraction);
            return function.apply(segmentFraction.segment(), segmentFraction.fraction());
        }
    }

    /**
     * Continuous definition of {@code <paramPoly3>} tag.
     */
    private static final class ParamPoly3 implements Curve2d
    {

        /** Start point. */
        private final DirectedPoint2d start;

        /** aU coefficient. */
        private final double aU;

        /** bU coefficient. */
        private final double bU;

        /** cU coefficient. */
        private final double cU;

        /** dU coefficient. */
        private final double dU;

        /** aV coefficient. */
        private final double aV;

        /** bV coefficient. */
        private final double bV;

        /** cV coefficient. */
        private final double cV;

        /** dV coefficient. */
        private final double dV;

        /** Range of p, either 1.0 or length. */
        private final double pRange;

        /** Geometry length. */
        private final Length length;

        /**
         * Constructor.
         * @param startPoint start point
         * @param tag tag
         * @param length length
         */
        private ParamPoly3(final DirectedPoint2d startPoint, final TRoadPlanViewGeometryParamPoly3 tag, final Length length)
        {
            this.start = startPoint;
            this.aU = tag.getAU();
            this.bU = tag.getBU();
            this.cU = tag.getCU();
            this.dU = tag.getDU();
            this.aV = tag.getAV();
            this.bV = tag.getBV();
            this.cV = tag.getCV();
            this.dV = tag.getDV();
            this.pRange = EParamPoly3PRange.ARC_LENGTH.equals(tag.getPRange()) ? length.si : 1.0;
            this.length = length;
        }

        @Override
        public DirectedPoint2d getStartPoint()
        {
            return this.start;
        }

        @Override
        public DirectedPoint2d getEndPoint()
        {
            return getPoint(this.pRange);
        }

        /**
         * Returns the point at given p-value.
         * @param p p-value.
         * @return point at given p-value
         */
        @Override
        public DirectedPoint2d getPoint(final double p)
        {
            double p2 = p * p;
            double p3 = p2 * p;
            double du = this.aU + this.bU * p + this.cU * p2 + this.dU * p3;
            double dv = this.aV + this.bV * p + this.cV * p2 + this.dV * p3;
            double ddu = this.bU + 2.0 * this.cU * p + 3.0 * this.dU * p2;
            double ddv = this.bV + 2.0 * this.cV * p + 3.0 * this.dV * p2;
            return new DirectedPoint2d(this.start.x + du, this.start.y + dv, this.start.dirZ + Math.atan2(ddv, ddu));
        }

        @Override
        public Double getDirection(final double p)
        {
            double p2 = p * p;
            double ddu = this.bU + 2.0 * this.cU * p + 3.0 * this.dU * p2;
            double ddv = this.bV + 2.0 * this.cV * p + 3.0 * this.dV * p2;
            return this.start.dirZ + Math.atan2(ddv, ddu);
        }

        /*-
         * Returns the curvature for given p-value.
         * @param p p-value
         * @return curvature for given p-value
         */
        /*-
        private double getCurvature(final double p)
        {
            // https://en.wikipedia.org/wiki/Curvature#In_terms_of_a_general_parametrization
            double p2 = p * p;
            double ddu = this.bU + 2.0 * this.cU * p + 3.0 * this.dU * p2;
            double ddv = this.bV + 2.0 * this.cV * p + 3.0 * this.dV * p2;
            double dddu = 2.0 * this.cU + 6.0 * this.dU * p;
            double dddv = 2.0 * this.cV + 6.0 * this.dV * p;
            return (ddu * dddv - ddv * dddu) / Math.pow(ddu * ddu + ddv * ddv, 1.5);
        }
        */

        @Override
        public PolyLine2d toPolyLine(final Flattener2d flattener)
        {
            return flattener.flatten(this);
        }

        @Override
        public double getLength()
        {
            return this.length.si;
        }

    }

}
