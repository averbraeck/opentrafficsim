//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.3.0 
// See <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2019.04.22 at 03:02:02 PM CEST 
//


package org.opentrafficsim.xml.generated;

import java.io.Serializable;
import javax.annotation.Generated;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.xml.bindings.TimeAdapter;


/**
 * <p>Java class for LEVELTIMETYPE complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="LEVELTIMETYPE"&gt;
 *   &lt;simpleContent&gt;
 *     &lt;extension base="&lt;http://www.opentrafficsim.org/ots&gt;LEVELTYPE"&gt;
 *       &lt;attribute name="TIME" type="{http://www.opentrafficsim.org/ots}TIMETYPE" /&gt;
 *     &lt;/extension&gt;
 *   &lt;/simpleContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "LEVELTIMETYPE", propOrder = {
    "value"
})
@Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T03:02:02+02:00", comments = "JAXB RI v2.3.0")
public class LEVELTIMETYPE
    implements Serializable
{

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T03:02:02+02:00", comments = "JAXB RI v2.3.0")
    private final static long serialVersionUID = 10102L;
    @XmlValue
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T03:02:02+02:00", comments = "JAXB RI v2.3.0")
    protected String value;
    @XmlAttribute(name = "TIME")
    @XmlJavaTypeAdapter(TimeAdapter.class)
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T03:02:02+02:00", comments = "JAXB RI v2.3.0")
    protected Time time;

    /**
     * Gets the value of the value property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T03:02:02+02:00", comments = "JAXB RI v2.3.0")
    public String getValue() {
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
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T03:02:02+02:00", comments = "JAXB RI v2.3.0")
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * Gets the value of the time property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T03:02:02+02:00", comments = "JAXB RI v2.3.0")
    public Time getTIME() {
        return time;
    }

    /**
     * Sets the value of the time property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T03:02:02+02:00", comments = "JAXB RI v2.3.0")
    public void setTIME(Time value) {
        this.time = value;
    }

}
