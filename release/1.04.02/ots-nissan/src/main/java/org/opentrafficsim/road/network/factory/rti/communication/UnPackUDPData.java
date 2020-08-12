package org.opentrafficsim.road.network.factory.rti.communication;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.opentrafficsim.road.network.factory.rti.data.RTIToOTSData;

/** */
public class UnPackUDPData
{
    /**
     * @param inputStream DataInputStream; input stream of data
     * @return RTIToOTSData element
     * @throws IOException on i/o error
     */
    static RTIToOTSData unPack(DataInputStream inputStream) throws IOException
    {

        RTIToOTSData simData = new RTIToOTSData();

        simData.setTimeStamp(parseFloat(inputStream));
        simData.setDeltaT(parseFloat(inputStream));

        simData.getEgoPos().setX(parseFloat(inputStream));
        simData.getEgoPos().setY(parseFloat(inputStream));
        simData.getEgoPos().setZ(parseFloat(inputStream));

        simData.getEgoOri().setYaw(parseFloat(inputStream));
        simData.getEgoOri().setPitch(parseFloat(inputStream));
        simData.getEgoOri().setRoll(parseFloat(inputStream));

        simData.getEgoVel().setVx(parseFloat(inputStream));
        simData.getEgoVel().setVy(parseFloat(inputStream));
        simData.getEgoVel().setVz(parseFloat(inputStream));

        simData.getEgoAngVel().setYawRate(parseFloat(inputStream));
        simData.getEgoAngVel().setPitchRate(parseFloat(inputStream));
        simData.getEgoAngVel().setRollRate(parseFloat(inputStream));

        simData.setIntersection_type(parseInt(inputStream));
        simData.setIntersection_phase(parseInt(inputStream));
        simData.setIntersection_distance(parseFloat(inputStream));

        // System.out.println(simData);

        return simData;
        // fos.write(receivePacket.getData(), 0, receivePacket.getLength());
        // fos.flush();
        // System.out.println(new String(receivePacket.getData()));

        // break;
        // InetAddress IPAddress = receivePacket.getAddress();
        // int port = receivePacket.getPort();
        // String capitalizedSentence = sentence.toUpperCase();
        /*
         * sendData = capitalizedSentence.getBytes(); DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length,
         * IPAddress, port); serverSocket.send(sendPacket);
         */

    }

    /**
     * @param inputStream DataInputStream; input data stream
     * @return float
     * @throws IOException on i/o error
     */
    private static float parseFloat(DataInputStream inputStream) throws IOException
    {
        byte[] floats = new byte[4];
        for (int i = 0; i < 4; i++)
            floats[i] = inputStream.readByte();

        /*
         * for(int i = 0; i < floats.length / 2; i++) { byte temp = floats[i]; floats[i] = floats[floats.length - i - 1];
         * floats[floats.length - i - 1] = temp; }
         */
        return ByteBuffer.wrap(floats).order(ByteOrder.LITTLE_ENDIAN).getFloat();
    }

    /**
     * @param inputStream DataInputStream; input data stream
     * @return int
     * @throws IOException on i/o error
     */
    private static int parseInt(DataInputStream inputStream) throws IOException
    {
        byte[] intBytes = new byte[4];
        for (int i = 0; i < 4; i++)
            intBytes[i] = inputStream.readByte();

        /*
         * byte[] newIntBytes = new byte[4]; newIntBytes[0] = intBytes[3]; newIntBytes[1] = intBytes[2]; newIntBytes[2] =
         * intBytes[1]; newIntBytes[3] = intBytes[0];
         */

        return ByteBuffer.wrap(intBytes).order(ByteOrder.LITTLE_ENDIAN).getInt();
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "UnPackUDPData []";
    }
}
