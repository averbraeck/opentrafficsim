package org.opentrafficsim.road.gtu.lane.tactical.mirova.util.units;

import org.djunits.quantity.Quantity;
import org.djunits.unit.Unit;
import org.djunits.unit.scale.IdentityScale;
import org.djunits.unit.si.SIPrefixes;
import org.djunits.unit.unitsystem.UnitSystem;

/**
 * Dimensionless unit for Mirova application.
 * Fixes the issue that DJUNITS DimensionlessUnit only returns empty string as unit, which crashes the output of extended trajectory data.
 *
 */
public final class DimensionlessUnitMirova extends Unit<DimensionlessUnitMirova>
{
    /** */
    private static final long serialVersionUID = 20150830L;

    /** The base, with the empty SI signature. */
    public static final Quantity<DimensionlessUnitMirova> BASE = new Quantity<>("Dimensionless", "");

    /** The SI unit for a dimension less unit is "". */
    public static final DimensionlessUnitMirova SI =
            new DimensionlessUnitMirova().build(new Unit.Builder<DimensionlessUnitMirova>().setQuantity(BASE).setId("-").setName("Dimensionless")
                    .setUnitSystem(UnitSystem.OTHER).setSiPrefixes(SIPrefixes.NONE, 1.0).setScale(IdentityScale.SCALE).setAdditionalAbbreviations("dimensionless"));

}
