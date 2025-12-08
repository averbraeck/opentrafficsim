
package org.opentrafficsim.xml.generated;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.opentrafficsim.xml.bindings.DoubleAdapter;
import org.opentrafficsim.xml.bindings.StringAdapter;
import org.opentrafficsim.xml.bindings.types.DoubleType;
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
 *         <element name="ShortestRoute" maxOccurs="unbounded">
 *           <complexType>
 *             <complexContent>
 *               <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 <attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}string" />
 *                 <attribute name="Weight" use="required" type="{http://www.opentrafficsim.org/ots}double" />
 *               </restriction>
 *             </complexContent>
 *           </complexType>
 *         </element>
 *         <element name="RandomStream" type="{http://www.opentrafficsim.org/ots}RandomStreamSource" minOccurs="0"/>
 *       </sequence>
 *       <attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}IdType" />
 *       <attribute name="GtuType" use="required" type="{http://www.opentrafficsim.org/ots}string" />
 *     </restriction>
 *   </complexContent>
 * </complexType>
 * }</pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "shortestRoute",
    "randomStream"
})
@XmlRootElement(name = "ShortestRouteMix")
@SuppressWarnings("all") public class ShortestRouteMix
    implements Serializable
{

    private static final long serialVersionUID = 10102L;
    @XmlElement(name = "ShortestRoute", required = true)
    protected List<ShortestRouteMix.ShortestRoute> shortestRoute;
    @XmlElement(name = "RandomStream")
    protected RandomStreamSource randomStream;
    @XmlAttribute(name = "Id", required = true)
    protected String id;
    @XmlAttribute(name = "GtuType", required = true)
    @XmlJavaTypeAdapter(StringAdapter.class)
    protected StringType gtuType;

    /**
     * Gets the value of the shortestRoute property.
     * 
     * <p>This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the shortestRoute property.</p>
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * </p>
     * <pre>
     * getShortestRoute().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ShortestRouteMix.ShortestRoute }
     * </p>
     * 
     * 
     * @return
     *     The value of the shortestRoute property.
     */
    public List<ShortestRouteMix.ShortestRoute> getShortestRoute() {
        if (shortestRoute == null) {
            shortestRoute = new ArrayList<>();
        }
        return this.shortestRoute;
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
     *       <attribute name="Weight" use="required" type="{http://www.opentrafficsim.org/ots}double" />
     *     </restriction>
     *   </complexContent>
     * </complexType>
     * }</pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class ShortestRoute
        implements Serializable
    {

        private static final long serialVersionUID = 10102L;
        @XmlAttribute(name = "Id", required = true)
        @XmlJavaTypeAdapter(StringAdapter.class)
        protected StringType id;
        @XmlAttribute(name = "Weight", required = true)
        @XmlJavaTypeAdapter(DoubleAdapter.class)
        protected DoubleType weight;

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

        /**
         * Gets the value of the weight property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public DoubleType getWeight() {
            return weight;
        }

        /**
         * Sets the value of the weight property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setWeight(DoubleType value) {
            this.weight = value;
        }

    }

}
