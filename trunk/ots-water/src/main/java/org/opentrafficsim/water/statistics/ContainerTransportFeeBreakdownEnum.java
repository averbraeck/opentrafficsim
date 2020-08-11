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
 * @version SVN $Revision: 97 $ $Author: averbraeck $ $Date: 2014-03-11 14:52:52 +0100 (Tue, 11 Mar 2014) $
 **/
@SuppressWarnings("javadoc")
public enum ContainerTransportFeeBreakdownEnum
{

    TRANSPORT_SHIPPER("voor/natransport", "tarief voor voor/natransport tussen verlader en inlandterminal met de truck"),

    INLAND_TERMINAL("move inlandterminal",
            "tarief voor opslag container op de inlandterminal, horizontaal transport, en kadeoverslag (1 move)"),

    SAILING_HINTERLAND("vaartarief",
            "tarief voor het vervoeren van de container op het schip (inclusief het varen in Rtm "
                    + "als er niet van een hub gebruik wordt gemaakt, en inclusief havengelden)"),

    HUB_TERMINAL("moves hub",
            "tarief voor opslag container op de hubterminal, horizontaal transport, en kadeoverslag (2 moves)"),

    SAILING_HUB_RTM("vaartarief Hub-Rtm",
            "tarief voor het varen van de container op het schip tussen Rtm en eerste hub-terminal "
                    + "(als van toepassing, en inclusief havengelden)"),

    RTM_TERMINAL("move Rtm", "tarief voor het laden/lossen van de container aan de kade van de terminal in Rtm (1 move)"),

    TRUCK_REPLACEMENT("vervangend truckvervoer", "tarief voor vervangend trucktransport");

    /*-
    INLAND_PORT_DUES("tarief tbv havengelden inlandterminals",
            "aandeel van de kosten voor het tarief tbv de havengelden bij de achterlandterminals, "
                    + "inclusief havengelden voor hop-terminals"),
    
    HUB_PORT_DUES("tarief tbv havengelden hubterminal", "aandeel van het tarief tbv de kosten voor "
            + "de havengelden bij hubterminal(s)"),
    
    RTM_PORT_DUES("tarief tbv havengelden Rtm", "aandeel van het tarief tbv de kosten voor de havengelden in Rtm");
     */

    /** description for screen. */
    private final String description;

    /** longer explanation. */
    private final String explanation;

    /**
     * @param description String; short description of the statistic
     * @param explanation String; longer description of the statistic
     */
    private ContainerTransportFeeBreakdownEnum(final String description, final String explanation)
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
