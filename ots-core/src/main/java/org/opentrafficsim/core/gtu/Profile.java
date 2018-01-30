package org.opentrafficsim.core.gtu;

import java.util.HashMap;
import java.util.Map;

import nl.tudelft.simulation.language.Throw;

/**
 * Utility class to profile code efficiency.
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 27 jan. 2018 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
// TODO nested profiling, using the last start
public class Profile
{

    /** Map containing infos. */
    private static final Map<String, ProfileInfo> infos = new HashMap<>();

    /** Map containing most recent part id's as line numbers. */
    private static final Map<String, String> lines = new HashMap<>();

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
     * Starts timing on the calling class and method, specified by given id.
     * @param id String; id
     */
    public static void start(final String id)
    {
        start0(id, System.nanoTime());
    }

    /**
     * Forwarding method used for consistent stack trace filtering.
     * @param id String; id
     * @param nanoTime long; time obtained by entrance method
     */
    private static void start0(final String id, final Long nanoTime)
    {
        getProfileInfo(id, true).start(nanoTime);
    }

    /**
     * Ends timing on the calling class and method, specified by line number (of the start command).
     */
    public static void end()
    {
        end0(null, System.nanoTime());
    }

    /**
     * Ends timing on the calling class and method, specified by given id.
     * @param id String; id
     */
    public static void end(final String id)
    {
        end0(id, System.nanoTime());
    }

    /**
     * Forwarding method used for consistent stack trace filtering.
     * @param id String; id
     * @param nanoTime long; time obtained by entrance method
     */
    private static void end0(final String id, final Long nanoTime)
    {
        getProfileInfo(id, false).end(System.nanoTime());
    }

    /**
     * Returns the profile info, which is created if none was present.
     * @param id String; id
     * @param start boolean; start command
     * @return ProfileInfo; applicable info
     */
    private static ProfileInfo getProfileInfo(final String id, final boolean start)
    {
        StackTraceElement element = Thread.currentThread().getStackTrace()[4];
        String classMethodId = element.getClassName() + ":" + element.getMethodName();
        String partId;
        if (id == null)
        {
            if (start)
            {
                partId = ":" + String.valueOf(element.getLineNumber());
                lines.put(classMethodId, partId);
            }
            else
            {
                partId = lines.get(classMethodId);
            }
        }
        else
        {
            partId = ":" + id;
        }
        classMethodId += partId;

        ProfileInfo info = infos.get(classMethodId);
        if (info == null)
        {
            info = new ProfileInfo();
            infos.put(classMethodId, info);
        }
        return info;
    }

    /**
     * Prints profiling output to the console.
     */
    public static void print()
    {
        System.out.println("Profile report");
        long sum = 0;
        for (String id : infos.keySet())
        {
            sum += infos.get(id).getTotal();
        }
        for (String id : infos.keySet())
        {
            ProfileInfo info = infos.get(id);
            if (info.getTotal() > 0)
            {
                System.out.println("  " + id);
                System.out.println(String.format("   -> [%05.2f%%] %.6f s | %d invocations | %.6f s/invocation",
                        100.0 * info.getTotal() / sum, info.getTotal() / 1000000000.0, info.getInvocations(),
                        info.getTotal() / info.getInvocations() / 1000000000.0));
            }
        }
    }

    /**
     * Contains info per profiling part.
     * <p>
     * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
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

        /** Start time of recording. */
        private long start = -1;

        /** Total time of profiling. */
        private long total;

        /** Number of invocations. */
        private int invocations;

        /**
         * Constructor.
         */
        public ProfileInfo()
        {
            //
        }

        /**
         * Sets the start time.
         * @param startTime long; start time
         */
        public void start(final long startTime)
        {
            Throw.when(this.start > 0, IllegalStateException.class, "Can only start profiling if it was ended.");
            this.start = startTime;
        }

        /**
         * Adds to total profiling time.
         * @param endTime long; end time
         */
        public void end(final long endTime)
        {
            Throw.when(this.start < 0, IllegalStateException.class, "Can only end profiling if it was started.");
            this.total += (endTime - this.start);
            this.invocations++;
            this.start = -1;
        }

        /**
         * Returns total profiling time.
         * @return long; total profiling time
         */
        public long getTotal()
        {
            return this.total;
        }

        /**
         * Returns the number of invocations.
         * @return int; number of invocations
         */
        public int getInvocations()
        {
            return this.invocations;
        }

    }

}
