package org.opentrafficsim.road.gtu.strategical.od;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.djunits.unit.FrequencyUnit;
import org.djunits.unit.TimeUnit;
import org.djunits.value.StorageType;
import org.djunits.value.ValueException;
import org.djunits.value.vdouble.scalar.Frequency;
import org.djunits.value.vdouble.scalar.Time;
import org.djunits.value.vdouble.vector.FrequencyVector;
import org.djunits.value.vdouble.vector.TimeVector;
import org.opentrafficsim.core.Throw;
import org.opentrafficsim.core.geometry.OTSPoint3D;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.core.network.OTSNetwork;
import org.opentrafficsim.core.network.OTSNode;
import org.opentrafficsim.core.network.route.Route;

/**
 * The minimal OD matrix has 1 origin, 1 destination and 1 time period. More of each can be used. Further categorization of data
 * is possible, i.e. for origin O to destination D, <i>for lane L, for route R and for vehicle class C</i>, the demand at time T
 * is D. The further categorization is defined by an array of {@code Class}'s that define the categorization.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Sep 15, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class ODMatrix
{

    /** Origin nodes. */
    private final Set<Node> origins;

    /** Destination nodes. */
    private final Set<Node> destinations;

    /** Categorization of demand data. */
    private final Categorization categorization;

    /** Global time vector. */
    private final TimeVector globalTimeVector;

    /** Global interpolation of the data. */
    private final Interpolation globalInterpolation;

    /** Demand data per origin and destination, and possibly further categorization. */
    private final Map<Node, Map<Node, Map<Category, ODEntry>>> demandData = new HashMap<>();

    /**
     * Constructs an OD matrix.
     * @param origins origin nodes
     * @param destinations destination nodes
     * @param categorization categorization of data
     * @param globalTimeVector default time
     * @param globalInterpolation interpolation of demand data
     * @throws NullPointerException if any input is null
     */
    public ODMatrix(final Set<Node> origins, final Set<Node> destinations, final Categorization categorization,
        final TimeVector globalTimeVector, final Interpolation globalInterpolation)
    {
        Throw.when(origins == null || origins.contains(null), NullPointerException.class,
            "Origin may not be or contain null.");
        Throw.when(destinations == null || destinations.contains(null), NullPointerException.class,
            "Destination may not be or contain null.");
        Throw.whenNull(categorization, "Categorization may not be null.");
        Throw.whenNull(globalTimeVector, "Global time vector may not be null.");
        Throw.whenNull(globalInterpolation, "Global interpolation may not be null.");
        this.origins = origins;
        this.destinations = destinations;
        this.categorization = categorization;
        this.globalTimeVector = globalTimeVector;
        this.globalInterpolation = globalInterpolation;
        // build empty OD
        for (Node origin : origins)
        {
            this.demandData.put(origin, new HashMap<>());
            for (Node destination : destinations)
            {
                this.demandData.get(origin).put(destination, new HashMap<>());
            }
        }
    }

    /**
     * @return origins.
     */
    public final Set<Node> getOrigins()
    {
        return this.origins;
    }

    /**
     * @return destinations.
     */
    public final Set<Node> getDestinations()
    {
        return this.destinations;
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
     * @param origin origin
     * @param destination destination
     * @param category category
     * @param demand demand data, length has to be equal to the global time vector
     * @throws IllegalArgumentException if origin or destination is not part of the OD matrix
     * @throws IllegalArgumentException if the category does not comply with the categorization
     * @throws NullPointerException if an input is null
     */
    public final void putDemandVector(final Node origin, final Node destination, final Category category,
        final FrequencyVector demand)
    {
        putDemandVector(origin, destination, category, demand, null, null);
    }

    /**
     * @param origin origin
     * @param destination destination
     * @param category category
     * @param demand demand data, length has to be equal to the time vector, or the global time vector if that is null
     * @param timeVector time vector, may be null
     * @param interpolation interpolation, may be null
     * @throws IllegalArgumentException if origin or destination is not part of the OD matrix
     * @throws IllegalArgumentException if the category does not comply with the categorization
     * @throws NullPointerException if an input is null, except for timeVector and interpolation
     */
    public final void putDemandVector(final Node origin, final Node destination, final Category category,
        final FrequencyVector demand, final TimeVector timeVector, final Interpolation interpolation)
    {
        Throw.whenNull(origin, "Origin may not be null.");
        Throw.whenNull(destination, "Destination may not be null.");
        Throw.whenNull(category, "Category may not be null.");
        Throw.whenNull(demand, "Demand data may not be null.");
        Throw.when(!this.origins.contains(origin), IllegalArgumentException.class,
            "Origin '%s' is not part of the OD matrix.", origin);
        Throw.when(!this.destinations.contains(destination), IllegalArgumentException.class,
            "Destination '%s' is not part of the OD matrix.", destination);
        Throw.when(!this.categorization.complies(category), IllegalArgumentException.class,
            "Provided category %s does not comply to the categorization %s.", category, this.categorization);
        ODEntry odEntry = new ODEntry(demand, timeVector, interpolation); // performs checks on vector length
        this.demandData.get(origin).get(destination).put(category, odEntry);
    }

    /**
     * @param origin origin
     * @param destination destination
     * @param category category
     * @return demand data for given origin, destination and categorization, {@code null} if no data is given
     * @throws IllegalArgumentException if origin or destination is not part of the OD matrix
     * @throws IllegalArgumentException if the category does not comply with the categorization
     * @throws NullPointerException if an input is null
     */
    public final FrequencyVector getDemandVector(final Node origin, final Node destination, final Category category)
    {
        ODEntry odEntry = getODEntry(origin, destination, category);
        if (odEntry == null)
        {
            return null;
        }
        return odEntry.getDemand();
    }

    /**
     * @param origin origin
     * @param destination destination
     * @param category category
     * @param time time
     * @return demand for given origin, destination and categorization, at given time, {@code null} if no data is given
     * @throws IllegalArgumentException if origin or destination is not part of the OD matrix
     * @throws IllegalArgumentException if the category does not comply with the categorization
     * @throws NullPointerException if an input is null
     */
    public final Frequency getDemand(final Node origin, final Node destination, final Category category, final Time time)
    {
        ODEntry odEntry = getODEntry(origin, destination, category);
        if (odEntry == null)
        {
            return null;
        }
        return odEntry.getDemand(time);
    }

    /**
     * @param origin origin
     * @param destination destination
     * @param category category
     * @return OD entry for given origin, destination and categorization.
     * @throws IllegalArgumentException if origin or destination is not part of the OD matrix
     * @throws IllegalArgumentException if the category does not comply with the categorization
     * @throws NullPointerException if an input is null
     */
    private ODEntry getODEntry(final Node origin, final Node destination, final Category category)
    {
        Throw.whenNull(origin, "Origin may not be null.");
        Throw.whenNull(destination, "Destination may not be null.");
        Throw.whenNull(category, "Category may not be null.");
        Throw.when(!this.origins.contains(origin), IllegalArgumentException.class,
            "Origin '%s' is not part of the OD matrix", origin);
        Throw.when(!this.destinations.contains(destination), IllegalArgumentException.class,
            "Destination '%s' is not part of the OD matrix.", destination);
        Throw.when(!this.categorization.complies(category), IllegalArgumentException.class,
            "Provided category %s does not comply to the categorization %s.", category, this.categorization);
        return this.demandData.get(origin).get(destination).get(category);
    }

    /** {@inheritDoc} */
    public final String toString()
    {
        return "ODMatrix [" + this.origins.size() + " origins, " + this.destinations.size() + " destinations]";
    }

    /**
     * Prints the complete OD matrix with each demand data on a single line.
     */
    public final void print()
    {
        for (Node origin : this.origins)
        {
            Map<Node, Map<Category, ODEntry>> destinationMap = this.demandData.get(origin);
            for (Node destination : this.destinations)
            {
                Map<Category, ODEntry> categoryMap = destinationMap.get(destination);
                if (categoryMap.isEmpty())
                {
                    System.out.println(origin.getId() + " -> " + destination.getId() + " | -no data-");
                }
                else
                {
                    for (Category category : categoryMap.keySet())
                    {
                        System.out.println(origin.getId() + " -> " + destination.getId() + " | " + category + " | "
                            + categoryMap.get(category).getDemand());
                    }
                }
            }
        }
    }

    public static void main(final String[] args) throws ValueException, NetworkException
    {
        OTSNetwork net = new OTSNetwork("test");
        OTSPoint3D point = new OTSPoint3D(0, 0, 0);
        Node a = new OTSNode(net, "A", point);
        Node b = new OTSNode(net, "B", point);
        Node c = new OTSNode(net, "C", point);
        Node d = new OTSNode(net, "D", point);
        Node e = new OTSNode(net, "E", point);
        Set<Node> origins = new HashSet<>();
        origins.add(a);
        origins.add(b);
        origins.add(c);
        Set<Node> destinations = new HashSet<>();
        destinations.add(a);
        destinations.add(c);
        destinations.add(d);
        Categorization categorization = new Categorization();

        categorization.add(Route.class);
        categorization.add(String.class);

        Route ac1 = new Route("AC1");
        Route ac2 = new Route("AC2");
        Route ad1 = new Route("AD1");
        Route bc1 = new Route("BC1");
        Route bc2 = new Route("BC2");
        Route bd1 = new Route("BD1");

        TimeVector timeVector = new TimeVector(new double[] {0, 1200, 3600}, TimeUnit.SECOND, StorageType.DENSE);
        ODMatrix odMatrix = new ODMatrix(origins, destinations, categorization, timeVector, Interpolation.LINEAR);

        Category category = new Category();
        category.add(ac1);
        category.add("car");
        odMatrix.putDemandVector(a, c, category, new FrequencyVector(new double[] {100, 200, 300}, FrequencyUnit.PER_HOUR,
            StorageType.DENSE));
        category = new Category();
        category.add(ac2);
        category.add("car");
        odMatrix.putDemandVector(a, c, category, new FrequencyVector(new double[] {100, 200, 300}, FrequencyUnit.PER_HOUR,
            StorageType.DENSE));
        category = new Category();
        category.add(ad1);
        category.add("car");
        odMatrix.putDemandVector(a, d, category, new FrequencyVector(new double[] {100, 200, 300}, FrequencyUnit.PER_HOUR,
            StorageType.DENSE));
        category = new Category();
        category.add(ac1);
        category.add("truck");
        odMatrix.putDemandVector(a, c, category, new FrequencyVector(new double[] {100, 200, 300}, FrequencyUnit.PER_HOUR,
            StorageType.DENSE));
        category = new Category();
        category.add(ac2);
        category.add("truck");
        odMatrix.putDemandVector(a, c, category, new FrequencyVector(new double[] {100, 200, 500}, FrequencyUnit.PER_HOUR,
            StorageType.DENSE));
        category = new Category();
        category.add(ad1);
        category.add("truck");
        odMatrix.putDemandVector(a, d, category, new FrequencyVector(new double[] {100, 200, 300}, FrequencyUnit.PER_HOUR,
            StorageType.DENSE));
        category = new Category();
        category.add(bc1);
        category.add("car");
        odMatrix.putDemandVector(b, c, category, new FrequencyVector(new double[] {100, 200, 300}, FrequencyUnit.PER_HOUR,
            StorageType.DENSE));
        category = new Category();
        category.add(bc2);
        category.add("car");
        odMatrix.putDemandVector(b, c, category, new FrequencyVector(new double[] {100, 200, 300}, FrequencyUnit.PER_HOUR,
            StorageType.DENSE));
        category = new Category();
        category.add(bd1);
        category.add("car");
        odMatrix.putDemandVector(b, d, category, new FrequencyVector(new double[] {100, 200, 300}, FrequencyUnit.PER_HOUR,
            StorageType.DENSE));
        category = new Category();
        category.add(bc1);
        category.add("truck");
        odMatrix.putDemandVector(b, c, category, new FrequencyVector(new double[] {100, 200, 300}, FrequencyUnit.PER_HOUR,
            StorageType.DENSE));
        category = new Category();
        category.add(bc2);
        category.add("truck");
        odMatrix.putDemandVector(b, c, category, new FrequencyVector(new double[] {100, 200, 300}, FrequencyUnit.PER_HOUR,
            StorageType.DENSE));
        category = new Category();
        category.add(bd1);
        category.add("truck");
        odMatrix.putDemandVector(b, d, category, new FrequencyVector(new double[] {100, 200, 300}, FrequencyUnit.PER_HOUR,
            StorageType.DENSE));

        odMatrix.print();
        System.out.println(odMatrix);

        category = new Category();
        category.add(ac2);
        category.add("truck");
        for (double t = -100; t <= 3700; t += 100)
        {
            Time time = new Time(t, TimeUnit.SECOND);
            System.out.println("@ t = " + time + ", q = " + odMatrix.getDemand(a, c, category, time));
        }

    }

    /**
     * An ODEntry contains a demand vector, and optionally a time vector and interpolation method that may differ from the
     * global time vector or interpolation method.
     * <p>
     * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
     * <p>
     * @version $Revision$, $LastChangedDate$, by $Author$, initial version Sep 16, 2016 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
     */
    private class ODEntry
    {

        /** Demand vector. */
        private final FrequencyVector demand;

        /** Time vector, may be null. */
        private final TimeVector timeVector;

        /** Interpolation, may be null. */
        private final Interpolation interpolation;

        /**
         * @param demand demand vector
         * @param timeVector time vector, may be null
         * @param interpolation interpolation, may be null
         * @throws IllegalArgumentException if the demand data has a different length than time data
         */
        ODEntry(final FrequencyVector demand, final TimeVector timeVector, final Interpolation interpolation)
        {
            this.demand = demand;
            this.timeVector = timeVector;
            this.interpolation = interpolation;
            Throw.when(demand.size() != getTimeVector().size(), IllegalArgumentException.class,
                "Demand data has different length than time vector.");
        }

        /**
         * Returns the demand at given time. If given time is before the first time slice, 0 demand is returned. If it is after
         * the last time slice, the last demand value is returned.
         * @param time time of demand requested
         * @return demand at given time
         */
        public final Frequency getDemand(final Time time)
        {
            TimeVector timeVec = getTimeVector();
            Interpolation interp = getInterpolation();
            try
            {
                // empty data or before start, return 0
                if (timeVec.size() == 0 || time.lt(timeVec.get(0)))
                {
                    return new Frequency(0.0, FrequencyUnit.PER_HOUR);
                }
                // after end, return last value
                if (time.ge(timeVec.get(timeVec.size() - 1)))
                {
                    return this.demand.get(timeVec.size() - 1);
                }
                // interpolate
                for (int i = 0; i < timeVec.size() - 1; i++)
                {
                    if (timeVec.get(i + 1).ge(time))
                    {
                        return interp.interpolate(this.demand.get(i), timeVec.get(i), this.demand.get(i + 1), timeVec
                            .get(i + 1), time);
                    }
                }
            }
            catch (ValueException ve)
            {
                // should not happen, vector lengths are checked when given is input
                throw new RuntimeException("Index out of bounds.", ve);
            }
            // should not happen
            throw new RuntimeException("Demand interpolation failed.");
        }

        /**
         * @return demand vector
         */
        final FrequencyVector getDemand()
        {
            return this.demand;
        }

        /**
         * @return time vector, either from this entry, or the global vector
         */
        private TimeVector getTimeVector()
        {
            return this.timeVector == null ? ODMatrix.this.getGlobalTimeVector() : this.timeVector;
        }

        /**
         * @return interpolation, either from this entry, or the global interpolation
         */
        private Interpolation getInterpolation()
        {
            return this.interpolation == null ? ODMatrix.this.getGlobalInterpolation() : this.interpolation;
        }

        /** {@inheritDoc} */
        @Override
        public final int hashCode()
        {
            final int prime = 31;
            int result = 1;
            result = prime * result + getOuterType().hashCode();
            result = prime * result + ((this.demand == null) ? 0 : this.demand.hashCode());
            result = prime * result + ((this.interpolation == null) ? 0 : this.interpolation.hashCode());
            result = prime * result + ((this.timeVector == null) ? 0 : this.timeVector.hashCode());
            return result;
        }

        /** {@inheritDoc} */
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
            ODEntry other = (ODEntry) obj;
            if (!getOuterType().equals(other.getOuterType()))
            {
                return false;
            }
            if (this.demand == null)
            {
                if (other.demand != null)
                {
                    return false;
                }
            }
            else if (!this.demand.equals(other.demand))
            {
                return false;
            }
            if (this.interpolation != other.interpolation)
            {
                return false;
            }
            if (this.timeVector == null)
            {
                if (other.timeVector != null)
                {
                    return false;
                }
            }
            else if (!this.timeVector.equals(other.timeVector))
            {
                return false;
            }
            return true;
        }

        /**
         * Accessor for hashcode and equals.
         * @return encompassing OD matrix
         */
        private ODMatrix getOuterType()
        {
            return ODMatrix.this;
        }

    }

}
