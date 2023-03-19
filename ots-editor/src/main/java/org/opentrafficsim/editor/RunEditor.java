package org.opentrafficsim.editor;

import java.io.IOException;
import java.net.URISyntaxException;

import javax.xml.parsers.ParserConfigurationException;

import org.opentrafficsim.base.Resource;
import org.xml.sax.SAXException;

/**
 * Runs the editor with default decoration and built-in XML schema.
 * @author wjschakel
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
