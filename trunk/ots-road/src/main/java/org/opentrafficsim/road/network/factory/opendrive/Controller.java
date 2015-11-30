package org.opentrafficsim.road.network.factory.opendrive;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.naming.NamingException;

import nl.tudelft.simulation.dsol.SimRuntimeException;

import org.djunits.unit.TimeUnit;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.core.dsol.OTSAnimatorInterface;
import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.road.gtu.lane.object.AbstractTrafficLight;

/**
 * Traffic Light Controller
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision: 1155 $, $LastChangedDate: 2015-07-26 01:01:13 +0200 (Sun, 26 Jul 2015) $, by $Author: averbraeck $,
 *          initial version 15 jul. 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class Controller
{
    
    /** */
    private OTSDEVSSimulatorInterface simulator;
    
    /** */
    private String id;
    
    /** */
    private Map<Integer, Set<AbstractTrafficLight>> trafficLights = new HashMap<Integer, Set<AbstractTrafficLight>>();
    
    
    /**
     * @param name the name of the OnOffTrafficLight
     * @param simulator the simulator to avoid NullPointerExceptions
     * @throws GTUException when GTU cannot be created.
     * @throws NamingException if an error occurs when adding the animation handler
     * @throws NetworkException when the GTU cannot be placed on the given lane
     */
    public Controller(final String name, final OTSDEVSSimulatorInterface simulator) throws GTUException, NetworkException, NamingException
    {
        this.id = name;
        this.simulator = simulator;

        try
        {
            //new DefaultBlockOnOffAnimation(this, getSimulator());
            // animation
            if (simulator instanceof OTSAnimatorInterface)
            {
                // TODO
            }
            simulator.scheduleEventRel(new Time.Rel(0.0, TimeUnit.SECOND), this, this, "unBlock", null);
        }
        catch (SimRuntimeException exception)
        {
            exception.printStackTrace();
        }
    }

    /**
     * 
     */
    protected void unBlock()
    {
        try
        {            
            this.simulator.scheduleEventRel(new Time.Rel(96.0, TimeUnit.SECOND), this, this, "unBlock", null);
        }
        catch (SimRuntimeException exception)
        {
            exception.printStackTrace();
        }
        
        if(this.trafficLights.containsKey(3))
        {
            for(AbstractTrafficLight light: this.trafficLights.get(3))
            {
                try
                {
                    this.simulator.scheduleEventRel(new Time.Rel(0.0, TimeUnit.SECOND), this, light, "setGreen", null);
                } catch (SimRuntimeException exception)
                {
                    exception.printStackTrace();
                }
            }
        }
        if(this.trafficLights.containsKey(4))
        {
            for(AbstractTrafficLight light: this.trafficLights.get(4))
            {
                try
                {
                    this.simulator.scheduleEventRel(new Time.Rel(0.0, TimeUnit.SECOND), this, light, "setGreen", null);
                } catch (SimRuntimeException exception)
                {
                    exception.printStackTrace();
                }
            }
        }
        if(this.trafficLights.containsKey(6))
        {
            for(AbstractTrafficLight light: this.trafficLights.get(6))
            {
                try
                {
                    this.simulator.scheduleEventRel(new Time.Rel(24.0, TimeUnit.SECOND), this, light, "setGreen", null);
                } catch (SimRuntimeException exception)
                {
                    exception.printStackTrace();
                }
            }
        }
        if(this.trafficLights.containsKey(7))
        {
            for(AbstractTrafficLight light: this.trafficLights.get(7))
            {
                try
                {
                    this.simulator.scheduleEventRel(new Time.Rel(24.0, TimeUnit.SECOND), this, light, "setGreen", null);
                } catch (SimRuntimeException exception)
                {
                    exception.printStackTrace();
                }
            }
        }
        if(this.trafficLights.containsKey(9))
        {
            for(AbstractTrafficLight light: this.trafficLights.get(9))
            {
                try
                {
                    this.simulator.scheduleEventRel(new Time.Rel(48.0, TimeUnit.SECOND), this, light, "setGreen", null);
                } catch (SimRuntimeException exception)
                {
                    exception.printStackTrace();
                }
            }
        }
        if(this.trafficLights.containsKey(10))
        {
            for(AbstractTrafficLight light: this.trafficLights.get(10))
            {
                try
                {
                    this.simulator.scheduleEventRel(new Time.Rel(48.0, TimeUnit.SECOND), this, light, "setGreen", null);
                } catch (SimRuntimeException exception)
                {
                    exception.printStackTrace();
                }
            }
        }
        if(this.trafficLights.containsKey(12))
        {
            for(AbstractTrafficLight light: this.trafficLights.get(12))
            {
                try
                {
                    this.simulator.scheduleEventRel(new Time.Rel(72.0, TimeUnit.SECOND), this, light, "setGreen", null);
                } catch (SimRuntimeException exception)
                {
                    exception.printStackTrace();
                }
            }
        }
        if(this.trafficLights.containsKey(13))
        {
            for(AbstractTrafficLight light: this.trafficLights.get(13))
            {
                try
                {
                    this.simulator.scheduleEventRel(new Time.Rel(72.0, TimeUnit.SECOND), this, light, "setGreen", null);
                } catch (SimRuntimeException exception)
                {
                    exception.printStackTrace();
                }
            }
        }
    }

    /**
     * @param sequence2
     * @param trafficLight
     */
    public void addTrafficLight(int sequence2, AbstractTrafficLight trafficLight)
    {
        if(!this.trafficLights.containsKey(sequence2))
        {
            Set<AbstractTrafficLight> trafficLightsSet = new HashSet<AbstractTrafficLight>();
            this.trafficLights.put(sequence2, trafficLightsSet);
        }
        this.trafficLights.get(sequence2).add(trafficLight);
    }

}
