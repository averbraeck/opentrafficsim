package org.opentrafficsim.core.value;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.opentrafficsim.core.unit.Unit;

import cern.colt.function.tfloat.Float9Function;
import cern.colt.function.tfloat.FloatFloatFunction;
import cern.colt.function.tfloat.FloatFunction;
import cern.colt.function.tfloat.FloatProcedure;
import cern.colt.function.tfloat.IntIntFloatFunction;
import cern.colt.list.tfloat.FloatArrayList;
import cern.colt.list.tint.IntArrayList;
import cern.colt.map.tfloat.AbstractLongFloatMap;
import cern.colt.matrix.io.MatrixVectorReader;
import cern.colt.matrix.tfcomplex.impl.SparseFComplexMatrix2D;
import cern.colt.matrix.tfloat.FloatMatrix1D;
import cern.colt.matrix.tfloat.FloatMatrix1DProcedure;
import cern.colt.matrix.tfloat.FloatMatrix2D;
import cern.colt.matrix.tfloat.impl.SparseCCFloatMatrix2D;
import cern.colt.matrix.tfloat.impl.SparseCCMFloatMatrix2D;
import cern.colt.matrix.tfloat.impl.SparseFloatMatrix2D;
import cern.colt.matrix.tfloat.impl.SparseRCFloatMatrix2D;
import cern.colt.matrix.tfloat.impl.SparseRCMFloatMatrix2D;

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
 * @author <a href="http://Hansvanlint.weblog.tudelft.nl">Hans van Lint</a>
 * @author <a href="http://www.citg.tudelft.nl">Peter Knoppers</a>
 * @author <a href="http://www.citg.tudelft.nl">Guus Tamminga</a>
 * @author <a href="http://www.citg.tudelft.nl">Yufei Yuan</a>
 */
public class FloatSparseMatrixAbs<U extends Unit<U>> extends SparseFloatMatrix2D
{
    private final U unit;

    /**
     * @param values
     */
    public FloatSparseMatrixAbs(float[][] values, U unit)
    {
        super(values);
        this.unit = unit;
    }

    /**
     * @param rows
     * @param columns
     * @param elements
     * @param rowZero
     * @param columnZero
     * @param rowStride
     * @param columnStride
     */
    public FloatSparseMatrixAbs(int rows, int columns, AbstractLongFloatMap elements, int rowZero, int columnZero,
            int rowStride, int columnStride, U unit)
    {
        super(rows, columns, elements, rowZero, columnZero, rowStride, columnStride);
        this.unit = unit;
    }

    /**
     * @param rows
     * @param columns
     * @param initialCapacity
     * @param minLoadFactor
     * @param maxLoadFactor
     */
    public FloatSparseMatrixAbs(int rows, int columns, int initialCapacity, float minLoadFactor, float maxLoadFactor, U unit)
    {
        super(rows, columns, initialCapacity, minLoadFactor, maxLoadFactor);
        this.unit = unit;
    }

    /**
     * @param rows
     * @param columns
     * @param rowIndexes
     * @param columnIndexes
     * @param value
     */
    public FloatSparseMatrixAbs(int rows, int columns, int[] rowIndexes, int[] columnIndexes, float value, U unit)
    {
        super(rows, columns, rowIndexes, columnIndexes, value);
        this.unit = unit;
    }

    /**
     * @param rows
     * @param columns
     * @param rowIndexes
     * @param columnIndexes
     * @param values
     */
    public FloatSparseMatrixAbs(int rows, int columns, int[] rowIndexes, int[] columnIndexes, float[] values, U unit)
    {
        super(rows, columns, rowIndexes, columnIndexes, values);
        this.unit = unit;
    }

    /**
     * @param rows
     * @param columns
     */
    public FloatSparseMatrixAbs(int rows, int columns, U unit)
    {
        super(rows, columns);
        this.unit = unit;
    }

