package floatVectorDemo;

import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.unit.TemperatureUnit;
import org.opentrafficsim.core.value.ValueException;
import org.opentrafficsim.core.value.vfloat.vector.FloatVector;
import org.opentrafficsim.core.value.vfloat.vector.MutableFloatVector;

/**
 * Demonstrate use of FloatVector and MutableFloatVector.
 * <p>
 * Copyright (c) 2002-2014 Delft University of Technology, Jaffalaan 5, 2628 BX Delft, the Netherlands. All rights
 * reserved.
 * <p>
 * See for project information <a href="http://www.simulation.tudelft.nl/"> www.simulation.tudelft.nl</a>.
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
 * @version Sep 2, 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public final class FloatVectorDemo
{
    /** 
     * Prevent instantiation of this class.
     */
    private FloatVectorDemo()
    {
        // This class should never be instantiated.
    }
    
    /**
     * Demonstrate the use of FloatVector and MutableFloatVector.
     * @param args String[]; the command line argument; not used
     */
    public static void main(final String[] args)
    {
        System.out.println("Creating FloatVector.Abs.Dense fad");
        FloatVector.Abs.Dense<LengthUnit> fad =
                new FloatVector.Abs.Dense<LengthUnit>(new float[]{1.0f, 2.0f, 3.0f}, LengthUnit.MILE);
        System.out.println("fad:             " + fad.toString());
        
        System.out.println("Creating FloatVector.Rel.Dense frd");
        FloatVector.Rel.Dense<LengthUnit> frd =
                new FloatVector.Rel.Dense<LengthUnit>(new float[]{4.0f, 5.0f, 6.0f}, LengthUnit.KILOMETER);
        System.out.println("frd:             " + frd.toString());
        
        System.out.println("Creating FloatVector.Abs.Dense fadsum by adding fdb to fda");
        MutableFloatVector.Abs.Dense<LengthUnit> fadsum = null;
        try
        {
            fadsum = MutableFloatVector.plus(fad, frd);
        }
        catch (ValueException exception)
        {
            exception.printStackTrace();
        }
        System.out.println("fadsum:          " + fadsum.toString());

        System.out.println("Creating MutableFloatVector.Abs.Dense mfad");
        MutableFloatVector.Abs.Dense<TemperatureUnit> mfad =
                new MutableFloatVector.Abs.Dense<TemperatureUnit>(new float[]{1.0f, 2.0f, 3.0f}, TemperatureUnit.KELVIN);
        System.out.println("mafa:            " + mfad.toString());
        System.out.println("Making immutable version ifad of mfad");
        FloatVector.Abs.Dense<TemperatureUnit> ifad = mfad.immutable();
        System.out.println("ifad:            " + ifad);
        System.out.println("normalizing mfad");
        try
        {
            mfad.normalize();
        }
        catch (ValueException exception)
        {
            exception.printStackTrace();
        }
        System.out.println("normalized mfad: " + mfad.toString());
        System.out.println("ifad:            " + ifad);
    }
}
