package org.opentrafficsim.core.definitions;

import java.util.Locale;
import java.util.function.BiFunction;

import org.djunits.unit.SpeedUnit;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.core.distributions.ConstantGenerator;
import org.opentrafficsim.core.gtu.GtuTemplate;
import org.opentrafficsim.core.gtu.GtuType;
import org.opentrafficsim.core.network.LinkType;
import org.opentrafficsim.core.object.DetectorType;
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
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
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

    /**
     * Returns a template for the given GTU type. This can be defined at the level of super types, returning {@code null} for
     * more specific types. There is no need to define a template for all default types defined for a locale, so long as at
     * least one parent of each type has a template defined.<br>
     * <br>
     * Note: implementations should not cache the template per GTU type, as different simulations may request templates for the
     * same GTU type, while having their separate random streams.
     * @param gtuType GTU type.
     * @param randomStream random stream.
     * @return template, {@code null} if no default is defined.
     */
    @Override
    public GtuTemplate apply(final GtuType gtuType, final StreamInterface randomStream)
    {
        GtuTemplate template;
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
            // Yamaha R7 2022
            template = new GtuTemplate(gtuType, new ConstantGenerator<>(Length.instantiateSI(2.1)),
                    new ConstantGenerator<>(Length.instantiateSI(0.7)),
                    new ConstantGenerator<>(new Speed(180, SpeedUnit.KM_PER_HOUR)));
        }
        else if (gtuType.equals(BICYCLE))
        {
            // length/width: https://www.verderfietsen.nl/fiets-afmetingen/
            // width: https://www.fietsberaad.nl/CROWFietsberaad/media/Kennis/Bestanden/document000172.pdf?ext=.pdf
            template = new GtuTemplate(gtuType, new ConstantGenerator<>(Length.instantiateSI(1.9)),
                    new ConstantGenerator<>(Length.instantiateSI(0.6)),
                    new ConstantGenerator<>(new Speed(35, SpeedUnit.KM_PER_HOUR)));
        }
        else
        {
            template = apply(gtuType.getParent(), randomStream);
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

    static
    {
        CONNECTOR.addCompatibleGtuType(ROAD_USER);
        ROAD.addCompatibleGtuType(ROAD_USER);
        FREEWAY.addIncompatibleGtuType(PEDESTRIAN);
        FREEWAY.addIncompatibleGtuType(BICYCLE);
        HIGHWAY.addIncompatibleGtuType(PEDESTRIAN);
        HIGHWAY.addIncompatibleGtuType(BICYCLE);
        PROVINCIAL.addIncompatibleGtuType(PEDESTRIAN);
        PROVINCIAL.addIncompatibleGtuType(BICYCLE);
    }

    /***************************************************************************************/
    /************************************** DETECTOR ***************************************/
    /***************************************************************************************/

    /** Makes a Detector compatible with all road users, e.g. for SinkDetector. */
    public static final DetectorType ROAD_USERS = new DetectorType("NL.ROAD_USERS");

    /** Makes a Detector compatible with all vehicles, e.g. for loop detectors. */
    public static final DetectorType VEHICLES = new DetectorType("NL.VEHICLES");

    /** Loop detector type. */
    public static final DetectorType LOOP_DETECTOR = new DetectorType("NL.LOOP_DETECTOR", VEHICLES);

    /** Traffic light detector type. */
    public static final DetectorType TRAFFIC_LIGHT = new DetectorType("NL.TRAFFIC_LIGHT", LOOP_DETECTOR);

    static
    {
        ROAD_USERS.addCompatibleGtuType(DefaultsNl.ROAD_USER);
        VEHICLES.addCompatibleGtuType(DefaultsNl.VEHICLE);
        TRAFFIC_LIGHT.addCompatibleGtuType(DefaultsNl.BICYCLE);
    }

}
