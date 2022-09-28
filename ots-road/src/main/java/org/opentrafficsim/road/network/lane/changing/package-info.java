/**
 * Lane change possibility models. This package codes lane change possibility models that indicate under what circumstances GTUs
 * are allowed to change lane, and what lane keeping policies are in place. Examples of lane keeping policies are "keep right",
 * "keep left" or "keep lane". The infrastructure could allow to overtake on the left, but not on the right; often this is
 * combines with allowing to pass another GTU on the right when the density is above a certain threshold, or the speed below a
 * certain value. Otherwise, cars would not drive to a traffic light in parallel, because that would be considered overtaking on
 * the right. There could also be details that differ per GTU type: on some stretches of highway, trucks are not allowed to
 * overtake. Similarly, on secondary roads it is sometimes forbidden to overtake, except when the GTU in front is a tractor, or
 * other vehicle with a lower speed limit. The models in this package should allow to code for these situations and indicate
 * whether changing lane to the left or right is allowed, given the circumstances.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
package org.opentrafficsim.road.network.lane.changing;
