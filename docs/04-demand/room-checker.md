# Room checker

A final component of the GTU generator is the room checker, which tells the GTU generator whether a GTU can be placed at a given position. It returns a suitable speed and position for the GTU to be generated at. The returned position may be the given position, or a position close to it. Changing the position slightly increases the maximum flow that can be generated, which would otherwise be lowered by the interval at which queued GTUs are attempted to be placed.

<pre>
Lane-based GTU generator
&lfloor; <b>Room checker</b>
</pre>

A useful implementation for the room checker is `CfRoomChecker`, where CF stands for car-following, as this room checker puts constrained GTUs in car-following relative to the downstream leader. It has the following algorithm.

* The desired speed is peeked from the strategical planner factory. If it doesn’t provide a desired speed, the speed limit is used.
* If there are no downstream GTUs, the desired speed and initial position are returned to the GTU generator.
* For each leader (multiple in case of a split) a following position is determined at a peeked desired headway behind the leader. The used speed is the minimum of the leader speed and the desired speed. If the strategical planner factory does not provide a desired headway, a 1s headway is used. The speed and headway of the most constraining leader are used to generate the GTU.
* If the most constraining leader does not provide sufficient space relative to the initial position, the GTU cannot be generated at the time. This is signaled to the GTU generator by returning `Placement.NO`.
* The distance over which the GTU is placed downstream of the initial position is constrained by the time since the GTU was first attempted to be placed, as the GTU could never travel more distance than at the desired speed during this time.

A similar room checker is `CfBaRoomChecker` which advances one aspect further. The BA stands for bounded acceleration, which is a 1<sup>st</sup>-order traffic flow theory for flow recovery. The theory states that the lower the speed in congestion, the lower the saturation flow out of congestion. In other words, the headways are larger. Counter-intuitively, increasing headways when the GTU generator experiences spillback, allows the congestion to recover more quickly as the additional room allows acceleration. Without it, GTUs are generated at their desired headway and are more likely to slow down. Especially with moving jams, the generator may then maintain low speeds at its location, while the jam should have moved upstream allowing free flow speeds at the generator location. From the desired speed and desired headway at that speed, a capacity flow <i>q<sub>0</sub></i> concerning the GTU to be generated is determined. From the headway and speed at which the GTU would be generated without increased headway, the traffic state in congestion is determined. Using the bounded acceleration theory, the recovery flow <i>q<sub>r</sub></i> that the congested state allows is determined. Finally, the headway is increased by a factor of <i>q<sub>0</sub></i>/<i>q<sub>r</sub></i>&nbsp;&gt;&nbsp;1. Lower speeds cause larger time headways. The theory of bounded acceleration and how it affects GTU generation headways is further discussed in [Appendix B](../99-appendices/queue-spillback.md).
