package org.opentrafficsim.core.geometry;

import java.util.ArrayList;
import java.util.List;
import java.util.NavigableMap;

import org.djunits.value.vdouble.scalar.Angle;
import org.djunits.value.vdouble.scalar.Direction;
import org.djutils.exceptions.Throw;
import org.djutils.exceptions.Try;

/**
 * Continuous definition of a clothoid. The following definitions are available:
 * <ul>
 * <li>A clothoid between two directed <i>points</i>.</li>
 * <li>A clothoid originating from a directed point with start curvature, end curvature, and <i>length</i> specified.</li>
 * <li>A clothoid originating from a directed point with start curvature, end curvature, and <i>A-value</i> specified.</li>
 * </ul>
 * This class is based on:
 * <ul>
 * <li>Dale Connor and Lilia Krivodonova (2014) "Interpolation of two-dimensional curves with Euler spirals", Journal of
 * Computational and Applied Mathematics, Volume 261, 1 May 2014, pp. 320-332.</li>
 * <li>D.J. Waltona and D.S. Meek (2009) "G<sup>1</sup> interpolation with a single Cornu spiral segment", Journal of
 * Computational and Applied Mathematics, Volume 223, Issue 1, 1 January 2009, pp. 86-96.</li>
 * </ul>
 * <p>
 * Copyright (c) 2023-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 * @see <a href="https://www.sciencedirect.com/science/article/pii/S0377042713006286">Connor and Krivodonova (2014)</a>
 * @see <a href="https://www.sciencedirect.com/science/article/pii/S0377042704000925">Waltona and Meek (2009)</a>
 */
public class ContinuousClothoid implements ContinuousLine
{

    /** Threshold to consider input to be a trivial straight or circle arc. The value is 1/10th of a degree. */
    private static final double ANGLE_TOLERANCE = 2.0 * Math.PI / 3600.0;

    /** Stopping tolerance for the Secant method to find optimal theta values. */
    private static final double SECANT_TOLERANCE = 1e-8;

    /** Start point with direction. */
    private final DirectedPoint startPoint;

    /** End point with direction. */
    private final DirectedPoint endPoint;

    /** Start curvature. */
    private final double startCurvature;

    /** End curvature. */
    private final double endCurvature;

    /** Length. */
    private final double length;

    /**
     * A-value; for scaling the Fresnal integral. The regular clothoid A-parameter is obtained by dividing by
     * {@code Math.sqrt(Math.PI)}.
     */
    private final double a;

    /** Minimum alpha value of line to draw. */
    private final double alphaMin;

    /** Maximum alpha value of line to draw. */
    private final double alphaMax;

    /** Unit vector from the origin of the clothoid, towards the positive side. */
    private final double[] t0;

    /** Normal unit vector to t0. */
    private final double[] n0;

    /** Whether the line needs to be flipped. */
    private final boolean opposite;

    /** Simplification to straight when valid. */
    private final ContinuousStraight straight;

    /** Simplification to arc when valid. */
    private final ContinuousArc arc;

