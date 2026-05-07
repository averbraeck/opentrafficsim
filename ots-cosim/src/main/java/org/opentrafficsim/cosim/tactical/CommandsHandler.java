package org.opentrafficsim.cosim.tactical;

import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Speed;
import org.djutils.exceptions.Throw;
import org.djutils.exceptions.Try;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.cosim.messages.CommandMessage.Command;
import org.opentrafficsim.road.gtu.LaneBasedGtu;
import org.opentrafficsim.road.network.RoadNetwork;

/**
 * This class is responsible for handling the commands that are given to a GTU. One handler should be created per GTU.
 * <p>
 * Copyright (c) 2026-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author Wouter Schakel
 */
public class CommandsHandler
{

    /** Network. */
    private final RoadNetwork network;

    /** GTU id. */
    private final String gtuId;

    /** GTU. */
    private LaneBasedGtu gtu;

    /**
     * Constructor.
     * @param network network
     * @param gtuId GTU id
     */
    public CommandsHandler(final RoadNetwork network, final String gtuId)
    {
        this.network = Throw.whenNull(network, "network");
        this.gtuId = Throw.whenNull(gtuId, "gtuId");
    }

    /**
     * Schedules command. If the time is in the past or now (except at time=0), the command is executed immediately.
     * @param command command
     */
    public void scheduleCommand(final Command command)
    {
        if (command.time().le(this.network.getSimulator().getSimulatorTime()) && !command.time().eq0())
        {
            executeCommand(command);
        }
        else
        {
            this.network.getSimulator().scheduleEventAbs(command.time(), this, "executeCommand", new Object[] {command});
        }
    }

    /**
     * Executes a command immediately.
     * @param command command
     */
    public void executeCommand(final Command command)
    {
        switch (command.type())
        {
            case SET_PARAMETER:
                String parameter =
                        Try.assign(() -> command.getData("parameter"), "Field 'parameter' not found for setParameter command.");
                String value = Try.assign(() -> command.getData("value"), "Field 'value' not found for setParameter command.");
                Try.execute(() -> getTacticalPlanner().setParameter(parameter, value),
                        "Parameter value %s for parameter %s is not valid.", value, parameter);
                break;
            case SET_DESIRED_SPEED:
                Speed speed = Speed.valueOf(
                        Try.assign(() -> command.getData("speed"), "Field 'speed' not found for setDesiredSpeed command."));
                getTacticalPlanner().setDesiredSpeed(speed);
                break;
            case RESET_DESIRED_SPEED:
                getTacticalPlanner().resetDesiredSpeed();
                break;
            case SET_ACCELERATION:
                Acceleration acceleration = Acceleration.valueOf(Try.assign(() -> command.getData("acceleration"),
                        "Field 'acceleration' not found for setAcceleration command."));
                getTacticalPlanner().setAcceleration(acceleration);
                break;
            case RESET_ACCELERATION:
                getTacticalPlanner().resetAcceleration();
                break;
            case DISABLE_LANE_CHANGES:
                getTacticalPlanner().disableLaneChanges();
                break;
            case ENABLE_LANE_CHANGES:
                getTacticalPlanner().enableLaneChanges();
                break;
            case CHANGE_LANE:
                LateralDirectionality laneChangeDirection = LateralDirectionality.valueOf(
                        Try.assign(() -> command.getData("direction"), "Field 'direction' not found for changeLane command."));
                getTacticalPlanner().changeLane(laneChangeDirection);
                break;
            case SET_INDICATOR:
                LateralDirectionality indicator = LateralDirectionality.valueOf(Try.assign(() -> command.getData("direction"),
                        "Field 'direction' not found for setIndicator command."));
                Duration duration =
                        Duration.valueOf(Try.assign(() -> command.getData("duration"), "Field 'duration' not found."));
                getTacticalPlanner().setIndicator(indicator, duration);
                break;
            default:
                throw new RuntimeException("Unknown command type " + command.type());
        }
    }

    /**
     * Returns the scenario based tactical planner of the GTU.
     * @return scenario based tactical planner of the GTU
     */
    private ScenarioTacticalPlanner getTacticalPlanner()
    {
        if (this.gtu == null)
        {
            this.gtu = (LaneBasedGtu) this.network.getGTU(this.gtuId).orElseThrow(() -> new IllegalStateException("GTU "
                    + this.gtuId
                    + " could not be found. Likely commands are defined before the GTU is spawned or after it is removed."));
        }
        return (ScenarioTacticalPlanner) this.gtu.getTacticalPlanner();
    }

    @Override
    public String toString()
    {
        return "CommandsHandler [gtuId=" + this.gtuId + "]";
    }



}
