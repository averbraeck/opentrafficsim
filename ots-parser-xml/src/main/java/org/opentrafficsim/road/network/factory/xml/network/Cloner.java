package org.opentrafficsim.road.network.factory.xml.network;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.opentrafficsim.road.network.factory.xml.XmlParserException;
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
     * Clone the ROADLAYOUT, as not all DJUNIT types are serializable...
     * @param in the object to clone
     * @return the cloned object
     */
    public static ROADLAYOUT cloneRoadLayout(ROADLAYOUT in)
    {
        ROADLAYOUT rl = new ROADLAYOUT();
        rl.setBase(in.getBase());
        rl.setLANEKEEPING(in.getLANEKEEPING());
        rl.setLINKTYPE(in.getBase());
        rl.setNAME(in.getNAME());
        rl.setOVERTAKING(in.getOVERTAKING());
        rl.getSPEEDLIMIT().addAll(in.getSPEEDLIMIT());

        for (CSELANE lane : Parser.getObjectsOfType(in.getLANEOrNOTRAFFICLANEOrSHOULDER(), CSELANE.class))
        {
            CSELANE lc = new CSELANE();
            lc.setCENTEROFFSET(lane.getCENTEROFFSET());
            lc.setCENTEROFFSETEND(lane.getCENTEROFFSETEND());
            lc.setCENTEROFFSETSTART(lane.getCENTEROFFSETSTART());
            lc.setDIRECTION(lane.getDIRECTION());
            lc.setLANETYPE(lane.getLANETYPE());
            lc.setNAME(lane.getNAME());
            lc.setOVERTAKING(lane.getOVERTAKING());
            lc.setWIDTH(lane.getWIDTH());
            lc.setWIDTHEND(lane.getWIDTHEND());
            lc.setWIDTHSTART(lane.getWIDTHSTART());
            lc.getSPEEDLIMIT().addAll(lane.getSPEEDLIMIT());
            rl.getLANEOrNOTRAFFICLANEOrSHOULDER().add(lc);
        }
        
        for (CSENOTRAFFICLANE ntl : Parser.getObjectsOfType(in.getLANEOrNOTRAFFICLANEOrSHOULDER(), CSENOTRAFFICLANE.class))
        {
            CSENOTRAFFICLANE ntlc = new CSENOTRAFFICLANE();
            ntlc.setCENTEROFFSET(ntl.getCENTEROFFSET());
            ntlc.setCENTEROFFSETEND(ntl.getCENTEROFFSETEND());
            ntlc.setCENTEROFFSETSTART(ntl.getCENTEROFFSETSTART());
            ntlc.setNAME(ntl.getNAME());
            ntlc.setWIDTH(ntl.getWIDTH());
            ntlc.setWIDTHEND(ntl.getWIDTHEND());
            ntlc.setWIDTHSTART(ntl.getWIDTHSTART());
            rl.getLANEOrNOTRAFFICLANEOrSHOULDER().add(ntlc);
        }

        for (CSESHOULDER shoulder : Parser.getObjectsOfType(in.getLANEOrNOTRAFFICLANEOrSHOULDER(), CSESHOULDER.class))
        {
            CSESHOULDER sc = new CSESHOULDER();
            sc.setCENTEROFFSET(shoulder.getCENTEROFFSET());
            sc.setCENTEROFFSETEND(shoulder.getCENTEROFFSETEND());
            sc.setCENTEROFFSETSTART(shoulder.getCENTEROFFSETSTART());
            sc.setNAME(shoulder.getNAME());
            sc.setWIDTH(shoulder.getWIDTH());
            sc.setWIDTHEND(shoulder.getWIDTHEND());
            sc.setWIDTHSTART(shoulder.getWIDTHSTART());
            rl.getLANEOrNOTRAFFICLANEOrSHOULDER().add(sc);
        }

        for (CSESTRIPE stripe : Parser.getObjectsOfType(in.getLANEOrNOTRAFFICLANEOrSHOULDER(), CSESTRIPE.class))
        {
            CSESTRIPE sc = new CSESTRIPE();
            sc.setCENTEROFFSET(stripe.getCENTEROFFSET());
            sc.setCENTEROFFSETEND(stripe.getCENTEROFFSETEND());
            sc.setCENTEROFFSETSTART(stripe.getCENTEROFFSETSTART());
            sc.setNAME(stripe.getNAME());
            sc.setWIDTH(stripe.getWIDTH());
            sc.setTYPE(stripe.getTYPE());
            rl.getLANEOrNOTRAFFICLANEOrSHOULDER().add(sc);
        }

        return rl;
    }
}
