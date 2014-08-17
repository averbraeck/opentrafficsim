package org.opentrafficsim.core.value;

import org.opentrafficsim.core.unit.Unit;

/**
 * Value is a static interface that implements a couple of unit-related static methods.
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
 * @version Aug 18, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @param <U> the unit type.
 */
public interface Value<U extends Unit<U>>
{
    /**
     * @return unit
     */
    U getUnit();
    
    /**
     * @param value the value to convert in SI units
     * @return the value in SI units
     */
    double expressAsSIUnit(final double value);

    /**
     * Set a new unit for displaying the results.
     * @param newUnit the new unit of the right unit type
     */
    void setDisplayUnit(final U newUnit);

    /**
     * @return whether the value is absolute.
     */
    boolean isAbsolute();

    /**
     * @return whether the value is relative.
     */
    boolean isRelative();

    /**
     * @return a copy of the object
     */
    public abstract Value<U> copy();
}
