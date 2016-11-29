package org.opentrafficsim.imb.demo.generators;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.core.distributions.Generator;
import org.opentrafficsim.core.distributions.ProbabilityException;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.ParameterException;

import nl.tudelft.simulation.language.Throw;

/**
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 16 nov. 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class GTUTypeGenerator
{

    /** GTU lengths. */
    private final List<Length> lengths = new ArrayList<>();

    /** GTU widths. */
    private final List<Length> widths = new ArrayList<>();

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

    /** Stream name of GTU class generation. */
    private final static String GTU_CLASS_STREAM = "gtuClass";

    /**
     * @param simulator the simulator to use
     */
    public GTUTypeGenerator(OTSSimulatorInterface simulator)
    {
        Throw.whenNull(simulator, "Simulator may not be null.");
        try
        {
            Throw.whenNull(simulator.getReplication().getStream(GTU_CLASS_STREAM),
                    "Could not obtain random stream '" + GTU_CLASS_STREAM + "'.");
        }
        catch (RemoteException exception)
        {
            throw new RuntimeException("Could not obtain replication.", exception);
        }
        this.simulator = simulator;
    }

    /**
     * @param length length of the GTU
     * @param width width of the GTU
     * @param gtuType GTU type
     * @param maximumSpeed maximum speed of the GTU
     * @param probability the probability to generate with these characteristics
     */
    public void addType(Length length, Length width, GTUType gtuType, Generator<Speed> maximumSpeed, double probability)
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
    public GTUTypeInfo draw()
    {
        double r;
        try
        {
            r = this.simulator.getReplication().getStream(GTU_CLASS_STREAM).nextDouble() * this.probabilitySum;
        }
        catch (RemoteException exception)
        {
            throw new RuntimeException("Could not obtain replication.", exception);
        }
        int i = 0;
        double probCumSum = this.probabilities.get(0);
        while (r > probCumSum && i < this.probabilities.size() - 1)
        {
            i++;
            probCumSum += this.probabilities.get(i);
        }
        try
        {
            return new GTUTypeInfo(this.lengths.get(i), this.widths.get(i), this.gtuTypes.get(i), this.maximumSpeeds.get(i).draw());
        }
        catch (ProbabilityException | ParameterException exception)
        {
            throw new RuntimeException("Could not draw speed.", exception);
        }
    }

    /**
     * <p>
     * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
     * <p>
     * @version $Revision$, $LastChangedDate$, by $Author$, initial version 19 nov. 2016 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
     */
    public class GTUTypeInfo
    {

        /** Length. */
        private final Length length;

        /** Width. */
        private final Length width;

        /** GTU type. */
        private final GTUType gtuType;

        /** Maximum speed. */
        private final Speed maximumSpeed;

        /**
         * @param length length of the GTU
         * @param width width of the GTU
         * @param gtuType GTU type
         * @param maximumSpeed maximum speed of the GTU
         */
        public GTUTypeInfo(Length length, Length width, GTUType gtuType, Speed maximumSpeed)
        {
            this.length = length;
            this.width = width;
            this.gtuType = gtuType;
            this.maximumSpeed = maximumSpeed;
        }

        /**
         * @return length.
         */
        public Length getLength()
        {
            return this.length;
        }

        /**
         * @return width.
         */
        public Length getWidth()
        {
            return this.width;
        }

        /**
         * @return gtuType.
         */
        public GTUType getGtuType()
        {
            return this.gtuType;
        }

        /**
         * @return maximumSpeed.
         */
        public Speed getMaximumSpeed()
        {
            return this.maximumSpeed;
        }

    }

}
