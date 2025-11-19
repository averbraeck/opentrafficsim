package org.opentrafficsim.demo.mirova.scenariomanagement.libraries;

import org.djunits.unit.SpeedUnit;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.core.units.distributions.ContinuousDistDoubleScalar;

import nl.tudelft.simulation.jstats.distributions.DistEmpiricalInterpolated;
import nl.tudelft.simulation.jstats.distributions.DistUniform;
import nl.tudelft.simulation.jstats.distributions.empirical.AbstractEmpiricalDistribution;
import nl.tudelft.simulation.jstats.distributions.empirical.InterpolatedEmpiricalDistribution;
import nl.tudelft.simulation.jstats.streams.StreamInterface;

public class DesiredSpeedLibrary {

    /**
     * Do not instantiate.
     */
    private DesiredSpeedLibrary()
    {
        //
    }

    /** German Autobahn driver distribution.
     * @param stream
     * @return */
    public static ContinuousDistDoubleScalar.Rel<Speed, SpeedUnit> germanMotorwayCars(final StreamInterface stream) {

        InterpolatedEmpiricalDistribution dist =
            new InterpolatedEmpiricalDistribution(
                    new Number[] {80, 90, 100, 110, 120, 130, 140, 150, 160, 180, 200},
                    new double[] {0.0, 0.05, 0.10, 0.25, 0.45, 0.65, 0.80, 0.90, 0.96, 0.99, 1.0}
            );

        return new ContinuousDistDoubleScalar.Rel<>(new DistEmpiricalInterpolated(stream, dist), SpeedUnit.KM_PER_HOUR);
    }

    /**
     * @param stream
     * @return
     */
    public static ContinuousDistDoubleScalar.Rel<Speed, SpeedUnit> trucks(final StreamInterface stream) {
             return new ContinuousDistDoubleScalar.Rel<>(new DistUniform(stream, 80.0, 100.0), SpeedUnit.KM_PER_HOUR);
    }



}
