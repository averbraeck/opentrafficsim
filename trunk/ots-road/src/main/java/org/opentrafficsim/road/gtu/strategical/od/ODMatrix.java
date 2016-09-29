package org.opentrafficsim.road.gtu.strategical.od;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.djunits.unit.FrequencyUnit;
import org.djunits.unit.TimeUnit;
import org.djunits.value.StorageType;
import org.djunits.value.ValueException;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Frequency;
import org.djunits.value.vdouble.vector.DurationVector;
import org.djunits.value.vdouble.vector.FrequencyVector;
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
public class ODMatrix implements Serializable
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
    private final DurationVector globalTimeVector;

    /** Global interpolation of the data. */
    private final Interpolation globalInterpolation;

    /** Demand data per origin and destination, and possibly further categorization. */
    private final Map<Node, Map<Node, Map<Category, ODEntry>>> demandData = new HashMap<>();

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
    public ODMatrix(final String id, final List<Node> origins, final List<Node> destinations,
        final Categorization categorization, final DurationVector globalTimeVector, final Interpolation globalInterpolation)
    {
        Throw.whenNull(id, "Id may not be null.");
        Throw.when(origins == null || origins.contains(null), NullPointerException.class,
            "Origin may not be or contain null.");
        Throw.when(destinations == null || destinations.contains(null), NullPointerException.class,
            "Destination may not be or contain null.");
        Throw.whenNull(categorization, "Categorization may not be null.");
        Throw.whenNull(globalTimeVector, "Global time vector may not be null.");
        Throw.whenNull(globalInterpolation, "Global interpolation may not be null.");
        this.id = id;
        this.origins = new ArrayList<>(origins);
        this.destinations = new ArrayList<>(destinations);
        this.categorization = categorization;
        this.globalTimeVector = globalTimeVector;
        this.globalInterpolation = globalInterpolation;
        // build empty OD
        for (Node origin : origins)
        {
            Map<Node, Map<Category, ODEntry>> map = new HashMap<>();
            for (Node destination : destinations)
            {
                map.put(destination, new HashMap<>());
            }
            this.demandData.put(origin, map);
        }
    }

    /**
     * @return id.
     */
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
    public final DurationVector getGlobalTimeVector()
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
     * @throws IllegalArgumentException if the category does not belong to the categorization
     * @throws NullPointerException if an input is null
     */
    public final void putDemandVector(final Node origin, final Node destination, final Category category,
        final FrequencyVector demand)
    {
        putDemandVector(origin, destination, category, demand, this.globalTimeVector, this.globalInterpolation);
    }

    /**
     * @param origin origin
     * @param destination destination
     * @param category category
     * @param demand demand data, length has to be equal to the time vector
     * @param timeVector time vector
     * @param interpolation interpolation
     * @throws IllegalArgumentException if origin or destination is not part of the OD matrix
     * @throws IllegalArgumentException if the category does not belong to the categorization
     * @throws NullPointerException if an input is null
     */
    public final void putDemandVector(final Node origin, final Node destination, final Category category,
        final FrequencyVector demand, final DurationVector timeVector, final Interpolation interpolation)
    {
        Throw.whenNull(origin, "Origin may not be null.");
        Throw.whenNull(destination, "Destination may not be null.");
        Throw.whenNull(category, "Category may not be null.");
        Throw.whenNull(demand, "Demand data may not be null.");
        Throw.whenNull(timeVector, "Time vector may not be null.");
        Throw.whenNull(interpolation, "Interpolation may not be null.");
        Throw.when(!this.origins.contains(origin), IllegalArgumentException.class,
            "Origin '%s' is not part of the OD matrix.", origin);
        Throw.when(!this.destinations.contains(destination), IllegalArgumentException.class,
            "Destination '%s' is not part of the OD matrix.", destination);
        Throw.when(!this.categorization.equals(category.getCategorization()), IllegalArgumentException.class,
            "Provided category %s does not belong to the categorization %s.", category, this.categorization);
        ODEntry odEntry = new ODEntry(demand, timeVector, interpolation); // performs checks on vector length
        this.demandData.get(origin).get(destination).put(category, odEntry);
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
        ODEntry odEntry = getODEntry(origin, destination, category);
        if (odEntry == null)
        {
            return null;
        }
        return odEntry.getDemandVector();
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
    public final DurationVector getTimeVector(final Node origin, final Node destination, final Category category)
    {
        ODEntry odEntry = getODEntry(origin, destination, category);
        if (odEntry == null)
        {
            return null;
        }
        return odEntry.getTimeVector();
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
        ODEntry odEntry = getODEntry(origin, destination, category);
        if (odEntry == null)
        {
            return null;
        }
        return odEntry.getInterpolation();
    }

    /**
     * Returns the demand at given time. If given time is before the first time slice or after the last time slice, 0 demand is
     * returned.
     * @param origin origin
     * @param destination destination
     * @param category category
     * @param time time
     * @return demand for given origin, destination and categorization, at given time
     * @throws IllegalArgumentException if origin or destination is not part of the OD matrix
     * @throws IllegalArgumentException if the category does not belong to the categorization
     * @throws NullPointerException if an input is null
     */
    public final Frequency
        getDemand(final Node origin, final Node destination, final Category category, final Duration time)
    {
        Throw.whenNull(time, "Time may not be null.");
        ODEntry odEntry = getODEntry(origin, destination, category);
        if (odEntry == null)
        {
            return new Frequency(0.0, FrequencyUnit.PER_HOUR); // Frequency.ZERO give "Hz" which is not nice for flow
        }
        return odEntry.getDemand(time);
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
    private ODEntry getODEntry(final Node origin, final Node destination, final Category category)
    {
        Throw.whenNull(origin, "Origin may not be null.");
        Throw.whenNull(destination, "Destination may not be null.");
        Throw.whenNull(category, "Category may not be null.");
        Throw.when(!this.origins.contains(origin), IllegalArgumentException.class,
            "Origin '%s' is not part of the OD matrix", origin);
        Throw.when(!this.destinations.contains(destination), IllegalArgumentException.class,
            "Destination '%s' is not part of the OD matrix.", destination);
        Throw.when(!this.categorization.equals(category.getCategorization()), IllegalArgumentException.class,
            "Provided category %s does not belong to the categorization %s.", category, this.categorization);
        return this.demandData.get(origin).get(destination).get(category);
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
        Throw.when(!this.origins.contains(origin), IllegalArgumentException.class,
            "Origin '%s' is not part of the OD matrix", origin);
        Throw.when(!this.destinations.contains(destination), IllegalArgumentException.class,
            "Destination '%s' is not part of the OD matrix.", destination);
        return new HashSet<>(this.demandData.get(origin).get(destination).keySet());
    }

    /** {@inheritDoc} */
    @SuppressWarnings("checkstyle:designforextension")
    public String toString()
    {
        return "ODMatrix [" + this.origins.size() + " origins, " + this.destinations.size() + " destinations, "
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
        String format = "%-" + originLength + "s -> %-" + destinLength + "s | ";
        for (Node origin : this.origins)
        {
            Map<Node, Map<Category, ODEntry>> destinationMap = this.demandData.get(origin);
            for (Node destination : this.destinations)
            {
                Map<Category, ODEntry> categoryMap = destinationMap.get(destination);
                if (categoryMap.isEmpty())
                {
                    System.out.println(String.format(format, origin.getId(), destination.getId()) + "-no data-");
                }
                else
                {
                    for (Category category : categoryMap.keySet())
                    {
                        System.out.println(String.format(format, origin.getId(), destination.getId()) + category + " | "
                            + categoryMap.get(category).getDemandVector());
                    }
                }
            }
        }
    }

    /** {@inheritDoc} */
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
        ODMatrix other = (ODMatrix) obj;
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

    // TODO remove this method as soon as there is a JUNIT test
    public static void main(final String[] args) throws ValueException, NetworkException
    {

        int aa = 10;
        System.out.println(aa);
        aa = aa + (aa >> 1);
        System.out.println(aa);
        aa = aa + (aa >> 1);
        System.out.println(aa);
        aa = aa + (aa >> 1);
        System.out.println(aa);
        aa = aa + (aa >> 1);
        System.out.println(aa);

        OTSNetwork net = new OTSNetwork("test");
        OTSPoint3D point = new OTSPoint3D(0, 0, 0);
        Node a = new OTSNode(net, "A", point);
        Node b = new OTSNode(net, "Barendrecht", point);
        Node c = new OTSNode(net, "C", point);
        Node d = new OTSNode(net, "Delft", point);
        Node e = new OTSNode(net, "E", point);
        List<Node> origins = new ArrayList<>();
        origins.add(a);
        origins.add(b);
        origins.add(c);
        List<Node> destinations = new ArrayList<>();
        destinations.add(a);
        destinations.add(c);
        destinations.add(d);
        Categorization categorization = new Categorization("test", Route.class, String.class);
        Route ac1 = new Route("AC1");
        Route ac2 = new Route("AC2");
        Route ad1 = new Route("AD1");
        Route bc1 = new Route("BC1");
        Route bc2 = new Route("BC2");
        Route bd1 = new Route("BD1");

        DurationVector timeVector = new DurationVector(new double[] {0, 1200, 3600}, TimeUnit.SECOND, StorageType.DENSE);
        ODMatrix odMatrix = new ODMatrix("TestOD", origins, destinations, categorization, timeVector, Interpolation.LINEAR);

        Category category = new Category(categorization, ac1, "car");
        odMatrix.putDemandVector(a, c, category, new FrequencyVector(new double[] {100, 200, 300}, FrequencyUnit.PER_HOUR,
            StorageType.DENSE));
        category = new Category(categorization, ac2, "car");
        odMatrix.putDemandVector(a, c, category, new FrequencyVector(new double[] {100, 200, 300}, FrequencyUnit.PER_HOUR,
            StorageType.DENSE));
        category = new Category(categorization, ad1, "car");
        odMatrix.putDemandVector(a, d, category, new FrequencyVector(new double[] {100, 200, 300}, FrequencyUnit.PER_HOUR,
            StorageType.DENSE));
        category = new Category(categorization, ac1, "car");
        odMatrix.putDemandVector(a, c, category, new FrequencyVector(new double[] {100, 200, 300}, FrequencyUnit.PER_HOUR,
            StorageType.DENSE));
        category = new Category(categorization, ac2, "truck");
        odMatrix.putDemandVector(a, c, category, new FrequencyVector(new double[] {100, 200, 500}, FrequencyUnit.PER_HOUR,
            StorageType.DENSE));
        category = new Category(categorization, ad1, "truck");
        odMatrix.putDemandVector(a, d, category, new FrequencyVector(new double[] {100, 200, 300}, FrequencyUnit.PER_HOUR,
            StorageType.DENSE));
        category = new Category(categorization, bc1, "truck");
        odMatrix.putDemandVector(b, c, category, new FrequencyVector(new double[] {100, 200, 300}, FrequencyUnit.PER_HOUR,
            StorageType.DENSE));
        category = new Category(categorization, bc2, "truck");
        odMatrix.putDemandVector(b, c, category, new FrequencyVector(new double[] {100, 200, 300}, FrequencyUnit.PER_HOUR,
            StorageType.DENSE));
        category = new Category(categorization, bd1, "car");
        odMatrix.putDemandVector(b, d, category, new FrequencyVector(new double[] {100, 200, 300}, FrequencyUnit.PER_HOUR,
            StorageType.DENSE));
        category = new Category(categorization, bc1, "car");
        odMatrix.putDemandVector(b, c, category, new FrequencyVector(new double[] {100, 200, 300}, FrequencyUnit.PER_HOUR,
            StorageType.DENSE));
        category = new Category(categorization, bc2, "car");
        odMatrix.putDemandVector(b, c, category, new FrequencyVector(new double[] {100, 200, 300}, FrequencyUnit.PER_HOUR,
            StorageType.DENSE));
        category = new Category(categorization, bd1, "truck");
        odMatrix.putDemandVector(b, d, category, new FrequencyVector(new double[] {100, 200, 300}, FrequencyUnit.PER_HOUR,
            StorageType.DENSE));

        odMatrix.print();
        System.out.println(odMatrix);

        category = new Category(categorization, ac2, "truck");
        for (double t = -100; t <= 3700; t += 100)
        {
            Duration time = new Duration(t, TimeUnit.SECOND);
            System.out.println("@ t = " + time + ", q = " + odMatrix.getDemand(a, c, category, time));
        }

        System.out.println("For OD       that does not exist; q = " + odMatrix.getDemand(c, a, category, Duration.ZERO));
        category = new Category(categorization, ac2, "does not exist");
        System.out.println("For category that does not exist; q = " + odMatrix.getDemand(a, c, category, Duration.ZERO));

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
        private final FrequencyVector demandVector;

        /** Time vector, may be null. */
        private final DurationVector timeVector;

        /** Interpolation, may be null. */
        private final Interpolation interpolation;

        /**
         * @param demandVector demand vector
         * @param timeVector time vector
         * @param interpolation interpolation
         * @throws IllegalArgumentException if the demand data has a different length than time data
         */
        ODEntry(final FrequencyVector demandVector, final DurationVector timeVector, final Interpolation interpolation)
        {
            Throw.when(demandVector.size() != timeVector.size(), IllegalArgumentException.class,
                "Demand data has different length than time vector.");
            this.demandVector = demandVector;
            this.timeVector = timeVector;
            this.interpolation = interpolation;
        }

        /**
         * Returns the demand at given time. If given time is before the first time slice or after the last time slice, 0 demand
         * is returned.
         * @param time time of demand requested
         * @return demand at given time
         */
        public final Frequency getDemand(final Duration time)
        {
            try
            {
                // empty data or before start or after end, return 0
                if (this.timeVector.size() == 0 || time.lt(this.timeVector.get(0))
                    || time.ge(this.timeVector.get(this.timeVector.size() - 1)))
                {
                    return new Frequency(0.0, FrequencyUnit.PER_HOUR); // Frequency.ZERO give "Hz" which is not nice for flow
                }
                // interpolate
                for (int i = 0; i < this.timeVector.size() - 1; i++)
                {
                    if (this.timeVector.get(i + 1).ge(time))
                    {
                        return this.interpolation.interpolate(this.demandVector.get(i), this.timeVector.get(i),
                            this.demandVector.get(i + 1), this.timeVector.get(i + 1), time);
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
         * @return demandVector
         */
        final FrequencyVector getDemandVector()
        {
            return this.demandVector;
        }

        /**
         * @return timeVector
         */
        final DurationVector getTimeVector()
        {
            return this.timeVector;
        }

        /**
         * @return interpolation
         */
        final Interpolation getInterpolation()
        {
            return this.interpolation;
        }

        /** {@inheritDoc} */
        @Override
        public final int hashCode()
        {
            final int prime = 31;
            int result = 1;
            result = prime * result + getOuterType().hashCode();
            result = prime * result + ((this.demandVector == null) ? 0 : this.demandVector.hashCode());
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
            if (this.demandVector == null)
            {
                if (other.demandVector != null)
                {
                    return false;
                }
            }
            else if (!this.demandVector.equals(other.demandVector))
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
