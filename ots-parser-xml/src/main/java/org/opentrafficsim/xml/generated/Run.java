//
// Diese Datei wurde mit der Eclipse Implementation of JAXB, v2.3.7 generiert 
// Siehe https://eclipse-ee4j.github.io/jaxb-ri 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2025.07.24 um 01:18:43 PM CEST 
//


package org.opentrafficsim.xml.generated;

import java.io.Serializable;
import javax.annotation.Generated;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.opentrafficsim.xml.bindings.PositiveDurationAdapter;
import org.opentrafficsim.xml.bindings.PositiveIntegerAdapter;
import org.opentrafficsim.xml.bindings.PositiveTimeAdapter;
import org.opentrafficsim.xml.bindings.types.DurationType;
import org.opentrafficsim.xml.bindings.types.IntegerType;
import org.opentrafficsim.xml.bindings.types.TimeType;


/**
 * <p>Java-Klasse für anonymous complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="StartTime" type="{http://www.opentrafficsim.org/ots}PositiveTimeType" minOccurs="0"/&gt;
 *         &lt;element name="WarmupPeriod" type="{http://www.opentrafficsim.org/ots}PositiveDurationType" minOccurs="0"/&gt;
 *         &lt;element name="RunLength" type="{http://www.opentrafficsim.org/ots}PositiveDurationType"/&gt;
 *         &lt;element name="NumberReplications" type="{http://www.opentrafficsim.org/ots}positiveInteger" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.opentrafficsim.org/ots}RandomStreams" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "startTime",
    "warmupPeriod",
    "runLength",
    "numberReplications",
    "randomStreams"
})
@XmlRootElement(name = "Run")
@Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
public class Run
    implements Serializable
{

    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    private final static long serialVersionUID = 10102L;
    @XmlElement(name = "StartTime", type = String.class, defaultValue = "0s")
    @XmlJavaTypeAdapter(PositiveTimeAdapter.class)
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    protected TimeType startTime;
    @XmlElement(name = "WarmupPeriod", type = String.class, defaultValue = "0s")
    @XmlJavaTypeAdapter(PositiveDurationAdapter.class)
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    protected DurationType warmupPeriod;
    @XmlElement(name = "RunLength", required = true, type = String.class, defaultValue = "1h")
    @XmlJavaTypeAdapter(PositiveDurationAdapter.class)
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    protected DurationType runLength;
    @XmlElement(name = "NumberReplications", type = String.class, defaultValue = "1")
    @XmlJavaTypeAdapter(PositiveIntegerAdapter.class)
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    protected IntegerType numberReplications;
    @XmlElement(name = "RandomStreams")
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    protected RandomStreams randomStreams;

    /**
     * Ruft den Wert der startTime-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    public TimeType getStartTime() {
        return startTime;
    }

    /**
     * Legt den Wert der startTime-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    public void setStartTime(TimeType value) {
        this.startTime = value;
    }

    /**
     * Ruft den Wert der warmupPeriod-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    public DurationType getWarmupPeriod() {
        return warmupPeriod;
    }

    /**
     * Legt den Wert der warmupPeriod-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    public void setWarmupPeriod(DurationType value) {
        this.warmupPeriod = value;
    }

    /**
     * Ruft den Wert der runLength-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    public DurationType getRunLength() {
        return runLength;
    }

    /**
     * Legt den Wert der runLength-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    public void setRunLength(DurationType value) {
        this.runLength = value;
    }

    /**
     * Ruft den Wert der numberReplications-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    public IntegerType getNumberReplications() {
        return numberReplications;
    }

    /**
     * Legt den Wert der numberReplications-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    public void setNumberReplications(IntegerType value) {
        this.numberReplications = value;
    }

    /**
     * Ruft den Wert der randomStreams-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link RandomStreams }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    public RandomStreams getRandomStreams() {
        return randomStreams;
    }

    /**
     * Legt den Wert der randomStreams-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link RandomStreams }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    public void setRandomStreams(RandomStreams value) {
        this.randomStreams = value;
    }

}
