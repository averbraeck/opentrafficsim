/**
 * The lane-based GTUs are the Generalized Travel Units that travel in lanes, and need to switch lanes to overtake. The GTUs
 * have a model that determines acceleration and initial speed for the next time step. They also have a lane changing model that
 * indicates when to change lanes and how long it takes to change lanes. The lateral position of the reference point of the GTU
 * relative to the center line of the lanes in which the vehicle is registered can be determined that way.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 */
package org.opentrafficsim.road.gtu.lane;
