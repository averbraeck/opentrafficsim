package org.opentrafficsim.draw.egtf.typed;

import java.util.Optional;

import org.djunits.unit.SpeedUnit;
import org.djunits.unit.Unit;
import org.djunits.value.base.Scalar;
import org.djunits.value.vdouble.matrix.base.DoubleMatrix;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.vector.DurationVector;
import org.djunits.value.vdouble.vector.LengthVector;
import org.djunits.value.vdouble.vector.base.DoubleVector;
import org.opentrafficsim.draw.egtf.DataStream;
import org.opentrafficsim.draw.egtf.Egtf;
import org.opentrafficsim.draw.egtf.Filter;
import org.opentrafficsim.draw.egtf.KernelShape;
import org.opentrafficsim.draw.egtf.Quantity;

/**
 * Typed version of the EGTF.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
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
     * @param cCong shock wave speed in congestion
     * @param cFree shock wave speed in free flow
     * @param deltaV speed range between congestion and free flow
     * @param vc flip-over speed below which we have congestion
     */
    public TypedEgtf(final Speed cCong, final Speed cFree, final Speed deltaV, final Speed vc)
    {
        super(cCong.getInUnit(SpeedUnit.KM_PER_HOUR), cFree.getInUnit(SpeedUnit.KM_PER_HOUR),
                deltaV.getInUnit(SpeedUnit.KM_PER_HOUR), vc.getInUnit(SpeedUnit.KM_PER_HOUR));
    }

    /**
     * Convenience constructor that also sets a specified kernel.
     * @param cCong shock wave speed in congestion
     * @param cFree shock wave speed in free flow
     * @param deltaV speed range between congestion and free flow
     * @param vc flip-over speed below which we have congestion
     * @param sigma spatial kernel size
     * @param tau temporal kernel size
     * @param xMax maximum spatial range
     * @param tMax maximum temporal range
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
     * @param quantity quantity of the data
     * @param location location
     * @param time time
     * @param value data value
     * @param <U> unit of type
     * @param <Z> value type
     * @throws IllegalStateException if data was added with a data stream previously
     */
    public synchronized <U extends Unit<U>, Z extends Scalar<U, Z>> void addPointData(final Quantity<Z, ?> quantity,
            final Length location, final Duration time, final Z value)
    {
        addPointDataSI(quantity, location.si, time.si, value.doubleValue());
    }

    /**
     * Adds point data.
     * @param dataStream data stream of the data
     * @param location location
     * @param time time
     * @param value data value
     * @param <U> unit of type
     * @param <Z> value type
     * @throws IllegalStateException if data was added with a quantity previously
     */
    public synchronized <U extends Unit<U>, Z extends Scalar<U, Z>> void addPointData(final DataStream<Z> dataStream,
            final Length location, final Duration time, final Z value)
    {
        addPointDataSI(dataStream, location.si, time.si, value.doubleValue());
    }

    /**
     * Adds vector data.
     * @param quantity quantity of the data
     * @param location locations
     * @param time times
     * @param values data values
     * @param <U> unit of type
     * @param <Z> value type
     * @throws IllegalStateException if data was added with a data stream previously
     */
    public synchronized <U extends Unit<U>, Z extends Scalar<U, Z>> void addVectorData(final Quantity<Z, ?> quantity,
            final LengthVector location, final DurationVector time, final DoubleVector<U, ?, ?> values)
    {
        addVectorDataSI(quantity, location.getValuesSI(), time.getValuesSI(), values.getValuesSI());
    }

    /**
     * Adds vector data.
     * @param dataStream data stream of the data
     * @param location locations
     * @param time times
     * @param values data values
     * @param <U> unit of type
     * @param <Z> value type
     * @throws IllegalStateException if data was added with a quantity previously
     */
    public synchronized <U extends Unit<U>, Z extends Scalar<U, Z>> void addVectorData(final DataStream<Z> dataStream,
            final LengthVector location, final DurationVector time, final DoubleVector<U, ?, ?> values)
    {
        addVectorDataSI(dataStream, location.getValuesSI(), time.getValuesSI(), values.getValuesSI());
    }

    /**
     * Adds grid data.
     * @param quantity quantity of the data
     * @param location locations
     * @param time times
     * @param values data values
     * @param <U> unit of type
     * @param <Z> value type
     * @throws IllegalStateException if data was added with a data stream previously
     */
    public synchronized <U extends Unit<U>, Z extends Scalar<U, Z>> void addGridData(final Quantity<Z, ?> quantity,
            final LengthVector location, final DurationVector time, final DoubleMatrix<U, ?, ?, ?> values)
    {
        addGridDataSI(quantity, location.getValuesSI(), time.getValuesSI(), values.getValuesSI());
    }

    /**
     * Adds grid data.
     * @param dataStream data stream of the data
     * @param location locations
     * @param time times
     * @param values data values
     * @param <U> unit of type
     * @param <Z> value type
     * @throws IllegalStateException if data was added with a quantity previously
     */
    public synchronized <U extends Unit<U>, Z extends Scalar<U, Z>> void addGridData(final DataStream<Z> dataStream,
            final LengthVector location, final DurationVector time, final DoubleMatrix<U, ?, ?, ?> values)
    {
        addGridDataSI(dataStream, location.getValuesSI(), time.getValuesSI(), values.getValuesSI());
    }

    /**
     * Removes all data from before the given time. This is useful in live usages of this class, where older data is no longer
     * required.
     * @param time time before which all data can be removed
     */
    public void clearDataBefore(final Duration time)
    {
        clearDataBefore(time.si);
    }

    /**
     * Sets an exponential kernel with infinite range.
     * @param sigma spatial kernel size
     * @param tau temporal kernel size
     */
    public void setKernel(final Length sigma, final Duration tau)
    {
        setKernelSI(sigma.si, tau.si);
    }

    /**
     * Returns an exponential kernel with limited range.
     * @param sigma spatial kernel size in [m]
     * @param tau temporal kernel size in [s]
     * @param xMax maximum spatial range in [m]
     * @param tMax maximum temporal range in [s]
     */
    public void setKernel(final Length sigma, final Duration tau, final Length xMax, final Duration tMax)
    {
        setKernelSI(sigma.si, tau.si, xMax.si, tMax.si);
    }

    /**
     * Sets a Gaussian kernel with infinite range.
     * @param sigma spatial kernel size
     * @param tau temporal kernel size
     */
    public void setGaussKernel(final Length sigma, final Duration tau)
    {
        setGaussKernelSI(sigma.si, tau.si);
    }

    /**
     * Returns a Gaussian kernel with limited range.
     * @param sigma spatial kernel size in [m]
     * @param tau temporal kernel size in [s]
     * @param xMax maximum spatial range in [m]
     * @param tMax maximum temporal range in [s]
     */
    public void setGaussKernel(final Length sigma, final Duration tau, final Length xMax, final Duration tMax)
    {
        setGaussKernelSI(sigma.si, tau.si, xMax.si, tMax.si);
    }

    /**
     * Sets a kernel with limited range and provided shape. The shape allows using non-exponential kernels.
     * @param xMax maximum spatial range
     * @param tMax maximum temporal range
     * @param shape shape of the kernel
     */
    public void setKernel(final Length xMax, final Duration tMax, final KernelShape shape)
    {
        setKernelSI(xMax.si, tMax.si, shape);
    }

    /**
     * Returns filtered data.
     * @param location location of output grid
     * @param time time of output grid
     * @param quantities quantities to calculate filtered data of
     * @return filtered data, empty when interrupted
     */
    public Optional<TypedFilter> filter(final LengthVector location, final DurationVector time,
            final Quantity<?, ?>... quantities)
    {
        Optional<Filter> filter = filterSI(location.getValuesSI(), time.getValuesSI(), quantities);
        if (filter.isEmpty())
        {
            return Optional.empty();
        }
        return Optional.of(new TypedFilter(filter.get()));
    }

    @Override
    public String toString()
    {
        return "TypedEGTF []";
    }

}
