package org.opentrafficsim.core;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

/**
 * Build a list of the classes in the project (or under a specific directory in the project).<br>
 * Adapted from <a href="http://stackoverflow.com/questions/3923129/get-a-list-of-resources-from-classpath-directory">
 * http://stackoverflow.com/questions/3923129/get-a-list-of-resources-from-classpath-directory</a>.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Apr 8, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public final class ClassList
{
    /**
     * Do not instantiate this class.
     */
    private ClassList()
    {
        // This class cannot be instantiated
    }

    /**
     * For all elements of java.class.path get a Collection of resources Pattern pattern = Pattern.compile(".*"); gets all
     * resources.
     * @param pattern the pattern to match
     * @return the resources in the order they are found
     */
    public static Collection<String> getResources(final Pattern pattern)
    {
        final ArrayList<String> retval = new ArrayList<String>();
        final String classPath = System.getProperty("java.class.path", ".");
        final String[] classPathElements = classPath.split(System.getProperty("path.separator"));
        for (final String element : classPathElements)
        {
            retval.addAll(getResources(element, pattern));
        }
        return retval;
    }

    /**
     * Recursively load names from a resource tree.
     * @param element String; root of the tree to load
     * @param pattern Pattern; only return names matching this pattern
     * @return Collection&lt;String&gt;; a list of names
     */
    private static Collection<String> getResources(final String element, final Pattern pattern)
    {
        final ArrayList<String> retval = new ArrayList<String>();
        final File file = new File(element);
        if (file.isDirectory())
        {
            retval.addAll(getResourcesFromDirectory(file, pattern));
        }
        else
        {
            retval.addAll(getResourcesFromJarFile(file, pattern));
        }
        return retval;
    }

    /**
     * Recursively load names from a jar file.
     * @param file File; root of the tree to load
     * @param pattern Pattern; only return names matching this pattern
     * @return Collection&lt;String&gt;; a list of names
     */
    private static Collection<String> getResourcesFromJarFile(final File file, final Pattern pattern)
    {
        final ArrayList<String> retval = new ArrayList<String>();
        ZipFile zf;
        try
        {
            zf = new ZipFile(file);
        }
        catch (final ZipException e)
        {
            throw new Error(e);
        }
        catch (final IOException e)
        {
            throw new Error(e);
        }
        final Enumeration<?> e = zf.entries();
        while (e.hasMoreElements())
        {
            final ZipEntry ze = (ZipEntry) e.nextElement();
            final String fileName = ze.getName();
            final boolean accept = pattern.matcher(fileName).matches();
            if (accept)
            {
                retval.add(fileName);
            }
        }
        try
        {
            zf.close();
        }
        catch (final IOException e1)
        {
            throw new Error(e1);
        }
        return retval;
    }

    /**
     * Recursively load names from a directory. 
     * @param directory File; root of the tree to load
     * @param pattern Pattern; only return names matching this pattern
     * @return Collection&lt;String&gt;; a list of names
     */
    private static Collection<String> getResourcesFromDirectory(final File directory, final Pattern pattern)
    {
        final ArrayList<String> retval = new ArrayList<String>();
        final File[] fileList = directory.listFiles();
        for (final File file : fileList)
        {
            if (file.isDirectory())
            {
                retval.addAll(getResourcesFromDirectory(file, pattern));
            }
            else
            {
                try
                {
                    final String fileName = file.getCanonicalPath();
                    final boolean accept = pattern.matcher(fileName).matches();
                    if (accept)
                    {
                        retval.add(fileName);
                    }
                }
                catch (final IOException e)
                {
                    throw new Error(e);
                }
            }
        }
        return retval;
    }

    /**
     * List the resources that match args[0], or a fixed pattern to demonstrate the use of this class.
     * @param args args[0] is the pattern to match, or list all resources matching a built-in pattern if there are no args
     */
    public static void main(final String[] args)
    {
        Pattern pattern;
        if (args.length < 1)
        {
            pattern = Pattern.compile(".*classes.org.opentrafficsim.*\\.class");
        }
        else
        {
            pattern = Pattern.compile(args[0]);
        }
        final Collection<String> list = ClassList.getResources(pattern);
        for (final String name : list)
        {
            System.out.println(name);
        }
    }

}