    /**
     * Create clothoid between two directed points. This constructor is based on the procedure in:<br>
     * <br>
     * Dale Connor and Lilia Krivodonova (2014) "Interpolation of two-dimensional curves with Euler spirals", Journal of
     * Computational and Applied Mathematics, Volume 261, 1 May 2014, pp. 320-332.<br>
     * <br>
     * Which applies the theory proven in:<br>
     * <br>
     * D.J. Waltona and D.S. Meek (2009) "G<sup>1</sup> interpolation with a single Cornu spiral segment", Journal of
     * Computational and Applied Mathematics, Volume 223, Issue 1, 1 January 2009, pp. 86-96.<br>
     * <br>
     * This procedure guarantees that the resulting line has the minimal angle rotation that is required to connect the points.
     * If the points approximate a straight line or circle, with a tolerance of up 1/10th of a degree, those respective lines
     * are created. The numerical approximation of the underlying Fresnal integral is different from the paper. See
     * {@code Clothoid.fresnal()}.
     * @param startPoint DirectedPoint; start point.
     * @param endPoint DirectedPoint; end point.
     * @see <a href="https://www.sciencedirect.com/science/article/pii/S0377042713006286">Connor and Krivodonova (2014)</a>
     * @see <a href="https://www.sciencedirect.com/science/article/pii/S0377042704000925">Waltona and Meek (2009)</a>
     */
    public ContinuousClothoid(final DirectedPoint startPoint, final DirectedPoint endPoint)
    {
        Throw.whenNull(startPoint, "Start point may not be null.");
        Throw.whenNull(endPoint, "End point may not be null.");
        this.startPoint = startPoint;
        this.endPoint = endPoint;

        double dx = endPoint.x - startPoint.x;
        double dy = endPoint.y - startPoint.y;
        double d2 = Math.hypot(dx, dy); // length of straight line from start to end
        double d = Math.atan2(dy, dx); // angle of line through start and end points

        double phi1 = normalizeAngle(d - startPoint.dirZ);
        double phi2 = normalizeAngle(endPoint.dirZ - d);
        double phi1Abs = Math.abs(phi1);
        double phi2Abs = Math.abs(phi2);

        if (phi1Abs < ANGLE_TOLERANCE && phi2Abs < ANGLE_TOLERANCE)
        {
            // Straight
            this.length = Math.hypot(endPoint.x - startPoint.x, endPoint.y - startPoint.y);
            this.a = Double.POSITIVE_INFINITY;
            this.startCurvature = 0.0;
            this.endCurvature = 0.0;
            this.straight = new ContinuousStraight(startPoint, this.length);
            this.arc = null;
            this.alphaMin = 0.0;
            this.alphaMax = 0.0;
            this.t0 = null;
            this.n0 = null;
            this.opposite = false;
            return;
        }
        else if (Math.abs(phi2 - phi1) < ANGLE_TOLERANCE)
        {
            // Arc
            double r = .5 * d2 / Math.sin(phi1);
            double cosStartDirection = Math.cos(startPoint.dirZ);
            double sinStartDirection = Math.sin(startPoint.dirZ);
            double ang = Math.PI / 2.0;
            double cosAng = Math.cos(ang); // =0
            double sinAng = Math.sin(ang); // =1
            double x0 = startPoint.x - r * (cosStartDirection * cosAng + sinStartDirection * sinAng);
            double y0 = startPoint.y - r * (cosStartDirection * -sinAng + sinStartDirection * cosAng);
            double from = Math.atan2(startPoint.y - y0, startPoint.x - x0);
            double to = Math.atan2(endPoint.y - y0, endPoint.x - x0);
            if (r < 0 && to > from)
            {
                to = to - 2.0 * Math.PI;
            }
            else if (r > 0 && to < from)
            {
                to = to + 2.0 * Math.PI;
            }
            Angle angle = Angle.instantiateSI(Math.abs(to - from));
            this.length = angle.si * Math.abs(r);
            this.a = 0.0;
            this.startCurvature = 1.0 / r;
            this.endCurvature = 1.0 / r;
            this.straight = null;
            this.arc = new ContinuousArc(startPoint, Math.abs(r), r > 0.0, angle);
            this.alphaMin = 0.0;
            this.alphaMax = 0.0;
            this.t0 = null;
            this.n0 = null;
            this.opposite = false;
            return;
        }
        this.straight = null;
        this.arc = null;

        // The algorithm assumes |phi2| to be larger than |phi1|. If this is not the case, the clothoid is created in the
        // opposite direction.
        if (phi2Abs < phi1Abs)
        {
            this.opposite = true;
            double phi3 = phi1;
            phi1 = -phi2;
            phi2 = -phi3;
            dx = -dx;
            dy = -dy;
        }
        else
        {
            this.opposite = false;
        }

        // The algorithm assumes 0 < phi2 < pi. If this is not the case, the input and output are reflected on 'd'.
        boolean reflect = false;
        if (phi2 < 0 || phi2 > Math.PI)
        {
            reflect = true;
            phi1 = -phi1;
            phi2 = -phi2;
        }

        // h(phi1, phi2) guarantees for negative values along with 0 < phi1 < phi2 < pi, that a C-shaped clothoid exists.
        // In our numerical case, this is an indication rather than a guarantee, hence getTheta() may shift shape.
        double[] cs = Clothoid.fresnel(alphaToT(phi1 + phi2));
        double h = cs[1] * Math.cos(phi1) - cs[0] * Math.sin(phi1);
        boolean cShape = 0 < phi1 && phi1 < phi2 && phi2 < Math.PI && h < 0; // otherwise, S-shape
        double theta = getTheta(phi1, phi2, cShape);
        double aSign = cShape ? -1.0 : 1.0;
        double thetaSign = -aSign;

        double v1 = theta + phi1 + phi2;
        double v2 = theta + phi1;
        double[] cs0 = Clothoid.fresnel(alphaToT(theta));
        double[] cs1 = Clothoid.fresnel(alphaToT(v1));
        this.a = d2 / ((cs1[1] + aSign * cs0[1]) * Math.sin(v2) + (cs1[0] + aSign * cs0[0]) * Math.cos(v2));

        dx /= d2; // normalized
        dy /= d2;
        if (reflect)
        {
            // reflect t0 and n0 on 'd' so that the created output clothoid is reflected back after input was reflected
            this.t0 = new double[] {Math.cos(-v2) * dx + Math.sin(-v2) * dy, -Math.sin(-v2) * dx + Math.cos(-v2) * dy};
            this.n0 = new double[] {-this.t0[1], this.t0[0]};
        }
        else
        {
            this.t0 = new double[] {Math.cos(v2) * dx + Math.sin(v2) * dy, -Math.sin(v2) * dx + Math.cos(v2) * dy};
            this.n0 = new double[] {this.t0[1], -this.t0[0]};
        }

        this.alphaMin = thetaSign * theta;
        this.alphaMax = v1; // alphaMax = theta + phi1 + phi2, which is v1
        this.startCurvature = Math.PI * alphaToT(this.alphaMin) / this.a;
        this.endCurvature = Math.PI * alphaToT(v1) / this.a;
        this.length = this.a * (alphaToT(v1) - alphaToT(this.alphaMin));
    }

