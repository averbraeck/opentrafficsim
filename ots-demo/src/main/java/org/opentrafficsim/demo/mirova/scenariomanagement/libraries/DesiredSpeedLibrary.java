package org.opentrafficsim.demo.mirova.scenariomanagement.libraries;

import java.util.Arrays;

import org.djunits.unit.SpeedUnit;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.core.units.distributions.ContinuousDistDoubleScalar;

import nl.tudelft.simulation.jstats.distributions.DistEmpiricalInterpolated;
import nl.tudelft.simulation.jstats.distributions.DistUniform;
import nl.tudelft.simulation.jstats.distributions.empirical.AbstractEmpiricalDistribution;
import nl.tudelft.simulation.jstats.distributions.empirical.InterpolatedEmpiricalDistribution;
import nl.tudelft.simulation.jstats.streams.StreamInterface;

/**
 * DesiredSpeedLibrary
 * -------------------
 *
 * Contains a collection of desired–speed (free–flow speed) distributions
 * for cars and trucks on motorways. These distributions represent
 * empirical observations of how drivers choose their desired speed under
 * different posted speed limits and traffic density conditions.
 *
 * The distributions follow a cumulative–distribution–function (CDF)
 * representation and are interpolated to yield continuous values.
 *
 */
public class DesiredSpeedLibrary {

    /**
     * Do not instantiate.
     */
    private DesiredSpeedLibrary()
    {
        //
    }



    // ----------------------------------------------------------------------
    // Empirical distributions from german motorways with different speed limits.
    // ----------------------------------------------------------------------

    /** Cars on motorways with 100 km/h limit
     *  These distributions already account for the speed limit and driver compliance.
     *  They are taken from a Vissim model, in which speed limits are not explicitly represented.
     *  Use with caution in OTS, where speed limits should be modeled explicitly.
     * @param stream
     * @return */
    public static ContinuousDistDoubleScalar.Rel<Speed, SpeedUnit>
            cars100kmh(final StreamInterface stream) {

        InterpolatedEmpiricalDistribution dist =
            new InterpolatedEmpiricalDistribution(
                new Number[] {88, 95, 100, 110, 120, 130},
                new double[] {0.0, 0.03, 0.10, 0.70, 0.91, 1.0}
            );

        return new ContinuousDistDoubleScalar.Rel<>(
            new DistEmpiricalInterpolated(stream, dist),
            SpeedUnit.KM_PER_HOUR
        );
    }

    /** Cars on motorways with 120 km/h limit
     *  These distributions already account for the speed limit and driver compliance.
     *  They are taken from a Vissim model, in which speed limits are not explicitly represented.
     *  Use with caution in OTS, where speed limits should be modeled explicitly.
     * @param stream
     * @return */
    public static ContinuousDistDoubleScalar.Rel<Speed, SpeedUnit>
            cars120kmh(final StreamInterface stream) {

        InterpolatedEmpiricalDistribution dist =
            new InterpolatedEmpiricalDistribution(
                new Number[] {85, 105, 110, 125, 140, 155},
                new double[] {0.0, 0.03, 0.10, 0.68, 0.91, 1.0}
            );

        return new ContinuousDistDoubleScalar.Rel<>(
            new DistEmpiricalInterpolated(stream, dist),
            SpeedUnit.KM_PER_HOUR
        );
    }

    /** Cars on motorways with 130 km/h limit
     *  These distributions already account for the speed limit and driver compliance.
     *  They are taken from a Vissim model, in which speed limits are not explicitly represented.
     *  Use with caution in OTS, where speed limits should be modeled explicitly.
     * @param stream
     * @return */
    public static ContinuousDistDoubleScalar.Rel<Speed, SpeedUnit>
            cars130kmh(final StreamInterface stream) {

        InterpolatedEmpiricalDistribution dist =
            new InterpolatedEmpiricalDistribution(
                new Number[] {80, 98, 110, 130, 135, 143, 155, 170},
                new double[] {0.0, 0.03, 0.10, 0.68, 0.80, 0.91, 0.97, 1.0}
            );

        return new ContinuousDistDoubleScalar.Rel<>(
            new DistEmpiricalInterpolated(stream, dist),
            SpeedUnit.KM_PER_HOUR
        );
    }

    /** Cars on unrestricted german motorways.
     * @param stream
     * @return */
    public static ContinuousDistDoubleScalar.Rel<Speed, SpeedUnit>
            carsUnrestricted(final StreamInterface stream) {

        InterpolatedEmpiricalDistribution dist =
            new InterpolatedEmpiricalDistribution(
                new Number[] {80, 99, 109, 121, 131, 149, 165, 185, 205},
                new double[] {0.0, 0.03, 0.10, 0.26, 0.47, 0.80, 0.93, 0.99, 1.0}
            );

        return new ContinuousDistDoubleScalar.Rel<>(
            new DistEmpiricalInterpolated(stream, dist),
            SpeedUnit.KM_PER_HOUR
        );
    }

