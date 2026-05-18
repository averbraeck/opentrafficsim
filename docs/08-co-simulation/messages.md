# Messages
Messages define the information flow between OTS and the external simulator and the functionality that the OTS transceiver supports. All messages are defined as Java records in the `org.opentrafficsim.cosim.messages` packages. A Java `record` is a simple collection of objects with predefined types and names. This defines the functional contents of the messages that the OTS transceiver can receive and send. How these messages are technically communicated between OTS and the external simulator is up to the used protocol and interface implementations on both sides. This section discusses the message flow and the functional contents of the messages by describing the contents of the Java records.

## Message flow

Figure 8.2 shows a sequence diagram that gives an overview of the messages that can be sent at different stages of simulation.

![](https://www.plantuml.com/plantuml/png/dP5FImCn4CNl_HG3lUYXKB0KB2BLjg9W_z0jeiTXTzW6yoSairg_lLbBrDRiefwMJNxlpRoNiMTqN2dPiU4hSc5HComt6NJx_U5mCZWud-sOd4QP2GNTi_xvy6RGAPsfB8IJIY8Bey4JLxPJMrpq3XGZM9kAoS_9UomfZIoBER8JkrReWROYbnGPRPZ0YNBB8x2EUbPYJWMyx2d_uT6xFDw5i7XhdR8WVZNkEJMbvZsnXn3TVqR_nI3InS_6SgFP6GadZb2oK7IQNYYhtogrZgiqH6r3y-GfIl_RHyR6jW8hPqeNWAOLqQFzIqVndK7jR_n-xB4wOgtnLX9rqqkYBk3swNfzDmLbYePmyIzX4iMrp-nnCrilhkvRdERXfhN7YL4gU4Kc7NfAafY-TPRssf1JGkCNEGvefUGx)

_Figure 8.2: Overview of messages at different stages_

## Simulation setup

Simulation setup data (network, demand, routes) can be communicated to OTS in two ways:

 1. Using an OTS XML file.
 2. Using an OpenDRIVE network file with separate JSON files for demand (Origin-Destination matrix) and routes.

A hybrid approach is also possible, where JSON files are used as routes and demand additional to what is specified in an OTS XML file.

### Routes message

The routes message (`RoutesMessage`) defines available routes for vehicles and is sent from the external simulator to OTS. OTS responds with a ready message. Figure 8.3 shows the message flow. This message is only required if routes are not defined within the network message (i.e. this message is typically sent before a network message with an OpenDRIVE network).

![](https://www.plantuml.com/plantuml/png/SoWkIImgAStDuSfFoafDBb7m3mbMK3OmC3DpDe49kLQKf1Rb9UO4P-OgA1iRM9gvcN21bDdOwAB8MIa4vPUMf1RxfXON9wQdWjNesjcXgqbDJAc4AN58pKi1MGu0)

_Figure 8.3: Routes message flow._

The payload of the routes message is given in Table 8.1.

_Table 8.1: Payload of the routes message._

| Field      | Type         | Description                                  |
|------------|--------------|----------------------------------------------|
| routes     | `RoutesJson` | Direct object translation from a JSON String |
| responseId | `Object`     | Any object                                   |

The `RoutesJson` object can be directly created from a JSON string using GSON. An example JSON string is given below. It contains a list of route objects named `routes`. Each route has an `id` and a list of `objects`. A single object may also be defined. Either all objects in the route are listed, or only the first and last. In the latter case `shortest` must be defined as `true` and a shortest-path algorithm will be used to create the full route. The objects are referred to by their ID. Depending on the network used, this may refer either to nodes (OTS) or to roads (OpenDRIVE).

```json
{
    "routes": [
        {
            "id": "A-B",
            "objects": "0"
        },
        {
            "id": "A-C",
            "objects": ["0", "1"],
            "shortest": true
        }
    ]
}
```

!!! info "Using OpenDRIVE road names instead of IDs"

    In case road names are more convenient than their ID, setting `useRoadName` can be used on the transceiver. Using road names is only compatible with using shortest routes as the name is mostly ignored by OTS. Links in OTS receive an ID based on the OpenDRIVE ID as this is guaranteed to be unique. For roads without a previous or next road, their road name is coupled to the start or end node to function as origin or destination. Therefore a shortest route can be determined between them. It is up to the end-user to guarantee that road names of such roads are unique. 

The `responseId` can be any object that is returned with the corresponding ready message.

### OD-matrix message

The OD-matrix message (`OdMatrixMessage`) defines demand for background traffic and is sent from the external simulator to OTS. OTS responds with a ready message. Figure 8.4 shows the message flow. This message is only required if demand is not defined within the network message (i.e. this message is typically sent before a network message with an OpenDRIVE network).

![](https://www.plantuml.com/plantuml/png/SoWkIImgAStDuSfFoafDBb7m3mbMK3OmC3DpDe49kLQKf1Rb9UO4P-OgA1iRM9gvcN21bDdOwAB8MIduf_WcbXHbLlYc5XSdfgU2LUdQsQ6f2afDJAc4AN58pKi1MGy0)

_Figure 8.4: OD-matrix message flow._

The payload of the OD-matrix message is given in Table 8.2.

_Table 8.2: Payload of the OD-matrix message._

| Field      | Type           | Description                                  |
|------------|----------------|----------------------------------------------|
| odMatrix   | `OdMatrixJson` | Direct object translation from a JSON String |
| responseId | `Object`       | Any object                                   |


The `OdMatrixJson` object can be directly created from a JSON string using GSON. An example JSON string is given below. It contains:

 - `categorization` (_optional_) defines what objects combine to create a category for which demand is defined. This can be either "GTU_TYPE" (i.e. vehicle type) or "ROUTE", or a list of both.
 - `globalTime` (_optional_) defines a global time array for which demand is defined.
 - `globalInterpolation` (_optional_) defines how demand is interpolated over time. This can be either "STEPWISE" or "LINEAR".
 - `demand` a list of demand objects, each containing:
     - `origin` ID of the origin object (node or road depending on the network).
     - `destination` ID of the destination object (node or road depending on the network).
     - `category` (_optional_) IDs of objects defining the category of this demand, can be a single value or a list for the GTU type and route.
     - `time` (_optional_) time array for which demand is defined. This can be either a single value or a list.
     - `frequency` demand in vehicles per time unit. This can be either a single value or a list.
     - `interpolation` (_optional_) interpolation over time for this demand. This can be either "STEPWISE" or "LINEAR".

```json
{
    "categorization": [
        "GTU_TYPE",
        "ROUTE"
    ],
    "globalTime": [
        "0s",
        "1500s",
        "3600s"
    ],
    "globalInterpolation": "STEPWISE",
    "demand": [
        {
            "origin": "A",
            "destination": "B",
            "category": [
                "NL.CAR",
                "A-B"
            ],
            "time": [
                "0s",
                "1800s",
                "3600s"
            ],
            "frequency": [
                "1200/h",
                "1900/h",
                "500/h"
            ],
            "interpolation": "LINEAR"
        }
    ]
}
```

The `responseId` can be any object that is returned with the corresponding ready message.

### Network message

The network message (`NetworkMessage`) defines the network (and possibly the routes and demand) and is sent from the external simulator to OTS. OTS responds with a ready message. Figure 8.5 shows the message flow. This message triggers OTS to setup the complete simulation. 

!!! info "Setup message order"

    This message should be sent after a possible routes and OD-matrix message.

![](https://www.plantuml.com/plantuml/png/SoWkIImgAStDuSfFoafDBb7m3mbMK3OmC3DpDe49kLQKf1Rb9UO4P-OgA1iRM9gvcN21bDdOwAB8MIdufPQKvnTb-wOM5oUcfu8LwThPeIaAIarCgOGfSKZDIm7P3W00)

_Figure 8.5: Network message flow._

The payload of the network message is given in Table 8.3.

_Table 8.3: Payload of the network message._

| Field      | Type                         | Description                                |
|------------|------------------------------|--------------------------------------------|
| type       | `NetworkMessage.NetworkType` | OTS or OPENDRIVE                           |
| network    | `String`                     | String content of an OTS or OpenDRIVE file |
| responseId | `Object`                     | Any object                                 |

!!! info "OpenDRIVE compatibility"
    
    The OTS transceiver allows specification of the network in the OpenDRIVE format, but the full OpenDRIVE standard is not supported and also not compatible. The following limitations apply:

    - Links are not allowed to overlap.
    - Road discontinuities (lane sections, road type change, road mark change, lane access change, lane speed change) are not allowed in parts of lanes that should become a conflict with other lanes. Conflicts are when lanes from different links split, merge or cross, i.e. have overlap.
    - Signals, including speed signs, are ignored. Lane and road type speed limits should be used.

### Ready message

The ready message (`ReadyMessage`) is sent by OTS to the external simulator as a response to appropriate messages. These are the above setup messages, but also the reset, progress, and some vehicle messages. The payload of the network message is given in Table 8.4.

_Table 8.4: Payload of the ready message._

| Field      | Type     | Description                                             |
|------------|----------|---------------------------------------------------------|
| responseId | `Object` | Object from the message of which the processing is done |

## Simulation control

The OTS transceiver can run OTS at real-time, or step-by-step and as fast as possible. This is controlled by the external simulator by sending the right messages.

### Start message

The start message (`StartMessage`) starts a real-time simulation and is sent from the external simulator to OTS. Figure 8.6 shows the message flow. There is no payload. Any previous simulation is stopped and cleaned-up.

![](https://www.plantuml.com/plantuml/png/SoWkIImgAStDuSfFoafDBb7m3mbMK3OmC3DpDe49kLQKf1Rb9UO4P-OgA1iRM9gvcN21bDdOwAB8MIa4WmppJIqkJanFvN98pKi16Gm0)

_Figure 8.6: Start message flow._

### Stop message

The stop message (`StopMessage`) stops any simulation and is sent from the external simulator to OTS. Figure 8.7 shows the message flow. There is no payload. The stopped simulation is cleaned-up.

![](https://www.plantuml.com/plantuml/png/SoWkIImgAStDuSfFoafDBb7m3mbMK3OmC3DpDe49kLQKf1Rb9UO4P-OgA1iRM9gvcN21bDdOwAB8MIa4b-GN-AOM5oUcft8vfEQbWCm50000)

_Figure 8.7: Stop message flow._

### Reset message

The reset message (`ResetMessage`) resets any simulation and is sent from the external simulator to OTS. OTS responds with a ready message. Figure 8.8 shows the message flow. Resetting entails stopping any simulation and performing a setup based on the last sent setup messages (routes, OD-matrix and network).

![](https://www.plantuml.com/plantuml/png/SoWkIImgAStDuSfFoafDBb7m3mbMK3OmC3DpDe49kLQKf1Rb9UO4P-OgA1iRM9gvcN21bDdOwAB8MIaeAGIb5gUM-AOM5oUcfoeeWPNfsjcXAGjAJKofX2hYSaZDIm7P3m00)

_Figure 8.8: Reset message flow._

The payload of the reset message is given in Table 8.5.

_Table 8.5: Payload of the reset message._

| Field      | Type     | Description |
|------------|----------|-------------|
| responseId | `Object` | Any object  |

### Progress message

The progress message (`ProgressMessage`) tells OTS to run up to a given time as fast as possible. OTS responds with a ready message. Figure 8.9 shows the message flow. This message can be used to skip time, or perform a step-based simulation control by the external simulator.

![](https://www.plantuml.com/plantuml/png/SoWkIImgAStDuSfFoafDBb7m3mbMK3OmC3DpDe49kLQKf1Rb9UO4P-OgA1iRM9gvcN21bDdOwAB8MIa45EMd5wKM5_i2SM9wAbYLhfqTcggWrCIKIfWIXzIy593r0000)

_Figure 8.9: Progress message flow._

The payload of the progress message is given in Table 8.6.

_Table 8.6: Payload of the progress message._

| Field      | Type       | Description                  |
|------------|------------|------------------------------|
| untilTime  | `Duration` | Time until which to simulate |
| responseId | `Object`   | Any object                   |

### Terminate message

The terminate message (`TerminateMessage`) stops the whole OTS transceiver. It is sent from the external simulator to OTS. Figure 8.10 shows the message flow. There is no payload.

![](https://www.plantuml.com/plantuml/png/SoWkIImgAStDuSfFoafDBb7m3mbMK3OmC3DpDe49kLQKf1Rb9UO4P-OgA1iRM9gvcN21bDdOwAB8MIc4f1RbPkObbgJwfXON9wQdSZcavgM030S0)

_Figure 8.10: Terminate message flow._

## Vehicle messages

Vehicle messages are about generating, controlling, and informing on vehicles in simulation. Vehicles can be controlled in three different modes as depicted in Table 8.7. The control mode can be changed during simulation.

_Table 8.7: Vehicle control modes for co-simulation._

| Mode     | Plan message     | External message | Description                                                   |
|----------|------------------|------------------|---------------------------------------------------------------|
| OTS      | Every model step |                  | OTS fully controls the vehicle and informs external simulator |
| HYBRID   | Every model step | Frequent         | As external, plan message as a guide to external simulator    |
| EXTERNAL |                  | Frequent         | External simulator fully controls the vehicle and informs OTS |

All messages related to vehicles contain a time stamp. They occur during simulation and either OTS or the external simulator can use the time stamp to determine the appropriate action, for example in [dead reckoning](dead-reckoning.md). There are two exceptions:

- Vehicle messages that are sent before the simulation is started (pre-placed vehicles) have a time stamp, but this is ignored.
- Command messages do not have a time stamp, but the command in the payload has a time stamp at which the command should be given.

### Vehicle message

The vehicle message (`VehicleMessage`) informs about a vehicle being generated and is either sent from OTS to the external simulator or from the external simulator to OTS. OTS will sent these messages for background traffic it generated from demand data. If OTS receives a vehicle message, OTS responds with a ready message _if the simulation has not yet started_. In this way the external simulator can know when OTS is ready to simulate. Figure 8.11 shows the message flow.

![](https://www.plantuml.com/plantuml/png/SoWkIImgAStDuSfFoafDBb7m3mbMK3OmC3DpDe49kLQKf1Rb9UO4P-OgA1iRM9gvcN21bDdOwDhPeSXQAOIbPcGcvwJwfXON9wQdWfLXAWfAJKofX2bnICrB0TeF0000)

_Figure 8.11: Vehicle message flow._

The payload of the vehicle message is given in Table 8.8.

_Table 8.8: Payload of the vehicle message._

| Field       | Type                  | Description                         |
|-------------|-----------------------|-------------------------------------|
| time        | `Duration`            | Duration since start of simulation  |
| vehicleId   | `String`              | Unique vehicle ID                   |
| controlMode | `ControlMode`         | OTS, HYBRID or EXTERNAL             |
| xCoordinate | `Length`              | Initial x-coordinate                |
| yCoordinate | `Length`              | Initial y-coordinate                |
| direction   | `Direction`           | Initial direction                   |
| speed       | `Speed`               | Initial speed                       |
| type        | `VehicleType`         | CAR or TRUCK                        |
| length      | `Length`              | Vehicle length                      |
| width       | `Length`              | Vehicle width                       |
| refToNose   | `Length`              | Distance from reference to nose     |
| parameters  | `Map<String, Object>` | Map of parameters (see [Settings and parameters](settings-parameters.md)) |
| route       | `String`              | ID of route as defined during setup |
| responseId  | `Object`              | Any object                          |

OTS will ignore the time stamp for pre-placed vehicles, i.e. vehicle messages before the simulation is started. OTS will send no parameter values for generated vehicles (background traffic). The parameters field is intended for external control over parameter values and model components.

### Plan message

The plan message (`PlanMessage`) informs the external simulator on the model results of a vehicle and is sent from OTS to the external simulator every model step for vehicles in OTS or HYBRID mode. Figure 8.12 shows the message flow.

![](https://www.plantuml.com/plantuml/png/SoWkIImgAStDuSfFoafDBb7m3mbMK3OmC3DpDe49kLQKf1Rb9UO4P-OgA1iRM9gvcN21bEdQsQ78MIa4v2Jc-QOM5oUcft8vfEQb0Cq50000)

_Figure 8.12: Plan message flow._

The payload of the plan message is given in Table 8.9.

_Table 8.9: Payload of the plan message._

| Field         | Type                      | Description                          |
|---------------|---------------------------|--------------------------------------|
| time          | `Duration`                | Duration since start of simulation   |
| vehicleId     | `String`                  | Vehicle ID                           |
| speed         | `Speed`                   | Start speed of the plan              |
| xCoordinates  | `FloatLengthVector`       | X-coordinates of the path            |
| yCoordinates  | `FloatLengthVector`       | Y-coordinates of the path            |
| steps         | `FloatDurationVector`     | Duration of the acceleration steps   |
| acceleration  | `FloatAccelerationVector` | Acceleration steps (usually 1 value) |
| turnIndicator | `TurnIndicatorStatus`     | NONE, LEFT, RIGHT or HAZARD          |

### External message

The external message (`ExternalMessage`) informs OTS on the kinematic state of an externally controlled vehicle and is sent from the external simulator to OTS. Figure 8.13 shows the message flow. OTS will perform [dead reckoning](dead-reckoning.md) with the information. For consistency this message is expected to be sent at a relatively high frequency (compared to the model time step), such as 20Hz.

![](https://www.plantuml.com/plantuml/png/SoWkIImgAStDuSfFoafDBb7m3mbMK3OmC3DpDe49kLQKf1Rb9UO4P-OgA1iRM9gvcN21bDdOwAB8MS5vlgd5nOdfgLmEgNafGBC1)

_Figure 8.13: External message flow._

The payload of the external message is given in Table 8.10.

_Table 8.10: Payload of the external message._

| Field        | Type           | Description                        |
|--------------|----------------|------------------------------------|
| time         | `Duration`     | Duration since start of simulation |
| vehicleId    | `String`       | Vehicle ID                         |
| xCoordinate  | `Length`       | X-coordinate                       |
| yCoordinate  | `Length`       | Y-coordinate                       |
| direction    | `Direction`    | Direction                          |
| speed        | `Speed`        | Speed                              |
| acceleration | `Acceleration` | Acceleration                       |

### Mode message

The mode message (`ModeMessage`) allows a change of control mode and is sent from the external simulator to OTS. Figure 8.14 shows the message flow.

![](https://www.plantuml.com/plantuml/png/SoWkIImgAStDuSfFoafDBb7m3mbMK3OmC3DpDe49kLQKf1Rb9UO4P-OgA1iRM9gvcN21bDdOwAB8MIduvgUa-gOM5oUcft8vfEQb0Cq50000)

_Figure 8.14: Mode message flow._

The payload of the mode message is given in Table 8.11.

_Table 8.11: Payload of the mode message._

| Field       | Type          | Description                        |
|-------------|---------------|------------------------------------|
| time        | `Duration`    | Duration since start of simulation |
| vehicleId   | `String`      | Vehicle ID                         |
| controlMode | `ControlMode` | OTS, HYBRID or EXTERNAL            |

### Command message

The command message (`CommandMessage`) allows the external simulator to control specific outputs of the OTS model. The intent of this is a high level of control for experiments, while a vehicle still behaves and interacts with surrounding vehicles through the OTS model mostly. Figure 8.15 shows the message flow.

![](https://www.plantuml.com/plantuml/png/SoWkIImgAStDuSfFoafDBb7m3mbMK3OmC3DpDe49kLQKf1Rb9UO4P-OgA1iRM9gvcN21bDdOwAB8MIauv-Vc9kQb-AOM5oUcft8vfEQb08q60000)

_Figure 8.15: Command message flow._

The payload of the command message is given in Table 8.12.

_Table 8.12: Payload of the command message._

| Field     | Type       | Description                                    |
|-----------|------------|------------------------------------------------|
| vehicleId | `String`   | Vehicle ID                                     |
| command   | `Command`  | Various commands (see [Commands](commands.md)) |

The `Command` object can be directly created from a JSON String. For example for a _setParameter_ command with payload to set parameter '_a_' to 1.84m/s<sup>2</sup>:

```json
{
    "time": "1845.4s",
    "type": "setParameter",
    "data": {
        "parameter": "a",
        "value": "1.84m/s2"
    }
}
```

### Delete message

The delete message (`DeleteMessage`) informs either the external simulator or OTS that a vehicle has been deleted and is sent from which ever simulator deletes a vehicle. Figure 8.16 shows the message flow.

![](https://www.plantuml.com/plantuml/png/SoWkIImgAStDuSfFoafDBb7m3mbMK3OmC3DpDe49kLQKf1Rb9UO4P-OgA1iRM9gvcN21bDdOwDhPeSXQARYavgIMfFgc5XSdfgToEQJcfG3D1W00)

_Figure 8.16: Delete message flow._

The payload of the delete message is given in Table 8.13.

_Table 8.13: Payload of the delete message._

| Field     | Type       | Description                        |
|-----------|------------|------------------------------------|
| time      | `Duration` | Duration since start of simulation |
| vehicleId | `String`   | Vehicle ID                         |
