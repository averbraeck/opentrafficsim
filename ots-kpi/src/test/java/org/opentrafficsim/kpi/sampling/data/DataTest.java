package org.opentrafficsim.kpi.sampling.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Arrays;
import java.util.List;

import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.djunits.value.vdouble.vector.SpeedVector;
import org.djunits.value.vdouble.vector.TimeVector;
import org.djunits.value.vfloat.scalar.FloatDuration;
import org.djunits.value.vfloat.scalar.FloatLength;
import org.djunits.value.vfloat.scalar.FloatSpeed;
import org.djunits.value.vfloat.scalar.FloatTime;
import org.djunits.value.vfloat.vector.FloatDurationVector;
import org.djunits.value.vfloat.vector.FloatLengthVector;
import org.djunits.value.vfloat.vector.FloatSpeedVector;
import org.djunits.value.vfloat.vector.FloatTimeVector;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.Mockito;
import org.opentrafficsim.kpi.interfaces.GtuData;
import org.opentrafficsim.kpi.sampling.impl.TestGtuData;

/**
 * Test extended data.
 * <p>
 * Copyright (c) 2024-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public final class DataTest
{

    /** */
    private DataTest()
    {
        // do not instantiate test class
    }

    /**
     * Test data.
     */
    @Test
    public void testData()
    {
        // ReferenceSpeed
        Speed speed = Speed.ofSI(10.0);
        TestGtuData gtu = new TestGtuData("id", "origin", "destination", "gtuType", "route", speed);
        assertNotNull(ReferenceSpeed.INSTANCE.toString());
        assertEquals(ReferenceSpeed.INSTANCE.getValue(gtu), FloatSpeed.ofSI((float) speed.si));

        float[] data = new float[] {0.0f, 1.0f};

        // ExtendedDataDuration
        ExtendedDataDuration<TestGtuData> durationData = new ExtendedDataDuration<>("id", "description")
        {
            @Override
            public FloatDuration getValue(final TestGtuData gtu)
            {
                return null;
            }
        };
        assertEquals(FloatDuration.ZERO, durationData.convertValue(0.0f));
        assertEquals(new FloatDurationVector(data), durationData.convert(data));
        assertEquals(FloatDuration.ofSI(0.3f), durationData.interpolate(FloatDuration.ZERO, FloatDuration.ONE, 0.3));
        assertEquals(FloatDuration.ONE, durationData.parseValue("1.0"));

        // ExtendedDataLength
        ExtendedDataLength<TestGtuData> lengthData = new ExtendedDataLength<>("id", "description")
        {
            @Override
            public FloatLength getValue(final TestGtuData gtu)
            {
                return null;
            }
        };
        assertEquals(FloatLength.ZERO, lengthData.convertValue(0.0f));
        assertEquals(new FloatLengthVector(data), lengthData.convert(data));
        assertEquals(FloatLength.ofSI(0.3f), lengthData.interpolate(FloatLength.ZERO, FloatLength.ONE, 0.3));
        assertEquals(FloatLength.ONE, lengthData.parseValue("1.0"));

        // ExtendedDataSpeed
        ExtendedDataSpeed<TestGtuData> speedData = new ExtendedDataSpeed<>("id", "description")
        {
            @Override
            public FloatSpeed getValue(final TestGtuData gtu)
            {
                return null;
            }
        };
        assertEquals(FloatSpeed.ZERO, speedData.convertValue(0.0f));
        assertEquals(new FloatSpeedVector(data), speedData.convert(data));
        assertEquals(FloatSpeed.ofSI(0.3f), speedData.interpolate(FloatSpeed.ZERO, FloatSpeed.ONE, 0.3));
        assertEquals(FloatSpeed.ONE, speedData.parseValue("1.0"));

        // ExtendedDataFloat
        assertEquals(FloatSpeed.ZERO, speedData.getOutputValue(new FloatSpeedVector(data), 0));
        assertEquals(FloatSpeed.ONE, speedData.getOutputValue(new FloatSpeedVector(data), 1));
        try
        {
            speedData.getOutputValue(new FloatSpeedVector(data), 2);
            fail("Should have thrown IndexOutOfBoundsException for index 2.");
        }
        catch (IndexOutOfBoundsException ex)
        {
            // expected
        }
        float[] newArray = new float[10];
        newArray[1] = 1.0f;
        newArray[2] = 2.0f;
        assertTrue(Arrays.equals(newArray, speedData.setValue(data, 2, FloatSpeed.ofSI(2.0f))));

        // ExtendedDataNumber
        ExtendedDataNumber<TestGtuData> numberData = new ExtendedDataNumber<>("id", "description")
        {
            @Override
            public Float getValue(final TestGtuData gtu)
            {
                return null;
            }
        };
        float[] storage = numberData.initializeStorage();
        storage = numberData.setValue(storage, 1, 1.0f);
        storage = numberData.setValue(storage, 2, 2.0f);
        assertTrue(Arrays.equals(newArray, storage));
        assertTrue(numberData.setValue(storage, 10, 10.0f).length > 10);
        assertEquals(numberData.getOutputValue(storage, 1), 1.0f, 0.001f);
        assertEquals(numberData.getOutputValue(storage, 2), 2.0f, 0.001f);
        assertEquals(numberData.getStorageValue(storage, 1), 1.0f, 0.001f);
        assertEquals(numberData.getStorageValue(storage, 2), 2.0f, 0.001f);
        assertTrue(Arrays.equals(data, numberData.convert(storage, 2)));
        assertEquals(numberData.parseValue("1.0"), 1.0f);

        // ExtendedDataString = ExtendedDataList
        ExtendedDataString<TestGtuData> stringData = new ExtendedDataString<>("id", "description")
        {
            @Override
            public String getValue(final TestGtuData gtu)
            {
                return null;
            }
        };
        List<String> storageList = stringData.initializeStorage();
        storageList = stringData.setValue(storageList, 0, "0.00");
        storageList = stringData.setValue(storageList, 0, "0.0");
        storageList = stringData.setValue(storageList, 1, "1.0");
        storageList = stringData.setValue(storageList, 2, "2.0");
        assertEquals("1.0", storageList.get(1));
        assertEquals("2.0", storageList.get(2));
        int preSize = storageList.size();
        assertTrue(stringData.setValue(storageList, storageList.size(), "increase").size() > preSize);
        assertEquals(stringData.getOutputValue(storageList, 1), "1.0");
        assertEquals(stringData.getOutputValue(storageList, 2), "2.0");
        assertEquals(stringData.getStorageValue(storageList, 1), "1.0");
        assertEquals(stringData.getStorageValue(storageList, 2), "2.0");
        assertEquals(List.of("0.0", "1.0", "2.0"), stringData.convert(storageList, 3));
        assertEquals(stringData.parseValue("parse"), "parse");
        assertNotNull(stringData.toString());
    }

    /**
     * Test default interpolation of various data types.
     */
    @Test
    public void testInterpolate()
    {
        // DoubleScalarRel
        ExtendedDataType<Speed, SpeedVector, double[], TestGtuData> doubleRel = mock();
        assertEquals(Speed.ofSI(0.3), doubleRel.interpolate(Speed.ZERO, Speed.ONE, 0.3));

        // DoubleScalarAbs
        ExtendedDataType<Time, TimeVector, double[], TestGtuData> doubleAbs = mock();
        assertEquals(Time.ofSI(0.3), doubleAbs.interpolate(Time.ZERO, Time.ofSI(1.0), 0.3));

        // FloatScalarRel
        ExtendedDataType<FloatSpeed, FloatSpeedVector, double[], TestGtuData> floatRel = mock();
        assertEquals(FloatSpeed.ofSI(0.3f), floatRel.interpolate(FloatSpeed.ZERO, FloatSpeed.ONE, 0.3));

        // FloatScalarAbs
        ExtendedDataType<FloatTime, FloatTimeVector, double[], TestGtuData> floatAbs = mock();
        assertEquals(FloatTime.ofSI(0.3f), floatAbs.interpolate(FloatTime.ZERO, FloatTime.ofSI(1.0f), 0.3));

        // Double
        ExtendedDataType<Double, double[], double[], TestGtuData> doubleType = mock();
        assertEquals(0.3, doubleType.interpolate(0.0, 1.0, 0.3));

        // Float
        ExtendedDataType<Float, float[], float[], TestGtuData> floatType = mock();
        assertEquals(0.3f, floatType.interpolate(0.0f, 1.0f, 0.3));

        // String (non numerical)
        ExtendedDataType<String, String[], String[], TestGtuData> stringType = mock();
        assertEquals("down", stringType.interpolate("down", "up", 0.0));
        assertEquals("down", stringType.interpolate("down", "up", 0.49));
        assertEquals("up", stringType.interpolate("down", "up", 0.50));
        assertEquals("up", stringType.interpolate("down", "up", 1.0));
    }

    /**
     * Return mocked instance of abstract class.
     * @param <T> type of value
     * @param <O> output type
     * @param <S> storage type
     * @param <G> GTU data type
     * @return mocked instance of abstract class
     */
    @SuppressWarnings("unchecked")
    private <T, O, S, G extends GtuData> ExtendedDataType<T, O, S, G> mock()
    {
        return Mockito.mock(ExtendedDataType.class, Answers.CALLS_REAL_METHODS);
    }

}