    /**
     * Create clothoid from one point based on curvature and A-value.
     * @param startPoint DirectedPoint; start point.
     * @param a Length; A-value.
     * @param startCurvature double; start curvature.
     * @param endCurvature double; end curvature;
     */
    public ContinuousClothoid(final DirectedPoint startPoint, final double a, final double startCurvature,
            final double endCurvature)
    {
        Throw.whenNull(startPoint, "Start point may not be null.");
        Throw.when(a <= 0.0, IllegalArgumentException.class, "A value must be above 0.");
        this.startPoint = startPoint;
        // Scale 'a', due to parameter conversion between C(alpha)/S(alpha) and C(t)/S(t); t = sqrt(2*alpha/pi).
        this.a = a * Math.sqrt(Math.PI);
        this.length = a * a * Math.abs(endCurvature - startCurvature);
        this.startCurvature = startCurvature;
        this.endCurvature = endCurvature;

        double l1 = a * a * startCurvature;
        double l2 = a * a * endCurvature;
        this.alphaMin = Math.abs(l1) * startCurvature / 2.0;
        this.alphaMax = Math.abs(l2) * endCurvature / 2.0;

        double ang = normalizeAngle(startPoint.dirZ) - Math.abs(this.alphaMin);
        this.t0 = new double[] {Math.cos(ang), Math.sin(ang)};
        this.n0 = new double[] {this.t0[1], -this.t0[0]};
        Direction endDirection = Direction.instantiateSI(ang + Math.abs(this.alphaMax));
        if (startCurvature > endCurvature)
        {
            // In these cases the algorithm works in the negative direction. We need to flip over the line through the start
            // point that runs perpendicular to the start direction.
            double m = Math.tan(startPoint.dirZ + Math.PI / 2.0);

            // Linear algebra flipping, see: https://math.stackexchange.com/questions/525082/reflection-across-a-line
            double onePlusMm = 1.0 + m * m;
            double oneMinusMm = 1.0 - m * m;
            double mmMinusOne = m * m - 1.0;
            double twoM = 2.0 * m;
            double t00 = this.t0[0];
            double t01 = this.t0[1];
            double n00 = this.n0[0];
            double n01 = this.n0[1];
            this.t0[0] = (oneMinusMm * t00 + 2 * m * t01) / onePlusMm;
            this.t0[1] = (twoM * t00 + mmMinusOne * t01) / onePlusMm;
            this.n0[0] = (oneMinusMm * n00 + 2 * m * n01) / onePlusMm;
            this.n0[1] = (twoM * n00 + mmMinusOne * n01) / onePlusMm;

            double ang2 = Math.atan2(this.t0[1], this.t0[0]);
            endDirection = Direction.instantiateSI(ang2 - Math.abs(this.alphaMax) + Math.PI);
        }
        OtsLine3d line = flatten(1);
        OtsPoint3d end = Try.assign(() -> line.get(line.size() - 1), "Line does not have an end point.");
        this.endPoint = new DirectedPoint(end.x, end.y, end.z, 0.0, 0.0, endDirection.si);

        // Fields not relevant for definition with curvatures
        this.straight = null;
        this.arc = null;
        this.opposite = false;
    }

