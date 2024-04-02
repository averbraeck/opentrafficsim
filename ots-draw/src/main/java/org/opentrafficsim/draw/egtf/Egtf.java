package org.opentrafficsim.draw.egtf;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.IntStream;

/**
 * Extended Generalized Treiber-Helbing Filter (van Lint and Hoogendoorn, 2009). This is an extension of the Adaptive Smoothing
 * Method (Treiber and Helbing, 2002). A fast filter for equidistant grids (Schreiter et al., 2010) is available. This fast
 * implementation also supports multiple data sources.
 * <p>
 * To allow flexible usage the EGTF works with {@code DataSource}, {@code DataStream} and {@code Quantity}.
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
 * in typed format using a {@code Converter}. Default quantities are available under {@code SPEED_SI}, {@code FLOW_SI} and
 * {@code DENSITY_SI}, all under {@code Quantity}.
 * <p>
 * Data can be added using several methods for point data, vector data (multiple independent location-time combinations) and
 * grid data (data in a grid pattern). Data is added for a particular {@code DataStream}.
 * <p>
 * For simple use-cases where a single data source is used, data can be added directly with a {@code Quantity}, in which case a
 * default {@code DataSource}, and default {@code DataStream} for each {@code Quantity} is internally used. All data should
 * either be added using {@code Quantity}'s, or it should all be added using {@code DataSource}'s. Otherwise relative data
 * reliability is undefined.
 * <p>
 * Output can be requested from the EGTF using a {@code Kernel}, a spatiotemporal pattern determining measurement weights. The
 * {@code Kernel} defines an optional maximum spatial and temporal range for measurements to consider, and uses a {@code Shape}
 * to determine the weight for a given distance and time from the estimated point. By default this is an exponential function. A
 * Gaussian kernel is also available, while any other shape could be also be implemented.
 * <p>
 * Parameters from the EGTF are found in the following places:
 * <ul>
 * <li>{@code EGTF}: <i>cCong</i>, <i>cFree</i>, <i>deltaV</i> and <i>vc</i>, defining the overall traffic flow properties.</li>
 * <li>{@code Kernel}: <i>tMax</i> and <i>xMax</i>, defining the maximum range to consider.</li>
 * <li>{@code KernelShape}: <i>sigma</i> and <i>tau</i>, determining the decay of weights for further measurements in space and
 * time. (Specifically {@code GaussKernelShape})</li>
 * <li>{@code DataStream}: <i>thetaCong</i> and <i>thetaFree</i>, defining the reliability by the standard deviation of measured
 * data in free flow and congestion from a particular data stream.</li>
 * </ul>
 * References:
 * <ul>
 * <li>van Lint, J. W. C. and Hoogendoorn, S. P. (2009). A robust and efficient method for fusing heterogeneous data from
 * traffic sensors on freeways. Computer Aided Civil and Infrastructure Engineering, accepted for publication.</li>
 * <li>Schreiter, T., van Lint, J. W. C., Treiber, M. and Hoogendoorn, S. P. (2010). Two fast implementations of the Adaptive
 * Smoothing Method used in highway traffic state estimation. 13th International IEEE Conference on Intelligent Transportation
 * Systems, 19-22 Sept. 2010, Funchal, Portugal.</li>
 * <li>Treiber, M. and Helbing, D. (2002). Reconstructing the spatio-temporal traffic dynamics from stationary detector data.
 * Cooper@tive Tr@nsport@tion Dyn@mics, 1:3.1–3.24.</li>
 * </ul>
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class Egtf
{

    /** Default sigma value. */
    private static final double DEFAULT_SIGMA = 300.0;

    /** Default tau value. */
    private static final double DEFAULT_TAU = 30.0;

    /** Filter kernel. */
    private Kernel kernel;

    /** Shock wave speed in congestion. */
    private final double cCong;

    /** Shock wave speed in free flow. */
    private final double cFree;

    /** Speed range between congestion and free flow. */
    private final double deltaV;

    /** Flip-over speed below which we have congestion. */
    private final double vc;

    /** Data sources by label so we can return the same instances upon repeated request. */
    private final Map<String, DataSource> dataSources = new LinkedHashMap<>();

    /** Default data source for cases where a single data source is used. */
    private DataSource defaultDataSource = null;

    /** Default data streams for cases where a single data source is used. */
    private Map<Quantity<?, ?>, DataStream<?>> defaultDataStreams = null;

    /** True if data is currently being added using a quantity, in which case a check should not occur. */
    private boolean addingByQuantity;

    /** All point data sorted by space and time, and per data stream. */
    private NavigableMap<Double, NavigableMap<Double, Map<DataStream<?>, Double>>> data = new TreeMap<>();

    /** Whether the calculation was interrupted. */
    private boolean interrupted = false;

    /** Listeners. */
    private Set<EgtfListener> listeners = new LinkedHashSet<>();

    /**
     * Constructor using cCong = -18km/h, cFree = 80km/h, deltaV = 10km/h and vc = 80km/h. A default kernel is set.
     */
    public Egtf()
    {
        this(-18.0, 80.0, 10.0, 80.0);
    }

    /**
     * Constructor defining global settings. A default kernel is set.
     * @param cCong double; shock wave speed in congestion [km/h]
     * @param cFree double; shock wave speed in free flow [km/h]
     * @param deltaV double; speed range between congestion and free flow [km/h]
     * @param vc double; flip-over speed below which we have congestion [km/h]
     */
    public Egtf(final double cCong, final double cFree, final double deltaV, final double vc)
    {
        this.cCong = cCong / 3.6;
        this.cFree = cFree / 3.6;
        this.deltaV = deltaV / 3.6;
        this.vc = vc / 3.6;
        setKernel();
    }

    /**
     * Convenience constructor that also sets a specified kernel.
     * @param cCong double; shock wave speed in congestion [km/h]
     * @param cFree double; shock wave speed in free flow [km/h]
     * @param deltaV double; speed range between congestion and free flow [km/h]
     * @param vc double; flip-over speed below which we have congestion [km/h]
     * @param sigma double; spatial kernel size in [m]
     * @param tau double; temporal kernel size in [s]
     * @param xMax double; maximum spatial range in [m]
     * @param tMax double; maximum temporal range in [s]
     */
    @SuppressWarnings("parameternumber")
    public Egtf(final double cCong, final double cFree, final double deltaV, final double vc, final double sigma,
            final double tau, final double xMax, final double tMax)
    {
        this(cCong, cFree, deltaV, vc);
        setKernelSI(sigma, tau, xMax, tMax);
    }

    // ********************
    // *** DATA METHODS ***
    // ********************

    /**
     * Return a data source, which is created if necessary.
     * @param name String; unique name for the data source
     * @return DataSource; data source
     * @throws IllegalStateException when data has been added without a data source
     */
    public DataSource getDataSource(final String name)
    {
        if (this.defaultDataSource != null)
        {
            throw new IllegalStateException(
                    "Obtaining a (new) data source after data has been added without a data source is not allowed.");
        }
        return this.dataSources.computeIfAbsent(name, (
                key
        ) -> new DataSource(key));
    }

    /**
     * Removes all data from before the given time. This is useful in live usages of this class, where older data is no longer
     * required.
     * @param time double; time before which all data can be removed
     */
    public synchronized void clearDataBefore(final double time)
    {
        for (SortedMap<Double, Map<DataStream<?>, Double>> map : this.data.values())
        {
            map.subMap(Double.NEGATIVE_INFINITY, time).clear();
        }
    }

    /**
     * Adds point data.
     * @param quantity Quantity&lt;?, ?&gt;; quantity of the data
     * @param location double; location in [m]
     * @param time double; time in [s]
     * @param value double; data value
     * @throws IllegalStateException if data was added with a data stream previously
     */
    public synchronized void addPointDataSI(final Quantity<?, ?> quantity, final double location, final double time,
            final double value)
    {
        this.addingByQuantity = true;
        addPointDataSI(getDefaultDataStream(quantity), location, time, value);
        this.addingByQuantity = false;
    }

    /**
     * Adds point data.
     * @param dataStream DataStream&lt;?&gt;; data stream of the data
     * @param location double; location in [m]
     * @param time double; time in [s]
     * @param value double; data value
     * @throws IllegalStateException if data was added with a quantity previously
     */
    public synchronized void addPointDataSI(final DataStream<?> dataStream, final double location, final double time,
            final double value)
    {
        checkNoQuantityData();
        Objects.requireNonNull(dataStream, "Datastream may not be null.");
        if (!Double.isNaN(value))
        {
            getSpacioTemporalData(getSpatialData(location), time).put(dataStream, value);
        }
    }

    /**
     * Adds vector data.
     * @param quantity Quantity&lt;?, ?&gt;; quantity of the data
     * @param location double[]; locations in [m]
     * @param time double[]; times in [s]
     * @param values double[]; data values in SI unit
     * @throws IllegalStateException if data was added with a data stream previously
     */
    public synchronized void addVectorDataSI(final Quantity<?, ?> quantity, final double[] location, final double[] time,
            final double[] values)
    {
        this.addingByQuantity = true;
        addVectorDataSI(getDefaultDataStream(quantity), location, time, values);
        this.addingByQuantity = false;
    }

    /**
     * Adds vector data.
     * @param dataStream DataStream&lt;?&gt;; data stream of the data
     * @param location double[]; locations in [m]
     * @param time double[]; times in [s]
     * @param values double[]; data values in SI unit
     * @throws IllegalStateException if data was added with a quantity previously
     */
    public synchronized void addVectorDataSI(final DataStream<?> dataStream, final double[] location, final double[] time,
            final double[] values)
    {
        checkNoQuantityData();
        Objects.requireNonNull(dataStream, "Datastream may not be null.");
        Objects.requireNonNull(location, "Location may not be null.");
        Objects.requireNonNull(time, "Time may not be null.");
        Objects.requireNonNull(values, "Values may not be null.");
        if (location.length != time.length || time.length != values.length)
        {
            throw new IllegalArgumentException(String.format("Unequal lengths: location %d, time %d, data %d.", location.length,
                    time.length, values.length));
        }
        for (int i = 0; i < values.length; i++)
        {
            if (!Double.isNaN(values[i]))
            {
                getSpacioTemporalData(getSpatialData(location[i]), time[i]).put(dataStream, values[i]);
            }
        }
    }

    /**
     * Adds grid data.
     * @param quantity Quantity&lt;?, ?&gt;; quantity of the data
     * @param location double[]; locations in [m]
     * @param time double[]; times in [s]
     * @param values double[][]; data values in SI unit
     * @throws IllegalStateException if data was added with a data stream previously
     */
    public synchronized void addGridDataSI(final Quantity<?, ?> quantity, final double[] location, final double[] time,
            final double[][] values)
    {
        this.addingByQuantity = true;
        addGridDataSI(getDefaultDataStream(quantity), location, time, values);
        this.addingByQuantity = false;
    }

    /**
     * Adds grid data.
     * @param dataStream DataStream&lt;?&gt;; data stream of the data
     * @param location double[]; locations in [m]
     * @param time double[]; times in [s]
     * @param values double[][]; data values in SI unit
     * @throws IllegalStateException if data was added with a quantity previously
     */
    public synchronized void addGridDataSI(final DataStream<?> dataStream, final double[] location, final double[] time,
            final double[][] values)
    {
        checkNoQuantityData();
        Objects.requireNonNull(dataStream, "Datastream may not be null.");
        Objects.requireNonNull(location, "Location may not be null.");
        Objects.requireNonNull(time, "Time may not be null.");
        Objects.requireNonNull(values, "Values may not be null.");
        if (values.length != location.length)
        {
            throw new IllegalArgumentException(
                    String.format("%d locations while length of data is %d", location.length, values.length));
        }
        for (int i = 0; i < location.length; i++)
        {
            if (values[i].length != time.length)
            {
                throw new IllegalArgumentException(
                        String.format("%d times while length of data is %d", time.length, values[i].length));
            }
            Map<Double, Map<DataStream<?>, Double>> spatialData = getSpatialData(location[i]);
            for (int j = 0; j < time.length; j++)
            {
                if (!Double.isNaN(values[i][j]))
                {
                    getSpacioTemporalData(spatialData, time[j]).put(dataStream, values[i][j]);
                }
            }
        }
    }

    /**
     * Check that no data was added using a quantity.
     * @throws IllegalStateException if data was added with a quantity previously
     */
    private void checkNoQuantityData()
    {
        if (!this.addingByQuantity && this.defaultDataSource != null)
        {
            throw new IllegalStateException(
                    "Adding data with a data stream is not allowed after data has been added with a quantity.");
        }
    }

    /**
     * Returns a default data stream and checks that no data with a data stream was added.
     * @param quantity Quantity&lt;?, ?&gt;; quantity
     * @return DataStream&lt;?&gt;; default data stream
     * @throws IllegalStateException if data was added with a data stream previously
     */
    private DataStream<?> getDefaultDataStream(final Quantity<?, ?> quantity)
    {
        Objects.requireNonNull(quantity, "Quantity may not be null.");
        if (!this.dataSources.isEmpty())
        {
            throw new IllegalStateException(
                    "Adding data with a quantity is not allowed after data has been added with a data stream.");
        }
        if (this.defaultDataSource == null)
        {
            this.defaultDataSource = new DataSource("default");
            this.defaultDataStreams = new LinkedHashMap<>();
        }
        return this.defaultDataStreams.computeIfAbsent(quantity, (
                key
        ) -> this.defaultDataSource.addStreamSI(quantity, 1.0, 1.0));
    }

    /**
     * Returns data from a specific location as a subset from all data. An empty map is returned if no such data exists.
     * @param location double; location in [m]
     * @return data from a specific location
     */
    private SortedMap<Double, Map<DataStream<?>, Double>> getSpatialData(final double location)
    {
        return this.data.computeIfAbsent(location, (
                key
        ) -> new TreeMap<>());
    }

    /**
     * Returns data from a specific time as a subset of data from a specific location. An empty map is returned if no such data
     * exists.
     * @param spatialData Map&lt;Double, Map&lt;DataStream&lt;?&gt;, Double&gt;&gt;; spatially selected data
     * @param time double; time in [s]
     * @return data from a specific time, from data from a specific location
     */
    private Map<DataStream<?>, Double> getSpacioTemporalData(final Map<Double, Map<DataStream<?>, Double>> spatialData,
            final double time)
    {
        return spatialData.computeIfAbsent(time, (
                key
        ) -> new LinkedHashMap<>());
    }

    // **********************
    // *** KERNEL METHODS ***
    // **********************

    /**
     * Sets a default exponential kernel with infinite range, sigma = 300m, and tau = 30s.
     */
    public void setKernel()
    {
        setKernelSI(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, new ExpKernelShape(DEFAULT_SIGMA, DEFAULT_TAU));
    }

    /**
     * Sets an exponential kernel with infinite range.
     * @param sigma double; spatial kernel size
     * @param tau double; temporal kernel size
     */
    public void setKernelSI(final double sigma, final double tau)
    {
        setKernelSI(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, sigma, tau);
    }

    /**
     * Sets an exponential kernel with limited range.
     * @param sigma double; spatial kernel size in [m]
     * @param tau double; temporal kernel size in [s]
     * @param xMax double; maximum spatial range in [m]
     * @param tMax double; maximum temporal range in [s]
     */
    public void setKernelSI(final double sigma, final double tau, final double xMax, final double tMax)
    {
        setKernelSI(xMax, tMax, new ExpKernelShape(sigma, tau));
    }

    /**
     * Sets a Gaussian kernel with infinite range.
     * @param sigma double; spatial kernel size
     * @param tau double; temporal kernel size
     */
    public void setGaussKernelSI(final double sigma, final double tau)
    {
        setGaussKernelSI(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, sigma, tau);
    }

    /**
     * Sets a Gaussian kernel with limited range.
     * @param sigma double; spatial kernel size in [m]
     * @param tau double; temporal kernel size in [s]
     * @param xMax double; maximum spatial range in [m]
     * @param tMax double; maximum temporal range in [s]
     */
    public void setGaussKernelSI(final double sigma, final double tau, final double xMax, final double tMax)
    {
        setKernelSI(xMax, tMax, new GaussKernelShape(sigma, tau));
    }

    /**
     * Sets a kernel with limited range and provided shape. The shape allows using non-exponential kernels.
     * @param xMax double; maximum spatial range
     * @param tMax double; maximum temporal range
     * @param shape KernelShape; shape of the kernel
     */
    public synchronized void setKernelSI(final double xMax, final double tMax, final KernelShape shape)
    {
        this.kernel = new Kernel(xMax, tMax, shape);
    }

    /**
     * Returns the wave speed in congestion.
     * @return double; wave speed in congestion
     */
    final double getWaveSpeedCongestion()
    {
        return this.cCong;
    }

    /**
     * Returns the wave speed in free flow.
     * @return double; wave speed in free flow
     */
    final double getWaveSpeedFreeFlow()
    {
        return this.cFree;
    }

    // **********************
    // *** FILTER METHODS ***
    // **********************

    /**
     * Executes filtering in parallel. The returned listener can be used to report progress and wait until the filtering is
     * done. Finally, the filtering results can then be obtained from the listener.
     * @param location double[]; location of output grid in [m]
     * @param time double[]; time of output grid in [s]
     * @param quantities Quantity&lt;?, ?&gt;...; quantities to calculate filtered data of
     * @return EgtfParallelListener; listener to notify keep track of the progress
     */
    public EgtfParallelListener filterParallelSI(final double[] location, final double[] time,
            final Quantity<?, ?>... quantities)
    {
        Objects.requireNonNull(location, "Location may not be null.");
        Objects.requireNonNull(time, "Time may not be null.");
        EgtfParallelListener listener = new EgtfParallelListener();
        addListener(listener);
        new Thread(new Runnable()
        {
            /** {@inheritDoc} */
            @Override
            public void run()
            {
                listener.setFilter(filterSI(location, time, quantities));
                removeListener(listener);
            }
        }, "Egtf calculation thread").start();
        return listener;
    }

    /**
     * Executes fast filtering in parallel. The returned listener can be used to report progress and wait until the filtering is
     * done. Finally, the filtering results can then be obtained from the listener.
     * @param xMin double; minimum location value of output grid [m]
     * @param xStep double; location step of output grid [m]
     * @param xMax double; maximum location value of output grid [m]
     * @param tMin double; minimum time value of output grid [s]
     * @param tStep double; time step of output grid [s]
     * @param tMax double; maximum time value of output grid [s]
     * @param quantities Quantity&lt;?, ?&gt;...; quantities to calculate filtered data of
     * @return EgtfParallelListener; listener to notify keep track of the progress
     */
    public EgtfParallelListener filterParallelFastSI(final double xMin, final double xStep, final double xMax,
            final double tMin, final double tStep, final double tMax, final Quantity<?, ?>... quantities)
    {
        EgtfParallelListener listener = new EgtfParallelListener();
        addListener(listener);
        new Thread(new Runnable()
        {
            /** {@inheritDoc} */
            @Override
            public void run()
            {
                listener.setFilter(filterFastSI(xMin, xStep, xMax, tMin, tStep, tMax, quantities));
                removeListener(listener);
            }
        }, "Egtf calculation thread").start();
        return listener;
    }

    /**
     * Returns filtered data. This is the standard EGTF implementation.
     * @param location double[]; location of output grid in [m]
     * @param time double[]; time of output grid in [s]
     * @param quantities Quantity&lt;?, ?&gt;...; quantities to calculate filtered data of
     * @return Filter; filtered data, {@code null} when interrupted
     */
    @SuppressWarnings({"synthetic-access", "methodlength"})
    public Filter filterSI(final double[] location, final double[] time, final Quantity<?, ?>... quantities)
    {
        Objects.requireNonNull(location, "Location may not be null.");
        Objects.requireNonNull(time, "Time may not be null.");

        // initialize data
        Map<Quantity<?, ?>, double[][]> map = new LinkedHashMap<>();
        for (Quantity<?, ?> quantity : quantities)
        {
            map.put(quantity, new double[location.length][time.length]);
        }

        // loop grid locations
        for (int i = 0; i < location.length; i++)
        {
            double xGrid = location[i];

            // filter applicable data for location
            Map<Double, NavigableMap<Double, Map<DataStream<?>, Double>>> spatialData =
                    this.data.subMap(this.kernel.fromLocation(xGrid), true, this.kernel.toLocation(xGrid), true);

            // loop grid times
            for (int j = 0; j < time.length; j++)
            {
                double tGrid = time[j];

                // notify
                if (notifyListeners((i + (double) j / time.length) / location.length))
                {
                    return null;
                }

                // initialize data per stream
                // quantity z assuming congestion and free flow
                Map<DataStream<?>, DualWeightedMean> zCongFree = new LinkedHashMap<>();

                // filter and loop applicable data for time
                for (Map.Entry<Double, NavigableMap<Double, Map<DataStream<?>, Double>>> xEntry : spatialData.entrySet())
                {
                    double dx = xEntry.getKey() - xGrid;
                    Map<Double, Map<DataStream<?>, Double>> temporalData =
                            xEntry.getValue().subMap(this.kernel.fromTime(tGrid), true, this.kernel.toTime(tGrid), true);

                    for (Map.Entry<Double, Map<DataStream<?>, Double>> tEntry : temporalData.entrySet())
                    {
                        double dt = tEntry.getKey() - tGrid;
                        Map<DataStream<?>, Double> pData = tEntry.getValue();

                        double phiCong = this.kernel.weight(this.cCong, dx, dt);
                        double phiFree = this.kernel.weight(this.cFree, dx, dt);

                        // loop streams data at point
                        for (Map.Entry<DataStream<?>, Double> vEntry : pData.entrySet())
                        {
                            DataStream<?> stream = vEntry.getKey();
                            if (map.containsKey(stream.getQuantity()) || stream.getQuantity().isSpeed())
                            {
                                double v = vEntry.getValue();
                                DualWeightedMean zCongFreeOfStream = zCongFree.computeIfAbsent(stream, (
                                        key
                                ) -> new DualWeightedMean());
                                zCongFreeOfStream.addCong(v, phiCong);
                                zCongFreeOfStream.addFree(v, phiFree);
                            }
                        }
                    }
                }

                // figure out the congestion level estimated for each data source
                Map<DataSource, Double> w = new LinkedHashMap<>();
                for (Map.Entry<DataStream<?>, DualWeightedMean> streamEntry : zCongFree.entrySet())
                {
                    DataStream<?> dataStream = streamEntry.getKey();
                    if (dataStream.getQuantity().isSpeed()) // only one speed quantity allowed per data source
                    {
                        DualWeightedMean zCongFreeOfStream = streamEntry.getValue();
                        double u = Math.min(zCongFreeOfStream.getCong(), zCongFreeOfStream.getFree());
                        w.put(dataStream.getDataSource(), // 1 speed quantity per source allowed
                                .5 * (1.0 + Math.tanh((Egtf.this.vc - u) / Egtf.this.deltaV)));
                        continue;
                    }
                }

                // sum available data sources per quantity
                Double wMean = null;
                for (Map.Entry<Quantity<?, ?>, double[][]> qEntry : map.entrySet())
                {
                    Quantity<?, ?> quantity = qEntry.getKey();
                    WeightedMean z = new WeightedMean();
                    for (Map.Entry<DataStream<?>, DualWeightedMean> zEntry : zCongFree.entrySet())
                    {
                        DataStream<?> dataStream = zEntry.getKey();
                        if (dataStream.getQuantity().equals(quantity))
                        {
                            // obtain congestion level
                            double wCong;
                            if (!w.containsKey(dataStream.getDataSource()))
                            {
                                // this data source has no speed data, but congestion level can be estimated from other sources
                                if (wMean == null)
                                {
                                    // let's see if speed was estimated already
                                    for (Quantity<?, ?> prevQuant : quantities)
                                    {
                                        if (prevQuant.equals(quantity))
                                        {
                                            // it was not, get mean of other data source
                                            wMean = 0.0;
                                            for (double ww : w.values())
                                            {
                                                wMean += ww / w.size();
                                            }
                                            break;
                                        }
                                        else if (prevQuant.isSpeed())
                                        {
                                            wMean = .5 * (1.0
                                                    + Math.tanh((Egtf.this.vc - map.get(prevQuant)[i][j]) / Egtf.this.deltaV));
                                            break;
                                        }
                                    }
                                }
                                wCong = wMean;
                            }
                            else
                            {
                                wCong = w.get(dataStream.getDataSource());
                            }
                            // calculate estimated value z of this data source (no duplicate quantities per source allowed)
                            double wfree = 1.0 - wCong;
                            DualWeightedMean zCongFreej = zEntry.getValue();
                            double zStream = wCong * zCongFreej.getCong() + wfree * zCongFreej.getFree();
                            double weight;
                            if (w.size() > 1)
                            {
                                // data source more important if more and nearer measurements
                                double beta = wCong * zCongFreej.getDenominatorCong() + wfree * zCongFreej.getDenominatorFree();
                                // more important if more reliable (smaller standard deviation) at congestion level
                                double alpha = wCong / dataStream.getThetaCong() + wfree / dataStream.getThetaFree();
                                weight = alpha * beta;
                            }
                            else
                            {
                                weight = 1.0;
                            }
                            z.add(zStream, weight);
                        }
                    }
                    qEntry.getValue()[i][j] = z.get();
                }
            }
        }
        notifyListeners(1.0);

        return new FilterDouble(location, time, map);
    }

    /**
     * Returns filtered data that is processed using fast fourier transformation. This is much faster than the standard filter,
     * at the cost that all input data is discretized to the output grid. The gain in computation speed is however such that
     * finer output grids can be used to alleviate this. For discretization the output grid needs to be equidistant. It is
     * recommended to set a Kernel with maximum bounds before using this method.
     * <p>
     * More than being a fast implementation of the Adaptive Smoothing Method, this implementation includes all data source like
     * the Extended Generalized Treiber-Helbing Filter.
     * @param xMin double; minimum location value of output grid [m]
     * @param xStep double; location step of output grid [m]
     * @param xMax double; maximum location value of output grid [m]
     * @param tMin double; minimum time value of output grid [s]
     * @param tStep double; time step of output grid [s]
     * @param tMax double; maximum time value of output grid [s]
     * @param quantities Quantity&lt;?, ?&gt;...; quantities to calculate filtered data of
     * @return Filter; filtered data, {@code null} when interrupted
     */
    @SuppressWarnings("methodlength")
    public Filter filterFastSI(final double xMin, final double xStep, final double xMax, final double tMin, final double tStep,
            final double tMax, final Quantity<?, ?>... quantities)
    {
        if (xMin > xMax || xStep <= 0.0 || tMin > tMax || tStep <= 0.0)
        {
            throw new IllegalArgumentException(
                    "Ill-defined grid. Make sure that xMax >= xMin, dx > 0, tMax >= tMin and dt > 0");
        }
        if (notifyListeners(0.0))
        {
            return null;
        }

        // initialize data
        int n = 1 + (int) ((xMax - xMin) / xStep);
        double[] location = new double[n];
        IntStream.range(0, n).forEach(i -> location[i] = xMin + i * xStep);
        n = 1 + (int) ((tMax - tMin) / tStep);
        double[] time = new double[n];
        IntStream.range(0, n).forEach(j -> time[j] = tMin + j * tStep);
        Map<Quantity<?, ?>, double[][]> map = new LinkedHashMap<>();
        Map<Quantity<?, ?>, double[][]> weights = new LinkedHashMap<>();
        for (Quantity<?, ?> quantity : quantities)
        {
            map.put(quantity, new double[location.length][time.length]);
            weights.put(quantity, new double[location.length][time.length]);
        }

        // discretize Kernel
        double xFrom = this.kernel.fromLocation(0.0);
        xFrom = Double.isInfinite(xFrom) ? 2.0 * (xMin - xMax) : xFrom;
        double xTo = this.kernel.toLocation(0.0);
        xTo = Double.isInfinite(xTo) ? 2.0 * (xMax - xMin) : xTo;
        double[] dx = equidistant(xFrom, xStep, xTo);
        double tFrom = this.kernel.fromTime(0.0);
        tFrom = Double.isInfinite(tFrom) ? 2.0 * (tMin - tMax) : tFrom;
        double tTo = this.kernel.toTime(0.0);
        tTo = Double.isInfinite(tTo) ? 2.0 * (tMax - tMin) : tTo;
        double[] dt = equidistant(tFrom, tStep, tTo);
        double[][] phiCong = new double[dx.length][dt.length];
        double[][] phiFree = new double[dx.length][dt.length];
        for (int i = 0; i < dx.length; i++)
        {
            for (int j = 0; j < dt.length; j++)
            {
                phiCong[i][j] = this.kernel.weight(this.cCong, dx[i], dt[j]);
                phiFree[i][j] = this.kernel.weight(this.cFree, dx[i], dt[j]);
            }
        }

        // discretize data
        Map<DataStream<?>, double[][]> dataSum = new LinkedHashMap<>();
        Map<DataStream<?>, double[][]> dataCount = new LinkedHashMap<>(); // integer counts, must be double[][] for convolution
        // loop grid locations
        for (int i = 0; i < location.length; i++)
        {
            // filter applicable data for location
            Map<Double, NavigableMap<Double, Map<DataStream<?>, Double>>> spatialData =
                    this.data.subMap(location[i] - 0.5 * xStep, true, location[i] + 0.5 * xStep, true);
            // loop grid times
            for (int j = 0; j < time.length; j++)
            {
                // filter and loop applicable data for time
                for (NavigableMap<Double, Map<DataStream<?>, Double>> locationData : spatialData.values())
                {
                    NavigableMap<Double, Map<DataStream<?>, Double>> temporalData =
                            locationData.subMap(time[j] - 0.5 * tStep, true, time[j] + 0.5 * tStep, true);
                    for (Map<DataStream<?>, Double> timeData : temporalData.values())
                    {
                        for (Map.Entry<DataStream<?>, Double> timeEntry : timeData.entrySet())
                        {
                            if (map.containsKey(timeEntry.getKey().getQuantity()) || timeEntry.getKey().getQuantity().isSpeed())
                            {
                                dataSum.computeIfAbsent(timeEntry.getKey(), (
                                        key
                                ) -> new double[location.length][time.length])[i][j] += timeEntry.getValue();
                                dataCount.computeIfAbsent(timeEntry.getKey(), (
                                        key
                                ) -> new double[location.length][time.length])[i][j]++;
                            }
                        }
                    }
                }
            }
        }

        // figure out the congestion level estimated for each data source
        double steps = quantities.length + 1; // speed (for congestion level) and then all in quantities
        double step = 0;
        // store maps to prevent us from calculating the convolution for speed again later
        Map<DataSource, double[][]> w = new LinkedHashMap<>();
        Map<DataSource, double[][]> zCongSpeed = new LinkedHashMap<>();
        Map<DataSource, double[][]> zFreeSpeed = new LinkedHashMap<>();
        Map<DataSource, double[][]> nCongSpeed = new LinkedHashMap<>();
        Map<DataSource, double[][]> nFreeSpeed = new LinkedHashMap<>();
        for (Map.Entry<DataStream<?>, double[][]> zEntry : dataSum.entrySet())
        {
            DataStream<?> dataStream = zEntry.getKey();
            if (dataStream.getQuantity().isSpeed()) // only one speed quantity allowed per data source
            {
                // notify
                double[][] vCong = Convolution.convolution(phiCong, zEntry.getValue());
                if (notifyListeners((step + 0.25) / steps))
                {
                    return null;
                }
                double[][] vFree = Convolution.convolution(phiFree, zEntry.getValue());
                if (notifyListeners((step + 0.5) / steps))
                {
                    return null;
                }
                double[][] count = dataCount.get(dataStream);
                double[][] nCong = Convolution.convolution(phiCong, count);
                if (notifyListeners((step + 0.75) / steps))
                {
                    return null;
                }
                double[][] nFree = Convolution.convolution(phiFree, count);
                double[][] wSource = new double[vCong.length][vCong[0].length];
                for (int i = 0; i < vCong.length; i++)
                {
                    for (int j = 0; j < vCong[0].length; j++)
                    {
                        double u = Math.min(vCong[i][j] / nCong[i][j], vFree[i][j] / nFree[i][j]);
                        wSource[i][j] = .5 * (1.0 + Math.tanh((Egtf.this.vc - u) / Egtf.this.deltaV));
                    }
                }
                w.put(dataStream.getDataSource(), wSource);
                zCongSpeed.put(dataStream.getDataSource(), vCong);
                zFreeSpeed.put(dataStream.getDataSource(), vFree);
                nCongSpeed.put(dataStream.getDataSource(), nCong);
                nFreeSpeed.put(dataStream.getDataSource(), nFree);
            }
        }
        step++;
        if (notifyListeners(step / steps))
        {
            return null;
        }

        // sum available data sources per quantity
        double[][] wMean = null;
        for (Quantity<?, ?> quantity : quantities)
        {
            // gather place for this quantity
            double[][] qData = map.get(quantity);
            double[][] qWeights = weights.get(quantity);
            // loop streams that provide this quantity
            Set<Map.Entry<DataStream<?>, double[][]>> zEntries = new LinkedHashSet<>();
            for (Map.Entry<DataStream<?>, double[][]> zEntry : dataSum.entrySet())
            {
                if (zEntry.getKey().getQuantity().equals(quantity))
                {
                    zEntries.add(zEntry);
                }
            }
            double streamCounter = 0;
            for (Map.Entry<DataStream<?>, double[][]> zEntry : zEntries)
            {
                DataStream<?> dataStream = zEntry.getKey();

                // obtain congestion level
                double[][] wj;
                if (!w.containsKey(dataStream.getDataSource()))
                {
                    // this data source has no speed data, but congestion level can be estimated from other sources
                    if (wMean == null)
                    {
                        // let's see if speed was estimated already
                        for (Quantity<?, ?> prevQuant : quantities)
                        {
                            if (prevQuant.equals(quantity))
                            {
                                // it was not, get mean of other data source
                                wMean = new double[location.length][time.length];
                                for (double[][] ww : w.values())
                                {
                                    for (int i = 0; i < location.length; i++)
                                    {
                                        for (int j = 0; j < time.length; j++)
                                        {
                                            wMean[i][j] += ww[i][j] / w.size();
                                        }
                                    }
                                }
                                break;
                            }
                            else if (prevQuant.isSpeed())
                            {
                                wMean = new double[location.length][time.length];
                                double[][] v = map.get(prevQuant);
                                for (int i = 0; i < location.length; i++)
                                {
                                    for (int j = 0; j < time.length; j++)
                                    {
                                        wMean[i][j] = .5 * (1.0 + Math.tanh((Egtf.this.vc - v[i][j]) / Egtf.this.deltaV));
                                    }
                                }
                                break;
                            }
                        }
                    }
                    wj = wMean;
                }
                else
                {
                    wj = w.get(dataStream.getDataSource());
                }

                // convolutions of filters with discretized data and data counts
                double[][] zCong;
                double[][] zFree;
                double[][] nCong;
                double[][] nFree;
                if (dataStream.getQuantity().isSpeed())
                {
                    zCong = zCongSpeed.get(dataStream.getDataSource());
                    zFree = zFreeSpeed.get(dataStream.getDataSource());
                    nCong = nCongSpeed.get(dataStream.getDataSource());
                    nFree = nFreeSpeed.get(dataStream.getDataSource());
                }
                else
                {
                    zCong = Convolution.convolution(phiCong, zEntry.getValue());
                    if (notifyListeners((step + (streamCounter + 0.25) / zEntries.size()) / steps))
                    {
                        return null;
                    }
                    zFree = Convolution.convolution(phiFree, zEntry.getValue());
                    if (notifyListeners((step + (streamCounter + 0.5) / zEntries.size()) / steps))
                    {
                        return null;
                    }
                    double[][] count = dataCount.get(dataStream);
                    nCong = Convolution.convolution(phiCong, count);
                    if (notifyListeners((step + (streamCounter + 0.75) / zEntries.size()) / steps))
                    {
                        return null;
                    }
                    nFree = Convolution.convolution(phiFree, count);
                }

                // loop grid to add to each weighted sum (weighted per data source)
                for (int i = 0; i < location.length; i++)
                {
                    for (int j = 0; j < time.length; j++)
                    {
                        double wCong = wj[i][j];
                        double wFree = 1.0 - wCong;
                        double value = wCong * zCong[i][j] / nCong[i][j] + wFree * zFree[i][j] / nFree[i][j];
                        // the fast filter supplies convoluted data counts, i.e. amount of data and filter proximity; this
                        // is exactly what the EGTF method needs to weigh data sources
                        double beta = wCong * nCong[i][j] + wFree * nFree[i][j];
                        double alpha = wCong / dataStream.getThetaCong() + wFree / dataStream.getThetaFree();
                        double weight = beta * alpha;
                        qData[i][j] += (value * weight);
                        qWeights[i][j] += weight;
                    }
                }
                streamCounter++;
                if (notifyListeners((step + streamCounter / zEntries.size()) / steps))
                {
                    return null;
                }
            }
            for (int i = 0; i < location.length; i++)
            {
                for (int j = 0; j < time.length; j++)
                {
                    qData[i][j] /= qWeights[i][j];
                }
            }
            step++;
        }

        return new FilterDouble(location, time, map);
    }

    /**
     * Returns an equidistant vector that includes 0.
     * @param from double; lowest value to include
     * @param step double; step
     * @param to double; highest value to include
     * @return double[]; equidistant vector that includes 0
     */
    private double[] equidistant(final double from, final double step, final double to)
    {
        int n1 = (int) (-from / step);
        int n2 = (int) (to / step);
        int n = n1 + n2 + 1;
        double[] array = new double[n];
        for (int i = 0; i < n; i++)
        {
            array[i] = i < n1 ? step * (-n1 + i) : step * (i - n1);
        }
        return array;
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
     * @return boolean; whether the filter is interrupted
     */
    private boolean notifyListeners(final double progress)
    {
        if (!this.listeners.isEmpty())
        {
            EgtfEvent event = new EgtfEvent(this, progress);
            for (EgtfListener listener : this.listeners)
            {
                listener.notifyProgress(event);
            }
        }
        return this.interrupted;
    }

    // **********************
    // *** HELPER CLASSES ***
    // **********************

    /**
     * Small class to build up a weighted mean under the congestion and free flow assumption.
     */
    private class DualWeightedMean
    {
        /** Cumulative congestion numerator of weighted mean fraction, i.e. weighted sum. */
        private double numeratorCong;

        /** Cumulative free flow numerator of weighted mean fraction, i.e. weighted sum. */
        private double numeratorFree;

        /** Cumulative congestion denominator of weighted mean fraction, i.e. sum of weights. */
        private double denominatorCong;

        /** Cumulative free flow denominator of weighted mean fraction, i.e. sum of weights. */
        private double denominatorFree;

        /**
         * Adds a congestion value with weight.
         * @param value double; value
         * @param weight double; weight
         */
        public void addCong(final double value, final double weight)
        {
            this.numeratorCong += value * weight;
            this.denominatorCong += weight;
        }

        /**
         * Adds a free flow value with weight.
         * @param value double; value
         * @param weight double; weight
         */
        public void addFree(final double value, final double weight)
        {
            this.numeratorFree += value * weight;
            this.denominatorFree += weight;
        }

        /**
         * Returns the weighted congestion mean of available data.
         * @return double; weighted mean of available data
         */
        public double getCong()
        {
            return this.numeratorCong / this.denominatorCong;
        }

        /**
         * Returns the weighted free flow mean of available data.
         * @return double; weighted free flow mean of available data
         */
        public double getFree()
        {
            return this.numeratorFree / this.denominatorFree;
        }

        /**
         * Returns the sum of congestion weights.
         * @return double; the sum of congestion weights
         */
        public double getDenominatorCong()
        {
            return this.denominatorCong;
        }

        /**
         * Returns the sum of free flow weights.
         * @return double; the sum of free flow weights
         */
        public double getDenominatorFree()
        {
            return this.denominatorFree;
        }

        /** {@inheritDoc} */
        @Override
        public String toString()
        {
            return "DualWeightedMean [numeratorCong=" + this.numeratorCong + ", numeratorFree=" + this.numeratorFree
                    + ", denominatorCong=" + this.denominatorCong + ", denominatorFree=" + this.denominatorFree + "]";
        }

    }

    /**
     * Small class to build up a weighted mean.
     */
    private class WeightedMean
    {
        /** Cumulative numerator of weighted mean fraction, i.e. weighted sum. */
        private double numerator;

        /** Cumulative denominator of weighted mean fraction, i.e. sum of weights. */
        private double denominator;

        /**
         * Adds a value with weight.
         * @param value double; value
         * @param weight double; weight
         */
        public void add(final double value, final double weight)
        {
            this.numerator += value * weight;
            this.denominator += weight;
        }

        /**
         * Returns the weighted mean of available data.
         * @return double; weighted mean of available data
         */
        public double get()
        {
            return this.numerator / this.denominator;
        }

        /** {@inheritDoc} */
        @Override
        public String toString()
        {
            return "WeightedMean [numerator=" + this.numerator + ", denominator=" + this.denominator + "]";
        }

    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "EGTF [kernel=" + this.kernel + ", cCong=" + this.cCong + ", cFree=" + this.cFree + ", deltaV=" + this.deltaV
                + ", vc=" + this.vc + ", dataSources=" + this.dataSources + ", data=" + this.data + ", interrupted="
                + this.interrupted + ", listeners=" + this.listeners + "]";
    }

}
