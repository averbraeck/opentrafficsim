package org.opentrafficsim.road.network.factory.opendrive.communicationRTI;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import org.opentrafficsim.road.network.factory.opendrive.data.OTSToRTIData;
import org.opentrafficsim.road.network.factory.opendrive.data.RTIToOTSData;

/**
 * <br />
 * Copyright (c) 2013-2014 Rijkswaterstaat - Dienst Water, Verkeer en Leefomgeving. All rights reserved. <br />
 * Some parts of the software (c) 2011-2014 TU Delft, Faculty of TBM, Systems & Simulation <br />
 * This software is licensed without restrictions to Nederlandse Organisatie voor Toegepast Natuurwetenschappelijk Onderzoek TNO
 * (TNO), Erasmus University Rotterdam, Delft University of Technology, Panteia B.V., Stichting Projecten Binnenvaart, Ab Ovo
 * Nederland B.V., Modality Software Solutions B.V., and Rijkswaterstaat - Dienst Water, Verkeer en Leefomgeving, including the
 * right to sub-license sources and derived products to third parties. <br />
 * @version Mar 24, 2013 <br>
 * @author <a href="http://tudelft.nl/averbraeck">Alexander Verbraeck </a>
 * @version SVN $Revision: 31 $ $Author: averbraeck $
 * @date $Date: 2011-08-15 04:38:04 +0200 (Mon, 15 Aug 2011) $
 **/
public class communicationTest
{
    /**
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException
    {
        // FileOutputStream fos = new FileOutputStream(new File("D:\\outtt.dat"));
        System.out.println("Server is ready");
        try
        {
            DatagramSocket Socket = new DatagramSocket(8090);
            byte[] receiveData = new byte[1000000];

            while (receiveData != null)
            {
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                Socket.receive(receivePacket);

                DataInputStream inputStream = new DataInputStream(new ByteArrayInputStream(receivePacket.getData()));

                RTIToOTSData simData = UnPackUDPData.unPack(inputStream);

                // System.out.println(simData);
                inputStream.close();

                simData = null;
                receivePacket = null;
                // fos.write(receivePacket.getData(), 0, receivePacket.getLength());
                // fos.flush();
                // System.out.println(new String(receivePacket.getData()));

                // break;
                InetAddress IPAddress = InetAddress.getLocalHost();
                int port = 8091;

                OTSToRTIData data = new OTSToRTIData();
                data.setTimeStamp(System.currentTimeMillis());

                byte[] sendData = PackUDPData.pack(data);

                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
                Socket.send(sendPacket);

                System.out.println(data.getTimeStamp() + " \n");

                data = null;
                sendData = null;
                sendPacket = null;
            }
            // fos.close();

        }
        catch (Exception e)
        {
            System.err.println(e);
        }
    }
}
