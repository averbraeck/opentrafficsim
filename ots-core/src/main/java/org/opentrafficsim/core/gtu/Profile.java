package org.opentrafficsim.core.gtu;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.djutils.exceptions.Throw;

/**
 * Utility class to profile code efficiency.
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 27 jan. 2018 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public final class Profile
{

    /** Map containing ProfileInfo objects. */
    private static final Map<String, ProfileInfo> INFOS = new LinkedHashMap<>();

    /** Map containing most recent part id's as line numbers. */
    private static final Map<String, String> LINES = new LinkedHashMap<>();

    /** Minimum print interval [ms]; during this time after a print, prints are suppressed. */
    private static long printInterval = 1000;

    /** Time of last print. */
    private static long lastPrint = 0;

    /** Private constructor. */
    private Profile()
    {
        //
    }

    /**
     * Starts timing on the calling class and method, specified by line number.
     */
    public static void start()
    {
        start0(null, System.nanoTime());
    }

    /**
     * Starts timing on the calling class and method, specified by given name.
     * @param name String; name
     */
    public static void start(final String name)
    {
        start0(name, System.nanoTime());
    }

    /**
     * Forwarding method used for consistent stack trace filtering.
     * @param name String; name
     * @param nanoTime Long; time obtained by entrance method
     */
    private static void start0(final String name, final Long nanoTime)
    {
        getProfileInfo(name, true).start(nanoTime);
    }

    /**
     * Ends timing on the calling class and method, specified by line number (of the start command).
     */
    public static void end()
    {
        end0(null, System.nanoTime());
    }

    /**
     * Ends timing on the calling class and method, specified by given name.
     * @param name String; name
     */
    public static void end(final String name)
    {
        end0(name, System.nanoTime());
    }

    /**
     * Forwarding method used for consistent stack trace filtering.
     * @param name String; name
     * @param nanoTime Long; time obtained by entrance method
     */
    private static void end0(final String name, final Long nanoTime)
    {
        getProfileInfo(name, false).end(nanoTime);
    }

    /**
     * Returns the profile info, which is created if none was present.
     * @param name String; name
     * @param start boolean; start command
     * @return ProfileInfo; applicable info
     */
    private static ProfileInfo getProfileInfo(final String name, final boolean start)
    {
        StackTraceElement element = Thread.currentThread().getStackTrace()[4];
        String classMethodId = element.getClassName() + ":" + element.getMethodName();
        String partId;
        if (name == null)
        {
            if (start)
            {
                partId = ":" + String.valueOf(element.getLineNumber());
                LINES.put(classMethodId, partId);
            }
            else
            {
                partId = LINES.get(classMethodId);
            }
        }
        else
        {
            partId = ":" + name;
        }
        classMethodId += partId;

        ProfileInfo info = INFOS.get(classMethodId);
        if (info == null)
        {
            info = new ProfileInfo(name);
            INFOS.put(classMethodId, info);
        }
        return info;
    }

    /**
     * Returns a formatted string of a table with statistics.
     * @return String formatted string of a table with statistics
     */
    public static String statistics()
    {
        // gather totals information and sort
        double sum = 0;
        int maxInvocations = 0;
        int maxNameLength = 4;
        Map<Double, Entry<String, ProfileInfo>> sorted = new TreeMap<>(Collections.reverseOrder());
        for (Entry<String, ProfileInfo> entry : INFOS.entrySet())
        {
            String id = entry.getKey();
            ProfileInfo info = INFOS.get(id);
            sorted.put(info.getTotal(), entry);
            sum += info.getTotal();
            maxInvocations = maxInvocations > info.getInvocations() ? maxInvocations : info.getInvocations();
            int nameLength = (info.getName() == null ? id : info.getName()).length();
            maxNameLength = maxNameLength > nameLength ? maxNameLength : nameLength;
        }
        int lengthInvoke = String.valueOf(maxInvocations).length();
        lengthInvoke = lengthInvoke > 6 ? lengthInvoke : 6;
        String invokeHeaderFormat = String.format("%%%d.%ds", lengthInvoke, lengthInvoke);
        String invokeLineFormat = String.format("%%%ds", lengthInvoke);
        String nameHeaderFormat = String.format("%%%d.%ds", maxNameLength, maxNameLength);
        String nameLineFormat = String.format("%%%ds", maxNameLength);
        String line = new String(new char[80 + lengthInvoke + maxNameLength]).replace("\0", "-"); // -------------- line

        // header
        StringBuilder builder = new StringBuilder();
        builder.append("-").append(line).append("-\n");
        builder.append(String.format("| %7.7s | ", "Perc."));
        builder.append(String.format(invokeHeaderFormat, "#Calls"));
        builder.append(String.format(" | %10.10s | %10.10s | %10.10s | %10.10s | %10.10s | ", "TotTime", "MinTime", "MaxTime",
                "AvgTime", "StdTime"));
        builder.append(String.format(nameHeaderFormat, "Name")).append(" |\n");
        builder.append("|").append(line).append("|\n");

        // lines
        for (Entry<String, ProfileInfo> entry : sorted.values())
        {
            String id = entry.getKey();
            ProfileInfo info = entry.getValue();
            if (info.getInvocations() > 0)
            {
                double perc = 100.0 * info.getTotal() / sum;
                builder.append(String.format("| %6.2f%% | ", perc));
                builder.append(String.format(invokeLineFormat, info.getInvocations()));
                builder.append(String.format(" | %9.4fs | %9.6fs | %9.6fs | %9.6fs | ", info.getTotal(), info.getMin(),
                        info.getMax(), info.getMean()));
                // std
                if (info.getInvocations() > 1)
                {
                    builder.append(String.format("%9.6fs", info.getStandardDeviation()));
                }
                else
                {
                    builder.append("          ");
                }
                // name
                builder.append(" | ").append(String.format(nameLineFormat, info.getName() == null ? id : info.getName()))
                        .append(" |\n");
            }
        }
        return builder.append("-").append(line).append("-\n").toString();
    }

    /**
     * Prints profiling output to the console once every 1s or longer, or according to the time set in
     * {@code setPrintInterval()}. This method needs to be called repeatedly, and will only print again after the print
     * interval. If execution time between two call to this method is longer than the print interval, this method prints
     * directly.
     */
    public static void print()
    {
        long t = System.currentTimeMillis();
        if (t - lastPrint > printInterval)
        {
            lastPrint = t;
            System.out.print(statistics());
        }
    }

    /**
     * Sets a print interval.
     * @param printInterval long; print interval in ms
     */
    public static void setPrintInterval(final long printInterval)
    {
        Profile.printInterval = printInterval;
    }

    /**
     * Contains info per profiling part.
     * <p>
     * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
     * <p>
     * @version $Revision$, $LastChangedDate$, by $Author$, initial version 27 jan. 2018 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
     */
    private static class ProfileInfo
    {

        /** User given name. */
        private final String name;

        /** Start time of recording. */
        private Long start = null;

        /** Total time of profiling. */
        private long total;

        /** Total of instance times squared. */
        private long totalSquared;

        /** Minimum execution time. */
        private long minTime;

        /** Maximum execution time. */
        private long maxTime;

        /** Number of invocations. */
        private int invocations;

        /**
         * Constructor.
         * @param name String; user given name
         */
        ProfileInfo(final String name)
        {
            this.name = name;
        }

        /**
         * Sets the start time.
         * @param startTime long; start time
         */
        public void start(final long startTime)
        {
            Throw.when(this.start != null, IllegalStateException.class, "Can only start profiling if it was ended.");
            this.start = startTime;
        }

        /**
         * Adds to total profiling time.
         * @param endTime long; end time
         */
        public void end(final long endTime)
        {
            Throw.when(this.start == null, IllegalStateException.class, "Can only end profiling if it was started.");
            long duration = endTime - this.start;
            this.total += duration;
            this.totalSquared += duration * duration;
            if (this.invocations == 0)
            {
                this.minTime = duration;
                this.maxTime = duration;
            }
            else
            {
                this.minTime = this.minTime < duration ? this.minTime : duration;
                this.maxTime = this.maxTime > duration ? this.maxTime : duration;
            }
            this.invocations++;
            this.start = null;
        }

        /**
         * Returns the user given id.
         * @return String; user given id
         */
        public String getName()
        {
            return this.name;
        }

        /**
         * Returns total profiling time [s].
         * @return double; total profiling time [s]
         */
        public double getTotal()
        {
            return this.total / 1000000000.0;
        }

        /**
         * Returns profiling time deviation [s].
         * @return double; profiling time deviation [s]
         */
        public double getStandardDeviation()
        {
            if (this.invocations < 2)
            {
                return Double.NaN;
            }
            double squared = this.totalSquared - this.total * this.total / this.invocations;
            return Math.sqrt(squared / (this.invocations - 1)) / 1000000000.0;
        }

        /**
         * Returns the number of invocations.
         * @return int; number of invocations
         */
        public int getInvocations()
        {
            return this.invocations;
        }

        /**
         * Returns the mean execution time [s].
         * @return double; mean execution time [s]
         */
        public double getMean()
        {
            return getTotal() / getInvocations();
        }

        /**
         * Returns the minimum execution time [s].
         * @return minimum execution time [s]
         */
        public double getMin()
        {
            return this.minTime / 1000000000.0;
        }

        /**
         * Returns the maximum execution time [s].
         * @return maximum execution time [s]
         */
        public double getMax()
        {
            return this.maxTime / 1000000000.0;
        }

        /** {@inheritDoc} */
        @Override
        public String toString()
        {
            return "ProfileInfo [name=" + this.name + ", start=" + this.start + ", total=" + this.total + ", totalSquared="
                    + this.totalSquared + ", minTime=" + this.minTime + ", maxTime=" + this.maxTime + ", invocations="
                    + this.invocations + "]";
        }

    }

}