    /**
     * @param reader
     * @throws IOException
     */
    public FloatSparseMatrixAbs(MatrixVectorReader reader, U unit) throws IOException
    {
        super(reader);
        this.unit = unit;
    }

    /**
     * @see cern.colt.matrix.tfloat.impl.SparseFloatMatrix2D#assign(cern.colt.function.tfloat.FloatFunction)
     */
    @Override
    public FloatMatrix2D assign(FloatFunction function)
    {
        return super.assign(function);
    }

    /**
     * implements functionality of cern.colt.matrix.tfloat.impl.SparseFloatMatrix2D#assign(float)
     */
    public FloatMatrix2D assign(FloatScalarAbs<U> value)
    {
        return super.assign(value.getValue());
    }

    /**
     * @see cern.colt.matrix.tfloat.impl.SparseFloatMatrix2D#assign(cern.colt.matrix.tfloat.FloatMatrix2D)
     */
    @Override
    public FloatMatrix2D assign(FloatMatrix2D source)
    {
        return super.assign(source);
    }

    /**
     * @see cern.colt.matrix.tfloat.impl.SparseFloatMatrix2D#assign(cern.colt.matrix.tfloat.FloatMatrix2D, cern.colt.function.tfloat.FloatFloatFunction)
     */
    @Override
    public FloatMatrix2D assign(FloatMatrix2D y, FloatFloatFunction function)
    {
        return super.assign(y, function);
    }

    /**
     * @see cern.colt.matrix.tfloat.impl.SparseFloatMatrix2D#assign(int[], int[], float, cern.colt.function.tfloat.FloatFloatFunction)
     */
    @Override
    public SparseFloatMatrix2D assign(int[] rowIndexes, int[] columnIndexes, float value, FloatFloatFunction function)
    {
        return super.assign(rowIndexes, columnIndexes, value, function);
    }

    /**
     * @see cern.colt.matrix.tfloat.impl.SparseFloatMatrix2D#assign(int[], int[], float[], cern.colt.function.tfloat.FloatFloatFunction)
     */
    @Override
    public SparseFloatMatrix2D assign(int[] rowIndexes, int[] columnIndexes, float[] values, FloatFloatFunction function)
    {
        return super.assign(rowIndexes, columnIndexes, values, function);
    }

    /**
     * @see cern.colt.matrix.tfloat.impl.SparseFloatMatrix2D#cardinality()
     */
    @Override
    public int cardinality()
    {
        return super.cardinality();
    }

    /**
     * @see cern.colt.matrix.tfloat.impl.SparseFloatMatrix2D#getColumnCompressed(boolean)
     */
    @Override
    public SparseCCFloatMatrix2D getColumnCompressed(boolean sortRowIndexes)
    {
        return super.getColumnCompressed(sortRowIndexes);
    }

    /**
     * @see cern.colt.matrix.tfloat.impl.SparseFloatMatrix2D#getColumnCompressedModified()
     */
    @Override
    public SparseCCMFloatMatrix2D getColumnCompressedModified()
    {
        return super.getColumnCompressedModified();
    }

    /**
     * @see cern.colt.matrix.tfloat.impl.SparseFloatMatrix2D#getRowCompressed(boolean)
     */
    @Override
    public SparseRCFloatMatrix2D getRowCompressed(boolean sortColumnIndexes)
    {
        return super.getRowCompressed(sortColumnIndexes);
    }

    /**
     * @see cern.colt.matrix.tfloat.impl.SparseFloatMatrix2D#getRowCompressedModified()
     */
    @Override
    public SparseRCMFloatMatrix2D getRowCompressedModified()
    {
        return super.getRowCompressedModified();
    }

    /**
     * @see cern.colt.matrix.tfloat.impl.SparseFloatMatrix2D#elements()
     */
    @Override
    public AbstractLongFloatMap elements()
    {
        return super.elements();
    }

