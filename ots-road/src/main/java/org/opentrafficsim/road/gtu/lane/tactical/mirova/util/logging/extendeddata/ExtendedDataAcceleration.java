package org.opentrafficsim.road.gtu.lane.tactical.mirova.util.logging.extendeddata;

import org.djunits.unit.AccelerationUnit;
import org.djunits.value.vfloat.scalar.FloatAcceleration;
import org.djunits.value.vfloat.vector.FloatAccelerationVector;
import org.opentrafficsim.kpi.interfaces.GtuData;
import org.opentrafficsim.kpi.sampling.data.ExtendedDataFloat;

/**
 * Extended data type for storing and interpolating Acceleration values in the KPI sampling framework.
 * <p>
 * This utility class facilitates the detailed logging and offline analysis of GTU accelerations
 * during the simulation. It is typically used to evaluate the smoothness and performance of
 * the maneuvers executed by Layer 4 (Procedure & Action).
 * </p>
 * <p>
 * Copyright (c) 2025 Marvin Baumann / KIT. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 *
 * @param <G> the GTU data type
 * @author <a href="https://github.com/baumarv">Marvin Baumann</a>
 */
public abstract class ExtendedDataAcceleration<G extends GtuData> extends ExtendedDataFloat<AccelerationUnit, FloatAcceleration, FloatAccelerationVector, G>
{

    /**
     * Constructs a new extended data type for acceleration.
     *
     * @param id          the unique identifier for this data type
     * @param description a human-readable description of the recorded data
     */
    public ExtendedDataAcceleration(final String id, final String description)
    {
        super(id, description, FloatAcceleration.class);
    }

    /**
     * Converts a primitive float value into a strongly-typed scalar.
     *
     * @param value the raw acceleration value in SI units (m/s²)
     * @return the instantiated {@link FloatAcceleration}
     */
    @Override
    protected final FloatAcceleration convertValue(final float value)
    {
        return FloatAcceleration.instantiateSI(value);
    }

    /**
     * Converts a primitive float array into a strongly-typed vector.
     *
     * @param storage the raw acceleration array in SI units (m/s²)
     * @return the instantiated {@link FloatAccelerationVector}
     */
    @Override
    protected final FloatAccelerationVector convert(final float[] storage)
    {
        return new FloatAccelerationVector(storage, AccelerationUnit.SI);
    }

    /**
     * Interpolates between two acceleration values.
     *
     * @param value0 the first acceleration value
     * @param value1 the second acceleration value
     * @param f      the interpolation fraction (0.0 to 1.0)
     * @return the interpolated {@link FloatAcceleration}
     */
    @Override
    public FloatAcceleration interpolate(final FloatAcceleration value0, final FloatAcceleration value1, final double f)
    {
        return FloatAcceleration.interpolate(value0, value1, (float) f);
    }

    /**
     * Parses a string representation into an acceleration value.
     *
     * @param string the string representation of the acceleration in SI units
     * @return the parsed {@link FloatAcceleration}
     */
    @Override
    public FloatAcceleration parseValue(final String string)
    {
        return FloatAcceleration.instantiateSI(Float.valueOf(string));
    }

}