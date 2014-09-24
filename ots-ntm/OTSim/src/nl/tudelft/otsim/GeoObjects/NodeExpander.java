package nl.tudelft.otsim.GeoObjects;

/**
 * A NodeExpander is used generate the internals of a {@link Node}.
 * <br /> Nodes with several incoming and outgoing {@link Link Links} must be
 * expanded into a more detailed description for use by micro simulators.
 * <br /> Some nodes are so simple that no expansion is needed. Others may
 * require the creation of a sub-Network with its own {@link Node Nodes} and
 * {@link Link Links}.
 * 
 * @author Peter Knoppers
 */
public interface NodeExpander {

	/**
	 * Expand a {@link Node}.
	 * @return {@link Network} a new sub-Network (or null) if no expansion was needed
	 */
	public Network expandNode();
	
	/**
	 * Retrieve a textual description of the way this NodeExpander works.
	 * <br /> Some NodeExpanders may generate an intersection with traffic
	 * lights, others may create a roundabout, etc.
	 * @return String; description of the way this NodeExpander works.
	 */
	public String description();
	
	/**
	 * Determine the space requirements for the {@link Node}.
	 * @return {@link SimplePolygon}; the SimplePolygon that defines the outline of the required space
	 */
	public SimplePolygon requiredSpace();
}
