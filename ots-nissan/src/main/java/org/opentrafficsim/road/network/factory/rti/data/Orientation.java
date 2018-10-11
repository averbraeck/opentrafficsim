package org.opentrafficsim.road.network.factory.rti.data;

import java.io.Serializable;

/** */
public class Orientation implements Serializable
{
    /** */
    private static final long serialVersionUID = 20150000L;

    /** */
    private float yaw; // position[m]

    /** */
    private float pitch; // position[m]

    /** */
    private float roll; // position[m]

    /**
     * @param rotX double; rotX
     * @param rotY double; rotY
     * @param rotZ double; rotZ
     */
    public Orientation(double rotX, double rotY, double rotZ)
    {
        this.setPitch((float) rotY);
        this.setRoll((float) rotX);
        this.setYaw((float) rotZ);
    }

    /**
     * 
     */
    public Orientation()
    {
    }

    /** {@inheritDoc} */
    public String toString()
    {
        return "Orientation [yaw=" + this.getYaw() + ", pitch=" + this.getPitch() + ", roll=" + this.getRoll() + "]";
    }

    /**
     * @return yaw
     */
    public float getYaw()
    {
        return this.yaw;
    }

    /**
     * @param yaw float; set yaw
     */
    public void setYaw(float yaw)
    {
        this.yaw = yaw;
    }

    /**
     * @return pitch
     */
    public float getPitch()
    {
        return this.pitch;
    }

    /**
     * @param pitch float; set pitch
     */
    public void setPitch(float pitch)
    {
        this.pitch = pitch;
    }

    /**
     * @return roll
     */
    public float getRoll()
    {
        return this.roll;
    }

    /**
     * @param roll float; set roll
     */
    public void setRoll(float roll)
    {
        this.roll = roll;
    }

}
