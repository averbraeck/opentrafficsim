//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a>
// Any modifications to this file will be lost upon recompilation of the source schema.
// Generated on: 2016.11.03 at 01:02:34 PM CET
//

package org.opentrafficsim.road.network.factory.vissim;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

@SuppressWarnings("checkstyle:JavadocStyle")
/**
 * <p>
 * Java class for anonymous complex type.
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * {@code
  <xsd:element name="GTUTYPE">
    <xsd:complexType>
      <xsd:attribute name="NAME" type="xsd:string" use="required" />
      <xsd:attribute ref="xml:base" />
    </xsd:complexType>
  </xsd:element>
 * }
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
@XmlRootElement(name = "GTUTYPE")
public class GTUTYPE
{

    @XmlAttribute(name = "NAME", required = true)
    protected String name;

    @XmlAttribute(name = "base", namespace = "http://www.w3.org/XML/1998/namespace")
    @XmlSchemaType(name = "anyURI")
    protected String base;

    /**
     * Gets the value of the name property.
     * @return possible object is {@link String }
     */
    public String getNAME()
    {
        return name;
    }

    /**
     * Sets the value of the name property.
     * @param value String; allowed object is {@link String }
     */
    public void setNAME(String value)
    {
        this.name = value;
    }

    /**
     * Gets the value of the base property.
     * @return possible object is {@link String }
     */
    public String getBase()
    {
        return base;
    }

    /**
     * Sets the value of the base property.
     * @param value String; allowed object is {@link String }
     */
    public void setBase(String value)
    {
        this.base = value;
    }

}
