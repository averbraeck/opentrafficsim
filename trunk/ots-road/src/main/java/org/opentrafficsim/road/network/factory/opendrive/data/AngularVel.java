package org.opentrafficsim.road.network.factory.opendrive.data;

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
public class AngularVel
{
    /** */
    private float yawRate; // position[m]

    /** */
    private float pitchRate; // position[m]

    /** */
    private float rollRate; // position[m]

    /**
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
        return "AngularVel [yawRate=" + this.getYawRate() + ", pitchRate=" + this.getPitchRate() + ", rollRate="
            + this.getRollRate() + "]";
    }

    /**
     * @return yawRate
     */
    public float getYawRate()
    {
        return this.yawRate;
    }

    /**
     * @param yawRate set yawRate
     */
    public void setYawRate(float yawRate)
    {
        this.yawRate = yawRate;
    }

    /**
     * @return pitchRate
     */
    public float getPitchRate()
    {
        return this.pitchRate;
    }

    /**
     * @param pitchRate set pitchRate
     */
    public void setPitchRate(float pitchRate)
    {
        this.pitchRate = pitchRate;
    }

    /**
     * @return rollRate
     */
    public float getRollRate()
    {
        return this.rollRate;
    }

    /**
     * @param rollRate set rollRate
     */
    public void setRollRate(float rollRate)
    {
        this.rollRate = rollRate;
    }

}
