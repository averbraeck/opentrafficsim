package org.opentrafficsim.road.gtu.tactical.following;

import org.opentrafficsim.road.gtu.tactical.ModelComponentSupplier;

/**
 * Factory for car-following models.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 * @param <T> class of car following model
 */
public interface CarFollowingModelFactory<T extends CarFollowingModel> extends ModelComponentSupplier<T>
{
}
