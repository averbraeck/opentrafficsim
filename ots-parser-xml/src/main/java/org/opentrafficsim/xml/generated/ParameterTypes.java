//
// Diese Datei wurde mit der Eclipse Implementation of JAXB, v2.3.7 generiert 
// Siehe https://eclipse-ee4j.github.io/jaxb-ri 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2025.07.24 um 01:18:43 PM CEST 
//


package org.opentrafficsim.xml.generated;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Generated;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


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
 *         &lt;choice maxOccurs="unbounded" minOccurs="0"&gt;
 *           &lt;element name="Duration" type="{http://www.opentrafficsim.org/ots}ParameterTypeDuration" maxOccurs="unbounded" minOccurs="0"/&gt;
 *           &lt;element name="Length" type="{http://www.opentrafficsim.org/ots}ParameterTypeLength" maxOccurs="unbounded" minOccurs="0"/&gt;
 *           &lt;element name="Speed" type="{http://www.opentrafficsim.org/ots}ParameterTypeSpeed" maxOccurs="unbounded" minOccurs="0"/&gt;
 *           &lt;element name="Acceleration" type="{http://www.opentrafficsim.org/ots}ParameterTypeAcceleration" maxOccurs="unbounded" minOccurs="0"/&gt;
 *           &lt;element name="LinearDensity" type="{http://www.opentrafficsim.org/ots}ParameterTypeLinearDensity" maxOccurs="unbounded" minOccurs="0"/&gt;
 *           &lt;element name="Frequency" type="{http://www.opentrafficsim.org/ots}ParameterTypeFrequency" maxOccurs="unbounded" minOccurs="0"/&gt;
 *           &lt;element name="Double" type="{http://www.opentrafficsim.org/ots}ParameterTypeDouble" maxOccurs="unbounded" minOccurs="0"/&gt;
 *           &lt;element name="Fraction" type="{http://www.opentrafficsim.org/ots}ParameterTypeFraction" maxOccurs="unbounded" minOccurs="0"/&gt;
 *           &lt;element name="Integer" type="{http://www.opentrafficsim.org/ots}ParameterTypeInteger" maxOccurs="unbounded" minOccurs="0"/&gt;
 *           &lt;element name="Boolean" type="{http://www.opentrafficsim.org/ots}ParameterTypeBoolean" maxOccurs="unbounded" minOccurs="0"/&gt;
 *           &lt;element name="String" type="{http://www.opentrafficsim.org/ots}ParameterTypeString" maxOccurs="unbounded" minOccurs="0"/&gt;
 *           &lt;element name="Class" type="{http://www.opentrafficsim.org/ots}ParameterTypeClass" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;/choice&gt;
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
    "durationOrLengthOrSpeed"
})
@XmlRootElement(name = "ParameterTypes")
@Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
public class ParameterTypes implements Serializable
{

    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    private final static long serialVersionUID = 10102L;
    @XmlElements({
        @XmlElement(name = "Duration", type = ParameterTypeDuration.class),
        @XmlElement(name = "Length", type = ParameterTypeLength.class),
        @XmlElement(name = "Speed", type = ParameterTypeSpeed.class),
        @XmlElement(name = "Acceleration", type = ParameterTypeAcceleration.class),
        @XmlElement(name = "LinearDensity", type = ParameterTypeLinearDensity.class),
        @XmlElement(name = "Frequency", type = ParameterTypeFrequency.class),
        @XmlElement(name = "Double", type = ParameterTypeDouble.class),
        @XmlElement(name = "Fraction", type = ParameterTypeFraction.class),
        @XmlElement(name = "Integer", type = ParameterTypeInteger.class),
        @XmlElement(name = "Boolean", type = ParameterTypeBoolean.class),
        @XmlElement(name = "String", type = ParameterTypeString.class),
        @XmlElement(name = "Class", type = ParameterTypeClass.class)
    })
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    protected List<ParameterType> durationOrLengthOrSpeed;

    /**
     * Gets the value of the durationOrLengthOrSpeed property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the durationOrLengthOrSpeed property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getDurationOrLengthOrSpeed().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ParameterTypeAcceleration }
     * {@link ParameterTypeBoolean }
     * {@link ParameterTypeClass }
     * {@link ParameterTypeDouble }
     * {@link ParameterTypeDuration }
     * {@link ParameterTypeFraction }
     * {@link ParameterTypeFrequency }
     * {@link ParameterTypeInteger }
     * {@link ParameterTypeLength }
     * {@link ParameterTypeLinearDensity }
     * {@link ParameterTypeSpeed }
     * {@link ParameterTypeString }
     * 
     * 
     */
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    public List<ParameterType> getDurationOrLengthOrSpeed() {
        if (durationOrLengthOrSpeed == null) {
            durationOrLengthOrSpeed = new ArrayList<ParameterType>();
        }
        return this.durationOrLengthOrSpeed;
    }

}
