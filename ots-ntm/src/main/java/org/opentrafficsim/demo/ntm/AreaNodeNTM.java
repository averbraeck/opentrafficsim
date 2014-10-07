package org.opentrafficsim.demo.ntm;

import java.rmi.RemoteException;

import javax.media.j3d.BoundingSphere;
import javax.media.j3d.Bounds;
import javax.vecmath.Point3d;

import nl.tudelft.simulation.language.d3.DirectedPoint;

import org.opentrafficsim.core.network.AbstractNode;

import com.vividsolutions.jts.geom.Point;

/**
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version Sep 12, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://Hansvanlint.weblog.tudelft.nl">Hans van Lint</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.citg.tudelft.nl">Guus Tamminga</a>
 * @author <a href="http://www.citg.tudelft.nl">Yufei Yuan</a>
 */
public class AreaNodeNTM extends AreaNode
{
    /** */
    private static final long serialVersionUID = 20140920L;

    /** */
    public CellBehaviourNTM cellBehaviourNTM;
    

    /**
     * @param centroid the center of the area for the simplified graph.
     * @param area the area to which the node belongs.
     * @param parametersNTM  
     */
    public AreaNodeNTM(final Point centroid, final Area area, final ParametersNTM parametersNTM)
    {
        super(area.getCentroidNr(), centroid, area);
        this.setCellBehaviourNTM(new CellBehaviourNTM(parametersNTM));
    }



    /**
     * @return cellBehaviourNTM.
     */
    public final CellBehaviourNTM getCellBehaviourNTM()
    {
        return this.cellBehaviourNTM;
    }

    /**
     * @param cellBehaviourNTM set cellBehaviourNTM.
     */
    public final void setCellBehaviourNTM(final CellBehaviourNTM cellBehaviourNTM)
    {
        this.cellBehaviourNTM = cellBehaviourNTM;
    }



}
