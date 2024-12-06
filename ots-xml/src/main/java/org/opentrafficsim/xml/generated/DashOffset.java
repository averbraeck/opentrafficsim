
package org.opentrafficsim.xml.generated;

import java.io.Serializable;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.opentrafficsim.xml.bindings.PositiveLengthAdapter;
import org.opentrafficsim.xml.bindings.types.LengthType;


/**
 * <p>Java class for DashOffset complex type</p>.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.</p>
 * 
 * <pre>{@code
 * <complexType name="DashOffset">
 *   <complexContent>
 *     <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       <choice>
 *         <element name="Fixed">
 *           <complexType>
 *             <complexContent>
 *               <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 <attribute name="Offset" use="required" type="{http://www.opentrafficsim.org/ots}PositiveLengthType" />
 *               </restriction>
 *             </complexContent>
 *           </complexType>
 *         </element>
 *         <element name="SyncUpstream" type="{http://www.opentrafficsim.org/ots}EmptyType"/>
 *         <element name="SyncDownstream" type="{http://www.opentrafficsim.org/ots}EmptyType"/>
 *       </choice>
 *     </restriction>
 *   </complexContent>
 * </complexType>
 * }</pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DashOffset", propOrder = {
    "fixed",
    "syncUpstream",
    "syncDownstream"
})
@SuppressWarnings("all") public class DashOffset
    implements Serializable
{

    private static final long serialVersionUID = 10102L;
    @XmlElement(name = "Fixed")
    protected DashOffset.Fixed fixed;
    @XmlElement(name = "SyncUpstream")
    protected EmptyType syncUpstream;
    @XmlElement(name = "SyncDownstream")
    protected EmptyType syncDownstream;

    /**
     * Gets the value of the fixed property.
     * 
     * @return
     *     possible object is
     *     {@link DashOffset.Fixed }
     *     
     */
    public DashOffset.Fixed getFixed() {
        return fixed;
    }

    /**
     * Sets the value of the fixed property.
     * 
     * @param value
     *     allowed object is
     *     {@link DashOffset.Fixed }
     *     
     */
    public void setFixed(DashOffset.Fixed value) {
        this.fixed = value;
    }

    /**
     * Gets the value of the syncUpstream property.
     * 
     * @return
     *     possible object is
     *     {@link EmptyType }
     *     
     */
    public EmptyType getSyncUpstream() {
        return syncUpstream;
    }

    /**
     * Sets the value of the syncUpstream property.
     * 
     * @param value
     *     allowed object is
     *     {@link EmptyType }
     *     
     */
    public void setSyncUpstream(EmptyType value) {
        this.syncUpstream = value;
    }

    /**
     * Gets the value of the syncDownstream property.
     * 
     * @return
     *     possible object is
     *     {@link EmptyType }
     *     
     */
    public EmptyType getSyncDownstream() {
        return syncDownstream;
    }

    /**
     * Sets the value of the syncDownstream property.
     * 
     * @param value
     *     allowed object is
     *     {@link EmptyType }
     *     
     */
    public void setSyncDownstream(EmptyType value) {
        this.syncDownstream = value;
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
     *       <attribute name="Offset" use="required" type="{http://www.opentrafficsim.org/ots}PositiveLengthType" />
     *     </restriction>
     *   </complexContent>
     * </complexType>
     * }</pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class Fixed
        implements Serializable
    {

        private static final long serialVersionUID = 10102L;
        @XmlAttribute(name = "Offset", required = true)
        @XmlJavaTypeAdapter(PositiveLengthAdapter.class)
        protected LengthType offset;

        /**
         * Gets the value of the offset property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public LengthType getOffset() {
            return offset;
        }

        /**
         * Sets the value of the offset property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setOffset(LengthType value) {
            this.offset = value;
        }

    }

}
