package org.opentrafficsim.road.network.factory.opendrive.data;

/** */
public class Velocity
{
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
     * @param vx set vx
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
     * @param vy set vy
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
     * @param vz set vz
     */
    public void setVz(float vz)
    {
        this.vz = vz;
    }

}
