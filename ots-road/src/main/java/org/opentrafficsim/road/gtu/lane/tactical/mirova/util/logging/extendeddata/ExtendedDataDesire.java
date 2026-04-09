package org.opentrafficsim.road.gtu.lane.tactical.mirova.util.logging.extendeddata;

import org.djunits.unit.DurationUnit;
import org.djunits.value.vfloat.scalar.FloatDuration;
import org.djunits.value.vfloat.vector.FloatDurationVector;
import org.opentrafficsim.kpi.interfaces.GtuData;
import org.opentrafficsim.kpi.sampling.data.ExtendedDataFloat;

/**
 * Extended data type for logging lane change "Desire" values.
 * <p>
 * Theoretically, desire is a dimensionless Key Performance Indicator (KPI) representing
 * the motivation to change lanes (computed in <b>Layer 2</b> of the MiRoVA architecture).
 * However, because the standard OpenTrafficSim trajectory output does not handle
 * dimensionless units well (often returning empty strings as units which breaks CSV parsing),
 * this class implements a pragmatic workaround by defining the desire metric using
 * {@link DurationUnit} internally.
 * </p>
 * <p>
 * Copyright (c) 2025 Marvin Baumann / KIT. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 *
 * @param <G> the GTU data type
 * @author <a href="https://github.com/baumarv">Marvin Baumann</a>
 */
public abstract class ExtendedDataDesire<G extends GtuData> extends ExtendedDataFloat<DurationUnit, FloatDuration, FloatDurationVector, G>
{

    /**
     * Constructs a new extended data type for logging desire.
     *
     * @param id          the unique identifier for this data type
     * @param description a human-readable description of the recorded data
     */
    public ExtendedDataDesire(final String id, final String description)
    {
        super(id, description, FloatDuration.class);
    }

    /**
     * Converts a primitive float value into a strongly-typed duration scalar.
     *
     * @param value the raw desire value
     * @return the instantiated {@link FloatDuration} acting as a container for the desire
     */
    @Override
    protected FloatDuration convertValue(final float value)
    {
        return new FloatDuration(value, DurationUnit.SI);
    }

    /**
     * Parses a unitless string representation into a typed scalar.
     *
     * @param string the string representation of the desire
     * @return the parsed {@link FloatDuration}
     */
    @Override
    public FloatDuration parseValue(final String string)
    {
        return new FloatDuration(Float.parseFloat(string), DurationUnit.SI);
    }

    /**
     * Converts a primitive float array into a strongly-typed duration vector.
     *
     * @param storage the raw array of desire values
     * @return the instantiated {@link FloatDurationVector}
     */
    @Override
    protected FloatDurationVector convert(final float[] storage)
    {
        return new FloatDurationVector(storage, DurationUnit.SI);
    }

    /**
     * Interpolates between two desire values.
     *
     * @param value0 the first desire value
     * @param value1 the second desire value
     * @param f      the interpolation fraction (0.0 to 1.0)
     * @return the interpolated {@link FloatDuration}
     */
    @Override
    public FloatDuration interpolate(final FloatDuration value0, final FloatDuration value1, final double f)
    {
        return FloatDuration.interpolate(value0, value1, (float) f);
    }

}