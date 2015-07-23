package floatvectordemo;

import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.unit.TemperatureUnit;
import org.opentrafficsim.core.value.ValueException;
import org.opentrafficsim.core.value.vfloat.vector.FloatVector;
import org.opentrafficsim.core.value.vfloat.vector.MutableFloatVector;

/**
 * Demonstrate use of FloatVector and MutableFloatVector.
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author: pknoppers
 * $, initial version Sep 2, 2014 <br>
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
     * @throws ValueException should not happen in this demo code
     */
    public static void main(final String[] args) throws ValueException
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
            fadsum = FloatVector.plus(fad, frd);
            System.out.println("fadsum:          " + fadsum.toString());
        }
        catch (ValueException exception)
        {
            exception.printStackTrace();
        }

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
        mfad.multiplyBy(0.5f).multiplyBy(0.2f);
        System.out.println("mfad * 0.5 * 0.2:" + mfad);
        System.out.println("ifad:            " + ifad);
    }
}
