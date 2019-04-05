//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.3.0 
// See <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2019.04.05 at 02:11:24 PM CEST 
//


package org.opentrafficsim.xml.generated;

import java.io.Serializable;
import javax.annotation.Generated;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.xml.bindings.DurationAdapter;
import org.opentrafficsim.xml.bindings.TimeAdapter;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="STARTTIME" type="{http://www.opentrafficsim.org/ots}TIMETYPE" minOccurs="0"/&gt;
 *         &lt;element name="WARMUPPERIOD" type="{http://www.opentrafficsim.org/ots}DURATIONTYPE" minOccurs="0"/&gt;
 *         &lt;element name="RUNLENGTH" type="{http://www.opentrafficsim.org/ots}DURATIONTYPE"/&gt;
 *         &lt;element name="NUMBERREPLICATIONS" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.opentrafficsim.org/ots}RANDOMSTREAMS" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute ref="{http://www.w3.org/XML/1998/namespace}base"/&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "starttime",
    "warmupperiod",
    "runlength",
    "numberreplications",
    "randomstreams"
})
@XmlRootElement(name = "RUN")
@Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-05T02:11:24+02:00", comments = "JAXB RI v2.3.0")
public class RUN
    implements Serializable
{

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-05T02:11:24+02:00", comments = "JAXB RI v2.3.0")
    private final static long serialVersionUID = 10102L;
    @XmlElement(name = "STARTTIME", type = String.class, defaultValue = "0s")
    @XmlJavaTypeAdapter(TimeAdapter.class)
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-05T02:11:24+02:00", comments = "JAXB RI v2.3.0")
    protected Time starttime;
    @XmlElement(name = "WARMUPPERIOD", type = String.class, defaultValue = "0s")
    @XmlJavaTypeAdapter(DurationAdapter.class)
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-05T02:11:24+02:00", comments = "JAXB RI v2.3.0")
    protected Duration warmupperiod;
    @XmlElement(name = "RUNLENGTH", required = true, type = String.class)
    @XmlJavaTypeAdapter(DurationAdapter.class)
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-05T02:11:24+02:00", comments = "JAXB RI v2.3.0")
    protected Duration runlength;
    @XmlElement(name = "NUMBERREPLICATIONS", defaultValue = "1")
    @XmlSchemaType(name = "unsignedInt")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-05T02:11:24+02:00", comments = "JAXB RI v2.3.0")
    protected Long numberreplications;
    @XmlElement(name = "RANDOMSTREAMS")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-05T02:11:24+02:00", comments = "JAXB RI v2.3.0")
    protected RANDOMSTREAMS randomstreams;
    @XmlAttribute(name = "base", namespace = "http://www.w3.org/XML/1998/namespace")
    @XmlSchemaType(name = "anyURI")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-05T02:11:24+02:00", comments = "JAXB RI v2.3.0")
    protected String base;

    /**
     * Gets the value of the starttime property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-05T02:11:24+02:00", comments = "JAXB RI v2.3.0")
    public Time getSTARTTIME() {
        return starttime;
    }

    /**
     * Sets the value of the starttime property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-05T02:11:24+02:00", comments = "JAXB RI v2.3.0")
    public void setSTARTTIME(Time value) {
        this.starttime = value;
    }

    /**
     * Gets the value of the warmupperiod property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-05T02:11:24+02:00", comments = "JAXB RI v2.3.0")
    public Duration getWARMUPPERIOD() {
        return warmupperiod;
    }

    /**
     * Sets the value of the warmupperiod property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-05T02:11:24+02:00", comments = "JAXB RI v2.3.0")
    public void setWARMUPPERIOD(Duration value) {
        this.warmupperiod = value;
    }

    /**
     * Gets the value of the runlength property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-05T02:11:24+02:00", comments = "JAXB RI v2.3.0")
    public Duration getRUNLENGTH() {
        return runlength;
    }

    /**
     * Sets the value of the runlength property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-05T02:11:24+02:00", comments = "JAXB RI v2.3.0")
    public void setRUNLENGTH(Duration value) {
        this.runlength = value;
    }

    /**
     * Gets the value of the numberreplications property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-05T02:11:24+02:00", comments = "JAXB RI v2.3.0")
    public Long getNUMBERREPLICATIONS() {
        return numberreplications;
    }

    /**
     * Sets the value of the numberreplications property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-05T02:11:24+02:00", comments = "JAXB RI v2.3.0")
    public void setNUMBERREPLICATIONS(Long value) {
        this.numberreplications = value;
    }

    /**
     * Gets the value of the randomstreams property.
     * 
     * @return
     *     possible object is
     *     {@link RANDOMSTREAMS }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-05T02:11:24+02:00", comments = "JAXB RI v2.3.0")
    public RANDOMSTREAMS getRANDOMSTREAMS() {
        return randomstreams;
    }

    /**
     * Sets the value of the randomstreams property.
     * 
     * @param value
     *     allowed object is
     *     {@link RANDOMSTREAMS }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-05T02:11:24+02:00", comments = "JAXB RI v2.3.0")
    public void setRANDOMSTREAMS(RANDOMSTREAMS value) {
        this.randomstreams = value;
    }

    /**
     * Gets the value of the base property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-05T02:11:24+02:00", comments = "JAXB RI v2.3.0")
    public String getBase() {
        return base;
    }

    /**
     * Sets the value of the base property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-05T02:11:24+02:00", comments = "JAXB RI v2.3.0")
    public void setBase(String value) {
        this.base = value;
    }

}