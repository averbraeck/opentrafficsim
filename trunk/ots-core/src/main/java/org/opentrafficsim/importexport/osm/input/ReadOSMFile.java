package org.opentrafficsim.importexport.osm.input;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

import org.openstreetmap.osmosis.core.task.v0_6.RunnableSource;
import org.openstreetmap.osmosis.xml.common.CompressionMethod;
import org.openstreetmap.osmosis.xml.v0_6.XmlReader;
import org.opentrafficsim.importexport.osm.OSMNetwork;
import org.opentrafficsim.importexport.osm.OSMTag;
import org.opentrafficsim.importexport.osm.events.ProgressEvent;
import org.opentrafficsim.importexport.osm.events.ProgressListener;

import crosby.binary.osmosis.OsmosisReader;

/**
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version 31 dec. 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/mzhang">Mingxin Zhang </a>
 * @author <a>Moritz Bergmann</a>
 */
public final class ReadOSMFile
{

    /** The parser/network builder. */
    private OSMParser sinkImplementation = null;

    /**  */
    private boolean isReaderThreadDead = false;

    /** ProgressListener. */
    private ProgressListener progressListener;

    /**
     * @param location String; the location of the OSM file
     * @param wantedTags List&lt;Tag&gt;; the list of wanted tags
     * @param filteredKeys List&lt;String&gt;; the list of filtered keys
     * @param progListener
     * @throws URISyntaxException when <cite>location</cite> is not a valid URL
     * @throws FileNotFoundException when the OSM file can not be found
     * @throws MalformedURLException when <cite>location</cite> is not valid
     */
    public ReadOSMFile(final String location, final List<OSMTag> wantedTags, final List<String> filteredKeys,
            final ProgressListener progListener) throws URISyntaxException, FileNotFoundException,
            MalformedURLException
    {
        this.setProgressListener(progListener);
        URL url = new URL(location);
        File file = new File(url.toURI());

        this.sinkImplementation = new OSMParser(wantedTags, filteredKeys);
        this.sinkImplementation.setProgressListener(this.progressListener);

        CompressionMethod compression = CompressionMethod.None;
        boolean protocolBufferBinaryFormat = false;

        if (file.getName().endsWith(".pbf"))
        {
            protocolBufferBinaryFormat = true;
        }
        else if (file.getName().endsWith(".gz"))
        {
            compression = CompressionMethod.GZip;
        }
        else if (file.getName().endsWith(".bz2"))
        {
            compression = CompressionMethod.BZip2;
        }

        RunnableSource reader = null;

        if (protocolBufferBinaryFormat)
        {
            try
            {
                reader = new OsmosisReader(new FileInputStream(file));
            }
            catch (FileNotFoundException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        else
        {
            reader = new XmlReader(file, false, compression);
        }

        reader.setSink(this.sinkImplementation);

        Thread readerThread = new Thread(reader);
        this.progressListener.progress(new ProgressEvent(this, "Starting Import."));
        readerThread.start();
        while (readerThread.isAlive())
        {
            try
            {
                readerThread.join();
            }
            catch (InterruptedException e)
            {
                System.err.println("Failed to join with the map reader thread: " + e.getMessage());
            }
        }
        // Reader has finished
        this.isReaderThreadDead = true;
    }

    /**
     * @return is reader thread dead
     */
    public boolean checkisReaderThreadDead()
    {
        return this.isReaderThreadDead;
    }

    /**
     * @return get the whole Network
     */
    public OSMNetwork getNetwork()
    {
        return this.sinkImplementation.getNetwork();
    }

    /**
     * @return progressListener.
     */
    public ProgressListener getProgressListener()
    {
        return this.progressListener;
    }

    /**
     * @param progressListener set progressListener.
     */
    public void setProgressListener(final ProgressListener progressListener)
    {
        this.progressListener = progressListener;
    }
}
