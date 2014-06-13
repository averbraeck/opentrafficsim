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
import cern.colt.matrix.AbstractMatrix;
import cern.colt.matrix.AbstractMatrix1D;
import cern.colt.matrix.io.MatrixVectorReader;
import cern.colt.matrix.tfcomplex.impl.SparseFComplexMatrix2D;
import cern.colt.matrix.tfloat.FloatMatrix1D;
import cern.colt.matrix.tfloat.FloatMatrix1DProcedure;
import cern.colt.matrix.tfloat.FloatMatrix2D;
import cern.colt.matrix.tfloat.FloatMatrix3D;
import cern.colt.matrix.tfloat.impl.DenseFloatMatrix1D;
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
public class FloatValue<U extends Unit<U>>
{
    private final float value;
    protected final U unit;

    /**
     * @param unit
     */
    public FloatValue(float value, U unit)
    {
        super();
        this.value = value;
        this.unit = unit;
    }

    public static class DenseVector<U>
    {
        private DenseFloatMatrix1D vector;
        protected final U unit;
        
        public DenseVector(float[] values, final U unit)
        {
            this.vector = new DenseFloatMatrix1D(values);
            this.unit = unit;
        }
        
        public String toString()
        {
            return this.unit + " " + this.vector.toString();
        }
    }
    
    public static class Matrix  extends AbstractMatrix1D
    {
        
    }
    
    public static class Cube  extends AbstractMatrix1D
    {
        
    }
}
