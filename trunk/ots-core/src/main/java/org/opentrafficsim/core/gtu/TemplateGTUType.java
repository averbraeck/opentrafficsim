package org.opentrafficsim.core.gtu;

import java.io.Serializable;

import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.core.distributions.Generator;
import org.opentrafficsim.core.distributions.ProbabilityException;
import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.idgenerator.IdGenerator;
import org.opentrafficsim.core.network.OTSNetwork;

/**
 * TemplateGTUType stores most of the information that is needed to generate a GTU.
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
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
    private final Generator<Length.Rel> lengthGenerator;

    /** Generator for the width of the GTU. */
    private final Generator<Length.Rel> widthGenerator;

    /** Generator for the maximum speed of the GTU. */
    private final Generator<Speed> maximumVelocityGenerator;

    /** The simulator. */
    private final OTSDEVSSimulatorInterface simulator;

    /** The network. */
    private final OTSNetwork network;

    /**
     * @param typeId String, the id of the GTUType to make it identifiable.
     * @param idGenerator IdGenerator; the id generator used to generate names for GTUs constructed using this TemplateGTUType.
     *            Provide null to use the default id generator of AbstractGTU.
     * @param lengthGenerator Generator&lt;Length.Rel&gt;; generator for the length of the GTU type (parallel with driving
     *            direction).
     * @param widthGenerator Generator&lt;Length.Rel&gt;; generator for the width of the GTU type (perpendicular to driving
     *            direction).
     * @param maximumVelocityGenerator Generator&lt;Speed&gt;; generator for the maximum velocity of the GTU type (in the
     *            driving direction).
     * @param simulator OTSDEVSSimulatorInterface; the simulator.
     * @param network OTSNetwork; the network that will own the GTUs
     * @throws GTUException when one or more arguments are invalid
     */
    public TemplateGTUType(final String typeId, IdGenerator idGenerator, final Generator<Length.Rel> lengthGenerator,
            final Generator<Length.Rel> widthGenerator, final Generator<Speed> maximumVelocityGenerator,
            final OTSDEVSSimulatorInterface simulator, final OTSNetwork network) throws GTUException
    {
        if (null == typeId)
        {
            throw new GTUException("typeId is null");
        }
        this.gtuType = GTUType.makeGTUType(typeId);
        if (null == idGenerator)
        {
            throw new GTUException("idGenerator is null");
        }
        this.idGenerator = idGenerator;
        if (null == lengthGenerator)
        {
            throw new GTUException("lengthGenerator is null");
        }
        this.lengthGenerator = lengthGenerator;
        if (null == widthGenerator)
        {
            throw new GTUException("widthGenerator is null");
        }
        this.widthGenerator = widthGenerator;
        if (null == maximumVelocityGenerator)
        {
            throw new GTUException("maximumVelocityGenerator is null");
        }
        this.maximumVelocityGenerator = maximumVelocityGenerator;
        if (null == simulator)
        {
            throw new GTUException("simulator");
        }
        this.simulator = simulator;
        if (null == network)
        {
            throw new GTUException("network is null");
        }
        this.network = network;
    }

    /** {@inheritDoc} */
    public GTUCharacteristics draw() throws ProbabilityException
    {
        return new GTUCharacteristics(this.gtuType, this.idGenerator, this.lengthGenerator.draw(), this.widthGenerator.draw(),
                this.maximumVelocityGenerator.draw(), this.simulator, this.network);
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
    public String toString()
    {
        return String.format("TemplateGTUType [%s, %s, %s, %s, %s]", this.gtuType, this.idGenerator, this.lengthGenerator,
                this.widthGenerator, this.maximumVelocityGenerator);
    }

}