    /**
     * Create clothoid from one point based on curvature and length. This method calculates the A-value as
     * <i>sqrt(L/|k2-k1|)</i>, where <i>L</i> is the length of the resulting clothoid, and <i>k2</i> and <i>k1</i> are the end
     * and start curvature.
     * @param startPoint DirectedPoint; start point.
     * @param length double; Length of the resulting clothoid.
     * @param startCurvature double; start curvature.
     * @param endCurvature double; end curvature;
     * @return ContinuousClothoid; clothoid based on curvature and length.
     */
    public static ContinuousClothoid withLength(final DirectedPoint startPoint, final double length,
            final double startCurvature, final double endCurvature)
    {
        Throw.when(length <= 0.0, IllegalArgumentException.class, "Length must be above 0.");
        double a = Math.sqrt(length / Math.abs(endCurvature - startCurvature));
        return new ContinuousClothoid(startPoint, a, startCurvature, endCurvature);
    }

    /**
     * Normalizes the angle to be in the range [-pi pi].
     * @param angle double; angle.
     * @return double; angle in the range [-pi pi].
     */
    private static double normalizeAngle(final double angle)
    {
        double out = angle;
        while (out > Math.PI)
        {
            out -= 2 * Math.PI;
        }
        while (out < -Math.PI)
        {
            out += 2 * Math.PI;
        }
        return out;
    }

    /**
     * Performs alpha to t variable change.
     * @param alpha double; alpha value, must be positive.
     * @return double; t value (length along the Fresnel integral, also known as x).
     */
    private static double alphaToT(final double alpha)
    {
        return alpha >= 0 ? Math.sqrt(alpha * 2.0 / Math.PI) : -Math.sqrt(-alpha * 2.0 / Math.PI);
    }

