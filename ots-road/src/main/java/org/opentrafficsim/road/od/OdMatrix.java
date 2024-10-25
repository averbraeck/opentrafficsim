package org.opentrafficsim.road.od;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.djunits.unit.FrequencyUnit;
import org.djunits.unit.TimeUnit;
import org.djunits.value.ValueRuntimeException;
import org.djunits.value.vdouble.scalar.Frequency;
import org.djunits.value.vdouble.scalar.Time;
import org.djunits.value.vdouble.vector.FrequencyVector;
import org.djunits.value.vdouble.vector.TimeVector;
import org.djutils.base.Identifiable;
import org.djutils.exceptions.Throw;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.core.network.route.Route;
import org.opentrafficsim.road.gtu.generator.headway.DemandPattern;

/**
 * The minimal OD matrix has 1 origin, 1 destination and 1 time period. More of each can be used. Further categorization of data
 * is possible, i.e. for origin O to destination D, <i>for lane L, for route R and for vehicle class C</i>, the demand at time T
 * is D. The further categorization is defined by an array of {@code Class}'s that define the categorization.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class OdMatrix implements Serializable, Identifiable
{

    /** */
    private static final long serialVersionUID = 20160921L;

    /** Id. */
    private final String id;

    /** Origin nodes. */
    private final List<Node> origins;

    /** Destination nodes. */
    private final List<Node> destinations;

    /** Categorization of demand data. */
    private final Categorization categorization;

    /** Global time vector. */
    private final TimeVector globalTimeVector;

    /** Global interpolation of the data. */
    private final Interpolation globalInterpolation;

    /** Demand data per origin and destination, and possibly further categorization. */
    private final Map<Node, Map<Node, Map<Category, DemandPattern>>> demandData = new LinkedHashMap<>();

    /** Node comparator. */
    private static final Comparator<Node> COMPARATOR = new Comparator<Node>()
    {
        @Override
        public int compare(final Node o1, final Node o2)
        {
            return o1.getId().compareTo(o2.getId());
        }
    };

    /**
     * Constructs an OD matrix.
     * @param id id
     * @param origins origin nodes
     * @param destinations destination nodes
     * @param categorization categorization of data
     * @param globalTimeVector default time
     * @param globalInterpolation interpolation of demand data
     * @throws NullPointerException if any input is null
     */
    public OdMatrix(final String id, final List<? extends Node> origins, final List<? extends Node> destinations,
            final Categorization categorization, final TimeVector globalTimeVector, final Interpolation globalInterpolation)
    {
        Throw.whenNull(id, "Id may not be null.");
        Throw.whenNull(origins, "Origins may not be null.");
        Throw.when(origins.contains(null), NullPointerException.class, "Origin may not contain null.");
        Throw.whenNull(destinations, "Destination may not be null.");
        Throw.when(destinations.contains(null), NullPointerException.class, "Destination may not contain null.");
        Throw.whenNull(categorization, "Categorization may not be null.");
        // Throw.whenNull(globalTimeVector, "Global time vector may not be null.");
        // Throw.whenNull(globalInterpolation, "Global interpolation may not be null.");
        this.id = id;
        this.origins = new ArrayList<>(origins);
        this.destinations = new ArrayList<>(destinations);
        Collections.sort(this.origins, COMPARATOR);
        Collections.sort(this.destinations, COMPARATOR);
        this.categorization = categorization;
        this.globalTimeVector = globalTimeVector;
        this.globalInterpolation = globalInterpolation;
        // build empty OD
        for (Node origin : origins)
        {
            Map<Node, Map<Category, DemandPattern>> map = new LinkedHashMap<>();
            for (Node destination : destinations)
            {
                map.put(destination, new TreeMap<>(new Comparator<Category>()
                {
                    @Override
                    public int compare(final Category o1, final Category o2)
                    {
                        for (int i = 0; i < o1.getCategorization().size(); i++)
                        {
                            int order = Integer.compare(o1.get(i).hashCode(), o2.get(i).hashCode());
                            if (order != 0)
                            {
                                return order;
                            }
                        }
                        return 0;
                    }
                }));
            }
            this.demandData.put(origin, map);
        }
    }

    /**
     * @return id.
     */
    @Override
    public final String getId()
    {
        return this.id;
    }

    /**
     * @return origins.
     */
    public final List<Node> getOrigins()
    {
        return new ArrayList<>(this.origins);
    }

    /**
     * @return destinations.
     */
    public final List<Node> getDestinations()
    {
        return new ArrayList<>(this.destinations);
    }

    /**
     * @return categorization.
     */
    public final Categorization getCategorization()
    {
        return this.categorization;
    }

    /**
     * @return globalTimeVector.
     */
    public final TimeVector getGlobalTimeVector()
    {
        return this.globalTimeVector;
    }

    /**
     * @return globalInterpolation.
     */
    public final Interpolation getGlobalInterpolation()
    {
        return this.globalInterpolation;
    }

    /**
     * Add a demand vector to OD.
     * @param origin origin
     * @param destination destination
     * @param category category
     * @param demand demand data, length has to be equal to the global time vector
     * @param fraction fraction of demand for this category
     * @throws IllegalArgumentException if origin or destination is not part of the OD matrix
     * @throws IllegalArgumentException if the category does not belong to the categorization
     * @throws IllegalArgumentException if the demand data has a different length than time data, or is less than 2
     * @throws IllegalArgumentException if demand is negative or time not strictly increasing
     * @throws IllegalArgumentException if the route (if in the category) is not from the origin to the destination
     * @throws NullPointerException if an input is null
     */
    public final void putDemandVector(final Node origin, final Node destination, final Category category,
            final FrequencyVector demand, final double fraction)
    {
        putDemandVector(origin, destination, category, demand, this.globalTimeVector, this.globalInterpolation, fraction);
    }

    /**
     * Add a demand vector to OD.
     * @param origin origin
     * @param destination destination
     * @param category category
     * @param demand demand data, length has to be equal to the global time vector
     * @throws IllegalArgumentException if origin or destination is not part of the OD matrix
     * @throws IllegalArgumentException if the category does not belong to the categorization
     * @throws IllegalArgumentException if the demand data has a different length than time data, or is less than 2
     * @throws IllegalArgumentException if demand is negative or time not strictly increasing
     * @throws IllegalArgumentException if the route (if in the category) is not from the origin to the destination
     * @throws NullPointerException if an input is null
     */
    public final void putDemandVector(final Node origin, final Node destination, final Category category,
            final FrequencyVector demand)
    {
        putDemandVector(origin, destination, category, demand, this.globalTimeVector, this.globalInterpolation);
    }

    /**
     * Add a demand vector to OD. In this method, which all other methods that add or put demand indirectly refer to, many
     * consistency and validity checks are performed. These do not include checks on network connectivity, since the network may
     * be subject to change during simulation.
     * @param origin origin
     * @param destination destination
     * @param category category
     * @param demand demand data, length has to be equal to the time vector
     * @param timeVector time vector
     * @param interpolation interpolation
     * @throws IllegalArgumentException if origin or destination is not part of the OD matrix
     * @throws IllegalArgumentException if the category does not belong to the categorization
     * @throws IllegalArgumentException if the demand data has a different length than time data, or is less than 2
     * @throws IllegalArgumentException if demand is negative or time not strictly increasing
     * @throws IllegalArgumentException if the route (if in the category) is not from the origin to the destination
     * @throws NullPointerException if an input is null
     */
    public final void putDemandVector(final Node origin, final Node destination, final Category category,
            final FrequencyVector demand, final TimeVector timeVector, final Interpolation interpolation)
    {
        Throw.whenNull(origin, "Origin may not be null.");
        Throw.whenNull(destination, "Destination may not be null.");
        Throw.whenNull(category, "Category may not be null.");
        Throw.whenNull(demand, "Demand data may not be null.");
        Throw.whenNull(timeVector, "Time vector may not be null.");
        Throw.whenNull(interpolation, "Interpolation may not be null.");
        Throw.when(!this.origins.contains(origin), IllegalArgumentException.class, "Origin '%s' is not part of the OD matrix.",
                origin);
        Throw.when(!this.destinations.contains(destination), IllegalArgumentException.class,
                "Destination '%s' is not part of the OD matrix.", destination);
        Throw.when(!this.categorization.equals(category.getCategorization()), IllegalArgumentException.class,
                "Provided category %s does not belong to the categorization %s.", category, this.categorization);
        Throw.when(demand.size() != timeVector.size() || demand.size() < 2, IllegalArgumentException.class,
                "Demand data has different length than time vector, or has less than 2 values.");
        for (Frequency q : demand)
        {
            Throw.when(q.lt0(), IllegalArgumentException.class, "Demand contains negative value(s).");
        }
        Time prevTime;
        try
        {
            prevTime = timeVector.get(0).eq0() ? Time.instantiateSI(-1.0) : Time.ZERO;
        }
        catch (ValueRuntimeException exception)
        {
            // verified to be > 1, so no empty vector
            throw new RuntimeException("Unexpected exception while checking time vector.", exception);
        }
        for (Time time : timeVector)
        {
            Throw.when(prevTime.ge(time), IllegalArgumentException.class,
                    "Time vector is not strictly increasing, or contains negative time.");
            prevTime = time;
        }
        if (this.categorization.entails(Route.class))
        {
            Route route = category.get(Route.class);
            try
            {
                Throw.when(!route.originNode().equals(origin) || !route.destinationNode().equals(destination),
                        IllegalArgumentException.class,
                        "Route from %s to %s does not comply with origin %s and destination %s.", route.originNode(),
                        route.destinationNode(), origin, destination);
            }
            catch (NetworkException exception)
            {
                throw new IllegalArgumentException("Route in OD has no nodes.", exception);
            }
        }
        DemandPattern demandPattern = new DemandPattern(demand, timeVector, interpolation);
        this.demandData.get(origin).get(destination).put(category, demandPattern);
    }

    /**
     * Add a demand vector to OD, by a fraction of total demand.
     * @param origin origin
     * @param destination destination
     * @param category category
     * @param demand demand data, length has to be equal to the time vector
     * @param timeVector time vector
     * @param interpolation interpolation
     * @param fraction fraction of demand for this category
     * @throws IllegalArgumentException if origin or destination is not part of the OD matrix
     * @throws IllegalArgumentException if the category does not belong to the categorization
     * @throws IllegalArgumentException if the demand data has a different length than time data, or is less than 2
     * @throws IllegalArgumentException if demand is negative or time not strictly increasing
     * @throws IllegalArgumentException if the route (if in the category) is not from the origin to the destination
     * @throws NullPointerException if an input is null
     */
    public final void putDemandVector(final Node origin, final Node destination, final Category category,
            final FrequencyVector demand, final TimeVector timeVector, final Interpolation interpolation, final double fraction)
    {
        Throw.whenNull(demand, "Demand data may not be null.");
        FrequencyVector demandScaled;
        if (fraction == 1.0)
        {
            demandScaled = demand;
        }
        else
        {
            double[] in = demand.getValuesInUnit();
            double[] scaled = new double[in.length];
            for (int i = 0; i < in.length; i++)
            {
                scaled[i] = in[i] * fraction;
            }
            try
            {
                demandScaled = new FrequencyVector(scaled, demand.getDisplayUnit(), demand.getStorageType());
            }
            catch (ValueRuntimeException exception)
            {
                // cannot happen, we use an existing vector
                throw new RuntimeException("An object was null.", exception);
            }
        }
        putDemandVector(origin, destination, category, demandScaled, timeVector, interpolation);
    }

    /**
     * Add a demand vector to OD, by a fraction per time period of total demand.
     * @param origin origin
     * @param destination destination
     * @param category category
     * @param demand demand data, length has to be equal to the time vector
     * @param timeVector time vector
     * @param interpolation interpolation
     * @param fraction fraction of demand for this category
     * @throws IllegalArgumentException if origin or destination is not part of the OD matrix
     * @throws IllegalArgumentException if the category does not belong to the categorization
     * @throws IllegalArgumentException if the demand data has a different length than time data, or is less than 2
     * @throws IllegalArgumentException if demand is negative or time not strictly increasing
     * @throws IllegalArgumentException if the route (if in the category) is not from the origin to the destination
     * @throws NullPointerException if an input is null
     */
    public final void putDemandVector(final Node origin, final Node destination, final Category category,
            final FrequencyVector demand, final TimeVector timeVector, final Interpolation interpolation,
            final double[] fraction)
    {
        Throw.whenNull(demand, "Demand data may not be null.");
        Throw.whenNull(fraction, "Fraction data may not be null.");
        Throw.whenNull(timeVector, "Time vector may not be null.");
        Throw.when(demand.size() != timeVector.size() || timeVector.size() != fraction.length, IllegalArgumentException.class,
                "Arrays are of unequal length: demand=%d, timeVector=%d, fraction=%d", demand.size(), timeVector.size(),
                fraction.length);
        double[] in = demand.getValuesInUnit();
        double[] scaled = new double[in.length];
        for (int i = 0; i < in.length; i++)
        {
            scaled[i] = in[i] * fraction[i];
        }
        FrequencyVector demandScaled;
        try
        {
            demandScaled = new FrequencyVector(scaled, demand.getDisplayUnit(), demand.getStorageType());
        }
        catch (ValueRuntimeException exception)
        {
            // cannot happen, we use an existing vector
            throw new RuntimeException("An object was null.", exception);
        }
        putDemandVector(origin, destination, category, demandScaled, timeVector, interpolation);
    }

    /**
     * @param origin origin
     * @param destination destination
     * @param category category
     * @return demand data for given origin, destination and categorization, {@code null} if no data is given
     * @throws IllegalArgumentException if origin or destination is not part of the OD matrix
     * @throws IllegalArgumentException if the category does not belong to the categorization
     * @throws NullPointerException if an input is null
     */
    public final FrequencyVector getDemandVector(final Node origin, final Node destination, final Category category)
    {
        DemandPattern demandPattern = getDemandPattern(origin, destination, category);
        if (demandPattern == null)
        {
            return null;
        }
        return demandPattern.demandVector();
    }

    /**
     * @param origin origin
     * @param destination destination
     * @param category category
     * @return interpolation for given origin, destination and categorization, {@code null} if no data is given
     * @throws IllegalArgumentException if origin or destination is not part of the OD matrix
     * @throws IllegalArgumentException if the category does not belong to the categorization
     * @throws NullPointerException if an input is null
     */
    public final TimeVector getTimeVector(final Node origin, final Node destination, final Category category)
    {
        DemandPattern demandPattern = getDemandPattern(origin, destination, category);
        if (demandPattern == null)
        {
            return null;
        }
        return demandPattern.timeVector();
    }

    /**
     * @param origin origin
     * @param destination destination
     * @param category category
     * @return interpolation for given origin, destination and categorization, {@code null} if no data is given
     * @throws IllegalArgumentException if origin or destination is not part of the OD matrix
     * @throws IllegalArgumentException if the category does not belong to the categorization
     * @throws NullPointerException if an input is null
     */
    public final Interpolation getInterpolation(final Node origin, final Node destination, final Category category)
    {
        DemandPattern demandPattern = getDemandPattern(origin, destination, category);
        if (demandPattern == null)
        {
            return null;
        }
        return demandPattern.interpolation();
    }

    /**
     * Returns the demand at given time. If given time is before the first time slice or after the last time slice, 0 demand is
     * returned.
     * @param origin origin
     * @param destination destination
     * @param category category
     * @param time time
     * @param sliceStart whether the time is at the start of an arbitrary time slice
     * @return demand for given origin, destination and categorization, at given time
     * @throws IllegalArgumentException if origin or destination is not part of the OD matrix
     * @throws IllegalArgumentException if the category does not belong to the categorization
     * @throws NullPointerException if an input is null
     */
    public final Frequency getDemand(final Node origin, final Node destination, final Category category, final Time time,
            final boolean sliceStart)
    {
        Throw.whenNull(time, "Time may not be null.");
        DemandPattern demandPattern = getDemandPattern(origin, destination, category);
        if (demandPattern == null)
        {
            return new Frequency(0.0, FrequencyUnit.PER_HOUR); // Frequency.ZERO gives "Hz" which is not nice for flow
        }
        return demandPattern.getFrequency(time, sliceStart);
    }

    /**
     * @param origin origin
     * @param destination destination
     * @param category category
     * @return OD entry for given origin, destination and categorization.
     * @throws IllegalArgumentException if origin or destination is not part of the OD matrix
     * @throws IllegalArgumentException if the category does not belong to the categorization
     * @throws NullPointerException if an input is null
     */
    public DemandPattern getDemandPattern(final Node origin, final Node destination, final Category category)
    {
        Throw.whenNull(origin, "Origin may not be null.");
        Throw.whenNull(destination, "Destination may not be null.");
        Throw.whenNull(category, "Category may not be null.");
        Throw.when(!this.origins.contains(origin), IllegalArgumentException.class, "Origin '%s' is not part of the OD matrix",
                origin);
        Throw.when(!this.destinations.contains(destination), IllegalArgumentException.class,
                "Destination '%s' is not part of the OD matrix.", destination);
        Throw.when(!this.categorization.equals(category.getCategorization()), IllegalArgumentException.class,
                "Provided category %s does not belong to the categorization %s.", category, this.categorization);
        return this.demandData.get(origin).get(destination).get(category);
    }

    /**
     * @param origin origin
     * @param destination destination
     * @param category category
     * @return whether there is data for the specified origin, destination and category
     * @throws IllegalArgumentException if origin or destination is not part of the OD matrix
     * @throws IllegalArgumentException if the category does not belong to the categorization
     * @throws NullPointerException if an input is null
     */
    public final boolean contains(final Node origin, final Node destination, final Category category)
    {
        return getDemandPattern(origin, destination, category) != null;
    }

    /**
     * Returns the categories specified for given origin-destination combination.
     * @param origin origin
     * @param destination destination
     * @return categories specified for given origin-destination combination
     * @throws IllegalArgumentException if origin or destination is not part of the OD matrix
     * @throws NullPointerException if an input is null
     */
    public final Set<Category> getCategories(final Node origin, final Node destination)
    {
        Throw.whenNull(origin, "Origin may not be null.");
        Throw.whenNull(destination, "Destination may not be null.");
        Throw.when(!this.origins.contains(origin), IllegalArgumentException.class, "Origin '%s' is not part of the OD matrix",
                origin);
        Throw.when(!this.destinations.contains(destination), IllegalArgumentException.class,
                "Destination '%s' is not part of the OD matrix.", destination);
        return new LinkedHashSet<>(this.demandData.get(origin).get(destination).keySet());
    }

    /******************************************************************************************************/
    /****************************************** TRIP METHODS **********************************************/
    /******************************************************************************************************/

    /**
     * @param origin origin
     * @param destination destination
     * @param category category
     * @param trips trip data, length has to be equal to the global time vector - 1, each value is the number of trips during a
     *            period
     * @throws IllegalArgumentException if origin or destination is not part of the OD matrix
     * @throws IllegalArgumentException if the category does not belong to the categorization
     * @throws IllegalArgumentException if the demand data has a different length than time data, or is less than 2
     * @throws IllegalArgumentException if demand is negative or time not strictly increasing
     * @throws IllegalArgumentException if the route (if in the category) is not from the origin to the destination
     * @throws NullPointerException if an input is null
     */
    public final void putTripsVector(final Node origin, final Node destination, final Category category, final int[] trips)
    {
        putTripsVector(origin, destination, category, trips, getGlobalTimeVector());
    }

    /**
     * Sets demand data by number of trips. Interpolation over time is stepwise.
     * @param origin origin
     * @param destination destination
     * @param category category
     * @param trips trip data, length has to be equal to the time vector - 1, each value is the number of trips during a period
     * @param timeVector time vector
     * @throws IllegalArgumentException if origin or destination is not part of the OD matrix
     * @throws IllegalArgumentException if the category does not belong to the categorization
     * @throws IllegalArgumentException if the demand data has a different length than time data, or is less than 2
     * @throws IllegalArgumentException if demand is negative or time not strictly increasing
     * @throws IllegalArgumentException if the route (if in the category) is not from the origin to the destination
     * @throws NullPointerException if an input is null
     */
    public final void putTripsVector(final Node origin, final Node destination, final Category category, final int[] trips,
            final TimeVector timeVector)
    {
        // this is what we need here, other checks in putDemandVector
        Throw.whenNull(trips, "Demand data may not be null.");
        Throw.whenNull(timeVector, "Time vector may not be null.");
        Throw.when(trips.length != timeVector.size() - 1, IllegalArgumentException.class,
                "Trip data and time data have wrong lengths. Trip data should be 1 shorter than time data.");
        // convert to flow
        double[] flow = new double[timeVector.size()];
        try
        {
            for (int i = 0; i < trips.length; i++)
            {
                flow[i] = trips[i] / (timeVector.get(i + 1).getInUnit(TimeUnit.BASE_HOUR)
                        - timeVector.get(i).getInUnit(TimeUnit.BASE_HOUR));
            }
            // last value can remain zero as initialized
            putDemandVector(origin, destination, category, new FrequencyVector(flow, FrequencyUnit.PER_HOUR), timeVector,
                    Interpolation.STEPWISE);
        }
        catch (ValueRuntimeException exception)
        {
            // should not happen as we check and then loop over the array length
            throw new RuntimeException("Could not translate trip vector into demand vector.", exception);
        }
    }

    /**
     * @param origin origin
     * @param destination destination
     * @param category category
     * @return trip data for given origin, destination and categorization, {@code null} if no data is given
     * @throws IllegalArgumentException if origin or destination is not part of the OD matrix
     * @throws IllegalArgumentException if the category does not belong to the categorization
     * @throws NullPointerException if an input is null
     */
    public final int[] getTripsVector(final Node origin, final Node destination, final Category category)
    {
        FrequencyVector demand = getDemandVector(origin, destination, category);
        if (demand == null)
        {
            return null;
        }
        int[] trips = new int[demand.size() - 1];
        TimeVector time = getTimeVector(origin, destination, category);
        Interpolation interpolation = getInterpolation(origin, destination, category);
        for (int i = 0; i < trips.length; i++)
        {
            try
            {
                trips[i] = interpolation.integrate(demand.get(i), time.get(i), demand.get(i + 1), time.get(i + 1));
            }
            catch (ValueRuntimeException exception)
            {
                // should not happen as we loop over the array length
                throw new RuntimeException("Could not translate demand vector into trip vector.", exception);
            }
        }
        return trips;
    }

    /**
     * Returns the number of trips in the given time period.
     * @param origin origin
     * @param destination destination
     * @param category category
     * @param periodIndex index of time period
     * @return demand for given origin, destination and categorization, at given time
     * @throws IllegalArgumentException if origin or destination is not part of the OD matrix
     * @throws IllegalArgumentException if the category does not belong to the categorization
     * @throws IndexOutOfBoundsException if the period is outside of the specified range
     * @throws NullPointerException if an input is null
     */
    public final int getTrips(final Node origin, final Node destination, final Category category, final int periodIndex)
    {
        TimeVector time = getTimeVector(origin, destination, category);
        if (time == null)
        {
            return 0;
        }
        Throw.when(periodIndex < 0 || periodIndex >= time.size() - 1, IndexOutOfBoundsException.class,
                "Period index out of range.");
        FrequencyVector demand = getDemandVector(origin, destination, category);
        Interpolation interpolation = getInterpolation(origin, destination, category);
        try
        {
            return interpolation.integrate(demand.get(periodIndex), time.get(periodIndex), demand.get(periodIndex + 1),
                    time.get(periodIndex + 1));
        }
        catch (ValueRuntimeException exception)
        {
            // should not happen as the index was checked
            throw new RuntimeException("Could not get number of trips.", exception);
        }
    }

    /**
     * Adds a number of trips to given origin-destination combination, category and time period. This can only be done for data
     * with stepwise interpolation.
     * @param origin origin
     * @param destination destination
     * @param category category
     * @param periodIndex index of time period
     * @param trips trips to add (may be negative)
     * @throws IllegalArgumentException if origin or destination is not part of the OD matrix
     * @throws IllegalArgumentException if the category does not belong to the categorization
     * @throws IndexOutOfBoundsException if the period is outside of the specified range
     * @throws UnsupportedOperationException if the interpolation of the data is not stepwise, or demand becomes negtive
     * @throws NullPointerException if an input is null
     */
    public final void increaseTrips(final Node origin, final Node destination, final Category category, final int periodIndex,
            final int trips)
    {
        Interpolation interpolation = getInterpolation(origin, destination, category);
        Throw.when(!interpolation.equals(Interpolation.STEPWISE), UnsupportedOperationException.class,
                "Can only increase the number of trips for data with stepwise interpolation.");
        TimeVector time = getTimeVector(origin, destination, category);
        Throw.when(periodIndex < 0 || periodIndex >= time.size() - 1, IndexOutOfBoundsException.class,
                "Period index out of range.");
        FrequencyVector demand = getDemandVector(origin, destination, category);
        try
        {
            double additionalDemand = trips / (time.get(periodIndex + 1).getInUnit(TimeUnit.BASE_HOUR)
                    - time.get(periodIndex).getInUnit(TimeUnit.BASE_HOUR));
            double[] dem = demand.getValuesInUnit(FrequencyUnit.PER_HOUR);
            Throw.when(dem[periodIndex] < -additionalDemand, UnsupportedOperationException.class,
                    "Demand may not become negative.");
            dem[periodIndex] += additionalDemand;
            putDemandVector(origin, destination, category, new FrequencyVector(dem, FrequencyUnit.PER_HOUR), time,
                    Interpolation.STEPWISE);
        }
        catch (ValueRuntimeException exception)
        {
            // should not happen as the index was checked
            throw new RuntimeException("Unexpected exception while getting number of trips.", exception);
        }
    }

    /**
     * Calculates total number of trips over time for given origin.
     * @param origin origin
     * @return total number of trips over time for given origin
     * @throws IllegalArgumentException if origin is not part of the OD matrix
     * @throws NullPointerException if origin is null
     */
    public final int originTotal(final Node origin)
    {
        int sum = 0;
        for (Node destination : getDestinations())
        {
            sum += originDestinationTotal(origin, destination);
        }
        return sum;
    }

    /**
     * Calculates total number of trips over time for given destination.
     * @param destination destination
     * @return total number of trips over time for given destination
     * @throws IllegalArgumentException if destination is not part of the OD matrix
     * @throws NullPointerException if destination is null
     */
    public final int destinationTotal(final Node destination)
    {
        int sum = 0;
        for (Node origin : getOrigins())
        {
            sum += originDestinationTotal(origin, destination);
        }
        return sum;
    }

    /**
     * Calculates total number of trips over time for the complete matrix.
     * @return total number of trips over time for the complete matrix
     * @throws IllegalArgumentException if origin or destination is not part of the OD matrix
     * @throws NullPointerException if an input is null
     */
    public final int matrixTotal()
    {
        int sum = 0;
        for (Node origin : getOrigins())
        {
            for (Node destination : getDestinations())
            {
                sum += originDestinationTotal(origin, destination);
            }
        }
        return sum;
    }

    /**
     * Calculates total number of trips over time for given origin-destination combination.
     * @param origin origin
     * @param destination destination
     * @return total number of trips over time for given origin-destination combination
     * @throws IllegalArgumentException if origin or destination is not part of the OD matrix
     * @throws NullPointerException if an input is null
     */
    public final int originDestinationTotal(final Node origin, final Node destination)
    {
        int sum = 0;
        for (Category category : getCategories(origin, destination))
        {
            TimeVector time = getTimeVector(origin, destination, category);
            FrequencyVector demand = getDemandVector(origin, destination, category);
            Interpolation interpolation = getInterpolation(origin, destination, category);
            for (int i = 0; i < time.size() - 1; i++)
            {
                try
                {
                    sum += interpolation.integrate(demand.get(i), time.get(i), demand.get(i + 1), time.get(i + 1));
                }
                catch (ValueRuntimeException exception)
                {
                    // should not happen as we loop over the array length
                    throw new RuntimeException("Unexcepted exception while determining total trips over time.", exception);
                }
            }
        }
        return sum;
    }

    /******************************************************************************************************/
    /****************************************** OTHER METHODS *********************************************/
    /******************************************************************************************************/

    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public String toString()
    {
        return "OdMatrix [" + this.id + ", " + this.origins.size() + " origins, " + this.destinations.size() + " destinations, "
                + this.categorization + " ]";
    }

    /**
     * Prints the complete OD matrix with each demand data on a single line.
     */
    public final void print()
    {
        int originLength = 0;
        for (Node origin : this.origins)
        {
            originLength = originLength >= origin.getId().length() ? originLength : origin.getId().length();
        }
        int destinLength = 0;
        for (Node destination : this.destinations)
        {
            destinLength = destinLength >= destination.getId().length() ? destinLength : destination.getId().length();
        }
        String format = "%-" + Math.max(originLength, 1) + "s -> %-" + Math.max(destinLength, 1) + "s | ";
        for (Node origin : this.origins)
        {
            Map<Node, Map<Category, DemandPattern>> destinationMap = this.demandData.get(origin);
            for (Node destination : this.destinations)
            {
                Map<Category, DemandPattern> categoryMap = destinationMap.get(destination);
                if (categoryMap.isEmpty())
                {
                    System.out.println(String.format(format, origin.getId(), destination.getId()) + "-no data-");
                }
                else
                {
                    for (Category category : categoryMap.keySet())
                    {
                        StringBuilder catStr = new StringBuilder("[");
                        String sep = "";
                        for (int i = 0; i < category.getCategorization().size(); i++)
                        {
                            catStr.append(sep);
                            Object obj = category.get(i);
                            if (obj instanceof Route)
                            {
                                catStr.append("Route: " + ((Route) obj).getId());
                            }
                            else
                            {
                                catStr.append(obj);
                            }
                            sep = ", ";
                        }
                        catStr.append("]");
                        // System.out.println("DEBUG format is \"" + format + "\"");
                        System.out.println(String.format(format, origin.getId(), destination.getId()) + catStr + " | "
                                + categoryMap.get(category).demandVector());
                    }
                }
            }
        }
    }

    @Override
    public final int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.categorization == null) ? 0 : this.categorization.hashCode());
        result = prime * result + ((this.demandData == null) ? 0 : this.demandData.hashCode());
        result = prime * result + ((this.destinations == null) ? 0 : this.destinations.hashCode());
        result = prime * result + ((this.globalInterpolation == null) ? 0 : this.globalInterpolation.hashCode());
        result = prime * result + ((this.globalTimeVector == null) ? 0 : this.globalTimeVector.hashCode());
        result = prime * result + ((this.id == null) ? 0 : this.id.hashCode());
        result = prime * result + ((this.origins == null) ? 0 : this.origins.hashCode());
        return result;
    }

    @Override
    public final boolean equals(final Object obj)
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
        OdMatrix other = (OdMatrix) obj;
        if (this.categorization == null)
        {
            if (other.categorization != null)
            {
                return false;
            }
        }
        else if (!this.categorization.equals(other.categorization))
        {
            return false;
        }
        if (this.demandData == null)
        {
            if (other.demandData != null)
            {
                return false;
            }
        }
        else if (!this.demandData.equals(other.demandData))
        {
            return false;
        }
        if (this.destinations == null)
        {
            if (other.destinations != null)
            {
                return false;
            }
        }
        else if (!this.destinations.equals(other.destinations))
        {
            return false;
        }
        if (this.globalInterpolation != other.globalInterpolation)
        {
            return false;
        }
        if (this.globalTimeVector == null)
        {
            if (other.globalTimeVector != null)
            {
                return false;
            }
        }
        else if (!this.globalTimeVector.equals(other.globalTimeVector))
        {
            return false;
        }
        if (this.id == null)
        {
            if (other.id != null)
            {
                return false;
            }
        }
        else if (!this.id.equals(other.id))
        {
            return false;
        }
        if (this.origins == null)
        {
            if (other.origins != null)
            {
                return false;
            }
        }
        else if (!this.origins.equals(other.origins))
        {
            return false;
        }
        return true;
    }

}
