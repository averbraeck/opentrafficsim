package org.opentrafficsim.core.value.vfloat.vector;

import java.io.Serializable;

import org.opentrafficsim.core.unit.Unit;
import org.opentrafficsim.core.value.AbstractValue;
import org.opentrafficsim.core.value.Format;
import org.opentrafficsim.core.value.ValueException;
import org.opentrafficsim.core.value.ValueUtil;
import org.opentrafficsim.core.value.vfloat.scalar.FloatScalar;

import cern.colt.matrix.tfloat.FloatMatrix1D;

/**
 * Methods, fields and code shared by FloatVector and mutableFloatVector.
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
 * @version Aug 28, 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @param <U> Unit of this AbstractFloatVector
 */
public abstract class AbstractFloatVector<U extends Unit<U>> extends AbstractValue<U> implements Serializable,
        ReadOnlyFloatVectorFunctions<U>

{
    /** */
    private static final long serialVersionUID = 20140828L;

    /** the internal storage for the vector; internally they are stored in SI units; can be dense or sparse. */
    protected FloatMatrix1D vectorSI;

    /**
     * @param unit
     */
    protected AbstractFloatVector(U unit)
    {
        super(unit);
        // System.out.println("Created AbstractFloatVector");
    }

    /**
     * Import the values and convert them into SI units.
     * @param values an array of values
     */
    protected void initialize(final float[] values)
    {
        this.vectorSI = createMatrix1D(values.length);
        if (this.unit.equals(this.unit.getStandardUnit()))
        {
            this.vectorSI.assign(values);
        }
        else
        {
            for (int index = 0; index < values.length; index++)
            {
                safeSet(index, (float) expressAsSIUnit(values[index]));
            }
        }
    }

    /**
     * @param values
     */
    protected void initialize(final FloatMatrix1D values)
    {
        this.vectorSI = values;
    }

    /**
     * Construct the vector and store the values in SI units.
     * @param values an array of values for the constructor
     * @throws ValueException exception thrown when array with zero elements is offered
     */
    protected void initialize(final FloatScalar<U>[] values) throws ValueException
    {
        this.vectorSI = createMatrix1D(values.length);
        for (int index = 0; index < values.length; index++)
        {
            safeSet(index, values[index].getValueSI());
        }
    }

    /**
     * This method has to be implemented by each leaf class.
     * @param size the number of cells in the vector
     * @return an instance of the right type of matrix (absolute / relative, dense / sparse, etc.).
     */
    protected abstract FloatMatrix1D createMatrix1D(final int size);

    /**
     * Create a float[] array filled with the values in SI unit.
     * @return float[]; array of values in SI unit
     */
    public float[] getValuesSI()
    {
        return this.vectorSI.toArray(); // this makes a deep copy
    }

    /**
     * Create a float[] array filled with the values in the original unit.
     * @return values in original unit
     */
    public float[] getValuesInUnit()
    {
        return getValuesInUnit(this.unit);
    }

    /**
     * Create a float[] array filled with the values in the specified unit.
     * @param targetUnit the unit to convert the values to
     * @return values in specific target unit
     */
    public float[] getValuesInUnit(final U targetUnit)
    {
        float[] values = this.vectorSI.toArray();
        for (int i = 0; i < values.length; i++)
            values[i] = (float) ValueUtil.expressAsUnit(values[i], targetUnit);
        return values;
    }

    public int size()
    {
        return (int) this.vectorSI.size();
    }

    /**
     * @see org.opentrafficsim.core.value.vfloat.vector.ReadOnlyFloatVectorFunctions#getSI(int)
     */
    public float getSI(final int index) throws ValueException
    {
        checkIndex(index);
        return safeGet(index);
    }

    /**
     * @see org.opentrafficsim.core.value.vfloat.vector.ReadOnlyFloatVectorFunctions#getInUnit(int)
     */
    public float getInUnit(final int index) throws ValueException
    {
        return (float) expressAsSpecifiedUnit(getSI(index));
    }

    /**
     * @see org.opentrafficsim.core.value.vfloat.vector.ReadOnlyFloatVectorFunctions#getInUnit(int,
     *      org.opentrafficsim.core.unit.Unit)
     */
    public float getInUnit(final int index, final U targetUnit) throws ValueException
    {
        return (float) ValueUtil.expressAsUnit(getSI(index), targetUnit);
    }

    /**
     * @see org.opentrafficsim.core.value.vfloat.vector.ReadOnlyFloatVectorFunctions#zSum()
     */
    public float zSum()
    {
        return this.vectorSI.zSum();
    }

    /**
     * @see org.opentrafficsim.core.value.vfloat.vectormut.Vectormut#cardinality()
     */
    @Override
    public int cardinality()
    {
        return this.vectorSI.cardinality();
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(final Object obj)
    {
        // unequal if object is of a different type.
        if (!(obj instanceof AbstractFloatVector<?>))
            return false;
        AbstractFloatVector<?> fv = (AbstractFloatVector<?>) obj;

        // unequal if the SI unit type differs (km/h and m/s could have the same content, so that is allowed)
        if (!this.getUnit().getStandardUnit().equals(fv.getUnit().getStandardUnit()))
            return false;

        // unequal if one is absolute and the other is relative
        if (this.isAbsolute() != fv.isAbsolute() || this.isRelative() != fv.isRelative())
            return false;

        // Colt's equals also tests the size of the vector
        return this.vectorSI.equals(fv.vectorSI);
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return toString(this.unit);
    }

    /**
     * Print this AbstractFloatVector with the values expressed in the specified unit.
     * @param displayUnit the unit to display the vector in.
     * @return a printable String with the vector contents
     */
    public String toString(final U displayUnit)
    {
        StringBuffer buf = new StringBuffer();
        if (this instanceof FloatVector)
        {
            buf.append("Immutable ");
            if (this instanceof FloatVector.Dense.Abs)
                buf.append("Dense  Abs ");
            else if (this instanceof FloatVector.Dense.Rel)
                buf.append("Dense  Rel ");
            else if (this instanceof FloatVector.Sparse.Abs)
                buf.append("Sparse Abs ");
            else if (this instanceof FloatVector.Sparse.Rel)
                buf.append("Sparse Rel ");
            else
                buf.append("??? ");
        }
        else if (this instanceof MutableFloatVector)
        {
            buf.append("Mutable   ");
            if (this instanceof MutableFloatVector.Dense.Abs)
                buf.append("Dense  Abs ");
            else if (this instanceof MutableFloatVector.Dense.Rel)
                buf.append("Dense  Rel ");
            else if (this instanceof MutableFloatVector.Sparse.Abs)
                buf.append("Sparse Abs ");
            else if (this instanceof MutableFloatVector.Sparse.Rel)
                buf.append("Sparse Rel ");
            else
                buf.append("??? ");
        }
        else
            buf.append("??? ");
        buf.append("[" + displayUnit.getAbbreviation() + "]");
        for (int i = 0; i < this.vectorSI.size(); i++)
        {
            float f = (float) ValueUtil.expressAsUnit(safeGet(i), displayUnit);
            buf.append(" " + Format.format(f));
        }
        return buf.toString();
    }

    /**
     * Centralized size equality check.
     * @param other FloatVector<U>; other FloatVector
     * @throws ValueException when vectors have unequal size
     */
    protected void checkSize(final AbstractFloatVector<?> other) throws ValueException
    {
        if (size() != other.size())
            throw new ValueException("The vectors have different sizes: " + size() + " != " + other.size());
    }

    /**
     * Centralized size equality check.
     * @param other float[]; array of float
     * @throws ValueException when vectors have unequal size
     */
    protected void checkSize(final float[] other) throws ValueException
    {
        if (size() != other.length)
            throw new ValueException("The vector and the array have different sizes: " + size() + " != " + other.length);
    }

    /**
     * Check that a provided index is valid. <br />
     * @param index integer; the value to check
     * @throws ValueException
     */
    protected void checkIndex(final int index) throws ValueException
    {
        if (index < 0 || index >= this.vectorSI.size())
            throw new ValueException("index out of range (valid range is 0.." + (this.vectorSI.size() - 1) + ", got "
                    + index + ")");
    }

    /**
     * Retrieve a value in vectorSI without checking validity of the index.
     * @param index integer; the index
     * @return float; the value stored at that index
     */
    protected float safeGet(int index)
    {
        return this.vectorSI.getQuick(index);
    }

    /**
     * Modify a value in vectorSI without checking validity of the index.
     * @param index integer; the index
     * @param valueSI float; the new value for the entry in vectorSI
     */
    protected void safeSet(final int index, final float valueSI)
    {
        this.vectorSI.setQuick(index, valueSI);
    }

    /**
     * Create a deep copy of the data.
     * @return FloatMatrix1D; deep copy of the data
     */
    protected FloatMatrix1D deepCopyOfData()
    {
        return this.vectorSI.copy();
    }

    /**
     * Check that a provided array can be used to create some descendant of an AbstractFloatVector.
     * @param fsArray FloatScalar[]; the provided array
     * @return FloatScalar[]; the provided array
     * @throws ValueException
     */
    protected static <U extends Unit<U>> FloatScalar<U>[] checkNonEmpty(FloatScalar<U>[] fsArray) throws ValueException
    {
        if (0 == fsArray.length)
            throw new ValueException(
                    "Cannot create a FloatValue or MutableFloatValue from an empty array of FloatScalar");
        return fsArray;
    }

}
