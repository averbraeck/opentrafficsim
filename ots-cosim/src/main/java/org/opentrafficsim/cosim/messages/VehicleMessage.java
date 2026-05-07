package org.opentrafficsim.cosim.messages;

import java.util.Map;

import org.djunits.value.vdouble.scalar.Direction;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djutils.base.Identifiable;
import org.djutils.exceptions.Throw;
import org.opentrafficsim.cosim.messages.ModeMessage.ControlMode;

/**
 * Vehicle message which is sent from OTS to ExternalSim or from ExternalSim to OTS for every generated vehicle in simulation.
 * OTS will send no parameter values with this message.
 * <p>
 * Copyright (c) 2026-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author Wouter Schakel
 * @param time duration since start of simulation
 * @param vehicleId vehicle ID
 * @param controlMode vehicle control mode
 * @param xCoordinate x-coordinate
 * @param yCoordinate y-coordinate
 * @param direction vehicle direction
 * @param speed vehicle speed
 * @param type vehicle type
 * @param length vehicle length
 * @param width vehicle width
 * @param refToNose distance from reference point (for which x and y are given) on the vehicle center line and the nose of the
 *            vehicle, which may be {@code null} in which case 75% of the length is used
 * @param parameters parameters, which may be an empty map
 * @param route route ID, as defined in {@link RoutesMessage} or {@link NetworkMessage} using an OTS file
 * @param responseId response ID for in the ready message
 */
public record VehicleMessage(Duration time, String vehicleId, ControlMode controlMode, Length xCoordinate, Length yCoordinate,
        Direction direction, Speed speed, VehicleType type, Length length, Length width, Length refToNose,
        Map<String, Object> parameters, String route, Object responseId) implements Identifiable
{

    /** Message type ID. */
    public static final String ID = "VEHICLE";

    /**
     * Constructor.
     * @param time time stamp
     * @param vehicleId vehicle ID
     * @param controlMode vehicle control mode
     * @param xCoordinate x-coordinate
     * @param yCoordinate y-coordinate
     * @param direction vehicle direction
     * @param speed vehicle speed
     * @param type vehicle type
     * @param length vehicle length
     * @param width vehicle width
     * @param refToNose distance from reference point (for which x and y are given) on the vehicle center line and the nose of
     *            the vehicle, which may be {@code null} in which case 75% of the length is used
     * @param parameters parameters, which may be an empty map
     * @param route route ID, as defined in {@link RoutesMessage} or {@link NetworkMessage} using an OTS file
     * @param responseId response ID for in the ready message
     */
    @SuppressWarnings("parameternumber")
    public VehicleMessage(final Duration time, final String vehicleId, final ControlMode controlMode, final Length xCoordinate,
            final Length yCoordinate, final Direction direction, final Speed speed, final VehicleType type, final Length length,
            final Length width, final Length refToNose, final Map<String, Object> parameters, final String route,
            final Object responseId)
    {
        this.time = Throw.whenNull(time, "time");
        Throw.when(time.lt0(), IllegalArgumentException.class, "Time should not be negative.");
        this.vehicleId = Throw.whenNull(vehicleId, "vehicleId");
        this.controlMode = Throw.whenNull(controlMode, "controlMode");
        this.xCoordinate = Throw.whenNull(xCoordinate, "xCoordinate");
        this.yCoordinate = Throw.whenNull(yCoordinate, "yCoordinate");
        this.direction = Throw.whenNull(direction, "direction");
        this.speed = Throw.whenNull(speed, "speed");
        this.type = Throw.whenNull(type, "type");
        this.length = Throw.whenNull(length, "length");
        Throw.when(length.le0(), IllegalArgumentException.class, "Length must be above zero.");
        this.width = Throw.whenNull(width, "width");
        Throw.when(width.le0(), IllegalArgumentException.class, "Width must be above zero.");
        this.refToNose = refToNose == null ? length.times(0.75) : refToNose;
        this.parameters = Throw.whenNull(parameters, "parameters");
        this.route = route;
        this.responseId = Throw.whenNull(responseId, "responseId");
    }

    /**
     * Vehicle type.
     */
    public enum VehicleType
    {
        /** Car. */
        CAR,

        /** Truck. */
        TRUCK;
    }

    @Override
    public String getId()
    {
        return ID;
    }

}
