package org.opentrafficsim.editor.decoration.string;

import java.io.File;
import java.rmi.RemoteException;
import java.util.function.Function;

import org.opentrafficsim.editor.OtsEditor;
import org.opentrafficsim.editor.XsdTreeNode;
import org.opentrafficsim.editor.XsdTreeNodeRoot;

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
                File file = new File(t.getAttributeValue(0));
                if (!file.isAbsolute())
                {
                    file = new File(((XsdTreeNodeRoot) t.getPath().get(0)).getDirectory() + t.getAttributeValue(0));
                }
                if (!file.exists() && t.getAttributeValue(1) != null)
                {
                    File file2 = new File(t.getAttributeValue(1));
                    if (!file2.isAbsolute())
                    {
                        file2 = new File(((XsdTreeNodeRoot) t.getPath().get(0)).getDirectory() + t.getAttributeValue(1));
                    }
                    if (file2.exists())
                    {
                        return file2.getName() + " [fallback]";
                    }
                }
                return file.getName();
            }
        };
    }
}
