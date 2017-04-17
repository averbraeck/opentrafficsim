//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a>
// Any modifications to this file will be lost upon recompilation of the source schema.
// Generated on: 2016.11.03 at 01:02:34 PM CET
//

package org.opentrafficsim.road.network.factory.vissim.xsd;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * <p>
 * Java class for anonymous complex type.
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="NAME" use="required" type="{http://www.w3.org/2001/XMLSchema}ID" />
 *       &lt;attribute name="COORDINATE" type="{http://www.opentrafficsim.org/ots}COORDINATETYPE" />
 *       &lt;attribute name="ANGLE" type="{http://www.opentrafficsim.org/ots}ANGLETYPE" />
 *       &lt;attribute ref="{http://www.w3.org/XML/1998/namespace}base"/>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
@XmlRootElement(name = "NODE")
public class NODE {

    @XmlAttribute(name = "NAME", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    @XmlSchemaType(name = "ID")
    protected String name;

    @XmlAttribute(name = "COORDINATE")
    protected String coordinate;

    @XmlAttribute(name = "ANGLE")
    protected String angle;

    @XmlIDREF
    private NODE refId;

    @XmlAttribute(name = "base", namespace = "http://www.w3.org/XML/1998/namespace")
    @XmlSchemaType(name = "anyURI")
    protected String base;

    /**
     * Gets the value of the name property.
     * @return possible object is {@link String }
     */
    public String getNAME() {
        return name;
    }

    /**
     * Sets the value of the name property.
     * @param value allowed object is {@link String }
     */
    public void setNAME(String value) {
        this.name = value;
    }

    /**
     * Gets the value of the coordinate property.
     * @return possible object is {@link String }
     */
    public String getCOORDINATE() {
        return coordinate;
    }

    /**
     * Sets the value of the coordinate property.
     * @param value allowed object is {@link String }
     */
    public void setCOORDINATE(String value) {
        this.coordinate = value;
    }

    /**
     * Gets the value of the angle property.
     * @return possible object is {@link String }
     */
    public String getANGLE() {
        return angle;
    }

    /**
     * Sets the value of the angle property.
     * @param value allowed object is {@link String }
     */
    public void setANGLE(String value) {
        this.angle = value;
    }

    /**
     * Gets the value of the base property.
     * @return possible object is {@link String }
     */
    public String getBase() {
        return base;
    }

    /**
     * Sets the value of the base property.
     * @param value allowed object is {@link String }
     */
    public void setBase(String value) {
        this.base = value;
    }

    public NODE getRefId() {
        return refId;
    }

    public void setRefId(NODE refId) {
        this.refId = refId;
    }

}
