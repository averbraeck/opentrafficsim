package org.opentrafficsim.core.egtf.typed;

import org.djunits.unit.SpeedUnit;
import org.djunits.unit.Unit;
import org.djunits.value.AbstractScalar;
import org.djunits.value.vdouble.matrix.base.DoubleMatrixInterface;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.vector.DurationVector;
import org.djunits.value.vdouble.vector.LengthVector;
import org.djunits.value.vdouble.vector.base.DoubleVectorInterface;
import org.opentrafficsim.core.egtf.DataStream;
import org.opentrafficsim.core.egtf.Egtf;
import org.opentrafficsim.core.egtf.KernelShape;
import org.opentrafficsim.core.egtf.Quantity;

/**
 * Typed version of the EGTF.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class TypedEgtf extends Egtf
{

    /**
     * Constructor using cCong = -18km/h, cFree = 80km/h, deltaV = 10km/h and vc = 80km/h. A default kernel is set.
     */
    public TypedEgtf()
    {
    }

    /**
     * Constructor defining global settings. A default kernel is set.
     * @param cCong Speed; shock wave speed in congestion
     * @param cFree Speed; shock wave speed in free flow
     * @param deltaV Speed; speed range between congestion and free flow
     * @param vc Speed; flip-over speed below which we have congestion
     */
    public TypedEgtf(final Speed cCong, final Speed cFree, final Speed deltaV, final Speed vc)
    {
        super(cCong.getInUnit(SpeedUnit.KM_PER_HOUR), cFree.getInUnit(SpeedUnit.KM_PER_HOUR),
                deltaV.getInUnit(SpeedUnit.KM_PER_HOUR), vc.getInUnit(SpeedUnit.KM_PER_HOUR));
    }

    /**
     * Convenience constructor that also sets a specified kernel.
     * @param cCong Speed; shock wave speed in congestion
     * @param cFree Speed; shock wave speed in free flow
     * @param deltaV Speed; speed range between congestion and free flow
     * @param vc Speed; flip-over speed below which we have congestion
     * @param sigma Length; spatial kernel size
     * @param tau Duration; temporal kernel size
     * @param xMax Length; maximum spatial range
     * @param tMax Duration; maximum temporal range
     */
    @SuppressWarnings("parameternumber")
    public TypedEgtf(final Speed cCong, final Speed cFree, final Speed deltaV, final Speed vc, final Length sigma,
            final Duration tau, final Length xMax, final Duration tMax)
    {
        super(cCong.getInUnit(SpeedUnit.KM_PER_HOUR), cFree.getInUnit(SpeedUnit.KM_PER_HOUR),
                deltaV.getInUnit(SpeedUnit.KM_PER_HOUR), vc.getInUnit(SpeedUnit.KM_PER_HOUR), sigma.si, tau.si, xMax.si,
                tMax.si);
    }

    /**
     * Adds point data.
     * @param quantity Quantity&lt;Z, ?&gt;; quantity of the data
     * @param location Length; location
     * @param time Duration; time
     * @param value Z; data value
     * @param <U> unit of type
     * @param <Z> value type
     * @throws IllegalStateException if data was added with a data stream previously
     */
    public synchronized <U extends Unit<U>, Z extends AbstractScalar<U, Z>> void addPointData(final Quantity<Z, ?> quantity,
            final Length location, final Duration time, final Z value)
    {
        addPointDataSI(quantity, location.si, time.si, value.doubleValue());
    }

    /**
     * Adds point data.
     * @param dataStream DataStream&lt;Z&gt;; data stream of the data
     * @param location Length; location
     * @param time Duration; time
     * @param value Z; data value
     * @param <U> unit of type
     * @param <Z> value type
     * @throws IllegalStateException if data was added with a quantity previously
     */
    public synchronized <U extends Unit<U>, Z extends AbstractScalar<U, Z>> void addPointData(final DataStream<Z> dataStream,
            final Length location, final Duration time, final Z value)
    {
        addPointDataSI(dataStream, location.si, time.si, value.doubleValue());
    }

    /**
     * Adds vector data.
     * @param quantity Quantity&lt;Z, ?&gt;; quantity of the data
     * @param location LengthVector; locations
     * @param time DurationVector; times
     * @param values DoubleVectorInterface&lt;U&gt;; data values
     * @param <U> unit of type
     * @param <Z> value type
     * @throws IllegalStateException if data was added with a data stream previously
     */
    public synchronized <U extends Unit<U>, Z extends AbstractScalar<U, Z>> void addVectorData(final Quantity<Z, ?> quantity,
            final LengthVector location, final DurationVector time, final DoubleVectorInterface<U, ?, ?> values)
    {
        addVectorDataSI(quantity, location.getValuesSI(), time.getValuesSI(), values.getValuesSI());
    }

    /**
     * Adds vector data.
     * @param dataStream DataStream&lt;Z&gt;; data stream of the data
     * @param location LengthVector; locations
     * @param time DurationVector; times
     * @param values DoubleVectorInterface&lt;U&gt;; data values
     * @param <U> unit of type
     * @param <Z> value type
     * @throws IllegalStateException if data was added with a quantity previously
     */
    public synchronized <U extends Unit<U>, Z extends AbstractScalar<U, Z>> void addVectorData(final DataStream<Z> dataStream,
            final LengthVector location, final DurationVector time, final DoubleVectorInterface<U, ?, ?> values)
    {
        addVectorDataSI(dataStream, location.getValuesSI(), time.getValuesSI(), values.getValuesSI());
    }

    /**
     * Adds grid data.
     * @param quantity Quantity&lt;Z, ?&gt;; quantity of the data
     * @param location LengthVector; locations
     * @param time DurationVector; times
     * @param values DoubleMatrixInterface&lt;U&gt;; data values
     * @param <U> unit of type
     * @param <Z> value type
     * @throws IllegalStateException if data was added with a data stream previously
     */
    public synchronized <U extends Unit<U>, Z extends AbstractScalar<U, Z>> void addGridData(final Quantity<Z, ?> quantity,
            final LengthVector location, final DurationVector time, final DoubleMatrixInterface<U, ?, ?, ?> values)
    {
        addGridDataSI(quantity, location.getValuesSI(), time.getValuesSI(), values.getValuesSI());
    }

    /**
     * Adds grid data.
     * @param dataStream DataStream&lt;Z&gt;; data stream of the data
     * @param location LengthVector; locations
     * @param time DurationVector; times
     * @param values DoubleMatrixInterface&lt;U&gt;; data values
     * @param <U> unit of type
     * @param <Z> value type
     * @throws IllegalStateException if data was added with a quantity previously
     */
    public synchronized <U extends Unit<U>, Z extends AbstractScalar<U, Z>> void addGridData(final DataStream<Z> dataStream,
            final LengthVector location, final DurationVector time, final DoubleMatrixInterface<U, ?, ?, ?> values)
    {
        addGridDataSI(dataStream, location.getValuesSI(), time.getValuesSI(), values.getValuesSI());
    }

    /**
     * Removes all data from before the given time. This is useful in live usages of this class, where older data is no longer
     * required.
     * @param time Duration; time before which all data can be removed
     */
    public void clearDataBefore(final Duration time)
    {
        clearDataBefore(time.si);
    }

    /**
     * Sets an exponential kernel with infinite range.
     * @param sigma Length; spatial kernel size
     * @param tau Duration; temporal kernel size
     */
    public void setKernel(final Length sigma, final Duration tau)
    {
        setKernelSI(sigma.si, tau.si);
    }

    /**
     * Returns an exponential kernel with limited range.
     * @param sigma Length; spatial kernel size in [m]
     * @param tau Duration; temporal kernel size in [s]
     * @param xMax Length; maximum spatial range in [m]
     * @param tMax Duration; maximum temporal range in [s]
     */
    public void setKernel(final Length sigma, final Duration tau, final Length xMax, final Duration tMax)
    {
        setKernelSI(sigma.si, tau.si, xMax.si, tMax.si);
    }

    /**
     * Sets a Gaussian kernel with infinite range.
     * @param sigma Length; spatial kernel size
     * @param tau Duration; temporal kernel size
     */
    public void setGaussKernel(final Length sigma, final Duration tau)
    {
        setGaussKernelSI(sigma.si, tau.si);
    }

    /**
     * Returns a Gaussian kernel with limited range.
     * @param sigma Length; spatial kernel size in [m]
     * @param tau Duration; temporal kernel size in [s]
     * @param xMax Length; maximum spatial range in [m]
     * @param tMax Duration; maximum temporal range in [s]
     */
    public void setGaussKernel(final Length sigma, final Duration tau, final Length xMax, final Duration tMax)
    {
        setGaussKernelSI(sigma.si, tau.si, xMax.si, tMax.si);
    }

    /**
     * Sets a kernel with limited range and provided shape. The shape allows using non-exponential kernels.
     * @param xMax Length; maximum spatial range
     * @param tMax Duration; maximum temporal range
     * @param shape KernelShape; shape of the kernel
     */
    public void setKernel(final Length xMax, final Duration tMax, final KernelShape shape)
    {
        setKernelSI(xMax.si, tMax.si, shape);
    }

    /**
     * Returns filtered data.
     * @param location LengthVector; location of output grid
     * @param time DurationVector; time of output grid
     * @param quantities Quantity&lt;?, ?&gt;...; quantities to calculate filtered data of
     * @return Filter; filtered data, {@code null} when interrupted
     */
    public TypedFilter filter(final LengthVector location, final DurationVector time, final Quantity<?, ?>... quantities)
    {
        return new TypedFilter(filterSI(location.getValuesSI(), time.getValuesSI(), quantities));
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "TypedEGTF []";
    }

}
