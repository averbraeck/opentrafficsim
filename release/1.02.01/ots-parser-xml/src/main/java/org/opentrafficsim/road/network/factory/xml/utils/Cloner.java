package org.opentrafficsim.road.network.factory.xml.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.opentrafficsim.road.network.factory.xml.XmlParserException;
import org.opentrafficsim.xml.generated.BASICROADLAYOUT;
import org.opentrafficsim.xml.generated.CSELANE;
import org.opentrafficsim.xml.generated.CSENOTRAFFICLANE;
import org.opentrafficsim.xml.generated.CSESHOULDER;
import org.opentrafficsim.xml.generated.CSESTRIPE;
import org.opentrafficsim.xml.generated.ROADLAYOUT;

/**
 * Cloner makes a deep clone of any serializable object with serializable fields. <br>
 * <br>
 * Copyright (c) 2003-2018 Delft University of Technology, Jaffalaan 5, 2628 BX Delft, the Netherlands. All rights reserved. See
 * for project information <a href="https://www.simulation.tudelft.nl/" target="_blank">www.simulation.tudelft.nl</a>. The
 * source code and binary code of this software is proprietary information of Delft University of Technology.
 * @author <a href="https://www.tudelft.nl/averbraeck" target="_blank">Alexander Verbraeck</a>
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
    public static <T> T clone(T object) throws XmlParserException
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
     * Clone the BASICROADLAYOUT, as not all DJUNIT types are serializable...
     * @param in the object to clone
     * @return the cloned object
     */
    public static ROADLAYOUT cloneRoadLayout(BASICROADLAYOUT in)
    {
        ROADLAYOUT rl = new ROADLAYOUT();
        rl.setBase(in.getBase());
        rl.setLANEKEEPING(in.getLANEKEEPING());
        rl.setLINKTYPE(in.getBase());
        rl.getSPEEDLIMIT().addAll(in.getSPEEDLIMIT());
        for (Object o : in.getLANEOrNOTRAFFICLANEOrSHOULDER())
        {
            if (o instanceof CSELANE)
            {
                CSELANE lane = (CSELANE) o;
                CSELANE lc = new CSELANE();
                lc.setCENTEROFFSET(lane.getCENTEROFFSET());
                lc.setCENTEROFFSETEND(lane.getCENTEROFFSETEND());
                lc.setCENTEROFFSETSTART(lane.getCENTEROFFSETSTART());
                lc.setLEFTOFFSET(lane.getLEFTOFFSET());
                lc.setLEFTOFFSETEND(lane.getLEFTOFFSETEND());
                lc.setLEFTOFFSETSTART(lane.getLEFTOFFSETSTART());
                lc.setRIGHTOFFSET(lane.getRIGHTOFFSET());
                lc.setRIGHTOFFSETEND(lane.getRIGHTOFFSETEND());
                lc.setRIGHTOFFSETSTART(lane.getRIGHTOFFSETSTART());
                lc.setDESIGNDIRECTION(lane.isDESIGNDIRECTION());
                lc.setLANETYPE(lane.getLANETYPE());
                lc.setID(lane.getID());
                lc.setWIDTH(lane.getWIDTH());
                lc.setWIDTHEND(lane.getWIDTHEND());
                lc.setWIDTHSTART(lane.getWIDTHSTART());
                lc.getSPEEDLIMIT().addAll(lane.getSPEEDLIMIT());
                rl.getLANEOrNOTRAFFICLANEOrSHOULDER().add(lc);
            }

            else if (o instanceof CSENOTRAFFICLANE)
            {
                CSENOTRAFFICLANE ntl = (CSENOTRAFFICLANE) o;
                CSENOTRAFFICLANE ntlc = new CSENOTRAFFICLANE();
                ntlc.setCENTEROFFSET(ntl.getCENTEROFFSET());
                ntlc.setCENTEROFFSETEND(ntl.getCENTEROFFSETEND());
                ntlc.setCENTEROFFSETSTART(ntl.getCENTEROFFSETSTART());
                ntlc.setLEFTOFFSET(ntl.getLEFTOFFSET());
                ntlc.setLEFTOFFSETEND(ntl.getLEFTOFFSETEND());
                ntlc.setLEFTOFFSETSTART(ntl.getLEFTOFFSETSTART());
                ntlc.setRIGHTOFFSET(ntl.getRIGHTOFFSET());
                ntlc.setRIGHTOFFSETEND(ntl.getRIGHTOFFSETEND());
                ntlc.setRIGHTOFFSETSTART(ntl.getRIGHTOFFSETSTART());
                ntlc.setID(ntl.getID());
                ntlc.setWIDTH(ntl.getWIDTH());
                ntlc.setWIDTHEND(ntl.getWIDTHEND());
                ntlc.setWIDTHSTART(ntl.getWIDTHSTART());
                rl.getLANEOrNOTRAFFICLANEOrSHOULDER().add(ntlc);
            }

            else if (o instanceof CSESHOULDER)
            {
                CSESHOULDER shoulder = (CSESHOULDER) o;
                CSESHOULDER sc = new CSESHOULDER();
                sc.setCENTEROFFSET(shoulder.getCENTEROFFSET());
                sc.setCENTEROFFSETEND(shoulder.getCENTEROFFSETEND());
                sc.setCENTEROFFSETSTART(shoulder.getCENTEROFFSETSTART());
                sc.setLEFTOFFSET(shoulder.getLEFTOFFSET());
                sc.setLEFTOFFSETEND(shoulder.getLEFTOFFSETEND());
                sc.setLEFTOFFSETSTART(shoulder.getLEFTOFFSETSTART());
                sc.setRIGHTOFFSET(shoulder.getRIGHTOFFSET());
                sc.setRIGHTOFFSETEND(shoulder.getRIGHTOFFSETEND());
                sc.setRIGHTOFFSETSTART(shoulder.getRIGHTOFFSETSTART());
                sc.setID(shoulder.getID());
                sc.setWIDTH(shoulder.getWIDTH());
                sc.setWIDTHEND(shoulder.getWIDTHEND());
                sc.setWIDTHSTART(shoulder.getWIDTHSTART());
                rl.getLANEOrNOTRAFFICLANEOrSHOULDER().add(sc);
            }

            else if (o instanceof CSESTRIPE)
            {
                CSESTRIPE stripe = (CSESTRIPE) o;
                CSESTRIPE sc = new CSESTRIPE();
                sc.setCENTEROFFSET(stripe.getCENTEROFFSET());
                sc.setCENTEROFFSETEND(stripe.getCENTEROFFSETEND());
                sc.setCENTEROFFSETSTART(stripe.getCENTEROFFSETSTART());
                sc.setID(stripe.getID());
                sc.setDRAWINGWIDTH(stripe.getDRAWINGWIDTH());
                sc.setTYPE(stripe.getTYPE());
                rl.getLANEOrNOTRAFFICLANEOrSHOULDER().add(sc);
            }
        }
        return rl;
    }
}
