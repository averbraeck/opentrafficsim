package org.opentrafficsim.core.definitions;

import java.awt.Color;
import java.util.LinkedHashMap;
import java.util.Locale;

import org.djunits.unit.SpeedUnit;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djutils.immutablecollections.Immutable;
import org.djutils.immutablecollections.ImmutableLinkedHashMap;
import org.djutils.immutablecollections.ImmutableMap;
import org.opentrafficsim.core.distributions.ConstantGenerator;
import org.opentrafficsim.core.gtu.GtuType;
import org.opentrafficsim.core.gtu.TemplateGtuType;
import org.opentrafficsim.core.units.distributions.ContinuousDistSpeed;

import nl.tudelft.simulation.jstats.distributions.DistNormal;
import nl.tudelft.simulation.jstats.streams.StreamInterface;

/**
 * Defaults for locale nl_NL.
 * <p>
 * Copyright (c) 2022-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public final class DefaultsNl extends Defaults
{
    // TODO: prepend id's with "NL."

    /** This is here only because it is in the file default_gtutypes.xml as a default, i.e the parser needs to find it. */
    @Deprecated
    public static final GtuType NONE = new GtuType("NONE");

    /** Super type for all road users. */
    public static final GtuType ROAD_USER = new GtuType("ROAD_USER");

    /** Super type for all water way users. */
    public static final GtuType WATERWAY_USER = new GtuType("WATERWAY_USER");

    /** Super type for all rail users. */
    public static final GtuType RAILWAY_USER = new GtuType("RAILWAY_USER");

    /** Super type for pedestrians. */
    public static final GtuType PEDESTRIAN = new GtuType("PEDESTRIAN", ROAD_USER);

    /** Super type for bicycle. */
    public static final GtuType BICYCLE = new GtuType("BICYCLE", ROAD_USER);

    /** Super type for mopeds. */
    public static final GtuType MOPED = new GtuType("MOPED", BICYCLE);

    /** Super type for vehicles. */
    public static final GtuType VEHICLE = new GtuType("VEHICLE", ROAD_USER);

    /** Super type for emergency vehicles. */
    public static final GtuType EMERGENCY_VEHICLE = new GtuType("EMERGENCY_VEHICLE", VEHICLE);

    /** Super type for ships. */
    public static final GtuType SHIP = new GtuType("SHIP", WATERWAY_USER);

    /** Super type for trains. */
    public static final GtuType TRAIN = new GtuType("TRAIN", RAILWAY_USER);

    /** Super type for cars. */
    public static final GtuType CAR = new GtuType("CAR", VEHICLE);

    /** Super type for vans. */
    public static final GtuType VAN = new GtuType("VAN", VEHICLE);

    /** Super type for busses. */
    public static final GtuType BUS = new GtuType("BUS", VEHICLE);

    /** Super type for trucks. */
    public static final GtuType TRUCK = new GtuType("TRUCK", VEHICLE);

    /** Super type for scheduled busses. */
    public static final GtuType SCHEDULED_BUS = new GtuType("SCHEDULED_BUS", BUS);

    /** Standard drawing colors for GTU types. */
    public static final ImmutableMap<GtuType, Color> GTU_TYPE_COLORS;

    static
    {
        LinkedHashMap<GtuType, Color> map = new LinkedHashMap<>();
        map.put(CAR, Color.BLUE);
        map.put(TRUCK, Color.RED);
        map.put(VEHICLE, Color.GRAY);
        map.put(PEDESTRIAN, Color.YELLOW);
        map.put(BICYCLE, Color.GREEN);
        GTU_TYPE_COLORS = new ImmutableLinkedHashMap<>(map, Immutable.WRAP);
    }

    /**
     * Constructor setting locale nl_NL.
     */
    DefaultsNl()
    {
        super(new Locale("nl", "NL"));
    }

    /** {@inheritDoc} */
    @Override
    public TemplateGtuType getTemplate(final GtuType gtuType, final StreamInterface randomStream)
    {
        TemplateGtuType template = null;
        if (gtuType.equals(CAR))
        {
            // from "Maatgevende normen in de Nederlandse richtlijnen voor wegontwerp", R-2014-38, SWOV
            template = new TemplateGtuType(gtuType, new ConstantGenerator<>(Length.instantiateSI(4.19)),
                    new ConstantGenerator<>(Length.instantiateSI(1.7)),
                    new ConstantGenerator<>(new Speed(180, SpeedUnit.KM_PER_HOUR)));
        }
        else if (gtuType.equals(TRUCK))
        {
            // from "Maatgevende normen in de Nederlandse richtlijnen voor wegontwerp", R-2014-38, SWOV
            template = new TemplateGtuType(gtuType, new ConstantGenerator<>(Length.instantiateSI(12.0)),
                    new ConstantGenerator<>(Length.instantiateSI(2.55)),
                    new ContinuousDistSpeed(new DistNormal(randomStream, 85.0, 2.5), SpeedUnit.KM_PER_HOUR));
        }
        else if (gtuType.equals(BUS))
        {
            template = new TemplateGtuType(gtuType, new ConstantGenerator<>(Length.instantiateSI(12.0)),
                    new ConstantGenerator<>(Length.instantiateSI(2.55)),
                    new ConstantGenerator<>(new Speed(90, SpeedUnit.KM_PER_HOUR)));
        }
        else if (gtuType.equals(VAN))
        {
            template = new TemplateGtuType(gtuType, new ConstantGenerator<>(Length.instantiateSI(5.0)),
                    new ConstantGenerator<>(Length.instantiateSI(2.4)),
                    new ConstantGenerator<>(new Speed(180, SpeedUnit.KM_PER_HOUR)));
        }
        else if (gtuType.equals(EMERGENCY_VEHICLE))
        {
            template = new TemplateGtuType(gtuType, new ConstantGenerator<>(Length.instantiateSI(5.0)),
                    new ConstantGenerator<>(Length.instantiateSI(2.55)),
                    new ConstantGenerator<>(new Speed(180, SpeedUnit.KM_PER_HOUR)));
        }
        return template;
    };
}
