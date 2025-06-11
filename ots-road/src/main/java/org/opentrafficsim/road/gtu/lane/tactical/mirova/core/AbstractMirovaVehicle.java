package org.opentrafficsim.road.gtu.lane.tactical.mirova.core;

import org.opentrafficsim.road.gtu.lane.tactical.mirova.VotingArbiter.*;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.DrivingTask.DrivingTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractMirovaVehicle
{
    protected final VotingArbiter votingArbiter;

    protected final Map<String, DrivingTask> dictDrivingTasks;

    protected List<DrivingTask> listActiveDrivingTasks;

    protected boolean runningManeuver = false;

    protected ActionState currentActionState = null;

    protected double updatedAcceleration = Double.NaN;

    // Konstruktor
    public AbstractMirovaVehicle() {
        this.votingArbiter = new VotingArbiter();
        this.dictDrivingTasks = new HashMap<>();
        initializeDrivingTasks();
        this.listActiveDrivingTasks = new ArrayList<>();
    }

    public void update() {
        this.updatedAcceleration = Double.NaN;

        // Blockiere VISSIM LaneChange
        // contextDriverDevice.getVissimVehicle().SetAttValue("DESLANE", getCurrentLaneId());

        if (this.runningManeuver && this.currentActionState != null) {
            this.currentActionState.update();
        } else {
            updateActiveDrivingTasks();
            List<ActionAdvice> listActionAdvices = new ArrayList<>();

            for (DrivingTask task : this.listActiveDrivingTasks) {
                ActionAdvice advice = task.decideManeuverAdvice();
                if (advice != null) {
                    listActionAdvices.add(advice);
                }
            }

            if (!listActionAdvices.isEmpty()) {
                this.currentActionState = this.votingArbiter.execute(listActionAdvices);
                if (this.currentActionState != null) {
                    this.currentActionState.update();
                }
            } else {
//                getGtu(); // set attribute "ActionState" to null
            }
        }

    }

    public void updateActiveDrivingTasks() {
        this.listActiveDrivingTasks.clear();
        for (Map.Entry<String, DrivingTask> entry : this.dictDrivingTasks.entrySet()) {
            if (entry.getValue().getActivation()) {
                this.listActiveDrivingTasks.add(entry.getValue());
            }
        }
    }

    protected abstract void initializeDrivingTasks();

 // Getter und Setter für runningManeuver
    public boolean isRunningManeuver() {
        return this.runningManeuver;
    }

    public void setRunningManeuver(final boolean runningManeuver) {
        this.runningManeuver = runningManeuver;
    }

    // Getter und Setter für currentActionState
    public ActionState getCurrentActionState() {
        return this.currentActionState;
    }

    public void setCurrentActionState(final ActionState currentActionState) {
        this.currentActionState = currentActionState;
    }

}
