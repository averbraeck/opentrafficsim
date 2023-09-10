package org.opentrafficsim.editor.decoration;

import java.rmi.RemoteException;
import java.util.function.Function;

import org.opentrafficsim.editor.OtsEditor;
import org.opentrafficsim.editor.XsdTreeNode;

/**
 * In nodes with only a choice as child node, displays the chosen child value.
 * @author wjschakel
 */
public class ClassNameTypeStringFunction extends AbstractStringFunction
{

    /** */
    private static final long serialVersionUID = 20230910L;

    /**
     * Constructor.
     * @param editor OtsEditor; editor.
     * @throws RemoteException if an exception occurs while listening for new nodes.
     */
    public ClassNameTypeStringFunction(final OtsEditor editor) throws RemoteException
    {
        super(editor, (node) -> node.isType("ClassNameType"));
    }

    /** {@inheritDoc} */
    @Override
    public Function<XsdTreeNode, String> getStringFunction()
    {
        return new Function<XsdTreeNode, String>()
        {
            /** {@inheritDoc} */
            @Override
            public String apply(final XsdTreeNode node)
            {
                String value = node.getValue();
                if (value == null || value.isBlank())
                {
                    return "";
                }
                int dot = value.lastIndexOf(".");
                if (dot < 0 || dot == value.length() - 1)
                {
                    return value;
                }
                return value.substring(dot + 1, value.length());
            }
        };
    }

}
