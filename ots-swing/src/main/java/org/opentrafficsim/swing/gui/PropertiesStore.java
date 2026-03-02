package org.opentrafficsim.swing.gui;

import java.awt.Color;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.djutils.exceptions.Throw;

/**
 * Class that can be used within a program to load and save properties. This class adheres to the XDG Base Directory
 * Specification regarding where setting files are stored.
 * <p>
 * Copyright (c) 2026-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 * @see <a href="https://specifications.freedesktop.org/basedir/latest/">XDG Base Directory Specification</a>
 */
public class PropertiesStore
{

    /**
     * Location within <code>${user.home}</code> where properties are stored to comply with XDG Base Directory Specification.
     */
    private static final String CONFIG = ".config";

    /** Enterprise folder. */
    private static final String OTS = "ots";

    /** Format to store dates with. Complies with {@link Date#toString} and {@link Properties}. */
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy");

    /** Properties. */
    private final Properties properties;

    /** Context. */
    private final String context;

    /** Description that is saved as a comment in the file that stores the properties. */
    private final String description;

    /** Cached colors. */
    private final Map<String, Color> colorCache = new LinkedHashMap<>();

    /** Cached ints. */
    private final Map<String, Integer> intCache = new LinkedHashMap<>();

    /** Cached booleans. */
    private final Map<String, Boolean> booleanCache = new LinkedHashMap<>();

    /** Maximum number of sub-contexts. */
    private int maxSubContexts = 50;

    /** Scheduler to delay limiting the contexts. */
    private final ScheduledExecutorService limitContextScheduler;

    /** Counter to skip intermediate context limitation requests. */
    private final AtomicLong limitContextRequest = new AtomicLong(0);

    /**
     * Constructor. To populate the default {@link Properties} use the various static {@code valueToString} methods.
     * @param properties properties pre-loaded with defaults
     * @param context context of the properties, e.g. {@code "appearance"} or {@code "editor"}
     * @param description description of the properties, which is saved as a comment in the file that stores the properties
     */
    public PropertiesStore(final Properties properties, final String context, final String description)
    {
        Throw.whenNull(context, "context");
        Path path = Paths.get(System.getProperty("user.home"), CONFIG, OTS, safeContext(context));
        Properties props = properties == null ? new Properties() : properties;
        try
        {
            props.load(new FileReader(path.toFile()));
        }
        catch (IOException exception)
        {
            // ignore
        }
        this.properties = props;
        this.context = context;
        this.description = description; // can be null
        save(); // saves defaults on missing values or completely missing file
        this.limitContextScheduler = Executors
                .newSingleThreadScheduledExecutor((runnable) -> new Thread(runnable, this.context + "-context-limiter"));
    }

    /**
     * Sets the maximum number of sub-contexts. The default value is 50.
     * @param maxSubContexts maximum number of sub-contexts
     */
    public void setMaxSubContexts(final int maxSubContexts)
    {
        Throw.when(maxSubContexts <= 0, IllegalArgumentException.class, "Number of maximum sub-contexts should be at least 1.");
        this.maxSubContexts = maxSubContexts;
        limitContexts();
    }

    /**
     * Saves properties.
     */
    public void save()
    {
        File f = Paths.get(System.getProperty("user.home"), CONFIG, OTS, safeContext(this.context)).toFile();
        f.getParentFile().mkdirs();
        try
        {
            FileWriter writer = new FileWriter(f);
            this.properties.store(writer, this.description);
        }
        catch (IOException exception)
        {
            // ignore
        }
    }

    /**
     * Returns a lower case context that will append .ini if the context does not already end with .ini.
     * @param context context
     * @return save context
     */
    private static String safeContext(final String context)
    {
        return context.toLowerCase().endsWith(".ini") ? context.toLowerCase() : context.toLowerCase() + ".ini";
    }

    /**
     * Returns the property value. If the program has not saved any value, a default value should have been given via the input
     * properties.
     * @param key key
     * @return property value
     */
    public String getProperty(final String key)
    {
        Throw.whenNull(key, "key");
        return this.properties.getProperty(key);
    }

    /**
     * Returns property that might not be given.
     * @param key key
     * @return property that might not be given
     */
    public Optional<String> getOptionalProperty(final String key)
    {
        return Optional.ofNullable(getProperty(key));
    }

