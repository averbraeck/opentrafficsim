
package org.opentrafficsim.xml.generated;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElements;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;


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
 *         <choice maxOccurs="unbounded" minOccurs="0">
 *           <element name="Duration" type="{http://www.opentrafficsim.org/ots}ParameterTypeDuration" maxOccurs="unbounded" minOccurs="0"/>
 *           <element name="Length" type="{http://www.opentrafficsim.org/ots}ParameterTypeLength" maxOccurs="unbounded" minOccurs="0"/>
 *           <element name="Speed" type="{http://www.opentrafficsim.org/ots}ParameterTypeSpeed" maxOccurs="unbounded" minOccurs="0"/>
 *           <element name="Acceleration" type="{http://www.opentrafficsim.org/ots}ParameterTypeAcceleration" maxOccurs="unbounded" minOccurs="0"/>
 *           <element name="LinearDensity" type="{http://www.opentrafficsim.org/ots}ParameterTypeLinearDensity" maxOccurs="unbounded" minOccurs="0"/>
 *           <element name="Frequency" type="{http://www.opentrafficsim.org/ots}ParameterTypeFrequency" maxOccurs="unbounded" minOccurs="0"/>
 *           <element name="Double" type="{http://www.opentrafficsim.org/ots}ParameterTypeDouble" maxOccurs="unbounded" minOccurs="0"/>
 *           <element name="Fraction" type="{http://www.opentrafficsim.org/ots}ParameterTypeFraction" maxOccurs="unbounded" minOccurs="0"/>
 *           <element name="Integer" type="{http://www.opentrafficsim.org/ots}ParameterTypeInteger" maxOccurs="unbounded" minOccurs="0"/>
 *           <element name="Boolean" type="{http://www.opentrafficsim.org/ots}ParameterTypeBoolean" maxOccurs="unbounded" minOccurs="0"/>
 *           <element name="String" type="{http://www.opentrafficsim.org/ots}ParameterTypeString" maxOccurs="unbounded" minOccurs="0"/>
 *           <element name="Class" type="{http://www.opentrafficsim.org/ots}ParameterTypeClass" maxOccurs="unbounded" minOccurs="0"/>
 *         </choice>
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
    "durationOrLengthOrSpeed"
})
@XmlRootElement(name = "ParameterTypes")
@SuppressWarnings("all") public class ParameterTypes implements Serializable
{

    private static final long serialVersionUID = 10102L;
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
    protected List<ParameterType> durationOrLengthOrSpeed;

    /**
     * Gets the value of the durationOrLengthOrSpeed property.
     * 
     * <p>This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the durationOrLengthOrSpeed property.</p>
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * </p>
     * <pre>
     * getDurationOrLengthOrSpeed().add(newItem);
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
     * </p>
     * 
     * 
     * @return
     *     The value of the durationOrLengthOrSpeed property.
     */
    public List<ParameterType> getDurationOrLengthOrSpeed() {
        if (durationOrLengthOrSpeed == null) {
            durationOrLengthOrSpeed = new ArrayList<>();
        }
        return this.durationOrLengthOrSpeed;
    }

}
