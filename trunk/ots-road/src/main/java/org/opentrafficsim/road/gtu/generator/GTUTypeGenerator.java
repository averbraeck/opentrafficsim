package org.opentrafficsim.road.gtu.generator;

import java.util.ArrayList;
import java.util.List;

import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.core.distributions.Generator;
import org.opentrafficsim.core.distributions.ProbabilityException;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.gtu.GTUType;

import nl.tudelft.simulation.jstats.streams.StreamInterface;
import nl.tudelft.simulation.language.Throw;

/**
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 16 nov. 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class GTUTypeGenerator implements Generator<GTUTypeInfo>
{

    /** GTU lengths. */
    private final List<Generator<Length>> lengths = new ArrayList<>();

    /** GTU widths. */
    private final List<Generator<Length>> widths = new ArrayList<>();

    /** GTU types. */
    private final List<GTUType> gtuTypes = new ArrayList<>();

    /** Maximum GTU speeds. */
    private final List<Generator<Speed>> maximumSpeeds = new ArrayList<>();

    /** GTU type probabilities. */
    private final List<Double> probabilities = new ArrayList<>();

    /** Probability sum. */
    private double probabilitySum = 0.0;

    /** Simulator. */
    private final OTSSimulatorInterface simulator;

    /** Random number stream. */
    private final StreamInterface stream;

    /**
     * @param simulator the simulator to use
     * @param stream random number stream
     */
    public GTUTypeGenerator(final OTSSimulatorInterface simulator, final StreamInterface stream)
    {
        Throw.whenNull(simulator, "Simulator may not be null.");
        Throw.whenNull(stream, "Stream for random numbers may not be null.");
        this.simulator = simulator;
        this.stream = stream;
    }

    /**
     * @param length length of the GTU
     * @param width width of the GTU
     * @param gtuType GTU type
     * @param maximumSpeed maximum speed of the GTU
     * @param probability the probability to generate with these characteristics
     */
    public final void addType(final Generator<Length> length, final Generator<Length> width, final GTUType gtuType,
            final Generator<Speed> maximumSpeed, final double probability)
    {
        this.lengths.add(length);
        this.widths.add(width);
        this.gtuTypes.add(gtuType);
        this.maximumSpeeds.add(maximumSpeed);
        this.probabilities.add(probability);
        this.probabilitySum += probability;
    }

    /**
     * @return random GTU type info
     */
    public final GTUTypeInfo draw()
    {
        int i = 0;
        if (this.gtuTypes.size() == 1)
        {
            double r = this.stream.nextDouble();
            double probCumSum = this.probabilities.get(0);
            while (r > probCumSum && i < this.probabilities.size() - 1)
            {
                i++;
                probCumSum += this.probabilities.get(i);
            }
        }
        try
        {
            return new GTUTypeInfo(this.lengths.get(i).draw(), this.widths.get(i).draw(), this.gtuTypes.get(i),
                    this.maximumSpeeds.get(i).draw());
        }
        catch (ProbabilityException | ParameterException exception)
        {
            throw new RuntimeException("Could not draw speed.", exception);
        }
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "GTUTypeGenerator [lengths=" + this.lengths + ", widths=" + this.widths + ", gtuTypes=" + this.gtuTypes
                + ", maximumSpeeds=" + this.maximumSpeeds + ", probabilities=" + this.probabilities + ", probabilitySum="
                + this.probabilitySum + ", simulator=" + this.simulator + "]";
    }

}
