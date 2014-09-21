package org.opentrafficsim.core.network;

import java.io.Serializable;

/**
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version Aug 19, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.citg.tudelft.nl">Guus Tamminga</a>
 * @param <ID>
 */
public class Node<ID> implements Serializable
{
    /** */
    private static final long serialVersionUID = 20140920L;

    /** the node id. */
    private final ID id;

    /** the node name. */
    private final String name;

    /**
     * Construction of a Node.
     * @param id the id of the Node.
     * @param name the name of the Node as a String.
     */

    public Node(final ID id, final String name)
    {
        this.id = id;
        this.name = name;
    }

    /**
     * @param id the id of the Node.
     */
    public Node(final ID id)
    {
        this(id, "");
    }

    /**
     * @return node name.
     */
    public final String getName()
    {
        return this.name;
    }

    /**
     * @return node id.
     */
    public final ID getId()
    {
        return this.id;
    }

}
