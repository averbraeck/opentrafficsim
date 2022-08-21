# Network directions

Traffic may travel with or against the direction of a link as defined by its start and end node. There is a subtle but important difference between allowable and actual direction of traffic, namely, that a GTU has to have one, while both, either or none can be allowed. Allowance is defined in `LongitudinalDirectionality`, for both links and lanes.

<pre>
<b>Longitudinal directionality</b>
&lfloor; Plus
&lfloor; Minus
&lfloor; Both
&lfloor; None
</pre>

For a GTU there are only two possible directions of travel, hence we also have `GTUDirectionality`.

<pre>
<b>GTU directionality</b>
&lfloor; Plus
&lfloor; Minus
</pre>

In many ways, a combination of a link or lane with a GTU directionality is a more suitable building block for an algorithm than the link or lane itself. Small classes define combinations as `LinkDirection` and `LaneDirection`. These have a number of helper methods that allow easier algorithms that search over a network from the perspective of a GTU. In particular `LaneDirection.getNextLaneDirection(…)` aides in dealing with splits in an easy and consistent manner.

<pre>
<b>Link / Lane direction</b>
&lfloor; Link / Lane
&lfloor; GTU directionality
</pre>

Other small classes additionally define a longitudinal position on the link or lane, namely `DirectedLinkPosition` and `DirectedLanePosition`.
