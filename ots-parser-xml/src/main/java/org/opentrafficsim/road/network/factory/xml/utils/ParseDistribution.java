package org.opentrafficsim.road.network.factory.xml.utils;

import org.djunits.unit.Unit;
import org.djunits.value.vdouble.scalar.base.DoubleScalarRel;
import org.djutils.eval.Eval;
import org.opentrafficsim.core.units.distributions.ContinuousDistDoubleScalar;
import org.opentrafficsim.road.network.factory.xml.XmlParserException;
import org.opentrafficsim.xml.generated.ConstantDistType;
import org.opentrafficsim.xml.generated.DiscreteDistType;

import nl.tudelft.simulation.dsol.experiment.StreamInformation;
import nl.tudelft.simulation.jstats.distributions.DistBernoulli;
import nl.tudelft.simulation.jstats.distributions.DistBeta;
import nl.tudelft.simulation.jstats.distributions.DistBinomial;
import nl.tudelft.simulation.jstats.distributions.DistConstant;
import nl.tudelft.simulation.jstats.distributions.DistContinuous;
import nl.tudelft.simulation.jstats.distributions.DistDiscrete;
import nl.tudelft.simulation.jstats.distributions.DistDiscreteConstant;
import nl.tudelft.simulation.jstats.distributions.DistDiscreteUniform;
import nl.tudelft.simulation.jstats.distributions.DistErlang;
import nl.tudelft.simulation.jstats.distributions.DistExponential;
import nl.tudelft.simulation.jstats.distributions.DistGamma;
import nl.tudelft.simulation.jstats.distributions.DistGeometric;
import nl.tudelft.simulation.jstats.distributions.DistLogNormal;
import nl.tudelft.simulation.jstats.distributions.DistNegBinomial;
import nl.tudelft.simulation.jstats.distributions.DistNormal;
import nl.tudelft.simulation.jstats.distributions.DistNormalTrunc;
import nl.tudelft.simulation.jstats.distributions.DistPearson5;
import nl.tudelft.simulation.jstats.distributions.DistPearson6;
import nl.tudelft.simulation.jstats.distributions.DistPoisson;
import nl.tudelft.simulation.jstats.distributions.DistTriangular;
import nl.tudelft.simulation.jstats.distributions.DistUniform;
import nl.tudelft.simulation.jstats.distributions.DistWeibull;
import nl.tudelft.simulation.jstats.streams.StreamInterface;

