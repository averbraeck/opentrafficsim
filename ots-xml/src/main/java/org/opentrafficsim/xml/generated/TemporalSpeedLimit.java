
package org.opentrafficsim.xml.generated;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.opentrafficsim.xml.bindings.PositiveDurationAdapter;
import org.opentrafficsim.xml.bindings.types.DurationType;


/**
 * <p>Java class for TemporalSpeedLimit complex type</p>.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.</p>
 * 
 * <pre>{@code
 * <complexType name="TemporalSpeedLimit">
 *   <complexContent>
 *     <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       <sequence>
 *         <element name="SpeedLimit" maxOccurs="unbounded">
 *           <complexType>
 *             <simpleContent>
 *               <extension base="<http://www.opentrafficsim.org/ots>SpeedLimit">
 *                 <attribute name="StartTimeOfDay" use="required" type="{http://www.opentrafficsim.org/ots}PositiveDurationType" />
 *               </extension>
 *             </simpleContent>
 *           </complexType>
 *         </element>
 *       </sequence>
 *     </restriction>
 *   </complexContent>
 * </complexType>
 * }</pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TemporalSpeedLimit", propOrder = {
    "speedLimit"
})
@SuppressWarnings("all") public class TemporalSpeedLimit
    implements Serializable
{

    private static final long serialVersionUID = 10102L;
    @XmlElement(name = "SpeedLimit", required = true)
    protected List<TemporalSpeedLimit.SpeedLimit> speedLimit;

    /**
     * Gets the value of the speedLimit property.
     * 
     * <p>This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the speedLimit property.</p>
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * </p>
     * <pre>
     * getSpeedLimit().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link TemporalSpeedLimit.SpeedLimit }
     * </p>
     * 
     * 
     * @return
     *     The value of the speedLimit property.
     */
    public List<TemporalSpeedLimit.SpeedLimit> getSpeedLimit() {
        if (speedLimit == null) {
            speedLimit = new ArrayList<>();
        }
        return this.speedLimit;
    }


    /**
     * <p>Java class for anonymous complex type</p>.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.</p>
     * 
     * <pre>{@code
     * <complexType>
     *   <simpleContent>
     *     <extension base="<http://www.opentrafficsim.org/ots>SpeedLimit">
     *       <attribute name="StartTimeOfDay" use="required" type="{http://www.opentrafficsim.org/ots}PositiveDurationType" />
     *     </extension>
     *   </simpleContent>
     * </complexType>
     * }</pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class SpeedLimit
        extends org.opentrafficsim.xml.generated.SpeedLimit
        implements Serializable
    {

        private static final long serialVersionUID = 10102L;
        @XmlAttribute(name = "StartTimeOfDay", required = true)
        @XmlJavaTypeAdapter(PositiveDurationAdapter.class)
        protected DurationType startTimeOfDay;

        /**
         * Gets the value of the startTimeOfDay property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public DurationType getStartTimeOfDay() {
            return startTimeOfDay;
        }

        /**
         * Sets the value of the startTimeOfDay property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setStartTimeOfDay(DurationType value) {
            this.startTimeOfDay = value;
        }

    }

}
