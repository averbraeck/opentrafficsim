package org.opentrafficsim.road.gtu.lane.perception.categories;

import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;

import nl.tudelft.simulation.jstats.distributions.DistNormal;
import nl.tudelft.simulation.jstats.streams.StreamInterface;

/**
 * A numerical update scheme that represents a Wiener process if {@code dt << tau}. A Wiener process is a process that results
 * in random values that follow a Normal distribution with parameters mu and sigma. However, there is a time progression with
 * correlation {@code tau}, such that the process has a tendency to stay close to the previous value. The correlation time
 * {@code tau} is a measure for this tendency. Given sufficient time (and {@code dt << tau}) the overall probability remains
 * equal to the Normal distribution.
 * <p>
 * The Wiener process is typically used for measurement or perception errors in cases where the error of two consecutive time
 * steps is unlikely to deviate much.
 * <p>
 * Treiber, M., A. Kesting, D. Helbing (2006) "Delays, Inaccuracies and Anticipation in Microscopic Traffic Models", Physica A –
 * Statistical Mechanics and its Applications, Vol. 360, Issue 1, pp. 71-88.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 18 okt. 2018 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class WienerProcess extends DistNormal
{

    /** */
    private static final long serialVersionUID = 20181018L;

    /** Simulator. */
    private final OTSSimulatorInterface simulator;

    /** Mean. */
    private final double muW;

    /** Standard deviation. */
    private final double sigmaW;

    /** Correlation time. */
    private final Duration tau;

    /** Value of the standard Wiener process (mu = 0, sigma = 1). */
    private Double value;

    /** Time the value was determined. */
    private Time prevTime;

    /**
     * @param stream StreamInterface; random number stream
     * @param mu double; mean
     * @param sigma double; standard deviation
     * @param tau Duration; correlation time
     * @param simulator OTSSimulatorInterface; simulator
     */
    public WienerProcess(final StreamInterface stream, final double mu, final double sigma, final Duration tau,
            final OTSSimulatorInterface simulator)
    {
        super(stream);
        this.muW = mu;
        this.sigmaW = sigma;
        this.tau = tau;
        this.simulator = simulator;
    }

    /** {@inheritDoc} */
    @Override
    public double draw()
    {
        if (this.value == null)
        {
            this.value = super.draw();
            this.prevTime = this.simulator.getSimulatorTime();
        }
        else if (this.simulator.getSimulatorTime().gt(this.prevTime))
        {
            // calculate next value
            Time now = this.simulator.getSimulatorTime();
            double dt = now.si - this.prevTime.si;
            if (dt <= this.tau.si)
            {
                this.value = Math.exp(-dt / this.tau.si) * this.value + Math.sqrt((2 * dt) / this.tau.si) * super.draw();
            }
            else
            {
                // too long ago, exp may result in extreme values, draw new independent value
                this.value = super.draw();
            }
            this.prevTime = now;
        }
        return this.muW + this.value * this.sigmaW;
    }

}
