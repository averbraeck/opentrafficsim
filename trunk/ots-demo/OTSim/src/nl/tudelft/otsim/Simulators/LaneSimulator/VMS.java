package nl.tudelft.otsim.Simulators.LaneSimulator;

/**
 * Simulate a Variable Message Sign
 * 
 * @author Peter Knoppers
 */
public class VMS extends RSU {
	private final nl.tudelft.otsim.GeoObjects.VMS schedule;

    /**
     * Constructor that sets the traffic light as noticeable.
     * @param lane Lane where the traffic light is at.
     * @param position Position on the lane.
     * @param description String; description of this VMS
     * @throws Exception 
     */
    public VMS(Lane lane, double position, String description) throws Exception {
        super(lane, position, true, false);
        schedule = new nl.tudelft.otsim.GeoObjects.VMS(description);
    }
    
    /**
     * Retrieve the currently shown message.
     * @return String; the currently shown message
     */
    public String getMessage() {
    	return schedule.message(model.t);
    }

	@Override
	public void init() {
		// Required; does nothing
	}

	@Override
	public void pass(Vehicle vehicle) {
    	vehicle.passVMS(getMessage(), this);
    	if (vehicle.model.t() >= 1800)
    		vehicle.getDriver().ActLInc = vehicle.getDriver().ActLInc + 0.1;
	}

	@Override
	public void control() {
		// Required; does nothing
	}

	@Override
	public void noControl() {
		// Required; does nothing;
	}

}
