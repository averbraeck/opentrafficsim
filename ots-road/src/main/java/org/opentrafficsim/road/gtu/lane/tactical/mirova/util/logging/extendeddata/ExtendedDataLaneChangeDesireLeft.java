package org.opentrafficsim.road.gtu.lane.tactical.mirova.util.logging.extendeddata;

import org.djunits.unit.DimensionlessUnit;
import org.djunits.unit.DurationUnit;
import org.djunits.unit.Unit;
import org.djunits.unit.scale.IdentityScale;
import org.djunits.unit.si.SIPrefixes;
import org.djunits.unit.unitsystem.UnitSystem;
import org.djunits.value.vfloat.scalar.FloatDimensionless;
import org.djunits.value.vfloat.scalar.FloatDuration;
import org.opentrafficsim.kpi.interfaces.GtuData;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.MirovaTacticalPlanner;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.util.units.DimensionlessUnitMirova;
import org.opentrafficsim.road.network.sampling.GtuDataRoad;

/** Desire für Fahrstreifenwechsel nach links. */
public class ExtendedDataLaneChangeDesireLeft extends ExtendedDataDesire<GtuData>
{
    /** Single instance. */
    public static final ExtendedDataLaneChangeDesireLeft INSTANCE = new ExtendedDataLaneChangeDesireLeft();

    public ExtendedDataLaneChangeDesireLeft()
    {
        super("LaneChangeDesireLeft", "Desire for lane change to the left (not really in seconds, but dimensionless)");
    }


    @Override
    public final FloatDuration getValue(final GtuData gtu)
    {
        if (gtu instanceof GtuDataRoad road)
        {
            LaneBasedGtu lgtu = road.getGtu();
            if (lgtu.getTacticalPlanner() instanceof MirovaTacticalPlanner p)
            {
                //double val = p.getLaneChangeDesire().getLeft() != null ? p.getLaneChangeDesire().getLeft() : Double.NaN;
                return FloatDuration.instantiateSI((float) p.getLaneChangeDesire().getLeft(), DurationUnit.SI);
            }
        }
        return FloatDuration.instantiateSI(Float.NaN, DurationUnit.SI);
    }

    @Override
    public final String toString()
    {
        return "Desire for lane change to the left (not really in seconds, but dimensionless)";
    }




}
