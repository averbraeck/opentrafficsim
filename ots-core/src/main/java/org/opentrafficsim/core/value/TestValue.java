package org.opentrafficsim.core.value;

import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.unit.SpeedUnit;
import org.opentrafficsim.core.unit.TimeUnit;
import org.opentrafficsim.core.value.vfloat.scalar.FloatScalar;
import org.opentrafficsim.core.value.vfloat.scalar.FloatScalarAbs;
import org.opentrafficsim.core.value.vfloat.vector.FloatVector;
import org.opentrafficsim.core.value.vfloat.vector.FloatVectorAbs;
import org.opentrafficsim.core.value.vfloat.vector.FloatVectorAbsDense;
import org.opentrafficsim.core.value.vfloat.vector.FloatVectorRel;
import org.opentrafficsim.core.value.vfloat.vector.FloatVectorRelSparse;

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
 * @version Jun 13, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class TestValue
{

    /**
     * 
     */
    public TestValue()
    {
        FloatScalarAbs<LengthUnit> l = new FloatScalarAbs<LengthUnit>(1.0f, LengthUnit.KILOMETER);
        FloatScalarAbs<TimeUnit> t = new FloatScalarAbs<TimeUnit>(1.0f, TimeUnit.HOUR);
        FloatScalarAbs<?> div = FloatScalar.divide(l, t);
        System.out.println(div);

        System.out.println();
        float[] f = new float[]{1.0f, 2.0f, 3.0f, 4.0f};
        FloatVectorAbs<SpeedUnit> v = new FloatVectorAbsDense<SpeedUnit>(f, SpeedUnit.KM_PER_HOUR);
        float[] g = new float[]{5.0f, 6.0f, 7.0f, 8.0f};
        FloatVectorRel<SpeedUnit> w = new FloatVectorRelSparse<SpeedUnit>(g, SpeedUnit.METER_PER_SECOND);
        System.out.println(v);
        System.out.println(w);
        try
        {
            System.out.println(FloatVector.plus(v, w).toString(SpeedUnit.METER_PER_SECOND));
            System.out.println(FloatVector.plus(w, w).toString(SpeedUnit.METER_PER_SECOND));
            System.out.println(FloatVector.plus(w, v).toString(SpeedUnit.KM_PER_HOUR));
            System.out.println(FloatVector.plus(w, v).toString(SpeedUnit.MILE_PER_HOUR));
        }
        catch (ValueException ve)
        {
            ve.printStackTrace();
        }
        
        // TODO: All in SI
    }

    /**
     * @param args String[]; program arguments (not used)
     */
    public static void main(final String[] args)
    {
        new TestValue();
    }
}
