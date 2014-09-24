package nl.tudelft.otsim.Simulators.LaneSimulator;

/**
 * Abstract shell for an on-board unit (OBUs).
 */
public abstract class OBU extends Controller {

    /** Vehicle in which this OBU is. */
    public Vehicle vehicle;
    
    /**
     * Constructor using the control every time step. The OBU is linked to the 
     * vehicle and vice versa.
     * @param vehicle Vehicle.
     */
    public OBU(Vehicle vehicle) {
        this(vehicle, 0, 0);
    }
    
    /**
     * Constructor using the control every <tt>period</tt>. The OBU is linked to
     * the vehicle and vice versa.
     * @param vehicle Vehicle.
     * @param period Time [s] between control runs.
     */
    public OBU(Vehicle vehicle, double period) {
        this(vehicle, period, 0);
    }
    
    /**
     * Constructor using the control every <tt>period</tt> but no sooner than
     * <tt>start</tt>. The OBU is linked to the vehicle and vice versa.
     * @param vehicle Vehicle.
     * @param period Time [s] between control runs.
     * @param start Time [s] of first control run.
     * 
     */
    // using this at the end is ok, OBU is fully initialized
    public OBU(Vehicle vehicle, double period, double start) {
        super(vehicle.model, period, start);
        this.vehicle = vehicle;
        vehicle.OBU = this;
    }
    
    /** Performs the initialization. */
    @Override
	public abstract void init();

    /** The delete command should delete any pointers to this object. */
    public abstract void delete();
}