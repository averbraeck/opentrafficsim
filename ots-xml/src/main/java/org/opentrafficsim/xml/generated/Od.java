
package org.opentrafficsim.xml.generated;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.opentrafficsim.xml.bindings.InterpolationAdapter;
import org.opentrafficsim.xml.bindings.PositiveFactorAdapter;
import org.opentrafficsim.xml.bindings.StringAdapter;
import org.opentrafficsim.xml.bindings.types.DoubleType;
import org.opentrafficsim.xml.bindings.types.InterpolationType;
import org.opentrafficsim.xml.bindings.types.StringType;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


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
 *         <element name="GlobalTime" type="{http://www.opentrafficsim.org/ots}GlobalTimeType" minOccurs="0"/>
 *         <element name="Category" type="{http://www.opentrafficsim.org/ots}CategoryType" maxOccurs="unbounded" minOccurs="0"/>
 *         <element name="Cell" maxOccurs="unbounded" minOccurs="0">
 *           <complexType>
 *             <complexContent>
 *               <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 <sequence>
 *                   <element name="Level" type="{http://www.opentrafficsim.org/ots}LevelTimeType" maxOccurs="unbounded" minOccurs="0"/>
 *                 </sequence>
 *                 <attribute name="Origin" use="required" type="{http://www.opentrafficsim.org/ots}string" />
 *                 <attribute name="Destination" use="required" type="{http://www.opentrafficsim.org/ots}string" />
 *                 <attribute name="Category" type="{http://www.opentrafficsim.org/ots}string" />
 *                 <attribute name="Interpolation" type="{http://www.opentrafficsim.org/ots}InterpolationType" />
 *                 <attribute name="Factor" type="{http://www.opentrafficsim.org/ots}PositiveFactor" />
 *               </restriction>
 *             </complexContent>
 *           </complexType>
 *         </element>
 *         <element name="RandomStream" type="{http://www.opentrafficsim.org/ots}RandomStreamSource" minOccurs="0"/>
 *       </sequence>
 *       <attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}IdType" />
 *       <attribute name="SinkType" use="required" type="{http://www.opentrafficsim.org/ots}string" />
 *       <attribute name="GlobalInterpolation" type="{http://www.opentrafficsim.org/ots}InterpolationType" default="LINEAR" />
 *       <attribute name="GlobalFactor" type="{http://www.opentrafficsim.org/ots}PositiveFactor" default="1.0" />
 *       <attribute name="Options" type="{http://www.opentrafficsim.org/ots}string" />
 *     </restriction>
 *   </complexContent>
 * </complexType>
 * }</pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "globalTime",
    "category",
    "cell",
    "randomStream"
})
@XmlRootElement(name = "Od")
@SuppressWarnings("all") public class Od
    implements Serializable
{

    private static final long serialVersionUID = 10102L;
    @XmlElement(name = "GlobalTime")
    protected GlobalTimeType globalTime;
    @XmlElement(name = "Category")
    protected List<CategoryType> category;
    @XmlElement(name = "Cell")
    protected List<Od.Cell> cell;
    @XmlElement(name = "RandomStream")
    protected RandomStreamSource randomStream;
    @XmlAttribute(name = "Id", required = true)
    protected String id;
    @XmlAttribute(name = "SinkType", required = true)
    @XmlJavaTypeAdapter(StringAdapter.class)
    protected StringType sinkType;
    @XmlAttribute(name = "GlobalInterpolation")
    @XmlJavaTypeAdapter(InterpolationAdapter.class)
    protected InterpolationType globalInterpolation;
    @XmlAttribute(name = "GlobalFactor")
    @XmlJavaTypeAdapter(PositiveFactorAdapter.class)
    protected DoubleType globalFactor;
    @XmlAttribute(name = "Options")
    @XmlJavaTypeAdapter(StringAdapter.class)
    protected StringType options;

    /**
     * Gets the value of the globalTime property.
     * 
     * @return
     *     possible object is
     *     {@link GlobalTimeType }
     *     
     */
    public GlobalTimeType getGlobalTime() {
        return globalTime;
    }

    /**
     * Sets the value of the globalTime property.
     * 
     * @param value
     *     allowed object is
     *     {@link GlobalTimeType }
     *     
     */
    public void setGlobalTime(GlobalTimeType value) {
        this.globalTime = value;
    }

    /**
     * Gets the value of the category property.
     * 
     * <p>This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the category property.</p>
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * </p>
     * <pre>
     * getCategory().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CategoryType }
     * </p>
     * 
     * 
     * @return
     *     The value of the category property.
     */
    public List<CategoryType> getCategory() {
        if (category == null) {
            category = new ArrayList<>();
        }
        return this.category;
    }

    /**
     * Gets the value of the cell property.
     * 
     * <p>This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the cell property.</p>
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * </p>
     * <pre>
     * getCell().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Od.Cell }
     * </p>
     * 
     * 
     * @return
     *     The value of the cell property.
     */
    public List<Od.Cell> getCell() {
        if (cell == null) {
            cell = new ArrayList<>();
        }
        return this.cell;
    }

    /**
     * Gets the value of the randomStream property.
     * 
     * @return
     *     possible object is
     *     {@link RandomStreamSource }
     *     
     */
    public RandomStreamSource getRandomStream() {
        return randomStream;
    }

    /**
     * Sets the value of the randomStream property.
     * 
     * @param value
     *     allowed object is
     *     {@link RandomStreamSource }
     *     
     */
    public void setRandomStream(RandomStreamSource value) {
        this.randomStream = value;
    }

    /**
     * Gets the value of the id property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setId(String value) {
        this.id = value;
    }

    /**
     * Gets the value of the sinkType property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public StringType getSinkType() {
        return sinkType;
    }

    /**
     * Sets the value of the sinkType property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSinkType(StringType value) {
        this.sinkType = value;
    }

    /**
     * Gets the value of the globalInterpolation property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public InterpolationType getGlobalInterpolation() {
        if (globalInterpolation == null) {
            return new InterpolationAdapter().unmarshal("LINEAR");
        } else {
            return globalInterpolation;
        }
    }

    /**
     * Sets the value of the globalInterpolation property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setGlobalInterpolation(InterpolationType value) {
        this.globalInterpolation = value;
    }

    /**
     * Gets the value of the globalFactor property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public DoubleType getGlobalFactor() {
        if (globalFactor == null) {
            return new PositiveFactorAdapter().unmarshal("1.0");
        } else {
            return globalFactor;
        }
    }

    /**
     * Sets the value of the globalFactor property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setGlobalFactor(DoubleType value) {
        this.globalFactor = value;
    }

    /**
     * Gets the value of the options property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public StringType getOptions() {
        return options;
    }

    /**
     * Sets the value of the options property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOptions(StringType value) {
        this.options = value;
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
     *         <element name="Level" type="{http://www.opentrafficsim.org/ots}LevelTimeType" maxOccurs="unbounded" minOccurs="0"/>
     *       </sequence>
     *       <attribute name="Origin" use="required" type="{http://www.opentrafficsim.org/ots}string" />
     *       <attribute name="Destination" use="required" type="{http://www.opentrafficsim.org/ots}string" />
     *       <attribute name="Category" type="{http://www.opentrafficsim.org/ots}string" />
     *       <attribute name="Interpolation" type="{http://www.opentrafficsim.org/ots}InterpolationType" />
     *       <attribute name="Factor" type="{http://www.opentrafficsim.org/ots}PositiveFactor" />
     *     </restriction>
     *   </complexContent>
     * </complexType>
     * }</pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "level"
    })
    public static class Cell
        implements Serializable
    {

        private static final long serialVersionUID = 10102L;
        @XmlElement(name = "Level")
        protected List<LevelTimeType> level;
        @XmlAttribute(name = "Origin", required = true)
        @XmlJavaTypeAdapter(StringAdapter.class)
        protected StringType origin;
        @XmlAttribute(name = "Destination", required = true)
        @XmlJavaTypeAdapter(StringAdapter.class)
        protected StringType destination;
        @XmlAttribute(name = "Category")
        @XmlJavaTypeAdapter(StringAdapter.class)
        protected StringType category;
        @XmlAttribute(name = "Interpolation")
        @XmlJavaTypeAdapter(InterpolationAdapter.class)
        protected InterpolationType interpolation;
        @XmlAttribute(name = "Factor")
        @XmlJavaTypeAdapter(PositiveFactorAdapter.class)
        protected DoubleType factor;

        /**
         * Gets the value of the level property.
         * 
         * <p>This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the level property.</p>
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * </p>
         * <pre>
         * getLevel().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link LevelTimeType }
         * </p>
         * 
         * 
         * @return
         *     The value of the level property.
         */
        public List<LevelTimeType> getLevel() {
            if (level == null) {
                level = new ArrayList<>();
            }
            return this.level;
        }

        /**
         * Gets the value of the origin property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public StringType getOrigin() {
            return origin;
        }

        /**
         * Sets the value of the origin property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setOrigin(StringType value) {
            this.origin = value;
        }

        /**
         * Gets the value of the destination property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public StringType getDestination() {
            return destination;
        }

        /**
         * Sets the value of the destination property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setDestination(StringType value) {
            this.destination = value;
        }

        /**
         * Gets the value of the category property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public StringType getCategory() {
            return category;
        }

        /**
         * Sets the value of the category property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setCategory(StringType value) {
            this.category = value;
        }

        /**
         * Gets the value of the interpolation property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public InterpolationType getInterpolation() {
            return interpolation;
        }

        /**
         * Sets the value of the interpolation property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setInterpolation(InterpolationType value) {
            this.interpolation = value;
        }

        /**
         * Gets the value of the factor property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public DoubleType getFactor() {
            return factor;
        }

        /**
         * Sets the value of the factor property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setFactor(DoubleType value) {
            this.factor = value;
        }

    }

}
