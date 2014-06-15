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
                if (first)
                {
                    s += "/";
                    first = false;
                }
                else
                    s += ".";
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
     * @param coefficientString such as kgm/s2 or kg-2m^3/s2A or Kmmol3/Askcd4 or mol.
     * @return an instance of SICoefficients
     */
    public static SICoefficients create(final String coefficientString)
    {
        EnumMap<SI, Integer> coefficients = new EnumMap<SI, Integer>(SI.class);
        String cs = coefficientString;
        cs = cs.replace("^", "").replace(".", "").replace(" ", "");
        int slash = cs.indexOf('/') >= 0 ? cs.indexOf('/') : cs.length();
        for (SI si : SI.values())
        {
            String name = si.name();
            int i = cs.indexOf(name);
            if (i >= 0)
            {
                char m = ' ';
                char n = ' ';
                if (i + name.length() < cs.length())
                    m = cs.charAt(i + name.length());
                if (m != 'o') // catch the 'mol' versus the 'meter'
                {
                    if (i + name.length() + 1 < cs.length())
                        n = cs.charAt(i + name.length() + 1);
                    int coefficient = 1;
                    if (m >= '0' && m <= '9')
                    {
                        coefficient = m - '0';
                    }
                    else
                    {
                        if (m == '-' && n >= '0' && n <= '9')
                            coefficient = -(n - '0');
                    }
                    if (i > slash)
                        coefficient = -coefficient;
                    coefficients.put(si, coefficient);
                }
            }
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
