package nl.tudelft.otsim.Simulators.LaneSimulator;

import nl.tudelft.otsim.GUI.Main;

/**
 * Simple representation of a route. The route is represented as an array of
 * destinations that should match <tt>jLane.destination</tt> attributes.
 */
public class Route {
    
    /** Array of destinations. */
    protected int[] destinations;
    
    /**
     * Default constructor.
     * @param destinations Ordered (intermediate) destinations of this route.
     */
    public Route(int[] destinations) {
        this.destinations = destinations;
    }
    
    /**
     * Returns the array of destinations.
     * @return Destination array.
     */
    public int[] destinations() {
        return destinations;
    }
    
    /**
     * Returns the sub-route after an intermediate destination has been passed.
     * If the intermediate destination is not part of the route, the full route
     * is returned.
     * @param destination Passed destination.
     * @return Sub-route after the passed destination.
     */
    public Route subRouteAfter(int destination) {
        int i = -1;
        for (int j = 0; j < destinations.length; j++)
            if (destinations[j] == destination)
                i = j + 1; // +1 for 'after'
        int[] newRoute = new int[destinations.length - i];
        if (i < 0) {	// did not find destination in the list
        	System.err.println("Destination " + destination + " not found in route " + toString());
        	return new Route(destinations);
        }
        for (int j = i; j < destinations.length; j++)
            newRoute[j - i] = destinations[j];
        return new Route(newRoute);
    }
    
    /**
     * Returns whether this route can be followed from the given lane.
     * @param lane Lane of which needs to be known if the route can be followed.
     * @return Whether this route can be followed from the given lane.
     */
    public boolean canBeFollowedFrom(Lane lane) {
        if (lane == null)
            return false;
        // Check and handling added by PK
        // TODO: check correctness of this handling with Wouter Schakel
        if (destinations.length == 0)
        	return false;
        // 20140314/PK: Also try the second destination
        boolean result = lane.leadsTo(destinations[0]);
        if ((! result) && (destinations.length > 1))
        	result = lane.leadsTo(destinations[1]);
        if ((! result) && (destinations.length > 2))
        	result = lane.leadsTo(destinations[2]);
        return result;
        //return lane.leadsTo(destinations[0]);
    }
    
    /**
     * Returns the number of lane changes that need to be performed to follow
     * this route from the given lane.
     * @param lane Considered lane.
     * @return Number of lane changes that needs to be performed for this route.
     */
    public int nLaneChanges(Lane lane) {
        if (lane.leadsTo(destinations[0]))
            return lane.nLaneChanges(destinations[0]);
        return 0;
    }
    
    /**
     * Returns the distance within which a number of lane changes has to be
     * performed to follow this route from the given lane.
     * @param lane Considered lane.
     * @return Distance [m] within which a number of lane changes has to be performed.
     */
    public double xLaneChanges(Lane lane) {
        if (lane.leadsTo(destinations[0]))
            return lane.xLaneChanges(destinations[0]);
        return 0;
    }
    
    @Override
	public String toString() {
    	String result = "[";
    	boolean first = true;
    	for (int destination : destinations) {
    		if (! first)
    			result += " ";
    		result += String.format(Main.locale, "%d", destination);
    		first = false;
    	}
		return result + "]";
    }
}