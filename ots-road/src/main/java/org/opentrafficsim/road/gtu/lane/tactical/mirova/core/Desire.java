package org.opentrafficsim.road.gtu.lane.tactical.mirova.core;

/**
 * Represents the directional driving desires for a vehicle, separated into
 * <b>mandatory</b> and <b>discretionary</b> lane-change components.
 * <p>
 * This class follows the LMRS philosophy (Schakel et al.): multiple components
 * (e.g. route following, keep-right, overtaking, cooperation) contribute to
 * desires for keeping the lane or changing left/right. Mandatory and
 * discretionary desires are combined differently when deciding the final
 * maneuver tendency.
 * </p>
 */
public final class Desire
{
    // ---- Core fields -------------------------------------------------------

    /** Desire to keep the current lane. */
    private final double keep;

    /** Desire to move left (positive = stronger motivation). */
    private final double left;

    /** Desire to move right (positive = stronger motivation). */
    private final double right;

    /** Whether this desire reflects an mandatory (true) or discretionary (false) component. */
    private final boolean mandatory;

    // ---- Construction -----------------------------------------------------

    /**
     * @param keep  desire to keep lane
     * @param left  desire to move left
     * @param right desire to move right
     * @param mandatory true if this represents an mandatory component (e.g. route-following)
     */
    public Desire(final double keep, final double left, final double right, final boolean mandatory)
    {
        this.keep = keep;
        this.left = left;
        this.right = right;
        this.mandatory = mandatory;
    }

    /** Zero desire (no preference). */
    public static Desire zero() { return new Desire(0, 0, 0, false); }

    // ---- Getters ----------------------------------------------------------

    public double getKeep()  { return this.keep; }
    public double getLeft()  { return this.left; }
    public double getRight() { return this.right; }
    public boolean isMandatory() { return this.mandatory; }

    // ---- Combination utilities -------------------------------------------

    /**
     * Adds another desire vector (component-wise) while preserving the mandatory flag.
     * If any of the two is mandatory, the result is marked mandatory.
     */
    public Desire add(final Desire other)
    {
        boolean newMandatory = this.mandatory || other.mandatory;
        return new Desire(this.keep + other.keep,
                          this.left + other.left,
                          this.right + other.right,
                          newMandatory);
    }

    /**
     * Returns a scaled version of this desire (component-wise multiplication).
     * @param factor scaling factor
     * @return scaled desire
     */
    public Desire scale(final double factor)
    {
        return new Desire(this.keep * factor, this.left * factor, this.right * factor, this.mandatory);
    }

    // ---- LMRS-style combination helpers -----------------------------------

    /**
     * Combine two desires into a single net desire according to LMRS logic.
     * Mandatory desires dominate discretionary ones: discretionary components
     * are only applied if the mandatory desire is below a threshold.
     *
     * @param mandatoryDesire mandatory component
     * @param discretionaryDesire discretionary component
     * @return resulting combined desire
     */
    public static Desire combine(final Desire mandatoryDesire, final Desire discretionaryDesire)
    {
        // typical LMRS logic: if mandatory change required, discretionary effects suppressed
        double wMan = 1.0;
        double wDis = mandatoryDesire.magnitude() > 0.2 ? 0.2 : 1.0; // heuristic
        return new Desire(
            mandatoryDesire.keep * wMan + discretionaryDesire.keep * wDis,
            mandatoryDesire.left * wMan + discretionaryDesire.left * wDis,
            mandatoryDesire.right * wMan + discretionaryDesire.right * wDis,
            mandatoryDesire.isMandatory() || discretionaryDesire.isMandatory()
        );
    }

    /**
     * @return approximate magnitude of the directional part (used for thresholds)
     */
    public double magnitude()
    {
        return Math.max(Math.abs(this.left), Math.abs(this.right));
    }

    @Override
    public String toString()
    {
        return String.format("Desire[keep=%.2f, left=%.2f, right=%.2f, %s]",
            this.keep, this.left, this.right, this.mandatory ? "mandatory" : "discretionary");
    }
}
