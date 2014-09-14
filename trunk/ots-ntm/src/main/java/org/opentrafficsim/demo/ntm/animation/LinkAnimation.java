package org.opentrafficsim.demo.ntm.animation;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.Line2D;
import java.awt.image.ImageObserver;
import java.rmi.RemoteException;

import javax.naming.NamingException;

import nl.tudelft.simulation.dsol.animation.D2.Renderable2D;

import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.demo.ntm.Link;

import com.vividsolutions.jts.geom.Point;

/**
 * <p>
 * Copyright (c) 2002-2014 Delft University of Technology, Jaffalaan 5, 2628 BX Delft, the Netherlands. All rights
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
 * of this software, even if advised of the possibility of such damage.
 * @version Sep 13, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class LinkAnimation extends Renderable2D
{
    /** stroke width for drawing links */
    private final float width;
    
    /**
     * @param source
     * @param simulator
     * @param width
     * @throws NamingException
     * @throws RemoteException
     */
    public LinkAnimation(Link source, OTSSimulatorInterface simulator, float width) throws NamingException, RemoteException
    {
        super(source, simulator);
        this.width = width;
    }

    /**
     * @see nl.tudelft.simulation.animation.Renderable2D#paint(java.awt.Graphics2D, java.awt.image.ImageObserver)
     */
    @Override
    public void paint(Graphics2D graphics, ImageObserver observer) throws RemoteException
    {
        graphics.setColor(Color.BLACK);
        Stroke oldStroke = graphics.getStroke();
        graphics.setStroke(new BasicStroke(this.width));
        Point a = ((Link) getSource()).getNodeA().getCentroid();
        Point b = ((Link) getSource()).getNodeB().getCentroid();
        // draw relative to point A (getLocation)
        graphics.draw(new Line2D.Double(0.0, 0.0, b.getX() - a.getX(), a.getY() - b.getY()));
        graphics.setStroke(oldStroke);
    }

}
