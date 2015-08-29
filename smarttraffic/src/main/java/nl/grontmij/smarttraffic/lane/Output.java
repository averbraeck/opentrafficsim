package nl.grontmij.smarttraffic.lane;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;

public class Output
{

    public Output()
    {
        // TODO Auto-generated constructor stub
    }

    public static BufferedWriter initiateOutputFile(String dirOutput, final OTSSimulatorInterface simulator,
        String fileName, String header)
    {
        BufferedWriter outputFile = null;
        {
            try
            {
                if (!new File(dirOutput).exists())
                {
                    Files.createDirectory(Paths.get(dirOutput));
                }
                File file = new File(dirOutput + "/" + fileName);
                if (!file.exists())
                {
                    file.createNewFile();
                }
                outputFile = new BufferedWriter(new FileWriter(file.getAbsoluteFile()));
                if (Settings.getBoolean((OTSDEVSSimulatorInterface) simulator, "HEADERS"))
                {
                    outputFile.write(header);
                    outputFile.flush();
                }
            }
            catch (IOException exception)
            {
                exception.printStackTrace();
                System.exit(-1);
            }
        }
        return outputFile;
    }

}
