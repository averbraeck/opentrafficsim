package org.opentrafficsim.road.network.factory.rti.data;

import java.io.Serializable;

/** */
public class Position implements Serializable
{
    /** */
    private static final long serialVersionUID = 20150000L;

    /** */
    private float x; // position[m]

    /** */
    private float y; // position[m]

    /** */
    private float z; // position[m]

    /**
     * @param y2 x
     * @param x2 y
     * @param z2 z
     */
    public Position(double y2, double x2, double z2)
    {
        this.setX((float) y2);
        this.setY((float) x2);
        this.setZ((float) z2);
    }

    /**
     * 
     */
    public Position()
    {
    }

    /** {@inheritDoc} */
    public String toString()
    {
        return "Position [x=" + this.getX() + ", y=" + this.getY() + ", z=" + this.getZ() + "]";
    }

    /**
     * @return x
     */
    public float getX()
    {
        return this.x;
    }

    /**
     * @param x set x
     */
    public void setX(float x)
    {
        this.x = x;
    }

    /**
     * @return y
     */
    public float getY()
    {
        return this.y;
    }

    /**
     * @param y set y
     */
    public void setY(float y)
    {
        this.y = y;
    }

    /**
     * @return z
     */
    public float getZ()
    {
        return this.z;
    }

    /**
     * @param z set z
     */
    public void setZ(float z)
    {
        this.z = z;
    }

}