    /* ====================================================================== */
    /**
     * The following passenger–car distributions are conceptually derived from empirical
     * free–flow speed observations published in:
     *
     *   Weyland, C. M. L. (2023).
     *   *Microscopic traffic flow simulation of motorways with
     *   variable speed limit control* (Doctoral dissertation,
     *   Karlsruhe Institute of Technology).
     *   https://doi.org/10.5445/IR/1000162768
     * Passenger cars under a posted speed limit of 80 km/h,
     * medium traffic density.
     */

    /* ====================================================================== */
    /* Cars – Speed Limit 80 km/h                                             */
    /* ====================================================================== */

    /** Passenger cars under a posted speed limit of 80 km/h,
     * Represents a moderately constrained environment with some drivers still
     * choosing speeds somewhat above the posted limit.
     */
    public static ContinuousDistDoubleScalar.Rel<Speed, SpeedUnit>
            carsLimit80_DensityMedium(final StreamInterface stream)
    {
        InterpolatedEmpiricalDistribution dist =
            new InterpolatedEmpiricalDistribution(
                new Number[] {60, 80, 90, 100, 110, 120, 130, 140, 160, 180, 200},
                new double[] {0.00, 0.04, 0.12, 0.23, 0.41, 0.60, 0.79, 0.88, 0.95, 0.99, 1.00}
            );
        return new ContinuousDistDoubleScalar.Rel<>(
                new DistEmpiricalInterpolated(stream, dist),
                SpeedUnit.KM_PER_HOUR);
    }

    /**
     * Passenger cars under a posted speed limit of 80 km/h,
     * high traffic density.
     *
     * Compared to the medium–density case, the distribution shifts further
     * toward lower desired speeds due to increased interaction and constraints.
     */
    public static ContinuousDistDoubleScalar.Rel<Speed, SpeedUnit>
            carsLimit80_DensityHigh(final StreamInterface stream)
    {
        InterpolatedEmpiricalDistribution dist =
            new InterpolatedEmpiricalDistribution(
                new Number[] {60, 80, 90, 100, 110, 120, 130, 140, 160, 180, 200},
                new double[] {0.00, 0.055, 0.14, 0.27, 0.41, 0.60, 0.79, 0.88, 0.95, 0.99, 1.00}
            );
        return new ContinuousDistDoubleScalar.Rel<>(
                new DistEmpiricalInterpolated(stream, dist),
                SpeedUnit.KM_PER_HOUR);
    }

    /* ====================================================================== */
    /* Cars – Speed Limit 100 km/h                                            */
    /* ====================================================================== */

    /** Passenger cars, 100 km/h limit, medium density. */
    public static ContinuousDistDoubleScalar.Rel<Speed, SpeedUnit>
            carsLimit100_DensityMedium(final StreamInterface stream)
    {
        InterpolatedEmpiricalDistribution dist =
            new InterpolatedEmpiricalDistribution(
                new Number[] {70, 80, 90, 100, 110, 120, 130, 140, 160, 180, 200},
                new double[] {0.00, 0.02, 0.08, 0.20, 0.40, 0.60, 0.78, 0.88, 0.96, 0.985, 1.00}
            );
        return new ContinuousDistDoubleScalar.Rel<>(
                new DistEmpiricalInterpolated(stream, dist),
                SpeedUnit.KM_PER_HOUR);
    }

    /** Passenger cars, 100 km/h limit, high density. */
    public static ContinuousDistDoubleScalar.Rel<Speed, SpeedUnit>
            carsLimit100_DensityHigh(final StreamInterface stream)
    {
        InterpolatedEmpiricalDistribution dist =
            new InterpolatedEmpiricalDistribution(
                new Number[] {70, 80, 90, 100, 110, 120, 130, 140, 160, 180, 200},
                new double[] {0.00, 0.05, 0.14, 0.27, 0.45, 0.63, 0.80, 0.89, 0.96, 0.985, 1.00}
            );
        return new ContinuousDistDoubleScalar.Rel<>(
                new DistEmpiricalInterpolated(stream, dist),
                SpeedUnit.KM_PER_HOUR);
    }

    /* ====================================================================== */
    /* Cars – Speed Limit 120 km/h                                            */
    /* ====================================================================== */

