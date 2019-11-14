package org.opentrafficsim.road.network.factory.rti.communication;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import org.opentrafficsim.road.network.factory.rti.data.OTSToRTIData;
import org.opentrafficsim.road.network.factory.rti.data.RTIToOTSData;

/** */
public class CommunicationTest
{

    /**
     * @param args String[]; args
     * @throws IOException i/o exception
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

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "communicationTest []";
    }
}