    /**
     * Returns theta value given shape to use. If no such value is found, the other shape may be attempted.
     * @param phi1 double; phi1.
     * @param phi2 double; phi2.
     * @param cShape boolean; C-shaped, or S-shaped otherwise.
     * @return double; theta value; the number of radians that is moved on to a side of the full clothoid.
     */
    private static double getTheta(final double phi1, final double phi2, final boolean cShape)
    {
        double sign, phiMin, phiMax;
        if (cShape)
        {
            double lambda = (1 - Math.cos(phi1)) / (1 - Math.cos(phi2));
            phiMin = 0.0;
            phiMax = (lambda * lambda * (phi1 + phi2)) / (1 - (lambda * lambda));
            sign = -1.0;
        }
        else
        {
            phiMin = Math.max(0, -phi1);
            phiMax = Math.PI / 2 - phi1;
            sign = 1;
        }

        double fMin = fTheta(phiMin, phi1, phi2, sign);
        double fMax = fTheta(phiMax, phi1, phi2, sign);
        if (fMin * fMax > 0)
        {
            throw new RuntimeException("f(phiMin) and f(phiMax) have the same sign, we cant find f(theta) = 0 between them.");
        }

        // Find optimum using Secant method, see https://en.wikipedia.org/wiki/Secant_method
        double x0 = phiMin;
        double x1 = phiMax;
        double x2 = 0;
        for (int i = 0; i < 100; i++) // max 100 iterations, otherwise use latest x2 value
        {
            double f1 = fTheta(x1, phi1, phi2, sign);
            x2 = x1 - f1 * (x1 - x0) / (f1 - fTheta(x0, phi1, phi2, sign));
            x2 = Math.max(Math.min(x2, phiMax), phiMin); // this line is an essential addition to keep the algorithm at bay
            x0 = x1;
            x1 = x2;
            if (Math.abs(x0 - x1) < SECANT_TOLERANCE || Math.abs(x0 / x1 - 1) < SECANT_TOLERANCE
                    || Math.abs(f1) < SECANT_TOLERANCE)
            {
                return x2;
            }
        }

        return x2;
    }

    /**
     * Function who's solution <i>f</i>(<i>theta</i>) = 0 for the given value of <i>phi1</i> and <i>phi2</i> gives the angle
     * that solves fitting a C-shaped clothoid through two points. This assumes that <i>sign</i> = -1. If <i>sign</i> = 1, this
     * changes to <i>g</i>(<i>theta</i>) = 0 being a solution for an S-shaped clothoid.
     * @param theta double; angle defining the curvature of the resulting clothoid.
     * @param phi1 double; angle between the line through both end points, and the direction of the first point.
     * @param phi2 double; angle between the line through both end points, and the direction of the last point.
     * @param sign double; 1 for C-shaped, -1 for S-shaped.
     * @return double; <i>f</i>(<i>theta</i>) for <i>sign</i> = -1, or <i>g</i>(<i>theta</i>) for <i>sign</i> = 1.
     */
    private static double fTheta(final double theta, final double phi1, final double phi2, final double sign)
    {
        double thetaPhi1 = theta + phi1;
        double[] cs0 = Clothoid.fresnel(alphaToT(theta));
        double[] cs1 = Clothoid.fresnel(alphaToT(thetaPhi1 + phi2));
        return (cs1[1] + sign * cs0[1]) * Math.cos(thetaPhi1) - (cs1[0] + sign * cs0[0]) * Math.sin(thetaPhi1);
    }

    /** {@inheritDoc} */
    @Override
    public DirectedPoint getStartPoint()
    {
        return this.startPoint;
    }

    /** {@inheritDoc} */
    @Override
    public DirectedPoint getEndPoint()
    {
        return this.endPoint;
    }

    /** {@inheritDoc} */
    @Override
    public double getStartCurvature()
    {
        return this.startCurvature;
    }

    /** {@inheritDoc} */
    @Override
    public double getEndCurvature()
    {
        return this.endCurvature;
    }

    /** {@inheritDoc} */
    @Override
    public double getStartRadius()
    {
        return 1.0 / this.startCurvature;
    }

    /** {@inheritDoc} */
    @Override
    public double getEndRadius()
    {
        return 1.0 / this.endCurvature;
    }

