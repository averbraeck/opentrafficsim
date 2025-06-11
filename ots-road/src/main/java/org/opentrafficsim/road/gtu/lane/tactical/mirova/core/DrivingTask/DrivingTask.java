package org.opentrafficsim.road.gtu.lane.tactical.mirova.core.DrivingTask;

import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.AbstractMirovaVehicle;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.ActionAdvice;

public abstract class DrivingTask {
    /**
     * Abstract base class representing a driving task in a traffic simulation.
     *
     * This class serves as the foundation for implementing specific driving tasks, such as
     * lane changes, merging, or cooperative behavior. It provides the structure for evaluating
     * the activation of a task, calculating its utility, and deciding on maneuver advice.
     *
     * Attributes:
     * - contextVehicle: The vehicle associated with this driving task.
     * - latestUtility: The most recent utility value calculated for this task.
     */

    protected AbstractMirovaVehicle AbstractMirovaVehicle;
    protected Double latestUtility;

    public DrivingTask(final AbstractMirovaVehicle AbstractMirovaVehicle) {
        this.AbstractMirovaVehicle = AbstractMirovaVehicle;
        this.latestUtility = null;
    }

    /**
     * Determines whether the driving task should be activated.
     * If the utility of the task (calculated using calculateUtility) is greater
     * than or equal to 100, the task is activated.
     *
     * @return true if the task should be activated, false otherwise.
     */
    public boolean getActivation() {
        return calculateUtility() >= 100;
    }

    /**
     * Calculates the current utility of the driving task.
     * The specific utility function should be implemented in subclasses.
     *
     * @return the latest utility value calculated for the task.
     */
    public abstract double calculateUtility();

    /**
     * Executes the decision tree to determine the appropriate maneuver advice.
     * The decision tree should be implemented in subclasses to reflect the specific
     * logic of the driving task.
     *
     * @return the advice for the next maneuver.
     */
    public abstract ActionAdvice decideManeuverAdvice();

    public AbstractMirovaVehicle getAbstractMirovaVehicle() {
        return this.AbstractMirovaVehicle;
    }
}