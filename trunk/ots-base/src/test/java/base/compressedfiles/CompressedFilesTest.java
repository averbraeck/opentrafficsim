package base.compressedfiles;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.commons.compress.utils.IOUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.opentrafficsim.base.compressedfiles.CompressionType;
import org.opentrafficsim.base.compressedfiles.Reader;
import org.opentrafficsim.base.compressedfiles.Writer;

/**
 * Test the compressed files package.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Oct 25, 2018 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class CompressedFilesTest
{
    /** Temporary directory that should be deleted by Junit at end of test. */
    @Rule
    public TemporaryFolder testDir = new TemporaryFolder();

    /**
     * Test the Writer class.
     * @throws IOException if that happens uncaught; this test has failed
     */
    @Test
    public final void testWriter() throws IOException
    {
        try
        {
            Writer.createOutputStream("", null);
            fail("null for compression type should have thrown a NullPointerException");
        }
        catch (NullPointerException npe)
        {
            // Ignore expected exception
        }
        try
        {
            Writer.createOutputStream(null, CompressionType.NONE);
            fail("null for compression type should have thrown a NullPointerException");
        }
        catch (NullPointerException npe)
        {
            // Ignore expected exception
        }
        try
        {
            Writer.createOutputStream("test", CompressionType.AUTODETECT);
            fail("null for compression type should have thrown an IOException");
        }
        catch (IOException ioe)
        {
            // Ignore expected exception
        }
    }

    /**
     * Test writes and read back the resulting file.
     * @throws IOException if that happens uncaught; this test has failed
     */
    @Test
    public final void writeAndReadTests() throws IOException
    {
        String testContent = "ABCDEFGHIJKLMNOPQRSTVWZYX1234567890";
        File containerDir = this.testDir.newFolder("subfolder");
        String testDirName = containerDir.getAbsolutePath();
        String testFileName = testDirName + File.separator + "test1";
        // System.out.println("test file name is " + testFileName);
        BufferedWriter bf =
                new BufferedWriter(new OutputStreamWriter(Writer.createOutputStream(testFileName, CompressionType.NONE)));
        bf.write(testContent);
        bf.close();
        String fixedFileName = testFileName + ".txt";
        assertTrue("File with .txt suffix should have been created", new File(fixedFileName).exists());
        // Read back the file
        String readContent = new String(Files.readAllBytes(new File(fixedFileName).toPath()));
        assertEquals("contents of txt file should match written data", testContent, readContent);
        // Delete the file
        Files.delete(Paths.get(fixedFileName));
        testFileName = testDirName + File.separator + "test2.tXT"; // Crazy capitalization
        bf = new BufferedWriter(new OutputStreamWriter(Writer.createOutputStream(testFileName, CompressionType.NONE)));
        bf.write(testContent);
        bf.close();
        fixedFileName = testFileName.substring(0, testFileName.length() - 4) + ".txt";
        assertTrue("File with .txt suffix should have been created", new File(fixedFileName).exists());
        // Read back the file
        readContent = new String(Files.readAllBytes(new File(fixedFileName).toPath()));
        assertEquals("contents of txt file should match written data", testContent, readContent);
        // Delete the file
        Files.delete(Paths.get(fixedFileName));
        testFileName = testDirName + File.separator + "t3"; // Very short file name (does not do as intended due to long path)
        bf = new BufferedWriter(new OutputStreamWriter(Writer.createOutputStream(testFileName, CompressionType.NONE)));
        bf.write(testContent);
        bf.close();
        fixedFileName = testFileName + ".txt";
        assertTrue("File with .txt suffix should have been created", new File(fixedFileName).exists());
        // Read back the file
        readContent = new String(Files.readAllBytes(new File(fixedFileName).toPath()));
        assertEquals("contents of txt file should match written data", testContent, readContent);
        // Read it back using the Reader
        InputStream inputStream = Reader.createInputStream(fixedFileName, CompressionType.NONE);
        readContent = new String(IOUtils.toByteArray(inputStream));
        inputStream.close();
        assertEquals("contents of txt file should match written data", testContent, readContent);
        // Read it back using the Reader using AUTODETECT
        inputStream = Reader.createInputStream(fixedFileName, CompressionType.AUTODETECT);
        readContent = new String(IOUtils.toByteArray(inputStream));
        inputStream.close();
        assertEquals("contents of txt file should match written data", testContent, readContent);
        // Read it back using the Reader using the no CompressionType method
        inputStream = Reader.createInputStream(fixedFileName);
        readContent = new String(IOUtils.toByteArray(inputStream));
        inputStream.close();
        assertEquals("contents of txt file should match written data", testContent, readContent);
        // Delete the file
        Files.delete(Paths.get(fixedFileName));
        testFileName = testDirName + File.separator + "test4";
        bf = new BufferedWriter(new OutputStreamWriter(Writer.createOutputStream(testFileName, CompressionType.ZIP)));
        bf.write(testContent);
        bf.close();
        fixedFileName = testFileName + ".zip";
        assertTrue("File with .zip suffix should have been created", new File(fixedFileName).exists());
        inputStream = Reader.createInputStream(fixedFileName, CompressionType.ZIP);
        assertTrue("toString method of zip reader is somewhat descriptive", inputStream.toString().contains("ZipInputStream"));
        readContent = new String(IOUtils.toByteArray(inputStream));
        inputStream.close();
        assertEquals("contents of txt file should match written data", testContent, readContent);
        // Read it back using the Reader using AUTODETECT
        inputStream = Reader.createInputStream(fixedFileName, CompressionType.AUTODETECT);
        readContent = new String(IOUtils.toByteArray(inputStream));
        inputStream.close();
        assertEquals("contents of txt file should match written data", testContent, readContent);
        // Delete the file
        Files.delete(Paths.get(fixedFileName));
        testFileName = testDirName + File.separator + "test5";
        bf = new BufferedWriter(new OutputStreamWriter(Writer.createOutputStream(testFileName, CompressionType.BZIP2)));
        bf.write(testContent);
        bf.close();
        fixedFileName = testFileName + ".bz2";
        assertTrue("File with .zip suffix should have been created", new File(fixedFileName).exists());
        inputStream = Reader.createInputStream(fixedFileName, CompressionType.BZIP2);
        readContent = new String(IOUtils.toByteArray(inputStream));
        inputStream.close();
        assertEquals("contents of txt file should match written data", testContent, readContent);
        // Read it back using the Reader using AUTODETECT
        inputStream = Reader.createInputStream(fixedFileName, CompressionType.AUTODETECT);
        readContent = new String(IOUtils.toByteArray(inputStream));
        inputStream.close();
        assertEquals("contents of txt file should match written data", testContent, readContent);
        // Delete the file
        Files.delete(Paths.get(fixedFileName));
        testFileName = testDirName + File.separator + "test6";
        bf = new BufferedWriter(new OutputStreamWriter(Writer.createOutputStream(testFileName, CompressionType.GZIP)));
        bf.write(testContent);
        bf.close();
        fixedFileName = testFileName + ".gz";
        assertTrue("File with .zip suffix should have been created", new File(fixedFileName).exists());
        inputStream = Reader.createInputStream(fixedFileName, CompressionType.GZIP);
        readContent = new String(IOUtils.toByteArray(inputStream));
        inputStream.close();
        assertEquals("contents of txt file should match written data", testContent, readContent);
        // Read it back using the Reader using AUTODETECT
        inputStream = Reader.createInputStream(fixedFileName, CompressionType.AUTODETECT);
        readContent = new String(IOUtils.toByteArray(inputStream));
        inputStream.close();
        assertEquals("contents of txt file should match written data", testContent, readContent);
        // Delete the file
        Files.delete(Paths.get(fixedFileName));
        // Not testing for the discontinuity problem in BZIP2 files https://issues.apache.org/jira/browse/COMPRESS-224
    }

}
