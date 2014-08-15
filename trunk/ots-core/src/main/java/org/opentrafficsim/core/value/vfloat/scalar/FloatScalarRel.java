package org.opentrafficsim.core.value.vfloat.scalar;

import org.opentrafficsim.core.unit.Unit;
import org.opentrafficsim.core.value.Relative;
import org.opentrafficsim.core.value.Scalar;

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
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @param <U> the unit of the values in the constructor and for display
 */
public class FloatScalarRel<U extends Unit<U>> extends FloatScalar<U> implements Relative,
        Comparable<FloatScalarRel<U>>
{
    /** */
    private static final long serialVersionUID = 20140615L;

    /**
     * Construct a value in and store it in SI units for calculation.
     * @param value the value in the given units
     * @param unit the unit of the value
     */
    public FloatScalarRel(final float value, final U unit)
    {
        super(value, unit);
    }

    /**
     * Construct a value from another value. The value is already in SI units.
     * @param value the value to duplicate
     */
    public FloatScalarRel(final FloatScalarRel<U> value)
    {
        super(value);
    }

    /**
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(final FloatScalarRel<U> fsr)
    {
        if (this.valueSI < fsr.valueSI)
            return -1;
        if (this.valueSI > fsr.valueSI)
            return 1;
        return 0;
    }

    /**
     * @see org.opentrafficsim.core.value.Scalar#copy()
     */
    @Override
    public Scalar<U> copy()
    {
        return new FloatScalarRel<U>(this);
    }
}
