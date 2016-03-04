package org.opentrafficsim.core.gtu;

import java.io.Serializable;

import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.core.distributions.Generator;
import org.opentrafficsim.core.distributions.ProbabilityException;
import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.idgenerator.IdGenerator;

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
public class TemplateGTUType implements Serializable
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

    /** Length of the next GTU. */
    private Length.Rel length = null;

    /** Width of the next GTU. */
    private Length.Rel width = null;

    /** Maximum velocity of the next GTU. */
    private Speed maximumVelocity = null;

    /**
     * @param typeId The id of the GTUType to make it identifiable.
     * @param idGenerator IdGenerator; the id generator used to generate names for GTUs constructed using this TemplateGTUType.
     *            Provide null to use the default id generator of AbstractGTU.
     * @param lengthGenerator Generator&lt;Length.Rel&gt;; generator for the length of the GTU type (parallel with driving
     *            direction).
     * @param widthGenerator Generator&lt;Length.Rel&gt;; generator for the width of the GTU type (perpendicular to driving
     *            direction).
     * @param maximumVelocityGenerator Generator&lt;Speed&gt;; generator for the maximum velocity of the GTU type (in the
     *            driving direction).
     * @param simulator the simulator.
     */
    public TemplateGTUType(final String typeId, IdGenerator idGenerator, final Generator<Length.Rel> lengthGenerator,
            final Generator<Length.Rel> widthGenerator, final Generator<Speed> maximumVelocityGenerator,
            final OTSDEVSSimulatorInterface simulator)
    {
        this.gtuType = GTUType.makeGTUType(typeId);
        this.idGenerator = idGenerator;
        this.lengthGenerator = lengthGenerator;
        this.widthGenerator = widthGenerator;
        this.maximumVelocityGenerator = maximumVelocityGenerator;
        this.simulator = simulator;
    }

    /**
     * Generate the properties of the next GTU.
     * @throws ProbabilityException when a generator is improperly configured
     */
    public void generateCharacteristics() throws ProbabilityException
    {
        if (null != this.length)
        {
            System.err.println("generateCharacteristics called without intevening call to clearCharacteristics");
        }
        this.length = this.lengthGenerator.draw();
        this.width = this.widthGenerator.draw();
        this.maximumVelocity = this.maximumVelocityGenerator.draw();
    }

    /**
     * Clear all generated values to ensure null pointer exceptions when a getter is called before the generateCharacteristics
     * method is called.
     */
    public void clearCharacteristics()
    {
        this.length = null;
        this.width = null;
        this.maximumVelocity = null;
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
     * @return Length.Rel
     */
    public final Length.Rel getLength()
    {
        return this.length;
    }

    /**
     * @return width.
     */
    public final Length.Rel getWidth()
    {
        return this.width;
    }

    /**
     * @return maximumVelocity.
     */
    public final Speed getMaximumVelocity()
    {
        return this.maximumVelocity;
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
    public GTUType getGtuType()
    {
        return this.gtuType;
    }
}
