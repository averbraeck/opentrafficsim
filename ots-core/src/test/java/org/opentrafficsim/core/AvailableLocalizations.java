package org.opentrafficsim.core;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * Copyright (c) 2002-2014 Delft University of Technology, Jaffalaan 5, 2628 BX Delft, the Netherlands. All rights
 * reserved.
 * <p>
 * See for project information <a href="http://www.simulation.tudelft.nl/"> www.simulation.tudelft.nl</a>.
 * <p>
 * The OpenTrafficSim project is distributed under the following BSD-style license:<br>
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 * <ul>
 * <li>Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 * disclaimer.</li>
 * <li>Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
 * following disclaimer in the documentation and/or other materials provided with the distribution.</li>
 * <li>Neither the name of Delft University of Technology, nor the names of its contributors may be used to endorse or
 * promote products derived from this software without specific prior written permission.</li>
 * </ul>
 * This software is provided by the copyright holders and contributors "as is" and any express or implied warranties,
 * including, but not limited to, the implied warranties of merchantability and fitness for a particular purpose are
 * disclaimed. In no event shall the copyright holder or contributors be liable for any direct, indirect, incidental,
 * special, exemplary, or consequential damages (including, but not limited to, procurement of substitute goods or
 * services; loss of use, data, or profits; or business interruption) however caused and on any theory of liability,
 * whether in contract, strict liability, or tort (including negligence or otherwise) arising in any way out of the use
 * of this software, even if advised of the possibility of such damage.
 * @version Jun 11, 2014 <br>
 * @author <a href="http://tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class AvailableLocalizations
{
    /**
     * Build a list of locale names that are available for a given prefix.
     * @param prefix String; the prefix of the localization file names
     * @param path String; the path to the resource directory to scan
     * @return List&lt;String&gt;; the list of matching locale names (which <b>always</b> starts with "en", even though
     *         there may not be such a localization file)
     */
    public static List<String> availableLocalizations(final String prefix, final String path)
    {
        final String tail = ".properties";
        File[] propertiesFiles = new File(path).listFiles(new FileFilter()
        {
            @Override
            public boolean accept(File pathname)
            {
                String name = pathname.getName();
                return name.endsWith(tail) && name.startsWith(prefix + "_");
            }
        });
        List<String> locales = new ArrayList<String>();
        locales.add("en");
        for (File f : propertiesFiles)
        {
            String localeName = f.getName().substring(prefix.length() + 1);
            localeName = localeName.substring(0, 2);
            locales.add(localeName);
        }
        return locales;
    }

}
