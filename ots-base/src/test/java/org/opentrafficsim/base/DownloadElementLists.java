package org.opentrafficsim.base;

import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.opentrafficsim.base.logger.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Downloads {@code element-list} files from each {@code <offlineLink>} tag in the main pom.xml. These are subsequently used as
 * part of {@code maven-javadoc-plugin}.
 * <p>
 * Copyright (c) 2026-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.<br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * @author Wouter Schakel
 */
public final class DownloadElementLists
{

    /** Property pattern. */
    private static final Pattern PROPERTY = Pattern.compile("\\$\\{([^}]+)\\}");

    /**
     * Constructor.
     */
    private DownloadElementLists()
    {
        //
    }

    /**
     * Download all element-list files referenced in offlineLink tags of the pom.
     * @param args ignored
     * @throws Exception on unrecoverable failure
     */
    public static void main(final String[] args) throws Exception
    {
        Path baseDirectory = Paths.get(".").toAbsolutePath().normalize();
        Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                .parse(baseDirectory.getParent().resolve("pom.xml").toFile());
        XPath xpath = XPathFactory.newInstance().newXPath();

        Map<String, String> properties = getProperties(document, xpath);

        NodeList offlineLinks = (NodeList) xpath.evaluate("//offlineLink", document, XPathConstants.NODESET);
        HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(30))
                .followRedirects(HttpClient.Redirect.NORMAL).build();
        Set<String> processed = new HashSet<>();
        for (int i = 0; i < offlineLinks.getLength(); i++)
        {
            Node offlineLink = offlineLinks.item(i);
            String url = resolveProperties(xpath.evaluate("url", offlineLink).trim(), properties);
            String location = xpath.evaluate("location", offlineLink).trim();
            if (url.isEmpty() || location.isEmpty() || url.contains("opentrafficsim.org"))
            {
                continue;
            }
            String key = url + "|" + location;
            if (!processed.add(key))
            {
                continue;
            }
            String elementListUrl = url.endsWith("/") ? url + "element-list" : url + "/element-list";
            if (!location.startsWith("${basedir}/../"))
            {
                Logger.ots().warn("<location> \"{}\" of <offlineLink> does not start with \"${basedir}/../\" (skipping)",
                        location);
                continue;
            }
            location = location.replace("${basedir}/..", baseDirectory.getParent().toString());
            Path targetDirectory = baseDirectory.resolve(location).normalize();
            Files.createDirectories(targetDirectory);
            Path targetFile = targetDirectory.resolve("element-list");
            System.out.println(elementListUrl);
            try
            {
                HttpRequest request = HttpRequest.newBuilder().uri(URI.create(elementListUrl)).timeout(Duration.ofSeconds(60))
                        .header("User-Agent", "OTS-ElementList-Downloader").GET().build();
                HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());
                if (response.statusCode() != 200)
                {
                    System.err.println("HTTP " + response.statusCode() + ": " + elementListUrl);
                    continue;
                }
                try (InputStream stream = response.body())
                {
                    Files.copy(stream, targetFile, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                }
            }
            catch (Exception exception)
            {
                System.err.println("Failed to download " + elementListUrl + ": " + exception.getMessage());
            }
        }
    }

    /**
     * Returns a map of all properties in the document. E.g. {@code djutils.version} to {@code 6.1.0}.
     * @param document document of pom file
     * @param xpath XPath evaluator
     * @return map of all properties in the document
     * @throws XPathExpressionException on wrong XPath
     */
    private static Map<String, String> getProperties(final Document document, final XPath xpath) throws XPathExpressionException
    {
        Map<String, String> out = new LinkedHashMap<String, String>();
        Node properties = (Node) xpath.evaluate("/project/properties", document, XPathConstants.NODE);
        Node property = properties.getFirstChild();
        while (property != null)
        {
            if (property.hasChildNodes())
            {
                out.put(property.getNodeName(), property.getFirstChild().getNodeValue());
            }
            property = property.getNextSibling();
        }
        return out;
    }

    /**
     * Resolve properties in URL.
     * @param url URL
     * @param properties properties
     * @return URL with properties resolved.
     */
    public static String resolveProperties(final String url, final Map<String, String> properties)
    {
        Matcher matcher = PROPERTY.matcher(url);
        StringBuffer result = new StringBuffer();
        while (matcher.find())
        {
            String propertyName = matcher.group(1);
            String replacement = properties.getOrDefault(propertyName, matcher.group());
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(result);
        return result.toString();
    }

}
