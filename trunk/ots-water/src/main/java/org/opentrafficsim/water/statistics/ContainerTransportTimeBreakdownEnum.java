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
 * @version SVN $Revision: 130 $ $Author: averbraeck $ $Date: 2014-04-03 21:21:03 +0200 (Thu, 03 Apr 2014) $
 **/
@SuppressWarnings("javadoc")
public enum ContainerTransportTimeBreakdownEnum
{

    TRANSPORT_SHIPPER("voor/natransport", "tijd in voor/natransport tussen verlader en inlandterminal met de truck"),

    INLAND_TERMINAL("inlandterminal",
            "statijd container op de inlandterminal, inclusief laad/lostijd (verwaarloosbaar), vooral van belang voor containers die van het achterland naar Rtm gaan"),

    ONSHIP_HINTERLAND("varen",
            "vaartijd container in achterland (inclusief stops op hop-terminals, en inclusief het varen in Rtm als er niet van een hub gebruik wordt gemaakt)"),

    HUB_TERMINAL("hubterminal", "statijd container op hub-terminal(s), inclusief laad/lostijd (verwaarloosbaar)"),

    ONSHIP_HUB_RTM("varen Hub-Rtm",
            "vaartijd container tussen ophalen/brengen container in Rtm en eerste hub-terminal (als van toepassing, inclusief stops)"),

    RTM_TERMINAL("Rtm-terminal",
            "statijd container in Rtm, inclusief laad/lostijd (verwaarloosbaar), vooral van belang voor containers die van Rtm naar het achterland gaan"),

    TRUCK_REPLACEMENT("vervangend truckvervoer", "tijdsduur vervangend trucktransport");

    /** description for screen. */
    private final String description;

    /** longer explanation. */
    private final String explanation;

    /**
     * @param description String; short description of the statistic
     * @param explanation String; longer description of the statistic
     */
    private ContainerTransportTimeBreakdownEnum(final String description, final String explanation)
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
