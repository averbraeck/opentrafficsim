package org.opentrafficsim.editor.decoration.validation;

import org.opentrafficsim.editor.OtsEditor;
import org.opentrafficsim.editor.XsdTreeNode;
import org.opentrafficsim.editor.decoration.AbstractNodeDecoratorAttribute;

/**
 * Validates that the two attributes of a node are not the same.
 * <p>
 * Copyright (c) 2023-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class AttributesNotEqualValidator extends AbstractNodeDecoratorAttribute implements ValueValidator
{

    /** */
    private static final long serialVersionUID = 20230910L;

    /**
     * Constructor.
     * @param editor editor.
     * @param path path location of nodes to attach to.
     * @param attribute1 first attribute to compare.
     * @param attribute2 second attribute to compare.
     */
    public AttributesNotEqualValidator(final OtsEditor editor, final String path, final String attribute1,
            final String attribute2)
    {
        super(editor, (n) -> n.isType(path), attribute1, attribute2);
    }

    /** {@inheritDoc} */
    @Override
    public String validate(final XsdTreeNode node)
    {
        if (!node.isActive())
        {
            return null;
        }
        String attribute1 = node.getAttributeValue(this.attributes.get(0));
        if (attribute1 == null)
        {
            return null;
        }
        String attribute2 = node.getAttributeValue(this.attributes.get(1));
        if (attribute2 == null || !attribute2.equals(attribute1))
        {
            return null;
        }
        return this.attributes.get(0) + " and " + this.attributes.get(1) + " may not be equal.";
    }

    /** {@inheritDoc} */
    @Override
    public void notifyCreated(final XsdTreeNode node)
    {
        if (this.predicate.test(node))
        {
            node.addAttributeValidator(this.attributes.get(0), AttributesNotEqualValidator.this, null);
            node.addAttributeValidator(this.attributes.get(1), AttributesNotEqualValidator.this, null);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void notifyAttributeChanged(final XsdTreeNode node, final String attribute)
    {
        node.invalidate();
    }

}
