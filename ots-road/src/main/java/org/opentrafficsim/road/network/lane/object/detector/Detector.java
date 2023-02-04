package org.opentrafficsim.road.network.lane.object.detector;

import org.opentrafficsim.base.HierarchicallyTyped;
import org.opentrafficsim.core.object.LocatedObject;

/**
 * Interface for detectors. This can be a lane-based object, classes using lane-based helper objects, or objects that detect in
 * a completely different manner. This interface is mainly used to toggle animation of all detector objects. In order to disable
 * drawing of the helper objects, no animations should be created for these types. If a default animation factory is used, that
 * may need to be extended to ignore such types.
 * <p>
 * Copyright (c) 2022-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public interface Detector extends LocatedObject, HierarchicallyTyped<DetectorType, Detector>
{
    //
}
