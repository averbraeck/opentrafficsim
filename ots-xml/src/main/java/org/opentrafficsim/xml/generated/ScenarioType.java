
package org.opentrafficsim.xml.generated;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.opentrafficsim.xml.bindings.StringAdapter;
import org.opentrafficsim.xml.bindings.types.StringType;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * <p>Java class for ScenarioType complex type</p>.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.</p>
 * 
 * <pre>{@code
 * <complexType name="ScenarioType">
 *   <complexContent>
 *     <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       <sequence>
 *         <element name="InputParameters" type="{http://www.opentrafficsim.org/ots}InputParameters" minOccurs="0"/>
 *         <element name="Od" maxOccurs="unbounded" minOccurs="0">
 *           <complexType>
 *             <complexContent>
 *               <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 <attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}string" />
 *               </restriction>
 *             </complexContent>
 *           </complexType>
 *         </element>
 *         <element name="Control" maxOccurs="unbounded" minOccurs="0">
 *           <complexType>
 *             <complexContent>
 *               <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 <attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}string" />
 *               </restriction>
 *             </complexContent>
 *           </complexType>
 *         </element>
 *         <element name="ModelIdReferral" type="{http://www.opentrafficsim.org/ots}ModelIdReferralType" maxOccurs="unbounded" minOccurs="0"/>
 *       </sequence>
 *       <attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}IdType" />
 *     </restriction>
 *   </complexContent>
 * </complexType>
 * }</pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ScenarioType", propOrder = {
    "inputParameters",
    "od",
    "control",
    "modelIdReferral"
})
@SuppressWarnings("all") public class ScenarioType
    implements Serializable
{

    private static final long serialVersionUID = 10102L;
    @XmlElement(name = "InputParameters")
    protected InputParameters inputParameters;
    @XmlElement(name = "Od")
    protected List<ScenarioType.Od> od;
    @XmlElement(name = "Control")
    protected List<ScenarioType.Control> control;
    @XmlElement(name = "ModelIdReferral")
    protected List<ModelIdReferralType> modelIdReferral;
    @XmlAttribute(name = "Id", required = true)
    protected String id;

    /**
     * Gets the value of the inputParameters property.
     * 
     * @return
     *     possible object is
     *     {@link InputParameters }
     *     
     */
    public InputParameters getInputParameters() {
        return inputParameters;
    }

    /**
     * Sets the value of the inputParameters property.
     * 
     * @param value
     *     allowed object is
     *     {@link InputParameters }
     *     
     */
    public void setInputParameters(InputParameters value) {
        this.inputParameters = value;
    }

    /**
     * Gets the value of the od property.
     * 
     * <p>This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the od property.</p>
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * </p>
     * <pre>
     * getOd().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ScenarioType.Od }
     * </p>
     * 
     * 
     * @return
     *     The value of the od property.
     */
    public List<ScenarioType.Od> getOd() {
        if (od == null) {
            od = new ArrayList<>();
        }
        return this.od;
    }

    /**
     * Gets the value of the control property.
     * 
     * <p>This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the control property.</p>
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * </p>
     * <pre>
     * getControl().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ScenarioType.Control }
     * </p>
     * 
     * 
     * @return
     *     The value of the control property.
     */
    public List<ScenarioType.Control> getControl() {
        if (control == null) {
            control = new ArrayList<>();
        }
        return this.control;
    }

    /**
     * Gets the value of the modelIdReferral property.
     * 
     * <p>This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the modelIdReferral property.</p>
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * </p>
     * <pre>
     * getModelIdReferral().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ModelIdReferralType }
     * </p>
     * 
     * 
     * @return
     *     The value of the modelIdReferral property.
     */
    public List<ModelIdReferralType> getModelIdReferral() {
        if (modelIdReferral == null) {
            modelIdReferral = new ArrayList<>();
        }
        return this.modelIdReferral;
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
     *     <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       <attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}string" />
     *     </restriction>
     *   </complexContent>
     * </complexType>
     * }</pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class Control
        implements Serializable
    {

        private static final long serialVersionUID = 10102L;
        @XmlAttribute(name = "Id", required = true)
        @XmlJavaTypeAdapter(StringAdapter.class)
        protected StringType id;

        /**
         * Gets the value of the id property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public StringType getId() {
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
        public void setId(StringType value) {
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
     *   <complexContent>
     *     <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       <attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}string" />
     *     </restriction>
     *   </complexContent>
     * </complexType>
     * }</pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class Od
        implements Serializable
    {

        private static final long serialVersionUID = 10102L;
        @XmlAttribute(name = "Id", required = true)
        @XmlJavaTypeAdapter(StringAdapter.class)
        protected StringType id;

        /**
         * Gets the value of the id property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public StringType getId() {
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
        public void setId(StringType value) {
            this.id = value;
        }

    }

}
