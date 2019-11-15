package org.opentrafficsim.water.statistics;

/**
 * <br>
 * Copyright (c) 2013-2014 Rijkswaterstaat - Dienst Water, Verkeer en Leefomgeving. All rights reserved. <br>
 * Some parts of the software (c) 2011-2014 TU Delft, Faculty of TBM, Systems and Simulation <br>
 * This software is licensed without restrictions to Nederlandse Organisatie voor Toegepast Natuurwetenschappelijk Onderzoek TNO
 * (TNO), Erasmus University Rotterdam, Delft University of Technology, Panteia B.V., Stichting Projecten Binnenvaart, Ab Ovo
 * Nederland B.V., Modality Software Solutions B.V., and Rijkswaterstaat - Dienst Water, Verkeer en Leefomgeving, including the
 * right to sub-license sources and derived products to third parties. <br>
 * @version Mar 24, 2013 <br>
 * @author <a href="http://tudelft.nl/averbraeck">Alexander Verbraeck </a>
 * @version SVN $Revision: 93 $ $Author: averbraeck $ $Date: 2014-03-07 07:46:54 +0100 (Fri, 07 Mar 2014) $
 **/
@SuppressWarnings("javadoc")
public enum ContainerTransportCostBreakdownEnum
{

    TRANSPORT_SHIPPER("voor/natransport", "kosten voor voor/natransport tussen verlader en inlandterminal met de truck"),

    INLAND_TERMINAL("kosten inlandterminal",
            "kosten voor opslag container op de inlandterminal, terminaltransport op de inlandterminal, "
                    + "tussen vrachtwagen en stack, tussen stack en kade, en tussen kade en schip (1 waterzijdige move)"),

    INLAND_PORT_DUES("havengelden inlandterminals",
            "aandeel van de kosten voor de havengelden bij de achterlandterminals, inclusief havengelden voor hop-terminals"),

    SAILING_HINTERLAND("vaarkosten",
            "aandeel van de kosten voor het varen van de container op het schip " + "(inclusief stops op hop-terminals, "
                    + "en inclusief het varen in Rtm als er niet van een hub gebruik wordt gemaakt)"),

    HUB_PORT_DUES("havengelden hubterminal", "aandeel van de kosten voor de havengelden bij hubterminal(s)"),

    HUB_TERMINAL("kosten hubterminal",
            "kosten voor opslag container op de hubterminal, terminaltransport op de hubterminal, "
                    + "tussen stack en kade, en kade en schip (2 waterzijdige moves, als van toepassing)"),

    SAILING_HUB_RTM("vaarkosten Hub-Rtm",
            "aandeel van de kosten voor het varen van de container op het schip tussen Rtm en eerste hub-terminal "
                    + "(als van toepassing, inclusief stops)"),

    RTM_PORT_DUES("havengelden Rtm", "aandeel van de kosten voor de havengelden in Rtm"),

    RTM_TERMINAL("kade Rtm",
            "kosten voor het laden/lossen van de container aan de kade van de terminal in Rtm, (1 waterzijdige move)"),

    TRUCK_REPLACEMENT("vervangend truckvervoer", "kosten voor vervangend trucktransport");

    /** description for screen */
    private final String description;

    /** longer explanation */
    private final String explanation;

    /**
     * @param description String; short description of the statistic
     * @param explanation String; longer description of the statistic
     */
    private ContainerTransportCostBreakdownEnum(final String description, final String explanation)
    {
        this.description = description;
        this.explanation = explanation;
    }

    /**
     * @return description
     */
    public String getDescription()
    {
        return this.description;
    }

    /**
     * @return explanation
     */
    public String getExplanation()
    {
        return this.explanation;
    }

}
