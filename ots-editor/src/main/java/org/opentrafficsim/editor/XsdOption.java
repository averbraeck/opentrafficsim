package org.opentrafficsim.editor;

/**
 * Class that groups some information on an option for a choice node, relevant to show to the user.
 * <p>
 * Copyright (c) 2023-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 * @param optionNode option node.
 * @param choice choice node, the option node is one of its options.
 * @param firstInGroup whether this option is first in a group and a separator might be shown in a menu.
 * @param selected whether this option is currently selected.
 */
public record XsdOption(XsdTreeNode optionNode, XsdTreeNode choice, boolean firstInGroup, boolean selected)
{
}
