package org.opentrafficsim.imb.kpi.demo;

import java.lang.reflect.InvocationTargetException;

import javax.swing.SwingUtilities;

import org.opentrafficsim.imb.IMBException;

import nl.tno.imb.TConnection;
import nl.tno.imb.mc.ModelParameters;
import nl.tno.imb.mc.ModelStarter;

/**
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Nov 11, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class ModelControlKPI extends ModelStarter
{
    /** Thread that handles the KPI process. */
    private Thread kpiThread;

    /**
     * @param args
     * @param providedModelName
     * @param providedModelId
     * @throws IMBException
     */
    public ModelControlKPI(String[] args, String providedModelName, int providedModelId) throws IMBException
    {
        super(args, providedModelName, providedModelId);
    }

    /**
     * Entry point
     * @param args
     * @throws InvocationTargetException
     * @throws InterruptedException
     */
    public static void main(final String[] args) throws InvocationTargetException, InterruptedException
    {
        SwingUtilities.invokeAndWait(new Runnable()
        {

            @Override
            public void run()
            {
                try
                {
                    new ModelControlKPI(args, "KPI Model", 3456);
                }
                catch (IMBException exception)
                {
                    exception.printStackTrace();
                }
            }
        });
    }

    /** {@inheritDoc} */
    @Override
    public void startModel(final ModelParameters parameters, final TConnection imbConnection)
    {
        System.out.println("startModel called");
        System.out.println("parameters: " + parameters);
        this.kpiThread = new Thread(new Runnable()
        {
            public void run()
            {
                VissimQueryKPI.run(parameters, imbConnection);
            }
        });
        this.kpiThread.start();
        System.out.println("Started the KPI thread");
    }

    /** {@inheritDoc} */
    @Override
    public void stopModel()
    {
        System.out.println("stopModel called");
        System.out.println("calling kpiThread interrupt");
        this.kpiThread.interrupt();
        try
        {
            System.out.println("joining with kpiThread");
            this.kpiThread.join();
        }
        catch (InterruptedException exception)
        {
            exception.printStackTrace();
        }
        System.out.println("joined with kpiThread");
    }

    /** {@inheritDoc} */
    @Override
    public void quitApplication()
    {
        System.out.println("Received quit application request");
    }

    /** {@inheritDoc} */
    @Override
    public void parameterRequest(ModelParameters parameters)
    {
        System.out.println("Received parameter request");
    }

}
