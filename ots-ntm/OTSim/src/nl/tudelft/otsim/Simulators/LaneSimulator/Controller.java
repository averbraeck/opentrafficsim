package nl.tudelft.otsim.Simulators.LaneSimulator;

/**
 * Abstract class for generic controller functionality. The model run can
 * access controllers using the methods defined here. The <tt>control()</tt>
 * method should be defined in sub classes and is called once each
 * <tt>period</tt>. Typical controllers may be: traffic light controller,
 * ramp-metering, dynamic speed limit controller etc. <br>
 * <br>
 * Note that the sensors and actuators of a controller (detector, signs, lights,
 * etc.) are RSUs and not part of the controller itself. The correct sensors
 * and actuators need to be linked to the controller via controller attributes.
 * <br><br>
 * Other types of controllers are vehicle generators, OBUs and RSUs. These
 * extend <tt>jController</tt> by default.<br>
 * <br>
 * Controllers that do not run with a fixed interval, should override the 
 * <tt>run()</tt> method and there determine whether to run <tt>control()</tt>
 * or <tt>noControl()</tt>.
 */
public abstract class Controller {

    /** Time between control runs of this controller [s]. */
    protected double period;
    
    /** Start time of the first control run of the controller [s]. */
    protected double start = 0;
    
    /** Time of last control run. */
    protected double t;
    
    /** Main model. */
    public Model model;

    /**
     * Constructor with control being used every time step.
     * @param model 
     */
    public Controller(Model model) {
        this(model, 0, 0);
    }

    /**
     * Constructor with control being used every <tt>period</tt>.
     * @param model Main model.
     * @param period Time between control runs.
     */
    public Controller(Model model, double period) {
        this(model, period, 0);
    }

    /**
     * Constructor with control being used every <tt>period</tt> but no sooner
     * than <tt>start</tt>.
     * @param model Main model.
     * @param period Time between control runs.
     * @param start Start time of first control run.
     */
    public Controller(Model model, double period, double start) {
        this.model = model;
        this.period = period;
        this.start = start;
    }

    /**
     * Initialization to be defined by subclasses.
     */
    public abstract void init();

    /**
     * Performs a single time step by either calling the <tt>control()</tt> or
     * <tt>noControl()</tt> method based on <tt>period</tt> and <tt>start</tt>.
     */
    public void run() {
        if (model.t >= t + period && model.t >= start) {
            t = t + period; // set time of latest control
            control();
        } else if (model.t >= t + period) {
            t = t + period; // increase t until t>start
            noControl();
        } else
            noControl();
    }

    /**
     * Control method to be defined by subclasses.
     */
    public abstract void control();

    /**
     * No control method to be defined by subclasses. The main purpose of this
     * method is to include the influence of time.
     */
    public abstract void noControl();
}