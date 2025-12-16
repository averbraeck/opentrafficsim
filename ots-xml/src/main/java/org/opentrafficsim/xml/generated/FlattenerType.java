
package org.opentrafficsim.xml.generated;

import java.io.Serializable;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.opentrafficsim.xml.bindings.AngleAdapter;
import org.opentrafficsim.xml.bindings.LengthAdapter;
import org.opentrafficsim.xml.bindings.PositiveIntegerAdapter;
import org.opentrafficsim.xml.bindings.types.AngleType;
import org.opentrafficsim.xml.bindings.types.IntegerType;
import org.opentrafficsim.xml.bindings.types.LengthType;


/**
 * <p>Java class for FlattenerType complex type</p>.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.</p>
 * 
 * <pre>{@code
 * <complexType name="FlattenerType">
 *   <complexContent>
 *     <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       <choice>
 *         <element name="NumSegments" type="{http://www.opentrafficsim.org/ots}positiveInteger"/>
 *         <element name="DeviationAndAngle">
 *           <complexType>
 *             <complexContent>
 *               <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 <sequence>
 *                   <element name="MaxDeviation" type="{http://www.opentrafficsim.org/ots}LengthType" minOccurs="0"/>
 *                   <element name="MaxAngle" type="{http://www.opentrafficsim.org/ots}AngleType" minOccurs="0"/>
 *                 </sequence>
 *               </restriction>
 *             </complexContent>
 *           </complexType>
 *         </element>
 *       </choice>
 *     </restriction>
 *   </complexContent>
 * </complexType>
 * }</pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "FlattenerType", propOrder = {
    "numSegments",
    "deviationAndAngle"
})
@SuppressWarnings("all") public class FlattenerType
    implements Serializable
{

    private static final long serialVersionUID = 10102L;
    @XmlElement(name = "NumSegments", type = String.class, defaultValue = "64")
    @XmlJavaTypeAdapter(PositiveIntegerAdapter.class)
    protected IntegerType numSegments;
    @XmlElement(name = "DeviationAndAngle")
    protected FlattenerType.DeviationAndAngle deviationAndAngle;

    /**
     * Gets the value of the numSegments property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public IntegerType getNumSegments() {
        return numSegments;
    }

    /**
     * Sets the value of the numSegments property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNumSegments(IntegerType value) {
        this.numSegments = value;
    }

    /**
     * Gets the value of the deviationAndAngle property.
     * 
     * @return
     *     possible object is
     *     {@link FlattenerType.DeviationAndAngle }
     *     
     */
    public FlattenerType.DeviationAndAngle getDeviationAndAngle() {
        return deviationAndAngle;
    }

    /**
     * Sets the value of the deviationAndAngle property.
     * 
     * @param value
     *     allowed object is
     *     {@link FlattenerType.DeviationAndAngle }
     *     
     */
    public void setDeviationAndAngle(FlattenerType.DeviationAndAngle value) {
        this.deviationAndAngle = value;
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
     *       <sequence>
     *         <element name="MaxDeviation" type="{http://www.opentrafficsim.org/ots}LengthType" minOccurs="0"/>
     *         <element name="MaxAngle" type="{http://www.opentrafficsim.org/ots}AngleType" minOccurs="0"/>
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
        "maxDeviation",
        "maxAngle"
    })
    public static class DeviationAndAngle
        implements Serializable
    {

        private static final long serialVersionUID = 10102L;
        @XmlElement(name = "MaxDeviation", type = String.class, defaultValue = "0.05m")
        @XmlJavaTypeAdapter(LengthAdapter.class)
        protected LengthType maxDeviation;
        @XmlElement(name = "MaxAngle", type = String.class, defaultValue = "1.0deg")
        @XmlJavaTypeAdapter(AngleAdapter.class)
        protected AngleType maxAngle;

        /**
         * Gets the value of the maxDeviation property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public LengthType getMaxDeviation() {
            return maxDeviation;
        }

        /**
         * Sets the value of the maxDeviation property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setMaxDeviation(LengthType value) {
            this.maxDeviation = value;
        }

        /**
         * Gets the value of the maxAngle property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public AngleType getMaxAngle() {
            return maxAngle;
        }

        /**
         * Sets the value of the maxAngle property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setMaxAngle(AngleType value) {
            this.maxAngle = value;
        }

    }

}
