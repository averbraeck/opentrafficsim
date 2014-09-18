package org.opentrafficsim.demo.ntm;

import org.opentrafficsim.core.unit.SpeedUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;

/**
 * <p>
 * Copyright (c) 2002-2014 Delft University of Technology, Jaffalaan 5, 2628 BX
 * Delft, the Netherlands. All rights reserved.
 * 
 * <p>
 * See for project information <a href="http://www.simulation.tudelft.nl/">
 * www.simulation.tudelft.nl</a>.
 * <p>
 * The OpenTrafficSim project is distributed under the following BSD-style license:<br>
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * <ul>
 * <li>Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.</li>
 * <li>Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.</li>
 * <li>Neither the name of Delft University of Technology, nor the names of its
 * contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.</li>
 * </ul>
 * This software is provided by the copyright holders and contributors "as is"
 * and any express or implied warranties, including, but not limited to, the
 * implied warranties of merchantability and fitness for a particular purpose
 * are disclaimed. In no event shall the copyright holder or contributors be
 * liable for any direct, indirect, incidental, special, exemplary, or
 * consequential damages (including, but not limited to, procurement of
 * substitute goods or services; loss of use, data, or profits; or business
 * interruption) however caused and on any theory of liability, whether in
 * contract, strict liability, or tort (including negligence or otherwise)
 * arising in any way out of the use of this software, even if advised of the
 * possibility of such damage.
 * 
 * @version 9 Sep 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://Hansvanlint.weblog.tudelft.nl">Hans van Lint</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.citg.tudelft.nl">Guus Tamminga</a>
 * @author <a href="http://www.citg.tudelft.nl">Yufei Yuan</a>
 */
public abstract class FundamentalDiagram<ID>
{
    /** */
    private ID id;


    /** carProduction: numbers of Cars produced from this CELL  */
    private double carProduction;
        

    /** currentSpeed: average current speed of Cars in this CELL  */
    private DoubleScalar.Abs<SpeedUnit> currentSpeed;
    
    /** 
     * freeSpeed: average free speed of cars in Network element (lane, link, network zone)
     * */
    private DoubleScalar.Abs<SpeedUnit> freeSpeed;
    
    
    /**
     * @param id
     */
    public FundamentalDiagram(ID id)
    {
        this.id = id;
    }

    /**
     * @return id
     */
    public ID getId()
    {
        return this.id;
    }

    /**
     * @return carProduction
     */
    public double getCarProduction()
    {
        return this.carProduction;
    }

    /**
     * @param carProduction set carProduction
     */
    public void setCarProduction(double carProduction)
    {
        this.carProduction = carProduction;
    }
    
    /**
     * @return currentSpeed
     */
    public DoubleScalar.Abs<SpeedUnit> getCurrentSpeed()
    {
        return this.currentSpeed;
    }

    /**
     * @return freeSpeed
     */
    public DoubleScalar.Abs<SpeedUnit> getFreeSpeed()
    {
        return this.freeSpeed;
    }

    /**
     * @param freeSpeed set freeSpeed
     */
    public void setFreeSpeed(DoubleScalar.Abs<SpeedUnit> freeSpeed)
    {
        this.freeSpeed = freeSpeed;
    }
    
}
