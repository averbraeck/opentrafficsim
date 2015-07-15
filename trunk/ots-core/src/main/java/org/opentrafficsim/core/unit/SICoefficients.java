package org.opentrafficsim.core.unit;

import java.util.EnumMap;

/**
 * <p>
 * Copyright (c) 2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author: pknoppers
 * $, initial versionJun 15, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class SICoefficients
{
    /** the map with SI base units and corresponding coefficients. */
    private final EnumMap<SI, Integer> coefficientsMap;

    /**
     * Construct an instance of SICoefficients.
     * @param coefficients the map with SI base units and corresponding coefficients
     */
    protected SICoefficients(final EnumMap<SI, Integer> coefficients)
    {
        this.coefficientsMap = coefficients;
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return enumMapToString(this.coefficientsMap);
    }

    /**
     * Convert an enumMap of coefficient to the normalized string representation.
     * @param map EnumMap&lt;{@link SI}, Integer&gt;; the EnumMap
     * @return String
     */
    protected static String enumMapToString(final EnumMap<SI, Integer> map)
    {
        StringBuffer result = new StringBuffer();
        boolean first = true;
        for (SI si : map.keySet())
        {
            if (map.get(si) > 0)
            {
                if (first)
                {
                    first = false;
                }
                else
                {
                    result.append(".");
                }
                result.append(si.name());
                if (map.get(si) != 1)
                {
                    result.append(map.get(si));
                }
            }
        }

        if (result.length() == 0)
        {
            result.append("1");
        }

        for (SI si : map.keySet())
        {
            if (map.get(si) < 0)
            {
                result.append("/" + si.name());
                if (map.get(si) != -1)
                {
                    result.append(-map.get(si));
                }
            }
        }
        return result.toString();
    }

    /**
     * @return coefficientsMap
     */
    public final EnumMap<SI, Integer> getCoefficientsMap()
    {
        return this.coefficientsMap;
    }

    /**
     * Convert a coefficient string to <i>standard format</i>.
     * @param coefficientString String; the string to convert
     * @return String; the normalized coefficient string
     * @throws UnitException when the coefficientString could not be parsed
     */
    public static String normalize(final String coefficientString) throws UnitException
    {
        return enumMapToString(parse(coefficientString));
    }

    /**
     * @param coefficientString such as kgm/s2 or kg-2m^3/s2A or Kmmol3/Askcd4 or mol. <br>
     *            The grammar of a coefficientString is:<br>
     *            <table summary="">
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
     * <br>
     *            White space can appear anywhere in a coefficientString. <br>
     *            If "integer" does not fit in an Integer, the resulting coefficient will be very wrong.
     * @return an instance of SICoefficients
     * @throws UnitException if the coefficientString is not parsable.
     */
    public static EnumMap<SI, Integer> parse(final String coefficientString) throws UnitException
    {
        // System.out.println("coefficientString is \"" + coefficientString + "\"");
        EnumMap<SI, Integer> coefficients = new EnumMap<SI, Integer>(SI.class);
        String cs = coefficientString;
        cs = cs.replace(".", "").replace(" ", "");
        if (cs.equals("1")) // This is a special case...
        {
            return coefficients;
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
                {
                    throw new UnitException("No SI name after slash in " + coefficientString);
                }
                factor = -1;
            }
            boolean parsedPowerString = false;
            for (SI si : SI.values())
            {
                String name = si.name();
                if (!cs.startsWith(name))
                {
                    continue;
                }
                int endPos = name.length();
                if (cs.substring(endPos).startsWith("ol"))
                {
                    continue; // Don't confuse "m" (for meter) and "mol"
                }
                // Found the unit name
                if (cs.substring(endPos).startsWith("^"))
                {
                    endPos++;
                }
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
                    if (digit >= '0' && digit <= '9')
                    {
                        if (0 == digitsSeen)
                        {
                            value = 0;
                        }
                        value = value * 10 + digit - '0';
                        endPos++;
                        digitsSeen++;
                    }
                    else
                    {
                        break;
                    }
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
            {
                throw new UnitException("Not an SI unit name in \"" + coefficientString + "\" at \"" + cs + "\"");
            }
        }
        return coefficients;
    }

    /**
     * @param a the first set of coefficients
     * @param b the second set of coefficients
     * @return the coefficients of a*b (coefficients are added)
     */
    public static SICoefficients multiply(final SICoefficients a, final SICoefficients b)
    {
        EnumMap<SI, Integer> coefficients = new EnumMap<SI, Integer>(SI.class);
        for (SI si : a.getCoefficientsMap().keySet())
        {
            coefficients.put(si, a.getCoefficientsMap().get(si));
        }

        for (SI si : b.getCoefficientsMap().keySet())
        {
            if (coefficients.containsKey(si))
            {
                coefficients.put(si, coefficients.get(si) + b.getCoefficientsMap().get(si));
            }
            else
            {
                coefficients.put(si, b.getCoefficientsMap().get(si));
            }
        }

        for (SI si : coefficients.keySet())
        {
            if (coefficients.get(si) == 0)
            {
                coefficients.remove(si);
            }
        }
        return new SICoefficients(coefficients);
    }

    /**
     * @param a the first set of coefficients
     * @param b the second set of coefficients
     * @return the coefficients of a/b (coefficients are subtracted)
     */
    public static SICoefficients divide(final SICoefficients a, final SICoefficients b)
    {
        EnumMap<SI, Integer> coefficients = new EnumMap<SI, Integer>(SI.class);
        for (SI si : a.getCoefficientsMap().keySet())
        {
            coefficients.put(si, a.getCoefficientsMap().get(si));
        }

        for (SI si : b.getCoefficientsMap().keySet())
        {
            if (coefficients.containsKey(si))
            {
                coefficients.put(si, coefficients.get(si) - b.getCoefficientsMap().get(si));
            }
            else
            {
                coefficients.put(si, -b.getCoefficientsMap().get(si));
            }
        }

        for (SI si : coefficients.keySet())
        {
            if (coefficients.get(si) == 0)
            {
                coefficients.remove(si);
            }
        }
        return new SICoefficients(coefficients);
    }

}