    /**
     * @see cern.colt.matrix.tfloat.impl.SparseFloatMatrix2D#ensureCapacity(int)
     */
    @Override
    public void ensureCapacity(int minCapacity)
    {
        super.ensureCapacity(minCapacity);
    }

    /**
     * @see cern.colt.matrix.tfloat.impl.SparseFloatMatrix2D#forEachNonZero(cern.colt.function.tfloat.IntIntFloatFunction)
     */
    @Override
    public FloatMatrix2D forEachNonZero(IntIntFloatFunction function)
    {
        return super.forEachNonZero(function);
    }

    /**
     * @see cern.colt.matrix.tfloat.impl.SparseFloatMatrix2D#getQuick(int, int)
     */
    @Override
    public synchronized float getQuick(int row, int column)
    {
        return super.getQuick(row, column);
    }

    /**
     * @see cern.colt.matrix.tfloat.impl.SparseFloatMatrix2D#index(int, int)
     */
    @Override
    public long index(int row, int column)
    {
        return super.index(row, column);
    }

    /**
     * @see cern.colt.matrix.tfloat.impl.SparseFloatMatrix2D#like(int, int)
     */
    @Override
    public FloatMatrix2D like(int rows, int columns)
    {
        return super.like(rows, columns);
    }

    /**
     * @see cern.colt.matrix.tfloat.impl.SparseFloatMatrix2D#like1D(int)
     */
    @Override
    public FloatMatrix1D like1D(int size)
    {
        return super.like1D(size);
    }

    /**
     * @see cern.colt.matrix.tfloat.impl.SparseFloatMatrix2D#setQuick(int, int, float)
     */
    @Override
    public synchronized void setQuick(int row, int column, float value)
    {
        super.setQuick(row, column, value);
    }

    /**
     * @see cern.colt.matrix.tfloat.impl.SparseFloatMatrix2D#toString()
     */
    @Override
    public String toString()
    {
        return super.toString();
    }

    /**
     * @see cern.colt.matrix.tfloat.impl.SparseFloatMatrix2D#trimToSize()
     */
    @Override
    public void trimToSize()
    {
        super.trimToSize();
    }

    /**
     * @see cern.colt.matrix.tfloat.impl.SparseFloatMatrix2D#vectorize()
     */
    @Override
    public FloatMatrix1D vectorize()
    {
        return super.vectorize();
    }

    /**
     * @see cern.colt.matrix.tfloat.impl.SparseFloatMatrix2D#zMult(cern.colt.matrix.tfloat.FloatMatrix1D, cern.colt.matrix.tfloat.FloatMatrix1D, float, float, boolean)
     */
    @Override
    public FloatMatrix1D zMult(FloatMatrix1D y, FloatMatrix1D z, float alpha, float beta, boolean transposeA)
    {
        return super.zMult(y, z, alpha, beta, transposeA);
    }

    /**
     * @see cern.colt.matrix.tfloat.impl.SparseFloatMatrix2D#zMult(cern.colt.matrix.tfloat.FloatMatrix2D, cern.colt.matrix.tfloat.FloatMatrix2D, float, float, boolean, boolean)
     */
    @Override
    public FloatMatrix2D zMult(FloatMatrix2D B, FloatMatrix2D C, float alpha, float beta, boolean transposeA,
            boolean transposeB)
    {
        return super.zMult(B, C, alpha, beta, transposeA, transposeB);
    }

    /**
     * @see cern.colt.matrix.tfloat.impl.SparseFloatMatrix2D#haveSharedCellsRaw(cern.colt.matrix.tfloat.FloatMatrix2D)
     */
    @Override
    protected boolean haveSharedCellsRaw(FloatMatrix2D other)
    {
        return super.haveSharedCellsRaw(other);
    }

    /**
     * @see cern.colt.matrix.tfloat.impl.SparseFloatMatrix2D#like1D(int, int, int)
     */
    @Override
    protected FloatMatrix1D like1D(int size, int offset, int stride)
    {
        return super.like1D(size, offset, stride);
    }

