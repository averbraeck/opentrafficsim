package org.opentrafficsim.editor.decoration;

import java.io.File;
import java.rmi.RemoteException;
import java.util.function.Function;

import org.djutils.event.Event;
import org.djutils.event.EventListener;
import org.opentrafficsim.editor.XsdTreeNode;
import org.opentrafficsim.editor.XsdTreeNodeRoot;

/**
 * Adds the included file name to the include node.
 * @author wjschakel
 */
class XiIncludeStringFunction implements EventListener
{
    /** */
    private static final long serialVersionUID = 20230313L;

    /** {@inheritDoc} */
    @Override
    public void notify(final Event event) throws RemoteException
    {
        XsdTreeNodeRoot root = (XsdTreeNodeRoot) event.getContent();
        root.addListener(new EventListener()
        {
            /** */
            private static final long serialVersionUID = 20230313L;

            /** {@inheritDoc} */
            @Override
            public void notify(final Event event) throws RemoteException
            {
                XsdTreeNode node = (XsdTreeNode) event.getContent();
                if (node.getNodeName().equals("xi:include"))
                {
                    node.setStringFunction(new Function<XsdTreeNode, String>()
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
                    });
                }
            }
        }, XsdTreeNodeRoot.NODE_CREATED);
    }
}