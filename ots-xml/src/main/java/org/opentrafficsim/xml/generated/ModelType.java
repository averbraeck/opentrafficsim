
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
import org.opentrafficsim.xml.bindings.StringAdapter;
import org.opentrafficsim.xml.bindings.types.StringType;


/**
 * <p>Java class for ModelType complex type</p>.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.</p>
 * 
 * <pre>{@code
 * <complexType name="ModelType">
 *   <complexContent>
 *     <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       <sequence>
 *         <element name="DefaultParameters" type="{http://www.opentrafficsim.org/ots}ModelParameters" minOccurs="0"/>
 *         <element name="GtuTypeParameters" maxOccurs="unbounded" minOccurs="0">
 *           <complexType>
 *             <complexContent>
 *               <extension base="{http://www.opentrafficsim.org/ots}ModelParameters">
 *                 <attribute name="GtuType" use="required" type="{http://www.opentrafficsim.org/ots}string" />
 *               </extension>
 *             </complexContent>
 *           </complexType>
 *         </element>
 *         <element name="StrategicalPlanner" minOccurs="0">
 *           <complexType>
 *             <complexContent>
 *               <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 <choice>
 *                   <element name="Route">
 *                     <complexType>
 *                       <complexContent>
 *                         <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                           <choice>
 *                             <element name="None" type="{http://www.opentrafficsim.org/ots}EmptyType"/>
 *                             <element name="Shortest" type="{http://www.opentrafficsim.org/ots}EmptyType"/>
 *                           </choice>
 *                         </restriction>
 *                       </complexContent>
 *                     </complexType>
 *                   </element>
 *                 </choice>
 *               </restriction>
 *             </complexContent>
 *           </complexType>
 *         </element>
 *         <element name="TacticalPlanner" minOccurs="0">
 *           <complexType>
 *             <complexContent>
 *               <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 <choice>
 *                   <element name="Lmrs">
 *                     <complexType>
 *                       <complexContent>
 *                         <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                           <sequence>
 *                             <element name="DefaultModel" type="{http://www.opentrafficsim.org/ots}LmrsModel"/>
 *                             <element name="GtuTypeModel" type="{http://www.opentrafficsim.org/ots}GtuTypeLmrsModel" maxOccurs="unbounded" minOccurs="0"/>
 *                           </sequence>
 *                         </restriction>
 *                       </complexContent>
 *                     </complexType>
 *                   </element>
 *                 </choice>
 *               </restriction>
 *             </complexContent>
 *           </complexType>
 *         </element>
 *       </sequence>
 *       <attribute name="Id" type="{http://www.opentrafficsim.org/ots}IdType" />
 *     </restriction>
 *   </complexContent>
 * </complexType>
 * }</pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ModelType", propOrder = {
    "defaultParameters",
    "gtuTypeParameters",
    "strategicalPlanner",
    "tacticalPlanner"
})
@SuppressWarnings("all") public class ModelType
    implements Serializable
{

    private static final long serialVersionUID = 10102L;
    /**
     * The default parameters are used to define default values for all GTU
     *             types in simulation. This only needs to be done if one needs to use a different default value for the GTU types than
     *             the default value of the parameter. For example if 'a' has a default value of 1.25m/s², but in your simulations
     *             you want to use 1.37m/s² for all or most GTU types, then this value should be specified here.
     * 
     */
    @XmlElement(name = "DefaultParameters")
    protected ModelParameters defaultParameters;
    /**
     * Here parameter values are defined that are specific to a GTU type.
     * 
     */
    @XmlElement(name = "GtuTypeParameters")
    protected List<ModelType.GtuTypeParameters> gtuTypeParameters;
    @XmlElement(name = "StrategicalPlanner")
    protected ModelType.StrategicalPlanner strategicalPlanner;
    @XmlElement(name = "TacticalPlanner")
    protected ModelType.TacticalPlanner tacticalPlanner;
    @XmlAttribute(name = "Id")
    protected String id;

    /**
     * The default parameters are used to define default values for all GTU
     *             types in simulation. This only needs to be done if one needs to use a different default value for the GTU types than
     *             the default value of the parameter. For example if 'a' has a default value of 1.25m/s², but in your simulations
     *             you want to use 1.37m/s² for all or most GTU types, then this value should be specified here.
     * 
     * @return
     *     possible object is
     *     {@link ModelParameters }
     *     
     */
    public ModelParameters getDefaultParameters() {
        return defaultParameters;
    }

    /**
     * Sets the value of the defaultParameters property.
     * 
     * @param value
     *     allowed object is
     *     {@link ModelParameters }
     *     
     * @see #getDefaultParameters()
     */
    public void setDefaultParameters(ModelParameters value) {
        this.defaultParameters = value;
    }

    /**
     * Here parameter values are defined that are specific to a GTU type.
     * 
     * Gets the value of the gtuTypeParameters property.
     * 
     * <p>This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the gtuTypeParameters property.</p>
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * </p>
     * <pre>
     * getGtuTypeParameters().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ModelType.GtuTypeParameters }
     * </p>
     * 
     * 
     * @return
     *     The value of the gtuTypeParameters property.
     */
    public List<ModelType.GtuTypeParameters> getGtuTypeParameters() {
        if (gtuTypeParameters == null) {
            gtuTypeParameters = new ArrayList<>();
        }
        return this.gtuTypeParameters;
    }

    /**
     * Gets the value of the strategicalPlanner property.
     * 
     * @return
     *     possible object is
     *     {@link ModelType.StrategicalPlanner }
     *     
     */
    public ModelType.StrategicalPlanner getStrategicalPlanner() {
        return strategicalPlanner;
    }

    /**
     * Sets the value of the strategicalPlanner property.
     * 
     * @param value
     *     allowed object is
     *     {@link ModelType.StrategicalPlanner }
     *     
     */
    public void setStrategicalPlanner(ModelType.StrategicalPlanner value) {
        this.strategicalPlanner = value;
    }

    /**
     * Gets the value of the tacticalPlanner property.
     * 
     * @return
     *     possible object is
     *     {@link ModelType.TacticalPlanner }
     *     
     */
    public ModelType.TacticalPlanner getTacticalPlanner() {
        return tacticalPlanner;
    }

    /**
     * Sets the value of the tacticalPlanner property.
     * 
     * @param value
     *     allowed object is
     *     {@link ModelType.TacticalPlanner }
     *     
     */
    public void setTacticalPlanner(ModelType.TacticalPlanner value) {
        this.tacticalPlanner = value;
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
     * <p>Java class for anonymous complex type</p>.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.</p>
     * 
     * <pre>{@code
     * <complexType>
     *   <complexContent>
     *     <extension base="{http://www.opentrafficsim.org/ots}ModelParameters">
     *       <attribute name="GtuType" use="required" type="{http://www.opentrafficsim.org/ots}string" />
     *     </extension>
     *   </complexContent>
     * </complexType>
     * }</pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class GtuTypeParameters
        extends ModelParameters
        implements Serializable
    {

        private static final long serialVersionUID = 10102L;
        @XmlAttribute(name = "GtuType", required = true)
        @XmlJavaTypeAdapter(StringAdapter.class)
        protected StringType gtuType;

        /**
         * Gets the value of the gtuType property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public StringType getGtuType() {
            return gtuType;
        }

        /**
         * Sets the value of the gtuType property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setGtuType(StringType value) {
            this.gtuType = value;
        }

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
     *       <choice>
     *         <element name="Route">
     *           <complexType>
     *             <complexContent>
     *               <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                 <choice>
     *                   <element name="None" type="{http://www.opentrafficsim.org/ots}EmptyType"/>
     *                   <element name="Shortest" type="{http://www.opentrafficsim.org/ots}EmptyType"/>
     *                 </choice>
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
    @XmlType(name = "", propOrder = {
        "route"
    })
    public static class StrategicalPlanner
        implements Serializable
    {

        private static final long serialVersionUID = 10102L;
        @XmlElement(name = "Route")
        protected ModelType.StrategicalPlanner.Route route;

        /**
         * Gets the value of the route property.
         * 
         * @return
         *     possible object is
         *     {@link ModelType.StrategicalPlanner.Route }
         *     
         */
        public ModelType.StrategicalPlanner.Route getRoute() {
            return route;
        }

        /**
         * Sets the value of the route property.
         * 
         * @param value
         *     allowed object is
         *     {@link ModelType.StrategicalPlanner.Route }
         *     
         */
        public void setRoute(ModelType.StrategicalPlanner.Route value) {
            this.route = value;
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
         *       <choice>
         *         <element name="None" type="{http://www.opentrafficsim.org/ots}EmptyType"/>
         *         <element name="Shortest" type="{http://www.opentrafficsim.org/ots}EmptyType"/>
         *       </choice>
         *     </restriction>
         *   </complexContent>
         * </complexType>
         * }</pre>
         * 
         * 
         */
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "", propOrder = {
            "none",
            "shortest"
        })
        public static class Route
            implements Serializable
        {

            private static final long serialVersionUID = 10102L;
            @XmlElement(name = "None")
            protected EmptyType none;
            @XmlElement(name = "Shortest")
            protected EmptyType shortest;

            /**
             * Gets the value of the none property.
             * 
             * @return
             *     possible object is
             *     {@link EmptyType }
             *     
             */
            public EmptyType getNone() {
                return none;
            }

            /**
             * Sets the value of the none property.
             * 
             * @param value
             *     allowed object is
             *     {@link EmptyType }
             *     
             */
            public void setNone(EmptyType value) {
                this.none = value;
            }

            /**
             * Gets the value of the shortest property.
             * 
             * @return
             *     possible object is
             *     {@link EmptyType }
             *     
             */
            public EmptyType getShortest() {
                return shortest;
            }

            /**
             * Sets the value of the shortest property.
             * 
             * @param value
             *     allowed object is
             *     {@link EmptyType }
             *     
             */
            public void setShortest(EmptyType value) {
                this.shortest = value;
            }

        }

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
     *       <choice>
     *         <element name="Lmrs">
     *           <complexType>
     *             <complexContent>
     *               <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                 <sequence>
     *                   <element name="DefaultModel" type="{http://www.opentrafficsim.org/ots}LmrsModel"/>
     *                   <element name="GtuTypeModel" type="{http://www.opentrafficsim.org/ots}GtuTypeLmrsModel" maxOccurs="unbounded" minOccurs="0"/>
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
    @XmlType(name = "", propOrder = {
        "lmrs"
    })
    public static class TacticalPlanner
        implements Serializable
    {

        private static final long serialVersionUID = 10102L;
        @XmlElement(name = "Lmrs")
        protected ModelType.TacticalPlanner.Lmrs lmrs;

        /**
         * Gets the value of the lmrs property.
         * 
         * @return
         *     possible object is
         *     {@link ModelType.TacticalPlanner.Lmrs }
         *     
         */
        public ModelType.TacticalPlanner.Lmrs getLmrs() {
            return lmrs;
        }

        /**
         * Sets the value of the lmrs property.
         * 
         * @param value
         *     allowed object is
         *     {@link ModelType.TacticalPlanner.Lmrs }
         *     
         */
        public void setLmrs(ModelType.TacticalPlanner.Lmrs value) {
            this.lmrs = value;
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
         *         <element name="DefaultModel" type="{http://www.opentrafficsim.org/ots}LmrsModel"/>
         *         <element name="GtuTypeModel" type="{http://www.opentrafficsim.org/ots}GtuTypeLmrsModel" maxOccurs="unbounded" minOccurs="0"/>
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
            "defaultModel",
            "gtuTypeModel"
        })
        public static class Lmrs
            implements Serializable
        {

            private static final long serialVersionUID = 10102L;
            @XmlElement(name = "DefaultModel", required = true)
            protected LmrsModel defaultModel;
            @XmlElement(name = "GtuTypeModel")
            protected List<GtuTypeLmrsModel> gtuTypeModel;

            /**
             * Gets the value of the defaultModel property.
             * 
             * @return
             *     possible object is
             *     {@link LmrsModel }
             *     
             */
            public LmrsModel getDefaultModel() {
                return defaultModel;
            }

            /**
             * Sets the value of the defaultModel property.
             * 
             * @param value
             *     allowed object is
             *     {@link LmrsModel }
             *     
             */
            public void setDefaultModel(LmrsModel value) {
                this.defaultModel = value;
            }

            /**
             * Gets the value of the gtuTypeModel property.
             * 
             * <p>This accessor method returns a reference to the live list,
             * not a snapshot. Therefore any modification you make to the
             * returned list will be present inside the JAXB object.
             * This is why there is not a <CODE>set</CODE> method for the gtuTypeModel property.</p>
             * 
             * <p>
             * For example, to add a new item, do as follows:
             * </p>
             * <pre>
             * getGtuTypeModel().add(newItem);
             * </pre>
             * 
             * 
             * <p>
             * Objects of the following type(s) are allowed in the list
             * {@link GtuTypeLmrsModel }
             * </p>
             * 
             * 
             * @return
             *     The value of the gtuTypeModel property.
             */
            public List<GtuTypeLmrsModel> getGtuTypeModel() {
                if (gtuTypeModel == null) {
                    gtuTypeModel = new ArrayList<>();
                }
                return this.gtuTypeModel;
            }

        }

    }

}
