package org.opentrafficsim.core.value.vdouble.scalar;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.opentrafficsim.core.unit.AreaUnit;
import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.unit.SIUnit;
import org.opentrafficsim.core.unit.SpeedUnit;
import org.opentrafficsim.core.unit.TemperatureUnit;
import org.opentrafficsim.core.unit.TimeUnit;
import org.opentrafficsim.core.value.Relative;

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
 * @version Jun 25, 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class DoubleScalarTest
{
    /**
     * Test creator, verify the various fields in the created objects, test conversions to other units
     */
    @SuppressWarnings("static-method")
    @Test
    public void basicsNonMutableAbs()
    {
        TemperatureUnit tempUnit = TemperatureUnit.DEGREE_CELSIUS;
        double value = 38.0f;
        DoubleScalar.Abs<TemperatureUnit> temperatureFS = new DoubleScalar.Abs<TemperatureUnit>(value, tempUnit);
        assertEquals("Unit should be Celsius", tempUnit, temperatureFS.getUnit());
        assertEquals("Value is what we put in", value, temperatureFS.getValueInUnit(), 0.0001);
        assertEquals("Value in SI is equivalent in Kelvin", 311.15f, temperatureFS.getValueSI(), 0.05);
        assertEquals("Value in Fahrenheit", 100.4f, temperatureFS.getValueInUnit(TemperatureUnit.DEGREE_FAHRENHEIT),
                0.1);
        DoubleScalar.Abs<TemperatureUnit> u2 = new DoubleScalar.Abs<TemperatureUnit>(temperatureFS);
        // temperatureFS.setDisplayUnit(TemperatureUnit.DEGREE_FAHRENHEIT);
        // assertEquals("Unit should now be Fahrenheit", TemperatureUnit.DEGREE_FAHRENHEIT, temperatureFS.getUnit());
        // assertEquals("Value in unit is now the equivalent in Fahrenheit", 100.4f, temperatureFS.getValueInUnit(),
        // 0.05);
        assertEquals("Value in SI is equivalent in Kelvin", 311.15f, temperatureFS.getValueSI(), 0.1);
        assertEquals("Value in Fahrenheit", 100.4f, temperatureFS.getValueInUnit(TemperatureUnit.DEGREE_FAHRENHEIT),
                0.1);
        assertTrue("Value is absolute", temperatureFS.isAbsolute());
        assertFalse("Value is absolute", temperatureFS.isRelative());
        assertEquals("Unit of copy made before calling setUnit should be unchanged", tempUnit, u2.getUnit());

        LengthUnit lengthUnit = LengthUnit.INCH;
        value = 12f;
        DoubleScalar.Abs<LengthUnit> lengthFS = new DoubleScalar.Abs<LengthUnit>(value, lengthUnit);
        // System.out.println("lengthFS is " + lengthFS);
        assertEquals("Unit should be Inch", lengthUnit, lengthFS.getUnit());
        assertEquals("Value is what we put in", value, lengthFS.getValueInUnit(), 0.0001);
        assertEquals("Value in SI is equivalent in Meter", 0.3048f, lengthFS.getValueSI(), 0.0005);
        assertEquals("Value in Foot", 1f, lengthFS.getValueInUnit(LengthUnit.FOOT), 0.0001);
        DoubleScalar.Abs<LengthUnit> copy = lengthFS.copy();
        assertEquals("compareTo should return 0", 0, copy.compareTo(lengthFS));
        DoubleScalar.Abs<LengthUnit> bigger = new DoubleScalar.Abs<LengthUnit>(lengthFS.getValueInUnit() + 100, lengthFS.getUnit());
        assertEquals("compareTo should return 1", 1, bigger.compareTo(lengthFS));
        assertEquals("compareTo should return -1", -1, lengthFS.compareTo(bigger));
        assertEquals("Unit of copy should still be in Inch", LengthUnit.INCH, copy.getUnit());
        assertTrue("copy should be equal to original", copy.equals(lengthFS));
        assertEquals("copy should have same hashcode as original", copy.hashCode(), lengthFS.hashCode());
        assertTrue("original should be equal to copy", lengthFS.equals(copy));
        assertTrue("original should be equal to itself", lengthFS.equals(lengthFS));

        DoubleScalar.Abs<LengthUnit> scalarFromScalar = new DoubleScalar.Abs<LengthUnit>(lengthFS);
        assertEquals("Unit should be Inch", lengthUnit, scalarFromScalar.getUnit());
        assertEquals("Value is what we put in", value, scalarFromScalar.getValueInUnit(), 0.0001);
        assertEquals("Value in SI is equivalent in Meter", 0.3048f, scalarFromScalar.getValueSI(), 0.0005);
        assertEquals("Value in Foot", 1f, scalarFromScalar.getValueInUnit(LengthUnit.FOOT), 0.0001);

        MutableDoubleScalar.Abs<LengthUnit> m = lengthFS.mutable();
        assertEquals("Unit should be Inch", lengthUnit, m.getUnit());
        assertEquals("Value is what we put in", value, m.getValueInUnit(), 0.0001);
        assertEquals("Value in SI is equivalent in Meter", 0.3048f, m.getValueSI(), 0.0005);
        assertEquals("Value in Foot", 1f, m.getValueInUnit(LengthUnit.FOOT), 0.0001);
        assertTrue("mutable version should be equal to original", m.equals(lengthFS));
        assertEquals("mutable version should have same hashCode as original", m.hashCode(), lengthFS.hashCode());
        MutableDoubleScalar.Abs<LengthUnit> mm = m.copy();
        DoubleScalar.Abs<LengthUnit> imm = m.immutable();
        assertEquals("compareTo should return 0", 0, m.compareTo(mm));
        assertEquals("compareTo should return 0", 0, mm.compareTo(m));
        // Modify the mutable
        m.setSI(m.getValueSI() + 1);
        assertFalse("modified version should NOT be equal to original", m.equals(lengthFS));
        assertTrue("HashCode of modified version should not be equal to hashCode of original", m.hashCode() != lengthFS.hashCode());
        assertEquals("original should have the original value", value, lengthFS.getValueInUnit(), 0.0001);
        assertEquals("copy of mutable version should have the original value", value, mm.getValueInUnit(), 0.0001);
        assertEquals("immutable variant of mutable version should have the original value", value,
                imm.getValueInUnit(), 0.0001);
        assertEquals("compareTo should return 1", 1, m.compareTo(mm));
        assertEquals("compareTo should return -1", -1, mm.compareTo(m));
        // undo change
        m.setSI(lengthFS.getValueSI());
        assertTrue("restored mutable version should be equal to original", m.equals(lengthFS));
        assertEquals("restored mutable version should have same hashCode as original", m.hashCode(), lengthFS.hashCode());
        DoubleScalar.Abs<LengthUnit> differentUnit =
                new DoubleScalar.Abs<LengthUnit>(lengthFS.getValueSI(), LengthUnit.METER);
        assertTrue("doubleScalar with different unit, but same SI value and also absolute should be equal",
                lengthFS.equals(differentUnit));
        assertEquals("doubleScalar with different unit, but same SI value should have same hashCode", lengthFS.hashCode(), differentUnit.hashCode());
        DoubleScalar.Rel<LengthUnit> differentUnitRel =
                new DoubleScalar.Rel<LengthUnit>(lengthFS.getValueSI(), LengthUnit.METER);
        assertFalse("doubleScalar with different unit, but same SI value but not also absolute should NOT be equal",
                lengthFS.equals(differentUnitRel));
        assertFalse("doubleScalar with different unit, but same SI value but not also absolute should NOT be equal",
                differentUnitRel.equals(lengthFS));

        assertEquals("intValue should return rounded value in SI", Math.round(lengthFS.getValueSI()),
                lengthFS.intValue(), 0.0001);
        assertEquals("longValue should return rounded value in SI", Math.round(lengthFS.getValueSI()),
                lengthFS.longValue(), 0.0001);
        assertEquals("floatValue should return rounded value in SI", lengthFS.getValueSI(), lengthFS.floatValue(),
                0.0001);
        assertEquals("doubleValue should return rounded value in SI", lengthFS.getValueSI(), lengthFS.doubleValue(),
                0.0001);
        assertFalse("equals to null should return false", lengthFS.equals(null));
    }

    /**
     * Test creator, verify the various fields in the created objects, test conversions to other units
     */
    @SuppressWarnings("static-method")
    @Test
    public void basicsNonMutableRel()
    {
        TemperatureUnit tempUnit = TemperatureUnit.DEGREE_CELSIUS;
        double value = 38.0f;
        DoubleScalar.Rel<TemperatureUnit> temperatureFS = new DoubleScalar.Rel<TemperatureUnit>(value, tempUnit);
        assertEquals("Unit should be Celsius", tempUnit, temperatureFS.getUnit());
        assertEquals("Value is what we put in", value, temperatureFS.getValueInUnit(), 0.0001);
        assertEquals("Value in SI is equivalent in Kelvin", 311.15f, temperatureFS.getValueSI(), 0.05);
        assertEquals("Value in Fahrenheit", 100.4f, temperatureFS.getValueInUnit(TemperatureUnit.DEGREE_FAHRENHEIT),
                0.1);
        DoubleScalar.Rel<TemperatureUnit> u2 = new DoubleScalar.Rel<TemperatureUnit>(temperatureFS);
        // temperatureFS.setDisplayUnit(TemperatureUnit.DEGREE_FAHRENHEIT);
        // assertEquals("Unit should now be Fahrenheit", TemperatureUnit.DEGREE_FAHRENHEIT, temperatureFS.getUnit());
        // assertEquals("Value in unit is now the equivalent in Fahrenheit", 100.4f, temperatureFS.getValueInUnit(),
        // 0.05);
        assertEquals("Value in SI is equivalent in Kelvin", 311.15f, temperatureFS.getValueSI(), 0.1);
        assertEquals("Value in Fahrenheit", 100.4f, temperatureFS.getValueInUnit(TemperatureUnit.DEGREE_FAHRENHEIT),
                0.1);
        assertFalse("Value is relative", temperatureFS.isAbsolute());
        assertTrue("Value is absolute", temperatureFS.isRelative());
        assertEquals("Unit of copy made before calling setUnit should be unchanged", tempUnit, u2.getUnit());

        LengthUnit lengthUnit = LengthUnit.INCH;
        value = 12f;
        DoubleScalar.Rel<LengthUnit> lengthFS = new DoubleScalar.Rel<LengthUnit>(value, lengthUnit);
        // System.out.println("lengthFS is " + lengthFS);
        assertEquals("Unit should be Inch", lengthUnit, lengthFS.getUnit());
        assertEquals("Value is what we put in", value, lengthFS.getValueInUnit(), 0.0001);
        assertEquals("Value in SI is equivalent in Meter", 0.3048f, lengthFS.getValueSI(), 0.0005);
        assertEquals("Value in Foot", 1f, lengthFS.getValueInUnit(LengthUnit.FOOT), 0.0001);
        DoubleScalar.Rel<LengthUnit> copy = lengthFS.copy();
        assertEquals("compareTo should return 0", 0, copy.compareTo(lengthFS));
        DoubleScalar.Rel<LengthUnit> bigger = new DoubleScalar.Rel<LengthUnit>(lengthFS.getValueInUnit() + 100, lengthFS.getUnit());
        assertEquals("compareTo should return 1", 1, bigger.compareTo(lengthFS));
        assertEquals("compareTo should return -1", -1, lengthFS.compareTo(bigger));
        assertEquals("Unit of copy should still be in Inch", LengthUnit.INCH, copy.getUnit());
        assertTrue("copy should be equal to original", copy.equals(lengthFS));
        assertEquals("HashCode of copy should be equals to hashCode of original", copy.hashCode(), lengthFS.hashCode());
        assertTrue("original should be equal to copy", lengthFS.equals(copy));
        assertTrue("original should be equal to itself", lengthFS.equals(lengthFS));

        DoubleScalar.Rel<LengthUnit> scalarFromScalar = new DoubleScalar.Rel<LengthUnit>(lengthFS);
        assertEquals("Unit should be Inch", lengthUnit, scalarFromScalar.getUnit());
        assertEquals("Value is what we put in", value, scalarFromScalar.getValueInUnit(), 0.0001);
        assertEquals("Value in SI is equivalent in Meter", 0.3048f, scalarFromScalar.getValueSI(), 0.0005);
        assertEquals("Value in Foot", 1f, scalarFromScalar.getValueInUnit(LengthUnit.FOOT), 0.0001);

        MutableDoubleScalar.Rel<LengthUnit> m = lengthFS.mutable();
        assertEquals("Unit should be Inch", lengthUnit, m.getUnit());
        assertEquals("Value is what we put in", value, m.getValueInUnit(), 0.0001);
        assertEquals("Value in SI is equivalent in Meter", 0.3048f, m.getValueSI(), 0.0005);
        assertEquals("Value in Foot", 1f, m.getValueInUnit(LengthUnit.FOOT), 0.0001);
        assertTrue("mutable version should be equal to original", m.equals(lengthFS));
        assertEquals("HashCode of mutable version should be equal to hash code of original", m.hashCode(), lengthFS.hashCode());
        MutableDoubleScalar.Rel<LengthUnit> mm = m.copy();
        DoubleScalar.Rel<LengthUnit> imm = m.immutable();
        assertEquals("compareTo should return 0", 0, m.compareTo(mm));
        assertEquals("compareTo should return 0", 0, mm.compareTo(m));
        // Modify the mutable
        m.setSI(m.getValueSI() + 1);
        assertFalse("modified version should NOT be equal to original", m.equals(lengthFS));
        assertEquals("original should have the original value", value, lengthFS.getValueInUnit(), 0.0001);
        assertEquals("copy of mutable version should have the original value", value, mm.getValueInUnit(), 0.0001);
        assertEquals("immutable variant of mutable version should have the original value", value,
                imm.getValueInUnit(), 0.0001);
        assertEquals("compareTo should return 1", 1, m.compareTo(mm));
        assertEquals("compareTo should return -1", -1, mm.compareTo(m));
        // undo change
        m.setSI(lengthFS.getValueSI());
        assertTrue("restored mutable version should be equal to original", m.equals(lengthFS));
        assertEquals("HashCode of restored mutable version should be equal to hash code of original", m.hashCode(), lengthFS.hashCode());
        DoubleScalar.Rel<LengthUnit> differentUnit =
                new DoubleScalar.Rel<LengthUnit>(lengthFS.getValueSI(), LengthUnit.METER);
        assertTrue("doubleScalar with different unit, but same SI value and also absolute should be equal",
                lengthFS.equals(differentUnit));
        assertEquals("hashCode of doubleScalar with different unit, but same SI value should be same", lengthFS.hashCode(), differentUnit.hashCode());
        DoubleScalar.Abs<LengthUnit> differentUnitAbs =
                new DoubleScalar.Abs<LengthUnit>(lengthFS.getValueSI(), LengthUnit.METER);
        assertFalse("doubleScalar with different unit, but same SI value but not also absolute should NOT be equal",
                lengthFS.equals(differentUnitAbs));
        assertFalse("doubleScalar with different unit, but same SI value but not also absolute should NOT be equal",
                differentUnitAbs.equals(lengthFS));

        assertEquals("intValue should return rounded value in SI", Math.round(lengthFS.getValueSI()),
                lengthFS.intValue(), 0.0001);
        assertEquals("longValue should return rounded value in SI", Math.round(lengthFS.getValueSI()),
                lengthFS.longValue(), 0.0001);
        assertEquals("floatValue should return rounded value in SI", lengthFS.getValueSI(), lengthFS.floatValue(),
                0.0001);
        assertEquals("doubleValue should return rounded value in SI", lengthFS.getValueSI(), lengthFS.doubleValue(),
                0.0001);
        assertFalse("equals to null should return false", lengthFS.equals(null));
    }

    /**
     * Test creator, verify the various fields in the created objects, test conversions to other units
     */
    @SuppressWarnings("static-method")
    @Test
    public void basicsMutable()
    {
        TemperatureUnit tempUnit = TemperatureUnit.DEGREE_CELSIUS;
        double value = 38.0f;
        MutableDoubleScalar.Abs<TemperatureUnit> temperatureFS =
                new MutableDoubleScalar.Abs<TemperatureUnit>(value, tempUnit);
        assertEquals("Unit should be Celsius", tempUnit, temperatureFS.getUnit());
        assertEquals("Value is what we put in", value, temperatureFS.getValueInUnit(), 0.0001);
        assertEquals("Value in SI is equivalent in Kelvin", 311.15f, temperatureFS.getValueSI(), 0.05);
        assertEquals("Value in Fahrenheit", 100.4f, temperatureFS.getValueInUnit(TemperatureUnit.DEGREE_FAHRENHEIT),
                0.1);
        MutableDoubleScalar.Abs<TemperatureUnit> u2 = new MutableDoubleScalar.Abs<TemperatureUnit>(temperatureFS);
        // temperatureFS.setDisplayUnit(TemperatureUnit.DEGREE_FAHRENHEIT);
        // assertEquals("Unit should now be Fahrenheit", TemperatureUnit.DEGREE_FAHRENHEIT, temperatureFS.getUnit());
        // assertEquals("Value in unit is now the equivalent in Fahrenheit", 100.4f, temperatureFS.getValueInUnit(),
        // 0.05);
        assertEquals("Value in SI is equivalent in Kelvin", 311.15f, temperatureFS.getValueSI(), 0.1);
        assertEquals("Value in Fahrenheit", 100.4f, temperatureFS.getValueInUnit(TemperatureUnit.DEGREE_FAHRENHEIT),
                0.1);
        assertTrue("Value is absolute", temperatureFS.isAbsolute());
        assertFalse("Value is absolute", temperatureFS.isRelative());
        assertEquals("Unit of copy made before calling setUnit should be unchanged", tempUnit, u2.getUnit());

        LengthUnit lengthUnit = LengthUnit.INCH;
        value = 12f;
        MutableDoubleScalar.Rel<LengthUnit> lengthFS = new MutableDoubleScalar.Rel<LengthUnit>(value, lengthUnit);
        // System.out.println("lengthFS is " + lengthFS);
        assertEquals("Unit should be Inch", lengthUnit, lengthFS.getUnit());
        assertEquals("Value is what we put in", value, lengthFS.getValueInUnit(), 0.0001);
        assertEquals("Value in SI is equivalent in Meter", 0.3048f, lengthFS.getValueSI(), 0.0005);
        assertEquals("Value in Foot", 1f, lengthFS.getValueInUnit(LengthUnit.FOOT), 0.0001);
        MutableDoubleScalar.Rel<LengthUnit> copy = lengthFS.copy();
        assertEquals("Unit of copy should still be in Inch", LengthUnit.INCH, copy.getUnit());
        assertEquals("value of copy should match original", lengthFS.getValueSI(), copy.getValueSI(), 0.0001);
        copy = lengthFS.mutable();
        assertEquals("Unit of mutable should still be in Inch", LengthUnit.INCH, copy.getUnit());
        assertEquals("value of new mutable should match original", lengthFS.getValueSI(), copy.getValueSI(), 0.0001);
        MutableDoubleScalar.Abs<TemperatureUnit> mabs = new MutableDoubleScalar.Abs<TemperatureUnit>(value, tempUnit);
        MutableDoubleScalar.Abs<TemperatureUnit> mmabs = mabs.mutable();
        assertEquals("duplicate has same value", mabs.getValueSI(), mmabs.getValueSI(), 0.0001);
        assertTrue("duplicate is equal to original", mmabs.equals(mabs));
        assertEquals("hashCode of duplicate should be equal to hashcode of original", mmabs.hashCode(), mabs.hashCode());

        // The code below disclosed a bug in ValueUtil.expressAsSIUnit
        mmabs.setInUnit(123, TemperatureUnit.DEGREE_FAHRENHEIT);
        // System.out.println("mmabs: " + mmabs);
        // System.out.println("in Fahrenheit: " + mmabs.getValueInUnit(TemperatureUnit.DEGREE_FAHRENHEIT));
        assertEquals("modified temperature should be equal to 323.706K", 323.706, mmabs.getValueSI(), 0.001);
    }

    /**
     * Check that copy really performs a deep copy.
     */
    @SuppressWarnings("static-method")
    @Test
    public void testCopyAbs()
    {
        MutableDoubleScalar.Abs<TemperatureUnit> value =
                new MutableDoubleScalar.Abs<TemperatureUnit>(10, TemperatureUnit.DEGREE_CELSIUS);
        MutableDoubleScalar.Abs<TemperatureUnit> copy = value.copy();
        assertEquals("Copy should have same value", value.getValueSI(), copy.getValueSI(), 0.0001);
        assertTrue("Copy should be equal to value", value.equals(copy));
        assertTrue("Value should be equal to copy", copy.equals(value));
        value.set(new DoubleScalar.Abs<TemperatureUnit>(20, TemperatureUnit.DEGREE_CELSIUS));
        assertFalse("Copy should have same value", value.getValueSI() == copy.getValueSI());
        assertFalse("Copy should be equal to value", value.equals(copy));
        assertFalse("Value should be equal to copy", copy.equals(value));
    }

    /**
     * Check that copy really performs a deep copy.
     */
    @SuppressWarnings("static-method")
    @Test
    public void testCopyRel()
    {
        MutableDoubleScalar.Rel<TemperatureUnit> value =
                new MutableDoubleScalar.Rel<TemperatureUnit>(10, TemperatureUnit.DEGREE_CELSIUS);
        MutableDoubleScalar.Rel<TemperatureUnit> copy = value.copy();
        assertEquals("Copy should have same value", value.getValueSI(), copy.getValueSI(), 0.0001);
        assertTrue("Copy should be equal to value", value.equals(copy));
        assertTrue("Value should be equal to copy", copy.equals(value));
        value.set(new MutableDoubleScalar.Rel<TemperatureUnit>(20, TemperatureUnit.DEGREE_CELSIUS));
        assertFalse("Copy should have same value", value.getValueSI() == copy.getValueSI());
        assertFalse("Copy should be equal to value", value.equals(copy));
        assertFalse("Value should be equal to copy", copy.equals(value));
    }

    /**
     * Test plus, minus, times.
     */
    @SuppressWarnings("static-method")
    @Test
    public void simpleArithmetic()
    {
        double leftValue = 123.456f;
        double rightValue = 21.098f;
        double rightValue2 = 1.0987f;
        DoubleScalar.Abs<LengthUnit> leftAbs = new DoubleScalar.Abs<LengthUnit>(leftValue, LengthUnit.METER);
        DoubleScalar.Rel<LengthUnit> right = new DoubleScalar.Rel<LengthUnit>(rightValue, LengthUnit.INCH);
        DoubleScalar.Rel<LengthUnit> right2 = new DoubleScalar.Rel<LengthUnit>(rightValue2, LengthUnit.MILLIMETER);
        MutableDoubleScalar.Abs<LengthUnit> sum = MutableDoubleScalar.plus(leftAbs, right);
        assertEquals("result should be in METER", LengthUnit.METER, sum.getUnit());
        assertEquals("value of result should be sum of meter equivalent of values", leftValue + rightValue * 0.0254,
                sum.getValueSI(), 0.0001);
        DoubleScalar.Rel<LengthUnit> leftRel = new DoubleScalar.Rel<LengthUnit>(leftValue, LengthUnit.METER);
        MutableDoubleScalar.Rel<LengthUnit> sum2 =
                MutableDoubleScalar.plus(LengthUnit.MILLIMETER, leftRel, right, right2);
        assertEquals("result should be in MILLIMETER", LengthUnit.MILLIMETER, sum2.getUnit());
        assertEquals("value in SI should be sum of meter equivalent of values", leftValue + rightValue * 0.0254
                + rightValue2 / 1000, sum2.getValueSI(), 0.0001);
        assertEquals("value in \"own\" unit should be equivalent in MILLIMETER", 1000
                * (leftValue + rightValue * 0.0254) + rightValue2, sum2.getValueInUnit(), 0.1);
        MutableDoubleScalar.Abs<LengthUnit> difference = MutableDoubleScalar.minus(leftAbs, right, right2);
        assertEquals("result should be in METER", LengthUnit.METER, difference.getUnit());
        assertEquals("value in SI should be sum of meter equivalent of values", leftValue - rightValue * 0.0254
                - rightValue2 / 1000, difference.getValueSI(), 0.0001);
        assertEquals("value in \"own\" unit should be equivalent in METER", leftValue - rightValue * 0.0254
                - rightValue2 / 1000, difference.getValueInUnit(), 0.1);
        MutableDoubleScalar.Rel<LengthUnit> differenceRel = MutableDoubleScalar.minus(leftRel, right, right2);
        assertEquals("result should be in METER", LengthUnit.METER, differenceRel.getUnit());
        assertEquals("value in SI should be sum of meter equivalent of values", leftValue - rightValue * 0.0254
                - rightValue2 / 1000, differenceRel.getValueSI(), 0.0001);
        assertEquals("value in \"own\" unit should be equivalent in METER", leftValue - rightValue * 0.0254
                - rightValue2 / 1000, differenceRel.getValueInUnit(), 0.001);
        differenceRel = MutableDoubleScalar.minus(sum.immutable(), leftAbs);
        assertEquals("result should be in METER", LengthUnit.METER, difference.getUnit());
        assertEquals("value of result should be minus leftValue", rightValue * 0.0254, differenceRel.getValueSI(),
                0.0001);
        MutableDoubleScalar.Abs<?> surface = MutableDoubleScalar.multiply(leftAbs, difference.immutable());
        // System.out.println("surface is " + surface);
        assertEquals("Surface should be in square meter", AreaUnit.SQUARE_METER.getSICoefficientsString(), surface
                .getUnit().getSICoefficientsString());
        assertEquals("Surface should be equal to the product of contributing values",
                leftAbs.getValueSI() * difference.getValueSI(), surface.getValueSI(), 0.05);
        MutableDoubleScalar.Rel<?> relSurface = MutableDoubleScalar.multiply(right, right2);
        assertEquals("Surface should be in square meter", AreaUnit.SQUARE_METER.getSICoefficientsString(), relSurface
                .getUnit().getSICoefficientsString());
        assertEquals("Surface should be equal to the product of contributing values",
                right.getValueSI() * right2.getValueSI(), relSurface.getValueSI(), 0.00000005);
        assertTrue("DoubleScalar should be equal to itself", leftAbs.equals(leftAbs));
        DoubleScalar<?> copy = new DoubleScalar.Abs<LengthUnit>(leftAbs);
        assertTrue("Copy of DoubleScalar should be equal to itself", leftAbs.equals(copy));
        copy = new DoubleScalar.Rel<LengthUnit>(leftAbs.getValueInUnit(), leftAbs.getUnit());
        assertTrue("this copy should be relative", copy instanceof Relative);
        assertFalse("Relative can not be equal to (otherwise equal) absolute", leftAbs.equals(copy));
        assertFalse("DoubleScalar is not a String", copy.equals("String"));
        DoubleScalar.Abs<TimeUnit> timeScalar = new DoubleScalar.Abs<TimeUnit>(leftValue, TimeUnit.SECOND);
        assertEquals("Value should match", leftAbs.getValueSI(), timeScalar.getValueSI(), 0.0001);
        assertTrue("Both are absolute", leftAbs.isAbsolute() && timeScalar.isAbsolute());
        assertFalse("Absolute length is not equal to absolute time with same value", leftAbs.equals(timeScalar));
        MutableDoubleScalar<LengthUnit> left = new MutableDoubleScalar.Abs<LengthUnit>(leftValue, LengthUnit.METER);
        left.add(right);
        assertEquals("after add-and-becomes the type should not be changed", LengthUnit.METER, left.getUnit());
        assertEquals("after add-and-becomes the value should be changed", leftValue + rightValue * 0.0254,
                left.getValueSI(), 0.0001);
        left = new MutableDoubleScalar.Abs<LengthUnit>(leftValue, LengthUnit.METER);
        left.subtract(right);
        assertEquals("after subtract-and-becomes the type should not be changed", LengthUnit.METER, left.getUnit());
        assertEquals("after subtract-and-becomes the value should be changed", leftValue - rightValue * 0.0254,
                left.getValueSI(), 0.0001);
    }

    /**
     * Test divide.
     */
    @SuppressWarnings("static-method")
    @Test
    public void divide()
    {
        double leftValue = 123.456f;
        double rightValue = 21.098f;
        DoubleScalar.Abs<LengthUnit> leftAbs = new DoubleScalar.Abs<LengthUnit>(leftValue, LengthUnit.METER);
        DoubleScalar.Abs<SpeedUnit> rightAbs = new DoubleScalar.Abs<SpeedUnit>(rightValue, SpeedUnit.KM_PER_HOUR);
        DoubleScalar.Abs<SIUnit> ratio = MutableDoubleScalar.divide(leftAbs, rightAbs);
        String unitString = ratio.getUnit().getAbbreviation();
        if (unitString.endsWith("!"))
            unitString = unitString.substring(0, unitString.length() - 1);
        if (unitString.startsWith("!"))
            unitString = unitString.substring(1);
        assertEquals("result should be in SECOND", TimeUnit.SECOND.toString(), unitString);
        assertEquals("value in SI should be ratio of SI equivalent of values", leftValue / rightValue * 3.600,
                ratio.getValueSI(), 0.0001);
        DoubleScalar.Rel<LengthUnit> leftRel = new DoubleScalar.Rel<LengthUnit>(leftValue, LengthUnit.METER);
        DoubleScalar.Rel<SpeedUnit> rightRel = new DoubleScalar.Rel<SpeedUnit>(rightValue, SpeedUnit.KM_PER_HOUR);
        DoubleScalar.Rel<SIUnit> ratioRel = MutableDoubleScalar.divide(leftRel, rightRel);
        unitString = ratioRel.getUnit().getAbbreviation();
        if (unitString.endsWith("!"))
            unitString = unitString.substring(0, unitString.length() - 1);
        if (unitString.startsWith("!"))
            unitString = unitString.substring(1);
        assertEquals("result should be in SECOND", TimeUnit.SECOND.toString(), unitString);
        assertEquals("value in SI should be ratio of SI equivalent of values", leftValue / rightValue * 3.600,
                ratioRel.getValueSI(), 0.0001);
    }

    /**
     * Test the math operations
     */
    @SuppressWarnings("static-method")
    @Test
    public void mathMethods()
    {
        double[] inputValues = {-10f, -2f, -1f, -0.5f, -0.1f, 0f, 0.1f, 0.5f, 1f, 2f, 10f};
        for (double inputValue : inputValues)
        {
            MutableDoubleScalar.Rel<LengthUnit> fs;
            fs = new MutableDoubleScalar.Rel<LengthUnit>(inputValue, LengthUnit.METER);
            fs.abs();
            MathTester.tester(inputValue, "abs", fs, 0.001, new DoubleToDouble()
            {
                @Override
                public double function(double f)
                {
                    return Math.abs(f);
                }
            });
            fs = new MutableDoubleScalar.Rel<LengthUnit>(inputValue, LengthUnit.METER);
            fs.acos();
            MathTester.tester(inputValue, "acos", fs, 0.001, new DoubleToDouble()
            {
                @Override
                public double function(double f)
                {
                    return Math.acos(f);
                }
            });
            fs = new MutableDoubleScalar.Rel<LengthUnit>(inputValue, LengthUnit.METER);
            fs.asin();
            MathTester.tester(inputValue, "asin", fs, 0.001, new DoubleToDouble()
            {
                @Override
                public double function(double f)
                {
                    return Math.asin(f);
                }
            });
            fs = new MutableDoubleScalar.Rel<LengthUnit>(inputValue, LengthUnit.METER);
            fs.atan();
            MathTester.tester(inputValue, "atan", fs, 0.001, new DoubleToDouble()
            {
                @Override
                public double function(double f)
                {
                    return Math.atan(f);
                }
            });
            fs = new MutableDoubleScalar.Rel<LengthUnit>(inputValue, LengthUnit.METER);
            fs.cbrt();
            MathTester.tester(inputValue, "cbrt", fs, 0.001, new DoubleToDouble()
            {
                @Override
                public double function(double f)
                {
                    return Math.cbrt(f);
                }
            });
            fs = new MutableDoubleScalar.Rel<LengthUnit>(inputValue, LengthUnit.METER);
            fs.ceil();
            MathTester.tester(inputValue, "ceil", fs, 0.001, new DoubleToDouble()
            {
                @Override
                public double function(double f)
                {
                    return Math.ceil(f);
                }
            });
            fs = new MutableDoubleScalar.Rel<LengthUnit>(inputValue, LengthUnit.METER);
            fs.cos();
            MathTester.tester(inputValue, "cos", fs, 0.001, new DoubleToDouble()
            {
                @Override
                public double function(double f)
                {
                    return Math.cos(f);
                }
            });
            fs = new MutableDoubleScalar.Rel<LengthUnit>(inputValue, LengthUnit.METER);
            fs.cosh();
            MathTester.tester(inputValue, "cosh", fs, 0.001, new DoubleToDouble()
            {
                @Override
                public double function(double f)
                {
                    return Math.cosh(f);
                }
            });
            fs = new MutableDoubleScalar.Rel<LengthUnit>(inputValue, LengthUnit.METER);
            fs.exp();
            MathTester.tester(inputValue, "exp", fs, 0.001, new DoubleToDouble()
            {
                @Override
                public double function(double f)
                {
                    return Math.exp(f);
                }
            });
            fs = new MutableDoubleScalar.Rel<LengthUnit>(inputValue, LengthUnit.METER);
            fs.expm1();
            MathTester.tester(inputValue, "expm1", fs, 0.001, new DoubleToDouble()
            {
                @Override
                public double function(double f)
                {
                    return Math.expm1(f);
                }
            });
            fs = new MutableDoubleScalar.Rel<LengthUnit>(inputValue, LengthUnit.METER);
            fs.floor();
            MathTester.tester(inputValue, "floor", fs, 0.001, new DoubleToDouble()
            {
                @Override
                public double function(double f)
                {
                    return Math.floor(f);
                }
            });
            fs = new MutableDoubleScalar.Rel<LengthUnit>(inputValue, LengthUnit.METER);
            fs.log();
            MathTester.tester(inputValue, "log", fs, 0.001, new DoubleToDouble()
            {
                @Override
                public double function(double f)
                {
                    return Math.log(f);
                }
            });
            fs = new MutableDoubleScalar.Rel<LengthUnit>(inputValue, LengthUnit.METER);
            fs.log10();
            MathTester.tester(inputValue, "log10", fs, 0.001, new DoubleToDouble()
            {
                @Override
                public double function(double f)
                {
                    return Math.log10(f);
                }
            });
            fs = new MutableDoubleScalar.Rel<LengthUnit>(inputValue, LengthUnit.METER);
            fs.log1p();
            MathTester.tester(inputValue, "log10", fs, 0.001, new DoubleToDouble()
            {
                @Override
                public double function(double f)
                {
                    return Math.log1p(f);
                }
            });
            fs = new MutableDoubleScalar.Rel<LengthUnit>(inputValue, LengthUnit.METER);
            fs.rint();
            MathTester.tester(inputValue, "rint", fs, 0.001, new DoubleToDouble()
            {
                @Override
                public double function(double f)
                {
                    return Math.rint(f);
                }
            });
            fs = new MutableDoubleScalar.Rel<LengthUnit>(inputValue, LengthUnit.METER);
            fs.round();
            MathTester.tester(inputValue, "round", fs, 0.001, new DoubleToDouble()
            {
                @Override
                public double function(double f)
                {
                    return Math.round(f);
                }
            });
            fs = new MutableDoubleScalar.Rel<LengthUnit>(inputValue, LengthUnit.METER);
            fs.signum();
            MathTester.tester(inputValue, "signum", fs, 0.001, new DoubleToDouble()
            {
                @Override
                public double function(double f)
                {
                    return Math.signum(f);
                }
            });
            fs = new MutableDoubleScalar.Rel<LengthUnit>(inputValue, LengthUnit.METER);
            fs.sin();
            MathTester.tester(inputValue, "sin", fs, 0.001, new DoubleToDouble()
            {
                @Override
                public double function(double f)
                {
                    return Math.sin(f);
                }
            });
            fs = new MutableDoubleScalar.Rel<LengthUnit>(inputValue, LengthUnit.METER);
            fs.sinh();
            MathTester.tester(inputValue, "sinh", fs, 0.001, new DoubleToDouble()
            {
                @Override
                public double function(double f)
                {
                    return Math.sinh(f);
                }
            });
            fs = new MutableDoubleScalar.Rel<LengthUnit>(inputValue, LengthUnit.METER);
            fs.sqrt();
            MathTester.tester(inputValue, "sqrt", fs, 0.001, new DoubleToDouble()
            {
                @Override
                public double function(double f)
                {
                    return Math.sqrt(f);
                }
            });
            fs = new MutableDoubleScalar.Rel<LengthUnit>(inputValue, LengthUnit.METER);
            fs.tan();
            MathTester.tester(inputValue, "tan", fs, 0.001, new DoubleToDouble()
            {
                @Override
                public double function(double f)
                {
                    return Math.tan(f);
                }
            });
            fs = new MutableDoubleScalar.Rel<LengthUnit>(inputValue, LengthUnit.METER);
            fs.tanh();
            MathTester.tester(inputValue, "tanh", fs, 0.001, new DoubleToDouble()
            {
                @Override
                public double function(double f)
                {
                    return Math.tanh(f);
                }
            });
            fs = new MutableDoubleScalar.Rel<LengthUnit>(inputValue, LengthUnit.METER);
            fs.toDegrees();
            MathTester.tester(inputValue, "toDegrees", fs, 0.001, new DoubleToDouble()
            {
                @Override
                public double function(double f)
                {
                    return Math.toDegrees(f);
                }
            });
            fs = new MutableDoubleScalar.Rel<LengthUnit>(inputValue, LengthUnit.METER);
            fs.toRadians();
            MathTester.tester(inputValue, "toRadians", fs, 0.001, new DoubleToDouble()
            {
                @Override
                public double function(double f)
                {
                    return Math.toRadians(f);
                }
            });
            fs = new MutableDoubleScalar.Rel<LengthUnit>(inputValue, LengthUnit.METER);
            fs.inv();
            MathTester.tester(inputValue, "inv", fs, 0.001, new DoubleToDouble()
            {
                @Override
                public double function(double f)
                {
                    return 1 / f;
                }
            });
            for (int i = -10; i <= 10; i++)
            {
                final double exponent = i * 0.5f;
                fs = new MutableDoubleScalar.Rel<LengthUnit>(inputValue, LengthUnit.METER);
                fs.pow(exponent);
                MathTester.tester(inputValue, "pow(" + exponent + ")", fs, 0.001, new DoubleToDouble()
                {
                    @Override
                    public double function(double f)
                    {
                        return Math.pow(f, exponent);
                    }
                });
            }
            double[] constants = {-1000, -100, -10, 0, 10, 100, 1000};
            for (final double constant : constants)
            {
                fs = new MutableDoubleScalar.Rel<LengthUnit>(inputValue, LengthUnit.METER);
                fs.multiply(constant);
                MathTester.tester(inputValue, "multiply by " + constant, fs, 0.001, new DoubleToDouble()
                {
                    @Override
                    public double function(double f)
                    {
                        return f * constant;
                    }
                });
                fs = new MutableDoubleScalar.Rel<LengthUnit>(inputValue, LengthUnit.METER);
                fs.divide(constant);
                MathTester.tester(inputValue, "divide by " + constant, fs, 0.001, new DoubleToDouble()
                {
                    @Override
                    public double function(double f)
                    {
                        return f / constant;
                    }
                });
            }
        }
    }

    /**
     * Interface encapsulating a function that takes a double and returns a double.
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
     * <li>Redistributions of source code must retain the above copyright notice, this list of conditions and the
     * following disclaimer.</li>
     * <li>Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
     * following disclaimer in the documentation and/or other materials provided with the distribution.</li>
     * <li>Neither the name of Delft University of Technology, nor the names of its contributors may be used to endorse
     * or promote products derived from this software without specific prior written permission.</li>
     * </ul>
     * This software is provided by the copyright holders and contributors "as is" and any express or implied
     * warranties, including, but not limited to, the implied warranties of merchantability and fitness for a particular
     * purpose are disclaimed. In no event shall the copyright holder or contributors be liable for any direct,
     * indirect, incidental, special, exemplary, or consequential damages (including, but not limited to, procurement of
     * substitute goods or services; loss of use, data, or profits; or business interruption) however caused and on any
     * theory of liability, whether in contract, strict liability, or tort (including negligence or otherwise) arising
     * in any way out of the use of this software, even if advised of the possibility of such damage.
     * @version Jun 23, 2014 <br>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     */
    interface DoubleToDouble
    {
        /**
         * @param f double value
         * @return double value
         */
        double function(double f);
    }

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
     * <li>Redistributions of source code must retain the above copyright notice, this list of conditions and the
     * following disclaimer.</li>
     * <li>Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
     * following disclaimer in the documentation and/or other materials provided with the distribution.</li>
     * <li>Neither the name of Delft University of Technology, nor the names of its contributors may be used to endorse
     * or promote products derived from this software without specific prior written permission.</li>
     * </ul>
     * This software is provided by the copyright holders and contributors "as is" and any express or implied
     * warranties, including, but not limited to, the implied warranties of merchantability and fitness for a particular
     * purpose are disclaimed. In no event shall the copyright holder or contributors be liable for any direct,
     * indirect, incidental, special, exemplary, or consequential damages (including, but not limited to, procurement of
     * substitute goods or services; loss of use, data, or profits; or business interruption) however caused and on any
     * theory of liability, whether in contract, strict liability, or tort (including negligence or otherwise) arising
     * in any way out of the use of this software, even if advised of the possibility of such damage.
     * @version Jun 23, 2014 <br>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     */
    abstract static class MathTester
    {

        /**
         * @param inputValue Double; unprocessed value
         * @param operation String; description of method that is being tested
         * @param actualResult DoubleScalar; the actual result of the operation
         * @param precision double expected accuracy
         * @param function DoubleToDouble encapsulating function that converts one value in inputValues to the
         *            corresponding value in resultValues
         */
        public static void tester(final double inputValue, String operation, final MutableDoubleScalar<?> actualResult,
                final double precision, final DoubleToDouble function)
        {
            double expectedResult = function.function(inputValue);
            String description =
                    String.format("%s(%f)->%f should be equal to %f with precision %f", operation, inputValue,
                            expectedResult, actualResult.getValueSI(), precision);
            // System.out.println(description);
            assertEquals(description, expectedResult, actualResult.getValueSI(), precision);

        }

        /**
         * Function that takes a double value and returns a double value
         * @param in double value
         * @return double value
         */
        public abstract double function(double in);
    }

}
