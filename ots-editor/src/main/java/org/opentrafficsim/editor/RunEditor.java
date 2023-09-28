package org.opentrafficsim.editor;

import java.io.IOException;
import java.net.URISyntaxException;

import javax.xml.parsers.ParserConfigurationException;

import org.opentrafficsim.base.Resource;
import org.opentrafficsim.editor.decoration.DefaultDecorator;
import org.xml.sax.SAXException;

/**
 * Runs the editor with default decoration and built-in XML schema.
 * <p>
 * Copyright (c) 2023-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public final class RunEditor
{

    /**
     * Private constructor.
     */
    private RunEditor()
    {

    }

    /**
     * Runs the editor.
     * @param args String[]; arguments.
     * @throws IOException exception
     * @throws SAXException exception
     * @throws ParserConfigurationException exception
     * @throws InterruptedException exception
     * @throws URISyntaxException exception
     */
    public static void main(final String[] args)
            throws IOException, SAXException, ParserConfigurationException, InterruptedException, URISyntaxException
    {
        OtsEditor editor = new OtsEditor();
        DefaultDecorator.decorate(editor);
        editor.setSchema(DocumentReader.open(Resource.getResourceAsUri("/xsd/ots.xsd")));
    }

}
