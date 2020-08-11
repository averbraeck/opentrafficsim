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
public enum SailingServiceTimeBreakdownEnum
{

    INLAND_WAIT_LOADS("wachten lading inland", "wachttijd op lading bij de inlandterminal(s), inclusief hopterminals"),

    INLAND_WAIT_QUAY("wachten kade inland", "wachttijd op kade bij de inlandterminal(s), inclusief hopterminals"),

    INLAND_WAIT_TRANSPORT("wachten kadetransport inland",
            "wachttijd op kadetransport bij de inlandterminal(s), inclusief hopterminals"),

    INLAND_LOADING("laden inlandterminal", "laadtijd inlandterminal(s), inclusief hopterminals"),

    INLAND_UNLOADING("lossen inlandterminal", "lostijd inlandterminal(s), inclusief hopterminals"),

    SAILING_HINTERLAND("varen achterland", "vaartijd container in achterland van / tot de eerste terminal in Rtm"),

    HUB_WAIT_LOADS("wachten lading Hub", "wachttijd op voldoende groot ladingpakket bij de hub"),

    HUB_WAIT_QUAY("wachten kade Hub", "wachttijd op kade bij de hubterminal(s)"),

    HUB_WAIT_TRANSPORT("wachten kadetransport Hub", "wachttijd op kadetransport bij de hubterminal(s)"),

    HUB_LOADING("laden terminal Hub", "laadtijd hubterminal(s)"),

    HUB_UNLOADING("lossen terminal Hub", "lostijd hubterminal(s)"),

    RTM_DS_WAIT_QUAY("wachten kade Rtm-DS", "wachttijd op kade bij deepsea terminals in Rtm"),

    RTM_DS_WAIT_TRANSPORT("wachten kadetransport Rtm-DS", "wachttijd op kadetransport bij deepsea terminals in Rtm"),

    RTM_DS_LOADING("laden terminal Rtm-DS", "laadtijd bij deepsea terminals in Rtm"),

    RTM_DS_UNLOADING("lossen terminal Rtm-DS", "lostijd bij deepsea terminals in Rtm"),

    RTM_ED_WAIT_QUAY("wachten kade Rtm-MTD", "wachttijd op kade bij empty depot terminals in Rtm"),

    RTM_ED_WAIT_TRANSPORT("wachten kadetransport Rtm-MTD", "wachttijd op kadetransport bij empty depot terminals in Rtm"),

    RTM_ED_LOADING("laden terminal Rtm-MTD", "laadtijd bij empty depot terminals in Rtm"),

    RTM_ED_UNLOADING("lossen terminal Rtm-MTD", "lostijd bij empty depot terminals in Rtm"),

    RTM_WAIT_LOADS("wachten lading Rtm", "wachttijd op voldoende groot ladingpakket in Rotterdam"),

    SAILING_RTM("varen Rtm", "vaartijd container tussen terminals in Rtm");

    /** description for screen. */
    private final String description;

    /** longer explanation. */
    private final String explanation;

    /**
     * @param description String; the short description of the enum
     * @param explanation String; the long explanation of the enum
     */
    private SailingServiceTimeBreakdownEnum(final String description, final String explanation)
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
