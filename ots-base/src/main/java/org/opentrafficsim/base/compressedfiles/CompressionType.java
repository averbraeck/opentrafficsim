package org.opentrafficsim.base.compressedfiles;

/**
 * Types of compression supported by the compressed file writers and readers.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Oct 24, 2018 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public enum CompressionType
{
    /** Do not compress / uncompress the data. */
    NONE("txt"),

    /** Create or read a ZIP archive with one, top-level entry in the TOC. */
    ZIP("zip"),

    /** Create or read a gzip-compressed file. */
    GZIP("gz"),

    /** Auto-detect the compression method (only for readers). */
    AUTODETECT(null);

    /** Commonly used file extension for files containing data that is compressed using this method. */
    private final String extension;

    /**
     * Construct a CompressionType.
     * @param extension String; commonly used file extension for files containing data that is compressed using this method
     */
    CompressionType(final String extension)
    {
        this.extension = extension;
    }

    /**
     * Retrieve the commonly used file extension for files containing data that is compressed using this method.
     * @return String; the commonly used file extension for files containing data that is compressed using this method
     */
    public String getExtension()
    {
        return this.extension;
    }

}