    /**
     * @see cern.colt.matrix.tfloat.impl.SparseFloatMatrix2D#viewSelectionLike(int[], int[])
     */
    @Override
    protected FloatMatrix2D viewSelectionLike(int[] rowOffsets, int[] columnOffsets)
    {
        return super.viewSelectionLike(rowOffsets, columnOffsets);
    }

    /**
     * @see cern.colt.matrix.tfloat.FloatMatrix2D#aggregate(cern.colt.function.tfloat.FloatFloatFunction, cern.colt.function.tfloat.FloatFunction)
     */
    @Override
    public float aggregate(FloatFloatFunction aggr, FloatFunction f)
    {
        return super.aggregate(aggr, f);
    }

    /**
     * @see cern.colt.matrix.tfloat.FloatMatrix2D#aggregate(cern.colt.function.tfloat.FloatFloatFunction, cern.colt.function.tfloat.FloatFunction, cern.colt.function.tfloat.FloatProcedure)
     */
    @Override
    public float aggregate(FloatFloatFunction aggr, FloatFunction f, FloatProcedure cond)
    {
        return super.aggregate(aggr, f, cond);
    }

    /**
     * @see cern.colt.matrix.tfloat.FloatMatrix2D#aggregate(cern.colt.function.tfloat.FloatFloatFunction, cern.colt.function.tfloat.FloatFunction, cern.colt.list.tint.IntArrayList, cern.colt.list.tint.IntArrayList)
     */
    @Override
    public float aggregate(FloatFloatFunction aggr, FloatFunction f, IntArrayList rowList, IntArrayList columnList)
    {
        return super.aggregate(aggr, f, rowList, columnList);
    }

    /**
     * @see cern.colt.matrix.tfloat.FloatMatrix2D#aggregate(cern.colt.matrix.tfloat.FloatMatrix2D, cern.colt.function.tfloat.FloatFloatFunction, cern.colt.function.tfloat.FloatFloatFunction)
     */
    @Override
    public float aggregate(FloatMatrix2D other, FloatFloatFunction aggr, FloatFloatFunction f)
    {
        return super.aggregate(other, aggr, f);
    }

    /**
     * @see cern.colt.matrix.tfloat.FloatMatrix2D#assign(cern.colt.function.tfloat.FloatProcedure, cern.colt.function.tfloat.FloatFunction)
     */
    @Override
    public FloatMatrix2D assign(FloatProcedure cond, FloatFunction f)
    {
        return super.assign(cond, f);
    }

    /**
     * @see cern.colt.matrix.tfloat.FloatMatrix2D#assign(cern.colt.function.tfloat.FloatProcedure, float)
     */
    @Override
    public FloatMatrix2D assign(FloatProcedure cond, float value)
    {
        return super.assign(cond, value);
    }

    /**
     * @see cern.colt.matrix.tfloat.FloatMatrix2D#assign(float[])
     */
    @Override
    public FloatMatrix2D assign(float[] values)
    {
        return super.assign(values);
    }

    /**
     * @see cern.colt.matrix.tfloat.FloatMatrix2D#assign(float[][])
     */
    @Override
    public FloatMatrix2D assign(float[][] values)
    {
        return super.assign(values);
    }

    /**
     * @see cern.colt.matrix.tfloat.FloatMatrix2D#assign(cern.colt.matrix.tfloat.FloatMatrix2D, cern.colt.function.tfloat.FloatFloatFunction, cern.colt.list.tint.IntArrayList, cern.colt.list.tint.IntArrayList)
     */
    @Override
    public FloatMatrix2D assign(FloatMatrix2D y, FloatFloatFunction function, IntArrayList rowList,
            IntArrayList columnList)
    {
        return super.assign(y, function, rowList, columnList);
    }

    /**
     * @see cern.colt.matrix.tfloat.FloatMatrix2D#copy()
     */
    @Override
    public FloatMatrix2D copy()
    {
        return super.copy();
    }

