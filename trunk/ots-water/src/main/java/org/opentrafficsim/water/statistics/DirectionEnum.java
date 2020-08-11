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
public enum DirectionEnum
{

    /** total. */
    TOTAL("van/naar Rtm"),

    /** From Rotterdam. */
    FROMRTM("van Rtm"),

    /** To Rotterdam. */
    TORTM("naar Rtm");

    /** description for screen. */
    private final String description;

    /**
     * @param description String; description of the enum
     */
    private DirectionEnum(final String description)
    {
        this.description = description;
    }

    /**
     * @return description
     */
    public String getDescription()
    {
        return this.description;
    }
}
