package org.opentrafficsim.editor.decoration.string;

import java.rmi.RemoteException;
import java.util.function.Function;

import org.opentrafficsim.editor.OtsEditor;
import org.opentrafficsim.editor.XsdTreeNode;

/**
 * Displays the simple class name in nodes of ClassNameType.
 * @author wjschakel
 */
public class ClassNameTypeStringFunction extends AbstractStringFunction
{

    /** */
    private static final long serialVersionUID = 20230910L;

    /**
     * Constructor.
     * @param editor OtsEditor; editor.
     * @throws RemoteException if an exception occurs while adding as a listener.
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
                if (value == null || value.isEmpty())
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
