package org.opentrafficsim.core.unit;

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
 * @version May 15, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://Hansvanlint.weblog.tudelft.nl">Hans van Lint</a>
 * @author <a href="http://www.citg.tudelft.nl">Peter Knoppers</a>
 * @author <a href="http://www.citg.tudelft.nl">Guus Tamminga</a>
 * @author <a href="http://www.citg.tudelft.nl">Yufei Yuan</a>
 */
public class VelocityUnit1D<S extends DistanceUnit, T extends TimeUnit> extends VelocityUnit<S, T>
{
    private final T timeUnit;

    private final S spaceUnit;

    public final VelocityUnit1D<DistanceUnit, TimeUnit> KM_PER_HOUR = new VelocityUnit1D<DistanceUnit, TimeUnit>("kmph",
            DistanceUnit.KILOMETER, TimeUnit.HOUR);

    /**
     * @param id
     * @param spaceUnit
     * @param timeUnit
     */
    public VelocityUnit1D(String id, S spaceUnit, T timeUnit)
    {
        super(id);
        this.spaceUnit = spaceUnit;
        this.timeUnit = timeUnit;
    }

    public double getConversionFactorToMetersPerSecond()
    {
        return this.spaceUnit.getConversionFactorToMeter() / this.timeUnit.getConvertToSecond();
    }

    /**
     * @return timeUnit
     */
    public T getTimeUnit()
    {
        return this.timeUnit;
    }

    /**
     * @return spaceUnit
     */
    public S getSpaceUnit()
    {
        return this.spaceUnit;
    }

}
