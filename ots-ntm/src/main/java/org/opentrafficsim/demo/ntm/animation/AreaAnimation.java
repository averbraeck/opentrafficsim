package org.opentrafficsim.demo.ntm.animation;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.Path2D;
import java.awt.image.ImageObserver;
import java.rmi.RemoteException;

import javax.naming.NamingException;

import nl.tudelft.simulation.dsol.animation.D2.Renderable2D;

import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.demo.ntm.Area;

/**
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version Sep 13, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class AreaAnimation extends Renderable2D
{
    private float width;
    private Color colorArea;
    private Color colorBorder;

    /**
     * @param source
     * @param simulator
     * @param width 
     * @param traffic 
     * @throws NamingException
     * @throws RemoteException
     */
    public AreaAnimation(Area source, OTSSimulatorInterface simulator, final float width) throws NamingException,
            RemoteException
    {
        super(source, simulator);
        this.width = width;
        if (source.getAccumulatedCars() > 0)
        {
            this.colorArea = colorFor(normalize(0, 5, source.getAccumulatedCars()));
        }
        else
        {
            this.colorArea = Color.YELLOW;    
        }

        //
        float[] hsv = new float[3];
//        Color.RGBtoHSB(r,g,b,hsv);
        //Color.RGBtoHSB(130,0,0,hsv);
        if (source.getRegio() == "Missing") {
            this.colorBorder = Color.ORANGE;
        }
        else if (source.getRegio() == "cordonPoint") {
            this.colorBorder = Color.RED;
            this.width = 5;
        }
        else {
            this.colorBorder = Color.BLACK;
        }
    }

    /** {@inheritDoc} */
    @Override
    public void paint(Graphics2D graphics, ImageObserver observer) throws RemoteException
    {
        for (Path2D polygon : ((Area) getSource()).getPolygons())
        {
            graphics.setColor(Color.BLACK);
            Stroke oldStroke = graphics.getStroke();
            graphics.setStroke(new BasicStroke(this.width));
            graphics.setColor(this.colorArea);
            graphics.fill(polygon);
            graphics.setColor(this.colorBorder);
            graphics.draw(polygon);
            graphics.setStroke(oldStroke);
        }
    }
    
    private static Color colorFor(double value) {
        value = Math.max(0, Math.min(1, value));
        int red = (int)(value * 255);
        return new Color(red,0,0);
    }
    
    private static double normalize(double min, double max, double value) {
        return (value - min) / (max - min);
    }
    

}
