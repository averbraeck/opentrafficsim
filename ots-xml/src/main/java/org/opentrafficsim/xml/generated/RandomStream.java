
package org.opentrafficsim.xml.generated;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlSchemaType;
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
 *       <sequence maxOccurs="unbounded">
 *         <element name="Replication">
 *           <complexType>
 *             <complexContent>
 *               <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 <attribute name="Id" use="required" type="{http://www.w3.org/2001/XMLSchema}positiveInteger" />
 *                 <attribute name="Seed" use="required" type="{http://www.w3.org/2001/XMLSchema}integer" />
 *               </restriction>
 *             </complexContent>
 *           </complexType>
 *         </element>
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
@XmlType(name = "", propOrder = {
    "replication"
})
@XmlRootElement(name = "RandomStream")
@SuppressWarnings("all") public class RandomStream
    implements Serializable
{

    private static final long serialVersionUID = 10102L;
    @XmlElement(name = "Replication", required = true)
    protected List<RandomStream.Replication> replication;
    @XmlAttribute(name = "Id", required = true)
    protected String id;

    /**
     * Gets the value of the replication property.
     * 
     * <p>This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the replication property.</p>
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * </p>
     * <pre>
     * getReplication().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link RandomStream.Replication }
     * </p>
     * 
     * 
     * @return
     *     The value of the replication property.
     */
    public List<RandomStream.Replication> getReplication() {
        if (replication == null) {
            replication = new ArrayList<>();
        }
        return this.replication;
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
     *       <attribute name="Id" use="required" type="{http://www.w3.org/2001/XMLSchema}positiveInteger" />
     *       <attribute name="Seed" use="required" type="{http://www.w3.org/2001/XMLSchema}integer" />
     *     </restriction>
     *   </complexContent>
     * </complexType>
     * }</pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class Replication
        implements Serializable
    {

        private static final long serialVersionUID = 10102L;
        @XmlAttribute(name = "Id", required = true)
        @XmlSchemaType(name = "positiveInteger")
        protected BigInteger id;
        @XmlAttribute(name = "Seed", required = true)
        protected BigInteger seed;

        /**
         * Gets the value of the id property.
         * 
         * @return
         *     possible object is
         *     {@link BigInteger }
         *     
         */
        public BigInteger getId() {
            return id;
        }

        /**
         * Sets the value of the id property.
         * 
         * @param value
         *     allowed object is
         *     {@link BigInteger }
         *     
         */
        public void setId(BigInteger value) {
            this.id = value;
        }

        /**
         * Gets the value of the seed property.
         * 
         * @return
         *     possible object is
         *     {@link BigInteger }
         *     
         */
        public BigInteger getSeed() {
            return seed;
        }

        /**
         * Sets the value of the seed property.
         * 
         * @param value
         *     allowed object is
         *     {@link BigInteger }
         *     
         */
        public void setSeed(BigInteger value) {
            this.seed = value;
        }

    }

}
