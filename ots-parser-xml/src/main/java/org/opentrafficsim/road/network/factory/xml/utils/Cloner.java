package org.opentrafficsim.road.network.factory.xml.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.opentrafficsim.road.network.factory.xml.XmlParserException;
import org.opentrafficsim.xml.generated.BasicRoadLayout;
import org.opentrafficsim.xml.generated.CseLane;
import org.opentrafficsim.xml.generated.CseNoTrafficLane;
import org.opentrafficsim.xml.generated.CseShoulder;
import org.opentrafficsim.xml.generated.CseStripe;
import org.opentrafficsim.xml.generated.RoadLayout;

/**
 * Cloner makes a deep clone of any serializable object with serializable fields.
 * <p>
 * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck" target="_blank">Alexander Verbraeck</a>
 */
public final class Cloner
{
    /** */
    private Cloner()
    {
        // utility class
    }

    /**
     * Clone an object that is serializable and that has serializable fields.
     * @param object the object to clone
     * @param <T> the type of the object to clone
     * @return the clone of the object
     * @throws XmlParserException on cloning error
     */
    @SuppressWarnings("unchecked")
    public static <T> T clone(final T object) throws XmlParserException
    {
        try
        {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(object);
            ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
            ObjectInputStream ois = new ObjectInputStream(bais);
            return (T) ois.readObject();
        }
        catch (SecurityException | IOException | ClassNotFoundException exception)
        {
            throw new XmlParserException(exception);
        }
    }

    /**
     * Clone the BasicRoadLayout, as not all DJUNIT types are serializable...
     * @param in the object to clone
     * @return the cloned object
     */
    public static RoadLayout cloneRoadLayout(final BasicRoadLayout in)
    {
        RoadLayout rl = new RoadLayout();
        rl.setBase(in.getBase());
        rl.setLaneKeeping(in.getLaneKeeping());
        rl.setLinkType(in.getBase());
        rl.getSpeedLimit().addAll(in.getSpeedLimit());
        for (Object o : in.getStripeOrLaneOrShoulder())
        {
            if (o instanceof CseLane)
            {
                CseLane lane = (CseLane) o;
                CseLane lc = new CseLane();
                lc.setCenterOffset(lane.getCenterOffset());
                lc.setCenterOffsetEnd(lane.getCenterOffsetEnd());
                lc.setCenterOffsetStart(lane.getCenterOffsetStart());
                lc.setLeftOffset(lane.getLeftOffset());
                lc.setLeftOffsetEnd(lane.getLeftOffsetEnd());
                lc.setLeftOffsetStart(lane.getLeftOffsetStart());
                lc.setRightOffset(lane.getRightOffset());
                lc.setRightOffsetEnd(lane.getRightOffsetEnd());
                lc.setRightOffsetStart(lane.getRightOffsetStart());
                lc.setDesignDirection(lane.isDesignDirection());
                lc.setLaneType(lane.getLaneType());
                lc.setId(lane.getId());
                lc.setWidth(lane.getWidth());
                lc.setWidthEnd(lane.getWidthEnd());
                lc.setWidthStart(lane.getWidthStart());
                lc.getSpeedLimit().addAll(lane.getSpeedLimit());
                rl.getStripeOrLaneOrShoulder().add(lc);
            }

            else if (o instanceof CseNoTrafficLane)
            {
                CseNoTrafficLane ntl = (CseNoTrafficLane) o;
                CseNoTrafficLane ntlc = new CseNoTrafficLane();
                ntlc.setCenterOffset(ntl.getCenterOffset());
                ntlc.setCenterOffsetEnd(ntl.getCenterOffsetEnd());
                ntlc.setCenterOffsetStart(ntl.getCenterOffsetStart());
                ntlc.setLeftOffset(ntl.getLeftOffset());
                ntlc.setLeftOffsetEnd(ntl.getLeftOffsetEnd());
                ntlc.setLeftOffsetStart(ntl.getLeftOffsetStart());
                ntlc.setRightOffset(ntl.getRightOffset());
                ntlc.setRightOffsetEnd(ntl.getRightOffsetEnd());
                ntlc.setRightOffsetStart(ntl.getRightOffsetStart());
                ntlc.setId(ntl.getId());
                ntlc.setWidth(ntl.getWidth());
                ntlc.setWidthEnd(ntl.getWidthEnd());
                ntlc.setWidthStart(ntl.getWidthStart());
                rl.getStripeOrLaneOrShoulder().add(ntlc);
            }

            else if (o instanceof CseShoulder)
            {
                CseShoulder shoulder = (CseShoulder) o;
                CseShoulder sc = new CseShoulder();
                sc.setCenterOffset(shoulder.getCenterOffset());
                sc.setCenterOffsetEnd(shoulder.getCenterOffsetEnd());
                sc.setCenterOffsetStart(shoulder.getCenterOffsetStart());
                sc.setLeftOffset(shoulder.getLeftOffset());
                sc.setLeftOffsetEnd(shoulder.getLeftOffsetEnd());
                sc.setLeftOffsetStart(shoulder.getLeftOffsetStart());
                sc.setRightOffset(shoulder.getRightOffset());
                sc.setRightOffsetEnd(shoulder.getRightOffsetEnd());
                sc.setRightOffsetStart(shoulder.getRightOffsetStart());
                sc.setId(shoulder.getId());
                sc.setWidth(shoulder.getWidth());
                sc.setWidthEnd(shoulder.getWidthEnd());
                sc.setWidthStart(shoulder.getWidthStart());
                rl.getStripeOrLaneOrShoulder().add(sc);
            }

            else if (o instanceof CseStripe)
            {
                CseStripe stripe = (CseStripe) o;
                CseStripe sc = new CseStripe();
                sc.setCenterOffset(stripe.getCenterOffset());
                sc.setCenterOffsetEnd(stripe.getCenterOffsetEnd());
                sc.setCenterOffsetStart(stripe.getCenterOffsetStart());
                sc.setId(stripe.getId());
                sc.setDrawingWidth(stripe.getDrawingWidth());
                sc.setType(stripe.getType());
                rl.getStripeOrLaneOrShoulder().add(sc);
            }
        }
        return rl;
    }
}