    /** Passenger cars, 120 km/h limit, low density (free flow). */
    public static ContinuousDistDoubleScalar.Rel<Speed, SpeedUnit>
            carsLimit120_DensityLow(final StreamInterface stream)
    {
        InterpolatedEmpiricalDistribution dist =
            new InterpolatedEmpiricalDistribution(
                new Number[] {
                    70, 80, 90, 100, 110, 120, 130, 140,
                    150, 160, 170, 180, 190, 200
                },
                new double[] {
                    0.00, 0.012, 0.036, 0.083, 0.168, 0.322,
                    0.519, 0.691, 0.819, 0.903, 0.945, 0.963,
                    0.975, 0.987
                }
            );
        return new ContinuousDistDoubleScalar.Rel<>(
                new DistEmpiricalInterpolated(stream, dist),
                SpeedUnit.KM_PER_HOUR);
    }

    /** Passenger cars, 120 km/h limit, medium density. */
    public static ContinuousDistDoubleScalar.Rel<Speed, SpeedUnit>
            carsLimit120_DensityMedium(final StreamInterface stream)
    {
        InterpolatedEmpiricalDistribution dist =
            new InterpolatedEmpiricalDistribution(
                new Number[] {
                    70, 80, 90, 100, 110, 120, 130, 140,
                    150, 160, 170, 180, 190, 200
                },
                new double[] {
                    0.00, 0.015, 0.045, 0.095, 0.185, 0.340,
                    0.535, 0.705, 0.830, 0.910, 0.950, 0.967,
                    0.978, 0.988
                }
            );
        return new ContinuousDistDoubleScalar.Rel<>(
                new DistEmpiricalInterpolated(stream, dist),
                SpeedUnit.KM_PER_HOUR);
    }

    /** Passenger cars, 120 km/h limit, high density. */
    public static ContinuousDistDoubleScalar.Rel<Speed, SpeedUnit>
            carsLimit120_DensityHigh(final StreamInterface stream)
    {
        InterpolatedEmpiricalDistribution dist =
            new InterpolatedEmpiricalDistribution(
                new Number[] {
                    70, 80, 90, 100, 110, 120, 130, 140,
                    150, 160, 170, 180, 190, 200
                },
                new double[] {
                    0.00, 0.020, 0.055, 0.110, 0.200, 0.360,
                    0.555, 0.720, 0.840, 0.915, 0.955, 0.970,
                    0.980, 0.989
                }
            );
        return new ContinuousDistDoubleScalar.Rel<>(
                new DistEmpiricalInterpolated(stream, dist),
                SpeedUnit.KM_PER_HOUR);
    }

    /* ====================================================================== */
    /* Cars – Speed Limit 140 km/h                                            */
    /* ====================================================================== */

    /** Passenger cars, 140 km/h limit, low density (free flow). */
    public static ContinuousDistDoubleScalar.Rel<Speed, SpeedUnit>
            carsLimit140_DensityLow(final StreamInterface stream)
    {
        InterpolatedEmpiricalDistribution dist =
            new InterpolatedEmpiricalDistribution(
                new Number[] {80, 90, 100, 110, 120, 130, 140, 150,
                              160, 170, 180, 190, 200},
                new double[] {0.011, 0.036, 0.083, 0.156, 0.294,
                              0.448, 0.593, 0.721, 0.824, 0.893,
                              0.939, 0.959, 0.973}
            );
        return new ContinuousDistDoubleScalar.Rel<>(
                new DistEmpiricalInterpolated(stream, dist),
                SpeedUnit.KM_PER_HOUR);
    }

    /** Passenger cars, 140 km/h limit, medium density. */
    public static ContinuousDistDoubleScalar.Rel<Speed, SpeedUnit>
            carsLimit140_DensityMedium(final StreamInterface stream)
    {
        InterpolatedEmpiricalDistribution dist =
            new InterpolatedEmpiricalDistribution(
                new Number[] {80, 90, 100, 110, 120, 130, 140, 150,
                              160, 170, 180, 190, 200},
                new double[] {0.011, 0.037, 0.095, 0.166, 0.325,
                              0.520, 0.673, 0.772, 0.839, 0.889,
                              0.923, 0.944, 0.960}
            );
        return new ContinuousDistDoubleScalar.Rel<>(
                new DistEmpiricalInterpolated(stream, dist),
                SpeedUnit.KM_PER_HOUR);
    }

    /** Passenger cars, 140 km/h limit, high density. */
    public static ContinuousDistDoubleScalar.Rel<Speed, SpeedUnit>
            carsLimit140_DensityHigh(final StreamInterface stream)
    {
        InterpolatedEmpiricalDistribution dist =
            new InterpolatedEmpiricalDistribution(
                new Number[] {80, 90, 100, 110, 120, 130, 140, 150,
                              160, 170, 180, 190, 200},
                new double[] {0.017, 0.059, 0.141, 0.230, 0.384,
                              0.583, 0.735, 0.828, 0.893, 0.936,
                              0.962, 0.977, 0.987}
            );
        return new ContinuousDistDoubleScalar.Rel<>(
                new DistEmpiricalInterpolated(stream, dist),
                SpeedUnit.KM_PER_HOUR);
    }

