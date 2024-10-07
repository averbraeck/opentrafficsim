/**
 * Strategical planners. A strategicalPlanner is the planner responsible for the overall 'mission' of the GTU, usually
 * indicating where it needs to go. It operates by instantiating tactical planners to do the actual work, which is generating
 * operational plans (paths over time) to follow to reach the destination that the strategical plan is aware of.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 */
package org.opentrafficsim.core.gtu.plan.strategical;
