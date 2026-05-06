# Commands

Commands can be sent to individual vehicles controlled by OTS or in hybrid mode in order to apply specific influence on the behaviour of vehicles. The intention of this is to obtain controlled repeated experiments that comply to specific needs regarding the investigated scenario. Commands are defined under the class `CommandMessage.Command` and contain the data as given in Table 8.19.

_Table 8.19: Command data._

| Attribute | Type                  | Description                                   |
|-----------|-----------------------|-----------------------------------------------|
| time      | `Duration`            | Simulation time at which to apply the command |
| type      | `CommandType`         | Defines what command is given                 |
| data      | `Map<String, String>` | Payload that is specific to the command type  |

!!! info "Time of the command"

    If the time is in the past, the command will be execute immediately when a command message is received. If the time is in the future, the command is scheduled to be executed at that time.

!!! info "Payload values"

    The values of key-value pairs in the command data are always of type `String`. See examples below for example values pertaining to specific keys of specific commands. Using `String` for the values allows commands to for example be stored as JSON within a scenario for the external simulator. 

The following sections describe the different commands and what is included in the payload.

## setParameter

This command changes a parameter value on a vehicle. The payload is given in Table 8.20.

_Table 8.20: Payload for the setParameter command._

| Key       | Description                                                          |
|-----------|----------------------------------------------------------------------|
| parameter | Parameter ID (see [Settings and parameters](settings-parameters.md)) |
| value     | String representation of the value, possibly with unit               |

An example of the data is: `Map.of("parameter", "a", "value", "1.84m/s2")`.

## setDesiredSpeed

This command sets the desired speed on a vehicle. The payload is given in Table 8.21.

_Table 8.21: Payload for the setDesiredSpeed command._

| Key   | Description     |
|-------|-----------------|
| speed | Speed with unit |

An example of the data is: `Map.of("speed", "48.3km/h")`.

## resetDesiredSpeed

This command resets the desired speed to normal behavior as per the model and parameter values. There is no payload.

## setAcceleration

This command sets the acceleration on a vehicle. The payload is given in Table 8.22.

_Table 8.22: Payload for the setAcceleration command._

| Key          | Description            |
|--------------|------------------------|
| acceleration | Acceleration with unit |

An example of the data is: `Map.of("acceleration", "-1.74m/s2")`.

## resetAcceleration

This command resets the acceleration to normal behavior as per the model and parameter values. There is no payload.

## disableLaneChanges

This command disables lane changes on a vehicle, overruling the model. There is no payload.

## enableLaneChanges

This command enables lane changes on a vehicle. There is no payload.

## changeLane

This command triggers a lane change on a vehicle. The payload is given in Table 8.23.

_Table 8.23: Payload for the changeLane command._

| Key       | Description          |
|-----------|----------------------|
| direction | Either LEFT or RIGHT |

An example of the data is: `Map.of("direction", "RIGHT")`.

## setIndicator

This command sets the indicator of a vehicles, overruling the model. The payload is given in Table 8.24.

_Table 8.24: Payload for the setIndicator command._

| Key       | Description                     |
|-----------|---------------------------------|
| direction | Either LEFT or RIGHT            |
| duration  | Duration of indicator with unit |

An example of the data is: `Map.of("direction", "LEFT", "duration", "5.6s")`.