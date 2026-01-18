package org.opentrafficsim.editor;

import java.awt.Color;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import org.djutils.exceptions.Throw;

/**
 * Stores preferences, recently opened files, etc.
 * <p>
 * Copyright (c) 2024-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class ApplicationStore
{

    /** Maximum number of recent files. */
    private static final int MAX_SAVED_FILES = 10;

    /** Maximum length for tooltips. */
    private static final int MAX_TOOLTIP_LENGTH = 96;

    /** Maximum number of items to show in a dropdown menu. */
    private static final int MAX_DROPDOWN_ITEMS = 20;

    /** Maximum number of back navigation steps stored. */
    private static final int MAX_NAVIGATE = 50;

    /** Color for invalid nodes and values (background). */
    private static final Color INVALID_COLOR = new Color(255, 240, 240);

    /** Color for expression nodes and values (background). */
    private static final Color EXPRESSION_COLOR = new Color(252, 250, 239);

    /** Store of loaded and set properties. */
    private final Properties store;

    /** Application name. */
    private final String applicationName;

    /** File to store settings. */
    private final String file;

    /** Cached colors. */
    private final Map<String, Color> colorCache = new LinkedHashMap<>();

    /** Cached ints. */
    private final Map<String, Integer> intCache = new LinkedHashMap<>();

    /**
     * Constructor. Properties are stored under "{user.home}/{enterprise}/{application}.ini".
     * @param enterpriseName name of the enterprise.
     * @param applicationName name of the application.
     */
    public ApplicationStore(final String enterpriseName, final String applicationName)
    {
        Throw.whenNull(enterpriseName, "Enterprise may not bee null.");
        this.store = new Properties();
        this.store.put("expression_color", stringFromColor(EXPRESSION_COLOR));
        this.store.put("invalid_color", stringFromColor(INVALID_COLOR));
        this.store.put("max_tooltip_length", Integer.toString(MAX_TOOLTIP_LENGTH));
        this.store.put("max_dropdown_items", Integer.toString(MAX_DROPDOWN_ITEMS));
        this.store.put("max_navigate", Integer.toString(MAX_NAVIGATE));
        this.applicationName = applicationName;
        this.file =
                System.getProperty("user.home") + File.separator + enterpriseName + File.separator + applicationName + ".ini";
        if (new File(this.file).exists())
        {
            try
            {
                FileReader reader = new FileReader(this.file);
                this.store.load(reader);
            }
            catch (IOException ex)
            {
                //
            }
        }
        save(); // stores defaults as an example to edit the file
    }

    /**
     * Returns the property value.
     * @param key key.
     * @return property value, empty if no value is given.
     */
    public synchronized Optional<String> getProperty(final String key)
    {
        Throw.whenNull(key, "Key may not be null.");
        return Optional.ofNullable((String) this.store.get(key));
    }

    /**
     * Sets a property value.
     * @param key key.
     * @param value value.
     */
    public synchronized void setProperty(final String key, final String value)
    {
        Throw.whenNull(key, "Key may not be null.");
        Throw.whenNull(value, "Value may not be null.");
        this.store.put(key, value);
        save();
    }

    /**
     * Save the properties.
     */
    private void save()
    {
        try
        {
            File f = new File(this.file);
            f.getParentFile().mkdirs();
            FileWriter writer = new FileWriter(f);
            this.store.store(writer, this.applicationName + " user settings");
        }
        catch (IOException exception)
        {
            //
        }
    }

    /**
     * Returns recent files.
     * @param key key under which files are stored.
     * @return files (recent to old).
     */
    public List<String> getRecentFiles(final String key)
    {
        Throw.whenNull(key, "Key may not be null.");
        List<String> out = new ArrayList<>();
        if (this.store.containsKey(key))
        {
            String filesAll = (String) this.store.get(key);
            String[] files = filesAll.split("\\|");
            Arrays.stream(files).forEach(out::add);
        }
        return out;
    }

    /**
     * Add recent file. If the file is already in the list, it is moved to the front.
     * @param key key under which files are stored.
     * @param fileName latest file.
     */
    public void addRecentFile(final String key, final String fileName)
    {
        Throw.whenNull(key, "Key may not be null.");
        Throw.whenNull(fileName, "File may not be null.");
        List<String> files = getRecentFiles(key);
        if (files.contains(fileName))
        {
            if (files.get(0).equals(fileName))
            {
                return;
            }
            files.remove(fileName);
        }
        files.add(0, fileName);
        setFiles(key, files);
    }

    /**
     * Clears a recent file.
     * @param key key.
     * @param fileName file.
     */
    public void removeRecentFile(final String key, final String fileName)
    {
        List<String> files = getRecentFiles(key);
        files.remove(fileName);
        setFiles(key, files);
    }

    /**
     * Sets the files.
     * @param key key.
     * @param files files.
     */
    private void setFiles(final String key, final List<String> files)
    {
        StringBuilder str = new StringBuilder();
        int n = Math.min(files.size(), MAX_SAVED_FILES);
        if (n > 0)
        {
            files.stream().limit(n - 1).forEach((f) -> str.append(f).append("|"));
            str.append(files.get(n - 1));
            setProperty(key, str.toString());
        }
        else
        {
            clearProperty(key);
        }
    }

    /**
     * Removes key from the store.
     * @param key key.
     */
    public void clearProperty(final String key)
    {
        Throw.whenNull(key, "Key may not be null.");
        this.store.remove(key);
        save();
    }

    /**
     * Returns color of given key.
     * @param key key
     * @return color
     */
    public Color getColor(final String key)
    {
        return this.colorCache.computeIfAbsent(key, (k) -> colorFromString(this.store.getProperty(k)));
    }

    /**
     * Returns color from string.
     * @param colorString color as string
     * @return color
     */
    private static Color colorFromString(final String colorString)
    {
        String value = colorString.replace(" ", "");
        String[] channels = value.substring(1, value.length() - 1).split(",");
        return new Color(Integer.valueOf(channels[0]), Integer.valueOf(channels[1]), Integer.valueOf(channels[2]));
    }

    /**
     * Returns string from color.
     * @param color color
     * @return string from color.
     */
    private String stringFromColor(final Color color)
    {
        return String.format("[%d, %d, %d]", color.getRed(), color.getGreen(), color.getBlue());
    }

    /**
     * Returns int for given key.
     * @param key key
     * @return int
     */
    public int getInt(final String key)
    {
        return this.intCache.computeIfAbsent(key, (k) -> Integer.valueOf(this.store.getProperty(k)));
    }
}
