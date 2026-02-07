package org.opentrafficsim.core.gtu;

import java.util.function.Supplier;

import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djutils.exceptions.Throw;
import org.opentrafficsim.core.distributions.ConstantSupplier;

/**
 * Stores some of the information that is needed to generate a GTU.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author$, initial version Jul 8, 2014 <br>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 */
public class GtuTemplate implements Supplier<GtuCharacteristics>
{
    /** The type of the GTU. */
    private final GtuType gtuType;

    /** Supplier for the length of the GTU. */
    private final Supplier<Length> lengthSupplier;

    /** Supplier for the width of the GTU. */
    private final Supplier<Length> widthSupplier;

    /** Supplier for the maximum speed of the GTU. */
    private final Supplier<Speed> maximumSpeedSupplier;

    /** Supplier for the maximum acceleration of the GTU. */
    private final Supplier<Acceleration> maxAcceleration;

    /** Supplier for the maximum deceleration of the GTU. */
    private final Supplier<Acceleration> maxDeceleration;

    /**
     * Constructor.
     * @param gtuType GtuType, the GtuType to make it identifiable.
     * @param lengthSupplier supplier for the length of the GTU type (parallel with driving direction).
     * @param widthSupplier supplier for the width of the GTU type (perpendicular to driving direction).
     * @param maximumSpeedSupplier supplier for the maximum speed of the GTU type (in the driving direction).
     * @throws NullPointerException when one of the arguments is null
     */
    public GtuTemplate(final GtuType gtuType, final Supplier<Length> lengthSupplier, final Supplier<Length> widthSupplier,
            final Supplier<Speed> maximumSpeedSupplier) throws NullPointerException
    {
        this(gtuType, lengthSupplier, widthSupplier, maximumSpeedSupplier, new ConstantSupplier<>(Acceleration.ofSI(3.0)),
                new ConstantSupplier<>(Acceleration.ofSI(-8.0)));
    }

    /**
     * Constructor.
     * @param gtuType GtuType, the GtuType to make it identifiable.
     * @param lengthSupplier supplier for the length of the GTU type (parallel with driving direction).
     * @param widthSupplier supplier for the width of the GTU type (perpendicular to driving direction).
     * @param maximumSpeedSupplier supplier for the maximum speed of the GTU type (in the driving direction).
     * @param maximumAccelerationSupplier supplier for the maximum acceleration of the GTU type
     * @param maximumDecelerationSupplier supplier for the maximum deceleration of the GTU type
     * @throws NullPointerException when one of the arguments is null
     */
    public GtuTemplate(final GtuType gtuType, final Supplier<Length> lengthSupplier, final Supplier<Length> widthSupplier,
            final Supplier<Speed> maximumSpeedSupplier, final Supplier<Acceleration> maximumAccelerationSupplier,
            final Supplier<Acceleration> maximumDecelerationSupplier) throws NullPointerException
    {
        Throw.whenNull(gtuType, "gtuType is null");
        Throw.whenNull(lengthSupplier, "lengthSupplier is null");
        Throw.whenNull(widthSupplier, "widthSupplier is null");
        Throw.whenNull(maximumSpeedSupplier, "maximumSpeedSupplier is null");
        Throw.whenNull(maximumAccelerationSupplier, "maximumAccelerationSupplier is null");
        Throw.whenNull(maximumDecelerationSupplier, "maximumDecelerationSupplier is null");

        this.gtuType = gtuType;
        this.lengthSupplier = lengthSupplier;
        this.widthSupplier = widthSupplier;
        this.maximumSpeedSupplier = maximumSpeedSupplier;
        this.maxAcceleration = maximumAccelerationSupplier;
        this.maxDeceleration = maximumDecelerationSupplier;
    }

    /**
     * Returns the same Characteristics, but pertaining to a different GTU type. This is useful for when the GTU type is used
     * for other purposes in simulation, where the {@code GtuCharacteristics} should be the same.
     * @param newGtuType the new GTU type.
     * @return Copy of this {@code TemplateGTUType} linked to the new GTU type.
     */
    public GtuTemplate copyForGtuType(final GtuType newGtuType)
    {
        return new GtuTemplate(newGtuType, this.lengthSupplier, this.widthSupplier, this.maximumSpeedSupplier,
                this.maxAcceleration, this.maxDeceleration);
    }

    /**
     * Returns characteristics for the given GTU.
     * @return characteristics for the given GTU
     */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public GtuCharacteristics get()
    {
        Acceleration acceleration = this.maxAcceleration.get();
        Acceleration deceleration = this.maxDeceleration.get();
        Throw.when(acceleration.si <= 0, IllegalArgumentException.class, "Acceleration should be above 0.");
        Throw.when(deceleration.si >= 0, IllegalArgumentException.class, "Deceleration should be below 0.");
        Length length = this.lengthSupplier.get();
        return new GtuCharacteristics(this.gtuType, length, this.widthSupplier.get(), this.maximumSpeedSupplier.get(),
                acceleration, deceleration, length.times(0.75));
    }

    /**
     * Returns the GTU type.
     * @return gtuType.
     */
    @SuppressWarnings("checkstyle:designforextension")
    public GtuType getGtuType()
    {
        return this.gtuType;
    }

    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public String toString()
    {
        return String.format("TemplateGTUType [%s, %s, %s, %s]", this.gtuType, this.lengthSupplier, this.widthSupplier,
                this.maximumSpeedSupplier);
    }

}