    /**
     * Desired speed distribution for passenger cars based on Hoogendoorn (European freeways).
     * Normal distribution: mean 120 km/h, sd 14 km/h, truncated at 5% and 95%.
     * Normal distribution: mean 90 km/h, sd 10 km/h, truncated at 5% and 95%.
     * Source: Hoogendoorn, S. P. (2005). Vehicle-Type and Lane–Specific Free Speed Distributions on Motorways:
     * A Novel Estimation Approach Using Censored Observations: A Novel Estimation Approach Using Censored Observations.
     * Transportation Research Record: Journal of the Transportation Research Board, 1934(1), 148-156.
     * https://doi.org/10.1177/0361198105193400116
     * @param stream Random stream
     * @return desired speed distribution (km/h)
     */
    public static ContinuousDistDoubleScalar.Rel<Speed, SpeedUnit>
            hoogendoornCars(final StreamInterface stream)
    {
        // 5% and 95% quantile truncation
        double vMin = 120 - 1.645 * 14; // ≈ 97 km/h
        double vMax = 120 + 1.645 * 14; // ≈ 143 km/h

        // Discretize speed values (11 points between vMin and vMax)
        Number[] values = new Number[11];
        double[] cdf = new double[11];

        for (int i = 0; i < 11; i++)
        {
            double v = vMin + i * (vMax - vMin) / 10.0;
            values[i] = v;

            // Normalized CDF between 0.05 and 0.95
            double z = (v - 120) / 14.0;
            double Phi = 0.5 * (1 + erf(z / Math.sqrt(2)));   // Standard normal CDF
            double PhiClipped = (Phi - 0.05) / 0.90;           // Rescale to [0,1]
            cdf[i] = Math.max(0.0, Math.min(1.0, PhiClipped));
        }

        InterpolatedEmpiricalDistribution dist =
            new InterpolatedEmpiricalDistribution(values, cdf);

        return new ContinuousDistDoubleScalar.Rel<>(
                new DistEmpiricalInterpolated(stream, dist),
                SpeedUnit.KM_PER_HOUR);
    }

    /**
     * Desired speed distribution for heavy-duty vehicles (Hoogendoorn).
     * Normal distribution: mean 90 km/h, sd 10 km/h, truncated at 5% and 95%.
     * Source: Hoogendoorn, S. P. (2005). Vehicle-Type and Lane–Specific Free Speed Distributions on Motorways:
     * A Novel Estimation Approach Using Censored Observations: A Novel Estimation Approach Using Censored Observations.
     * Transportation Research Record: Journal of the Transportation Research Board, 1934(1), 148-156.
     * https://doi.org/10.1177/0361198105193400116
     *
     * @param stream Random stream
     * @return desired speed distribution (km/h)
     */
    public static ContinuousDistDoubleScalar.Rel<Speed, SpeedUnit>
            hoogendoornTrucks(final StreamInterface stream)
    {
        double vMin = 90 - 1.645 * 10;  // ≈ 73 km/h
        double vMax = 90 + 1.645 * 10;  // ≈ 106 km/h

        Number[] values = new Number[11];
        double[] cdf = new double[11];

        for (int i = 0; i < 11; i++)
        {
            double v = vMin + i * (vMax - vMin) / 10.0;
            values[i] = v;

            double z = (v - 90) / 10.0;
            double Phi = 0.5 * (1 + erf(z / Math.sqrt(2)));
            double PhiClipped = (Phi - 0.05) / 0.90;
            cdf[i] = Math.max(0.0, Math.min(1.0, PhiClipped));
        }

        InterpolatedEmpiricalDistribution dist =
            new InterpolatedEmpiricalDistribution(values, cdf);

        return new ContinuousDistDoubleScalar.Rel<>(
                new DistEmpiricalInterpolated(stream, dist),
                SpeedUnit.KM_PER_HOUR);
    }

    /** Helper: error function approximation */
    private static double erf(final double x)
    {
        // Numerical approximation (Abramowitz/Stegun)
        double t = 1.0 / (1.0 + 0.5 * Math.abs(x));
        double tau =
            t * Math.exp(-x*x - 1.26551223 +
                  t * (1.00002368 +
                  t * (0.37409196 +
                  t * (0.09678418 +
                  t * (-0.18628806 +
                  t * (0.27886807 +
                  t * (-1.13520398 +
                  t * (1.48851587 +
                  t * (-0.82215223 +
                  t * 0.17087277)))))))));

        return x >= 0 ? 1 - tau : tau - 1;
    }



    /**
     * @param stream
     * @return
     */
    public static ContinuousDistDoubleScalar.Rel<Speed, SpeedUnit> trucks(final StreamInterface stream) {
             return new ContinuousDistDoubleScalar.Rel<>(new DistUniform(stream, 80.0, 100.0), SpeedUnit.KM_PER_HOUR);
    }




}
