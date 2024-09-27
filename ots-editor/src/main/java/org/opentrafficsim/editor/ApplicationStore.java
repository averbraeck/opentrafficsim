package org.opentrafficsim.editor;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
    private static final int MAX = 10;

    /** Store of loaded and set properties. */
    private final Properties store = new Properties();

    /** Application name. */
    private final String applicationName;

    /** File to store settings. */
    private final String file;

    /**
     * Constructor. Properties are stored under "{user.home}/{enterprise}/{application}.ini".
     * @param enterpriseName name of the enterprise.
     * @param applicationName name of the application.
     */
    public ApplicationStore(final String enterpriseName, final String applicationName)
    {
        Throw.whenNull(enterpriseName, "Enterprise may not bee null.");
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
    }

    /**
     * Returns the property value.
     * @param key key.
     * @return property value, or {@code null} if no value is given.
     */
    public synchronized String getProperty(final String key)
    {
        Throw.whenNull(key, "Key may not be null.");
        return (String) this.store.get(key);
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
        try
        {
            File f = new File(this.file);
            f.getParentFile().mkdirs();
            FileWriter writer = new FileWriter(f);
            this.store.store(writer, this.applicationName);
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
     * @param file latest file.
     */
    public void addRecentFile(final String key, final String file)
    {
        Throw.whenNull(key, "Key may not be null.");
        Throw.whenNull(file, "File may not be null.");
        List<String> files = getRecentFiles(key);
        if (files.contains(file))
        {
            if (files.get(0).equals(file))
            {
                return;
            }
            files.remove(file);
        }
        files.add(0, file);
        setFiles(key, files);
    }

    /**
     * Clears a recent file.
     * @param key key.
     * @param file file.
     */
    public void removeRecentFile(final String key, final String file)
    {
        List<String> files = getRecentFiles(key);
        files.remove(file);
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
        int n = Math.min(files.size(), MAX);
        files.stream().limit(n - 1).forEach((f) -> str.append(f).append("|"));
        str.append(files.get(n - 1));
        setProperty(key, str.toString());
    }

    /**
     * Removes key from the store.
     * @param key key.
     */
    public void clearProperty(final String key)
    {
        Throw.whenNull(key, "Key may not be null.");
        this.store.remove(key);
    }

}
