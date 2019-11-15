/**
 * 
 */
package org.opentrafficsim.water.network;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.opentrafficsim.water.transfer.Terminal;

/**
 * <br>
 * Copyright (c) 2013 Rijkswaterstaat - Dienst Water, Verkeer en Leefomgeving. All rights reserved. <br>
 * Some parts of the software (c) 2011-2013 TU Delft, Faculty of TBM, Systems and Simulation <br>
 * This software is licensed without restrictions to Nederlandse Organisatie voor Toegepast Natuurwetenschappelijk Onderzoek TNO
 * (TNO), Erasmus University Rotterdam, Delft University of Technology, Panteia B.V., Stichting Projecten Binnenvaart, Ab Ovo
 * Nederland B.V., Modality Software Solutions B.V., and Rijkswaterstaat - Dienst Water, Verkeer en Leefomgeving, including the
 * right to sub-license sources and derived products to third parties. <br>
 * @version Sep 28, 2012 <br>
 * @author <a href="http://tudelft.nl/averbraeck">Alexander Verbraeck </a>
 */
public class StudyArea implements Serializable
{
    /** */
    private static final long serialVersionUID = 1L;

    /** code to identify the study area */
    private String code;

    /** the terminals in / for this sailing area */
    private List<Terminal> terminals = new ArrayList<Terminal>();

    /**
     * @param code String; code
     */
    public StudyArea(final String code)
    {
        this.code = code;
    }

    /**
     * @param terminal Terminal; terminal
     */
    public void addTerminal(final Terminal terminal)
    {
        this.terminals.add(terminal);
    }

    /**
     * @return the code
     */
    public String getCode()
    {
        return this.code;
    }

    /**
     * @return the terminals
     */
    public List<Terminal> getTerminals()
    {
        return this.terminals;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return "StudyArea " + this.code;
    }

}
