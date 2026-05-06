# Protocol

As described at the start of this chapter, the protocol defines the technical implementation of how OTS and the external simulator work together and interchange information. Functions of the OTS transceiver are defined in the class `AbstractOtsTransceiver`. Messages that are sent back and forth are defined as classes in the package `org.opentrafficsim.cosim.messages`. The protocol sends this information in some form that the other application understands. 

The next sections describe the default protocol using sim0mq, and relevant information for implementing a different protocol.

## Sim0MQ

The class `OtsTransceiverSim0mq` implements a protocol using [Sim0MQ](https://sim0mq.org/manual/) with standard (de)serialization and TCP communication over a PAIR socket. Libraries in Java and Python exist for (de)serialization at the side of the external simulator. The external simulator needs to implement the interaction with the messages sent over the protocol.

Data sent by sim0mq contains meta information additional to the payload. Details on this can be found in the sim0mq documentation. Relevant remarks for `OtsTransceiverSim0mq` are:

- Federation, sender and receiver IDs are `String`. They can be specified using command line settings. By default they are `Ots_ExternalSim`, `Ots` and `ExternalSim`. Note that both sender and receiver can be the OTS ID or the ID for the external simulator.
- Message IDs are `String`, specifically: ROUTES, ODMATRIX, NETWORK, READY, START, STOP, RESET, PROGRESS, TERMINATE, VEHICLE, PLAN, EXTERNAL, MODE, COMMAND and DELETE.
- The routes, OD-matrix and command messages contain fields of types `RoutesJson`, `OdMatrixJson` and `CommandMessage.Command`. These should be sent to OTS as JSON `String`. See [Messages](messages.md) for examples.
- In the _vehicle message_ the _parameters_ field is defined as a `Map<String, Object>`. OTS expects this to be sent as a number of objects equal to 1 + 2*_n_ in series, where _n_ is the number of parameters. First, an `int` is sent that gives the value of _n_. After that the parameters are sent as key (`String`) and object pairs. The objects are of a type dependent on the parameter. See [Settings and parameters](settings-parameters.md) for more details.
- For all messages that contain a field _responseId_, `OtsTransceiverSim0mq` will use the _Message id_ (frame 6) from the sim0mq message meta information. No value in the payload itself is used for this. This value can be of any type that sim0mq supports for the message ID. OTS will return the value in the same type.

## Custom protocol

A custom protocol can be used. This requires implementation at both the OTS side and the side of the external simulator. The only thing that the OTS transceiver specifies is the use of message objects as defined in the package `org.opentrafficsim.cosim.messages`. This structure is depicted in Figure 8.16.

![](https://www.plantuml.com/plantuml/png/PL1B2i8m4Dtd54DS2xLAGIYb80ekHA6zG8oZLDD4abG4yUwcz9EeksJUc_UPt5YcRLCBScVynYu8T5yM52PHb2Ih1Rm8W3fUaLieDPE6O_L0xOSxDCPj6J_PIelwp3ZEW1cWuvSwIY01QJhDid1_UFQ8ro5lGdhnWrPMSIL8uDBHHyGNMJyTHp9HLBKhD8-MoUQhqAWH_oVjAMwr4Sogjd50xomzw08E_h_fOvAZFBK7_W00)

_Figure 8.17: Co-simulation software architecture._

The class `AbstractOtsTransceiver` needs to be extended for a specific protocol implementation. This class has several `send(...)` and `receive(...)` methods, that each have a specific message type as input. The implementation should compose a message object when a message is received, and invoke the appropriate `receive(...)` method. The OTS transceiver will automatically respond to this information. When appropriate the transceiver will invoke any of the `send(...)` methods to send a message. These methods are abstract and need to be implemented for a specific protocol. The protocol implementation has to make sure that the content of the message is sent to the external simulator.

For all messages that contain a field _responseId_, the value for this field should be sent as a unique object of a type that the sim0mq serialization supports. Usually this is of type `String`. OTS will return the value in the same type.

The default sim0mq implementation `OtsTransceiverSim0mq` can provide more clarity on the specific role of a protocol implementation.