    /**
     * Sets a property value.
     * @param key key
     * @param value value
     * @param save whether to save the properties (typically yes, but only on last if multiple properties are set)
     */
    public void setProperty(final String key, final String value, final boolean save)
    {
        Throw.whenNull(key, "key");
        Throw.whenNull(value, "value");
        this.properties.setProperty(key, value);
        if (save)
        {
            save();
        }
    }

    /**
     * Sets a property value.
     * @param key key
     * @param value value
     */
    public void setProperty(final String key, final String value)
    {
        setProperty(key, value, true);
    }

    /**
     * Removes key from the store.
     * @param key key
     */
    public void clearProperty(final String key)
    {
        Throw.whenNull(key, "key");
        this.properties.remove(key);
        save();
    }

    /**
     * Returns a key specific for the sub-context. The resulting key is <code>context.{hashCode}.{key}</code>, using the hash
     * code of the sub-context. This method also deals with limiting the number of saved sub-contexts.
     * @param key key
     * @param subContext sub-context
     * @return contextual key
     */
    public String contextKey(final String key, final Object subContext)
    {
        Throw.whenNull(key, "key");
        Throw.whenNull(subContext, "context");
        String keyPart = "context." + Integer.toString(subContext.hashCode());
        setProperty(key + "_date", DATE_FORMAT.format(new Date())); // context.123456789_date
        limitContexts();
        return keyPart + "." + key;
    }

    /**
     * Limit the number of contexts stored. This method is delayed. Intermediate invocations will cancel previous invocations.
     */
    private void limitContexts()
    {
        long request = this.limitContextRequest.incrementAndGet();
        this.limitContextScheduler.schedule(() ->
        {
            // If a newer request arrived, skip
            if (request != this.limitContextRequest.get())
            {
                return;
            }
            limitContexts0();
        }, 500L, TimeUnit.MILLISECONDS);

    }

    /**
     * Performs the actual limiting of the number of contexts.
     */
    private void limitContexts0()
    {
        // Gather sorted contexts
        SortedMap<Date, String> contexts = new TreeMap<>();
        Map<String, Set<String>> contextKeys = new LinkedHashMap<>();
        Pattern pattern = Pattern.compile("context\\.(%d+)(\\.|_date).*=(.*)");
        for (Object keyObj : PropertiesStore.this.properties.keySet())
        {
            String key = keyObj.toString();
            Matcher matcher = pattern.matcher(key);
            if (matcher.matches())
            {
                String subContext = matcher.group(0);
                if ("_".equals(matcher.group(1)))
                {
                    // date value
                    Date date;
                    try
                    {
                        date = DATE_FORMAT.parse(matcher.group(2));
                        contexts.put(date, subContext);
                    }
                    catch (ParseException exception)
                    {
                        // throw it away by assuming old time
                        contexts.put(new Date(0L), subContext);
                    }
                }
                else
                {
                    contextKeys.computeIfAbsent(subContext, (s) -> new LinkedHashSet<>()).add(key);
                }
            }
        }

        // Clear old contexts
        boolean removed = contexts.size() > PropertiesStore.this.maxSubContexts;
        while (contexts.size() > PropertiesStore.this.maxSubContexts)
        {
            Date first = contexts.firstKey();
            String oldContext = contexts.remove(first);
            contextKeys.computeIfAbsent(oldContext, (d) -> new LinkedHashSet<>()).forEach((k) -> clearProperty(k));
        }

        // Save if any removed
        if (removed)
        {
            save();
        }
    }

    // ====== List ======

    /**
     * Returns list property.
     * @param key key under which list is stored
     * @return list (recent to old)
     */
    public List<String> getList(final String key)
    {
        Throw.whenNull(key, "key");
        List<String> out = new ArrayList<>();
        if (this.properties.containsKey(key))
        {
            String[] values = ((String) this.properties.get(key)).split("\\|");
            Arrays.stream(values).forEach(out::add);
        }
        return out;
    }