    /**
     * @see cern.colt.matrix.tfloat.FloatMatrix2D#equals(float)
     */
    @Override
    public boolean equals(float value)
    {
        return super.equals(value);
    }

    /**
     * @see cern.colt.matrix.tfloat.FloatMatrix2D#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj)
    {
        return super.equals(obj);
    }

    /**
     * @see cern.colt.matrix.tfloat.FloatMatrix2D#get(int, int)
     */
    @Override
    public float get(int row, int column)
    {
        return super.get(row, column);
    }

    /**
     * @see cern.colt.matrix.tfloat.FloatMatrix2D#getMaxLocation()
     */
    @Override
    public float[] getMaxLocation()
    {
        return super.getMaxLocation();
    }

    /**
     * @see cern.colt.matrix.tfloat.FloatMatrix2D#getMinLocation()
     */
    @Override
    public float[] getMinLocation()
    {
        return super.getMinLocation();
    }

    /**
     * @see cern.colt.matrix.tfloat.FloatMatrix2D#getNegativeValues(cern.colt.list.tint.IntArrayList, cern.colt.list.tint.IntArrayList, cern.colt.list.tfloat.FloatArrayList)
     */
    @Override
    public void getNegativeValues(IntArrayList rowList, IntArrayList columnList, FloatArrayList valueList)
    {
        super.getNegativeValues(rowList, columnList, valueList);
    }

    /**
     * @see cern.colt.matrix.tfloat.FloatMatrix2D#getNonZeros(cern.colt.list.tint.IntArrayList, cern.colt.list.tint.IntArrayList, cern.colt.list.tfloat.FloatArrayList)
     */
    @Override
    public void getNonZeros(IntArrayList rowList, IntArrayList columnList, FloatArrayList valueList)
    {
        super.getNonZeros(rowList, columnList, valueList);
    }

    /**
     * @see cern.colt.matrix.tfloat.FloatMatrix2D#getPositiveValues(cern.colt.list.tint.IntArrayList, cern.colt.list.tint.IntArrayList, cern.colt.list.tfloat.FloatArrayList)
     */
    @Override
    public void getPositiveValues(IntArrayList rowList, IntArrayList columnList, FloatArrayList valueList)
    {
        super.getPositiveValues(rowList, columnList, valueList);
    }

    /**
     * @see cern.colt.matrix.tfloat.FloatMatrix2D#like()
     */
    @Override
    public FloatMatrix2D like()
    {
        return super.like();
    }

    /**
     * @see cern.colt.matrix.tfloat.FloatMatrix2D#normalize()
     */
    @Override
    public void normalize()
    {
        super.normalize();
    }

    /**
     * @see cern.colt.matrix.tfloat.FloatMatrix2D#set(int, int, float)
     */
    @Override
    public void set(int row, int column, float value)
    {
        super.set(row, column, value);
    }

    /**
     * @see cern.colt.matrix.tfloat.FloatMatrix2D#toArray()
     */
    @Override
    public float[][] toArray()
    {
        return super.toArray();
    }

    /**
     * @see cern.colt.matrix.tfloat.FloatMatrix2D#viewColumn(int)
     */
    @Override
    public FloatMatrix1D viewColumn(int column)
    {
        return super.viewColumn(column);
    }

    /**
     * @see cern.colt.matrix.tfloat.FloatMatrix2D#viewColumnFlip()
     */
    @Override
    public FloatMatrix2D viewColumnFlip()
    {
        return super.viewColumnFlip();
    }

    /**
     * @see cern.colt.matrix.tfloat.FloatMatrix2D#viewDice()
     */
    @Override
    public FloatMatrix2D viewDice()
    {
        return super.viewDice();
    }

    /**
     * @see cern.colt.matrix.tfloat.FloatMatrix2D#viewPart(int, int, int, int)
     */
    @Override
    public FloatMatrix2D viewPart(int row, int column, int height, int width)
    {
        return super.viewPart(row, column, height, width);
    }

