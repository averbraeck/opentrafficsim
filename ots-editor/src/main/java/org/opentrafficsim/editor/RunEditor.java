package org.opentrafficsim.editor;

import java.io.IOException;
import java.net.URISyntaxException;

import javax.naming.NamingException;
import javax.xml.parsers.ParserConfigurationException;

import org.djutils.logger.CategoryLogger;
import org.opentrafficsim.base.Resource;
import org.opentrafficsim.editor.decoration.DefaultDecorator;
import org.pmw.tinylog.Level;
import org.xml.sax.SAXException;

/**
 * Runs the editor with default decoration and built-in XML schema.
 * <p>
 * Copyright (c) 2023-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
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
     * @param args arguments.
     * @throws IOException exception
     * @throws SAXException exception
     * @throws ParserConfigurationException exception
     * @throws InterruptedException exception
     * @throws URISyntaxException exception
     * @throws NamingException exception
     */
    public static void main(final String[] args) throws IOException, SAXException, ParserConfigurationException,
            InterruptedException, URISyntaxException, NamingException
    {
        CategoryLogger.setAllLogLevel(Level.TRACE);
        OtsEditor editor = new OtsEditor();
        DefaultDecorator.decorate(editor);
        editor.setSchema(DocumentReader.open(Resource.getResourceAsUri("/xsd/ots.xsd")));
    }

}
