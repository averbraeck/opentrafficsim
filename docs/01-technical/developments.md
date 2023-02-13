# Developments for OTS

This page lists some of the current ideas that are being worked on for the development of OpenTrafficSim.


## Free GTU movement

In OTS GTUs and objects are currently coupled to lanes. GTUs coupled to lanes are on the lane, and objects coupled to lanes are functionally related to the lane. For example, a traffic light may in actuality be next to a lane, but is considered at a specific spot on a `Lane`. Especially for GTUs, the book-keeping system is a complex and error-prone system. GTUs plan their own enter-lane and leave-lane events, which are triggered by the nose or tail respectively. But lanes may not be connected perfectly, and as GTUs move between lanes the consistency of these events is troublesome. As a result, events may be triggered multiple times (for example causing data inconsistencies), or a GTU may find itself not referenced on any `Lane`, at which points several algorithms will fail.

With the advent of faster and efficient spatial algorithms such as the [Quadtree](https://en.wikipedia.org/wiki/Quadtree), the need for such book-keeping diminishes. Rather, GTUs are completely free to move and are perceived completely on the fly. To this end the following development is planned:

1. Quadtree, or Quadtree-like, spatial algorithms are implemented. A distinction will be made between static objects and dynamic objects. For static objects the search speed may be optimized, while for dynamic objects it is important that the spatial algorithm allows fast addition and removal of objects.
2. Existing functionality in specific methods is replaced with spatial search. For example, think of obtaining a list of GTUs on a lane.
3. The book-keeping system is removed.

After these steps further development might take place in the realm of perception, that will be able to make use of specific and dedicated spatial search algorithms, rather than such algorithms mimicking the former lane book-keeping. The removalof the book-keeping does lead to some open questions:

1. How will sensors be triggered? Will GTUs do this actively, or will a sensor perform a spatial search with some given frequency?
2. Likewise, how will lane-enter and lane-leave events be triggered? Note that beyond the book-keeping that will be removed, such events are also used for other purposes such as sampling.

<<<<<<< HEAD
Regarding [Perception](../05-perception/introduction.md), the lane structure will be abandoned. Perception iterables may also be removed or replaced, as these currently often use the lane structure. The explicit spatial search may allow for completely new ways of performing the fundamental part of perception; seeing, recognizing and understanding objects (e.g. on what lane a GTU is). Regarding the [Deviative nature of operational plans](../06-behavior/tactical-planner.md#operational-plan), this nature becomes deprecated.
=======
Regarding [Perception](../05-perception/), the lane structure will be abandoned. Perception iterables may also be removed or replaced, as these currently often use the lane structure. The explicit spatial search may allow for completely new ways of performing the fundamental part of perception; seeing, recognizing and understanding objects (e.g. on what lane a GTU is). Regarding the [Deviative nature of operational plans](../06-behavior/tactical-planner.md#operational-plan), this nature becomes deprecated.
>>>>>>> branch 'main' of https://github.com/averbraeck/opentrafficsim.git


## nFlow – Neural model structure for data flow

In the years that OTS has been used, the default tactical planner LMRS with the IDM+ car-following model, have been extended with quite some features:

1. Dynamic set of mandatory lane change incentives.
2. Dynamic set of voluntary lane change incentives.
3. Dynamic set of acceleration incentives.
4. Different synchronization implementations.
5. Different cooperation implementations.
6. Different gap-acceptance implementations.
7. Different tailgating implementations.
8. Different desired speed model implementations.

The information that is given to the tactical planner comes from perception. Here too quite some developments were made:

1. `Mental` module added, with an implementation of Fuller's task-interface model.
2. Different tasks that take any information from perception or the model itself.
3. Different behavioral adaptations that may affect any part or parameter in the model.
<<<<<<< HEAD
4. Different task managers, including simple summation of tasks, or implementing the concept of [anticipation reliance (Calvert et al., 2020)](../10-references/references.md).
=======
4. Different task managers, including simple summation of tasks, or implementing the concept of [anticipation reliance (Calvert et al., 2020)](../10-references/).
>>>>>>> branch 'main' of https://github.com/averbraeck/opentrafficsim.git

On top of this, there are many classes added for the visualization of these internal mechanisms, as well as to provide information on this for trajectory sampling. Consequently the simulation functionality for certain modelling concepts is distributed among various projects and packages in OTS. On top of that, parts of this functionality do not comply with coding standards. For example, the `LMRSFactory` is defined by 9 different inputs, and certain methods are quite long, which is all relatively incomprehensible. These are signs of a need for a better structure. The particular challenge here is the integrated nature of all components. For example a behavioral adaptation may be influenced by anything (what is task demanding) and may influence anything (that will alleviate the task demand). A highly adaptable and exposed structure is required.

To this end, a concept analogous to biological neurons is being developed. This analogy is made to gain from the highly adaptable nature of neurons. Each step of the model is captured inside a neuron. Input is obtained from a small set of upstream neurons, and one output value is given upon request by any number of downstream neurons. Each neuron consists of:

1. _Dendrites_; which have the synapses to which input neurons attach.
2. _Function_; the actual operation that the neuron performs.
3. _Axon_; the output value flows through this, and may be altered along the way by other neurons that attach to the axon.
4. _Axon dendrite_; single stand-in for many attachments to downstream neurons, either at their dendrites or axon.

Some neurons have no input neurons as they provide a constant, a parameter value, or some (perceived) information from the simulation environment. Some neurons are defined as _dynamic_, which means any number of neurons may attach to the dendrites as long as they provide the right data type. Dynamic neurons are generic operations, for example summing all input values. Other neurons ideally have up to three input neurons, each with input of a specific meaning.

Neurons may in principle do anything with the input, and may hence output a different data type from the input data type. All neurons expose their result to be influenced through the axon. The mechanism of this influence is up to the neuron, but other neurons provide values for this influence. The typical axon allows serial multiplication of the result by the values from the neurons attached to the axon. Note that in this case the neuron is completely _inhibited_ if any value on the axon is 0. In this case, the neuron does not have to supply a value, including all upstream neurons that only this neuron uses (indirectly). The framework allows the value not to be determined for inhibited neurons, such that no unnecessary computation is done. Axons are always dynamic, any number of neurons providing the right data type may attach. But the value type through the axon may not be altered, and the various neurons attaching to the axon are unaware of each other; their influence is implemented in a serial manner. This is different from the operation of the neuron itself, where input-specific logic can be implemented such as simple if-statements. An overview of these different roles is given in table 1.

_Table 1.1: Function of dendrites and axon of neuron._
<table border="1" id="table-1" style="text-align: left">
    <tr style="font-weight: bold"><td></td><td>Dendrites</td><td>Axon</td></tr>
    <tr><td>Role</td><td>Core function</td><td>Influence on output</td></tr>
    <tr><td>Processing</td><td>Anything</td><td>Serial</td></tr>
    <tr><td>Data type conversion</td><td>Implementation specific</td><td>No</td></tr>
    <tr><td>Dynamic</td><td>Implementation specific</td><td>Always</td></tr>
</table>

The network of neurons that each implements a specific and small part of a model, allows for a high level of adaptability. Some _recipe_ for additional functionality may add, insert or remove neurons. Information can be obtained from any of the existing neurons. Output of all existing neurons may be influenced at their axon. And finally, existing dynamic neurons allow for easy additional functionality (for example when providing lane change desired from additional lane change incentives). Beyond the structure of the network, neurons can interact with various _schedulers_, such that their information is updated for example at different intervals.

Regarding [mandatory and voluntary lane change incentives](../06-behavior/lane-change.md#lane-change-incentives), these are currently dependent on the order, but will become explicitly dependent on other neurons (possibly pertaining to other incentives). The order will no longer matter. Regarding [acceleration incentives and other behaviors](../06-behavior/lane-change.md#acceleration-incentives), these will come in neural form attaching to the rest of the neural network.

This concept is mostly aimed at software maintainability, and less so to create models that are (somewhat) in line with a broad class of algorithms that are called neural networks. Tactical models are designed; causality is highly pre-determined. Hence this concept is more in line with _knowledge networks_ or _graphical causal models_.


## Manual

This manual is partially outdated as development is continuous. This holds especially for some of the tutorials that may differ on details from the current OTS version. We are doing our best to keep this manual up to date.
