package org.opentrafficsim.road.gtu.lane.tactical.mirova.core;

import org.opentrafficsim.core.network.LateralDirectionality;

/**
 * Represents directional lane-change desires according to the LMRS philosophy.
 * <p>
 * A {@code Desire} encodes the tendency of a vehicle to move laterally to the
 * left or right, separated into <b>mandatory</b> (required) and
 * <b>discretionary</b> (optional) components.
 * The desire to stay in the current lane is implied when both left and right
 * desires remain below their activation thresholds.
 * </p>
 * <p>
 * Typical usage:
 * <ul>
 *   <li>Each {@link KnowledgeChunk} produces a directional {@code Desire} (e.g., route following, cooperation).</li>
 *   <li>These are aggregated within the vehicle to form a combined mandatory and discretionary tendency.</li>
 *   <li>Thresholds in the tactical planner decide whether a lane change is initiated.</li>
 * </ul>
 * </p>
 */
public final class Desire {

    // ----------------------------------------------------------------------
    // Core fields
    // ----------------------------------------------------------------------

    /** Desire to move left (positive = stronger motivation). */
    private final double left;

    /** Desire to move right (positive = stronger motivation). */
    private final double right;

    /** Whether this desire represents a mandatory component (e.g. route-following). */
    private final boolean mandatory;

    // ----------------------------------------------------------------------
    // Construction
    // ----------------------------------------------------------------------

    /**
     * Constructs a directional desire vector.
     *
     * @param left        directional desire to move left (positive = stronger)
     * @param right       directional desire to move right (positive = stronger)
     * @param mandatory   true if this represents a mandatory desire (required maneuver)
     */
    public Desire(final double left, final double right, final boolean mandatory) {
        this.left = left;
        this.right = right;
        this.mandatory = mandatory;
    }

    /** Returns a zero desire (no lateral preference). */
    public static Desire zero() {
        return new Desire(0.0, 0.0, false);
    }

    // ----------------------------------------------------------------------
    // Getters
    // ----------------------------------------------------------------------

    public double getLeft() { return this.left; }

    public double getRight() { return this.right; }

    public boolean isMandatory() { return this.mandatory; }

    // ----------------------------------------------------------------------
    // Combination utilities
    // ----------------------------------------------------------------------

    /**
     * Adds another desire vector component-wise.
     * The resulting desire retains the {@code mandatory} flag if any input is mandatory.
     *
     * @param other the other {@code Desire} to add
     * @return summed {@code Desire}
     */
    public Desire add(final Desire other) {
        boolean newMandatory = this.mandatory || other.mandatory;
        return new Desire(
            this.left + other.left,
            this.right + other.right,
            newMandatory
        );
    }

    /**
     * Returns a scaled version of this desire.
     *
     * @param factor scaling factor (e.g., to attenuate influence)
     * @return scaled {@code Desire}
     */
    public Desire scale(final double factor) {
        return new Desire(this.left * factor, this.right * factor, this.mandatory);
    }

    /**
     * Combines mandatory and discretionary desires into a single net desire vector,
     * applying LMRS-inspired per-direction weighting.
     * <p>
     * In the LMRS, left and right directional desires are combined independently:
     * a strong mandatory desire on one side suppresses discretionary influence
     * in that same direction, while the opposite side may remain unaffected.
     * </p>
     *
     * @param mandatoryDesire   the mandatory desire component
     * @param discretionaryDesire the discretionary desire component
     * @param dSync threshold for synchronization
     * @param dCoop threshold for cooperation (mandatory dominance)
     * @return the combined directional desire
     */
    public static Desire combine(final Desire mandatoryDesire, final Desire discretionaryDesire, final double dSync, final double dCoop)
    {
        // Per-direction discretionary weighting:
        // if mandatory is strong in one direction, discretionary contribution there is reduced
        double wDisLeft  = computeDiscLcWeight(mandatoryDesire.getLeft(),  discretionaryDesire.getLeft(),  dSync, dCoop);
        double wDisRight = computeDiscLcWeight(mandatoryDesire.getRight(), discretionaryDesire.getRight(), dSync, dCoop);

        // Combine each direction independently
        double leftCombined  = mandatoryDesire.left  + wDisLeft  * discretionaryDesire.left;
        double rightCombined = mandatoryDesire.right + wDisRight * discretionaryDesire.right;

        // Mandatory flag: true if either component contains mandatory motivation
        boolean combinedMandatory = mandatoryDesire.isMandatory() || discretionaryDesire.isMandatory();

        return new Desire(leftCombined, rightCombined, combinedMandatory);
    }


    // ----------------------------------------------------------------------
    // Utility functions
    // ----------------------------------------------------------------------

    /**
     * Computes the LMRS weighting factor θ_v for combining mandatory and discretionary desires.
     * <p>
     * Based on Schakel et al. (2012), Eq. (12). The weight determines how strongly the
     * discretionary component should contribute, depending on the interaction between
     * mandatory and discretionary desires.
     * </p>
     *
     * @param mandatory the mandatory (route-following) desire for this direction
     * @param discretionary the discretionary (free) desire for this direction
     * @param dSync synchronization threshold (below which mandatory and discretionary coexist)
     * @param dCoop cooperation threshold (above which mandatory dominates completely)
     * @return weighting factor θ_v in [0, 1]
     */
    public static double computeDiscLcWeight(
            final double mandatory,
            final double discretionary,
            final double dSync,
            final double dCoop)
    {

        double product = mandatory * discretionary;
        double absMand = Math.abs(mandatory);

        // Case 1: same direction (no conflict) or mandatory weak
        if (product >= 0.0 || absMand <= dSync)
            return 1.0;

        // Case 2: strong conflict and mandatory very strong
        if (absMand >= dCoop)
            return 0.0;

        // Case 3: conflict and mandatory moderate (linear interpolation)
        return (dCoop - absMand) / (dCoop - dSync);
    }



    /**
     * Returns the overall magnitude of this desire (the maximum absolute directional value).
     *
     * @return maximum(|left|, |right|)
     */
    public double magnitude() {
        return Math.max(Math.abs(this.left), Math.abs(this.right));
    }

    /**
     * Determines the dominant direction of this desire.
     *
     * @return LateralDirectionality.LEFT or LateralDirectionality.RIGHT or null if no dominant direction
     */
    public LateralDirectionality dominantDirection() {
        if (Math.abs(this.left - this.right) < 1e-3)
            return LateralDirectionality.NONE; // No dominant direction
        return (this.left > this.right) ? LateralDirectionality.LEFT : LateralDirectionality.RIGHT;
    }

    @Override
    public String toString() {
        return String.format("Desire[left=%.2f, right=%.2f, %s]",
            this.left, this.right, this.mandatory ? "mandatory" : "discretionary");
    }
}
