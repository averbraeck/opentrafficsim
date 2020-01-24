package org.opentrafficsim.road.gtu.generator.headway;

import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.core.distributions.Generator;
import org.opentrafficsim.core.distributions.ProbabilityException;

import nl.tudelft.simulation.dsol.simulators.SimulatorInterface;
import nl.tudelft.simulation.jstats.distributions.DistNormal;
import nl.tudelft.simulation.jstats.streams.StreamInterface;

/**
 * Headway generation based on {@code Arrivals}.
 * <p>
 * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 13 dec. 2017 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class ArrivalsHeadwayGenerator implements Generator<Duration>
{

    /** Arrivals. */
    private final Arrivals arrivals;

    /** Simulator. */
    private final SimulatorInterface.TimeDoubleUnit simulator;

    /** Random stream to draw headway. */
    private final StreamInterface stream;

    /** Random headway generator. */
    private final HeadwayDistribution distribution;

    /** First GTU. */
    private boolean first = true;

    /**
     * @param arrivals Arrivals; arrivals
     * @param simulator SimulatorInterface.TimeDoubleUnit; simulator
     * @param stream StreamInterface; random stream to draw headway
     * @param distribution HeadwayDistribution; random headway distribution
     */
    public ArrivalsHeadwayGenerator(final Arrivals arrivals, final SimulatorInterface.TimeDoubleUnit simulator,
            final StreamInterface stream, final HeadwayDistribution distribution)
    {
        this.arrivals = arrivals;
        this.simulator = simulator;
        this.stream = stream;
        this.distribution = distribution;
    }

    /**
     * Returns a new headway {@code h} assuming that the previous vehicle arrived at the current time {@code t0}. The vehicle
     * thus arrives at {@code t1 = t0 + h}. This method guarantees that no vehicle arrives during periods where demand is zero,
     * while maintaining random headways based on average demand over a certain time period.<br>
     * <br>
     * The general method is to find {@code h} such that the integral of the demand pattern {@code D} from {@code t0} until
     * {@code t1} equals {@code r}: &#931;{@code D(t0 > t1) = r}. One can think of {@code r} as being 1 and representing an
     * additional vehicle to arrive. The headway {@code h} that results correlates directly to the mean demand between
     * {@code t0} and {@code t1}.<br>
     * <br>
     * The value of {@code r} always has a mean of 1, but may vary between specific vehicle arrivals depending on the headway
     * distribution. When assuming constant headways for any given demand level, {@code r} always equals 1. For exponentially
     * distributed headways {@code r} may range anywhere between 0 and infinity.<br>
     * <br>
     * This usage of {@code r} guarantees that no vehicles arrive during periods with 0 demand. For example:
     * <ul>
     * <li>Suppose we have 0 demand between 300s and 400s.</li>
     * <li>The previous vehicle was generated at 299s.</li>
     * <li>The demand at 299s equals 1800veh/h (1 veh per 2s).</li>
     * <li>For both constant and exponentially distributed headways, the expected next vehicle arrival based on this demand
     * value alone would be 299 + 2 = 301s. This is within the 0-demand period and should not happen. It's also not
     * theoretically sound, as the demand from 299s until 301s is not 1800veh/h on average.</li>
     * <li>Using integration we find that the surface of demand from 299s until 300s equals 0.5 veh for stepwise demand, and
     * 0.25 veh for linear demand. Consequently, the vehicle will not arrive until later slices integrate to an additional 0.5
     * veh or 0.75 veh respectively. This additional surface under the demand curve is only found after 400s.</li>
     * <li>In case the exponential headway distribution would have resulted in {@code r} &lt; 0.5 (stepwise demand) or 0.25
     * (linear demand), a vehicle will simply arrive between 299s and 300s.</li>
     * </ul>
     * <br>
     * @return Duration; new headway
     * @throws ProbabilityException if the stored collection is empty
     * @throws ParameterException in case of a parameter exception
     */
    @Override
    public Duration draw() throws ProbabilityException, ParameterException
    {
        Time now = this.simulator.getSimulatorTime();
        // initial slice times and frequencies
        Time t1 = now;
        double f1 = this.arrivals.getFrequency(t1, true).si;
        Time t2 = this.arrivals.nextTimeSlice(t1);
        if (t2 == null)
        {
            return null; // no new vehicle
        }
        double f2 = this.arrivals.getFrequency(t2, false).si;
        // next vehicle's random factor
        double rem = this.distribution.draw(this.stream);
        if (this.first)
        {
            // first headway may be partially in the past, take a random factor
            rem *= this.stream.nextDouble();
            this.first = false;
        }
        // integrate until rem (by reducing it to 0.0, possibly in steps per slice)
        while (rem > 0.0)
        {
            // extrapolate to find 'integration = rem' in this slice giving demand slope, this may beyond the slice length
            double dt = t2.si - t1.si;
            double t;
            double slope = (f2 - f1) / dt;
            if (Math.abs(slope) < 1e-12) // no slope
            {
                if (f1 > 0.0)
                {
                    t = rem / f1; // rem = t * f1, t = rem / f1
                }
                else
                {
                    t = Double.POSITIVE_INFINITY; // no demand in this slice
                }
            }
            else
            {
                // reverse of trapezoidal rule: rem = t * (f1 + (f1 + t * slope)) / 2
                double sqrt = 2 * slope * rem + f1 * f1;
                if (sqrt >= 0.0)
                {
                    t = (-f1 + Math.sqrt(sqrt)) / slope;
                }
                else
                {
                    t = Double.POSITIVE_INFINITY; // not sufficient demand in this slice, with negative slope
                }
            }
            if (t > dt)
            {
                // next slice
                rem -= dt * (f1 + f2) / 2; // subtract integral of this slice using trapezoidal rule
                t1 = t2;
                t2 = this.arrivals.nextTimeSlice(t1);
                if (t2 == null)
                {
                    return null; // no new vehicle
                }
                f1 = this.arrivals.getFrequency(t1, true).si; // we can't use f1 = f2 due to possible steps in demand
                f2 = this.arrivals.getFrequency(t2, false).si;
            }
            else
            {
                // return resulting integration times
                return Duration.instantiateSI(t1.si + t - now.si);
            }
        }
        throw new RuntimeException("Exception while determining headway from Arrivals.");
    }

    /**
     * Headway distribution.
     * <p>
     * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
     * <p>
     * @version $Revision$, $LastChangedDate$, by $Author$, initial version 5 dec. 2017 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
     */
    public interface HeadwayDistribution
    {

        /** Constant headway. */
        HeadwayDistribution CONSTANT = new HeadwayDistribution()
        {
            @Override
            public double draw(final StreamInterface randomStream)
            {
                return 1.0;
            }

            @Override
            public String getName()
            {
                return "CONSTANT";
            }
        };

        /** Exponential headway distribution. */
        HeadwayDistribution EXPONENTIAL = new HeadwayDistribution()
        {
            @Override
            public double draw(final StreamInterface randomStream)
            {
                return -Math.log(randomStream.nextDouble());
            }

            @Override
            public String getName()
            {
                return "EXPONENTIAL";
            }
        };

        /** Uniform headway distribution. */
        HeadwayDistribution UNIFORM = new HeadwayDistribution()
        {
            @Override
            public double draw(final StreamInterface randomStream)
            {
                return 2.0 * randomStream.nextDouble();
            }

            @Override
            public String getName()
            {
                return "UNIFORM";
            }
        };

        /** Triangular headway distribution. */
        HeadwayDistribution TRIANGULAR = new HeadwayDistribution()
        {
            @Override
            public double draw(final StreamInterface randomStream)
            {
                double r = randomStream.nextDouble();
                if (r < .5)
                {
                    return Math.sqrt(r * 2.0);
                }
                return 2.0 - Math.sqrt((1.0 - r) * 2.0);
            }

            @Override
            public String getName()
            {
                return "TRIANGULAR";
            }
        };

        /** Triangular (left side, mean 2/3) and exponential (right side, mean 4/3) headway distribution. */
        HeadwayDistribution TRI_EXP = new HeadwayDistribution()
        {
            @Override
            public double draw(final StreamInterface randomStream)
            {
                double r = randomStream.nextDouble();
                if (r < .5)
                {
                    return Math.sqrt(r * 2.0); // left-hand side of triangular distribution, with mean 2/3
                }
                return 1.0 - Math.log((1.0 - r) * 2.0) / 3.0; // 1 + 1/3, where 1/3 is mean of exponential right-hand side
                // note: 50% with mean 2/3 and 50% with mean 1 + 1/3 gives a mean of 1
            }

            @Override
            public String getName()
            {
                return "TRI_EXP";
            }
        };

        /** Log-normal headway distribution (variance = 1.0). */
        HeadwayDistribution LOGNORMAL = new HeadwayDistribution()
        {
            /** Mu. */
            private final double mu = Math.log(1.0 / Math.sqrt(2.0));

            /** Sigma. */
            private final double sigma = Math.sqrt(Math.log(2.0));

            @Override
            public double draw(final StreamInterface randomStream)
            {
                return Math.exp(new DistNormal(randomStream, this.mu, this.sigma).draw());
            }

            @Override
            public String getName()
            {
                return "LOGNORMAL";
            }
        };

        /**
         * Draws a randomized headway factor. The average value returned is always 1.0. The returned value is applied on the
         * demand pattern by (reversed) integration to derive actual headways.
         * @param randomStream StreamInterface; random number stream
         * @return randomized headway factor
         */
        double draw(StreamInterface randomStream);

        /**
         * Returns the distribution name.
         * @return distribution name
         */
        String getName();

    }

}
