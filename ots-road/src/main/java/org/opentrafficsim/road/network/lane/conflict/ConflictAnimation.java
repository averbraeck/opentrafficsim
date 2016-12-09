package org.opentrafficsim.road.network.lane.conflict;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.ImageObserver;
import java.io.Serializable;
import java.rmi.RemoteException;

import javax.naming.NamingException;

import org.opentrafficsim.core.dsol.OTSSimulatorInterface;

import nl.tudelft.simulation.dsol.animation.D2.Renderable2D;

/**
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 7 dec. 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class ConflictAnimation extends Renderable2D implements Serializable
{

    /** */
    private static final long serialVersionUID = 20161207L;

    /** The half width left and right of the center line that is used to draw the block. */
    private final double halfWidth;
    
    /**
     * @param source the conflict to draw
     * @param simulator the simulator to schedule on
     * @throws NamingException in case of registration failure of the animation
     * @throws RemoteException on communication failure
     */
    public ConflictAnimation(final Conflict source, final OTSSimulatorInterface simulator) throws NamingException, RemoteException
    {
        super(source, simulator);
        this.halfWidth = 0.45 * source.getLane().getWidth(source.getLongitudinalPosition()).getSI();
    }

    /** {@inheritDoc} */
    @Override
    public final void paint(final Graphics2D graphics, final ImageObserver observer) throws RemoteException
    {
        Conflict conflict = (Conflict) this.getSource();
        Color fillColor;
        switch (conflict.getConflictRule())
        {
            case SPLIT:
                fillColor = Color.blue;
                break;

            case PRIORITY:
                fillColor = Color.green;
                break;

            case GIVE_WAY:
                fillColor = Color.orange;
                break;
                
            default:
                // STOP, ALL_STOP
                fillColor = Color.red;
                break;
        }
        
        // TODO geometry, as soon as conflicts have that
        // PaintPolygons.paintMultiPolygon(graphics, fillColor, conflict.getLocation(), conflict.getGeometry(), false);
        graphics.setColor(fillColor);
        Rectangle2D rectangle = new Rectangle2D.Double(-0.25, -this.halfWidth, 0.5, 2 * this.halfWidth);
        graphics.fill(rectangle);
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "ConflictAnimation [getSource()=" + getSource() + "]";
    }

}
