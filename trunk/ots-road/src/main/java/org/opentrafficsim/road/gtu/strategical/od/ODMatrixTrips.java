package org.opentrafficsim.road.gtu.strategical.od;

import java.util.List;

import org.djunits.unit.FrequencyUnit;
import org.djunits.unit.TimeUnit;
import org.djunits.value.StorageType;
import org.djunits.value.ValueException;
import org.djunits.value.vdouble.vector.DurationVector;
import org.djunits.value.vdouble.vector.FrequencyVector;
import org.opentrafficsim.core.Throw;
import org.opentrafficsim.core.network.Node;

/**
 * Extension of ODMatrix where all input and output can be given in number of trips. All data that is defined in number of trips
 * has Interpolation.STEPWISE.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Sep 28, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class ODMatrixTrips extends ODMatrix
{

    /** */
    private static final long serialVersionUID = 20160928L;

    /**
     * Constructs an OD matrix based on trips.
     * @param id id
     * @param origins origin nodes
     * @param destinations destination nodes
     * @param categorization categorization of data
     * @param globalTimeVector default time
     * @param globalInterpolation interpolation of demand data
     * @throws NullPointerException if any input is null
     */
    public ODMatrixTrips(final String id, final List<Node> origins, final List<Node> destinations,
        final Categorization categorization, final DurationVector globalTimeVector, final Interpolation globalInterpolation)
    {
        super(id, origins, destinations, categorization, globalTimeVector, globalInterpolation);
    }

    /**
     * @param origin origin
     * @param destination destination
     * @param category category
     * @param trips trip data, length has to be equal to the global time vector - 1
     * @throws IllegalArgumentException if origin or destination is not part of the OD matrix
     * @throws IllegalArgumentException if the category does not belong to the categorization
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
     * @param trips trip data, length has to be equal to the time vector - 1
     * @param timeVector time vector
     * @throws IllegalArgumentException if origin or destination is not part of the OD matrix
     * @throws IllegalArgumentException if the category does not belong to the categorization
     * @throws NullPointerException if an input is null
     */
    public final void putTripsVector(final Node origin, final Node destination, final Category category, final int[] trips,
        final DurationVector timeVector)
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
                flow[i] =
                    trips[i] / (timeVector.get(i + 1).getInUnit(TimeUnit.HOUR) - timeVector.get(i).getInUnit(TimeUnit.HOUR));
            }
            // last value can remain zero as initialized
            putDemandVector(origin, destination, category, new FrequencyVector(flow, FrequencyUnit.PER_HOUR,
                StorageType.DENSE), timeVector, Interpolation.STEPWISE);
        }
        catch (ValueException exception)
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
        DurationVector time = getTimeVector(origin, destination, category);
        Interpolation interpolation = getInterpolation(origin, destination, category);
        for (int i = 0; i < trips.length; i++)
        {
            try
            {
                trips[i] = interpolation.integrate(demand.get(i), time.get(i), demand.get(i + 1), time.get(i + 1));
            }
            catch (ValueException exception)
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
     * @throws IllegalArgumentException if the period is outside of the specified range
     * @throws NullPointerException if an input is null
     */
    public final int getTrips(final Node origin, final Node destination, final Category category, final int periodIndex)
    {
        DurationVector time = getTimeVector(origin, destination, category);
        if (time == null)
        {
            return 0;
        }
        Throw.when(periodIndex < 0 || periodIndex >= time.size() - 1, IllegalArgumentException.class,
            "Period index out of range.");
        FrequencyVector demand = getDemandVector(origin, destination, category);
        Interpolation interpolation = getInterpolation(origin, destination, category);
        try
        {
            return interpolation.integrate(demand.get(periodIndex), time.get(periodIndex), demand.get(periodIndex + 1), time
                .get(periodIndex + 1));
        }
        catch (ValueException exception)
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
     * @throws IllegalArgumentException if the period is outside of the specified range
     * @throws UnsupportedOperationException if the interpolation of the data is not stepwise
     * @throws NullPointerException if an input is null
     */
    public final void increaseTrips(final Node origin, final Node destination, final Category category,
        final int periodIndex, final int trips)
    {
        Interpolation interpolation = getInterpolation(origin, destination, category);
        Throw.when(!interpolation.equals(Interpolation.STEPWISE), UnsupportedOperationException.class,
            "Can only increase the number of trips for data with stepwise interpolation.");
        DurationVector time = getTimeVector(origin, destination, category);
        Throw.when(periodIndex < 0 || periodIndex >= time.size() - 1, IllegalArgumentException.class,
            "Period index out of range.");
        FrequencyVector demand = getDemandVector(origin, destination, category);
        try
        {
            double additionalDemand =
                trips
                    / (time.get(periodIndex + 1).getInUnit(TimeUnit.HOUR) - time.get(periodIndex).getInUnit(TimeUnit.HOUR));
            double[] dem = demand.getValuesInUnit(FrequencyUnit.PER_HOUR);
            dem[periodIndex] += additionalDemand;
            putDemandVector(origin, destination, category, new FrequencyVector(dem, FrequencyUnit.PER_HOUR,
                StorageType.DENSE), time, Interpolation.STEPWISE);
        }
        catch (ValueException exception)
        {
            // should not happen as the index was checked
            throw new RuntimeException("Could not get number of trips.", exception);
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
            DurationVector time = getTimeVector(origin, destination, category);
            FrequencyVector demand = getDemandVector(origin, destination, category);
            Interpolation interpolation = getInterpolation(origin, destination, category);
            for (int i = 0; i < time.size(); i++)
            {
                try
                {
                    sum += interpolation.integrate(demand.get(i), time.get(i), demand.get(i + 1), time.get(i + 1));
                }
                catch (ValueException exception)
                {
                    // should not happen as we loop over the array length
                    throw new RuntimeException("Could not determine total trips over time.", exception);
                }
            }
        }
        return sum;
    }

    /** {@inheritDoc} */
    public final String toString()
    {
        return "ODMatrixTrips [" + getOrigins().size() + " origins, " + getDestinations().size() + " destinations, "
            + getCategorization() + " ]";
    }

}
