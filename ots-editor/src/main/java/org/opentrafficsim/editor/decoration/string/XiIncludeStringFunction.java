package org.opentrafficsim.editor.decoration.string;

import java.io.File;
import java.rmi.RemoteException;
import java.util.function.Function;

import org.opentrafficsim.editor.OtsEditor;
import org.opentrafficsim.editor.XsdTreeNode;

/**
 * Adds the included file name to the include node.
 * @author wjschakel
 */
public class XiIncludeStringFunction extends AbstractStringFunction
{

    /** */
    private static final long serialVersionUID = 20230910L;

    /**
     * Constructor.
     * @param editor OtsEditor; editor.
     * @throws RemoteException if an exception occurs while adding as a listener.
     */
    public XiIncludeStringFunction(final OtsEditor editor) throws RemoteException
    {
        super(editor, (node) -> node.getNodeName().equals("xi:include"));
        this.overwrite = false;
    }

    /** {@inheritDoc} */
    @Override
    public Function<XsdTreeNode, String> getStringFunction()
    {
        return new Function<XsdTreeNode, String>()
        {
            /** {@inheritDoc} */
            @Override
            public String apply(final XsdTreeNode t)
            {
                if (t.getAttributeValue(0) == null)
                {
                    return "";
                }
                return new File(t.getAttributeValue(0)).getName();
            }
        };
    }
}
