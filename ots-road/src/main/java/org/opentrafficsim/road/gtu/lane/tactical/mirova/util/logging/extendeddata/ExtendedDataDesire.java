package org.opentrafficsim.road.gtu.lane.tactical.mirova.util.logging.extendeddata;

import org.djunits.quantity.Quantity;
import org.djunits.unit.DimensionlessUnit;
import org.djunits.unit.DurationUnit;
import org.djunits.unit.Unit;
import org.djunits.unit.scale.IdentityScale;
import org.djunits.unit.si.SIPrefixes;
import org.djunits.unit.unitsystem.UnitSystem;
import org.djunits.value.vfloat.scalar.FloatDimensionless;
import org.djunits.value.vfloat.scalar.FloatDuration;
import org.djunits.value.vfloat.scalar.FloatSpeed;
import org.djunits.value.vfloat.scalar.base.FloatScalar;
import org.djunits.value.vfloat.vector.FloatDimensionlessVector;
import org.djunits.value.vfloat.vector.FloatDurationVector;
import org.djunits.value.vfloat.vector.base.FloatVector;
import org.opentrafficsim.kpi.interfaces.GtuData;
import org.opentrafficsim.kpi.sampling.data.ExtendedDataFloat;
import org.opentrafficsim.kpi.sampling.data.ExtendedDataString;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.MirovaTacticalPlanner;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.util.units.DimensionlessUnitMirova;
import org.opentrafficsim.road.network.sampling.GtuDataRoad;

/** Desire: theoretically a kpi without dimension, but since trajectory output does not allow dimensionless Unit (as they do return an empty string as
 * unit), we define it as Duration. */
public abstract class ExtendedDataDesire<G extends GtuData> extends ExtendedDataFloat<DurationUnit, FloatDuration,
        FloatDurationVector, G>
{
    public ExtendedDataDesire(final String id, final String description)
    {
        super(id, description, FloatDuration.class);
    }


    /** float -> typisierter Scalar. */
    @Override
    protected FloatDuration convertValue(final float value)
    {
        return new FloatDuration(value, DurationUnit.SI);
    }

    /** String (ohne Einheit) -> typisierter Scalar. */
    @Override
    public FloatDuration parseValue(final String string)
    {
        return new FloatDuration(Float.parseFloat(string), DurationUnit.SI);
    }

    /** float[] -> typisierter Vector. */
    @Override
    protected FloatDurationVector convert(final float[] storage)
    {
        return new FloatDurationVector(storage);
    }

    @Override
    public FloatDuration interpolate(final FloatDuration value0, final FloatDuration value1, final double f)
    {
        return FloatDuration.interpolate(value0, value1, (float) f);
    }



}