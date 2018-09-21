package org.opentrafficsim.base;

import org.opentrafficsim.base.logger.Cat;
import org.opentrafficsim.base.logger.CategoryLogger;
import org.opentrafficsim.base.logger.LogCategory;
import org.pmw.tinylog.Level;

/**
 * TestCategoryLogger.java. <br>
 * <br>
 * Copyright (c) 2003-2018 Delft University of Technology, Jaffalaan 5, 2628 BX Delft, the Netherlands. All rights reserved. See
 * for project information <a href="https://www.simulation.tudelft.nl/" target="_blank">www.simulation.tudelft.nl</a>. The
 * source code and binary code of this software is proprietary information of Delft University of Technology.
 * @author <a href="https://www.tudelft.nl/averbraeck" target="_blank">Alexander Verbraeck</a>
 */
public final class TestCategoryLogger
{
    /** */
    private TestCategoryLogger()
    {
        // Utility class
    }
    
    /** 
     * Avoid err and out messages to be mixed. 
     */
    private static void delay()
    {
        try
        {
            Thread.sleep(1);
        }
        catch (InterruptedException exception)
        {
            // ok
        }
    }
    /**
     * Test the CategoryLogger.
     * @param args not used
     */
    public static void main(final String[] args)
    {
        CategoryLogger.setAllLogLevel(Level.INFO);
        CategoryLogger.trace("TRACE - this should NOT be logged");
        delay();
        CategoryLogger.debug("DEBUG - this should NOT be logged");
        delay();
        CategoryLogger.info("INFO - this should be logged");
        delay();
        CategoryLogger.warn("WARN - this should be logged");
        delay();
        CategoryLogger.error("ERROR - this should be logged");
        delay();
        CategoryLogger.error(new Exception("this exception should be logged!"), "message for the exception to be logged");
        delay();
        
        System.out.println("\nBASE...");
        delay();
        CategoryLogger.setLogCategories(Cat.BASE);
        delay();
        CategoryLogger.trace(Cat.BASE, "TRACE Cat.BASE - this should NOT be logged");
        delay();
        CategoryLogger.debug(Cat.BASE, "DEBUG Cat.BASE - this should NOT be logged");
        delay();
        CategoryLogger.info(Cat.BASE, "INFO Cat.BASE - this should be logged");
        delay();
        CategoryLogger.warn(Cat.BASE, "WARN Cat.BASE - this should be logged");
        delay();
        CategoryLogger.error(Cat.BASE, "ERROR Cat.BASE - this should be logged");
        delay();
        CategoryLogger.error(new Exception("this exception should be logged!"), "message for the exception to be logged");
        delay();
        
        System.out.println("\nCORE... NOTHING should be logged");
        delay();
        CategoryLogger.trace(Cat.CORE, "TRACE Cat.CORE - this should NOT be logged");
        delay();
        CategoryLogger.debug(Cat.CORE, "DEBUG Cat.CORE - this should NOT be logged");
        delay();
        CategoryLogger.info(Cat.CORE, "INFO Cat.CORE - this should NOT be logged");
        delay();
        CategoryLogger.warn(Cat.CORE, "WARN Cat.CORE - this should NOT be logged");
        delay();
        CategoryLogger.error(Cat.CORE, "ERROR Cat.CORE - this should NOT be logged");
        delay();
        CategoryLogger.error(new Exception("should all exceptions be logged?"), "should all exceptions be logged?");
        delay();
        
        LogCategory[] cats = new LogCategory[] {Cat.BASE, Cat.CORE, Cat.ROAD};
        System.out.println("\nBASE + CORE + ROAD...");
        delay();
        CategoryLogger.trace(cats, "TRACE cats - this should NOT be logged");
        delay();
        CategoryLogger.debug(cats, "DEBUG cats - this should NOT be logged");
        delay();
        CategoryLogger.info(cats, "INFO cats - this should be logged");
        delay();
        CategoryLogger.warn(cats, "WARN cats - this should be logged");
        delay();
        CategoryLogger.error(cats, "ERROR cats - this should be logged");
        delay();
        CategoryLogger.error(new Exception("this exception should be logged!"), "message for the exception to be logged");
        delay();

        System.out.println("\nArguments...");
        CategoryLogger.info(Cat.BASE, "INFO Cat.BASE with args {}, {} and {} - this should be logged", 1, 2, "[TEST]");
        delay();

    }

}

