package org.opentrafficsim.road.gtu.generator;

import java.rmi.RemoteException;

import org.djunits.unit.TimeUnit;
import org.djunits.value.ValueException;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Frequency;
import org.djunits.value.vdouble.scalar.Time;
import org.djunits.value.vdouble.vector.FrequencyVector;
import org.djunits.value.vdouble.vector.TimeVector;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.core.distributions.Generator;
import org.opentrafficsim.core.distributions.ProbabilityException;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.road.gtu.strategical.od.Interpolation;

import nl.tudelft.simulation.language.Throw;

/**
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 17 nov. 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
// TODO Link this class to an OD
public class HeadwayGeneratorDemand implements Generator<Duration>
{

    /** Interpolation of demand. */
    private final Interpolation interpolation;

    /** Vector of time. */
    private final TimeVector timeVector;

    /** Vector of flow values. */
    private final FrequencyVector demandVector;

    /** Simulator. */
    private final OTSSimulatorInterface simulator;

    /** Stream name of headway generation. */
    private static final String HEADWAY_STREAM = "headwayGeneration";

    /**
     * @param timeVector a time vector
     * @param demandVector the corresponding demand vector
     * @param simulator the simulator
     */
    public HeadwayGeneratorDemand(final TimeVector timeVector, final FrequencyVector demandVector,
            final OTSSimulatorInterface simulator)
    {
        this(timeVector, demandVector, simulator, Interpolation.STEPWISE);
    }

    /**
     * @param timeVector a time vector
     * @param demandVector the corresponding demand vector
     * @param simulator the simulator
     * @param interpolation interpolation type
     */
    public HeadwayGeneratorDemand(final TimeVector timeVector, final FrequencyVector demandVector,
            final OTSSimulatorInterface simulator, final Interpolation interpolation)
    {
        Throw.whenNull(timeVector, "Time vector may not be null.");
        Throw.whenNull(demandVector, "Demand vector may not be null.");
        Throw.whenNull(simulator, "Simulator may not be null.");
        Throw.whenNull(interpolation, "Interpolation may not be null.");
        try
        {
            Throw.whenNull(simulator.getReplication().getStream(HEADWAY_STREAM),
                    "Could not obtain random stream '" + HEADWAY_STREAM + "'.");
        }
        catch (RemoteException exception)
        {
            throw new RuntimeException("Could not obtain replication.", exception);
        }
        for (int i = 0; i < timeVector.size() - 1; i++)
        {
            try
            {
                Throw.when(timeVector.get(i).ge(timeVector.get(i + 1)), IllegalArgumentException.class,
                        "Time vector is not increasing.");
            }
            catch (ValueException exception)
            {
                throw new RuntimeException(
                        "Value out of range of time vector. Note that HeadwayGenerator does not create a safe copy.",
                        exception);
            }
        }
        Throw.when(timeVector.size() != demandVector.size(), IllegalArgumentException.class,
                "Time and flow vector should be of the same size.");
        Throw.when(timeVector.size() < 2, IllegalArgumentException.class, "Time and flow vector should be at least of size 2.");
        this.timeVector = timeVector;
        this.demandVector = demandVector;
        this.simulator = simulator;
        this.interpolation = interpolation;
    }

    /** {@inheritDoc} */
    @Override
    public final Duration draw() throws ProbabilityException, ParameterException
    {
        Time time = this.simulator.getSimulatorTime().getTime();
        try
        {
            Throw.when(time.lt(this.timeVector.get(0)), IllegalArgumentException.class,
                    "Cannot return a headway at time before first time in vector.");

            // get time period of current time
            int i = 0;
            while (this.timeVector.get(i + 1).lt(time) && i < this.timeVector.size() - 1)
            {
                i++;
            }
            try
            {
                return nextArrival(i, time.minus(this.timeVector.get(i)), 1.0).minus(time);
            }
            catch (RemoteException exception)
            {
                throw new RuntimeException("Could not obtain replication.", exception);
            }
        }
        catch (ValueException exception)
        {
            throw new RuntimeException(
                    "Value out of range of time or demand vector. Note that HeadwayGenerator does not create safe copies.",
                    exception);
        }
    }

    /**
     * Recursive determination of the next arrival time. Each recursion moves to the next time period. This occurs if a randomly
     * determined arrival falls outside of a time period, or when demand in a time period is 0.
     * @param i index of time period
     * @param start reference time from start of period i, pertains to previous arrival, or zero during recursion
     * @param fractionRemaining remaining fraction of headway to apply due to time in earlier time periods
     * @return time of next arrival
     * @throws ValueException in case of an illegal time vector
     * @throws RemoteException in case of not being able to retrieve the replication
     */
    private Time nextArrival(final int i, final Duration start, final double fractionRemaining)
            throws ValueException, RemoteException
    {

        // escape if beyond specified time by infinite next arrival (= no traffic)
        if (i == this.timeVector.size() - 1)
        {
            return new Time(Double.POSITIVE_INFINITY, TimeUnit.BASE);
        }

        // skip zero-demand periods
        if (this.demandVector.get(i).equals(Frequency.ZERO))
        {
            // after zero-demand, the next headway is a random fraction of a random headway as there is no previous arrival
            return nextArrival(i + 1, Duration.ZERO, this.simulator.getReplication().getStream(HEADWAY_STREAM).nextDouble());
        }

        // calculate headway from demand
        Frequency demand;
        if (this.interpolation.isStepWise())
        {
            demand = this.demandVector.get(i);
        }
        else
        {
            double f = start.si / (this.timeVector.get(i + 1).si - this.timeVector.get(i).si);
            demand = Frequency.interpolate(this.demandVector.get(i), this.demandVector.get(i + 1), f);
        }
        double t = -Math.log(this.simulator.getReplication().getStream(HEADWAY_STREAM).nextDouble()) / demand.si;

        // calculate arrival
        Time arrival = new Time(this.timeVector.get(i).si + start.si + t * fractionRemaining, TimeUnit.BASE);

        // go to next period if arrival is beyond current period
        if (arrival.gt(this.timeVector.get(i + 1)))
        {
            double inStep = this.timeVector.get(i + 1).si - (this.timeVector.get(i).si + start.si);
            return nextArrival(i + 1, Duration.ZERO, fractionRemaining - inStep / t);
        }

        return arrival;

    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "HeadwayGeneratorDemand [interpolation=" + this.interpolation + ", timeVector=" + this.timeVector
                + ", demandVector=" + this.demandVector + ", simulator=" + this.simulator + "]";
    }

}
