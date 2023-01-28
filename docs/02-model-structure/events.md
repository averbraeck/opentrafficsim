# Events in OTS

_Table 2: Overview of events in OTS._
<table border="1" width="800px">
  <tr style="font-weight: bold"><td>Class</td><td>Event field</td><td>Listeners (excluding test code)</td><td>Used</td></tr>
  <tr><td>Network</td><td>ANIMATION_GENERATOR_ADD_EVENT</td><td>DefaultAnimationFactory<sup>1,2</sup></td><td>yes</td></tr>
  <tr><td></td><td>ANIMATION_GENERATOR_REMOVE_EVENT</td><td>DefaultAnimationFactory<sup>1,2</sup></td><td>yes</td></tr>
  <tr><td></td><td>ANIMATION_GTU_ADD_EVENT</td><td>DefaultAnimationFactory, NetworkAnimation<sup>1</sup></td><td>yes</td></tr>
  <tr><td></td><td>ANIMATION_GTU_REMOVE_EVENT</td><td>DefaultAnimationFactory, NetworkAnimation<sup>1</sup></td><td>yes</td></tr>
  <tr><td></td><td>ANIMATION_INVISIBLE_OBJECT_ADD_EVENT</td><td>NetworkAnimation<sup>1</sup></td><td>no</td></tr>
  <tr><td></td><td>ANIMATION_INVISIBLE_OBJECT_REMOVE_EVENT</td><td>NetworkAnimation<sup>1</sup></td><td>no</td></tr>
  <tr><td></td><td>ANIMATION_LINK_ADD_EVENT</td><td>NetworkAnimation<sup>1</sup></td><td>no</td></tr>
  <tr><td></td><td>ANIMATION_LINK_REMOVE_EVENT</td><td>NetworkAnimation<sup>1</sup></td><td>no</td></tr>
  <tr><td></td><td>ANIMATION_NODE_ADD_EVENT</td><td>NetworkAnimation<sup>1</sup></td><td>no</td></tr>
  <tr><td></td><td>ANIMATION_NODE_REMOVE_EVENT</td><td>NetworkAnimation<sup>1</sup></td><td>no</td></tr>
  <tr><td></td><td>ANIMATION_OBJECT_ADD_EVENT</td><td>DefaultAnimationFactory, NetworkAnimation<sup>1</sup></td><td>yes</td></tr>
  <tr><td></td><td>ANIMATION_OBJECT_REMOVE_EVENT</td><td>DefaultAnimationFactory, NetworkAnimation<sup>1</sup></td><td>yes</td></tr>
  <tr><td></td><td>ANIMATION_ROUTE_ADD_EVENT</td><td>NetworkAnimation<sup>1</sup></td><td>no</td></tr>
  <tr><td></td><td>ANIMATION_ROUTE_REMOVE_EVENT</td><td>NetworkAnimation<sup>1</sup></td><td>no</td></tr>
  <tr><td></td><td>GENERATOR_ADD_EVENT</td><td><i>not thrown</i></td><td>no</td></tr>
  <tr><td></td><td>GENERATOR_REMOVE_EVENT</td><td><i>not thrown</i></td><td>no</td></tr>
  <tr><td></td><td>GTU_ADD_EVENT</td><td>AbstractLaneBasedMoveChecker, Publisher<sup>3</sup>, NetworkModel, StochasticDistractionModel, RampMeteringDemo, OtsAnimationPanel</td><td>yes</td></tr>
  <tr><td></td><td>GTU_REMOVE_EVENT</td><td>AbstractLaneBasedMoveChecker, Publisher<sup>3</sup>, NetworkModel, StochasticDistractionModel, RampMeteringDemo, OtsAnimationPanel</td><td>yes</td></tr>
  <tr><td></td><td>INVISIBLE_OBJECT_ADD_EVENT</td><td></td><td>no</td></tr>
  <tr><td></td><td>INVISIBLE_OBJECT_REMOVE_EVENT</td><td></td><td>no</td></tr>
  <tr><td></td><td>LINK_ADD_EVENT</td><td>Publisher<sup>3</sup></td><td>yes</td></tr>
  <tr><td></td><td>LINK_REMOVE_EVENT</td><td>Publisher<sup>3</sup></td><td>yes</td></tr>
  <tr><td></td><td>NODE_ADD_EVENT</td><td>Publisher<sup>3</sup></td><td>yes</td></tr>
  <tr><td></td><td>NODE_REMOVE_EVENT</td><td>Publisher<sup>3</sup></td><td>yes</td></tr>
  <tr><td></td><td>OBJECT_ADD_EVENT</td><td></td><td>no</td></tr>
  <tr><td></td><td>OBJECT_REMOVE_EVENT</td><td></td><td>no</td></tr>
  <tr><td></td><td>ROUTE_ADD_EVENT</td><td></td><td>no</td></tr>
  <tr><td></td><td>ROUTE_REMOVE_EVENT</td><td></td><td>no</td></tr>
  <tr><td>Link</td><td>GTU_ADD_EVENT</td><td>Publisher<sup>3</sup></td><td>yes</td></tr>
  <tr><td></td><td>GTU_REMOVE_EVENT</td><td>Publisher<sup>3</sup></td><td>yes</td></tr>
  <tr><td>CrossSectionLink</td><td>LANE_ADD_EVENT</td><td>Publisher<sup>3</sup></td><td>yes</td></tr>
  <tr><td></td><td>LANE_REMOVE_EVENT</td><td>Publisher<sup>3</sup></td><td>yes</td></tr>
  <tr><td>Gtu</td><td>INIT_EVENT</td><td>StochasticDistractionModel</td><td>yes</td></tr>
  <tr><td></td><td>MOVE_EVENT</td><td>Publisher<sup>3</sup>, GtuTransceiver<sup>3</sup></td><td>yes</td></tr>
  <tr><td></td><td>DESTROY_EVENT</td><td></td><td>no</td></tr>  
  <tr><td>LaneBasedGtu</td><td>LANEBASED_INIT_EVENT</td><td></td><td>no</td></tr>
  <tr><td></td><td>LANEBASED_MOVE_EVENT</td><td>AbstractLaneBasedMoveChecker, RoadSampler</td><td>yes</td></tr>
  <tr><td></td><td>LANEBASED_DESTROY_EVENT</td><td></td><td>no</td></tr>
  <tr><td></td><td>LANE_CHANGE_EVENT</td><td>Conflict, StrategiesDemo</td><td>yes</td></tr>
  <tr><td></td><td>LANE_ENTER_EVENT</td><td><i>not thrown</i></td><td>no</td></tr>
  <tr><td></td><td>LANE_EXIT_EVENT</td><td><i>not thrown</i></td><td>no</td></tr>
  <tr><td>Lane</td><td>GTU_ADD_EVENT</td><td>RoadSampler, TrafficLightSensor</td><td>yes</td></tr>
  <tr><td></td><td>GTU_REMOVE_EVENT</td><td>RoadSampler, TrafficLightSensor</td><td>yes</td></tr>
  <tr><td></td><td>OBJECT_ADD_EVENT</td><td></td><td>no</td></tr>
  <tr><td></td><td>OBJECT_REMOVE_EVENT</td><td></td><td>no</td></tr>
  <tr><td></td><td>SENSOR_ADD_EVENT</td><td></td><td>no</td></tr>
  <tr><td></td><td>SENSOR_REMOVE_EVENT</td><td></td><td>no</td></tr>
  <tr><td>LaneBasedGtuGenerator</td><td>GTU_GENERATED_EVENT</td><td></td><td>no</td></tr>
  <tr><td>SingleSensor</td><td>SENSOR_TRIGGER_EVENT</td><td></td><td>no</td></tr>
  <tr><td>Detector</td><td>DETECTOR_TRIGGERED</td><td></td><td>no</td></tr>
  <tr><td></td><td>DETECTOR_AGGREGATE</td><td></td><td>no</td></tr>
  <tr><td>NonDirectionalOccupancySensor</td><td>NON_DIRECTIONAL_OCCUPANCY_SENSOR_ TRIGGER_ENTRY_EVENT</td><td>TrafficLightSensor, TrafCod, DetectrorImage, Variable</td><td>yes</td></tr>
  <tr><td></td><td>NON_DIRECTIONAL_OCCUPANCY_SENSOR_ TRIGGER_EXIT_EVENT</td><td>TrafficLightSensor, TrafCod, DetectrorImage, Variable</td><td>yes</td></tr>
  <tr><td>DirectionalOccupancySensor</td><td>DIRECTIONAL_OCCUPANCY_SENSOR_ TRIGGER_ENTRY_EVENT</td><td></td><td>no</td></tr>
  <tr><td></td><td>DIRECTIONAL_OCCUPANCY_SENSOR_ TRIGGER_EXIT_EVENT</td><td></td><td>no</td></tr>
  <tr><td>TrafficController</td><td>TRAFFICCONTROL_CONTROLLER_CREATED</td><td>AbstractTrafficController</td><td>yes</td></tr>
  <tr><td></td><td>TRAFFICCONTROL_CONTROLLER_EVALUATING</td><td>TrafCod, TrafCODModel</td><td>yes</td></tr>
  <tr><td></td><td>TRAFFICCONTROL_CONTROLLER_WARNING</td><td>TrafCodDemo2, TrafCod, TrafCODModel</td><td>yes</td></tr>
  <tr><td></td><td>TRAFFICCONTROL_STATE_CHANGED</td><td>TrafCodDemo2</td><td>yes</td></tr>
  <tr><td></td><td>TRAFFIC_LIGHT_CHANGED</td><td>TrafCod</td><td>yes</td></tr>
  <tr><td></td><td>TRAFFICCONTROL_VARIABLE_CREATED</td><td>TrafCodDemo2, TrafCod</td><td>yes</td></tr>
  <tr><td></td><td>TRAFFICCONTROL_SET_TRACING</td><td>TrafCod</td><td>yes</td></tr>
  <tr><td></td><td>TRAFFICCONTROL_TRACED_VARIABLE_UPDATED</td><td>TrafCodDemo2, TrafCODModel, Variable</td><td>yes</td></tr>
  <tr><td></td><td>TRAFFICCONTROL_CONFLICT_GROUP_CHANGED</td><td>TrafCodDemo2, TrafCODModel, TrafCod</td><td>yes</td></tr>
  <tr><td>TrafficLight</td><td>TRAFFICLIGHT_CHANGE_EVENT</td><td>AbtstractTrafficLight</td><td>yes</td></tr>
  <tr><td>AbstractPlot</td><td>GRAPH_ADD_EVENT</td><td><i>not thrown</i></td><td>no</td></tr>
  <tr><td></td><td>GRAPH_REMOVE_EVENT</td><td><i>not thrown</i></td><td>no</td></tr>
  <tr><td>XYSeries</td><td>LOWER_RANGE_EVENT</td><td><i>not thrown</i></td><td>no</td></tr>
  <tr><td></td><td>UPPER_RANGE_EVENT</td><td><i>not thrown</i></td><td>no</td></tr>
</table>
<sup>1</sup>) Registers as listener, but does (effectively) nothing with it in `notify()`.<br>
<sup>2</sup>) Should use `GtuGeneratorQueueAnimation`.<br>
<sup>3</sup>) Part of `ots-sim0mq`.