package org.opentrafficsim.cosim.messages;

import java.util.Map;

import org.djunits.value.vdouble.scalar.Duration;
import org.djutils.base.Identifiable;
import org.djutils.exceptions.Throw;

/**
 * Scenario command sent from ExternalSim to OTS.
 * <p>
 * Copyright (c) 2026-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 * @param vehicleId vehicle ID
 * @param command command
 */
public record CommandMessage(String vehicleId, Command command) implements Identifiable
{

    /** Message type ID. */
    public static final String ID = "COMMAND";

    /**
     * Constructor.
     */
    public CommandMessage
    {
        Throw.whenNull(vehicleId, "vehicleId");
        Throw.whenNull(command, "command");
    }

    /**
     * Class containing information of a command.
     * @param time duration since the start of simulation at which the command should be given
     * @param type type of the command
     * @param data data for the command
     */
    public record Command(Duration time, CommandType type, Map<String, String> data)
    {

        /**
         * Returns the data under the name of the given field.
         * @param field name of the data field
         * @return value of the data
         * @throws NoSuchFieldException if no field is present under the given name
         */
        public String getData(final String field) throws NoSuchFieldException
        {
            Throw.when(this.data == null || !this.data.containsKey(field), NoSuchFieldException.class, "No field %s.", field);
            return this.data.get(field);
        }

    }

    /**
     * Types of commands that can be given.
     */
    public enum CommandType
    {

        /** Sets a parameter value. Data: {"parameter": parameter ID, "value": parameter value possibly with unit}. */
        SET_PARAMETER("setParameter"),

        /** Sets desired speed. Data: {"speed": speed value with unit}. */
        SET_DESIRED_SPEED("setDesiredSpeed"),

        /** Resets the desired speed to regular operation. Data: none. */
        RESET_DESIRED_SPEED("resetDesiredSpeed"),

        /** Sets the acceleration. Data {"acceleration": acceleration value with unit}. */
        SET_ACCELERATION("setAcceleration"),

        /** Resets the acceleration to regular operation. Data: none. */
        RESET_ACCELERATION("resetAcceleration"),

        /** Disables lane changes. Data: none. */
        DISABLE_LANE_CHANGES("disableLaneChanges"),

        /** Resets the lane change behavior to regular operations. Data: none. */
        ENABLE_LANE_CHANGES("enableLaneChanges"),

        /** Initiates a lane change. Data: {"direction": LEFT or RIGHT}. */
        CHANGE_LANE("changeLane"),

        /** Set indicator. Data: {"direction": LEFT or RIGHT, "duration": duration value with unit}. */
        SET_INDICATOR("setIndicator");

        /** Print value for in JSON file. */
        private String printValue;

        /**
         * Constructor.
         * @param printValue print value for in JSON file.
         */
        CommandType(final String printValue)
        {
            this.printValue = printValue;
        }

        /**
         * Returns the print value for in a JSON file.
         * @return print value for in a JSON file.
         */
        public String printValue()
        {
            return this.printValue;
        }

    }

    @Override
    public String getId()
    {
        return ID;
    }

}
