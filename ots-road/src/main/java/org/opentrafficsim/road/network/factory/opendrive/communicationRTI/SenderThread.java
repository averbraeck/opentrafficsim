package org.opentrafficsim.road.network.factory.opendrive.communicationRTI;

import java.net.DatagramPacket;
import java.net.InetAddress;

import org.opentrafficsim.road.network.factory.opendrive.data.OTSToRTIData;

/**
 * <br />
 * Copyright (c) 2013-2014 Rijkswaterstaat - Dienst Water, Verkeer en Leefomgeving. All rights reserved. <br />
 * Some parts of the software (c) 2011-2014 TU Delft, Faculty of TBM, Systems & Simulation <br />
 * This software is licensed without restrictions to Nederlandse Organisatie voor Toegepast Natuurwetenschappelijk
 * Onderzoek TNO (TNO), Erasmus University Rotterdam, Delft University of Technology, Panteia B.V., Stichting Projecten
 * Binnenvaart, Ab Ovo Nederland B.V., Modality Software Solutions B.V., and Rijkswaterstaat - Dienst Water, Verkeer en
 * Leefomgeving, including the right to sub-license sources and derived products to third parties. <br />
 * 
 * @version Mar 24, 2013 <br>
 * @author <a href="http://tudelft.nl/averbraeck">Alexander Verbraeck </a>
 * @version SVN $Revision: 31 $ $Author: averbraeck $
 * @date $Date: 2011-08-15 04:38:04 +0200 (Mon, 15 Aug 2011) $
 **/
public class SenderThread implements Runnable {

    //InetAddress IPAddress = InetAddress.getLocalHost();
    int port = 8091;
    
    
    //Socket Socket = null;
    DatagramPacket packet = null;

    public SenderThread(OTSToRTIData data) {

    }

    public void run() {
/*        byte[] data = makeResponse(); // code not shown
        DatagramPacket response = new DatagramPacket(data, data.length,
            packet.getAddress(), packet.getPort());
        socket.send(response);
        

        
        //OTSToRTIData data = new OTSToRTIData();
        data.setTimeStamp(System.currentTimeMillis());
        
        byte[] sendData = PackUDPData.pack(data);
        
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port); 
        Socket.send(sendPacket);
        
        System.out.println(data.getTimeStamp() + " \n");
        
        data = null; 
        sendData = null;
        sendPacket = null;*/
        
    }
}