//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.3.0 
// See <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2019.04.04 at 05:47:23 PM CEST 
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
import org.opentrafficsim.xml.bindings.ClassNameAdapter;


/**
 * <p>Java class for CLASSATTRIBUTETYPE complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CLASSATTRIBUTETYPE"&gt;
 *   &lt;simpleContent&gt;
 *     &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema&gt;string"&gt;
 *       &lt;attribute name="CLASS" type="{http://www.opentrafficsim.org/ots}CLASSNAMETYPE" /&gt;
 *     &lt;/extension&gt;
 *   &lt;/simpleContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CLASSATTRIBUTETYPE", propOrder = {
    "value"
})
@XmlSeeAlso({
    org.opentrafficsim.xml.generated.MODELTYPE.TACTICALPLANNER.LMRS.MANDATORYINCENTIVES.INCENTIVE.class,
    org.opentrafficsim.xml.generated.MODELTYPE.TACTICALPLANNER.LMRS.VOLUNTARYINCENTIVES.INCENTIVE.class,
    org.opentrafficsim.xml.generated.MODELTYPE.TACTICALPLANNER.LMRS.ACCELERATIONINCENTIVES.INCENTIVE.class,
    org.opentrafficsim.xml.generated.PERCEPTIONTYPE.CATEGORY.class,
    org.opentrafficsim.xml.generated.PERCEPTIONTYPE.MENTAL.FULLER.BEHAVIORALADAPTATION.class
})
@Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-04T05:47:23+02:00", comments = "JAXB RI v2.3.0")
public class CLASSATTRIBUTETYPE
    implements Serializable
{

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-04T05:47:23+02:00", comments = "JAXB RI v2.3.0")
    private final static long serialVersionUID = 10102L;
    @XmlValue
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-04T05:47:23+02:00", comments = "JAXB RI v2.3.0")
    protected String value;
    @XmlAttribute(name = "CLASS")
    @XmlJavaTypeAdapter(ClassNameAdapter.class)
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-04T05:47:23+02:00", comments = "JAXB RI v2.3.0")
    protected Class _class;

    /**
     * Gets the value of the value property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-04T05:47:23+02:00", comments = "JAXB RI v2.3.0")
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
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-04T05:47:23+02:00", comments = "JAXB RI v2.3.0")
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * Gets the value of the class property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-04T05:47:23+02:00", comments = "JAXB RI v2.3.0")
    public Class getCLASS() {
        return _class;
    }

    /**
     * Sets the value of the class property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-04T05:47:23+02:00", comments = "JAXB RI v2.3.0")
    public void setCLASS(Class value) {
        this._class = value;
    }

}
