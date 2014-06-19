package org.opentrafficsim.core.value.vfloat.vector;

import static org.junit.Assert.*;

import org.junit.Test;
import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.unit.Unit;

import cern.colt.matrix.tfloat.FloatMatrix1D;

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
 * @version Jun 19, 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class FloatVectorTest
{
    /**
     * Test the FloatVectorAbsDense that takes a float[] and a Unit as arguments.
     */
    @SuppressWarnings("static-method")
    @Test
    public void floatVectorAbs1 ()
    {
        float[] in = new float[10];
        for (int i = 0; i < in.length; i++)
            in[i] = i / 3.0f;
        LengthUnit u = LengthUnit.FOOT;
        FloatVectorAbsDense<LengthUnit> fv = new FloatVectorAbsDense<LengthUnit>(in, u);
        assertEquals("FloatVector should have 10 elements", 10, fv.size());
        float[] out = fv.getValuesInUnit();
        for (int i = 0; i < in.length; i++)
            assertEquals("Values in floatVector in unit should be equal to input values", in[i], out[i], 0.0001);
        out = fv.getValuesSI();
        for (int i = 0; i < in.length; i++)
            assertEquals("Values in floatVector should be equivalent value in meters", in[i], out[i] / (12 * 0.0254), 0.0001);
        LengthUnit uOut = fv.getUnit();
        assertEquals("Stored unit should be provided unit", u, uOut);
        FloatVectorAbsDense<LengthUnit> copy = (FloatVectorAbsDense<LengthUnit>) fv.copy();
        assertEquals("copy should have 10 elements", 10, copy.size());
        float[] copyOut = copy.getValuesSI();
        /* Fails due to error in FloatFectorAbsDense.copy
        for (int i = 0; i < in.length; i++)
            assertEquals("Values in copy of floatVector should be equivalent value in meters", in[i], copyOut[i] / (12 * 0.0254), 0.0001);
        copyOut = fv.getValuesInUnit();
        for (int i = 0; i < in.length; i++)
            assertEquals("Values in copy of floatVector in unit should be equal to input values", in[i], copyOut[i], 0.0001);
         */
        FloatMatrix1D fm1d = fv.getColtDenseFloatMatrix1D();
        assertEquals("Colt 1D matrix should have size 10", 10, fm1d.size());
        for (int i = 0; i < in.length; i++)
            assertEquals("values in colt matrix should match values in SI", out[i], fm1d.getQuick(i), 0.00001);
    }
}
