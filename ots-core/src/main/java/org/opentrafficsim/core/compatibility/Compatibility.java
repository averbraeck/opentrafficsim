package org.opentrafficsim.core.compatibility;

import org.opentrafficsim.base.HierarchicalType;

/**
 * Compatibility of infrastructure and users of that infrastructure. Examples are compatibility of a GtuType with a LinkType, or
 * GtuType with a LaneType, or GtuType with a SensorType. Both infrastructure type and user type are hierarchical, and the
 * information to make the decision might be higher up in the hierarchy. The outcome depends on whether the infrastructure or
 * the user hierarchy is traversed first. Example:
 * 
 * <pre>
 * Infrastructure: Road, Highway is-a Road
 * User:           Vehicle, Car is-a Vehicle, SlowVehicle is-a Vehicle, Bicycle is-a SlowVehicle.
 * Compatibility:  (Road, Vehicle) = True, (Highway, SlowVehicle) = False
 * </pre>
 * 
 * Suppose we want to know the compatibility between Bicycle and Highway. The compatibility (Highway, Bicycle) is not defined.
 * <ul>
 * <li>Infra first: When we examine infrastructure first, we go up the infra hierarchy to Road. (Road, Bicycle) is not given.
 * Now move up Bicycle. (Road, SlowVehicle) is not provided. Move up. (Road, Vehicle) is provided and True. So, Bicycle is
 * allowed on Highway</li>
 * <li>user first: we go up the user hierarchy to SlowVehicle. (Highway, SlowVehicle) is defined and False. So, Bicycle is NOT
 * allowed on Highway</li>
 * </ul>
 * To avoid such problems, a very strict definition of what comes "first" has to be provided. The way this is implemented is as
 * follows: First, all combinations in the two hierarchies will be searched for explicitly forbidden combinations. If there is a
 * forbidden combination, compatibility is false. If the compatibility is not explicitly false, the hierarchies will be checked
 * to look for an explicit compatibility that is true. If there is any, the result is true. If nothing is specified in the
 * hierarchies (neither true nor false), the compatibility is false.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 * @param <U> infrastructure user type
 * @param <I> infrastructure type
 */
public interface Compatibility<U extends HierarchicalType<U, ?>, I extends HierarchicalType<I, ?>>
{
    /**
     * Test if a user type is compatible with the infrastructure type. Examples are compatibility of a GtuType with a LinkType,
     * or GtuType with a LaneType, or GtuType with a SensorType. Since both GtuType and InfrastructureType are hierarchical
     * types, it might be that compatibility has to look one or more levels up to determine whether user type and infrastructure
     * type are compatible. The outcome depends on which hierarchy is examined first. The way this is implemented is as follows:
     * First, all combinations in the two hierarchies will be searched for explicitly forbidden combinations. If there is a
     * forbidden combination, compatibility is false. If the compatibility is not explicitly false, the hierarchies will be
     * checked to look for an explicit compatibility that is true. If there is any, the result is true. If nothing is specified
     * in the hierarchies (neither true nor false), the compatibility is false.
     * @param userType the type of the infrastructure user
     * @return true if the user type is compatible with the infrastructure type
     */
    boolean isCompatible(U userType);

    /**
     * Return whether the user type is compatible on this infrastructure level.
     * @param userType the type of the infrastructure user
     * @return true if explicitly defined to be compatible on this level; false if explicitly defined to be incompatible on this
     *         level; null if not defined on this level
     */
    Boolean isCompatibleOnInfraLevel(U userType);

    /**
     * Return the infrastructure for which this compatibility has been defined.
     * @return the infrastructure for which this compatibility has been defined
     */
    I getInfrastructure();

    /**
     * Remove the compatibility cache for this type and all its subtypes.
     */
    void clearCompatibilityCache();

}
