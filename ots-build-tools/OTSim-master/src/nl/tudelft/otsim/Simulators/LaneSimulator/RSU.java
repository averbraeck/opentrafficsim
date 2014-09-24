package nl.tudelft.otsim.Simulators.LaneSimulator;

/**
 * Abstract shell for road-side units (RSUs). Besides controller functionality a
 * RSU can be passable, in which case passing vehicles will call the 
 * <tt>pass(jVehicle)</tt> method. A RSU can also be noticable in which case the
 * driver responds when being near to the RSU by calling the 
 * <tt>isNoticed(jDriver)</tt> method. It is not intended that driver behavior 
 * is located in the <tt>isNoticed(jDriver)</tt> method. Instead, forward the
 * notice to the driver by calling <tt>jDriver.notice((myRSU) this)</tt>. The 
 * driver should have a method <tt>notice(myRSU)</tt> where <tt>myRSU</tt> 
 * extends <tt>jRSU</tt>. Note that if that method is missing, the drivers 
 * <tt>notice(jRSU)</tt> method is called, which will recognize that it is
 * being called in a loop and will report an error.
 */
public abstract class RSU extends Controller {

    /** Lane where the RSU is located. */
    public Lane lane;
    
    /** Position [m] of the RSU on the lane. */
    protected double x;
    
    /** Whether the RSU is passable (RSU reacts to vehicle). */
    public boolean passable;
    
    /** Whether the RSU is noticeable (driver reacts to RSU). */
    public boolean noticeable;
  
    /**
     * Constructor using the control every time step. The RSU is linked to the 
     * lane and vice versa.
     * @param lane Lane where the RSU is located.
     * @param x Position [m] of the RSU on the lane.
     * @param passable Whether the RSU is passable.
     * @param noticeable Whether the RSU is noticeable.
     */
    public RSU(Lane lane, double x, boolean passable, boolean noticeable) {
        this(lane, x, 0, 0, passable, noticeable);
    }
    
    /**
     * Constructor using the control every <tt>period</tt>. The RSU is linked to
     * the lane and vice versa.
     * @param lane Lane where the RSU is located.
     * @param x Position [m] of the RSU on the lane.
     * @param period Time [s] between control runs.
     * @param passable Whether the RSU is passable.
     * @param noticeable Whether the RSU is noticeable.
     */
    public RSU(Lane lane, double x, double period, boolean passable, boolean noticeable) {
        this(lane, x, period, 0, passable, noticeable);
    }
    
    /**
     * Constructor using the control every <tt>period</tt> but no sooner than
     * <tt>start</tt>. The RSU is linked to the lane and vice versa.
     * @param lane Lane where the RSU is located.
     * @param x Position [m] of the RSU on the lane.
     * @param period Time [s] between control runs.
     * @param start Time [s] of first control run.
     * @param passable Whether the RSU is passable.
     * @param noticeable Whether the RSU is noticeable.
     */
    // using this at the end is ok, RSU is fully initialized
    public RSU(Lane lane, double x, double period, double start, boolean passable, boolean noticeable) {
        super(lane.model, period, start);
        this.lane = lane;
        this.x = x;
        this.passable = passable;
        this.noticeable = noticeable;
        lane.addRSU(this);
    }
    
    /** 
     * Returns the location of this RSU.
     */
    public double x() {
        return x;
    }
    
    /** Performs the initialization. */
    @Override
	public abstract void init();
    
    /**
     * Vehicle pass method to be defined by subclasses. This method is also 
     * invoked for <tt>noticable</tt> RSUs which may be used to invoke a method
     * of the driver to clear bookkeeping regarding the prior RSU notices.
     * @param vehicle Vehicle that passes the RSU.
     */
    public abstract void pass(Vehicle vehicle);
}