package org.opentrafficsim.core.unit.unitsystem;

import java.io.Serializable;

import org.opentrafficsim.core.unit.UnitLocale;

/**
 * Systems of Units such as SI, including SI-derived; cgs (centimeter-gram-second).
 * <p>
 * Copyright (c) 2014 Delft University of Technology, Jaffalaan 5, 2628 BX Delft, the Netherlands. All rights reserved.
 * <p>
 * See for project information <a href="http://www.opentrafficsim.org/"> www.opentrafficsim.org</a>.
 * <p>
 * The OpenTrafficSim project is distributed under the following BSD-style license:<br>
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 * <ul>
 * <li>Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 * disclaimer.</li>
 * <li>Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
 * following disclaimer in the documentation and/or other materials provided with the distribution.</li>
 * <li>Neither the name of Delft University of Technology, nor the names of its contributors may be used to endorse or
 * promote products derived from this software without specific prior written permission.</li>
 * </ul>
 * This software is provided by the copyright holders and contributors "as is" and any express or implied warranties,
 * including, but not limited to, the implied warranties of merchantability and fitness for a particular purpose are
 * disclaimed. In no event shall the copyright holder or contributors be liable for any direct, indirect, incidental,
 * special, exemplary, or consequential damages (including, but not limited to, procurement of substitute goods or
 * services; loss of use, data, or profits; or business interruption) however caused and on any theory of liability,
 * whether in contract, strict liability, or tort (including negligence or otherwise) arising in any way out of the use
 * of this software, even if advised of the possibility of such damage.
 * @version Jun 6, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public abstract class UnitSystem implements Serializable
{
    /** */
    private static final long serialVersionUID = 20140606L;

    /** the abbreviation of the unit system, such as cgs */
    private final String abbreviationKey;

    /** the name of the unit system, such as centimeter-gram-second */
    private final String nameKey;

    /**
     * @param abbreviationKey the abbreviation of the unit system, such as cgs
     * @param nameKey the name of the unit system, such as centimeter-gram-second
     */
    protected UnitSystem(final String abbreviationKey, final String nameKey)
    {
        this.abbreviationKey = abbreviationKey;
        this.nameKey = nameKey;
    }

    /**
     * @return name, e.g. centimeter-gram-second
     */
    public String getName()
    {
        return UnitLocale.getString(this.nameKey);
    }

    /**
     * @return name key, e.g. CGS.centimeter-gram-second
     */
    public String getNameKey()
    {
        return this.nameKey;
    }

    /**
     * @return abbreviation, e.g., CGS.cgs
     */
    public String getAbbreviation()
    {
        return UnitLocale.getString(this.abbreviationKey);
    }

    /**
     * @return abbreviation key, e.g. cgs
     */
    public String getAbbreviationKey()
    {
        return this.abbreviationKey;
    }

}
