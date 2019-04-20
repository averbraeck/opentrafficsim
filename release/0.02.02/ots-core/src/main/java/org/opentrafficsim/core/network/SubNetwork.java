package org.opentrafficsim.core.network;

import java.util.HashSet;
import java.util.Set;

/**
 * <p>
 * Copyright (c) 2002-2015 Delft University of Technology, Jaffalaan 5, 2628 BX Delft, the Netherlands. All rights
 * reserved.
 * <p>
 * See for project information <a href="http://www.simulation.tudelft.nl/"> www.simulation.tudelft.nl</a>.
 * <p>
 * The OpenTrafficSim project is distributed under the following BSD-style license:<br>
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 * <ul>
 * <li>Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 * disclaimer.</li>
 * <li>Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
 * following disclaimer in the documentation and/or other materials provided with the distribution.</li>
 * <li>Neither the name of Delft University of Technology, nor the names of its contributors may be used to endorse or
 * promote products derived from this software without specific prior written permission.</li>
 * </ul>
 * This software is provided by the copyright holders and contributors "as is" and any express or implied warranties,
 * including, but not limited to, the implied warranties of merchantability and fitness for a particular purpose are
 * disclaimed. In no event shall the copyright holder or contributors be liable for any direct, indirect, incidental,
 * special, exemplary, or consequential damages (including, but not limited to, procurement of substitute goods or
 * services; loss of use, data, or profits; or business interruption) however caused and on any theory of liability,
 * whether in contract, strict liability, or tort (including negligence or otherwise) arising in any way out of the use
 * of this software, even if advised of the possibility of such damage. $LastChangedDate: 2015-07-15 11:18:39 +0200
 * (Wed, 15 Jul 2015) $, @version $Revision$, by $Author$, initial version 30 Oct 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://Hansvanlint.weblog.tudelft.nl">Hans van Lint</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.citg.tudelft.nl">Guus Tamminga</a>
 * @author <a href="http://www.citg.tudelft.nl">Yufei Yuan</a>
 * @param <ID> ID type of the sub network
 * @param <N> the node type
 * @param <L> Link type
 */
public class SubNetwork<ID, N extends Node<?, ?>, L extends Link<?, N>> extends Network<ID, N, L>
{

    /** */
    private static final long serialVersionUID = 20141112L;

    /** The Links in this SubNetwork. */
    private Set<L> links2Out = new HashSet<L>();

    /**
     * Construct a new SubNetwork.
     * @param id ID; the Id of the new SubNetwork
     */
    public SubNetwork(final ID id)
    {
        super(id);
    }

    /**
     * Retrieve the Links in this SubNetwork.
     * @return Set&lt;L&gt;; the links in this SubNetwork
     */
    public final Set<L> getLinks2Out()
    {
        // TODO should probably return a copy of the set
        return this.links2Out;
    }

}