package nl.tudelft.otsim.Simulators.LaneSimulator;

/**
 * Consistency checker for the Lane Simulator.
 * 
 * @author Peter Knoppers
 */
public class ConsistencyCheck {
	
	private static String describeLane(Lane lane) {
		return String.format("Lane %d", lane.id);
	}
	/**
	 * Check the internal consistency of a jModel.
	 * @param model {@link Model}; the model to check
	 * @throws Exception 
	 */
	public static void checkPreInit(Model model) throws Exception {
		// Currently only checks the relations between jLanes
		for (Lane lane : model.network) {
			// Check shape
			if (lane.x.length != lane.y.length)
				throw new Exception("Lane " + describeLane(lane) + " has incompatible x and y arrays");
			if (lane.x.length < 2)
				throw new Exception("Lane " + describeLane(lane) + " has too short x and y arrays");
			// Check the up consistency
			if (null != lane.up) {
				if (lane.ups.size() != 0)
					throw new Exception (describeLane(lane) + "has both up and non-empty ups");
				Lane up = lane.up;
				if (null != up.down) {
					if (up.down != lane)
						throw new Exception("The down of the up of " + describeLane(lane) + " is not equal to " + describeLane(lane));
				} else {
					int hits = 0;
					for (Lane down : up.downs)
						if (down == lane)
							hits++;
					if (1 != hits)
						throw new Exception("The downs of the up of " + describeLane(lane) + " contains " + hits + " instances of " + describeLane(lane));
				}
			}
			if (null == lane.ups)
				throw new Exception("The ups of " + describeLane(lane) + " is null");
			for (Lane up : lane.ups) {
				if (null != up.down) {
					if (up.down != lane)
						throw new Exception("The down of up " + describeLane(up) + " of " + describeLane(lane) + " is not equal to " + describeLane(lane));
				} else {
					if (null == up.downs)
						throw new Exception("The up of " + describeLane(lane) + " has now downs");
					int hits = 0;
					for (Lane down : up.downs)
						if (down == lane)
							hits++;
					if (1 != hits)
						throw new Exception("Up " + describeLane(up) + " of " + describeLane(lane) + " contains " + hits + " instances of " + describeLane(lane));
				}
			}
			// Check the down consistency
			if (null != lane.down) {
				if (lane.downs.size() != 0)
					throw new Exception(describeLane(lane) + " has both down and non-empty downs");
				Lane down = lane.down;
				if (null != down.up) {
					if (down.up != lane)
						throw new Exception("The up of the down of " + describeLane(lane)+ " is not equals to " + describeLane(lane));
				} else {
					int hits = 0;
					for (Lane up : down.ups)
						if (up == lane)
							hits++;
					if (1 != hits)
						throw new Exception("The ups of the down of " + describeLane(lane) + " contains " + hits + " instances of " + describeLane(lane));
					
				}
			}
			if (null == lane.downs)
				throw new Exception("The downs of " + describeLane(lane) + " is null");
			for (Lane down : lane.downs){
				if (null != down.up){
					if (down.up != lane)
						throw new Exception("The up of " + describeLane(down) + " of " + describeLane(lane) + " is not equals to " + describeLane(lane));
				} else {
					if (null == down.ups)
						throw new Exception("The down of " + describeLane(lane) + " has no ups");
					int hits = 0;
					for (Lane up : down.ups)
						if (up == lane)
							hits++;
					if (1 != hits)
						throw new Exception("Down " + describeLane(down) + " of " + describeLane(lane) + " contains " + hits + " instances of " + describeLane(lane));
				}
			}
			// Check the left consistency
			Lane left = lane.left;
			if (null != left) {
				if (left.right != lane)
					throw new Exception("The right of the left of " + describeLane(lane) + " is not equal to " + describeLane(lane));
				if (left.x.length != lane.x.length)
					throw new Exception("lane " + describeLane(lane) + " has a number of form points that differs from it\'s left neighbor " + describeLane(left));
			} else if (lane.goLeft)
				throw new Exception("lane " + describeLane(lane) + " has goLeft set but has no left neighbor lane");
			Lane right = lane.right;
			if (null != right) {
				if (right.left != lane)
					throw new Exception("The left of the right of " + describeLane(lane) + " is not equal to " + describeLane(lane));
				if (right.x.length != lane.x.length)
					throw new Exception("lane " + describeLane(lane) + " has a number of form points that differs from it\'s right neighbor " + describeLane(right));
			} else if (lane.goRight)
				throw new Exception("lane " + describeLane(lane) + " has goRight set but has no right neighbor lane");
		}
	}
	
	/**
	 * Check the internal consistency of a jModel.
	 * @param model {@link Model}; the model to check
	 * @throws Exception 
	 */
	public static void checkPostInit(Model model) throws Exception {
		for (Lane lane : model.network) {
			if (null != lane.generator) {
				for (Route route : lane.generator.routes) {
					if (! lane.leadsTo(route.destinations[0]))
						throw new Exception("lane " + describeLane(lane) + " does not lead to destination " + route.destinations[0]);
					for (int index = 1; index < route.destinations.length - 1; index++) {
						for (Lane otherLane : model.network) {
							if (otherLane.destination == route.destinations[index]) {
								if (null != otherLane.down) {
									if (! otherLane.down.leadsTo(route.destinations[index + 1]))
										throw new Exception ("lane " + describeLane(lane) + " has route to " + describeRoute (route) + " but downLane " + describeLane (otherLane.down) + " does not lead to destination " + route.destinations[index + 1]);
								} else if (otherLane.downs.size() == 0)
									throw new Exception("lane " + describeLane(otherLane) + " has empty set of downs");
								else {
									boolean connectionFound = false;
									for (Lane downLane : otherLane.downs) {
										if (downLane.leadsTo(route.destinations[index + 1]))
											connectionFound = true;
									}
									if (! connectionFound)
										throw new Exception("lane " + describeLane(lane) + " has route to " + describeRoute(route) + " but none of the downLanes leads to desctination " + route.destinations[index + 1]); 
								}
							}
								
						}
					}					
				}
			}
		}
	}
	private static String describeRoute(Route route) {
		String result = "";
		String separator = "";
		for (int destination : route.destinations) {
			result += separator + destination;
			separator = ", ";
		}
		return result;
	}

}