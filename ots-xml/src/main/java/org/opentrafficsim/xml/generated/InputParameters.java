
package org.opentrafficsim.xml.generated;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElements;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.XmlValue;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.opentrafficsim.xml.bindings.AccelerationAdapter;
import org.opentrafficsim.xml.bindings.BooleanAdapter;
import org.opentrafficsim.xml.bindings.ClassAdapter;
import org.opentrafficsim.xml.bindings.DoubleAdapter;
import org.opentrafficsim.xml.bindings.DurationAdapter;
import org.opentrafficsim.xml.bindings.FractionAdapter;
import org.opentrafficsim.xml.bindings.FrequencyAdapter;
import org.opentrafficsim.xml.bindings.LengthAdapter;
import org.opentrafficsim.xml.bindings.LinearDensityAdapter;
import org.opentrafficsim.xml.bindings.LongAdapter;
import org.opentrafficsim.xml.bindings.SpeedAdapter;
import org.opentrafficsim.xml.bindings.StringAdapter;
import org.opentrafficsim.xml.bindings.types.AccelerationType;
import org.opentrafficsim.xml.bindings.types.BooleanType;
import org.opentrafficsim.xml.bindings.types.ClassType;
import org.opentrafficsim.xml.bindings.types.DurationType;
import org.opentrafficsim.xml.bindings.types.FrequencyType;
import org.opentrafficsim.xml.bindings.types.LengthType;
import org.opentrafficsim.xml.bindings.types.LinearDensityType;
import org.opentrafficsim.xml.bindings.types.LongType;
import org.opentrafficsim.xml.bindings.types.SpeedType;
import org.opentrafficsim.xml.bindings.types.StringType;