    /** {@inheritDoc} */
    @Override
    public OtsLine3d flatten(final int numSegments)
    {
        Throw.when(numSegments < 1, IllegalArgumentException.class, "Number of segments should be at least 1.");
        if (this.straight != null)
        {
            return this.straight.flatten();
        }
        if (this.arc != null)
        {
            return this.arc.flatten(numSegments);
        }
        double step = (this.alphaMax - this.alphaMin) / numSegments;
        List<OtsPoint3d> points = new ArrayList<>(numSegments + 1);

        DirectedPoint p1 = this.opposite ? this.endPoint : this.startPoint;
        DirectedPoint p2 = this.opposite ? this.startPoint : this.endPoint;

        // Create first point to figure out the required overall shift
        double[] csMin = Clothoid.fresnel(alphaToT(this.alphaMin));
        double xMin = this.a * (csMin[0] * this.t0[0] - csMin[1] * this.n0[0]);
        double yMin = this.a * (csMin[0] * this.t0[1] - csMin[1] * this.n0[1]);
        double dx = p1.x - xMin;
        double dy = p1.y - yMin;
        points.add(new OtsPoint3d(xMin + dx, yMin + dy, 0.0));

        // Due to numerical precision, we linearly scale over alpha such that the final point is exactly on p2
        double xShift = 0.0;
        double yShift = 0.0;

        // Create last point to figure out linear shift over alpha such that last point ends up at p2 (if any)
        if (p2 != null)
        {
            double[] csMax = Clothoid.fresnel(alphaToT(this.alphaMax));
            double xMax = this.a * (csMax[0] * this.t0[0] - csMax[1] * this.n0[0]);
            double yMax = this.a * (csMax[0] * this.t0[1] - csMax[1] * this.n0[1]);
            xShift = p2.x - (xMax + dx);
            yShift = p2.y - (yMax + dy);
        }

        double dAlpha = this.alphaMax - this.alphaMin;
        for (int i = 1; i <= numSegments; i++)
        {
            double alpha = this.alphaMin + i * step;
            double r = (alpha - this.alphaMin) / dAlpha;
            double[] cs = Clothoid.fresnel(alphaToT(alpha));
            points.add(new OtsPoint3d(dx + this.a * (cs[0] * this.t0[0] - cs[1] * this.n0[0]) + r * xShift,
                    dy + this.a * (cs[0] * this.t0[1] - cs[1] * this.n0[1]) + r * yShift, 0.0));
        }

        OtsLine3d line = Try.assign(() -> new OtsLine3d(points), "Unable to create OtsLine3d.");
        if (this.opposite)
        {
            line = line.reverse();
        }
        return line;
    }

    /** {@inheritDoc} */
    @Override
    public OtsLine3d flatten(final Angle maxAngleError, final double maxSpatialError)
    {
        Throw.whenNull(maxAngleError, "Maximum angle error may not be null");
        Throw.when(maxAngleError.si <= 0.0, IllegalArgumentException.class, "Max angle error should be above 0.");
        Throw.when(maxSpatialError <= 0.0, IllegalArgumentException.class, "Max spatial error should be above 0.");
        int numSegmentsAngle = (int) Math.ceil(getTotalAngle().si / maxAngleError.si);
        int numSegmentsDeviation = numSegmentsForRadius(maxSpatialError, 0.0);
        int numSegments = numSegmentsAngle > numSegmentsDeviation ? numSegmentsAngle : numSegmentsDeviation;
        return flatten(numSegments);
    }

    /**
     * Returns the total angle that the Clothoid covers.
     * @return Angle; total angle that the Clothoid covers.
     */
    private Angle getTotalAngle()
    {
        return Angle.instantiateSI(Math.abs(this.alphaMin) + Math.abs(this.alphaMax));
    }

