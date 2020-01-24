package org.opentrafficsim.demo.ntm.animation;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.Path2D;
import java.awt.image.ImageObserver;
import java.rmi.RemoteException;

import javax.naming.NamingException;

import org.djunits.unit.SpeedUnit;
import org.opentrafficsim.demo.ntm.Area;
import org.opentrafficsim.demo.ntm.AreaFlowLink;
import org.opentrafficsim.demo.ntm.FlowCell;

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
public class AreaFlowLinkAnimation extends Renderable2D
{
    private float width;

    private int x;

    private int y;

    private Color colorArea;

    private Color colorBorder;

    /**
     * @param source AreaFlowLink;
     * @param simulator SimulatorInterface.TimeDoubleUnit;
     * @param width float;
     * @param traffic
     * @throws NamingException
     * @throws RemoteException
     */
    public AreaFlowLinkAnimation(AreaFlowLink source, SimulatorInterface.TimeDoubleUnit simulator, final float width)
            throws NamingException, RemoteException
    {
        super(source, simulator);
        this.width = width;
        this.x = (int) source.getGeometry().getInteriorPoint().getCoordinate().x;
        this.y = (int) source.getGeometry().getInteriorPoint().getCoordinate().y;
        FlowCell cell = source.getFlowLink().getCells().get(source.getIndexCell());
        // if (source.getAccumulatedCars() > 0)
        if (cell.getCellBehaviourFlow().getCurrentSpeed() != null)
        {
            if (cell.getCellBehaviourFlow().getCurrentSpeed().getInUnit(SpeedUnit.KM_PER_HOUR) > 0)
            {
                this.colorArea = colorFor(
                        normalize(0, 100, cell.getCellBehaviourFlow().getCurrentSpeed().getInUnit(SpeedUnit.KM_PER_HOUR)));
                // this.colorArea = Color.RED;
            }
        }
        else
        {
            this.colorArea = Color.GREEN;
        }

        //
        // float[] hsv = new float[3];
        // Color.RGBtoHSB(r,g,b,hsv);
        // Color.RGBtoHSB(130,0,0,hsv);
        if (source.getRegio() == "Missing")
        {
            this.colorBorder = Color.ORANGE;
        }
        else if (source.getRegio() == "cordonPoint")
        {
            this.colorBorder = Color.RED;
            this.width = 5;
        }
        else
        {
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
            graphics.drawString("Text", this.x, this.y);
            graphics.setStroke(oldStroke);
        }
    }

    private static Color colorFor(double value)
    {
        value = Math.max(0, Math.min(1, value));
        // int red = (int) (value * 255);
        Float red = (float) value;
        Float green = (float) (1 - value);
        Float blue = (float) 0.0;
        Float[] color = RGBtoHSV(red, green, blue);
        return Color.getHSBColor(color[0] / 360, color[1], color[2]);
        // return new Color(red, green, black);
    }

    private static double normalize(double min, double max, double value)
    {
        return (value - min) / (max - min);
    }

    /**
     * @param red float;
     * @param green float;
     * @param blue float;
     * @param h
     * @param s
     * @param v
     * @return
     */
    public static Float[] RGBtoHSV(float red, float green, float blue)
    {
        Float[] color = new Float[3];
        float min, max, delta, h, v, s;
        min = Math.min(red, green);
        min = Math.min(min, blue);
        max = Math.max(red, green);
        max = Math.max(max, blue);
        v = max; // v
        delta = max - min;
        if (max != 0)
            s = delta / max; // s
        else
        {
            // r = g = b = 0 // s = 0, v is undefined
            s = 0;
            h = -1;
        }
        if (red == max)
            h = (green - blue) / delta; // between yellow & magenta
        else if (green == max)
            h = 2 + (blue - red) / delta; // between cyan & yellow
        else
            h = 4 + (red - green) / delta; // between magenta & cyan
        h *= 60; // degrees
        if (h < 0)
            h += 360;
        color[0] = h;
        color[1] = s;
        color[2] = v;

        return color;
    }
}
