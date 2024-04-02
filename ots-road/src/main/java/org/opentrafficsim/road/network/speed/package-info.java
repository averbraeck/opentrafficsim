/**
 * Classes to connect driver behavior with many different aspects that govern the desired speed. This constitutes both legal and
 * physical speed limits.<br>
 * <br>
 * Each aspect is defined in a {@code SpeedLimitType<T>}, where {@code T} is the class of underlying info, generally referred to
 * as Speed Info or with the parameter name {@code speedInfo}. Often, {@code T} will be {@code Speed}. For this purpose the
 * easier {@code SpeedLimitTypeSpeed} should be used. A distinction between <i>legal</i> and <i>non-legal</i> speed limit types
 * is made. For <i>legal</i> speed limit types implement interface {@code LegalSpeedLimit}. For <i>legal</i> speed limits which
 * have {@code Speed} as info, extend {@code SpeedLimitTypeSpeedLegal}. A list of default {@code SpeedLimitType}'s can be found
 * in {@code SpeedLimitTypes}.<br>
 * <br>
 * At any time a set of {@code SpeedLimitType}'s may be active with an accompanying set of Speed Info's of whatever class. This
 * information is stored in {@code SpeedLimitInfo}. Based on such a set, driver models can derive <i>current</i> desired speed.
 * <br>
 * <br>
 * However, deceleration for lower speed limits ahead may also be required. For this purpose {@code SpeedLimitType}'s and
 * accompanying Speed Info's are stored together with a <i>distance</i> in a {@code SpeedLimitProspect}. Suggested use of this
 * class is:
 * 
 * <pre>
 * TODO
 * </pre>
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
package org.opentrafficsim.road.network.speed;
