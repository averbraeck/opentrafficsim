package org.opentrafficsim.core.unit.unitsystem;

import java.io.Serializable;
import java.util.Set;

import org.opentrafficsim.core.unit.UnitLocale;
import org.reflections.Reflections;

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

    /** CGS: centimeter-gram-second system */
    public static final CGS CGS = new CGS("UnitSystem.CGS", "UnitSystem.centimeter-gram-second_system");

    /** CGS_ESU: centimeter-gram-second system, electrostatic units */
    public static final CGS_ESU CGS_ESU = new CGS_ESU("UnitSystem.CGS_(ESU)",
            "UnitSystem.centimeter-gram-second_system,_electrostatic_units");

    /** CGS_EMU: centimeter-gram-second system, electromagnetic units */
    public static final CGS_EMU CGS_EMU = new CGS_EMU("UnitSystem.CGS_(EMU)",
            "UnitSystem.centimeter-gram-second_system,_electromagnetic_units");

    /** Imperial system */
    public static final Imperial IMPERIAL = new Imperial("UnitSystem.Imperial", "UnitSystem.Imperial_system");

    /** MTS: meter-tonne-second system */
    public static final MTS MTS = new MTS("UnitSystem.MTS", "UnitSystem.meter-tonne-second_system");

    /** Other (or no) system */
    public static final Other OTHER = new Other("UnitSystem.Other", "UnitSystem.other_system");

    /** SI units, accepted for use in addition to SI */
    public static final SIAccepted SI_ACCEPTED = new SIAccepted("UnitSystem.SI_accepted",
            "UnitSystem.International_System_of_Units_(Accepted_Unit)");

    /** SI base units: temperature, time, length, mass, luminous intensity, amount of substance and electric current */
    public static final SIBase SI_BASE = new SIBase("UnitSystem.SI",
            "UnitSystem.International_System_of_Units_(Base_Unit)");

    /** SI derived units, by combining SI-base elements (and quantifiers such as milli or kilo) */
    public static final SIDerived SI_DERIVED = new SIDerived("UnitSystem.SI_derived",
            "UnitSystem.International_System_of_Units_(Derived_Unit)");

    /** US additions to the Imperial system */
    public static final USCustomary US_CUSTOMARY = new USCustomary("UnitSystem.US_customary",
            "UnitSystem.US_customary_system");

    /** AU: Atomic Unit system */
    public static final AU AU = new AU("UnitSystem.AU", "UnitSystem.Atomic_Unit_system");

    /** the abbreviation of the unit system, such as cgs */
    private final String abbreviationKey;

    /** the name of the unit system, such as centimeter-gram-second */
    private final String nameKey;

    /** force loading of all UnitSystems */
    static
    {
        Reflections reflections = new Reflections("org.opentrafficsim.core.unit.unitsystem");
        Set<Class<? extends UnitSystem>> classes = reflections.getSubTypesOf(UnitSystem.class);

        for (Class<? extends UnitSystem> clazz : classes)
        {
            try
            {
                Class.forName(clazz.getCanonicalName());
            }
            catch (Exception exception)
            {
                // TODO: professional logging of errors
                exception.printStackTrace();
            }
        }
    }
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
