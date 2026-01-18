package org.opentrafficsim.editor.decoration.validation;

import java.util.Optional;

import org.opentrafficsim.editor.OtsEditor;
import org.opentrafficsim.editor.XsdTreeNode;
import org.opentrafficsim.editor.decoration.AbstractNodeDecoratorAttribute;

/**
 * Validates that two attributes of a node are not the same.
 * <p>
 * Copyright (c) 2023-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class AttributesNotEqualValidator extends AbstractNodeDecoratorAttribute implements ValueValidator
{

    /*-
     * This somehow seems not to be possible with a key
     * (https://stackoverflow.com/questions/16357694/xml-schema-force-two-attributes-to-be-different,
     * https://stackoverflow.com/questions/19080473/xsd-unique-attribute-values-within-node,
     * https://stackoverflow.com/questions/25054526/xsd-unique-values-of-two-different-attributes).
     *
     * This and similar attempts were tried within the Link element, but did not create issues in validation:
     *
     * <xsd:key name=linkStartEndNodeKey>
     *   <xsd:selector xpath=".|." />
     *   <xsd:field xpath="@NodeStart|@NodeEnd" />
     * <xsd:key />
     */

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

    @Override
    public Optional<String> validate(final XsdTreeNode node)
    {
        if (!node.isActive())
        {
            return Optional.empty();
        }
        String attribute1 = node.getAttributeValue(getAttributes().get(0));
        if (attribute1 == null)
        {
            return Optional.empty();
        }
        String attribute2 = node.getAttributeValue(getAttributes().get(1));
        if (attribute2 == null || !attribute2.equals(attribute1))
        {
            return Optional.empty();
        }
        return Optional.of(getAttributes().get(0) + " and " + getAttributes().get(1) + " may not be equal.");
    }

    @Override
    public void notifyCreated(final XsdTreeNode node)
    {
        node.addAttributeValidator(getAttributes().get(0), AttributesNotEqualValidator.this);
        node.addAttributeValidator(getAttributes().get(1), AttributesNotEqualValidator.this);
    }

    @Override
    public void notifyAttributeChanged(final XsdTreeNode node, final String attribute)
    {
        node.invalidate();
    }

}
