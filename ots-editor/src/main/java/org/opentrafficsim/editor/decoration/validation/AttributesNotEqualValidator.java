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

    /** First attribute to compare. */
    private final String attribute1;

    /** Second attribute to compare. */
    private final String attribute2;

    /**
     * Constructor.
     * @param editor OtsEditor; editor.
     * @param path String; path location of nodes to attach to.
     * @param attribute1 String; first attribute to compare.
     * @param attribute2 String; second attribute to compare.
     */
    public AttributesNotEqualValidator(final OtsEditor editor, final String path, final String attribute1,
            final String attribute2)
    {
        super(editor, (n) -> n.isType(path), attribute1, attribute2);
        this.attribute1 = attribute1;
        this.attribute2 = attribute2;
    }

    /** {@inheritDoc} */
    @Override
    public String validate(final XsdTreeNode node)
    {
        if (!node.isActive())
        {
            return null;
        }
        String attribute1 = node.getAttributeValue(this.attribute1);
        if (attribute1 == null)
        {
            return null;
        }
        String attribute2 = node.getAttributeValue(this.attribute2);
        if (attribute2 == null || !attribute2.equals(attribute1))
        {
            return null;
        }
        return this.attribute1 + " and " + this.attribute2 + " may not be equal.";
    }

    /** {@inheritDoc} */
    @Override
    public void notifyCreated(final XsdTreeNode node)
    {
        node.addAttributeValidator(this.attribute1, AttributesNotEqualValidator.this);
        node.addAttributeValidator(this.attribute2, AttributesNotEqualValidator.this);
    }

    /** {@inheritDoc} */
    @Override
    public void notifyAttributeChanged(final XsdTreeNode node, final String attribute)
    {
        node.invalidate();
    }

}
