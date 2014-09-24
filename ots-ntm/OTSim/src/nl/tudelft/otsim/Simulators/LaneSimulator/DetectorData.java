package nl.tudelft.otsim.Simulators.LaneSimulator;

/**
 * Storable detector data from a <tt>jDetector</tt> object.
 */
public class DetectorData implements java.io.Serializable {
	private static final long serialVersionUID = 1L;

	/** Flow counts. */
    public int[] q;

    /** Average speeds. */
    public double[] v;

    /** Lane ID of lane where the detector is positioned. */
    public int lane;

    /** Location of detector on lane. */
    public double x;
    
    /**
     * Constructs a data object from the given <tt>jDetector</tt>.
     * @param detector Detector of which the data needs to be stored.
     */
    public DetectorData(Detector detector) {
        q = new int[detector.qHist.size()];
        for (int i = 0; i<detector.qHist.size(); i++)
            q[i] = detector.qHist.get(i);
        v = new double[detector.vHist.size()];
        for (int i = 0; i<detector.vHist.size(); i++)
            v[i] = detector.vHist.get(i);
        lane = detector.lane.id;
        x = detector.x;
    }
}