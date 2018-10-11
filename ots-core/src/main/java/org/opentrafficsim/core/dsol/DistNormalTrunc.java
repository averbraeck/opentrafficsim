package org.opentrafficsim.core.dsol;

import static nl.tudelft.simulation.jstats.distributions.DistNormal.CUMULATIVE_NORMAL_PROBABILITIES;

import java.util.Locale;

import nl.tudelft.simulation.jstats.distributions.DistContinuous;
import nl.tudelft.simulation.jstats.streams.MersenneTwister;
import nl.tudelft.simulation.jstats.streams.StreamInterface;

/**
 * The Normal Truncated distribution. For more information on the normal distribution see
 * <a href="http://mathworld.wolfram.com/NormalDistribution.html"> http://mathworld.wolfram.com/NormalDistribution.html </a>
 * <p>
 * This version of the normal distribution uses the numerically approached inverse cumulative distribution.
 * <p>
 * (c) copyright 2002-2018 <a href="http://www.simulation.tudelft.nl">Delft University of Technology </a>, the Netherlands. <br>
 * See for project information <a href="http://www.simulation.tudelft.nl"> www.simulation.tudelft.nl </a> <br>
 * License of use: <a href="http://www.gnu.org/copyleft/lesser.html">Lesser General Public License (LGPL) </a>, no warranty.
 * @author <a href="mailto:a.verbraeck@tudelft.nl"> Alexander Verbraeck </a> <br>
 *         <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class DistNormalTrunc extends DistContinuous
{
    /** */
    private static final long serialVersionUID = 1L;

    /** mu refers to the mean of the normal distribution. */
    private final double mu;

    /** mu refers to the mean of the normal distribution. */
    private final double sigma;

    /** minimum x-value of the distribution. */
    private final double min;

    /** maximum x-value of the distribution. */
    private final double max;

    /** cumulative distribution value of the minimum. */
    private final double cumulMin;

    /** cumulative distribution value difference between max and min. */
    private final double cumulDiff;

    /** factor on probability density to normalize to 1. */
    private final double probDensFactor;

    /**
     * constructs a normal distribution with mu=0 and sigma=1. Errors of various types, e.g., in the impact point of a bomb;
     * quantities that are the sum of a large number of other quantities by the virtue of the central limit theorem.
     * @param stream StreamInterface; the numberstream
     * @param min double; minimum x-value of the distribution
     * @param max double; maximum x-value of the distribution
     */
    public DistNormalTrunc(final StreamInterface stream, final double min, final double max)
    {
        this(stream, 0.0, 1.0, min, max);
    }

    /**
     * constructs a normal distribution with mu and sigma.
     * @param stream StreamInterface; the numberstream
     * @param mu double; the medium
     * @param sigma double; the standard deviation
     * @param min double; minimum x-value of the distribution
     * @param max double; maximum x-value of the distribution
     */
    public DistNormalTrunc(final StreamInterface stream, final double mu, final double sigma, final double min,
            final double max)
    {
        super(stream);
        if (max < min)
        {
            throw new IllegalArgumentException("Error Normal Truncated - max < min");
        }
        this.mu = mu;
        this.sigma = sigma;
        this.min = min;
        this.max = max;
        this.cumulMin = getCumulativeProbabilityNotTruncated(min);
        this.cumulDiff = getCumulativeProbabilityNotTruncated(max) - this.cumulMin;
        this.probDensFactor = 1.0 / this.cumulDiff;
    }

    /** {@inheritDoc} */
    @Override
    public double draw()
    {
        return getInverseCumulativeProbabilityNotTruncated(this.cumulMin + this.cumulDiff * this.stream.nextDouble());
    }

    /**
     * returns the cumulative probability of the x-value.
     * @param x double; the observation x
     * @return double the cumulative probability
     */
    public double getCumulativeProbability(final double x)
    {
        if (x < this.min)
        {
            return 0.0;
        }
        if (x > this.max)
        {
            return 1.0;
        }
        return (getCumulativeProbabilityNotTruncated(x) - this.cumulMin) * this.probDensFactor;
    }

    /**
     * returns the cumulative probability of the x-value.
     * @param x double; the observation x
     * @return double the cumulative probability
     */
    private double getCumulativeProbabilityNotTruncated(final double x)
    {
        double z = (x - this.mu) / this.sigma * 100;
        double absZ = Math.abs(z);
        int intZ = (int) absZ;
        double f = 0.0;
        if (intZ >= 1000)
        {
            intZ = 999;
            f = 1.0;
        }
        else
        {
            f = absZ - intZ;
        }
        if (z >= 0)
        {
            return (1 - f) * CUMULATIVE_NORMAL_PROBABILITIES[intZ] + f * CUMULATIVE_NORMAL_PROBABILITIES[intZ + 1];
        }
        return 1 - ((1 - f) * CUMULATIVE_NORMAL_PROBABILITIES[intZ] + f * CUMULATIVE_NORMAL_PROBABILITIES[intZ + 1]);
    }

    /**
     * returns the x-value of the given cumulativePropability.
     * @param cumulativeProbability double; reflects cum prob
     * @return double the inverse cumulative probability
     */
    public double getInverseCumulativeProbability(final double cumulativeProbability)
    {
        if (cumulativeProbability < 0 || cumulativeProbability > 1)
        {
            throw new IllegalArgumentException("1<cumulativeProbability<0 ?");
        }
        /*
         * For extreme cases we return the min and max directly. The method getInverseCumulativeProbabilityNotTruncated() can
         * only return values from "mu - 10*sigma" to "mu + 10*sigma". If min or max is beyond these values, those values would
         * result erroneously. For any cumulative probability that is slightly above 0.0 or slightly below 1.0, values in the
         * range from "mu - 10*sigma" to "mu + 10*sigma" will always result.
         */
        if (cumulativeProbability == 0.0)
        {
            return this.min;
        }
        if (cumulativeProbability == 1.0)
        {
            return this.max;
        }
        return getInverseCumulativeProbabilityNotTruncated(this.cumulMin + cumulativeProbability * this.cumulDiff);
    }

    /** {@inheritDoc} */
    @Override
    public double probDensity(final double x)
    {
        if (x < this.min || x > this.max)
        {
            return 0.0;
        }
        return this.probDensFactor / (Math.sqrt(2 * Math.PI * Math.pow(this.sigma, 2)))
                * Math.exp(-1 * Math.pow(x - this.mu, 2) / (2 * Math.pow(this.sigma, 2)));
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "NormalTrunc(" + this.mu + "," + this.sigma + "," + this.min + "," + this.max + ")";
    }

    /**
     * Test.
     * @param args String[]; args
     */
    public static void main(final String[] args)
    {
        StreamInterface stream = new MersenneTwister();
        double mu = 2.0;
        double sigma = 3.0;
        double min = -5.0;
        double max = 4.0;
        DistNormalTrunc dist = new DistNormalTrunc(stream, mu, sigma, min, max);

        System.out.println("<< probability density >>");
        double sum = 0.0;
        double step = (max - min) / 96;
        for (double x = min - 2 * step; x <= max + 2 * step; x += step)
        {
            double p = dist.probDensity(x);
            System.out.println(String.format(Locale.GERMAN, "%.8f;%.8f", x, p));
            sum += p * step;
        }
        System.out.println(String.format(Locale.GERMAN, "Approx. sum = %.8f", sum));
        System.out.println("");

        System.out.println("<< cumulative density >>");
        for (double x = min - 2 * step; x <= max + 2 * step; x += step)
        {
            double c = dist.getCumulativeProbability(x);
            System.out.println(String.format(Locale.GERMAN, "%.8f;%.8f", x, c));
        }
        System.out.println("");

        System.out.println("<< inverse cumulative density >>");
        for (double c = 0.0; c < 1.005; c += 0.01)
        {
            double x = dist.getInverseCumulativeProbability(Math.min(c, 1.0)); // want to include 1.0, also if 1.0000000000001
            System.out.println(String.format(Locale.GERMAN, "%.8f;%.8f", c, x));
        }
        System.out.println("");

        System.out.println("<< 10000 random numbers. >>");
        for (int i = 1; i < 10000; i++)
        {
            System.out.println(String.format(Locale.GERMAN, "%,8f", dist.draw()));
        }

    }

    /**
     * returns the x-value of the given cumulativePropability.
     * @param cumulativeProbability double; reflects cum prob
     * @return double the inverse cumulative probability
     */
    private double getInverseCumulativeProbabilityNotTruncated(final double cumulativeProbability)
    {
        if (cumulativeProbability < 0 || cumulativeProbability > 1)
        {
            throw new IllegalArgumentException("1<cumulativeProbability<0 ?");
        }
        boolean located = false;
        double prob = cumulativeProbability;
        if (cumulativeProbability < 0.5)
        {
            prob = 1 - cumulativeProbability;
        }
        int i = 0;
        double f = 0.0;
        while (!located)
        {
            if (CUMULATIVE_NORMAL_PROBABILITIES[i] < prob && CUMULATIVE_NORMAL_PROBABILITIES[i + 1] >= prob)
            {
                located = true;
                if (CUMULATIVE_NORMAL_PROBABILITIES[i] < CUMULATIVE_NORMAL_PROBABILITIES[i + 1])
                {
                    f = (prob - CUMULATIVE_NORMAL_PROBABILITIES[i])
                            / (CUMULATIVE_NORMAL_PROBABILITIES[i + 1] - CUMULATIVE_NORMAL_PROBABILITIES[i]);
                }
            }
            else
            {
                i++;
            }
        }
        if (cumulativeProbability < 0.5)
        {
            return this.mu - ((f + i) / 100.0) * this.sigma;
        }
        return ((f + i) / 100.0) * this.sigma + this.mu;
    }

}