    /**
     * @see cern.colt.matrix.tfloat.FloatMatrix2D#viewRow(int)
     */
    @Override
    public FloatMatrix1D viewRow(int row)
    {
        return super.viewRow(row);
    }

    /**
     * @see cern.colt.matrix.tfloat.FloatMatrix2D#viewRowFlip()
     */
    @Override
    public FloatMatrix2D viewRowFlip()
    {
        return super.viewRowFlip();
    }

    /**
     * @see cern.colt.matrix.tfloat.FloatMatrix2D#viewSelection(cern.colt.matrix.tfloat.FloatMatrix1DProcedure)
     */
    @Override
    public FloatMatrix2D viewSelection(FloatMatrix1DProcedure condition)
    {
        return super.viewSelection(condition);
    }

    /**
     * @see cern.colt.matrix.tfloat.FloatMatrix2D#viewSelection(int[], int[])
     */
    @Override
    public FloatMatrix2D viewSelection(int[] rowIndexes, int[] columnIndexes)
    {
        return super.viewSelection(rowIndexes, columnIndexes);
    }

    /**
     * @see cern.colt.matrix.tfloat.FloatMatrix2D#viewSelection(java.util.Set)
     */
    @Override
    public FloatMatrix2D viewSelection(Set<int[]> indexes)
    {
        return super.viewSelection(indexes);
    }

    /**
     * @see cern.colt.matrix.tfloat.FloatMatrix2D#viewSorted(int)
     */
    @Override
    public FloatMatrix2D viewSorted(int column)
    {
        return super.viewSorted(column);
    }

    /**
     * @see cern.colt.matrix.tfloat.FloatMatrix2D#viewStrides(int, int)
     */
    @Override
    public FloatMatrix2D viewStrides(int rowStride, int columnStride)
    {
        return super.viewStrides(rowStride, columnStride);
    }

    /**
     * @see cern.colt.matrix.tfloat.FloatMatrix2D#zAssign8Neighbors(cern.colt.matrix.tfloat.FloatMatrix2D, cern.colt.function.tfloat.Float9Function)
     */
    @Override
    public void zAssign8Neighbors(FloatMatrix2D B, Float9Function function)
    {
        super.zAssign8Neighbors(B, function);
    }

    /**
     * @see cern.colt.matrix.tfloat.FloatMatrix2D#zMult(cern.colt.matrix.tfloat.FloatMatrix1D, cern.colt.matrix.tfloat.FloatMatrix1D)
     */
    @Override
    public FloatMatrix1D zMult(FloatMatrix1D y, FloatMatrix1D z)
    {
        return super.zMult(y, z);
    }

    /**
     * @see cern.colt.matrix.tfloat.FloatMatrix2D#zMult(cern.colt.matrix.tfloat.FloatMatrix2D, cern.colt.matrix.tfloat.FloatMatrix2D)
     */
    @Override
    public FloatMatrix2D zMult(FloatMatrix2D B, FloatMatrix2D C)
    {
        return super.zMult(B, C);
    }

    /**
     * @see cern.colt.matrix.tfloat.FloatMatrix2D#zSum()
     */
    @Override
    public float zSum()
    {
        return super.zSum();
    }

    /**
     * @see cern.colt.matrix.tfloat.FloatMatrix2D#getContent()
     */
    @Override
    protected FloatMatrix2D getContent()
    {
        return super.getContent();
    }

    /**
     * @see cern.colt.matrix.tfloat.FloatMatrix2D#haveSharedCells(cern.colt.matrix.tfloat.FloatMatrix2D)
     */
    @Override
    protected boolean haveSharedCells(FloatMatrix2D other)
    {
        return super.haveSharedCells(other);
    }

    /**
     * @see cern.colt.matrix.tfloat.FloatMatrix2D#view()
     */
    @Override
    protected FloatMatrix2D view()
    {
        return super.view();
    }

    
}
