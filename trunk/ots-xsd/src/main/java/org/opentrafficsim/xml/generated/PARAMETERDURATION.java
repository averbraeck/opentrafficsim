//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.3.0 
// See <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2020.08.12 at 12:28:09 AM CEST 
//


package org.opentrafficsim.xml.generated;

import java.io.Serializable;
import javax.annotation.Generated;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.djunits.value.vdouble.scalar.Duration;
import org.opentrafficsim.xml.bindings.DurationAdapter;


/**
 * <p>Java class for PARAMETERDURATION complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="PARAMETERDURATION"&gt;
 *   &lt;simpleContent&gt;
 *     &lt;extension base="&lt;http://www.opentrafficsim.org/ots&gt;DURATIONTYPE"&gt;
 *       &lt;attribute name="ID" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *     &lt;/extension&gt;
 *   &lt;/simpleContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PARAMETERDURATION", propOrder = {
    "value"
})
@XmlSeeAlso({
    org.opentrafficsim.xml.generated.MODELTYPE.MODELPARAMETERS.DURATION.class
})
@Generated(value = "com.sun.tools.xjc.Driver", date = "2020-08-12T12:28:09+02:00", comments = "JAXB RI v2.3.0")
public class PARAMETERDURATION implements Serializable
{

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2020-08-12T12:28:09+02:00", comments = "JAXB RI v2.3.0")
    private final static long serialVersionUID = 10102L;
    @XmlValue
    @XmlJavaTypeAdapter(DurationAdapter.class)
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2020-08-12T12:28:09+02:00", comments = "JAXB RI v2.3.0")
    protected Duration value;
    @XmlAttribute(name = "ID")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2020-08-12T12:28:09+02:00", comments = "JAXB RI v2.3.0")
    protected String id;

    /**
     * Gets the value of the value property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2020-08-12T12:28:09+02:00", comments = "JAXB RI v2.3.0")
    public Duration getValue() {
        return value;
    }

    /**
     * Sets the value of the value property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2020-08-12T12:28:09+02:00", comments = "JAXB RI v2.3.0")
    public void setValue(Duration value) {
        this.value = value;
    }

    /**
     * Gets the value of the id property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2020-08-12T12:28:09+02:00", comments = "JAXB RI v2.3.0")
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
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2020-08-12T12:28:09+02:00", comments = "JAXB RI v2.3.0")
    public void setID(String value) {
        this.id = value;
    }

}
