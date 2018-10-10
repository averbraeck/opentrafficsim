package org.opentrafficsim.road.network.factory.rti.data;

import java.io.Serializable;

/** */
public class Velocity implements Serializable
{
    /** */
    private static final long serialVersionUID = 20150000L;

    /** */
    private float vx; // position[m]

    /** */
    private float vy; // position[m]

    /** */
    private float vz; // position[m]

    /** {@inheritDoc} */
    public String toString()
    {
        return "Velocity [vx=" + this.getVx() + ", vy=" + this.getVy() + ", vz=" + this.getVz() + "]";
    }

    /**
     * @return vx
     */
    public float getVx()
    {
        return this.vx;
    }

    /**
     * @param vx float; set vx
     */
    public void setVx(float vx)
    {
        this.vx = vx;
    }

    /**
     * @return vy
     */
    public float getVy()
    {
        return this.vy;
    }

    /**
     * @param vy float; set vy
     */
    public void setVy(float vy)
    {
        this.vy = vy;
    }

    /**
     * @return vz
     */
    public float getVz()
    {
        return this.vz;
    }

    /**
     * @param vz float; set vz
     */
    public void setVz(float vz)
    {
        this.vz = vz;
    }

}
