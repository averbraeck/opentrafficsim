package org.opentrafficsim.core.gtu;

import java.io.Serializable;

import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.core.distributions.Generator;
import org.opentrafficsim.core.distributions.ProbabilityException;
import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.ParameterException;
import org.opentrafficsim.core.idgenerator.IdGenerator;
import org.opentrafficsim.core.network.OTSNetwork;

import nl.tudelft.simulation.language.Throw;

/**
 * TemplateGTUType stores most of the information that is needed to generate a GTU.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author$,
 * initial version Jul 8, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class TemplateGTUType implements Serializable, Generator<GTUCharacteristics>
{
    /** */
    private static final long serialVersionUID = 20141230L;

    /** The type of the GTU. */
    private final GTUType gtuType;

    /** The IdGenerator for constructed GTUs. */
    private final IdGenerator idGenerator;

    /** Generator for the length of the GTU. */
    private final Generator<Length> lengthGenerator;

    /** Generator for the width of the GTU. */
    private final Generator<Length> widthGenerator;

    /** Generator for the maximum speed of the GTU. */
    private final Generator<Speed> maximumSpeedGenerator;

    /** The simulator. */
    private final OTSDEVSSimulatorInterface simulator;

    /** The network. */
    private final OTSNetwork network;

    /**
     * @param gtuType GTUType, the GTUType to make it identifiable.
     * @param idGenerator IdGenerator; the id generator used to generate names for GTUs constructed using this TemplateGTUType.
     *            Provide null to use the default id generator of AbstractGTU.
     * @param lengthGenerator Generator&lt;Length&gt;; generator for the length of the GTU type (parallel with driving
     *            direction).
     * @param widthGenerator Generator&lt;Length&gt;; generator for the width of the GTU type (perpendicular to driving
     *            direction).
     * @param maximumSpeedGenerator Generator&lt;Speed&gt;; generator for the maximum speed of the GTU type (in the
     *            driving direction).
     * @param simulator OTSDEVSSimulatorInterface; the simulator.
     * @param network OTSNetwork; the network that will own the GTUs
     * @throws NullPointerException when one of the arguments is null
     */
    public TemplateGTUType(final GTUType gtuType, final IdGenerator idGenerator, final Generator<Length> lengthGenerator,
            final Generator<Length> widthGenerator, final Generator<Speed> maximumSpeedGenerator,
            final OTSDEVSSimulatorInterface simulator, final OTSNetwork network) throws NullPointerException
    {
        Throw.whenNull(gtuType, "gtuType is null");
        Throw.whenNull(idGenerator, "idGenerator is null");
        Throw.whenNull(lengthGenerator, "lengthGenerator is null");
        Throw.whenNull(widthGenerator, "widthGenerator is null");
        Throw.whenNull(maximumSpeedGenerator, "maximumSpeedGenerator is null");
        Throw.whenNull(simulator, "simulator is null");
        Throw.whenNull(network, "network is null");

        this.gtuType = gtuType;
        this.idGenerator = idGenerator;
        this.lengthGenerator = lengthGenerator;
        this.widthGenerator = widthGenerator;
        this.maximumSpeedGenerator = maximumSpeedGenerator;
        this.simulator = simulator;
        this.network = network;
    }

    /**
     * Returns characteristics for the given GTU.
     * @return characteristics for the given GTU
     * @throws ProbabilityException in case of probability exception
     * @throws ParameterException in case of parameter exception
     */
    @SuppressWarnings("checkstyle:designforextension")
    public GTUCharacteristics draw() throws ProbabilityException, ParameterException
    {
        return new GTUCharacteristics(this.gtuType, this.idGenerator, this.lengthGenerator.draw(), this.widthGenerator.draw(),
                this.maximumSpeedGenerator.draw(), this.simulator, this.network);
    }

    /**
     * Retrieve the id generator.
     * @return IdGenerator
     */
    public final IdGenerator getIdGenerator()
    {
        return this.idGenerator;
    }

    /**
     * @return simulator.
     */
    public final OTSDEVSSimulatorInterface getSimulator()
    {
        return this.simulator;
    }

    /**
     * @return gtuType.
     */
    @SuppressWarnings("checkstyle:designforextension")
    public GTUType getGTUType()
    {
        return this.gtuType;
    }

    /** {@inheritDoc} */
    @SuppressWarnings("checkstyle:designforextension")
    public String toString()
    {
        return String.format("TemplateGTUType [%s, %s, %s, %s, %s]", this.gtuType, this.idGenerator, this.lengthGenerator,
                this.widthGenerator, this.maximumSpeedGenerator);
    }

}