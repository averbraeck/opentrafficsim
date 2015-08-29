package nl.grontmij.smarttraffic.lane;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FileUtilities
{

    public FileUtilities()
    {
        // TODO Auto-generated constructor stub
    }

    // check and create outputMap
    public static void checkAndCreateMap(String dir)
    {
        if (!new File(dir).exists())
        {
            try
            {
                Files.createDirectory(Paths.get(dir));
            }
            catch (IOException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
}
