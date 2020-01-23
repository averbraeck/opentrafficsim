package org.opentrafficsim.base;

import org.djutils.logger.CategoryLogger;
import org.djutils.logger.LogCategory;
import org.opentrafficsim.base.logger.Cat;
import org.pmw.tinylog.Level;

/**
 * TestCategoryLogger.java. <br>
 * <br>
 * Copyright (c) 2003-2020 Delft University of Technology, Jaffalaan 5, 2628 BX Delft, the Netherlands. All rights reserved. See
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
        CategoryLogger.always().trace("TRACE - this should NOT be logged");
        delay();
        CategoryLogger.always().debug("DEBUG - this should NOT be logged");
        delay();
        CategoryLogger.always().info("INFO - this should be logged");
        delay();
        CategoryLogger.always().warn("WARN - this should be logged");
        delay();
        CategoryLogger.always().error("ERROR - this should be logged");
        delay();
        CategoryLogger.always().error(new Exception("this exception should be logged!"),
                "message for the exception to be logged");
        delay();

        System.out.println("\nBASE...");
        delay();
        CategoryLogger.setLogCategories(Cat.BASE);
        delay();
        CategoryLogger.filter(Cat.BASE).trace("TRACE Cat.BASE - this should NOT be logged");
        delay();
        CategoryLogger.filter(Cat.BASE).debug("DEBUG Cat.BASE - this should NOT be logged");
        delay();
        CategoryLogger.filter(Cat.BASE).info("INFO Cat.BASE - this should be logged");
        delay();
        CategoryLogger.filter(Cat.BASE).warn("WARN Cat.BASE - this should be logged");
        delay();
        CategoryLogger.filter(Cat.BASE).error("ERROR Cat.BASE - this should be logged");
        delay();
        CategoryLogger.filter(Cat.BASE).error(new Exception("this exception should be logged!"),
                "message for the exception to be logged");
        delay();

        System.out.println("\nCORE... NOTHING should be logged");
        delay();
        CategoryLogger.filter(Cat.CORE).trace("TRACE Cat.CORE - this should NOT be logged");
        delay();
        CategoryLogger.filter(Cat.CORE).debug("DEBUG Cat.CORE - this should NOT be logged");
        delay();
        CategoryLogger.filter(Cat.CORE).info("INFO Cat.CORE - this should NOT be logged");
        delay();
        CategoryLogger.filter(Cat.CORE).warn("WARN Cat.CORE - this should NOT be logged");
        delay();
        CategoryLogger.filter(Cat.CORE).error("ERROR Cat.CORE - this should NOT be logged");
        delay();
        CategoryLogger.filter(Cat.CORE).error(new Exception("should all exceptions be logged?"),
                "should all exceptions be logged?");
        delay();

        LogCategory[] cats = new LogCategory[] {Cat.BASE, Cat.CORE, Cat.ROAD};
        System.out.println("\nBASE + CORE + ROAD...");
        delay();
        CategoryLogger.filter(cats).trace("TRACE cats - this should NOT be logged");
        delay();
        CategoryLogger.filter(cats).debug("DEBUG cats - this should NOT be logged");
        delay();
        CategoryLogger.filter(cats).info("INFO cats - this should be logged");
        delay();
        CategoryLogger.filter(cats).warn("WARN cats - this should be logged");
        delay();
        CategoryLogger.filter(cats).error("ERROR cats - this should be logged");
        delay();
        CategoryLogger.filter(Cat.BASE, Cat.CORE, Cat.ROAD).error(new Exception("this exception should be logged!"),
                "called with filter(Cat.BASE, Cat.CORE, Cat.ROAD) -- message for the exception to be logged");
        delay();

        System.out.println("\nArguments...");
        CategoryLogger.filter(Cat.BASE).info("INFO Cat.BASE with args {}, {} and {} - this should be logged", 1, 2, "[TEST]");
        delay();
    }

}
