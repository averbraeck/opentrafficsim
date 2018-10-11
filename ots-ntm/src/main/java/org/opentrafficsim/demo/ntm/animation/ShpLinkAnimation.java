package org.opentrafficsim.demo.ntm.animation;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.Path2D;
import java.awt.image.ImageObserver;
import java.rmi.RemoteException;

import javax.naming.NamingException;

import org.opentrafficsim.demo.ntm.NTMLink;

import nl.tudelft.simulation.dsol.animation.D2.Renderable2D;
import nl.tudelft.simulation.dsol.simulators.SimulatorInterface;

/**
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author$,
 * initial version Sep 13, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class ShpLinkAnimation extends Renderable2D
{
    /** */
    private float width;

    /** */
    private Color color;

    /**
     * @param source NTMLink;
     * @param simulator SimulatorInterface.TimeDoubleUnit;
     * @throws NamingException
     * @throws RemoteException
     */
    public ShpLinkAnimation(NTMLink source, SimulatorInterface.TimeDoubleUnit simulator, final float width, Color color)
            throws NamingException, RemoteException
    {
        super(source, simulator);
        this.width = width;
        this.color = color;
    }

    /** {@inheritDoc} */
    @Override
    public void paint(Graphics2D graphics, ImageObserver observer) throws RemoteException
    {
        graphics.setColor(Color.RED);
        for (Path2D line : ((NTMLink) getSource()).getLines())
        {
            Stroke oldStroke = graphics.getStroke();
            graphics.setStroke(new BasicStroke(this.width));
            graphics.setColor(this.color);
            graphics.draw(line);
            graphics.setStroke(oldStroke);

        }
    }

}
