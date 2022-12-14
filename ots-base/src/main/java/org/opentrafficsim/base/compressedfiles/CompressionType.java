package org.opentrafficsim.base.compressedfiles;

/**
 * Types of compression supported by the compressed file writers and readers.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
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
