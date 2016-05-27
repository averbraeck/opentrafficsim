package org.opentrafficsim.road.gtu.lane.tactical.lmrs;

import org.opentrafficsim.core.network.LateralDirectionality;

/**
 * Reflects the level of lane change desire a driver experiences in both the 
 * left and right direction. This may be either total desire, or only for a 
 * single lane change incentive. Desire is defined as ranging from 0 to 1, 
 * where 0 means no desire and 1 means full desire. Values above 1 are not valid
 * and should be limited to 1. Values below 0 are allowed and reflect that a 
 * lane change is undesired (which is different from not desired).
 * @author Wouter Schakel
 */
public class Desire {
    
    /** Level of left desire. */
    private final double left;
    
    /** Level of right desire. */
    private final double right;
    
    /**
     * Constructor which sets the supplied desire. Desire is limited to a maximum of 1.
     * @param left Left desire.
     * @param right Right desire.
     */
    public Desire(final double left, final double right) {
        this.left = left<=1 ? left : 1;;
        this.right = right<=1 ? right : 1;;
    }
    
    /**
     * Returns desire in the given direction.
     * @param dir Direction for the desire to return.
     * @return Desire in the given direction.
     */
    public double get(LateralDirectionality dir) {
        if (dir==LateralDirectionality.LEFT) {
            return this.left;
        } else if (dir==LateralDirectionality.RIGHT) {
            return this.right;
        }
        throw new NullPointerException("Lateral direction may not be null.");
    }
    
    /**
     * Returns lane change desire to left.
     * @return Lane change desire to left.
     */
    public double getLeft() {
    	return this.left;
    }
    
    /**
     * Returns lane change desire to right.
     * @return Lane change desire to right.
     */
    public double getRight() {
    	return this.right;
    }
    
    /**
     * Returns whether the left desire is larger than (or equal to) the right.
     * @return Returns whether the left desire is larger than (or equal to) the right.
     */
    public boolean leftIsLargerOrEqual() {
    	return this.left>=this.right;
    }
    
}