
package org.opentrafficsim.xml.generated;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.w3c.dom.Element;

import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAnyAttribute;
import jakarta.xml.bind.annotation.XmlAnyElement;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElementRef;
import jakarta.xml.bind.annotation.XmlMixed;
import jakarta.xml.bind.annotation.XmlSchemaType;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Java class for includeType complex type</p>.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.</p>
 * 
 * <pre>{@code
 * <complexType name="includeType">
 *   <complexContent>
 *     <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       <choice maxOccurs="unbounded" minOccurs="0">
 *         <element ref="{http://www.w3.org/2001/XInclude}fallback"/>
 *         <any processContents='lax' namespace='##other'/>
 *         <any processContents='lax' namespace=''/>
 *       </choice>
 *       <attribute name="href" type="{http://www.w3.org/2001/XMLSchema}anyURI" />
 *       <attribute name="parse" type="{http://www.w3.org/2001/XInclude}parseType" default="xml" />
 *       <attribute name="xpointer" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       <attribute name="encoding" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       <attribute name="accept" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       <attribute name="accept-language" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       <anyAttribute processContents='lax' namespace='##other'/>
 *     </restriction>
 *   </complexContent>
 * </complexType>
 * }</pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "includeType", namespace = "http://www.w3.org/2001/XInclude", propOrder = {
    "content"
})
@SuppressWarnings("all") public class IncludeType implements Serializable
{

    private static final long serialVersionUID = 10102L;
    @XmlElementRef(name = "fallback", namespace = "http://www.w3.org/2001/XInclude", type = JAXBElement.class, required = false)
    @XmlMixed
    @XmlAnyElement(lax = true)
    protected List<Object> content;
    @XmlAttribute(name = "href")
    @XmlSchemaType(name = "anyURI")
    protected String href;
    @XmlAttribute(name = "parse")
    protected ParseType parse;
    @XmlAttribute(name = "xpointer")
    protected String xpointer;
    @XmlAttribute(name = "encoding")
    protected String encoding;
    @XmlAttribute(name = "accept")
    protected String accept;
    @XmlAttribute(name = "accept-language")
    protected String acceptLanguage;
    @XmlAnyAttribute
    private Map<QName, String> otherAttributes = new HashMap<>();

    /**
     * Gets the value of the content property.
     * 
     * <p>This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the content property.</p>
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * </p>
     * <pre>
     * getContent().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link JAXBElement }{@code <}{@link FallbackType }{@code >}
     * {@link Object }
     * {@link String }
     * {@link Element }
     * </p>
     * 
     * 
     * @return
     *     The value of the content property.
     */
    public List<Object> getContent() {
        if (content == null) {
            content = new ArrayList<>();
        }
        return this.content;
    }

    /**
     * Gets the value of the href property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getHref() {
        return href;
    }

    /**
     * Sets the value of the href property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setHref(String value) {
        this.href = value;
    }

    /**
     * Gets the value of the parse property.
     * 
     * @return
     *     possible object is
     *     {@link ParseType }
     *     
     */
    public ParseType getParse() {
        if (parse == null) {
            return ParseType.XML;
        } else {
            return parse;
        }
    }

    /**
     * Sets the value of the parse property.
     * 
     * @param value
     *     allowed object is
     *     {@link ParseType }
     *     
     */
    public void setParse(ParseType value) {
        this.parse = value;
    }

    /**
     * Gets the value of the xpointer property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getXpointer() {
        return xpointer;
    }

    /**
     * Sets the value of the xpointer property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setXpointer(String value) {
        this.xpointer = value;
    }

    /**
     * Gets the value of the encoding property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEncoding() {
        return encoding;
    }

    /**
     * Sets the value of the encoding property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEncoding(String value) {
        this.encoding = value;
    }

    /**
     * Gets the value of the accept property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAccept() {
        return accept;
    }

    /**
     * Sets the value of the accept property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAccept(String value) {
        this.accept = value;
    }

    /**
     * Gets the value of the acceptLanguage property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAcceptLanguage() {
        return acceptLanguage;
    }

    /**
     * Sets the value of the acceptLanguage property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAcceptLanguage(String value) {
        this.acceptLanguage = value;
    }

    /**
     * Gets a map that contains attributes that aren't bound to any typed property on this class.
     * 
     * <p>
     * the map is keyed by the name of the attribute and 
     * the value is the string value of the attribute.
     * 
     * the map returned by this method is live, and you can add new attribute
     * by updating the map directly. Because of this design, there's no setter.
     * 
     * 
     * @return
     *     always non-null
     */
    public Map<QName, String> getOtherAttributes() {
        return otherAttributes;
    }

}
