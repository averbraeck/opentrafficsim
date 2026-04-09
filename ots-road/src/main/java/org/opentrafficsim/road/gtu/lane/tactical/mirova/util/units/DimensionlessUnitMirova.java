package org.opentrafficsim.road.gtu.lane.tactical.mirova.util.units;

import org.djunits.quantity.Quantity;
import org.djunits.unit.Unit;
import org.djunits.unit.scale.IdentityScale;
import org.djunits.unit.si.SIPrefixes;
import org.djunits.unit.unitsystem.UnitSystem;

/**
 * Custom dimensionless unit for the MiRoVA application.
 * <p>
 * This class fixes a known issue where the standard DJUNITS {@code DimensionlessUnit}
 * returns an empty string as its unit abbreviation. An empty string can crash the
 * OpenTrafficSim extended trajectory output formatters. By defining a custom unit
 * with the explicit ID "-", the trajectory output can parse the column safely.
 * </p>
 * <p>
 * Copyright (c) 2025 Marvin Baumann / KIT. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 *
 * @author <a href="https://github.com/baumarv">Marvin Baumann</a>
 */
public final class DimensionlessUnitMirova extends Unit<DimensionlessUnitMirova>
{
    /** Serialization version. */
    private static final long serialVersionUID = 20150830L;

    /** The base quantity with an empty SI signature. */
    public static final Quantity<DimensionlessUnitMirova> BASE = new Quantity<>("Dimensionless", "");

    /** * The standard unit for a dimensionless unit in MiRoVA, safely represented by "-".
     */
    public static final DimensionlessUnitMirova SI = new DimensionlessUnitMirova().build(
            new Unit.Builder<DimensionlessUnitMirova>()
                    .setQuantity(BASE)
                    .setId("-")
                    .setName("Dimensionless")
                    .setUnitSystem(UnitSystem.OTHER)
                    .setSiPrefixes(SIPrefixes.NONE, 1.0)
                    .setScale(IdentityScale.SCALE)
                    .setAdditionalAbbreviations("dimensionless")
    );

    /**
     * Private constructor to enforce proper unit instantiation via DJUNITS registry.
     */
    private DimensionlessUnitMirova()
    {
        // Utility class pattern
    }
}