package org.opentrafficsim.core.gtu;

import java.io.Serializable;

import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djutils.exceptions.Throw;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.core.distributions.ConstantGenerator;
import org.opentrafficsim.core.distributions.Generator;
import org.opentrafficsim.core.distributions.ProbabilityException;

/**
 * Stores some of the information that is needed to generate a GTU.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author$, initial version Jul 8, 2014 <br>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 */
public class GtuTemplate implements Serializable, Generator<GtuCharacteristics>
{
    /** */
    private static final long serialVersionUID = 20141230L;

    /** The type of the GTU. */
    private final GtuType gtuType;

    /** Generator for the length of the GTU. */
    private final Generator<Length> lengthGenerator;

    /** Generator for the width of the GTU. */
    private final Generator<Length> widthGenerator;

    /** Generator for the maximum speed of the GTU. */
    private final Generator<Speed> maximumSpeedGenerator;

    /** Generator for the maximum acceleration of the GTU. */
    private final Generator<Acceleration> maxAcceleration;

    /** Generator for the maximum deceleration of the GTU. */
    private final Generator<Acceleration> maxDeceleration;

    /**
     * @param gtuType GtuType; GtuType, the GtuType to make it identifiable.
     * @param lengthGenerator Generator&lt;Length&gt;; generator for the length of the GTU type (parallel with driving
     *            direction).
     * @param widthGenerator Generator&lt;Length&gt;; generator for the width of the GTU type (perpendicular to driving
     *            direction).
     * @param maximumSpeedGenerator Generator&lt;Speed&gt;; generator for the maximum speed of the GTU type (in the driving
     *            direction).
     * @throws NullPointerException when one of the arguments is null
     */
    public GtuTemplate(final GtuType gtuType, final Generator<Length> lengthGenerator,
            final Generator<Length> widthGenerator, final Generator<Speed> maximumSpeedGenerator) throws NullPointerException
    {
        this(gtuType, lengthGenerator, widthGenerator, maximumSpeedGenerator,
                new ConstantGenerator<>(Acceleration.instantiateSI(3.0)),
                new ConstantGenerator<>(Acceleration.instantiateSI(-8.0)));
    }

    /**
     * @param gtuType GtuType; GtuType, the GtuType to make it identifiable.
     * @param lengthGenerator Generator&lt;Length&gt;; generator for the length of the GTU type (parallel with driving
     *            direction).
     * @param widthGenerator Generator&lt;Length&gt;; generator for the width of the GTU type (perpendicular to driving
     *            direction).
     * @param maximumSpeedGenerator Generator&lt;Speed&gt;; generator for the maximum speed of the GTU type (in the driving
     *            direction).
     * @param maximumAccelerationGenerator Generator&lt;Acceleration&gt;; generator for the maximum acceleration of the GTU type
     * @param maximumDecelerationGenerator Generator&lt;Acceleration&gt;; generator for the maximum deceleration of the GTU type
     * @throws NullPointerException when one of the arguments is null
     */
    public GtuTemplate(final GtuType gtuType, final Generator<Length> lengthGenerator,
            final Generator<Length> widthGenerator, final Generator<Speed> maximumSpeedGenerator,
            final Generator<Acceleration> maximumAccelerationGenerator,
            final Generator<Acceleration> maximumDecelerationGenerator) throws NullPointerException
    {
        Throw.whenNull(gtuType, "gtuType is null");
        Throw.whenNull(lengthGenerator, "lengthGenerator is null");
        Throw.whenNull(widthGenerator, "widthGenerator is null");
        Throw.whenNull(maximumSpeedGenerator, "maximumSpeedGenerator is null");
        Throw.whenNull(maximumAccelerationGenerator, "maximumAccelerationGenerator is null");
        Throw.whenNull(maximumDecelerationGenerator, "maximumDecelerationGenerator is null");

        this.gtuType = gtuType;
        this.lengthGenerator = lengthGenerator;
        this.widthGenerator = widthGenerator;
        this.maximumSpeedGenerator = maximumSpeedGenerator;
        this.maxAcceleration = maximumAccelerationGenerator;
        this.maxDeceleration = maximumDecelerationGenerator;
    }

    /**
     * Returns the same Characteristics, but pertaining to a different GTU type. This is useful for when the GTU type is used
     * for other purposes in simulation, where the {@code GtuCharacteristics} should be the same.
     * @param newGtuType GtuType; the new GTU type.
     * @return Copy of this {@code TemplateGTUType} linked to the new GTU type.
     */
    public GtuTemplate copyForGtuType(final GtuType newGtuType)
    {
        return new GtuTemplate(newGtuType, this.lengthGenerator, this.widthGenerator, this.maximumSpeedGenerator,
                this.maxAcceleration, this.maxDeceleration);
    }

    /**
     * Returns characteristics for the given GTU.
     * @return characteristics for the given GTU
     * @throws ProbabilityException in case of probability exception
     * @throws ParameterException in case of parameter exception
     */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public GtuCharacteristics draw() throws ProbabilityException, ParameterException
    {
        Acceleration acceleration = this.maxAcceleration.draw();
        Acceleration deceleration = this.maxDeceleration.draw();
        Throw.when(acceleration.si <= 0, IllegalArgumentException.class, "Acceleration should be above 0.");
        Throw.when(deceleration.si >= 0, IllegalArgumentException.class, "Deceleration should be below 0.");
        Length length = this.lengthGenerator.draw();
        return new GtuCharacteristics(this.gtuType, length, this.widthGenerator.draw(), this.maximumSpeedGenerator.draw(),
                acceleration, deceleration, length.times(0.75));
    }

    /**
     * @return gtuType.
     */
    @SuppressWarnings("checkstyle:designforextension")
    public GtuType getGtuType()
    {
        return this.gtuType;
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public String toString()
    {
        return String.format("TemplateGTUType [%s, %s, %s, %s]", this.gtuType, this.lengthGenerator, this.widthGenerator,
                this.maximumSpeedGenerator);
    }

}