/**
 * Parse a distribution from text to a distribution.
 * <p>
 * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public final class ParseDistribution
{
    /** Utility class. */
    private ParseDistribution()
    {
        // do not instantiate
    }

    /**
     * Parse a relative unit distribution, e.g. <code>UNIFORM(1, 3) m</code>.
     * @param streamMap StreamInformation; the map with streams from the RUN tag
     * @param distribution ConstantDistType; the tag to parse, a sub type of ConstantDistType
     * @param unit U; unit
     * @param eval Eval; expression evaluator.
     * @return a typed continuous random distribution.
     * @throws XmlParserException in case of a parse error.
     */
    public static <T extends DoubleScalarRel<U, T>, U extends Unit<U>> ContinuousDistDoubleScalar.Rel<T, U> parseContinuousDist(
            final StreamInformation streamMap, final ConstantDistType distribution, final U unit, final Eval eval)
            throws XmlParserException
    {
        return new ContinuousDistDoubleScalar.Rel<T, U>(makeDistContinuous(streamMap, distribution, eval), unit);
    }

    /**
     * Parse a discrete distribution.
     * @param streamMap StreamInformation; map with stream information
     * @param distType DiscreteDistType; the distribution to parse
     * @param eval Eval; expression evaluator.
     * @return the generated distribution.
     * @throws XmlParserException in case distribution unknown or parameter number does not match.
     */
    public static DistDiscrete makeDistDiscrete(final StreamInformation streamMap, final DiscreteDistType distType,
            final Eval eval) throws XmlParserException
    {
        StreamInterface stream = ParseUtil.findStream(streamMap, distType.getRandomStream(), eval);
        if (distType.getBernoulliI() != null)
        {
            return new DistBernoulli(stream, distType.getBernoulliI().getP().get(eval));
        }
        else if (distType.getBinomial() != null)
        {
            return new DistBinomial(stream, distType.getBinomial().getN().get(eval), distType.getBinomial().getP().get(eval));
        }
        else if (distType.getConstant() != null)
        {
            return new DistDiscreteConstant(stream, distType.getConstant().getC().get(eval));
        }
        else if (distType.getGeometric() != null)
        {
            return new DistGeometric(stream, distType.getGeometric().getP().get(eval));
        }
        else if (distType.getNegBinomial() != null)
        {
            return new DistNegBinomial(stream, (int) distType.getNegBinomial().getN().get(eval),
                    distType.getGeometric().getP().get(eval));
        }
        else if (distType.getPoisson() != null)
        {
            return new DistPoisson(stream, distType.getPoisson().getLambda().get(eval));
        }
        else if (distType.getUniform() != null)
        {
            return new DistDiscreteUniform(stream, distType.getUniform().getMin().get(eval),
                    distType.getUniform().getMax().get(eval));
        }
        throw new XmlParserException("makeDistDiscrete - unknown distribution function " + distType);
    }

    /**
     * Parse a continuous distribution.
     * @param streamMap StreamInformation; map with stream information
     * @param distType ConstantDistType; the distribution to parse
     * @param eval Eval; expression evaluator.
     * @return the generated distribution.
     * @throws XmlParserException in case distribution unknown or parameter number does not match.
     */
    public static DistContinuous makeDistContinuous(final StreamInformation streamMap, final ConstantDistType distType,
            final Eval eval) throws XmlParserException
    {
        StreamInterface stream = ParseUtil.findStream(streamMap, distType.getRandomStream(), eval);
        if (distType.getConstant() != null)
        {
            return new DistConstant(stream, distType.getConstant().getC().get(eval));
        }
        else if (distType.getExponential() != null)
        {
            return new DistExponential(stream, distType.getExponential().getLambda().get(eval));
        }
        else if (distType.getTriangular() != null)
        {
            return new DistTriangular(stream, distType.getTriangular().getMin().get(eval),
                    distType.getTriangular().getMode().get(eval), distType.getTriangular().getMax().get(eval));
        }
        else if (distType.getNormal() != null)
        {
            return new DistNormal(stream, distType.getNormal().getMu().get(eval), distType.getNormal().getSigma().get(eval));
        }
        else if (distType.getNormal() != null)
        {
            return new DistNormalTrunc(stream, distType.getNormalTrunc().getMu().get(eval),
                    distType.getNormalTrunc().getSigma().get(eval), distType.getNormalTrunc().getMin().get(eval),
                    distType.getNormalTrunc().getMax().get(eval));
        }
        else if (distType.getBeta() != null)
        {
            return new DistBeta(stream, distType.getBeta().getAlpha1().get(eval), distType.getBeta().getAlpha2().get(eval));
        }
        else if (distType.getErlang() != null)
        {
            return new DistErlang(stream, distType.getErlang().getMean().get(eval), distType.getErlang().getK().get(eval));
        }
        else if (distType.getGamma() != null)
        {
            return new DistGamma(stream, distType.getGamma().getAlpha().get(eval), distType.getGamma().getBeta().get(eval));
        }
        else if (distType.getLogNormal() != null)
        {
            return new DistLogNormal(stream, distType.getLogNormal().getMu().get(eval),
                    distType.getLogNormal().getSigma().get(eval));
        }
        else if (distType.getPearson5() != null)
        {
            return new DistPearson5(stream, distType.getPearson5().getAlpha().get(eval),
                    distType.getPearson5().getBeta().get(eval));
        }
        else if (distType.getPearson6() != null)
        {
            return new DistPearson6(stream, distType.getPearson6().getAlpha1().get(eval),
                    distType.getPearson6().getAlpha2().get(eval), distType.getPearson6().getBeta().get(eval));
        }
        else if (distType.getUniform() != null)
        {
            return new DistUniform(stream, distType.getUniform().getMin().get(eval), distType.getUniform().getMax().get(eval));
        }
        else if (distType.getWeibull() != null)
        {
            return new DistWeibull(stream, distType.getWeibull().getAlpha().get(eval),
                    distType.getWeibull().getBeta().get(eval));
        }
        throw new XmlParserException("makeDistContinuous - unknown distribution function " + distType);
    }

}
