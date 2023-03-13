package org.opentrafficsim.editor;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import javax.xml.parsers.ParserConfigurationException;

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
        URL url = RunEditor.class.getResource("/resources/xsd/ots.xsd");
        editor.setSchema(DocumentReader.open(url.toURI()));
    }

}
