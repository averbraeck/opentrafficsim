package org.opentrafficsim.core.egtf;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Extended Generalized Treiber-Helbing Filter (van Lint and Hoogendoorn, 2009). This is an extension of the Adaptive Smoothing
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
 * in strongly typed format using a {@code Converter}. Default quantities are available under {@code SPEED_SI}, {@code FLOW_SI}
 * and {@code DENSITY_SI}, all under {@code Quantity}.
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
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class EGTF
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

    /** All point data sorted by space and time, and per data stream. */
    private SortedMap<Double, SortedMap<Double, Map<DataStream<?>, Double>>> pointData = new TreeMap<>();

    /** Whether the calculation was interrupted. */
    private boolean interrupted = false;

    /** Listeners. */
    private Set<EgtfListener> listeners = new LinkedHashSet<>();

    /**
     * Constructor using cCong = -18km/h, cFree = 80km/h, deltaV = 10km/h and vc = 80km/h. A default kernel is set.
     */
    public EGTF()
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
    public EGTF(final double cCong, final double cFree, final double deltaV, final double vc)
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
    public EGTF(final double cCong, final double cFree, final double deltaV, final double vc, final double sigma,
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
     */
    public DataSource getDataSource(final String name)
    {
        return this.dataSources.computeIfAbsent(name, (key) -> new DataSource(key));
    }

    /**
     * Removes all data from before the given time. This is useful in live usages of this class, where older data is no longer
     * required.
     * @param time double; time before which all data can be removed
     */
    public synchronized void clearDataBefore(final double time)
    {
        for (SortedMap<Double, Map<DataStream<?>, Double>> map : this.pointData.values())
        {
            map.subMap(Double.NEGATIVE_INFINITY, time).clear();
        }
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
            getSpacioTemporalData(getSpatialData(location), time).put(dataStream, value);
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
     * Adds data.
     * @param dataStream DataStream; data stream of the data
     * @param location double[]; locations in [m]
     * @param time double[]; times in [s]
     * @param values double[][]; data values in SI unit
     */
    public synchronized void addGridDataSI(final DataStream<?> dataStream, final double[] location, final double[] time,
            final double[][] values)
    {
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
     * Returns data from a specific location as a subset from all data. An empty map is returned if no such data exists.
     * @param location double; location in [m]
     * @return data from a specific location
     */
    private SortedMap<Double, Map<DataStream<?>, Double>> getSpatialData(final double location)
    {
        return this.pointData.computeIfAbsent(location, (key) -> new TreeMap<>());
    }

    /**
     * Returns data from a specific time as a subset of data from a specific location. An empty map is returned if no such data
     * exists.
     * @param spatialData SortedMap; spatially selected data
     * @param time double; time in [s]
     * @return data from a specific time, from data from a specific location
     */
    private Map<DataStream<?>, Double> getSpacioTemporalData(final Map<Double, Map<DataStream<?>, Double>> spatialData,
            final double time)
    {
        return spatialData.computeIfAbsent(time, (key) -> new LinkedHashMap<>());
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
        setKernelSI(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, new ExpKernelShape(sigma, tau));
    }

    /**
     * Returns an exponential kernel with limited range.
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
     * Sets a kernel with limited range and provided shape. The shape allows using non-exponential kernels.
     * @param xMax double; maximum spatial range
     * @param tMax double; maximum temporal range
     * @param shape Shape; shape of the kernel
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
     * @param quantities Quantity...; quantities to calculate filtered data of
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
     * Returns filtered data.
     * @param location double[]; location of output grid in [m]
     * @param time double[]; time of output grid in [s]
     * @param quantities Quantity...; quantities to calculate filtered data of
     * @return Filter; filtered data, {@code null} when interrupted
     */
    @SuppressWarnings("synthetic-access")
    public Filter filterSI(final double[] location, final double[] time, final Quantity<?, ?>... quantities)
    {
        addListener(new EgtfListener()
        {
            @Override
            public void notifyProgress(final EgtfEvent event)
            {
                //
            }
        });
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
            SortedMap<Double, SortedMap<Double, Map<DataStream<?>, Double>>> spatialData =
                    this.pointData.subMap(this.kernel.fromLocation(xGrid), this.kernel.toLocation(xGrid));

            // loop grid times
            for (int j = 0; j < time.length; j++)
            {
                double tGrid = time[j];

                // notify
                notifyListeners((i + (double) j / time.length) / location.length);
                if (this.interrupted)
                {
                    return null;
                }

                // initialize data per stream
                // quantity z assuming congestion and free flow
                Map<DataStream<?>, DualWeightedMean> zCongFree = new LinkedHashMap<>();

                // filter and loop applicable data for time
                for (Map.Entry<Double, SortedMap<Double, Map<DataStream<?>, Double>>> xEntry : spatialData.entrySet())
                {
                    double dx = xEntry.getKey() - xGrid;
                    SortedMap<Double, Map<DataStream<?>, Double>> temporalData =
                            xEntry.getValue().subMap(this.kernel.fromTime(tGrid), this.kernel.toTime(tGrid));

                    for (Map.Entry<Double, Map<DataStream<?>, Double>> tEntry : temporalData.entrySet())
                    {
                        double dt = tEntry.getKey() - tGrid;
                        Map<DataStream<?>, Double> pData = tEntry.getValue();

                        double phiCong = this.kernel.weightCong(this.cCong, dx, dt);
                        double phiFree = this.kernel.weightFree(this.cFree, dx, dt);

                        // loop streams data at point
                        for (Map.Entry<DataStream<?>, Double> vEntry : pData.entrySet())
                        {
                            DataStream<?> stream = vEntry.getKey();
                            double v = vEntry.getValue();
                            DualWeightedMean zCongFreeOfStream =
                                    zCongFree.computeIfAbsent(stream, (key) -> new DualWeightedMean());
                            zCongFreeOfStream.addCong(v, phiCong);
                            zCongFreeOfStream.addFree(v, phiFree);
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
                                .5 * (1.0 + Math.tanh((EGTF.this.vc - u) / EGTF.this.deltaV)));
                        continue;
                    }
                }

                // sum available data sources per quantity
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
                            DualWeightedMean zCongFreej = zEntry.getValue();
                            double zj = wj * zCongFreej.getFree() + (1.0 - wj) * zCongFreej.getCong();
                            double weight;
                            if (w.size() > 1)
                            {
                                // data source more important if more and nearer measurements
                                double beta =
                                        wj * zCongFreej.getDenominatorCong() + (1.0 - wj) * zCongFreej.getDenominatorFree();
                                // more important if more reliable (smaller standard deviation) at congestion level
                                double alpha = wj / dataStream.getThetaFree() + (1.0 - wj) / dataStream.getThetaCong();
                                weight = alpha * beta;
                            }
                            else
                            {
                                weight = 1.0;
                            }
                            z.add(zj, weight);
                        }
                    }
                    qEntry.getValue()[i][j] = z.get();
                }
            }
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
            EgtfEvent event = new EgtfEvent(this, progress);
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
    }
    
}
