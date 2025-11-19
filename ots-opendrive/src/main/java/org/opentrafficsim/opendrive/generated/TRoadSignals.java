
package org.opentrafficsim.opendrive.generated;

import java.util.ArrayList;
import java.util.List;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElements;
import jakarta.xml.bind.annotation.XmlType;


/**
 * The &lt;signals&gt; element is the container for all signals along a road.
 * 
 * <p>Java class for t_road_signals complex type</p>.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.</p>
 * 
 * <pre>{@code
 * <complexType name="t_road_signals">
 *   <complexContent>
 *     <extension base="{http://code.asam.net/simulation/standard/opendrive_schema}_OpenDriveElement">
 *       <sequence>
 *         <element name="signal" type="{http://code.asam.net/simulation/standard/opendrive_schema}t_road_signals_signal" maxOccurs="unbounded" minOccurs="0"/>
 *         <element name="signalReference" type="{http://code.asam.net/simulation/standard/opendrive_schema}t_road_signals_signalReference" maxOccurs="unbounded" minOccurs="0"/>
 *         <group ref="{http://code.asam.net/simulation/standard/opendrive_schema}g_additionalData" maxOccurs="unbounded" minOccurs="0"/>
 *       </sequence>
 *     </extension>
 *   </complexContent>
 * </complexType>
 * }</pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "t_road_signals", propOrder = {
    "signal",
    "signalReference",
    "gAdditionalData"
})
@SuppressWarnings("all") public class TRoadSignals
    extends OpenDriveElement
{

    protected List<TRoadSignalsSignal> signal;
    protected List<TRoadSignalsSignalReference> signalReference;
    /**
     * OpenDRIVE offers the possibility to include external data. The processing of this data depends on the application.
     * Additional data may be placed at any position in OpenDRIVE.
     * 
     */
    @XmlElements({
        @XmlElement(name = "include", type = TInclude.class),
        @XmlElement(name = "userData", type = TUserData.class),
        @XmlElement(name = "dataQuality", type = TDataQuality.class)
    })
    protected List<Object> gAdditionalData;

    /**
     * Gets the value of the signal property.
     * 
     * <p>This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the signal property.</p>
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * </p>
     * <pre>
     * getSignal().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link TRoadSignalsSignal }
     * </p>
     * 
     * 
     * @return
     *     The value of the signal property.
     */
    public List<TRoadSignalsSignal> getSignal() {
        if (signal == null) {
            signal = new ArrayList<>();
        }
        return this.signal;
    }

    /**
     * Gets the value of the signalReference property.
     * 
     * <p>This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the signalReference property.</p>
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * </p>
     * <pre>
     * getSignalReference().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link TRoadSignalsSignalReference }
     * </p>
     * 
     * 
     * @return
     *     The value of the signalReference property.
     */
    public List<TRoadSignalsSignalReference> getSignalReference() {
        if (signalReference == null) {
            signalReference = new ArrayList<>();
        }
        return this.signalReference;
    }

    /**
     * OpenDRIVE offers the possibility to include external data. The processing of this data depends on the application.
     * Additional data may be placed at any position in OpenDRIVE.
     * 
     * Gets the value of the gAdditionalData property.
     * 
     * <p>This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the gAdditionalData property.</p>
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * </p>
     * <pre>
     * getGAdditionalData().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link TDataQuality }
     * {@link TInclude }
     * {@link TUserData }
     * </p>
     * 
     * 
     * @return
     *     The value of the gAdditionalData property.
     */
    public List<Object> getGAdditionalData() {
        if (gAdditionalData == null) {
            gAdditionalData = new ArrayList<>();
        }
        return this.gAdditionalData;
    }

}
