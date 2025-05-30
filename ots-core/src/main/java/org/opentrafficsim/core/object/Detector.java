package org.opentrafficsim.core.object;

import org.opentrafficsim.base.HierarchicallyTyped;

/**
 * Interface for detectors. This can be a lane-based object, classes using lane-based helper objects, or objects that detect in
 * a completely different manner.
 * <p>
 * Copyright (c) 2022-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public interface Detector extends LocatedObject, HierarchicallyTyped<DetectorType, Detector>
{
    //
}
