package org.opentrafficsim.core.animation;

import java.util.ArrayList;
import java.util.EventListener;
import java.util.EventObject;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.djunits.unit.FrequencyUnit;
import org.djunits.unit.LengthUnit;
import org.djunits.unit.LinearDensityUnit;
import org.djunits.unit.SpeedUnit;
import org.djunits.unit.TimeUnit;
import org.djunits.value.StorageType;
import org.djunits.value.ValueException;
import org.djunits.value.vdouble.matrix.FrequencyMatrix;
import org.djunits.value.vdouble.matrix.LinearDensityMatrix;
import org.djunits.value.vdouble.matrix.SpeedMatrix;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Frequency;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.LinearDensity;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.djunits.value.vdouble.vector.LengthVector;
import org.djunits.value.vdouble.vector.TimeVector;
import org.opentrafficsim.base.WeightedMeanAndSum;

import nl.tudelft.simulation.language.Throw;

/**
 * Extended Generalized Treiber-Helbing Filter (van Lint & Hoogendoorn, 2009). This is an extension of the Adaptive Smoothing
 * Method (Treiber and Helbing, 2002). To allow flexible usage the EGTF works with {@code DataSource}, {@code Quantity} and
 * {@code DataStream}.
 * <p>
 * A {@code DataSource}, such as "loop detectors", "floating-car data" or "camera" is mostly an identifier, but can be requested
 * to provide several data streams.
 * <p>
 * A {@code DataStream} is one {@code DataSource} supplying one {@code Quantity}. For instance "loop detectors" supplying
 * "flow". In a {@code DataStream}, supplied by the {@code DataSource}, standard deviation of measurements in congestion and
 * free flow are defined. These determine the reliability of the {@code Quantity} data from the given {@code DataSource}, and
 * thus ultimately the weight of the data in the estimation of the quantity.
 * <p>
 * A {@code Quantity}, such as "flow" or "density" defines what is measured and what is requested as output. The output can be
 * in strongly typed format using a {@code Converter}. Default quantities are available under {@code SPEED}, {@code FLOW} and
 * {@code DENSITY}, all under {@code EGTF.Quantity}.
 * <p>
 * Data can be added using several methods for point data, vector data (multiple independent location-time combinations) and
 * grid data (data in a grid pattern). All data is added for a particular {@code DataStream}.
 * <p>
 * Output can be requested from the EGTF using a {@code Kernel}, a spatiotemporal pattern determining measurement weights. The
 * {@code Kernel} defines an optional maximum spatial and temporal range for measurements to consider, and uses a {@code Shape}
 * to determine the weight for a given distance and time from the estimated point. By default this is an exponential function.
 * <p>
 * Parameters from the EGTF are found in the following places:
 * <ul>
 * <li>{@code EGTF}: <i>cCong</i>, <i>cFree</i>, <i>deltaV</i> and <i>vc</i>, defining the overall traffic flow properties.</li>
 * <li>{@code Kernel}: <i>tMax</i> and <i>xMax</i>, defining the maximum range to consider.</li>
 * <li>{@code Shape}: <i>sigma</i> and <i>tau</i>, determining the decay of weight for further measurements in space and
 * time.</li>
 * <li>{@code DataStream}: <i>thetaCong</i> and <i>thetaFree</i>, defining the reliability by the standard deviation of measured
 * data in free flow and congestion from a particular data stream.</li>
 * </ul>
 * References:
 * <ul>
 * <li>van Lint, J. W. C. and Hoogendoorn, S. P. (2009). A robust and efficient method for fusing heterogeneous data from
 * traffic sensors on freeways. Computer Aided Civil and Infrastructure Engineering, accepted for publication.</li>
 * <li>Treiber, M. and Helbing, D. (2002). Reconstructing the spatio-temporal traffic dynamics from stationary detector data.
 * Cooper@tive Tr@nsport@tion Dyn@mics, 1:3.1–3.24.</li>
 * </ul>
 * <p>
 * Copyright (c) 2013-2018 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 3 okt. 2018 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class EGTF
{

    /** Default sigma value. */
    private static final Length DEFAULT_SIGMA = Length.createSI(300.0);

    /** Default tau value. */
    private static final Duration DEFAULT_TAU = Duration.createSI(30.0);

    /** Filter kernel. */
    private Kernel kernel;

    /** Shock wave speed in congestion. */
    private final Speed cCong;

    /** Shock wave speed in free flow. */
    private final Speed cFree;

    /** Speed range between congestion and free flow. */
    private final Speed deltaV;

    /** Flip-over speed below which we have congestion. */
    private final Speed vc;

    /** Data sources by label so we can return the same instances upon repeated request. */
    private final Map<String, DataSource> dataSources = new LinkedHashMap<>();

    /** All the data sorted by space and time, and per data stream. */
    private SortedMap<Double, SortedMap<Double, Map<DataStream<?>, List<Number>>>> data = new TreeMap<>();

    /** Whether the calculation was interrupted. */
    private boolean interrupted = false;

    /** Listeners. */
    private Set<EgtfListener> listeners = new LinkedHashSet<>();

    /**
     * Constructor using cCong = -18km/h, cFree = 80km/h, deltaV = 10km/h and vc = 80km/h. A default kernel is set.
     */
    public EGTF()
    {
        this(new Speed(-18, SpeedUnit.KM_PER_HOUR), new Speed(80, SpeedUnit.KM_PER_HOUR), new Speed(10, SpeedUnit.KM_PER_HOUR),
                new Speed(80, SpeedUnit.KM_PER_HOUR));
    }

    /**
     * Constructor defining global settings. A default kernel is set.
     * @param cCong Speed; shock wave speed in congestion
     * @param cFree Speed; shock wave speed in free flow
     * @param deltaV Speed; speed range between congestion and free flow
     * @param vc Speed; flip-over speed below which we have congestion
     */
    public EGTF(final Speed cCong, final Speed cFree, final Speed deltaV, final Speed vc)
    {
        this.cCong = cCong;
        this.cFree = cFree;
        this.deltaV = deltaV;
        this.vc = vc;
        setKernel();
    }

    /**
     * Convenience constructor that also sets a specified kernel.
     * @param cCong double; shock wave speed in congestion [km/h]
     * @param cFree double; shock wave speed in free flow [km/h]
     * @param deltaV double; speed range between congestion and free flow [km/h]
     * @param vc double; flip-over speed below which we have congestion [km/h]
     * @param tMax double; maximum temporal range in [s]
     * @param xMax double; maximum spatial range in [m]
     * @param sigma double; spatial kernel size in [m]
     * @param tau double; temporal kernel size in [s]
     */
    @SuppressWarnings("parameternumber")
    public EGTF(final double cCong, final double cFree, final double deltaV, final double vc, final double tMax,
            final double xMax, final double sigma, final double tau)
    {
        this(new Speed(cCong, SpeedUnit.KM_PER_HOUR), new Speed(cFree, SpeedUnit.KM_PER_HOUR),
                new Speed(deltaV, SpeedUnit.KM_PER_HOUR), new Speed(vc, SpeedUnit.KM_PER_HOUR));
        setKernel(Duration.createSI(tMax), Length.createSI(xMax), Length.createSI(sigma), Duration.createSI(tau));
    }

    // ********************
    // *** DATA METHODS ***
    // ********************

    /**
     * Return a data source, which is created if necessary.
     * @param name String; unique name for the data source
     * @return DataSource; data source
     */
    @SuppressWarnings("synthetic-access")
    public DataSource getDataSource(final String name)
    {
        return this.dataSources.computeIfAbsent(name, (key) -> new DataSource(key));
    }

    /**
     * Removes all data from before the given time. This is useful in live usages of this class, where older data is no longer
     * required.
     * @param time Time; time before which all data can be removed
     */
    public synchronized void removeDataBefore(final Time time)
    {
        this.data.subMap(Double.NEGATIVE_INFINITY, time.si).clear();
    }

    /**
     * Adds data.
     * @param dataStream DataStream; data stream of the data
     * @param location double; location in [m]
     * @param time double; time in [s]
     * @param value double; data value
     */
    public synchronized void addPointDataSI(final DataStream<?> dataStream, final double location, final double time,
            final double value)
    {
        if (!Double.isNaN(value))
        {
            getStreamData(getSpacioTemporalData(getSpatialData(location), time), dataStream).add(value);
        }
    }

    /**
     * Adds data.
     * @param dataStream DataStream; data stream of the data
     * @param location Length; location
     * @param time Time; time
     * @param value T; data value in SI unit
     * @param <T> implicit data type
     */
    public synchronized <T extends Number> void addPointData(final DataStream<T> dataStream, final Length location,
            final Time time, final T value)
    {
        if (!Double.isNaN(value.doubleValue()))
        {
            getStreamData(getSpacioTemporalData(getSpatialData(location.si), time.si), dataStream).add(value);
        }
    }

    /**
     * Adds data.
     * @param dataStream DataStream; data stream of the data
     * @param location double[]; locations in [m]
     * @param time double[]; times in [s]
     * @param values double[]; data values in SI unit
     */
    public synchronized void addVectorDataSI(final DataStream<?> dataStream, final double[] location, final double[] time,
            final double[] values)
    {
        Throw.when(location.length != time.length || time.length != values.length, IllegalArgumentException.class,
                "Unequal lengths: location %d, time %d, data %d.", location.length, time.length, values.length);
        for (int i = 0; i < values.length; i++)
        {
            if (!Double.isNaN(values[i]))
            {
                getStreamData(getSpacioTemporalData(getSpatialData(location[i]), time[i]), dataStream).add(values[i]);
            }
        }
    }

    /**
     * Adds data.
     * @param dataStream DataStream; data stream of the data
     * @param location LengthVector; locations
     * @param time TimeVector; times
     * @param values double[]; data values
     * @param <T> implicit data type
     */
    public synchronized <T extends Number> void addVectorData(final DataStream<T> dataStream, final LengthVector location,
            final TimeVector time, final T[] values)
    {
        Throw.when(location.size() != time.size() || time.size() != values.length, IllegalArgumentException.class,
                "Unequal lengths: location %d, time %d, data %d.", location.size(), time.size(), values.length);
        for (int i = 0; i < values.length; i++)
        {
            if (!Double.isNaN(values[i].doubleValue()))
            {
                try
                {
                    addPointData(dataStream, location.get(i), time.get(i), values[i]);
                }
                catch (ValueException exception)
                {
                    // should not happen, we loop and check
                    throw new RuntimeException("Unexcepted exception while looping grid data.", exception);
                }
            }
        }
    }

    /**
     * Adds data.
     * @param dataStream DataStream; data stream of the data
     * @param location double[]; locations in [m]
     * @param time double[]; times in [s]
     * @param values T[][]; data values in SI unit
     */
    public synchronized void addGridDataSI(final DataStream<?> dataStream, final double[] location, final double[] time,
            final double[][] values)
    {
        Throw.when(values.length != location.length, IllegalArgumentException.class, "%d locations while length of data is %d",
                location.length, values.length);
        for (int i = 0; i < location.length; i++)
        {
            Throw.when(values[i].length != time.length, IllegalArgumentException.class, "%d times while length of data is %d",
                    time.length, values[i].length);
            SortedMap<Double, Map<DataStream<?>, List<Number>>> spatialData = getSpatialData(location[i]);
            for (int j = 0; j < time.length; j++)
            {
                if (!Double.isNaN(values[i][j]))
                {
                    getStreamData(getSpacioTemporalData(spatialData, time[j]), dataStream).add(values[i][j]);
                }
            }
        }
    }

    /**
     * Adds data.
     * @param dataStream DataStream; data stream of the data
     * @param location LengthVector; locations
     * @param time TimeVector; times
     * @param values T[][]; data values
     * @param <T> implicit data type
     */
    public synchronized <T extends Number> void addGridData(final DataStream<T> dataStream, final LengthVector location,
            final TimeVector time, final T[][] values)
    {
        Throw.when(values.length != location.size(), IllegalArgumentException.class, "%d locations while length of data is %d",
                location.size(), values.length);
        for (int i = 0; i < location.size(); i++)
        {
            Throw.when(values[i].length != time.size(), IllegalArgumentException.class, "%d times while length of data is %d",
                    time.size(), values[i].length);
            try
            {
                SortedMap<Double, Map<DataStream<?>, List<Number>>> spatialData = getSpatialData(location.get(i).si);
                for (int j = 0; j < time.size(); j++)
                {
                    if (!Double.isNaN(values[i][j].doubleValue()))
                    {
                        getStreamData(getSpacioTemporalData(spatialData, time.get(j).si), dataStream).add(values[i][j]);
                    }
                }
            }
            catch (ValueException exception)
            {
                // should not happen, we loop
                throw new RuntimeException("Unexcepted exception while looping grid data.", exception);
            }
        }
    }

    /**
     * Returns data from a specific location as a subset from all data. An empty map is returned if no such data.
     * @param location double; location in [m]
     * @return data from a specific location
     */
    private SortedMap<Double, Map<DataStream<?>, List<Number>>> getSpatialData(final double location)
    {
        return this.data.computeIfAbsent(location, (key) -> new TreeMap<>());
    }

    /**
     * Returns data from a specific time as a subset of data from a specific location. An empty map is returned if no such data.
     * @param spatialData SortedMap; spatially selected data
     * @param time double; time in [s]
     * @return data from a specific time, from data from a specific location
     */
    private Map<DataStream<?>, List<Number>> getSpacioTemporalData(
            final SortedMap<Double, Map<DataStream<?>, List<Number>>> spatialData, final double time)
    {
        return spatialData.computeIfAbsent(time, (key) -> new LinkedHashMap<>());
    }

    /**
     * Returns the data at a specific point. An empty list is returned if no such data.
     * @param spatiotemporalData Map; data from a specific time and specific location
     * @param stream DataStream; data stream to obtain the data of
     * @return data at a specific point, per data stream
     */
    private List<Number> getStreamData(final Map<DataStream<?>, List<Number>> spatiotemporalData, final DataStream<?> stream)
    {
        return spatiotemporalData.computeIfAbsent(stream, (key) -> new ArrayList<>());
    }

    // **********************
    // *** KERNEL METHODS ***
    // **********************

    /**
     * Sets a default exponential kernel with infinite range, sigma = 300m, and tau = 30s.
     */
    public void setKernel()
    {
        setKernel(Duration.POSITIVE_INFINITY, Length.POSITIVE_INFINITY, new ExpShape(DEFAULT_SIGMA, DEFAULT_TAU));
    }

    /**
     * Sets an exponential kernel with limited range, sigma = 300m, and tau = 30s.
     * @param tMax Duration; maximum temporal range
     * @param xMax Length; maximum spatial range
     */
    public void setKernel(final Duration tMax, final Length xMax)
    {
        setKernel(tMax, xMax, new ExpShape(DEFAULT_SIGMA, DEFAULT_TAU));
    }

    /**
     * Sets an exponential kernel with infinite range.
     * @param sigma Length; spatial kernel size
     * @param tau Duration; temporal kernel size
     */
    public void setKernel(final Length sigma, final Duration tau)
    {
        setKernel(Duration.POSITIVE_INFINITY, Length.POSITIVE_INFINITY, new ExpShape(sigma, tau));
    }

    /**
     * Sets an exponential kernel with limited range.
     * @param tMax Duration; maximum temporal range
     * @param xMax Length; maximum spatial range
     * @param sigma Length; spatial kernel size
     * @param tau Duration; temporal kernel size
     */
    public void setKernel(final Duration tMax, final Length xMax, final Length sigma, final Duration tau)
    {
        setKernel(tMax, xMax, new ExpShape(sigma, tau));
    }

    /**
     * Sets a kernel with limited range and provided shape. The shape allows using non-exponential kernels.
     * @param tMax Duration; maximum temporal range
     * @param xMax Length; maximum spatial range
     * @param shape Shape; shape of the kernel
     */
    public synchronized void setKernel(final Duration tMax, final Length xMax, final Shape shape)
    {
        this.kernel = new Kernel(tMax, xMax, shape);
    }

    /**
     * Returns an exponential kernel with limited range.
     * @param tMax double; maximum temporal range in [s]
     * @param xMax double; maximum spatial range in [m]
     * @param sigma double; spatial kernel size in [m]
     * @param tau double; temporal kernel size in [s]
     */
    public void setKernelSI(final double tMax, final double xMax, final double sigma, final double tau)
    {
        setKernel(Duration.createSI(tMax), Length.createSI(xMax), new ExpShape(Length.createSI(sigma), Duration.createSI(tau)));
    }

    // **********************
    // *** FILTER METHODS ***
    // **********************

    /**
     * Returns filtered data.
     * @param location double[]; location of output grid in [m]
     * @param time double[]; time of output grid in [s]
     * @param quantities Quantity...; quantities to calculate filtered data of
     * @return Filter; filtered data, {@code null} when interrupted
     */
    public Filter filterSI(final double[] location, final double[] time, final Quantity<?, ?>... quantities)
    {
        Throw.whenNull(location, "Location may not be null.");
        Throw.whenNull(time, "Time may not be null.");
        try
        {
            return filter(new LengthVector(location, LengthUnit.SI, StorageType.DENSE),
                    new TimeVector(time, TimeUnit.BASE, StorageType.DENSE), quantities);
        }
        catch (ValueException exception)
        {
            throw new RuntimeException("Unexpected exception while create location or time vector.", exception);
        }
    }

    /**
     * Returns filtered data.
     * @param location LengthVector]; location of output grid
     * @param time TimeVector; time of output grid
     * @param quantities Quantity...; quantities to calculate filtered data of
     * @return Filter; filtered data, {@code null} when interrupted
     */
    @SuppressWarnings("synthetic-access")
    public synchronized Filter filter(final LengthVector location, final TimeVector time, final Quantity<?, ?>... quantities)
    {
        this.interrupted = false;
        Throw.whenNull(location, "Location may not be null.");
        Throw.whenNull(time, "Time may not be null.");

        // initialize data
        Map<Quantity<?, ?>, double[][]> map = new LinkedHashMap<>();
        for (Quantity<?, ?> quantity : quantities)
        {
            map.put(quantity, new double[location.size()][time.size()]);
        }

        try
        {
            // loop grid locations
            for (int i = 0; i < location.size(); i++)
            {
                Length xGrid = location.get(i);

                // filter applicable data for location
                SortedMap<Double, SortedMap<Double, Map<DataStream<?>, List<Number>>>> spatialData =
                        this.data.subMap(this.kernel.fromLocation(xGrid).si, this.kernel.toLocation(xGrid).si);

                // loop grid times
                for (int j = 0; j < time.size(); j++)
                {
                    Time tGrid = time.get(j);

                    // notify
                    notifyListeners((i + (double) j / time.size()) / location.size());
                    if (this.interrupted)
                    {
                        return null;
                    }

                    // initialize data per stream
                    Map<DataStream<?>, WeightedMeanAndSum<Double, Double>> zCong = new LinkedHashMap<>(); // quantity z assuming
                                                                                                          // congestion
                    Map<DataStream<?>, WeightedMeanAndSum<Double, Double>> zFree = new LinkedHashMap<>(); // quantity z assuming
                                                                                                          // free flow

                    // loop applicable data for location
                    for (double x : spatialData.keySet())
                    {
                        Length xMeasurement = Length.createSI(x);

                        // filter and loop applicable data for time
                        SortedMap<Double, Map<DataStream<?>, List<Number>>> temporalData =
                                spatialData.get(x).subMap(this.kernel.fromTime(tGrid).si, this.kernel.toTime(tGrid).si);
                        for (double t : temporalData.keySet())
                        {
                            Time tMeasurement = Time.createSI(t);
                            double phiCong = this.kernel.weightCong(tGrid, xGrid, tMeasurement, xMeasurement);
                            double phiFree = this.kernel.weightFree(tGrid, xGrid, tMeasurement, xMeasurement);

                            // get and loop data at point (will often be 1 measurement)
                            Map<DataStream<?>, List<Number>> pointData = temporalData.get(t);
                            for (DataStream<?> stream : pointData.keySet())
                            {
                                for (Number z : pointData.get(stream))
                                {
                                    zCong.computeIfAbsent(stream, (key) -> new WeightedMeanAndSum<>()).add(z.doubleValue(),
                                            phiCong);
                                    zFree.computeIfAbsent(stream, (key) -> new WeightedMeanAndSum<>()).add(z.doubleValue(),
                                            phiFree);
                                }
                            }
                        }

                    }

                    // figure out the congestion level estimated for each data source
                    Map<DataSource, Double> w = new LinkedHashMap<>();
                    for (DataStream<?> dataStream : zCong.keySet())
                    {
                        if (dataStream.getQuantity().isSpeed()) // only one speed quantity allowed per data source
                        {
                            double u = Math.min(zCong.get(dataStream).getMean(), zFree.get(dataStream).getMean());
                            w.put(dataStream.getDataSource(), // 1 speed quantity per source allowed
                                    .5 * (1.0 + Math.tanh((EGTF.this.vc.si - u) / EGTF.this.deltaV.si)));
                            continue;
                        }
                    }

                    // sum available data sources per quantity
                    for (Quantity<?, ?> quantity : quantities)
                    {
                        WeightedMeanAndSum<Double, Double> z = new WeightedMeanAndSum<>();
                        for (DataStream<?> dataStream : zCong.keySet())
                        {
                            if (dataStream.getQuantity().equals(quantity))
                            {
                                // obtain congestion level
                                double wj;
                                if (!w.containsKey(dataStream.getDataSource()))
                                {
                                    wj = 0.0;
                                }
                                else
                                {
                                    wj = w.get(dataStream.getDataSource());
                                }
                                // calculate estimated value z of this data source (no duplicate quantities per source allowed)
                                WeightedMeanAndSum<Double, Double> zCongj = zCong.get(dataStream);
                                WeightedMeanAndSum<Double, Double> zFreej = zFree.get(dataStream);
                                double zj = wj * zCongj.getMean() + (1.0 - wj) * zFreej.getMean();
                                double weight;
                                if (w.size() > 1)
                                {
                                    // data source more important if more and nearer measurements
                                    double beta = wj * zCongj.getWeightSum() + (1.0 - wj) * zFreej.getWeightSum();
                                    // more important if more reliable (smaller standard deviation) at congestion level
                                    double alpha = wj / dataStream.getThetaCong() + (1.0 - wj) / dataStream.getThetaFree();
                                    weight = alpha * beta;
                                }
                                else
                                {
                                    weight = 1.0;
                                }
                                z.add(zj, weight);
                            }
                        }
                        map.get(quantity)[i][j] = z.getMean();
                    }
                }
            }
        }
        catch (ValueException exception)
        {
            // should not happen, we loop
            throw new RuntimeException("Unexpected exception during filtering.", exception);
        }
        notifyListeners(1.0);

        return new Filter(location, time, map);
    }

    // *********************
    // *** EVENT METHODS ***
    // *********************

    /**
     * Interrupt the calculation.
     */
    public final void interrupt()
    {
        this.interrupted = true;
    }

    /**
     * Add listener.
     * @param listener EgtfListener; listener
     */
    public final void addListener(final EgtfListener listener)
    {
        this.listeners.add(listener);
    }

    /**
     * Remove listener.
     * @param listener EgtfListener; listener
     */
    public final void removeListener(final EgtfListener listener)
    {
        this.listeners.remove(listener);
    }

    /**
     * Notify all listeners.
     * @param progress double; progress, a value in the range [0 ... 1]
     */
    private void notifyListeners(final double progress)
    {
        if (!this.listeners.isEmpty())
        {
            EgtfEvent event = new EgtfEvent(progress);
            for (EgtfListener listener : this.listeners)
            {
                listener.notifyProgress(event);
            }
        }
    }

    // **********************
    // *** HELPER CLASSES ***
    // **********************

    /**
     * Kernel with maximum range and shape.
     */
    private class Kernel
    {

        /** Maximum temporal range. */
        private final Duration tMax;

        /** Maximum spatial range. */
        private final Length xMax;

        /** Shape of the kernel. */
        private final Shape shape;

        /**
         * Constructor.
         * @param tMax Duration; maximum temporal range
         * @param xMax Length; maximum spatial range
         * @param shape Shape; shape of the kernel
         */
        Kernel(final Duration tMax, final Length xMax, final Shape shape)
        {
            this.tMax = tMax;
            this.xMax = xMax;
            this.shape = shape;
        }

        /**
         * Returns a weight assuming congestion.
         * @param t Time; time of estimated point
         * @param x Length; location of estimated point
         * @param measurementTime Time; time of measurement
         * @param measurementLocation Length; location of measurement
         * @return double; weight assuming congestion
         */
        @SuppressWarnings("synthetic-access")
        public double weightCong(final Time t, final Length x, final Time measurementTime, final Length measurementLocation)
        {
            return this.shape.weight(EGTF.this.cCong, measurementTime.minus(t), measurementLocation.minus(x));
        }

        /**
         * Returns a weight assuming free flow.
         * @param t Time; time of estimated point
         * @param x Length; location of estimated point
         * @param measurementTime Time; time of measurement
         * @param measurementLocation Length; location of measurement
         * @return double; weight assuming free flow
         */
        @SuppressWarnings("synthetic-access")
        public double weightFree(final Time t, final Length x, final Time measurementTime, final Length measurementLocation)
        {
            return this.shape.weight(EGTF.this.cFree, measurementTime.minus(t), measurementLocation.minus(x));
        }

        /**
         * Returns the from location of the valid data range.
         * @param x Length; location of estimated point
         * @return Length; from location of the valid data range
         */
        protected Length fromLocation(final Length x)
        {
            return x.minus(this.xMax);
        }

        /**
         * Returns the to location of the valid data range.
         * @param x Length; location of estimated point
         * @return Length; to location of the valid data range
         */
        protected Length toLocation(final Length x)
        {
            return x.plus(this.xMax);
        }

        /**
         * Returns the from time of the valid data range.
         * @param t Time; time of estimated point
         * @return Time; from time of the valid data range
         */
        protected Time fromTime(final Time t)
        {
            return t.minus(this.tMax);
        }

        /**
         * Returns the to time of the valid data range.
         * @param t Time; time of estimated point
         * @return Time; to time of the valid data range
         */
        protected Time toTime(final Time t)
        {
            return t.plus(this.tMax);
        }

    }

    /**
     * Shape interface for a kernel.
     */
    public interface Shape
    {
        /**
         * Calculates a weight.
         * @param c Speed; assumed propagation speed
         * @param dt Duration; time between measurement and estimated point
         * @param dx Length; distance between measurement and estimated point
         * @return double; weight
         */
        double weight(Speed c, Duration dt, Length dx);
    }

    /**
     * Exponential implementation of a shape. Used as default when kernels are created.
     */
    private class ExpShape implements Shape
    {
        /** Spatial size of the kernel. */
        private final Length sigma;

        /** Temporal size of the kernel. */
        private final Duration tau;

        /**
         * Constructor.
         * @param sigma Length; spatial size of the kernel
         * @param tau Duration; temporal size of the kernel
         */
        ExpShape(final Length sigma, final Duration tau)
        {
            this.sigma = sigma;
            this.tau = tau;
        }

        /** {@inheritDoc} */
        @Override
        public double weight(final Speed c, final Duration dt, final Length dx)
        {
            return Math.exp(-Math.abs(dx.si) / this.sigma.si - (Math.abs(dt.si - dx.si / c.si)) / this.tau.si);
        }
    }

    /**
     * Data source for the EGTF. These are obtained using {@code EGTF.getDataSource()}.
     */
    public static final class DataSource
    {
        /** Unique name. */
        private final String name;

        /** Data stream of this data source. */
        private final Map<String, DataStream<?>> streams = new LinkedHashMap<>();

        /**
         * Constructor.
         * @param name String; unique name
         */
        private DataSource(final String name)
        {
            this.name = name;
        }

        /**
         * Returns the name.
         * @return String; name
         */
        public String getName()
        {
            return this.name;
        }

        /**
         * Add a non-speed stream for the quantity to this data source.
         * @param quantity Quantity&lt;T, ?&gt;; quantity
         * @param <T> implicit data type
         * @return DataStream; the created data stream
         * @throws IllegalArgumentException if the quantity is speed
         */
        public <T extends Number> DataStream<T> addNonSpeedStream(final Quantity<T, ?> quantity)
        {
            Throw.when(quantity.isSpeed(), IllegalArgumentException.class, "Non-speed stream created for speed quantity.");
            return addStreamSI(quantity, 0.0, 0.0);
        }

        /**
         * Add a speed stream for the quantity to this data source.
         * @param quantity Quantity&lt;T, ?&gt;; quantity
         * @param thetaCong T; standard deviation of this quantity of measurements in congestion by this data source
         * @param thetaFree T; standard deviation of this quantity of measurements in free flow by this data source
         * @param <T> implicit data type
         * @return DataStream; the created data stream
         * @throws IllegalArgumentException if the quantity is not speed
         */
        public <T extends Number> DataStream<T> addSpeedStream(final Quantity<T, ?> quantity, final T thetaCong,
                final T thetaFree)
        {
            Throw.when(!quantity.isSpeed(), IllegalArgumentException.class, "Speed stream created for non-speed quantity.");
            return addStreamSI(quantity, thetaCong.doubleValue(), thetaFree.doubleValue());
        }

        /**
         * Add a stream for the quantity to this data source.
         * @param quantity Quantity&lt;T, ?&gt;; quantity
         * @param thetaCong double; standard deviation of this quantity of measurements in congestion by this data source in SI
         * @param thetaFree double; standard deviation of this quantity of measurements in free flow by this data source in SI
         * @param <T> implicit data type
         * @return DataStream; the created data stream
         */
        @SuppressWarnings("synthetic-access")
        public <T extends Number> DataStream<T> addStreamSI(final Quantity<T, ?> quantity, final double thetaCong,
                final double thetaFree)
        {
            Throw.when(this.streams.containsKey(quantity.getName()), IllegalStateException.class,
                    "Data source %s already has a stream for quantity %s.", this.name, quantity.getName());
            Throw.when(thetaCong < 0.0 || thetaFree < 0.0, IllegalArgumentException.class,
                    "Standard deviation must be positive.");
            DataStream<T> dataStream = new DataStream<>(this, quantity, thetaCong, thetaFree);
            this.streams.put(quantity.getName(), dataStream);
            return dataStream;
        }

        /**
         * Get a stream for the quantity of this data source. If no stream has been created, one will be created with 1.0
         * standard deviation.
         * @param quantity Quantity&lt;T, ?&gt;; quantity
         * @return DataStream&ltlT&gt;; stream for the quantity of this data source
         * @param <T> implicit data type
         */
        @SuppressWarnings({ "unchecked" })
        public <T extends Number> DataStream<T> getStream(final Quantity<T, ?> quantity)
        {
            if (!this.streams.containsKey(quantity.getName()))
            {
                addStreamSI(quantity, 1.0, 1.0);
            }
            return (DataStream<T>) this.streams.get(quantity.getName());
        }

        /** {@inheritDoc} */
        @Override
        public int hashCode()
        {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((this.name == null) ? 0 : this.name.hashCode());
            return result;
        }

        /** {@inheritDoc} */
        @Override
        public boolean equals(final Object obj)
        {
            if (this == obj)
            {
                return true;
            }
            if (obj == null)
            {
                return false;
            }
            if (getClass() != obj.getClass())
            {
                return false;
            }
            DataSource other = (DataSource) obj;
            if (this.name == null)
            {
                if (other.name != null)
                {
                    return false;
                }
            }
            else if (!this.name.equals(other.name))
            {
                return false;
            }
            return true;
        }

        /** {@inheritDoc} */
        @Override
        public String toString()
        {
            return "DataSource " + this.name;
        }

    }

    /**
     * Data stream for the EGTF. These are obtained by {@code DataSource.addStream()} and {@code DataSource.getStream()}.
     * @param <T> data type of the stream
     */
    public static final class DataStream<T extends Number>
    {
        /** Data source. */
        private final DataSource dataSource;

        /** Quantity. */
        private final Quantity<T, ?> quantity;

        /** Standard deviation in congestion. */
        private final double thetaCong;

        /** Standard deviation in free flow. */
        private final double thetaFree;

        /**
         * Constructor.
         * @param dataSource DataSource; data source
         * @param quantity Quantity; quantity
         * @param thetaCong double; standard deviation in congestion
         * @param thetaFree double; standard deviation in free flow
         */
        private DataStream(final DataSource dataSource, final Quantity<T, ?> quantity, final double thetaCong,
                final double thetaFree)
        {
            Throw.whenNull(dataSource, "Data source may not be null.");
            Throw.whenNull(quantity, "Quantity may not be null.");
            Throw.whenNull(thetaCong, "Theta cong may not be null.");
            Throw.whenNull(thetaFree, "Theta free may not be null.");
            this.dataSource = dataSource;
            this.quantity = quantity;
            this.thetaCong = thetaCong;
            this.thetaFree = thetaFree;
        }

        /**
         * Returns the data source.
         * @return DataSource; the data source
         */
        public DataSource getDataSource()
        {
            return this.dataSource;
        }

        /**
         * Returns the quantity.
         * @return Quantity; the quantity
         */
        public Quantity<T, ?> getQuantity()
        {
            return this.quantity;
        }

        /**
         * Returns the standard deviation in congestion.
         * @return double; the standard deviation in congestion
         */
        public double getThetaCong()
        {
            return this.thetaCong;
        }

        /**
         * Returns the standard deviation in free flow.
         * @return double; the standard deviation in free flow
         */
        public double getThetaFree()
        {
            return this.thetaFree;
        }

        /** {@inheritDoc} */
        @Override
        public int hashCode()
        {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((this.dataSource.getName() == null) ? 0 : this.dataSource.getName().hashCode());
            result = prime * result + ((this.quantity == null) ? 0 : this.quantity.hashCode());
            return result;
        }

        /** {@inheritDoc} */
        @Override
        public boolean equals(final Object obj)
        {
            if (this == obj)
            {
                return true;
            }
            if (obj == null)
            {
                return false;
            }
            if (getClass() != obj.getClass())
            {
                return false;
            }
            DataStream<?> other = (DataStream<?>) obj;
            return Objects.equals(this.dataSource.getName(), other.dataSource.getName())
                    && Objects.equals(this.quantity, other.quantity);
        }

        /** {@inheritDoc} */
        @Override
        public String toString()
        {
            return "DataStream (" + this.dataSource.getName() + ", " + this.quantity.getName() + ")";
        }

    }

    /**
     * Quantity.
     * @param <T> data type
     * @param <K> grid output format
     */
    public static class Quantity<T extends Number, K>
    {
        /** Standard quantity for speed. */
        public static final Quantity<Speed, SpeedMatrix> SPEED = new Quantity<>("Speed", true, new Converter<SpeedMatrix>()
        {
            @Override
            public SpeedMatrix convert(final double[][] data)
            {
                try
                {
                    return new SpeedMatrix(data, SpeedUnit.SI, StorageType.DENSE);
                }
                catch (ValueException exception)
                {
                    // should not happen
                    throw new RuntimeException("Unexcepted exception: data is null when converting.", exception);
                }
            }
        });

        /** Standard quantity for flow. */
        public static final Quantity<Frequency, FrequencyMatrix> FLOW =
                new Quantity<>("Flow", false, new Converter<FrequencyMatrix>()
                {
                    @Override
                    public FrequencyMatrix convert(final double[][] data)
                    {
                        try
                        {
                            return new FrequencyMatrix(data, FrequencyUnit.SI, StorageType.DENSE);
                        }
                        catch (ValueException exception)
                        {
                            // should not happen
                            throw new RuntimeException("Unexcepted exception: data is null when converting.", exception);
                        }
                    }
                });

        /** Standard quantity for density. */
        public static final Quantity<LinearDensity, LinearDensityMatrix> DENSITY =
                new Quantity<>("Density", false, new Converter<LinearDensityMatrix>()
                {
                    @Override
                    public LinearDensityMatrix convert(final double[][] data)
                    {
                        try
                        {
                            return new LinearDensityMatrix(data, LinearDensityUnit.SI, StorageType.DENSE);
                        }
                        catch (ValueException exception)
                        {
                            // should not happen
                            throw new RuntimeException("Unexcepted exception: data is null when converting.", exception);
                        }
                    }
                });

        /** Name. */
        private final String name;

        /** Whether this quantity is speed. */
        private final boolean speed;

        /** Converter for output format. */
        private final Converter<K> converter;

        /**
         * Constructor.
         * @param name String; name
         * @param converter Converter&lt;K&gt;; converter for output format
         */
        public Quantity(final String name, final Converter<K> converter)
        {
            this(name, false, converter);
        }

        /**
         * Constructor. Private so only the default SPEED quantity is speed.
         * @param name String; name
         * @param speed boolean; whether this quantity is speed
         * @param converter Converter&lt;K&gt;; converter for output format
         */
        private Quantity(final String name, final boolean speed, final Converter<K> converter)
        {
            this.name = name;
            this.speed = speed;
            this.converter = converter;
        }

        /**
         * Returns a quantity with {@code double[][]} containing SI values as output format.
         * @param name String; name
         * @return quantity with {@code double[][]} containing SI values as output format
         */
        public static Quantity<?, double[][]> si(final String name)
        {
            return new SI<>(name);
        }

        /**
         * Returns the name.
         * @return String; name
         */
        public final String getName()
        {
            return this.name;
        }

        /**
         * Returns whether this quantity is speed.
         * @return boolean; whether this quantity is speed
         */
        public final boolean isSpeed()
        {
            return this.speed;
        }

        /**
         * Converts the filtered data to an output format.
         * @param data double[][]; filtered data
         * @return K; output data
         */
        public K convert(final double[][] data)
        {
            return this.converter.convert(data);
        }

        /** {@inheritDoc} */
        @Override
        public int hashCode()
        {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((this.name == null) ? 0 : this.name.hashCode());
            return result;
        }

        /** {@inheritDoc} */
        @Override
        public boolean equals(final Object obj)
        {
            if (this == obj)
            {
                return true;
            }
            if (obj == null)
            {
                return false;
            }
            if (getClass() != obj.getClass())
            {
                return false;
            }
            Quantity<?, ?> other = (Quantity<?, ?>) obj;
            if (this.name == null)
            {
                if (other.name != null)
                {
                    return false;
                }
            }
            else if (!this.name.equals(other.name))
            {
                return false;
            }
            return true;
        }

        /**
         * Class to return in {@code double[][]} output format.
         * @param <T> data type
         */
        private static class SI<T extends Number> extends Quantity<T, double[][]>
        {
            /**
             * Constructor.
             * @param name String name
             */
            SI(final String name)
            {
                super(name, Converter.SI);
            }
        }

    }

    /**
     * Converter for use in {@code Quantity} to convert internal filtered data to an output type.
     * @param <K> grid output format
     */
    public interface Converter<K>
    {
        /** Standard converter that returns the internal SI data directly. */
        Converter<double[][]> SI = new Converter<double[][]>()
        {
            /** {@inheritDoc} */
            @Override
            public double[][] convert(final double[][] filteredData)
            {
                return filteredData;
            }
        };

        /**
         * Convert the filtered data to an output format.
         * @param filteredData double[][]; filtered data
         * @return K; data in output format
         */
        K convert(double[][] filteredData);
    }

    /**
     * Class containing processed output data.
     */
    public final class Filter
    {

        /** Grid locations of output data. */
        private final LengthVector location;

        /** Grid times of output data. */
        private final TimeVector time;

        /** Map of all filtered data. */
        private final Map<Quantity<?, ?>, double[][]> map;

        /**
         * Constructor.
         * @param location LengthVector; grid locations of output data
         * @param time TimeVector; grid times of output data
         * @param map Map; filtered data
         */
        private Filter(final LengthVector location, final TimeVector time, final Map<Quantity<?, ?>, double[][]> map)
        {
            this.location = location;
            this.time = time;
            this.map = map;
        }

        /**
         * Returns the grid location.
         * @return LengthVector; grid location
         */
        public LengthVector getLocation()
        {
            return this.location;
        }

        /**
         * Returns the grid time.
         * @return TimeVector; grid time
         */
        public TimeVector getTime()
        {
            return this.time;
        }

        /**
         * Returns filtered data as SI values.
         * @param quantity Quantity; quantity
         * @return double[][]; filtered data as SI values
         */
        public double[][] getSI(final Quantity<?, ?> quantity)
        {
            return this.map.get(quantity);
        }

        /**
         * Returns the filtered data in output format.
         * @param quantity Quantity; quantity.
         * @return K; filtered data in output format
         * @param <K> output format.
         */
        public <K> K get(final Quantity<?, K> quantity)
        {
            Throw.when(!this.map.containsKey(quantity), IllegalStateException.class,
                    "Filter does not contain data for quantity %s", quantity.getName());
            return quantity.convert(this.map.get(quantity));
        }

    }

    /**
     * Interface for EGTF listeners.
     * <p>
     * Copyright (c) 2013-2018 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
     * <p>
     * @version $Revision$, $LastChangedDate$, by $Author$, initial version 8 okt. 2018 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
     */
    public interface EgtfListener extends EventListener
    {
        /**
         * Notifies progress.
         * @param event EgtfEvent; event
         */
        void notifyProgress(EgtfEvent event);
    }

    /**
     * EGTF event with progress and the ability to interrupt calculations.
     * <p>
     * Copyright (c) 2013-2018 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
     * <p>
     * @version $Revision$, $LastChangedDate$, by $Author$, initial version 8 okt. 2018 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
     */
    public class EgtfEvent extends EventObject
    {

        /** */
        private static final long serialVersionUID = 20181008L;

        /** Progress, a value in the range [0 ... 1]. */
        private final double progress;

        /**
         * Constructor.
         * @param progress double; progress, a value in the range [0 ... 1]
         */
        public EgtfEvent(final double progress)
        {
            super(EGTF.this);
            this.progress = progress;
        }

        /**
         * Returns the progress, a value in the range [0 ... 1].
         * @return double; progress, a value in the range [0 ... 1]
         */
        public final double getProgress()
        {
            return this.progress;
        }

        /**
         * Interrupts the filter. If a {@code filter()} calculation is ongoing, it will stop and return {@code null}.
         */
        public final void interrupt()
        {
            EGTF.this.interrupt();
        }

    }
}
