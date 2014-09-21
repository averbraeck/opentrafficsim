package org.opentrafficsim.core.network;

import java.io.Serializable;

import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;

/**
 * <p>
 * Copyright (c) 2002-2014 Delft University of Technology, Jaffalaan 5, 2628 BX Delft, the Netherlands. All rights reserved.
 * <p>
 * See for project information <a href="http://www.simulation.tudelft.nl/"> www.simulation.tudelft.nl</a>.
 * <p>
 * The OpenTrafficSim project is distributed under the following BSD-style license:<br>
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following
 * conditions are met:
 * <ul>
 * <li>Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 * disclaimer.</li>
 * <li>Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided with the distribution.</li>
 * <li>Neither the name of Delft University of Technology, nor the names of its contributors may be used to endorse or promote
 * products derived from this software without specific prior written permission.</li>
 * </ul>
 * This software is provided by the copyright holders and contributors "as is" and any express or implied warranties, including,
 * but not limited to, the implied warranties of merchantability and fitness for a particular purpose are disclaimed. In no
 * event shall the copyright holder or contributors be liable for any direct, indirect, incidental, special, exemplary, or
 * consequential damages (including, but not limited to, procurement of substitute goods or services; loss of use, data, or
 * profits; or business interruption) however caused and on any theory of liability, whether in contract, strict liability, or
 * tort (including negligence or otherwise) arising in any way out of the use of this software, even if advised of the
 * possibility of such damage.
 * @version Aug 19, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.citg.tudelft.nl">Guus Tamminga</a>
 * @param <ID>
 */
public class Link<ID> implements Serializable
{
    /** */
    private static final long serialVersionUID = 20140920L;

    /** link id. */
    private final ID id;

    /** begin node (directional). */
    private final Node<?> beginNode;

    /** end node (directional). */
    private final Node<?> endNode;

    /** link length in a length unit. */
    private final DoubleScalar<LengthUnit> length;

    /** link capacity in vehicles per hour. This is a mutable property (e.g., blockage). */
    private double capacity;

    /** link resistance in TODO: unit. This is a mutable property (e.g., blockage). */
    private double resistance;

    /**
     * Construction of a link.
     * @param id the link id.
     * @param beginNode begin node (directional).
     * @param endNode end node (directional).
     * @param length link length in a length unit.
     * @param capacity link capacity in vehicles per hour.
     * @param resistance link resistance in ???????? unit.
     */
    public Link(final ID id, final Node<?> beginNode, final Node<?> endNode, final DoubleScalar<LengthUnit> length,
            final double capacity, final double resistance)
    {
        this.id = id;
        this.beginNode = beginNode;
        this.endNode = endNode;
        this.length = length;
        setCapacity(capacity);
        setResistance(resistance);

    }

    /**
     * @return link length.
     */
    public final DoubleScalar<LengthUnit> getLenght()
    {
        return this.length;
    }

    /**
     * @return id.
     */
    public final ID getId()
    {
        return this.id;
    }

    /**
     * @return link capacity.
     */
    public final double getCapacity()
    {
        return this.getLinkCapacity();
    }

    /**
     * @return begin node.
     */
    public final Node<?> getBeginNode()
    {
        return this.beginNode;
    }

    /**
     * @return end node.
     */
    public final Node<?> getEndNode()
    {
        return this.endNode;
    }

    /**
     * @return link capacity.
     */
    public final double getLinkCapacity()
    {
        return this.capacity;
    }

    /**
     * @param capacity set the link capacity.
     */
    public final void setCapacity(final double capacity)
    {
        this.capacity = capacity;
    }

    /**
     * @return link resistance.
     */
    public final double getResistance()
    {
        return this.resistance;
    }

    /**
     * @param resistance set link resistance.
     */
    public final void setResistance(final double resistance)
    {
        this.resistance = resistance;
    }

}