/**
 * <p>Java class for InputParameters complex type</p>.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.</p>
 * 
 * <pre>{@code
 * <complexType name="InputParameters">
 *   <complexContent>
 *     <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       <choice maxOccurs="unbounded">
 *         <element name="Duration">
 *           <complexType>
 *             <simpleContent>
 *               <extension base="<http://www.opentrafficsim.org/ots>DurationType">
 *                 <attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}InputParameterIdType" />
 *               </extension>
 *             </simpleContent>
 *           </complexType>
 *         </element>
 *         <element name="Length">
 *           <complexType>
 *             <simpleContent>
 *               <extension base="<http://www.opentrafficsim.org/ots>LengthType">
 *                 <attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}InputParameterIdType" />
 *               </extension>
 *             </simpleContent>
 *           </complexType>
 *         </element>
 *         <element name="Speed">
 *           <complexType>
 *             <simpleContent>
 *               <extension base="<http://www.opentrafficsim.org/ots>SpeedType">
 *                 <attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}InputParameterIdType" />
 *               </extension>
 *             </simpleContent>
 *           </complexType>
 *         </element>
 *         <element name="Acceleration">
 *           <complexType>
 *             <simpleContent>
 *               <extension base="<http://www.opentrafficsim.org/ots>AccelerationType">
 *                 <attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}InputParameterIdType" />
 *               </extension>
 *             </simpleContent>
 *           </complexType>
 *         </element>
 *         <element name="LinearDensity">
 *           <complexType>
 *             <simpleContent>
 *               <extension base="<http://www.opentrafficsim.org/ots>LinearDensityType">
 *                 <attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}InputParameterIdType" />
 *               </extension>
 *             </simpleContent>
 *           </complexType>
 *         </element>
 *         <element name="Frequency">
 *           <complexType>
 *             <simpleContent>
 *               <extension base="<http://www.opentrafficsim.org/ots>FrequencyType">
 *                 <attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}InputParameterIdType" />
 *               </extension>
 *             </simpleContent>
 *           </complexType>
 *         </element>
 *         <element name="Double">
 *           <complexType>
 *             <simpleContent>
 *               <extension base="<http://www.opentrafficsim.org/ots>double">
 *                 <attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}InputParameterIdType" />
 *               </extension>
 *             </simpleContent>
 *           </complexType>
 *         </element>
 *         <element name="Fraction">
 *           <complexType>
 *             <simpleContent>
 *               <extension base="<http://www.opentrafficsim.org/ots>FractionType">
 *                 <attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}InputParameterIdType" />
 *               </extension>
 *             </simpleContent>
 *           </complexType>
 *         </element>
 *         <element name="Integer">
 *           <complexType>
 *             <simpleContent>
 *               <extension base="<http://www.opentrafficsim.org/ots>integer">
 *                 <attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}InputParameterIdType" />
 *               </extension>
 *             </simpleContent>
 *           </complexType>
 *         </element>
 *         <element name="Boolean">
 *           <complexType>
 *             <simpleContent>
 *               <extension base="<http://www.opentrafficsim.org/ots>boolean">
 *                 <attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}InputParameterIdType" />
 *               </extension>
 *             </simpleContent>
 *           </complexType>
 *         </element>
 *         <element name="String">
 *           <complexType>
 *             <simpleContent>
 *               <extension base="<http://www.opentrafficsim.org/ots>string">
 *                 <attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}InputParameterIdType" />
 *               </extension>
 *             </simpleContent>
 *           </complexType>
 *         </element>
 *         <element name="Class">
 *           <complexType>
 *             <simpleContent>
 *               <extension base="<http://www.opentrafficsim.org/ots>ClassNameType">
 *                 <attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}InputParameterIdType" />
 *               </extension>
 *             </simpleContent>
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
@XmlType(name = "InputParameters", propOrder = {
    "durationOrLengthOrSpeed"
})
@SuppressWarnings("all") public class InputParameters
    implements Serializable
{

    private static final long serialVersionUID = 10102L;
    @XmlElements({
        @XmlElement(name = "Duration", type = InputParameters.Duration.class),
        @XmlElement(name = "Length", type = InputParameters.Length.class),
        @XmlElement(name = "Speed", type = InputParameters.Speed.class),
        @XmlElement(name = "Acceleration", type = InputParameters.Acceleration.class),
        @XmlElement(name = "LinearDensity", type = InputParameters.LinearDensity.class),
        @XmlElement(name = "Frequency", type = InputParameters.Frequency.class),
        @XmlElement(name = "Double", type = InputParameters.Double.class),
        @XmlElement(name = "Fraction", type = InputParameters.Fraction.class),
        @XmlElement(name = "Integer", type = InputParameters.Integer.class),
        @XmlElement(name = "Boolean", type = InputParameters.Boolean.class),
        @XmlElement(name = "String", type = InputParameters.String.class),
        @XmlElement(name = "Class", type = InputParameters.Class.class)
    })
    protected List<Serializable> durationOrLengthOrSpeed;

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
     * {@link InputParameters.Acceleration }
     * {@link InputParameters.Boolean }
     * {@link InputParameters.Class }
     * {@link InputParameters.Double }
     * {@link InputParameters.Duration }
     * {@link InputParameters.Fraction }
     * {@link InputParameters.Frequency }
     * {@link InputParameters.Integer }
     * {@link InputParameters.Length }
     * {@link InputParameters.LinearDensity }
     * {@link InputParameters.Speed }
     * {@link InputParameters.String }
     * </p>
     * 
     * 
     * @return
     *     The value of the durationOrLengthOrSpeed property.
     */
    public List<Serializable> getDurationOrLengthOrSpeed() {
        if (durationOrLengthOrSpeed == null) {
            durationOrLengthOrSpeed = new ArrayList<>();
        }
        return this.durationOrLengthOrSpeed;
    }


    /**
     * <p>Java class for anonymous complex type</p>.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.</p>
     * 
     * <pre>{@code
     * <complexType>
     *   <simpleContent>
     *     <extension base="<http://www.opentrafficsim.org/ots>AccelerationType">
     *       <attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}InputParameterIdType" />
     *     </extension>
     *   </simpleContent>
     * </complexType>
     * }</pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "value"
    })
    public static class Acceleration implements Serializable
    {

        private static final long serialVersionUID = 10102L;
        @XmlValue
        @XmlJavaTypeAdapter(AccelerationAdapter.class)
        protected AccelerationType value;
        @XmlAttribute(name = "Id", required = true)
        protected java.lang.String id;

        /**
         * Gets the value of the value property.
         * 
         * @return
         *     possible object is
         *     {@link java.lang.String }
         *     
         */
        public AccelerationType getValue() {
            return value;
        }

        /**
         * Sets the value of the value property.
         * 
         * @param value
         *     allowed object is
         *     {@link java.lang.String }
         *     
         */
        public void setValue(AccelerationType value) {
            this.value = value;
        }

        /**
         * Gets the value of the id property.
         * 
         * @return
         *     possible object is
         *     {@link java.lang.String }
         *     
         */
        public java.lang.String getId() {
            return id;
        }

        /**
         * Sets the value of the id property.
         * 
         * @param value
         *     allowed object is
         *     {@link java.lang.String }
         *     
         */
        public void setId(java.lang.String value) {
            this.id = value;
        }

    }


    /**
     * <p>Java class for anonymous complex type</p>.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.</p>
     * 
     * <pre>{@code
     * <complexType>
     *   <simpleContent>
     *     <extension base="<http://www.opentrafficsim.org/ots>boolean">
     *       <attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}InputParameterIdType" />
     *     </extension>
     *   </simpleContent>
     * </complexType>
     * }</pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "value"
    })
    public static class Boolean implements Serializable
    {

        private static final long serialVersionUID = 10102L;
        @XmlValue
        @XmlJavaTypeAdapter(BooleanAdapter.class)
        protected BooleanType value;
        @XmlAttribute(name = "Id", required = true)
        protected java.lang.String id;

        /**
         * Gets the value of the value property.
         * 
         * @return
         *     possible object is
         *     {@link java.lang.String }
         *     
         */
        public BooleanType getValue() {
            return value;
        }

        /**
         * Sets the value of the value property.
         * 
         * @param value
         *     allowed object is
         *     {@link java.lang.String }
         *     
         */
        public void setValue(BooleanType value) {
            this.value = value;
        }

        /**
         * Gets the value of the id property.
         * 
         * @return
         *     possible object is
         *     {@link java.lang.String }
         *     
         */
        public java.lang.String getId() {
            return id;
        }

        /**
         * Sets the value of the id property.
         * 
         * @param value
         *     allowed object is
         *     {@link java.lang.String }
         *     
         */
        public void setId(java.lang.String value) {
            this.id = value;
        }

    }


    /**
     * <p>Java class for anonymous complex type</p>.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.</p>
     * 
     * <pre>{@code
     * <complexType>
     *   <simpleContent>
     *     <extension base="<http://www.opentrafficsim.org/ots>ClassNameType">
     *       <attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}InputParameterIdType" />
     *     </extension>
     *   </simpleContent>
     * </complexType>
     * }</pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "value"
    })
    public static class Class implements Serializable
    {

        private static final long serialVersionUID = 10102L;
        @XmlValue
        @XmlJavaTypeAdapter(ClassAdapter.class)
        protected ClassType value;
        @XmlAttribute(name = "Id", required = true)
        protected java.lang.String id;

        /**
         * Gets the value of the value property.
         * 
         * @return
         *     possible object is
         *     {@link java.lang.String }
         *     
         */
        public ClassType getValue() {
            return value;
        }

        /**
         * Sets the value of the value property.
         * 
         * @param value
         *     allowed object is
         *     {@link java.lang.String }
         *     
         */
        public void setValue(ClassType value) {
            this.value = value;
        }

        /**
         * Gets the value of the id property.
         * 
         * @return
         *     possible object is
         *     {@link java.lang.String }
         *     
         */
        public java.lang.String getId() {
            return id;
        }

        /**
         * Sets the value of the id property.
         * 
         * @param value
         *     allowed object is
         *     {@link java.lang.String }
         *     
         */
        public void setId(java.lang.String value) {
            this.id = value;
        }

    }


    /**
     * <p>Java class for anonymous complex type</p>.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.</p>
     * 
     * <pre>{@code
     * <complexType>
     *   <simpleContent>
     *     <extension base="<http://www.opentrafficsim.org/ots>double">
     *       <attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}InputParameterIdType" />
     *     </extension>
     *   </simpleContent>
     * </complexType>
     * }</pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "value"
    })
    public static class Double implements Serializable
    {

        private static final long serialVersionUID = 10102L;
        @XmlValue
        @XmlJavaTypeAdapter(DoubleAdapter.class)
        protected org.opentrafficsim.xml.bindings.types.DoubleType value;
        @XmlAttribute(name = "Id", required = true)
        protected java.lang.String id;

        /**
         * Gets the value of the value property.
         * 
         * @return
         *     possible object is
         *     {@link java.lang.String }
         *     
         */
        public org.opentrafficsim.xml.bindings.types.DoubleType getValue() {
            return value;
        }

        /**
         * Sets the value of the value property.
         * 
         * @param value
         *     allowed object is
         *     {@link java.lang.String }
         *     
         */
        public void setValue(org.opentrafficsim.xml.bindings.types.DoubleType value) {
            this.value = value;
        }

        /**
         * Gets the value of the id property.
         * 
         * @return
         *     possible object is
         *     {@link java.lang.String }
         *     
         */
        public java.lang.String getId() {
            return id;
        }

        /**
         * Sets the value of the id property.
         * 
         * @param value
         *     allowed object is
         *     {@link java.lang.String }
         *     
         */
        public void setId(java.lang.String value) {
            this.id = value;
        }

    }


    /**
     * <p>Java class for anonymous complex type</p>.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.</p>
     * 
     * <pre>{@code
     * <complexType>
     *   <simpleContent>
     *     <extension base="<http://www.opentrafficsim.org/ots>DurationType">
     *       <attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}InputParameterIdType" />
     *     </extension>
     *   </simpleContent>
     * </complexType>
     * }</pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "value"
    })
    public static class Duration implements Serializable
    {

        private static final long serialVersionUID = 10102L;
        @XmlValue
        @XmlJavaTypeAdapter(DurationAdapter.class)
        protected DurationType value;
        @XmlAttribute(name = "Id", required = true)
        protected java.lang.String id;

        /**
         * Gets the value of the value property.
         * 
         * @return
         *     possible object is
         *     {@link java.lang.String }
         *     
         */
        public DurationType getValue() {
            return value;
        }

        /**
         * Sets the value of the value property.
         * 
         * @param value
         *     allowed object is
         *     {@link java.lang.String }
         *     
         */
        public void setValue(DurationType value) {
            this.value = value;
        }

        /**
         * Gets the value of the id property.
         * 
         * @return
         *     possible object is
         *     {@link java.lang.String }
         *     
         */
        public java.lang.String getId() {
            return id;
        }

        /**
         * Sets the value of the id property.
         * 
         * @param value
         *     allowed object is
         *     {@link java.lang.String }
         *     
         */
        public void setId(java.lang.String value) {
            this.id = value;
        }

    }


    /**
     * <p>Java class for anonymous complex type</p>.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.</p>
     * 
     * <pre>{@code
     * <complexType>
     *   <simpleContent>
     *     <extension base="<http://www.opentrafficsim.org/ots>FractionType">
     *       <attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}InputParameterIdType" />
     *     </extension>
     *   </simpleContent>
     * </complexType>
     * }</pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "value"
    })
    public static class Fraction implements Serializable
    {

        private static final long serialVersionUID = 10102L;
        @XmlValue
        @XmlJavaTypeAdapter(FractionAdapter.class)
        protected org.opentrafficsim.xml.bindings.types.DoubleType value;
        @XmlAttribute(name = "Id", required = true)
        protected java.lang.String id;

        /**
         * Gets the value of the value property.
         * 
         * @return
         *     possible object is
         *     {@link java.lang.String }
         *     
         */
        public org.opentrafficsim.xml.bindings.types.DoubleType getValue() {
            return value;
        }

        /**
         * Sets the value of the value property.
         * 
         * @param value
         *     allowed object is
         *     {@link java.lang.String }
         *     
         */
        public void setValue(org.opentrafficsim.xml.bindings.types.DoubleType value) {
            this.value = value;
        }

        /**
         * Gets the value of the id property.
         * 
         * @return
         *     possible object is
         *     {@link java.lang.String }
         *     
         */
        public java.lang.String getId() {
            return id;
        }

        /**
         * Sets the value of the id property.
         * 
         * @param value
         *     allowed object is
         *     {@link java.lang.String }
         *     
         */
        public void setId(java.lang.String value) {
            this.id = value;
        }

    }


    /**
     * <p>Java class for anonymous complex type</p>.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.</p>
     * 
     * <pre>{@code
     * <complexType>
     *   <simpleContent>
     *     <extension base="<http://www.opentrafficsim.org/ots>FrequencyType">
     *       <attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}InputParameterIdType" />
     *     </extension>
     *   </simpleContent>
     * </complexType>
     * }</pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "value"
    })
    public static class Frequency implements Serializable
    {

        private static final long serialVersionUID = 10102L;
        @XmlValue
        @XmlJavaTypeAdapter(FrequencyAdapter.class)
        protected FrequencyType value;
        @XmlAttribute(name = "Id", required = true)
        protected java.lang.String id;

        /**
         * Gets the value of the value property.
         * 
         * @return
         *     possible object is
         *     {@link java.lang.String }
         *     
         */
        public FrequencyType getValue() {
            return value;
        }

        /**
         * Sets the value of the value property.
         * 
         * @param value
         *     allowed object is
         *     {@link java.lang.String }
         *     
         */
        public void setValue(FrequencyType value) {
            this.value = value;
        }

        /**
         * Gets the value of the id property.
         * 
         * @return
         *     possible object is
         *     {@link java.lang.String }
         *     
         */
        public java.lang.String getId() {
            return id;
        }

        /**
         * Sets the value of the id property.
         * 
         * @param value
         *     allowed object is
         *     {@link java.lang.String }
         *     
         */
        public void setId(java.lang.String value) {
            this.id = value;
        }

    }


    /**
     * <p>Java class for anonymous complex type</p>.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.</p>
     * 
     * <pre>{@code
     * <complexType>
     *   <simpleContent>
     *     <extension base="<http://www.opentrafficsim.org/ots>integer">
     *       <attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}InputParameterIdType" />
     *     </extension>
     *   </simpleContent>
     * </complexType>
     * }</pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "value"
    })
    public static class Integer implements Serializable
    {

        private static final long serialVersionUID = 10102L;
        @XmlValue
        @XmlJavaTypeAdapter(LongAdapter.class)
        protected LongType value;
        @XmlAttribute(name = "Id", required = true)
        protected java.lang.String id;

        /**
         * Gets the value of the value property.
         * 
         * @return
         *     possible object is
         *     {@link java.lang.String }
         *     
         */
        public LongType getValue() {
            return value;
        }

        /**
         * Sets the value of the value property.
         * 
         * @param value
         *     allowed object is
         *     {@link java.lang.String }
         *     
         */
        public void setValue(LongType value) {
            this.value = value;
        }

        /**
         * Gets the value of the id property.
         * 
         * @return
         *     possible object is
         *     {@link java.lang.String }
         *     
         */
        public java.lang.String getId() {
            return id;
        }

        /**
         * Sets the value of the id property.
         * 
         * @param value
         *     allowed object is
         *     {@link java.lang.String }
         *     
         */
        public void setId(java.lang.String value) {
            this.id = value;
        }

    }


    /**
     * <p>Java class for anonymous complex type</p>.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.</p>
     * 
     * <pre>{@code
     * <complexType>
     *   <simpleContent>
     *     <extension base="<http://www.opentrafficsim.org/ots>LengthType">
     *       <attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}InputParameterIdType" />
     *     </extension>
     *   </simpleContent>
     * </complexType>
     * }</pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "value"
    })
    public static class Length implements Serializable
    {

        private static final long serialVersionUID = 10102L;
        @XmlValue
        @XmlJavaTypeAdapter(LengthAdapter.class)
        protected LengthType value;
        @XmlAttribute(name = "Id", required = true)
        protected java.lang.String id;

        /**
         * Gets the value of the value property.
         * 
         * @return
         *     possible object is
         *     {@link java.lang.String }
         *     
         */
        public LengthType getValue() {
            return value;
        }

        /**
         * Sets the value of the value property.
         * 
         * @param value
         *     allowed object is
         *     {@link java.lang.String }
         *     
         */
        public void setValue(LengthType value) {
            this.value = value;
        }

        /**
         * Gets the value of the id property.
         * 
         * @return
         *     possible object is
         *     {@link java.lang.String }
         *     
         */
        public java.lang.String getId() {
            return id;
        }

        /**
         * Sets the value of the id property.
         * 
         * @param value
         *     allowed object is
         *     {@link java.lang.String }
         *     
         */
        public void setId(java.lang.String value) {
            this.id = value;
        }

    }


    /**
     * <p>Java class for anonymous complex type</p>.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.</p>
     * 
     * <pre>{@code
     * <complexType>
     *   <simpleContent>
     *     <extension base="<http://www.opentrafficsim.org/ots>LinearDensityType">
     *       <attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}InputParameterIdType" />
     *     </extension>
     *   </simpleContent>
     * </complexType>
     * }</pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "value"
    })
    public static class LinearDensity implements Serializable
    {

        private static final long serialVersionUID = 10102L;
        @XmlValue
        @XmlJavaTypeAdapter(LinearDensityAdapter.class)
        protected LinearDensityType value;
        @XmlAttribute(name = "Id", required = true)
        protected java.lang.String id;

        /**
         * Gets the value of the value property.
         * 
         * @return
         *     possible object is
         *     {@link java.lang.String }
         *     
         */
        public LinearDensityType getValue() {
            return value;
        }

        /**
         * Sets the value of the value property.
         * 
         * @param value
         *     allowed object is
         *     {@link java.lang.String }
         *     
         */
        public void setValue(LinearDensityType value) {
            this.value = value;
        }

        /**
         * Gets the value of the id property.
         * 
         * @return
         *     possible object is
         *     {@link java.lang.String }
         *     
         */
        public java.lang.String getId() {
            return id;
        }

        /**
         * Sets the value of the id property.
         * 
         * @param value
         *     allowed object is
         *     {@link java.lang.String }
         *     
         */
        public void setId(java.lang.String value) {
            this.id = value;
        }

    }


    /**
     * <p>Java class for anonymous complex type</p>.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.</p>
     * 
     * <pre>{@code
     * <complexType>
     *   <simpleContent>
     *     <extension base="<http://www.opentrafficsim.org/ots>SpeedType">
     *       <attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}InputParameterIdType" />
     *     </extension>
     *   </simpleContent>
     * </complexType>
     * }</pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "value"
    })
    public static class Speed implements Serializable
    {

        private static final long serialVersionUID = 10102L;
        @XmlValue
        @XmlJavaTypeAdapter(SpeedAdapter.class)
        protected SpeedType value;
        @XmlAttribute(name = "Id", required = true)
        protected java.lang.String id;

        /**
         * Gets the value of the value property.
         * 
         * @return
         *     possible object is
         *     {@link java.lang.String }
         *     
         */
        public SpeedType getValue() {
            return value;
        }

        /**
         * Sets the value of the value property.
         * 
         * @param value
         *     allowed object is
         *     {@link java.lang.String }
         *     
         */
        public void setValue(SpeedType value) {
            this.value = value;
        }

        /**
         * Gets the value of the id property.
         * 
         * @return
         *     possible object is
         *     {@link java.lang.String }
         *     
         */
        public java.lang.String getId() {
            return id;
        }

        /**
         * Sets the value of the id property.
         * 
         * @param value
         *     allowed object is
         *     {@link java.lang.String }
         *     
         */
        public void setId(java.lang.String value) {
            this.id = value;
        }

    }


    /**
     * <p>Java class for anonymous complex type</p>.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.</p>
     * 
     * <pre>{@code
     * <complexType>
     *   <simpleContent>
     *     <extension base="<http://www.opentrafficsim.org/ots>string">
     *       <attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}InputParameterIdType" />
     *     </extension>
     *   </simpleContent>
     * </complexType>
     * }</pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "value"
    })
    public static class String implements Serializable
    {

        private static final long serialVersionUID = 10102L;
        @XmlValue
        @XmlJavaTypeAdapter(StringAdapter.class)
        protected StringType value;
        @XmlAttribute(name = "Id", required = true)
        protected java.lang.String id;

        /**
         * Gets the value of the value property.
         * 
         * @return
         *     possible object is
         *     {@link java.lang.String }
         *     
         */
        public StringType getValue() {
            return value;
        }

        /**
         * Sets the value of the value property.
         * 
         * @param value
         *     allowed object is
         *     {@link java.lang.String }
         *     
         */
        public void setValue(StringType value) {
            this.value = value;
        }

        /**
         * Gets the value of the id property.
         * 
         * @return
         *     possible object is
         *     {@link java.lang.String }
         *     
         */
        public java.lang.String getId() {
            return id;
        }

        /**
         * Sets the value of the id property.
         * 
         * @param value
         *     allowed object is
         *     {@link java.lang.String }
         *     
         */
        public void setId(java.lang.String value) {
            this.id = value;
        }

    }

}