    /**
     * Returns the number of segments to use for given maximum spatial error, accounting for given offset.
     * @param maxSpatialError double; maximum spatial error.
     * @param maxOffset double; maximum offset along the Clothoid.
     * @return int; number of segments to use for given maximum spatial error, accounting for given offset.
     */
    private int numSegmentsForRadius(final double maxSpatialError, final double maxOffset)
    {
        double maxRadius = Math.max(Math.abs(getStartRadius()), Math.abs(getEndRadius()));
        return OtsGeometryUtil.getNumSegmentsForRadius(maxSpatialError, getTotalAngle(), maxRadius + maxOffset);
    }

    /** {@inheritDoc} */
    @Override
    public OtsLine3d offset(final NavigableMap<Double, Double> offsets, final int numSegments)
    {
        Throw.whenNull(offsets, "Offsets may not be null.");
        return offset(offsets, flatten(numSegments));
    }

    /** {@inheritDoc} */
    @Override
    public OtsLine3d offset(final NavigableMap<Double, Double> offsets, final Angle maxAngleError, final double maxSpatialError)
    {
        Throw.whenNull(maxAngleError, "Maximum angle error may not be null");
        Throw.when(maxAngleError.si <= 0.0, IllegalArgumentException.class, "Max angle error should be above 0.");
        Throw.when(maxSpatialError <= 0.0, IllegalArgumentException.class, "Max spatial error should be above 0.");
        Throw.whenNull(offsets, "Offsets may not be null.");
        int numSegmentsAngle = (int) Math.ceil(getTotalAngle().si / maxAngleError.si);
        double maxOffset = 0.0;
        for (double offset : offsets.values())
        {
            maxOffset = Math.max(maxOffset, Math.abs(offset));
        }
        int numSegmentsDeviation = numSegmentsForRadius(maxSpatialError, maxOffset);
        int numSegments = numSegmentsAngle > numSegmentsDeviation ? numSegmentsAngle : numSegmentsDeviation;
        return offset(offsets, flatten(numSegments));
    }

    /**
     * Applies a naive offset on the line, and then adjusts the start and end point to be on the line perpendicular through
     * each end point.
     * @param offsets NavigableMap&lt;Double, Double&gt;; offsets, should contain keys 0.0 and 1.0.
     * @param line OtsLine3d; flattened line to offset.
     * @return OtsLine3d; offset line.
     */
    private OtsLine3d offset(final NavigableMap<Double, Double> offsets, final OtsLine3d line)
    {
        Throw.when(!offsets.containsKey(0.0), IllegalArgumentException.class, "Offsets need to contain key 0.0.");
        Throw.when(!offsets.containsKey(1.0), IllegalArgumentException.class, "Offsets need to contain key 1.0.");
        double[] relativeFractions = new double[offsets.size()];
        double[] offs = new double[offsets.size()];
        int i = 0;
        for (double f : offsets.navigableKeySet())
        {
            relativeFractions[i] = f;
            offs[i] = offsets.get(f);
            i++;
        }
        OtsLine3d offsetLine =
                Try.assign(() -> line.offsetLine(relativeFractions, offs), "Unexpected exception while creating offset line.");
        OtsPoint3d start = new OtsPoint3d(OtsGeometryUtil.offsetPoint(this.startPoint, offs[0]));
        OtsPoint3d end = new OtsPoint3d(OtsGeometryUtil.offsetPoint(this.endPoint, offs[offs.length - 1]));
        OtsPoint3d[] points = offsetLine.getPoints();
        points[0] = start;
        points[points.length - 1] = end;
        return Try.assign(() -> new OtsLine3d(points), "Unexpected exception while creating offset line.");
    }

    /** {@inheritDoc} */
    @Override
    public double getLength()
    {
        return this.length;
    }

    /**
     * Return A, the clothoid scaling parameter.
     * @return double; a, the clothoid scaling parameter.
     */
    public double getA()
    {
        // Scale 'a', due to parameter conversion between C(alpha)/S(alpha) and C(t)/S(t); t = sqrt(2*alpha/pi).
        // The value of 'this.a' is used when scaling the Fresnel integral, which is why this is stored.
        return this.a / Math.sqrt(Math.PI);
    }

}