    /**
     * Add value to list. If the value is already in the list, it is moved to the front. If the list does not exist it will be
     * created. The resulting list is saved.
     * @param key key under which list is stored
     * @param value value to add to the list
     * @param maxNumber maximum number of elements in the list
     * @throws IllegalArgumentException when the value contains a '|'
     */
    public void addToList(final String key, final String value, final int maxNumber)
    {
        Throw.whenNull(key, "key");
        Throw.whenNull(value, "value");
        Throw.when(value.contains("|"), IllegalArgumentException.class, "Value in a list may not contain '|'.");
        List<String> files = getList(key);
        if (files.contains(value))
        {
            if (files.get(0).equals(value))
            {
                return;
            }
            files.remove(value);
        }
        files.add(0, value);
        setList(key, files, maxNumber);
    }

    /**
     * Remove value from list. The resulting list is saved.
     * @param key key
     * @param value value
     */
    public void removeFromList(final String key, final String value)
    {
        List<String> list = getList(key);
        list.remove(value);
        setList(key, list, list.size()); // size is ok, we shrink the list so we can't run in to the limit
    }

    /**
     * Sets the list.
     * @param key key
     * @param list list
     * @param maxNumber maximum number of elements in the list
     */
    private void setList(final String key, final List<String> list, final int maxNumber)
    {
        StringBuilder str = new StringBuilder();
        int n = Math.min(list.size(), maxNumber);
        if (n > 0)
        {
            list.stream().limit(n - 1).forEach((f) -> str.append(f).append("|"));
            str.append(list.get(n - 1));
            setProperty(key, str.toString());
        }
        else
        {
            clearProperty(key);
        }
    }

    // ====== Color ======

    /**
     * Returns color of given key.
     * @param key key
     * @return color
     */
    public Color getColor(final String key)
    {
        Throw.whenNull(key, "key");
        return this.colorCache.computeIfAbsent(key, (k) -> stringToColor(this.properties.getProperty(k)));
    }

    /**
     * Returns color from string.
     * @param colorString color as string
     * @return color
     */
    public static Color stringToColor(final String colorString)
    {
        Throw.whenNull(colorString, "colorString");
        String value = colorString.replace(" ", "");
        String[] channels = value.substring(1, value.length() - 1).split(",");
        return new Color(Integer.valueOf(channels[0]), Integer.valueOf(channels[1]), Integer.valueOf(channels[2]));
    }

    /**
     * Set color.
     * @param key key
     * @param color color
     */
    public void setColor(final String key, final Color color)
    {
        Throw.whenNull(key, "key");
        this.colorCache.put(key, color);
        setProperty(key, valueToString(color));
    }

    /**
     * Returns string from color.
     * @param color color
     * @return string from color
     */
    public static String valueToString(final Color color)
    {
        Throw.whenNull(color, "color");
        return String.format("[%d, %d, %d]", color.getRed(), color.getGreen(), color.getBlue());
    }

    // ====== int ======

    /**
     * Returns int for given key.
     * @param key key
     * @return int
     */
    public int getInt(final String key)
    {
        Throw.whenNull(key, "key");
        return this.intCache.computeIfAbsent(key, (k) -> Integer.valueOf(getProperty(k)));
    }

    /**
     * Set int value.
     * @param key key
     * @param value value
     */
    public void setInt(final String key, final int value)
    {
        Throw.whenNull(key, "key");
        this.intCache.put(key, value);
        setProperty(key, valueToString(value));
    }

    /**
     * Converts int to String.
     * @param value value
     * @return string
     */
    public static String valueToString(final int value)
    {
        return Integer.toString(value);
    }

    // ====== boolean ======

    /**
     * Returns boolean for given key.
     * @param key key
     * @return boolean
     */
    public boolean getBoolean(final String key)
    {
        Throw.whenNull(key, "key");
        return this.booleanCache.computeIfAbsent(key, (k) -> Boolean.valueOf(getProperty(k)));
    }

    /**
     * Set boolean value.
     * @param key key
     * @param value value
     */
    public void setBoolean(final String key, final boolean value)
    {
        Throw.whenNull(key, "key");
        this.booleanCache.put(key, value);
        setProperty(key, valueToString(value));
    }

    /**
     * Converts boolean to String.
     * @param value value
     * @return string
     */
    public static String valueToString(final boolean value)
    {
        return Boolean.toString(value);
    }

}
