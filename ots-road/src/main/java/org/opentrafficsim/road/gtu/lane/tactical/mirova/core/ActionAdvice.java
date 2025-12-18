package org.opentrafficsim.road.gtu.lane.tactical.mirova.core;

import java.util.HashMap;
import java.util.Map;

public class ActionAdvice {
    private ActionState initialActionState;
    private Double leftLaneDesire;
    private Double leftLaneAcceleration;
    private Double keepLaneDesire;
    private Double keepLaneAcceleration;
    private Double rightLaneDesire;
    private Double rightLaneAcceleration;

    private Map<String, Object> dictAdvices;

    public ActionAdvice(
            final ActionState initialActionState,
            final Double leftLaneDesire,
            final Double leftLaneAcceleration,
            final Double keepLaneDesire,
            final Double keepLaneAcceleration,
            final Double rightLaneDesire,
            final Double rightLaneAcceleration
    ) {
        this.initialActionState = initialActionState;
        this.leftLaneDesire = leftLaneDesire;
        this.leftLaneAcceleration = leftLaneAcceleration;
        this.keepLaneDesire = keepLaneDesire;
        this.keepLaneAcceleration = keepLaneAcceleration;
        this.rightLaneDesire = rightLaneDesire;
        this.rightLaneAcceleration = rightLaneAcceleration;

        this.dictAdvices = new HashMap<>();
        this.dictAdvices.put("initial_action_state", initialActionState);
        this.dictAdvices.put("left_lane_desire", leftLaneDesire);
        this.dictAdvices.put("left_lane_acceleration", leftLaneAcceleration);
        this.dictAdvices.put("keep_lane_desire", keepLaneDesire);
        this.dictAdvices.put("keep_lane_acceleration", keepLaneAcceleration);
        this.dictAdvices.put("right_lane_desire", rightLaneDesire);
        this.dictAdvices.put("right_lane_acceleration", rightLaneAcceleration);
    }

    // Getter-Methoden (optional)
    public ActionState getInitialActionState() { return this.initialActionState; }
    public Double getLeftLaneDesire() { return this.leftLaneDesire; }
    public Double getLeftLaneAcceleration() { return this.leftLaneAcceleration; }
    public Double getKeepLaneDesire() { return this.keepLaneDesire; }
    public Double getKeepLaneAcceleration() { return this.keepLaneAcceleration; }
    public Double getRightLaneDesire() { return this.rightLaneDesire; }
    public Double getRightLaneAcceleration() { return this.rightLaneAcceleration; }
    public Map<String, Object> getDictAdvices() { return this.dictAdvices; }
}