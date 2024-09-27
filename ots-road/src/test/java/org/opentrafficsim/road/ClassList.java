package org.opentrafficsim.road;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

/**
 * Build a list of the classes in the project (or under a specific directory/package in the project).<br>
 * Adapted from <a href="https://stackoverflow.com/questions/3923129/get-a-list-of-resources-from-classpath-directory">
 * https://stackoverflow.com/questions/3923129/get-a-list-of-resources-from-classpath-directory</a> which apparently copied the
 * code from <a href="http://forums.devx.com/showthread.php?153784-how-to-list-resources-in-a-package">
 * http://forums.devx.com/showthread.php?153784-how-to-list-resources-in-a-package</a>. Original poster stoughto has not visited
 * that forum after 2006.
 * <p>
 * Copyright (c) 2006 by stoughto! TODO replace this by something that is provably free code.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
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
     * @param element root of the tree to load
     * @param pattern only return names matching this pattern
     * @return a list of names
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
     * @param file root of the tree to load
     * @param pattern only return names matching this pattern
     * @return a list of names
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
     * @param directory root of the tree to load
     * @param pattern only return names matching this pattern
     * @return a list of names
     */
    private static Collection<String> getResourcesFromDirectory(final File directory, final Pattern pattern)
    {
        final ArrayList<String> retval = new ArrayList<String>();
        final File[] fileList = directory.listFiles();
        if (null == fileList)
        {
            throw new Error("Could not list files in directory " + directory.toString());
        }
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
     * Return a List of all the classes under a package. Test-classes are excluded from the result.
     * @param packageRoot String package name
     * @param excludeInterfaces if true; interfaces are excluded from the result
     * @return the classes under the package
     */
    public static Collection<Class<?>> classList(final String packageRoot, final boolean excludeInterfaces)
    {
        Collection<String> classList = ClassList.getResources(Pattern.compile(".*[^-]classes." + packageRoot + ".*\\.class"));
        Collection<Class<?>> result = new ArrayList<Class<?>>();
        for (String className : classList)
        {
            int pos = className.indexOf("\\org\\");
            if (pos >= 0)
            {
                className = className.substring(pos + 1);
            }
            className = className.replaceAll("\\\\", ".");
            pos = className.lastIndexOf(".class");
            if (pos >= 0)
            {
                className = className.substring(0, pos);
            }
            if (className.endsWith("package-info"))
            {
                continue; // Not a real class
            }
            // System.out.println("Checking class \"" + className + "\"");
            try
            {
                Class<?> c = Class.forName(className);
                // System.out.println("modifiers: " + Modifier.toString(c.getModifiers()));
                boolean exclude = false;
                if (excludeInterfaces)
                {
                    for (String modifierString : Modifier.toString(c.getModifiers()).split(" "))
                    {
                        if (modifierString.equals("interface"))
                        {
                            // System.out.println(className + " is an interface");
                            exclude = true;
                            continue;
                        }
                    }
                }
                if (!exclude)
                {
                    result.add(c);
                }
            }
            catch (ClassNotFoundException exception)
            {
                exception.printStackTrace();
            }
        }
        return result;
    }

    /**
     * Determine if a class is an anonymous inner class.
     * @param c the class to check
     * @return true if <cite>c</cite> is an anonymous inner class; false otherwise
     */
    public static boolean isAnonymousInnerClass(final Class<?> c)
    {
        String className = c.getName();
        int pos = className.lastIndexOf("$");
        if (pos > 0)
        {
            while (++pos < className.length())
            {
                if (!Character.isDigit(className.charAt(pos)))
                {
                    break;
                }
            }
            if (pos >= className.length())
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Report if a class has non-static fields.
     * @param c the class
     * @return true if the class has non-static fields
     */
    public static boolean hasNonStaticFields(final Class<?> c)
    {
        for (Field f : c.getDeclaredFields())
        {
            // System.out.println("field " + f.getName() + ": " + Modifier.toString(f.getModifiers()));
            if (!Modifier.isStatic(f.getModifiers()))
            {
                return true;
            }
        }
        if (c.equals(Object.class))
        {
            return false;
        }
        return hasNonStaticFields(c.getSuperclass());
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
            pattern = Pattern.compile(".*[^-]classes.org.opentrafficsim.*\\.class");
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
