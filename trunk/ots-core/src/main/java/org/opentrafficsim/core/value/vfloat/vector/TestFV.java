package org.opentrafficsim.core.value.vfloat.vector;

import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.unit.TemperatureUnit;
import org.opentrafficsim.core.value.ValueException;

/**
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
public class TestFV
{
    /**
     * @param args
     */
    public static void main(final String[] args)
    {
        System.out.println("Creating FloatVector.Dense.Abs fda");
        FloatVector.Dense.Abs<LengthUnit> fda =
                new FloatVector.Dense.Abs<LengthUnit>(new float[]{1.0f, 2.0f, 3.0f}, LengthUnit.MILE);
        System.out.println("fda:             " + fda.toString());
        
        System.out.println("Creating FloatVector.Dense.Rel fdr");
        FloatVector.Dense.Rel<LengthUnit> fdr =
                new FloatVector.Dense.Rel<LengthUnit>(new float[]{4.0f, 5.0f, 6.0f}, LengthUnit.KILOMETER);
        System.out.println("fdr:             " + fdr.toString());
        
        System.out.println("Creating FloatVector.Dense.Abs fdsum by adding fdb to fda");
        MutableFloatVector.Dense.Abs<LengthUnit> fdsum = null;
        try
        {
            fdsum = MutableFloatVector.plus(fda, fdr);
        }
        catch (ValueException exception)
        {
            exception.printStackTrace();
        }
        System.out.println("fdsum:           " + fdsum.toString());

        System.out.println("Creating MutableFloatVector.Dense.Abs mfda");
        MutableFloatVector.Dense.Abs<TemperatureUnit> mfda =
                new MutableFloatVector.Dense.Abs<TemperatureUnit>(new float[]{1.0f, 2.0f, 3.0f}, TemperatureUnit.KELVIN);
        System.out.println("mfda:            " + mfda.toString());
        System.out.println("Making immutable version ifda of mfda");
        FloatVector.Dense.Abs<TemperatureUnit> ifda = mfda.immutable();
        System.out.println("ifda:            " + ifda);
        System.out.println("normalizing mfda");
        try
        {
            mfda.normalize();
        }
        catch (ValueException exception)
        {
            exception.printStackTrace();
        }
        System.out.println("normalized mfda: " + mfda.toString());
        System.out.println("ifda:            " + ifda);
    }
}
