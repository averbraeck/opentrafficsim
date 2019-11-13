package org.opentrafficsim.road.network.factory.rti.communication;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.opentrafficsim.road.network.factory.rti.data.OTSToRTIData;

/** */
public class PackUDPData
{
    /**
     * @param data OTSToRTIData; data to pack
     * @return packed bytes
     */
    static byte[] pack(OTSToRTIData data)
    {
        ByteBuffer buffer = ByteBuffer.allocate(2720);
        // buffer.order(ByteOrder.BIG_ENDIAN);

        buffer.put(packLong(data.getTimeStamp()));
        buffer.put(packInt(data.getNumCars()));
        buffer.put(packInt(data.getNumPedestrians()));
        buffer.put(packInt(data.getNumObjects()));

        // other vehicles
        for (int i = 0; i < data.getNumCars(); i++)
        {
            buffer.put(packFloat(data.getExoPos().get(i).getX()));
            buffer.put(packFloat(data.getExoPos().get(i).getY()));
            buffer.put(packFloat(data.getExoPos().get(i).getZ()));
        }

        for (int i = 0; i < data.getNumCars(); i++)
        {
            buffer.put(packFloat(data.getExoOri().get(i).getYaw()));
            buffer.put(packFloat(data.getExoOri().get(i).getPitch()));
            buffer.put(packFloat(data.getExoOri().get(i).getRoll()));
        }

        for (int i = 0; i < data.getNumCars(); i++)
        {
            buffer.put(packFloat(data.getExoVel().get(i).getVx()));
            buffer.put(packFloat(data.getExoVel().get(i).getVy()));
            buffer.put(packFloat(data.getExoVel().get(i).getVz()));
        }

        // Pedestrian
        for (int i = 0; i < data.getNumPedestrians(); i++)
        {
            buffer.put(packFloat(data.getPedPos().get(i).getX()));
            buffer.put(packFloat(data.getPedPos().get(i).getY()));
            buffer.put(packFloat(data.getPedPos().get(i).getZ()));
        }

        for (int i = 0; i < data.getNumPedestrians(); i++)
        {
            buffer.put(packFloat(data.getPedOri().get(i).getYaw()));
            buffer.put(packFloat(data.getPedOri().get(i).getPitch()));
            buffer.put(packFloat(data.getPedOri().get(i).getRoll()));
        }

        for (int i = 0; i < data.getNumPedestrians(); i++)
        {
            buffer.put(packFloat(data.getPedVel().get(i).getVx()));
            buffer.put(packFloat(data.getPedVel().get(i).getVy()));
            buffer.put(packFloat(data.getPedVel().get(i).getVz()));
        }

        // objectState
        for (int i = 0; i < data.getNumObjects(); i++)
        {
            buffer.put(packFloat(data.getObjPos().get(i).getX()));
            buffer.put(packFloat(data.getObjPos().get(i).getY()));
            buffer.put(packFloat(data.getObjPos().get(i).getZ()));
        }

        for (int i = 0; i < data.getNumObjects(); i++)
        {
            buffer.put(packFloat(data.getObjOri().get(i).getYaw()));
            buffer.put(packFloat(data.getObjOri().get(i).getPitch()));
            buffer.put(packFloat(data.getObjOri().get(i).getRoll()));
        }

        for (int i = 0; i < data.getNumObjects(); i++)
        {
            buffer.put(packFloat(data.getObjVel().get(i).getVx()));
            buffer.put(packFloat(data.getObjVel().get(i).getVy()));
            buffer.put(packFloat(data.getObjVel().get(i).getVz()));
        }

        return buffer.array();

    }

    /**
     * @param value float; value to pack
     * @return byte array
     */
    public static byte[] packFloat(float value)
    {
        return ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putFloat(value).array();
    }

    /**
     * @param value long; value to pack
     * @return byte array
     */
    public static byte[] packLong(long value)
    {
        return ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN).putLong(value).array();
    }

    /**
     * @param value int; value to pack
     * @return byte array
     */
    private static byte[] packInt(int value)
    {
        /*
         * byte[] newIntBytes = new byte[4]; newIntBytes[0] = intBytes[3]; newIntBytes[1] = intBytes[2]; newIntBytes[2] =
         * intBytes[1]; newIntBytes[3] = intBytes[0];
         */

        // return ByteBuffer.wrap(intBytes).order(ByteOrder.LITTLE_ENDIAN).getInt();
        return ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(value).array();
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "PackUDPData []";
    }
}
