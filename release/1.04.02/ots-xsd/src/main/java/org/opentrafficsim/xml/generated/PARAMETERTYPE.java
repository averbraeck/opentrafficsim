//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.3.0 
// See <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2020.08.12 at 01:25:19 AM CEST 
//


package org.opentrafficsim.xml.generated;

import java.io.Serializable;
import javax.annotation.Generated;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.opentrafficsim.xml.bindings.StaticFieldNameAdapter;


/**
 * <p>Java class for PARAMETERTYPE complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="PARAMETERTYPE"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;attribute name="ID" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="FIELD" use="required" type="{http://www.opentrafficsim.org/ots}STATICFIELDNAMETYPE" /&gt;
 *       &lt;attribute name="DESCRIPTION" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PARAMETERTYPE")
@XmlSeeAlso({
    PARAMETERTYPESTRING.class,
    PARAMETERTYPEACCELERATION.class,
    PARAMETERTYPEBOOLEAN.class,
    PARAMETERTYPECLASS.class,
    PARAMETERTYPEDOUBLE.class,
    PARAMETERTYPEFLOAT.class,
    PARAMETERTYPELONG.class,
    PARAMETERTYPEDURATION.class,
    PARAMETERTYPEFRACTION.class,
    PARAMETERTYPEFREQUENCY.class,
    PARAMETERTYPEINTEGER.class,
    PARAMETERTYPELENGTH.class,
    PARAMETERTYPELINEARDENSITY.class,
    PARAMETERTYPESPEED.class
})
@Generated(value = "com.sun.tools.xjc.Driver", date = "2020-08-12T01:25:19+02:00", comments = "JAXB RI v2.3.0")
public class PARAMETERTYPE implements Serializable
{

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2020-08-12T01:25:19+02:00", comments = "JAXB RI v2.3.0")
    private final static long serialVersionUID = 10102L;
    @XmlAttribute(name = "ID", required = true)
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2020-08-12T01:25:19+02:00", comments = "JAXB RI v2.3.0")
    protected String id;
    @XmlAttribute(name = "FIELD", required = true)
    @XmlJavaTypeAdapter(StaticFieldNameAdapter.class)
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2020-08-12T01:25:19+02:00", comments = "JAXB RI v2.3.0")
    protected Object field;
    @XmlAttribute(name = "DESCRIPTION")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2020-08-12T01:25:19+02:00", comments = "JAXB RI v2.3.0")
    protected String description;

    /**
     * Gets the value of the id property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2020-08-12T01:25:19+02:00", comments = "JAXB RI v2.3.0")
    public String getID() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2020-08-12T01:25:19+02:00", comments = "JAXB RI v2.3.0")
    public void setID(String value) {
        this.id = value;
    }

    /**
     * Gets the value of the field property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2020-08-12T01:25:19+02:00", comments = "JAXB RI v2.3.0")
    public Object getFIELD() {
        return field;
    }

    /**
     * Sets the value of the field property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2020-08-12T01:25:19+02:00", comments = "JAXB RI v2.3.0")
    public void setFIELD(Object value) {
        this.field = value;
    }

    /**
     * Gets the value of the description property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2020-08-12T01:25:19+02:00", comments = "JAXB RI v2.3.0")
    public String getDESCRIPTION() {
        return description;
    }

    /**
     * Sets the value of the description property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2020-08-12T01:25:19+02:00", comments = "JAXB RI v2.3.0")
    public void setDESCRIPTION(String value) {
        this.description = value;
    }

}