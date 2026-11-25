package org.opentrafficsim.editor.extensions.map;

import java.util.List;

import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.base.StripeElement.StripeLateralSync;
import org.opentrafficsim.editor.XsdTreeNode;
import org.opentrafficsim.road.network.StripeData.StripePhaseSync;

/**
 * Container of collected stripe data.
 * <p>
 * Copyright (c) 2026-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.<br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author Wouter Schakel
 * @param phaseSync phase synchronization
 * @param dashOffset dash offset
 * @param lateralSync lateral synchronization
 * @param elementNodes element nodes
 */
record StripeDefinition(StripePhaseSync phaseSync, Length dashOffset, StripeLateralSync lateralSync,
        List<XsdTreeNode> elementNodes)
{
}
