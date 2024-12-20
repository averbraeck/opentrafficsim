
package org.opentrafficsim.xml.generated;

import java.io.Serializable;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.opentrafficsim.xml.bindings.PositiveDurationAdapter;
import org.opentrafficsim.xml.bindings.PositiveIntegerAdapter;
import org.opentrafficsim.xml.bindings.PositiveTimeAdapter;
import org.opentrafficsim.xml.bindings.types.DurationType;
import org.opentrafficsim.xml.bindings.types.IntegerType;
import org.opentrafficsim.xml.bindings.types.TimeType;


/**
 * <p>Java class for anonymous complex type</p>.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.</p>
 * 
 * <pre>{@code
 * <complexType>
 *   <complexContent>
 *     <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       <sequence>
 *         <element name="StartTime" type="{http://www.opentrafficsim.org/ots}PositiveTimeType" minOccurs="0"/>
 *         <element name="WarmupPeriod" type="{http://www.opentrafficsim.org/ots}PositiveDurationType" minOccurs="0"/>
 *         <element name="RunLength" type="{http://www.opentrafficsim.org/ots}PositiveDurationType"/>
 *         <element name="History" type="{http://www.opentrafficsim.org/ots}PositiveDurationType" minOccurs="0"/>
 *         <element name="NumberReplications" type="{http://www.opentrafficsim.org/ots}positiveInteger" minOccurs="0"/>
 *         <element ref="{http://www.opentrafficsim.org/ots}RandomStreams" minOccurs="0"/>
 *       </sequence>
 *     </restriction>
 *   </complexContent>
 * </complexType>
 * }</pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "startTime",
    "warmupPeriod",
    "runLength",
    "history",
    "numberReplications",
    "randomStreams"
})
@XmlRootElement(name = "Run")
@SuppressWarnings("all") public class Run
    implements Serializable
{

    private static final long serialVersionUID = 10102L;
    @XmlElement(name = "StartTime", type = String.class, defaultValue = "0s")
    @XmlJavaTypeAdapter(PositiveTimeAdapter.class)
    protected TimeType startTime;
    @XmlElement(name = "WarmupPeriod", type = String.class, defaultValue = "0s")
    @XmlJavaTypeAdapter(PositiveDurationAdapter.class)
    protected DurationType warmupPeriod;
    @XmlElement(name = "RunLength", required = true, type = String.class, defaultValue = "1h")
    @XmlJavaTypeAdapter(PositiveDurationAdapter.class)
    protected DurationType runLength;
    @XmlElement(name = "History", type = String.class, defaultValue = "0s")
    @XmlJavaTypeAdapter(PositiveDurationAdapter.class)
    protected DurationType history;
    @XmlElement(name = "NumberReplications", type = String.class, defaultValue = "1")
    @XmlJavaTypeAdapter(PositiveIntegerAdapter.class)
    protected IntegerType numberReplications;
    @XmlElement(name = "RandomStreams")
    protected RandomStreams randomStreams;

    /**
     * Gets the value of the startTime property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public TimeType getStartTime() {
        return startTime;
    }

    /**
     * Sets the value of the startTime property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setStartTime(TimeType value) {
        this.startTime = value;
    }

    /**
     * Gets the value of the warmupPeriod property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public DurationType getWarmupPeriod() {
        return warmupPeriod;
    }

    /**
     * Sets the value of the warmupPeriod property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setWarmupPeriod(DurationType value) {
        this.warmupPeriod = value;
    }

    /**
     * Gets the value of the runLength property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public DurationType getRunLength() {
        return runLength;
    }

    /**
     * Sets the value of the runLength property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRunLength(DurationType value) {
        this.runLength = value;
    }

    /**
     * Gets the value of the history property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public DurationType getHistory() {
        return history;
    }

    /**
     * Sets the value of the history property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setHistory(DurationType value) {
        this.history = value;
    }

    /**
     * Gets the value of the numberReplications property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public IntegerType getNumberReplications() {
        return numberReplications;
    }

    /**
     * Sets the value of the numberReplications property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNumberReplications(IntegerType value) {
        this.numberReplications = value;
    }

    /**
     * Gets the value of the randomStreams property.
     * 
     * @return
     *     possible object is
     *     {@link RandomStreams }
     *     
     */
    public RandomStreams getRandomStreams() {
        return randomStreams;
    }

    /**
     * Sets the value of the randomStreams property.
     * 
     * @param value
     *     allowed object is
     *     {@link RandomStreams }
     *     
     */
    public void setRandomStreams(RandomStreams value) {
        this.randomStreams = value;
    }

}
