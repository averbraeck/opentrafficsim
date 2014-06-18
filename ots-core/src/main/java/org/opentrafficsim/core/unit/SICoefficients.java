package org.opentrafficsim.core.unit;

import java.util.EnumMap;

/**
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
 * @version Jun 15, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class SICoefficients
{
    /** the map with SI base units and corresponding coefficients */
    private final EnumMap<SI, Integer> coefficientsMap;

    /**
     * Construct an instance of SICoefficients.
     * @param coefficients the map with SI base units and corresponding coefficients
     */
    protected SICoefficients(final EnumMap<SI, Integer> coefficients)
    {
        this.coefficientsMap = coefficients;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        String s = "";
        boolean first = true;
        for (SI si : this.coefficientsMap.keySet())
        {
            if (this.coefficientsMap.get(si) > 0)
            {
                if (first)
                    first = false;
                else
                    s += ".";
                s += si.name();
                if (this.coefficientsMap.get(si) != 1)
                    s += this.coefficientsMap.get(si);
            }
        }

        if (s.length() == 0)
            s += "1";

        first = true;
        for (SI si : this.coefficientsMap.keySet())
        {
            if (this.coefficientsMap.get(si) < 0)
            {
                s += "/";
                first = false;
                s += si.name();
                if (this.coefficientsMap.get(si) != -1)
                    s += (-this.coefficientsMap.get(si));
            }
        }
        return s;
    }

    /**
     * @return coefficientsMap
     */
    public EnumMap<SI, Integer> getCoefficientsMap()
    {
        return this.coefficientsMap;
    }

    /**
     * @param coefficientString such as kgm/s2 or kg-2m^3/s2A or Kmmol3/Askcd4 or mol. <br />
     *            The grammar of a coefficientString is:<br />
     *            <table>
     *            <tr>
     *            <td>coefficientString</td>
     *            <td>::=</td>
     *            <td>&lt;empty&gt; | [ 1 | powerString ] | [ '1 /' powerString ]</td>
     *            </tr>
     *            <tr>
     *            <td>powerString</td>
     *            <td>::=</td>
     *            <td>unitName [ [ ^ ] integer ] [ [ dotOrSlash ] powerString ]</td>
     *            </tr>
     *            <tr>
     *            <td>dotOrSlash</td>
     *            <td>::=</td>
     *            <td>. | /</td>
     *            </tr>
     *            <tr>
     *            <td>unitName</td>
     *            <td>::=</td>
     *            <td>kg | m | s | A | K | cd | mol</td>
     *            </tr>
     *            </table>
     * <br />
     *            White space can appear anywhere in a coefficientString. <br />
     *            If "integer" does not fit in an Integer, the resulting coefficient will be very wrong.
     * @return an instance of SICoefficients
     * @throws UnitException 
     */
    public static SICoefficients create(final String coefficientString) throws UnitException
    {
        // System.out.println("coefficientString is \"" + coefficientString + "\"");
        EnumMap<SI, Integer> coefficients = new EnumMap<SI, Integer>(SI.class);
        String cs = coefficientString;
        cs = cs.replace("^", "").replace(".", "").replace(" ", "");
        if (cs.equals("1")) // This is a special case...
        {
            return new SICoefficients(coefficients);
        }
        if (cs.startsWith("1/"))
        {
            cs = cs.substring(1); // remove the leading "1"
        }
        while (cs.length() > 0)
        {
            int factor = 1;
            if (cs.startsWith("/"))
            {
                cs = cs.substring(1);
                if (cs.length() < 1)
                    throw new UnitException("No SI name after slash in " + coefficientString);
                factor = -1;
            }
            boolean parsedPowerString = false;
            for (SI si : SI.values())
            {
                String name = si.name();
                if (!cs.startsWith(name))
                    continue;
                int endPos = name.length();
                if (cs.substring(endPos).startsWith("ol"))
                    continue; // Don't confuse "m" (for meter) and "mol"
                // Found the unit name
                if (cs.substring(endPos).startsWith("^"))
                    endPos++;
                int value = 1;
                int digitsSeen = 0;
                if (cs.substring(endPos).startsWith("-"))
                {
                    factor *= -1;
                    endPos++;
                }
                while (cs.length() > endPos)
                {
                    char digit = cs.charAt(endPos);
                    if (digit >= '0' && (digit <= '9'))
                    {
                        if (0 == digitsSeen)
                            value = 0;
                        value = value * 10 + (digit - '0');
                        endPos++;
                        digitsSeen++;
                    }
                    else
                        break;
                }
                Integer oldValue = coefficients.get(si);
                if (null == oldValue)
                {
                    oldValue = 0;
                }
                coefficients.put(si, oldValue + value * factor);
                parsedPowerString = true;
                cs = cs.substring(endPos);
                break;
            }
            if (!parsedPowerString)
                throw new UnitException("Not an SI unit name in \"" + coefficientString + "\" at \"" + cs + "\"");
        }
        return new SICoefficients(coefficients);
    }

    /**
     * @param a the first set of coefficients
     * @param b the second set of coefficients
     * @return the coefficients of a*b (coefficients are added)
     */
    public static SICoefficients multiply(SICoefficients a, SICoefficients b)
    {
        EnumMap<SI, Integer> coefficients = new EnumMap<SI, Integer>(SI.class);
        for (SI si : a.getCoefficientsMap().keySet())
            coefficients.put(si, a.getCoefficientsMap().get(si));

        for (SI si : b.getCoefficientsMap().keySet())
        {
            if (coefficients.containsKey(si))
                coefficients.put(si, coefficients.get(si) + b.getCoefficientsMap().get(si));
            else
                coefficients.put(si, b.getCoefficientsMap().get(si));
        }

        for (SI si : coefficients.keySet())
        {
            if (coefficients.get(si) == 0)
                coefficients.remove(si);
        }
        return new SICoefficients(coefficients);
    }

    /**
     * @param a the first set of coefficients
     * @param b the second set of coefficients
     * @return the coefficients of a/b (coefficients are subtracted)
     */
    public static SICoefficients divide(SICoefficients a, SICoefficients b)
    {
        EnumMap<SI, Integer> coefficients = new EnumMap<SI, Integer>(SI.class);
        for (SI si : a.getCoefficientsMap().keySet())
            coefficients.put(si, a.getCoefficientsMap().get(si));

        for (SI si : b.getCoefficientsMap().keySet())
        {
            if (coefficients.containsKey(si))
                coefficients.put(si, coefficients.get(si) - b.getCoefficientsMap().get(si));
            else
                coefficients.put(si, -b.getCoefficientsMap().get(si));
        }

        for (SI si : coefficients.keySet())
        {
            if (coefficients.get(si) == 0)
                coefficients.remove(si);
        }
        return new SICoefficients(coefficients);
    }

}
