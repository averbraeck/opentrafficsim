package org.opentrafficsim.road.network.factory.osm.input;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

import org.openstreetmap.osmosis.core.task.v0_6.RunnableSource;
import org.openstreetmap.osmosis.xml.common.CompressionMethod;
import org.openstreetmap.osmosis.xml.v0_6.XmlReader;
import org.opentrafficsim.road.network.factory.osm.OSMNetwork;
import org.opentrafficsim.road.network.factory.osm.OSMTag;
import org.opentrafficsim.road.network.factory.osm.events.ProgressEvent;
import org.opentrafficsim.road.network.factory.osm.events.ProgressListener;

import crosby.binary.osmosis.OsmosisReader;

/**
 * <p>
 * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2015-07-26 01:01:13 +0200 (Sun, 26 Jul 2015) $, @version $Revision: 1155 $, by $Author: averbraeck $,
 * initial version 31 dec. 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/mzhang">Mingxin Zhang </a>
 * @author <a>Moritz Bergmann</a>
 */
public final class ReadOSMFile implements Serializable
{

    /** */
    private static final long serialVersionUID = 20141231L;

    /** The parser/network builder. */
    private OSMParser sinkImplementation = null;

    /**  */
    private boolean isReaderThreadDead = false;

    /**
     * @param location String; the location of the OSM file
     * @param wantedTags List&lt;OSMTag&gt;; the list of wanted tags
     * @param filteredKeys List&lt;String&gt;; the list of filtered keys
     * @param progressListener ProgressListener; ProgressListener
     * @throws URISyntaxException when <cite>location</cite> is not a valid URL
     * @throws FileNotFoundException when the OSM file can not be found
     * @throws MalformedURLException when <cite>location</cite> is not valid
     */
    public ReadOSMFile(final String location, final List<OSMTag> wantedTags, final List<String> filteredKeys,
            final ProgressListener progressListener) throws URISyntaxException, FileNotFoundException, MalformedURLException
    {
        URL url = new URL(location);
        File file = new File(url.toURI());

        this.sinkImplementation = new OSMParser(wantedTags, filteredKeys);
        this.sinkImplementation.setProgressListener(progressListener);

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

        RunnableSource reader = protocolBufferBinaryFormat ? new OsmosisReader(new FileInputStream(file))
                : new XmlReader(file, false, compression);

        reader.setSink(this.sinkImplementation);

        Thread readerThread = new Thread(reader);
        this.sinkImplementation.getProgressListener().progress(new ProgressEvent(this, "Starting Import."));
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
        return this.sinkImplementation.getProgressListener();
    }

    /**
     * @param progressListener ProgressListener; set progressListener.
     */
    public void setProgressListener(final ProgressListener progressListener)
    {
        this.sinkImplementation.setProgressListener(progressListener);
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "ReadOSMFile [sinkImplementation=" + this.sinkImplementation + ", isReaderThreadDead=" + this.isReaderThreadDead
                + "]";
    }
}
