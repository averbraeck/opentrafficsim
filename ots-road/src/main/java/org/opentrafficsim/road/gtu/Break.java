package org.opentrafficsim.road.gtu;

import org.djutils.exceptions.Try;
import org.opentrafficsim.base.logger.Logger;
import org.opentrafficsim.core.gtu.Gtu;
import org.opentrafficsim.core.gtu.perception.Perception;
import org.opentrafficsim.road.gtu.tactical.TacticalContextEgo;

/**
 * Utility to make debugging on a specific GTU more convenient. There is a method {@code on()} for a GTU and for perception.
 * Should neither be available within the context of a method that needs to be debugged, {@code onSub()} can be used in
 * combination with {@code onSuper()} at a higher-level method with a GTU or perception. <i>This class requires the user to set
 * a break point in the method {@code trigger()}.</i>
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public final class Break
{

    /** Condition to allow or prevent breaking in lower-level functionality. */
    private static boolean superCondition = false;

    /**
     * Constructor.
     */
    private Break()
    {
        //
    }

    /**
     * Sets a break condition to true which is triggered by {@code onSub()} at a lower level where context is insufficient to
     * determine the break condition.
     * @param perception perception to obtain gtu from
     * @param id GTU id to break on
     * @param time time to break at (or after)
     * @param additionalCondition additional condition
     */
    public static void onSuper(final Perception<?> perception, final String id, final double time,
            final boolean additionalCondition)
    {
        Try.execute(() -> onSuper(perception.getGtu(), id, time, additionalCondition),
                "Trying to break on gtu, but gtu could not be obtained from perception.");
    }

    /**
     * Sets a break condition to true which is triggered by {@code onSub()} at a lower level where context is insufficient to
     * determine the break condition.
     * @param gtu GTU
     * @param id GTU id to break on
     * @param time time to break at (or after)
     * @param additionalCondition additional condition
     */
    public static void onSuper(final Gtu gtu, final String id, final double time, final boolean additionalCondition)
    {
        superCondition = gtu.getId().equals(id) && gtu.getSimulator().getSimulatorTime().si >= time && additionalCondition;
    }

    /**
     * This method can be used if the context of a lower-level function does not contain the information on which to break. This
     * method will only trigger a break if at a higher-level function where the context was sufficient, the break condition was
     * set to true using {@code onSuper()}.
     */
    public static void onSub()
    {
        if (superCondition)
        {
            trigger();
        }
    }

    /**
     * This method can be used if the context of a lower-level function does not contain the information on which to break. This
     * method will only trigger a break if at a higher-level function where the context was sufficient, the break condition was
     * set to true using {@code onSuper()}.
     * @param additionalCondition additional condition
     */
    public static void onSub(final boolean additionalCondition)
    {
        if (superCondition && additionalCondition)
        {
            trigger();
        }
    }

    /**
     * Break on perception and String of time.
     * @param perception perception to obtain gtu from
     * @param id GTU id to break on
     * @param time time to break at (or after), in format ss, mm:ss or hh:mm:ss
     * @param additionalCondition additional condition
     */
    public static void on(final Perception<?> perception, final String id, final String time, final boolean additionalCondition)
    {
        on(perception, id, timeFromString(time), additionalCondition);
    }

    /**
     * Returns a double representation of a String time.
     * @param time string format, ss, mm:ss or hh:mm:ss
     * @return double representation of a String time
     */
    private static double timeFromString(final String time)
    {
        int index = time.indexOf(":");
        if (index < 0)
        {
            return Double.valueOf(time) - 1e-6;
        }
        int index2 = time.indexOf(":", index + 1);
        double h;
        double m;
        double s;
        if (index2 < 0)
        {
            h = 0;
            m = Double.valueOf(time.substring(0, index));
            s = Double.valueOf(time.substring(index + 1));
        }
        else
        {
            h = Double.valueOf(time.substring(0, index));
            m = Double.valueOf(time.substring(index + 1, index2));
            s = Double.valueOf(time.substring(index2 + 1));
        }
        return h * 3600.0 + m * 60.0 + s - 1e-6;
    }

    /**
     * Break on perception and double time.
     * @param perception perception to obtain gtu from
     * @param id GTU id to break on
     * @param time time to break at (or after)
     * @param additionalCondition additional condition
     */
    public static void on(final Perception<?> perception, final String id, final double time, final boolean additionalCondition)
    {
        Try.execute(() -> on(perception.getGtu(), id, time, additionalCondition),
                "Trying to break on gtu, but gtu could not be obtained from perception.");
    }

    /**
     * Break on perception and double time.
     * @param context tactical information such as parameters and car-following model
     * @param id GTU id to break on
     * @param time time to break at (or after)
     * @param additionalCondition additional condition
     */
    public static void on(final TacticalContextEgo context, final String id, final double time,
            final boolean additionalCondition)
    {
        on(context.getGtu(), id, time, additionalCondition);
    }

    /**
     * Break on GTU and String of time.
     * @param gtu GTU
     * @param id GTU id to break on
     * @param time time to break at (or after), in format ss, mm:ss or hh:mm:ss
     * @param additionalCondition additional condition
     */
    public static void on(final Gtu gtu, final String id, final String time, final boolean additionalCondition)
    {
        on(gtu, id, timeFromString(time), additionalCondition);
    }

    /**
     * Break on GTU and double time.
     * @param gtu GTU
     * @param id GTU id to break on
     * @param time time to break at (or after)
     * @param additionalCondition additional condition
     */
    public static void on(final Gtu gtu, final String id, final double time, final boolean additionalCondition)
    {
        if (gtu.getId().equals(id) && gtu.getSimulator().getSimulatorTime().si >= time && additionalCondition)
        {
            trigger();
        }
    }

    /**
     * Method that is invoked on a break condition. A break-point here allows the user to debug specific situations.
     */
    private static void trigger()
    {
        Logger.ots().error("Break condition for debugging is true."); // SET BREAK POINT ON THIS LINE
    }

}
