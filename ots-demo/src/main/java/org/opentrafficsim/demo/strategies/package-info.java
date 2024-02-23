/**
 * This package contains simulations regarding LMRS lane change strategies. This entails the base LMRS with:
 * <ul>
 * <li>Distributed Tmax</li>
 * <li>Distributed vGain</li>
 * <li>Distributed socio-speed sensitivity parameter (LmrsParameters.SOCIO)</li>
 * <li>Altered gap-acceptance: use own Tmax (GapAcceptance.EGO_HEADWAY)</li>
 * <li>Altered desired speed: increase during overtaking (SocioDesiredSpeed)</li>
 * <li>Lane change incentive to get out of the way (IncentiveSocioSpeed)</li>
 * </ul>
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
package org.opentrafficsim.demo.strategies;
