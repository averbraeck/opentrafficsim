package org.opentrafficsim.core.unit;

/**
 * <p>
 * Copyright (c) 2014 Delft University of Technology, Jaffalaan 5, 2628 BX
 * Delft, the Netherlands. All rights reserved.
 * 
 * <p>
 * See for project information <a href="http://www.opentrafficsim.org/">
 * www.opentrafficsim.org</a>.
 * <p>
 * The OpenTrafficSim project is distributed under the following BSD-style license:<br>
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * <ul>
 * <li>Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.</li>
 * <li>Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.</li>
 * <li>Neither the name of Delft University of Technology, nor the names of its
 * contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.</li>
 * </ul>
 * This software is provided by the copyright holders and contributors "as is"
 * and any express or implied warranties, including, but not limited to, the
 * implied warranties of merchantability and fitness for a particular purpose
 * are disclaimed. In no event shall the copyright holder or contributors be
 * liable for any direct, indirect, incidental, special, exemplary, or
 * consequential damages (including, but not limited to, procurement of
 * substitute goods or services; loss of use, data, or profits; or business
 * interruption) however caused and on any theory of liability, whether in
 * contract, strict liability, or tort (including negligence or otherwise)
 * arising in any way out of the use of this software, even if advised of the
 * possibility of such damage.
 * 
 * @version Jun 4, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class Test
{
    /**
     * @param args
     */
    public static void main(String[] args)
    {
        System.out.println(TimeUnit.MINUTE.getConversionFactorToStandardUnit());
        System.out.println(TimeUnit.WEEK.getConversionFactorToStandardUnit());
        System.out.println(TimeUnit.MILLISECOND.getConversionFactorToStandardUnit());
        System.out.println();
        System.out.println(LengthUnit.KILOMETER.getConversionFactorToStandardUnit());
        System.out.println(LengthUnit.MILE.getConversionFactorToStandardUnit());
        System.out.println(LengthUnit.FOOT.getConversionFactorToStandardUnit());
        System.out.println(LengthUnit.INCH.getConversionFactorToStandardUnit());
        System.out.println();
        System.out.println(SpeedUnit.KM_PER_HOUR.getConversionFactorToStandardUnit());
        System.out.println(SpeedUnit.MILE_PER_HOUR.getConversionFactorToStandardUnit());
        System.out.println();
        System.out.println(AreaUnit.HECTARE.getConversionFactorToStandardUnit());
        System.out.println(AreaUnit.SQUARE_FOOT.getConversionFactorToStandardUnit());
        System.out.println();
        System.out.println(AccelerationUnit.KM_PER_HOUR_2.getConversionFactorToStandardUnit());
        System.out.println(AccelerationUnit.MILE_PER_SECOND_2.getConversionFactorToStandardUnit());
        System.out.println(AccelerationUnit.KNOT_PER_SECOND.getConversionFactorToStandardUnit());
        System.out.println(AccelerationUnit.MILE_PER_HOUR_2.getConversionFactorToStandardUnit());
        System.out.println(AccelerationUnit.MILE_PER_HOUR_PER_SECOND.getConversionFactorToStandardUnit());
        System.out.println("\n");
        System.out.println(TimeUnit.WEEK.getMultiplicationFactorTo(TimeUnit.DAY));
        System.out.println();
        System.out.println(LengthUnit.KILOMETER.getMultiplicationFactorTo(LengthUnit.METER));
        System.out.println(LengthUnit.FOOT.getMultiplicationFactorTo(LengthUnit.METER));
        System.out.println(LengthUnit.INCH.getMultiplicationFactorTo(LengthUnit.METER));
        System.out.println(LengthUnit.FOOT.getMultiplicationFactorTo(LengthUnit.INCH));
        System.out.println();
        System.out.println(SpeedUnit.KM_PER_HOUR.getMultiplicationFactorTo(SpeedUnit.METER_PER_SECOND));
        System.out.println(SpeedUnit.MILE_PER_HOUR.getMultiplicationFactorTo(SpeedUnit.METER_PER_SECOND));
        System.out.println(SpeedUnit.MILE_PER_HOUR.getMultiplicationFactorTo(SpeedUnit.KM_PER_HOUR));
        System.out.println();
        System.out.println(AreaUnit.HECTARE.getMultiplicationFactorTo(AreaUnit.SQUARE_METER));
        System.out.println(AreaUnit.SQUARE_FOOT.getMultiplicationFactorTo(AreaUnit.SQUARE_INCH));
        System.out.println();
        System.out.println(AccelerationUnit.KM_PER_HOUR_2.getMultiplicationFactorTo(AccelerationUnit.METER_PER_SECOND_2));
        System.out.println(AccelerationUnit.MILE_PER_SECOND_2.getMultiplicationFactorTo(AccelerationUnit.METER_PER_SECOND_2));
        System.out.println(AccelerationUnit.MILE_PER_HOUR_2.getMultiplicationFactorTo(AccelerationUnit.METER_PER_SECOND_2));
        System.out.println(AccelerationUnit.KNOT_PER_SECOND.getMultiplicationFactorTo(AccelerationUnit.METER_PER_SECOND_2));
        System.out.println(AccelerationUnit.MILE_PER_HOUR_PER_SECOND.getMultiplicationFactorTo(AccelerationUnit.METER_PER_SECOND_2));
    }
}
