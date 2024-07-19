package org.opentrafficsim.core.definitions;

import java.awt.Color;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.function.BiFunction;

import org.djunits.unit.SpeedUnit;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djutils.immutablecollections.Immutable;
import org.djutils.immutablecollections.ImmutableLinkedHashMap;
import org.djutils.immutablecollections.ImmutableMap;
import org.opentrafficsim.core.distributions.ConstantGenerator;
import org.opentrafficsim.core.gtu.GtuType;
import org.opentrafficsim.core.gtu.GtuType.Marker;
import org.opentrafficsim.core.gtu.GtuTemplate;
import org.opentrafficsim.core.network.LinkType;
import org.opentrafficsim.core.units.distributions.ContinuousDistSpeed;

import nl.tudelft.simulation.jstats.distributions.DistNormal;
import nl.tudelft.simulation.jstats.streams.StreamInterface;

/**
 * Defaults for locale nl_NL.
 * <p>
 * Copyright (c) 2022-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public final class DefaultsNl extends Defaults implements BiFunction<GtuType, StreamInterface, GtuTemplate>
{

    /**
     * Constructor setting locale nl_NL.
     */
    DefaultsNl()
    {
        super(new Locale("nl", "NL"));
    }

    /***************************************************************************************/
    /***************************************** GTU *****************************************/
    /***************************************************************************************/

    /** Super type for all road users. */
    public static final GtuType ROAD_USER = new GtuType("NL.ROAD_USER");

    /** Super type for all water way users. */
    public static final GtuType WATERWAY_USER = new GtuType("NL.WATERWAY_USER");

    /** Super type for all rail users. */
    public static final GtuType RAILWAY_USER = new GtuType("NL.RAILWAY_USER");

    /** Super type for pedestrians. */
    public static final GtuType PEDESTRIAN = new GtuType("NL.PEDESTRIAN", ROAD_USER);

    /** Super type for bicycle. */
    public static final GtuType BICYCLE = new GtuType("NL.BICYCLE", ROAD_USER);

    /** Super type for mopeds. */
    public static final GtuType MOPED = new GtuType("NL.MOPED", BICYCLE);

    /** Super type for vehicles. */
    public static final GtuType VEHICLE = new GtuType("NL.VEHICLE", ROAD_USER);

    /** Super type for emergency vehicles. */
    public static final GtuType EMERGENCY_VEHICLE = new GtuType("NL.EMERGENCY_VEHICLE", VEHICLE);

    /** Super type for ships. */
    public static final GtuType SHIP = new GtuType("NL.SHIP", WATERWAY_USER);

    /** Super type for trains. */
    public static final GtuType TRAIN = new GtuType("NL.TRAIN", RAILWAY_USER);

    /** Super type for cars. */
    public static final GtuType CAR = new GtuType("NL.CAR", VEHICLE);

    /** Super type for motorcycles. */
    public static final GtuType MOTORCYCLE = new GtuType("NL.MOTORCYCLE", VEHICLE);

    /** Super type for vans. */
    public static final GtuType VAN = new GtuType("NL.VAN", VEHICLE);

    /** Super type for busses. */
    public static final GtuType BUS = new GtuType("NL.BUS", VEHICLE);

    /** Super type for trucks. */
    public static final GtuType TRUCK = new GtuType("NL.TRUCK", VEHICLE);

    /** Super type for scheduled busses. */
    public static final GtuType SCHEDULED_BUS = new GtuType("NL.SCHEDULED_BUS", BUS);

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
        map.put(MOTORCYCLE, Color.PINK);
        GTU_TYPE_COLORS = new ImmutableLinkedHashMap<>(map, Immutable.WRAP);

        TRUCK.setMarker(Marker.SQUARE);
    }

    /**
     * Returns a template for the given GTU type. This can be defined at the level of super types, returning {@code null} for
     * more specific types. There is no need to define a template for all default types defined for a locale, so long as at
     * least one parent of each type has a template defined.<br>
     * <br>
     * Note: implementations should not cache the template per GTU type, as different simulations may request templates for the
     * same GTU type, while having their separate random streams.
     * @param gtuType GtuType; GTU type.
     * @param randomStream StreamInterface; random stream.
     * @return TemplateGtuType; template, {@code null} if no default is defined.
     */
    @Override
    public GtuTemplate apply(final GtuType gtuType, final StreamInterface randomStream)
    {
        GtuTemplate template = null;
        if (gtuType.equals(CAR))
        {
            // from "Maatgevende normen in de Nederlandse richtlijnen voor wegontwerp", R-2014-38, SWOV
            template = new GtuTemplate(gtuType, new ConstantGenerator<>(Length.instantiateSI(4.19)),
                    new ConstantGenerator<>(Length.instantiateSI(1.7)),
                    new ConstantGenerator<>(new Speed(180, SpeedUnit.KM_PER_HOUR)));
        }
        else if (gtuType.equals(TRUCK))
        {
            // from "Maatgevende normen in de Nederlandse richtlijnen voor wegontwerp", R-2014-38, SWOV
            template = new GtuTemplate(gtuType, new ConstantGenerator<>(Length.instantiateSI(12.0)),
                    new ConstantGenerator<>(Length.instantiateSI(2.55)),
                    new ContinuousDistSpeed(new DistNormal(randomStream, 85.0, 2.5), SpeedUnit.KM_PER_HOUR));
        }
        else if (gtuType.equals(BUS))
        {
            template = new GtuTemplate(gtuType, new ConstantGenerator<>(Length.instantiateSI(12.0)),
                    new ConstantGenerator<>(Length.instantiateSI(2.55)),
                    new ConstantGenerator<>(new Speed(90, SpeedUnit.KM_PER_HOUR)));
        }
        else if (gtuType.equals(VAN))
        {
            template = new GtuTemplate(gtuType, new ConstantGenerator<>(Length.instantiateSI(5.0)),
                    new ConstantGenerator<>(Length.instantiateSI(2.4)),
                    new ConstantGenerator<>(new Speed(180, SpeedUnit.KM_PER_HOUR)));
        }
        else if (gtuType.equals(EMERGENCY_VEHICLE))
        {
            template = new GtuTemplate(gtuType, new ConstantGenerator<>(Length.instantiateSI(5.0)),
                    new ConstantGenerator<>(Length.instantiateSI(2.55)),
                    new ConstantGenerator<>(new Speed(180, SpeedUnit.KM_PER_HOUR)));
        }
        else if (gtuType.equals(MOTORCYCLE))
        {
            template = new GtuTemplate(gtuType, new ConstantGenerator<>(Length.instantiateSI(2.1)),
                    new ConstantGenerator<>(Length.instantiateSI(0.9)),
                    new ConstantGenerator<>(new Speed(180, SpeedUnit.KM_PER_HOUR)));
        }
        else if (gtuType.equals(BICYCLE))
        {
            template = new GtuTemplate(gtuType, new ConstantGenerator<>(Length.instantiateSI(2)),
                    new ConstantGenerator<>(Length.instantiateSI(0.7)),
                    new ConstantGenerator<>(new Speed(35, SpeedUnit.KM_PER_HOUR)));
        }
        return template;
    };

    /***************************************************************************************/
    /**************************************** LINK *****************************************/
    /***************************************************************************************/

    /** Connector type. */
    public static final LinkType CONNECTOR = new LinkType("NL.CONNECTOR");

    /** Super type for all roads. */
    public static final LinkType ROAD = new LinkType("NL.ROAD");

    /** Freeway (snelweg, 130km/h). */
    public static final LinkType FREEWAY = new LinkType("NL.FREEWAY", ROAD);

    /** Highway (autoweg, 100km/h). */
    public static final LinkType HIGHWAY = new LinkType("NL.HIGHWAY", ROAD);

    /** Provincial (provinciaalse weg / N-weg, 80km/h). */
    public static final LinkType PROVINCIAL = new LinkType("NL.PROVINCIAL", ROAD);

    /** Rural (landelijk, 60km/h). */
    public static final LinkType RURAL = new LinkType("NL.RURAL", ROAD);

    /** Urban (stedelijk, 50km/h). */
    public static final LinkType URBAN = new LinkType("NL.URBAN", ROAD);

    /** Residential (buurtweg, 30km/h). */
    public static final LinkType RESIDENTIAL = new LinkType("NL.RESIDENTIAL", ROAD);

    /** Waterway. */
    public static final LinkType WATERWAY = new LinkType("NL.WATERWAY");

    /** Railway. */
    public static final LinkType RAILWAY = new LinkType("NL.RAILWAY");

    static
    {
        CONNECTOR.addCompatibleGtuType(ROAD_USER);
        CONNECTOR.addCompatibleGtuType(WATERWAY_USER);
        CONNECTOR.addCompatibleGtuType(RAILWAY_USER);
        ROAD.addCompatibleGtuType(ROAD_USER);
        FREEWAY.addIncompatibleGtuType(PEDESTRIAN);
        FREEWAY.addIncompatibleGtuType(BICYCLE);
        HIGHWAY.addIncompatibleGtuType(PEDESTRIAN);
        HIGHWAY.addIncompatibleGtuType(BICYCLE);
        PROVINCIAL.addIncompatibleGtuType(PEDESTRIAN);
        PROVINCIAL.addIncompatibleGtuType(BICYCLE);
        WATERWAY.addCompatibleGtuType(WATERWAY_USER);
        RAILWAY.addCompatibleGtuType(RAILWAY_USER);
    }

}
