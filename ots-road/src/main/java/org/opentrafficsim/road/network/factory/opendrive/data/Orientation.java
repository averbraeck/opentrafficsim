package org.opentrafficsim.road.network.factory.opendrive.data;

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
public class Orientation
{
    /** */
    private float  yaw;  // position[m]
    
    /** */
    private float  pitch;  // position[m]
    
    /** */
    private float  roll;  // position[m]

    /**
     * @param rotX
     * @param rotY
     * @param rotZ
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

    /**
     * @see java.lang.Object#toString()
     */
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
     * @param yaw set yaw
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
     * @param pitch set pitch
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
     * @param roll set roll
     */
    public void setRoll(float roll)
    {
        this.roll = roll;
    }
    
    
}
