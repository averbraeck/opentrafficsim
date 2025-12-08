
package org.opentrafficsim.xml.generated;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.opentrafficsim.xml.bindings.TimeAdapter;
import org.opentrafficsim.xml.bindings.types.TimeType;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * <p>Java class for GlobalTimeType complex type</p>.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.</p>
 * 
 * <pre>{@code
 * <complexType name="GlobalTimeType">
 *   <complexContent>
 *     <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       <sequence>
 *         <element name="Time" maxOccurs="unbounded" minOccurs="2">
 *           <complexType>
 *             <complexContent>
 *               <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 <attribute name="Value" use="required" type="{http://www.opentrafficsim.org/ots}TimeType" />
 *               </restriction>
 *             </complexContent>
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
@XmlType(name = "GlobalTimeType", propOrder = {
    "time"
})
@SuppressWarnings("all") public class GlobalTimeType
    implements Serializable
{

    private static final long serialVersionUID = 10102L;
    @XmlElement(name = "Time", required = true)
    protected List<GlobalTimeType.Time> time;

    /**
     * Gets the value of the time property.
     * 
     * <p>This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the time property.</p>
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * </p>
     * <pre>
     * getTime().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link GlobalTimeType.Time }
     * </p>
     * 
     * 
     * @return
     *     The value of the time property.
     */
    public List<GlobalTimeType.Time> getTime() {
        if (time == null) {
            time = new ArrayList<>();
        }
        return this.time;
    }


    /**
     * <p>Java class for anonymous complex type</p>.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.</p>
     * 
     * <pre>{@code
     * <complexType>
     *   <complexContent>
     *     <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       <attribute name="Value" use="required" type="{http://www.opentrafficsim.org/ots}TimeType" />
     *     </restriction>
     *   </complexContent>
     * </complexType>
     * }</pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class Time
        implements Serializable
    {

        private static final long serialVersionUID = 10102L;
        @XmlAttribute(name = "Value", required = true)
        @XmlJavaTypeAdapter(TimeAdapter.class)
        protected TimeType value;

        /**
         * Gets the value of the value property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public TimeType getValue() {
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
        public void setValue(TimeType value) {
            this.value = value;
        }

    }

}
