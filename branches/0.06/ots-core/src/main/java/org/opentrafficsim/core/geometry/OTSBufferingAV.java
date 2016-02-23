package org.opentrafficsim.core.geometry;

import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import org.opentrafficsim.core.network.NetworkException;

/**
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Nov 9, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public final class OTSBufferingAV
{
    /** */
    private OTSBufferingAV()
    {
    }

    /**
     * @param args args
     * @throws NetworkException on error
     * @throws OTSGeometryException on error
     */
    public static void main(final String[] args) throws NetworkException, OTSGeometryException
    {
        OTSLine3D line0 =
            new OTSLine3D(new OTSPoint3D(10, 10, 0), new OTSPoint3D(10, 8, 0), new OTSPoint3D(0, 6, 0), new OTSPoint3D(
                10, 4, 0), new OTSPoint3D(10, 0, 0));
        OTSLine3D line1 =
            new OTSLine3D(new OTSPoint3D(10, 10, 0), new OTSPoint3D(9.999, 8, 0), new OTSPoint3D(9.996, 7.99, 0),
                new OTSPoint3D(9.999, 7.98, 0), new OTSPoint3D(10.03, 7.95, 0), new OTSPoint3D(10.01, 7.94, 0),
                new OTSPoint3D(10.0, 7.94, 0), new OTSPoint3D(10, 6, 0), new OTSPoint3D(10, 2, 0));
        OTSLine3D line2 =
            new OTSLine3D(new OTSPoint3D(10, 10, 0), new OTSPoint3D(9.999, 8, 0), new OTSPoint3D(9.996, 7.99, 0),
                new OTSPoint3D(9.999, 7.98, 0), new OTSPoint3D(10.03, 7.95, 0), new OTSPoint3D(10.01, 7.94, 0),
                new OTSPoint3D(10.0, 7.94, 0), new OTSPoint3D(10, 6, 0), new OTSPoint3D(9.999, 6, 0), new OTSPoint3D(
                    9.996, 5.99, 0), new OTSPoint3D(9.999, 5.98, 0), new OTSPoint3D(10.03, 5.95, 0), new OTSPoint3D(
                    10.01, 5.94, 0), new OTSPoint3D(10.0, 5.94, 0), new OTSPoint3D(10, 2, 0));
        OTSLine3D line3 =
            new OTSLine3D(new OTSPoint3D(-115.3680561332295, -548.0151713307242, 0.0), new OTSPoint3D(
                -121.1405898342023, -546.9967679699366, 0.0), new OTSPoint3D(-133.3954402170488, -545.1596234831587,
                0.0), new OTSPoint3D(-133.49497466097273, -545.1499853728319, 0.0), new OTSPoint3D(-133.59452107477017,
                -545.1404716880575, 0.0), new OTSPoint3D(-133.69407930289987, -545.1310824437005, 0.0), new OTSPoint3D(
                -133.7936491898021, -545.1218176544314, 0.0), new OTSPoint3D(-133.89323057989893, -545.1126773347269,
                0.0), new OTSPoint3D(-133.99282331759446, -545.1036614988684, 0.0), new OTSPoint3D(-134.09242724727505,
                -545.0947701609432, 0.0), new OTSPoint3D(-134.19204221330963, -545.086003334844, 0.0), new OTSPoint3D(
                -134.29166806004977, -545.077361034269, 0.0), new OTSPoint3D(-134.39130463183014, -545.0688432727218,
                0.0), new OTSPoint3D(-134.4909517729686, -545.0604500635113, 0.0), new OTSPoint3D(-134.59060932776654,
                -545.0521814197522, 0.0), new OTSPoint3D(-134.690277140509, -545.0440373543638, 0.0), new OTSPoint3D(
                -134.78995505546513, -545.0360178800717, 0.0), new OTSPoint3D(-134.88964291688814, -545.0281230094058,
                0.0), new OTSPoint3D(-134.98934056901578, -545.0203527547022, 0.0), new OTSPoint3D(-135.08904785607044,
                -545.0127071281019, 0.0), new OTSPoint3D(-135.18876462225958, -545.005186141551, 0.0), new OTSPoint3D(
                -135.28849071177578, -544.9977898068012, 0.0), new OTSPoint3D(-135.38822596879697, -544.9905181354092,
                0.0), new OTSPoint3D(-135.4879702374869, -544.9833711387371, 0.0), new OTSPoint3D(-135.58772336199513,
                -544.9763488279517, 0.0), new OTSPoint3D(-135.68748518645745, -544.9694512140259, 0.0), new OTSPoint3D(
                -135.78725555499602, -544.9626783077368, 0.0), new OTSPoint3D(-135.88703431171965, -544.9560301196673,
                0.0), new OTSPoint3D(-135.98682130072405, -544.9495066602052, 0.0), new OTSPoint3D(-136.08661636609207,
                -544.9431079395432, 0.0), new OTSPoint3D(-136.18641935189396, -544.9368339676794, 0.0), new OTSPoint3D(
                -136.28623010218757, -544.9306847544171, 0.0), new OTSPoint3D(-136.38604846101862, -544.9246603093641,
                0.0), new OTSPoint3D(-136.48587427242094, -544.9187606419338, 0.0), new OTSPoint3D(-136.58570738041672,
                -544.9129857613443, 0.0), new OTSPoint3D(-136.68554762901675, -544.907335676619, 0.0), new OTSPoint3D(
                -136.78539486222067, -544.9018103965861, 0.0), new OTSPoint3D(-136.88524892401722, -544.8964099298789,
                0.0), new OTSPoint3D(-136.98510965838437, -544.8911342849353, 0.0), new OTSPoint3D(-137.08497690928982,
                -544.8859834699989, 0.0), new OTSPoint3D(-137.18485052069096, -544.8809574931176, 0.0), new OTSPoint3D(
                -137.28473033653535, -544.8760563621447, 0.0), new OTSPoint3D(-137.38461620076075, -544.8712800847381,
                0.0), new OTSPoint3D(-137.48450795729553, -544.8666286683607, 0.0), new OTSPoint3D(-137.58440545005882,
                -544.8621021202804, 0.0), new OTSPoint3D(-137.68430852296086, -544.8577004475699, 0.0), new OTSPoint3D(
                -137.78421701990308, -544.8534236571068, 0.0), new OTSPoint3D(-137.8841307847785, -544.8492717555735,
                0.0), new OTSPoint3D(-137.98404966147183, -544.8452447494575, 0.0), new OTSPoint3D(-138.08397349385993,
                -544.841342645051, 0.0), new OTSPoint3D(-138.18390212581176, -544.837565448451, 0.0), new OTSPoint3D(
                -138.28383540118887, -544.8339131655592, 0.0), new OTSPoint3D(-138.38377316384558, -544.8303858020826,
                0.0), new OTSPoint3D(-138.4837152576291, -544.8269833635323, 0.0), new OTSPoint3D(-138.58366152637993,
                -544.8237058552252, 0.0), new OTSPoint3D(-138.68361181393206, -544.8205532822818, 0.0), new OTSPoint3D(
                -138.78356596411322, -544.8175256496284, 0.0), new OTSPoint3D(-138.883523820745, -544.8146229619954,
                0.0), new OTSPoint3D(-138.98348522764337, -544.8118452239183, 0.0), new OTSPoint3D(-139.08345002861856,
                -544.8091924397376, 0.0), new OTSPoint3D(-139.18341806747563, -544.806664613598, 0.0), new OTSPoint3D(
                -139.28338918801452, -544.8042617494492, 0.0), new OTSPoint3D(-139.38336323403036, -544.8019838510459,
                0.0), new OTSPoint3D(-139.48334004931382, -544.7998309219471, 0.0), new OTSPoint3D(-139.58331947765103,
                -544.7978029655169, 0.0), new OTSPoint3D(-139.6833013628242, -544.7958999849238, 0.0), new OTSPoint3D(
                -139.78328554861167, -544.7941219831416, 0.0), new OTSPoint3D(-139.88327187878815, -544.7924689629479,
                0.0), new OTSPoint3D(-139.98326019712502, -544.7909409269257, 0.0), new OTSPoint3D(-140.08325034739056,
                -544.7895378774629, 0.0), new OTSPoint3D(-140.18324217335015, -544.7882598167514, 0.0), new OTSPoint3D(
                -140.28323551876667, -544.7871067467883, 0.0), new OTSPoint3D(-140.38323022740042, -544.7860786693752,
                0.0), new OTSPoint3D(-140.48322614300977, -544.7851755861186, 0.0), new OTSPoint3D(-140.58322310935108,
                -544.7843974984295, 0.0), new OTSPoint3D(-140.68322097017915, -544.7837444075236, 0.0), new OTSPoint3D(
                -140.7832195692473, -544.7832163144215, 0.0), new OTSPoint3D(-140.88321875030778, -544.7828132199481,
                0.0), new OTSPoint3D(-140.98321835711187, -544.7825351247336, 0.0), new OTSPoint3D(-141.0832182334102,
                -544.7823820292122, 0.0), new OTSPoint3D(-141.18321822295303, -544.7823539336232, 0.0), new OTSPoint3D(
                -141.28321816949028, -544.7824508380106, 0.0), new OTSPoint3D(-141.38321791677217, -544.7826727422229,
                0.0), new OTSPoint3D(-141.48321730854906, -544.7830196459133, 0.0), new OTSPoint3D(-141.58321618857192,
                -544.7834915485399, 0.0), new OTSPoint3D(-141.68321440059256, -544.7840884493653, 0.0), new OTSPoint3D(
                -141.7832117883638, -544.7848103474569, 0.0), new OTSPoint3D(-141.88320819563967, -544.7856572416866,
                0.0), new OTSPoint3D(-141.98320346617584, -544.7866291307313, 0.0), new OTSPoint3D(-142.08319744372974,
                -544.7877260130723, 0.0), new OTSPoint3D(-142.1831899720608, -544.7889478869957, 0.0), new OTSPoint3D(
                -142.28318089493067, -544.7902947505925, 0.0), new OTSPoint3D(-142.3831700561036, -544.7917666017579,
                0.0), new OTSPoint3D(-142.48315729934654, -544.7933634381925, 0.0), new OTSPoint3D(-142.58314246842943,
                -544.7950852574011, 0.0), new OTSPoint3D(-142.68312540712546, -544.7969320566932, 0.0), new OTSPoint3D(
                -142.78310595921133, -544.7989038331833, 0.0), new OTSPoint3D(-142.8830839684674, -544.8010005837906,
                0.0), new OTSPoint3D(-142.9830592786781, -544.8032223052389, 0.0), new OTSPoint3D(-143.08303173363203,
                -544.8055689940566, 0.0), new OTSPoint3D(-143.1830011771222, -544.8080406465771, 0.0), new OTSPoint3D(
                -143.28296745294642, -544.8106372589384, 0.0), new OTSPoint3D(-143.3829304049074, -544.8133588270834,
                0.0), new OTSPoint3D(-143.48288987681303, -544.8162053467596, 0.0), new OTSPoint3D(-143.58284571247668,
                -544.8191768135193, 0.0), new OTSPoint3D(-143.68279775571733, -544.8222732227196, 0.0), new OTSPoint3D(
                -143.78274585036, -544.8254945695223, 0.0),
                new OTSPoint3D(-143.88268984023574, -544.8288408488941, 0.0), new OTSPoint3D(-143.98262956918217,
                    -544.8323120556064, 0.0), new OTSPoint3D(-144.0825648810434, -544.8359081842356, 0.0),
                new OTSPoint3D(-144.18249561967056, -544.8396292291625, 0.0), new OTSPoint3D(-144.2824216289219,
                    -544.8434751845731, 0.0), new OTSPoint3D(-144.38234275266305, -544.847446044458, 0.0),
                new OTSPoint3D(-144.48225883476726, -544.8515418026129, 0.0), new OTSPoint3D(-144.58216971911568,
                    -544.8557624526381, 0.0), new OTSPoint3D(-144.68207524959763, -544.8601079879389, 0.0),
                new OTSPoint3D(-144.7819752701106, -544.8645784017253, 0.0), new OTSPoint3D(-144.88186962456095,
                    -544.8691736870123, 0.0), new OTSPoint3D(-144.98175815686372, -544.8738938366197, 0.0),
                new OTSPoint3D(-145.0816407109431, -544.8787388431724, 0.0), new OTSPoint3D(-145.18151713073263,
                    -544.8837086991001, 0.0), new OTSPoint3D(-145.2813872601755, -544.8888033966373, 0.0),
                new OTSPoint3D(-145.3812509432245, -544.8940229278235, 0.0), new OTSPoint3D(-145.48110802384275,
                    -544.8993672845032, 0.0), new OTSPoint3D(-145.58095834600357, -544.9048364583259, 0.0),
                new OTSPoint3D(-145.68080175369082, -544.9104304407462, 0.0), new OTSPoint3D(-145.7806380908992,
                    -544.9161492230231, 0.0), new OTSPoint3D(-145.8804672016345, -544.9219927962213, 0.0),
                new OTSPoint3D(-145.98028892991368, -544.9279611512101, 0.0), new OTSPoint3D(-146.0801031197654,
                    -544.934054278664, 0.0), new OTSPoint3D(-146.17990961522992, -544.9402721690625, 0.0),
                new OTSPoint3D(-146.2797082603597, -544.9466148126901, 0.0), new OTSPoint3D(-146.37949889921927,
                    -544.9530821996364, 0.0), new OTSPoint3D(-146.47928137588588, -544.9596743197961, 0.0),
                new OTSPoint3D(-146.57905553444937, -544.9663911628691, 0.0), new OTSPoint3D(-146.67882121901263,
                    -544.9732327183604, 0.0), new OTSPoint3D(-146.77857827369186, -544.9801989755798, 0.0),
                new OTSPoint3D(-146.87832654261663, -544.9872899236427, 0.0), new OTSPoint3D(-146.97806586993033,
                    -544.9945055514696, 0.0), new OTSPoint3D(-147.0777960997902, -545.001845847786, 0.0),
                new OTSPoint3D(-147.17751707636785, -545.0093108011225, 0.0), new OTSPoint3D(-147.27722864384927,
                    -545.0169003998153, 0.0), new OTSPoint3D(-147.37693064643514, -545.0246146320056, 0.0),
                new OTSPoint3D(-147.47662292834113, -545.0324534856402, 0.0), new OTSPoint3D(-147.57630533379802,
                    -545.0404169484702, 0.0), new OTSPoint3D(-147.67597770705208, -545.0485050080534, 0.0),
                new OTSPoint3D(-147.7756398923653, -545.0567176517519, 0.0), new OTSPoint3D(-147.87529173401546,
                    -545.0650548667334, 0.0), new OTSPoint3D(-147.97493307629662, -545.0735166399711, 0.0),
                new OTSPoint3D(-148.07456376351922, -545.0821029582435, 0.0), new OTSPoint3D(-148.1741836400103,
                    -545.0908138081345, 0.0), new OTSPoint3D(-148.27379255011385, -545.0996491760332, 0.0),
                new OTSPoint3D(-148.37339033819092, -545.1086090481344, 0.0), new OTSPoint3D(-148.47297684862002,
                    -545.1176934104385, 0.0), new OTSPoint3D(-148.57255192579726, -545.1269022487511, 0.0),
                new OTSPoint3D(-148.67211541413658, -545.1362355486832, 0.0), new OTSPoint3D(-148.77166715807007,
                    -545.1456932956519, 0.0), new OTSPoint3D(-148.87120700204815, -545.1552754748791, 0.0),
                new OTSPoint3D(-148.9707347905398, -545.1649820713927, 0.0), new OTSPoint3D(-149.0702503680329,
                    -545.1748130700264, 0.0), new OTSPoint3D(-149.16975357903436, -545.1847684554191, 0.0),
                new OTSPoint3D(-149.2692442680705, -545.1948482120155, 0.0), new OTSPoint3D(-149.368722279687,
                    -545.205052324066, 0.0), new OTSPoint3D(-149.46818745844962, -545.2153807756266, 0.0),
                new OTSPoint3D(-149.567639648944, -545.2258335505592, 0.0), new OTSPoint3D(-149.6670786957761,
                    -545.2364106325315, 0.0), new OTSPoint3D(-149.76650444357242, -545.2471120050163, 0.0),
                new OTSPoint3D(-149.86591673698024, -545.2579376512932, 0.0), new OTSPoint3D(-149.96531542066793,
                    -545.268887554447, 0.0), new OTSPoint3D(-150.06470033932501, -545.2799616973684, 0.0),
                new OTSPoint3D(-150.1640713376626, -545.2911600627541, 0.0), new OTSPoint3D(-150.26342826041352,
                    -545.3024826331067, 0.0), new OTSPoint3D(-150.3627709523326, -545.3139293907345, 0.0),
                new OTSPoint3D(-150.46209925819687, -545.3255003177524, 0.0), new OTSPoint3D(-150.56141302280594,
                    -545.3371953960801, 0.0), new OTSPoint3D(-150.66071209098203, -545.3490146074447, 0.0),
                new OTSPoint3D(-150.7599963075704, -545.3609579333784, 0.0), new OTSPoint3D(-150.85926551743938,
                    -545.3730253552196, 0.0), new OTSPoint3D(-150.95851956548103, -545.3852168541134, 0.0),
                new OTSPoint3D(-151.05775829661076, -545.3975324110104, 0.0), new OTSPoint3D(-151.15698155576814,
                    -545.4099720066673, 0.0), new OTSPoint3D(-151.25618918791685, -545.4225356216475, 0.0),
                new OTSPoint3D(-151.35538103804492, -545.4352232363202, 0.0), new OTSPoint3D(-151.4545569511652,
                    -545.448034830861, 0.0), new OTSPoint3D(-151.55371677231528, -545.460970385252, 0.0),
                new OTSPoint3D(-151.652860346558, -545.4740298792813, 0.0), new OTSPoint3D(-151.75198751898154,
                    -545.4872132925433, 0.0), new OTSPoint3D(-151.85109813469967, -545.5005206044389, 0.0),
                new OTSPoint3D(-151.95019203885212, -545.5139517941758, 0.0), new OTSPoint3D(-152.04926907660465,
                    -545.5275068407674, 0.0), new OTSPoint3D(-152.14832909314944, -545.5411857230341, 0.0),
                new OTSPoint3D(-152.24737193370524, -545.5549884196025, 0.0), new OTSPoint3D(-152.3463974435176,
                    -545.5689149089062, 0.0), new OTSPoint3D(-152.4454054678592, -545.5829651691847, 0.0),
                new OTSPoint3D(-152.54439585203, -545.5971391784847, 0.0), new OTSPoint3D(-152.64336844135755,
                    -545.6114369146592, 0.0), new OTSPoint3D(-152.74232308119724, -545.625858355368, 0.0),
                new OTSPoint3D(-152.84125961693243, -545.6404034780777, 0.0), new OTSPoint3D(-152.9401778939748,
                    -545.6550722600615, 0.0), new OTSPoint3D(-153.03907775776457, -545.6698646783994, 0.0),
                new OTSPoint3D(-153.13795905377074, -545.6847807099783, 0.0), new OTSPoint3D(-153.23682162749128,
                    -545.6998203314919, 0.0), new OTSPoint3D(-153.33566532445343, -545.7149835194407, 0.0),
                new OTSPoint3D(-153.434489990214, -545.7302702501322, 0.0), new OTSPoint3D(-153.53329547035938,
                    -545.7456804996812, 0.0), new OTSPoint3D(-153.63208161050608, -545.7612142440088, 0.0),
                new OTSPoint3D(-153.73084825630076, -545.7768714588436, 0.0), new OTSPoint3D(-153.82959525342056,
                    -545.7926521197215, 0.0), new OTSPoint3D(-153.92832244757332, -545.808556201985, 0.0),
                new OTSPoint3D(-154.02702968449785, -545.8245836807839, 0.0), new OTSPoint3D(-154.12571680996405,
                    -545.8407345310753, 0.0), new OTSPoint3D(-154.22438366977332, -545.8570087276237, 0.0),
                new OTSPoint3D(-154.32303010975875, -545.8734062450004, 0.0), new OTSPoint3D(-154.42165597578528,
                    -545.8899270575845, 0.0), new OTSPoint3D(-154.52026111375, -545.9065711395622, 0.0),
                new OTSPoint3D(-154.6188453695824, -545.9233384649269, 0.0), new OTSPoint3D(-154.71740858924463,
                    -545.94022900748, 0.0), new OTSPoint3D(-154.81595061873168, -545.9572427408298, 0.0),
                new OTSPoint3D(-154.91447130407158, -545.9743796383924, 0.0), new OTSPoint3D(-155.01297049132586,
                    -545.9916396733913, 0.0), new OTSPoint3D(-155.11144802658953, -546.0090228188579, 0.0),
                new OTSPoint3D(-155.20990375599146, -546.026529047631, 0.0), new OTSPoint3D(-155.3083375256946,
                    -546.044158332357, 0.0), new OTSPoint3D(-155.4067491818962, -546.0619106454902, 0.0),
                new OTSPoint3D(-155.50513857082805, -546.0797859592926, 0.0), new OTSPoint3D(-155.60350553875676,
                    -546.0977842458342, 0.0), new OTSPoint3D(-155.701849931984, -546.1159054769923, 0.0),
                new OTSPoint3D(-155.8001715968466, -546.1341496244529, 0.0), new OTSPoint3D(-155.89847037971708,
                    -546.1525166597092, 0.0), new OTSPoint3D(-155.99674612700352, -546.171006554063, 0.0),
                new OTSPoint3D(-156.0949986851501, -546.1896192786236, 0.0), new OTSPoint3D(-156.19322790063725,
                    -546.2083548043087, 0.0), new OTSPoint3D(-156.29143361998186, -546.227213101844, 0.0),
                new OTSPoint3D(-156.38961568973744, -546.2461941417636, 0.0), new OTSPoint3D(-156.4877739564946,
                    -546.2652978944094, 0.0), new OTSPoint3D(-156.585908266881, -546.2845243299319, 0.0),
                new OTSPoint3D(-156.68401846756186, -546.3038734182899, 0.0), new OTSPoint3D(-156.78210440523998,
                    -546.3233451292501, 0.0), new OTSPoint3D(-156.88016592665613, -546.3429394323884, 0.0),
                new OTSPoint3D(-156.97820287858914, -546.3626562970885, 0.0), new OTSPoint3D(-157.07621510785637,
                    -546.3824956925428, 0.0), new OTSPoint3D(-157.17420246131368, -546.4024575877521, 0.0),
                new OTSPoint3D(-157.27216478585586, -546.4225419515262, 0.0), new OTSPoint3D(-157.37010192841683,
                    -546.4427487524832, 0.0), new OTSPoint3D(-157.46801373596978, -546.46307795905, 0.0),
                new OTSPoint3D(-157.56590005552755, -546.4835295394621, 0.0), new OTSPoint3D(-157.66376073414278,
                    -546.504103461764, 0.0), new OTSPoint3D(-157.76159561890822, -546.524799693809, 0.0),
                new OTSPoint3D(-157.8594045569568, -546.5456182032591, 0.0), new OTSPoint3D(-157.95718739546214,
                    -546.5665589575855, 0.0), new OTSPoint3D(-158.05494398163856, -546.5876219240682, 0.0),
                new OTSPoint3D(-158.15267416274142, -546.6088070697964, 0.0), new OTSPoint3D(-158.2503777860673,
                    -546.6301143616682, 0.0), new OTSPoint3D(-158.34805469895434, -546.6515437663911, 0.0),
                new OTSPoint3D(-158.44570474878236, -546.6730952504817, 0.0), new OTSPoint3D(-158.54332778297322,
                    -546.6947687802656, 0.0), new OTSPoint3D(-158.6409236489909, -546.716564321878, 0.0),
                new OTSPoint3D(-158.7384921943419, -546.7384818412634, 0.0), new OTSPoint3D(-158.83603326657538,
                    -546.7605213041757, 0.0), new OTSPoint3D(-158.93354671328345, -546.7826826761781, 0.0),
                new OTSPoint3D(-159.03103238210136, -546.8049659226436, 0.0), new OTSPoint3D(-159.1284901207078,
                    -546.8273710087547, 0.0), new OTSPoint3D(-159.225919776825, -546.8498978995033, 0.0),
                new OTSPoint3D(-159.32332119821922, -546.8725465596913, 0.0), new OTSPoint3D(-159.4206942327007,
                    -546.8953169539299, 0.0), new OTSPoint3D(-159.51803872812414, -546.9182090466406, 0.0),
                new OTSPoint3D(-159.61535453238878, -546.9412228020545, 0.0), new OTSPoint3D(-159.71264149343864,
                    -546.9643581842125, 0.0), new OTSPoint3D(-159.80989945926294, -546.9876151569657, 0.0),
                new OTSPoint3D(-159.90712827789608, -547.010993683975, 0.0), new OTSPoint3D(-160.00432779741806,
                    -547.0344937287114, 0.0), new OTSPoint3D(-160.10149786595466, -547.0581152544562, 0.0),
                new OTSPoint3D(-160.19863833167767, -547.0818582243007, 0.0), new OTSPoint3D(-160.2957490428051,
                    -547.1057226011466, 0.0), new OTSPoint3D(-160.39282984760155, -547.1297083477057, 0.0),
                new OTSPoint3D(-160.4898805943782, -547.1538154265004, 0.0), new OTSPoint3D(-160.58690113149333,
                    -547.1780437998632, 0.0), new OTSPoint3D(-160.68389130735238, -547.2023934299375, 0.0),
                new OTSPoint3D(-160.78085097040818, -547.2268642786769, 0.0), new OTSPoint3D(-160.87777996916128,
                    -547.2514563078457, 0.0), new OTSPoint3D(-160.97467815216018, -547.2761694790188, 0.0),
                new OTSPoint3D(-161.07154536800144, -547.3010037535821, 0.0), new OTSPoint3D(-161.1683814653301,
                    -547.3259590927318, 0.0), new OTSPoint3D(-161.26518629283973, -547.3510354574753, 0.0),
                new OTSPoint3D(-161.36195969927286, -547.3762328086307, 0.0), new OTSPoint3D(-161.45870153342102,
                    -547.4015511068272, 0.0), new OTSPoint3D(-161.55541164412512, -547.426990312505, 0.0),
                new OTSPoint3D(-161.65208988027564, -547.4525503859154, 0.0), new OTSPoint3D(-161.74873609081288,
                    -547.4782312871207, 0.0), new OTSPoint3D(-161.8453501247271, -547.5040329759945, 0.0),
                new OTSPoint3D(-161.94193183105892, -547.5299554122216, 0.0), new OTSPoint3D(-162.0384810588995,
                    -547.5559985552984, 0.0), new OTSPoint3D(-162.13499765739058, -547.5821623645322, 0.0),
                new OTSPoint3D(-162.23148147572508, -547.6084467990423, 0.0), new OTSPoint3D(-162.327932363147,
                    -547.6348518177593, 0.0), new OTSPoint3D(-162.4243501689519, -547.6613773794252, 0.0),
                new OTSPoint3D(-162.52073474248692, -547.6880234425938, 0.0), new OTSPoint3D(-162.61708593315126,
                    -547.7147899656309, 0.0), new OTSPoint3D(-162.71340359039613, -547.7416769067133, 0.0),
                new OTSPoint3D(-162.80968756372525, -547.7686842238307, 0.0), new OTSPoint3D(-162.9059377026949,
                    -547.795811874784, 0.0), new OTSPoint3D(-163.0021538569143, -547.8230598171862, 0.0),
                new OTSPoint3D(-163.0983358760457, -547.8504280084622, 0.0), new OTSPoint3D(-163.1944836098047,
                    -547.8779164058496, 0.0), new OTSPoint3D(-163.29059690796055, -547.9055249663976, 0.0),
                new OTSPoint3D(-163.38667562033615, -547.9332536469677, 0.0), new OTSPoint3D(-163.4827195968086,
                    -547.961102404234, 0.0), new OTSPoint3D(-163.5787286873092, -547.9890711946829, 0.0),
                new OTSPoint3D(-163.67470274182375, -548.0171599746129, 0.0), new OTSPoint3D(-163.7706416103928,
                    -548.0453687001356, 0.0), new OTSPoint3D(-163.8665451431119, -548.0736973271746, 0.0),
                new OTSPoint3D(-163.96241319013177, -548.1021458114666, 0.0), new OTSPoint3D(-164.05824560165868,
                    -548.1307141085607, 0.0), new OTSPoint3D(-164.15404222795442, -548.1594021738192, 0.0),
                new OTSPoint3D(-164.24980291933684, -548.1882099624167, 0.0), new OTSPoint3D(-164.34552752617986,
                    -548.2171374293412, 0.0), new OTSPoint3D(-164.4412158989138, -548.2461845293935, 0.0),
                new OTSPoint3D(-164.53686788802563, -548.2753512171876, 0.0), new OTSPoint3D(-164.63248334405907,
                    -548.3046374471504, 0.0), new OTSPoint3D(-164.72806211761502, -548.3340431735223, 0.0),
                new OTSPoint3D(-164.82360405935168, -548.3635683503568, 0.0), new OTSPoint3D(-164.91910901998477,
                    -548.3932129315208, 0.0), new OTSPoint3D(-165.01457685028782, -548.4229768706948, 0.0),
                new OTSPoint3D(-165.11000740109236, -548.4528601213724, 0.0), new OTSPoint3D(-165.20540052328818,
                    -548.4828626368612, 0.0), new OTSPoint3D(-165.30075606782353, -548.5129843702822, 0.0),
                new OTSPoint3D(-165.3960738857054, -548.5432252745702, 0.0), new OTSPoint3D(-165.49135382799972,
                    -548.5735853024739, 0.0), new OTSPoint3D(-165.58659574583157, -548.6040644065556, 0.0),
                new OTSPoint3D(-165.68179949038554, -548.6346625391919, 0.0), new OTSPoint3D(-165.77696491290575,
                    -548.665379652573, 0.0), new OTSPoint3D(-165.87209186469624, -548.6962156987037, 0.0),
                new OTSPoint3D(-165.9671801971212, -548.7271706294023, 0.0), new OTSPoint3D(-166.06222976160512,
                    -548.7582443963021, 0.0), new OTSPoint3D(-166.15724040963306, -548.7894369508501, 0.0),
                new OTSPoint3D(-166.2522119927509, -548.8207482443081, 0.0), new OTSPoint3D(-166.34714436256556,
                    -548.852178227752, 0.0), new OTSPoint3D(-166.44203737074525, -548.8837268520728, 0.0),
                new OTSPoint3D(-166.5368908690197, -548.9153940679754, 0.0), new OTSPoint3D(-166.63170470918024,
                    -548.94717982598, 0.0), new OTSPoint3D(-166.72647874308035, -548.9790840764214, 0.0),
                new OTSPoint3D(-166.82121282263557, -549.011106769449, 0.0), new OTSPoint3D(-166.91590679982392,
                    -549.0432478550275, 0.0), new OTSPoint3D(-167.01056052668613, -549.0755072829365, 0.0),
                new OTSPoint3D(-167.10517385532575, -549.1078850027706, 0.0), new OTSPoint3D(-167.19974663790944,
                    -549.1403809639396, 0.0), new OTSPoint3D(-167.29427872666727, -549.1729951156685, 0.0),
                new OTSPoint3D(-167.3887699738929, -549.2057274069979, 0.0), new OTSPoint3D(-167.4832202319437,
                    -549.2385777867835, 0.0), new OTSPoint3D(-167.57762935324124, -549.2715462036964, 0.0),
                new OTSPoint3D(-167.67199719027124, -549.3046326062237, 0.0), new OTSPoint3D(-167.766323595584,
                    -549.3378369426678, 0.0), new OTSPoint3D(-167.86060842179452, -549.3711591611469, 0.0),
                new OTSPoint3D(-167.95485152158278, -549.4045992095952, 0.0), new OTSPoint3D(-168.04905274769393,
                    -549.4381570357625, 0.0), new OTSPoint3D(-168.1432119529386, -549.4718325872147, 0.0),
                new OTSPoint3D(-168.23732899019308, -549.5056258113337, 0.0), new OTSPoint3D(-168.33140371239944,
                    -549.5395366553179, 0.0), new OTSPoint3D(-168.42543597256602, -549.5735650661812, 0.0),
                new OTSPoint3D(-168.5194256237674, -549.6077109907545, 0.0), new OTSPoint3D(-168.61337251914478,
                    -549.6419743756846, 0.0), new OTSPoint3D(-168.70727651190614, -549.6763551674352, 0.0),
                new OTSPoint3D(-168.8011374553265, -549.7108533122862, 0.0), new OTSPoint3D(-168.8949552027482,
                    -549.7454687563342, 0.0), new OTSPoint3D(-168.988729607581, -549.7802014454926, 0.0),
                new OTSPoint3D(-169.08246052330242, -549.8150513254916, 0.0), new OTSPoint3D(-169.1761478034579,
                    -549.8500183418782, 0.0), new OTSPoint3D(-169.2697913016611, -549.8851024400167, 0.0),
                new OTSPoint3D(-169.36339087159408, -549.9203035650879, 0.0), new OTSPoint3D(-169.45694636700753,
                    -549.9556216620903, 0.0), new OTSPoint3D(-169.55045764172098, -549.9910566758391, 0.0),
                new OTSPoint3D(-169.64392454962314, -550.0266085509672, 0.0), new OTSPoint3D(-169.73734694467194,
                    -550.062277231925, 0.0), new OTSPoint3D(-169.8307246808949, -550.09806266298, 0.0), new OTSPoint3D(
                    -169.92405761238936, -550.1339647882176, 0.0), new OTSPoint3D(-170.0173455933226,
                    -550.1699835515406, 0.0), new OTSPoint3D(-170.1105884779322, -550.2061188966698, 0.0),
                new OTSPoint3D(-170.20378612052616, -550.2423707671435, 0.0), new OTSPoint3D(-170.29693837548317,
                    -550.2787391063184, 0.0), new OTSPoint3D(-170.39004509725288, -550.315223857369, 0.0),
                new OTSPoint3D(-170.48310614035597, -550.3518249632878, 0.0), new OTSPoint3D(-170.57612135938473,
                    -550.3885423668855, 0.0), new OTSPoint3D(-170.66909060900278, -550.4253760107913, 0.0),
                new OTSPoint3D(-170.76201374394572, -550.4623258374525, 0.0), new OTSPoint3D(-170.85489061902118,
                    -550.4993917891351, 0.0), new OTSPoint3D(-170.94772108910905, -550.5365738079236, 0.0),
                new OTSPoint3D(-171.04050500916173, -550.573871835721, 0.0), new OTSPoint3D(-171.13324223420437,
                    -550.6112858142492, 0.0), new OTSPoint3D(-171.2259326193351, -550.6488156850488, 0.0),
                new OTSPoint3D(-171.3185760197252, -550.6864613894795, 0.0), new OTSPoint3D(-171.4111722906194,
                    -550.7242228687198, 0.0), new OTSPoint3D(-171.50372128733594, -550.7621000637674, 0.0),
                new OTSPoint3D(-171.59622286526712, -550.8000929154392, 0.0), new OTSPoint3D(-171.68867687987927,
                    -550.8382013643715, 0.0), new OTSPoint3D(-171.78108318671295, -550.8764253510196, 0.0),
                new OTSPoint3D(-171.8734416413833, -550.9147648156588, 0.0), new OTSPoint3D(-171.96575209958038,
                    -550.9532196983835, 0.0), new OTSPoint3D(-172.058014417069, -550.991789939108, 0.0),
                new OTSPoint3D(-183.2636723755513, -556.3855708716345, 0.0), new OTSPoint3D(-183.7248063744403,
                    -556.6224974422428, 0.0), new OTSPoint3D(-184.4647247962342, -557.0026609839204, 0.0),
                new OTSPoint3D(-186.64575105571316, -558.2116382472677, 0.0));
        OTSLine3D buf1 = offsetLine(line3, 1.625);
        System.out.println(buf1.toExcel());
        // OTSLine3D buf2 = offsetLine(line0, 4.0);
        // printExcel(line2);
        // printExcel(buf1);
        // printExcel(buf2);

        // Line2D.Double l1 = new Line2D.Double(0, 0, 10, 0);
        // Line2D.Double l2 = new Line2D.Double(5, 0, 5, 10);
        // List<Line2D.Double> spl1 = splitAtIntersection(l1, l2);
        // print(spl1);
        //
        // Line2D.Double l3 = new Line2D.Double(5, 0, 15, 0);
        // List<Line2D.Double> spl2 = splitAtIntersection(l1, l3);
        // print(spl2);
    }

    private static void printExcel(final OTSLine3D line)
    {
        for (OTSPoint3D p : line.getPoints())
        {
            System.out.println(p.x + "\t" + p.y);
        }
        System.out.println();
    }

    private static final int LINES2PI = 128;

    public static OTSLine3D offsetLine(final OTSLine3D line, final double offset) throws OTSGeometryException
    {
        // if offset extremely small: return (immutable) copy of the original line
        if (offset < 0.00001)
        {
            return line;
        }

        List<Point2D.Double> orig = new ArrayList<>();
        List<Line2D.Double> lines = new ArrayList<>();
        List<Path2D.Double> rects = new ArrayList<>();
        List<Path2D.Double> circs = new ArrayList<>();
        double sign = (offset < 0) ? -1.0 : 1.0;
        double pi2 = Math.PI / 2.0;

        // define the two points that SHOULD ALWAYS be on the line
        Point2D.Double startPoint = null;
        Point2D.Double endPoint = null;

        // for each line segment, define a line segment at the offset; also define a rectangle
        for (int i = 0; i < line.size() - 1; i++)
        {
            Point2D.Double p1 = new Point2D.Double(line.get(i).x, line.get(i).y);
            Point2D.Double p2 = new Point2D.Double(line.get(i + 1).x, line.get(i + 1).y);
            double p1x = p1.x;
            double p1y = p1.y;
            double p2x = p2.x;
            double p2y = p2.y;
            double angle = Math.atan2(p2y - p1y, p2x - p1x);
            double osina = sign * offset * Math.sin(angle + sign * pi2);
            double ocosa = sign * offset * Math.cos(angle + sign * pi2);
            Point2D.Double o1 = new Point2D.Double(p1x + ocosa, p1y + osina);
            Point2D.Double o2 = new Point2D.Double(p2x + ocosa, p2y + osina);
            lines.add(new Line2D.Double(o1, o2));
            rects.add(makeRectangle(p1, p2, o2, o1));

            // assign the two points that SHOULD ALWAYS be on the line
            if (i == 0)
            {
                startPoint = o1;
            }
            if (i == line.size() - 2)
            {
                endPoint = o2;
            }
        }

        for (int i = 0; i < line.size(); i++)
        {
            orig.add(new Point2D.Double(line.get(i).x, line.get(i).y));
        }

        // for each subsequent line segment, draw an arc as line segments based on the angles
        for (int i = 0; i < orig.size() - 2; i++)
        {
            Point2D.Double p1 = orig.get(i);
            Point2D.Double p2 = orig.get(i + 1);
            Point2D.Double p3 = orig.get(i + 2);

            // test if the line and its successor have an angle > pi towards each other
            double angle1 = norm(Math.atan2(p2.y - p1.y, p2.x - p1.x));
            double angle2 = norm(Math.atan2(p3.y - p2.y, p3.x - p2.x));
            if (angle1 != angle2 && norm(sign * (angle2 - angle1)) > Math.PI)
            {
                // make an arc between the points; O = p2; leave out first and last point!
                int numPoints = (int) Math.ceil(LINES2PI * norm(Math.abs(angle2 - angle1)) / (2.0 * Math.PI)) + 1;
                Point2D.Double[] arc = new Point2D.Double[numPoints + 1];
                arc[0] = new Point2D.Double(lines.get(i).x2, lines.get(i).y2);
                arc[arc.length - 1] = new Point2D.Double(lines.get(i + 1).x1, lines.get(i + 1).y1);
                for (int j = 1; j < numPoints; j++)
                {
                    double angle = angle1 + sign * pi2 + (angle2 - angle1) * (1.0 * j / numPoints);
                    arc[j] =
                        new Point2D.Double(p2.x + sign * offset * Math.cos(angle), p2.y + sign * offset
                            * Math.sin(angle));
                }

                for (int j = 0; j < arc.length - 1; j++)
                {
                    lines.add(new Line2D.Double(arc[j], arc[j + 1]));
                }

                Path2D.Double circ = new Path2D.Double();
                circ.moveTo(p2.x, p2.y);
                for (int j = 0; j <= numPoints; j++)
                {
                    double angle = angle1 + sign * pi2 + (angle2 - angle1) * (1.0 * j / numPoints);
                    circ.lineTo(p2.x + sign * offset * Math.cos(angle), p2.y + sign * offset * Math.sin(angle));
                }
                circ.closePath();
                circs.add(circ);
            }
        }

        // add the 'cube' at both ends of the line as a no-go area.
        Point2D.Double po1 = orig.get(0);
        Point2D.Double po2 = orig.get(1);
        double angle1 = norm(Math.atan2(po2.y - po1.y, po2.x - po1.x) + ((sign > 0) ? Math.PI : 0));
        Point2D.Double p1 =
            new Point2D.Double(po1.x + offset * Math.cos(angle1 - pi2), po1.y + offset * Math.sin(angle1 - pi2));
        Point2D.Double p2 =
            new Point2D.Double(po1.x + offset * Math.cos(angle1 + pi2), po1.y + offset * Math.sin(angle1 + pi2));
        Point2D.Double p3 = new Point2D.Double(p2.x + offset * Math.cos(angle1), p2.y + offset * Math.sin(angle1));
        Point2D.Double p4 = new Point2D.Double(p1.x + offset * Math.cos(angle1), p1.y + offset * Math.sin(angle1));
        rects.add(makeRectangle(p1, p2, p3, p4));

        po1 = orig.get(orig.size() - 1);
        po2 = orig.get(orig.size() - 2);
        double angle2 = norm(Math.atan2(po2.y - po1.y, po2.x - po1.x) + ((sign > 0) ? Math.PI : 0));
        p1 = new Point2D.Double(po1.x + offset * Math.cos(angle2 - pi2), po1.y + offset * Math.sin(angle2 - pi2));
        p2 = new Point2D.Double(po1.x + offset * Math.cos(angle2 + pi2), po1.y + offset * Math.sin(angle2 + pi2));
        p3 = new Point2D.Double(p2.x + offset * Math.cos(angle2), p2.y + offset * Math.sin(angle2));
        p4 = new Point2D.Double(p1.x + offset * Math.cos(angle2), p1.y + offset * Math.sin(angle2));
        rects.add(makeRectangle(p1, p2, p3, p4));

        // determine all crossing lines and split both at the crossing point.
        List<Line2D.Double> lines2 = new ArrayList<>(lines);
        while (!lines2.isEmpty())
        {
            boolean crossed = false;
            Line2D.Double line1 = lines2.get(0);
            for (int i = 1; i < lines2.size() && !crossed; i++)
            {
                Line2D.Double line2 = lines2.get(i);
                List<Line2D.Double> splitLines = splitAtIntersection(line1, line2);
                if (splitLines != null)
                {
                    if (splitLines.size() != 0) // if == 0: two of the same lines; remove one of them
                    {
                        lines2.addAll(splitLines);
                        lines.remove(line1);
                        lines.addAll(splitLines);
                    }
                    lines.remove(line2);
                    lines2.remove(line2);
                    crossed = true;
                }
            }
            lines2.remove(0);
        }

        // throw out all lines that cross the center line or come too close a point of the center line (10% of the offset)
        List<Line2D.Double> remove = new ArrayList<>();
        for (int i = 0; i < orig.size() - 1; i++)
        {
            Line2D.Double o = new Line2D.Double(orig.get(i), orig.get(i + 1));
            for (Line2D.Double l : lines)
            {
                if (o.intersectsLine(l) || o.getP1().distance(l.getP1()) < 0.1 * sign * offset
                    || o.getP1().distance(l.getP2()) < 0.1 * sign * offset
                    || o.getP2().distance(l.getP1()) < 0.1 * sign * offset
                    || o.getP2().distance(l.getP2()) < 0.1 * sign * offset)
                {
                    remove.add(l);
                }
            }
        }
        lines.removeAll(remove);

        // print(lines);

        // throw out all lines that are 'inside' the rectangles with one of their points
        for (Path2D.Double rect : rects)
        {
            List<Line2D.Double> contour = getContour(rect);
            remove = new ArrayList<>();
            for (Line2D.Double l : lines)
            {
                if (inside(rect, l, contour))
                {
                    remove.add(l);
                    // System.out.println("Removed " + p(l) + " from " + p(contour));
                }
            }
            lines.removeAll(remove);
        }

        // throw out all lines that are 'inside' the circles around each center line
        for (Path2D.Double circle : circs)
        {
            List<Line2D.Double> contour = getContour(circle);
            remove = new ArrayList<>();
            for (Line2D.Double l : lines)
            {
                if (inside(circle, l, contour))
                {
                    remove.add(l);
                }
            }
            lines.removeAll(remove);
        }

        // print(lines);
        /*-
         for (Path2D.Double rect : rects)
         {
             print(rect);
         }
         for (Path2D.Double circ : circs)
         {
             print(circ);
         }
         */

        // walk through the line segments and string them together.
        List<Point2D> offsetLine = new ArrayList<>();
        Line2D.Double l0 = lines.remove(0);
        offsetLine.add(l0.getP1());
        offsetLine.add(l0.getP2());
        while (!lines.isEmpty())
        {
            boolean found = false;
            Point2D ps = offsetLine.get(0);
            Point2D pe = offsetLine.get(offsetLine.size() - 1);
            for (int i = 0; i < lines.size() && !found; i++)
            {
                Line2D.Double l = lines.get(i);
                if (l.getP1().equals(ps))
                {
                    offsetLine.add(0, l.getP2());
                    lines.remove(i);
                    found = true;
                }
                if (l.getP2().equals(ps))
                {
                    offsetLine.add(0, l.getP1());
                    lines.remove(i);
                    found = true;
                }
                if (l.getP1().equals(pe))
                {
                    offsetLine.add(l.getP2());
                    lines.remove(i);
                    found = true;
                }
                if (l.getP2().equals(pe))
                {
                    offsetLine.add(l.getP1());
                    lines.remove(i);
                    found = true;
                }
            }
            if (!found)
            {
                System.err.println("#offsetLine: Problem connecting one or more points");
                // System.out.println(toJava(line));
                lines.clear();
                offsetLine.clear();
                offsetLine.add(startPoint);
                offsetLine.add(endPoint);
            }
        }

        // see if the start and end points are on the line
        double ss = startPoint.distance(offsetLine.get(0));
        double ee = endPoint.distance(offsetLine.get(offsetLine.size() - 1));
        double es = endPoint.distance(offsetLine.get(0));
        double se = startPoint.distance(offsetLine.get(offsetLine.size() - 1));

        // reverse if necessary
        if (ss == 0.0 && ee == 0.0)
        {
            return makeOTSLine3D(offsetLine);
        }
        if (es == 0.0 && se == 0.0)
        {
            return makeOTSLine3D(offsetLine).reverse();
        }

        // start point and/or end point are missing...
        if (ss > 0.0 && se > 0.0)
        {
            // start is not connected
            if (ss < se)
            {
                offsetLine.add(0, startPoint);
            }
            else
            {
                offsetLine.add(startPoint);
            }
        }
        if (ee > 0.0 && es > 0.0)
        {
            // end is not connected
            if (ee < es)
            {
                offsetLine.add(endPoint);
            }
            else
            {
                offsetLine.add(0, endPoint);
            }
        }

        // reverse if necessary
        if (startPoint.distance(offsetLine.get(0)) == 0.0
            && endPoint.distance(offsetLine.get(offsetLine.size() - 1)) == 0.0)
        {
            return makeOTSLine3D(offsetLine);
        }
        else
        {
            return makeOTSLine3D(offsetLine).reverse();
        }
    }

    private static String toJava(final OTSLine3D line)
    {
        StringBuffer s = new StringBuffer();
        s.append("  OTSLine3D line = new OTSLine3D(");
        boolean first = false;
        for (OTSPoint3D p : line.getPoints())
        {
            if (!first)
            {
                first = true;
                s.append("\n      ");
            }
            else
            {
                s.append("\n    , ");
            }
            s.append("new OTSPoint3D(" + p.x + ", " + p.y + ", " + p.z + ")");
        }
        s.append(" );");
        return s.toString();
    }

    private static OTSLine3D makeOTSLine3D(List<Point2D> points) throws OTSGeometryException
    {
        List<OTSPoint3D> otsPoints = new ArrayList<>();
        for (Point2D point : points)
        {
            otsPoints.add(new OTSPoint3D(point.getX(), point.getY(), 0.0));
        }
        return new OTSLine3D(otsPoints);
    }

    private static Path2D.Double makeRectangle(Point2D.Double p1, Point2D.Double p2, Point2D.Double p3,
        Point2D.Double p4)
    {
        Path2D.Double rect = new Path2D.Double();
        rect.moveTo(p1.x, p1.y);
        rect.lineTo(p2.x, p2.y);
        rect.lineTo(p3.x, p3.y);
        rect.lineTo(p4.x, p4.y);
        rect.closePath();
        return rect;
    }

    private static double norm(double angle)
    {
        while (angle < 0)
        {
            angle += 2.0 * Math.PI;
        }
        while (angle > 2.0 * Math.PI)
        {
            angle -= 2.0 * Math.PI;
        }
        return angle;
    }

    private static boolean inside(Path2D.Double shape, Line2D.Double line, List<Line2D.Double> contour)
    {
        if (shape.contains(line.getP1()))
        {
            if (!onContour(contour, line.getP1()))
            {
                return true;
            }
        }
        if (shape.contains(line.getP2()))
        {
            if (!onContour(contour, line.getP2()))
            {
                return true;
            }
        }
        return false;
    }

    private static boolean onContour(List<Line2D.Double> contour, Point2D point)
    {
        for (Line2D.Double l : contour)
        {
            if (l.ptLineDist(point) < 1E-6)
            {
                return true;
            }
        }
        return false;
    }

    private static List<Line2D.Double> getContour(Path2D.Double shape)
    {
        List<Line2D.Double> contour = new ArrayList<>();
        PathIterator pi = shape.getPathIterator(null);
        Point2D.Double lastPoint = null;
        Point2D.Double firstPoint = null;
        while (!pi.isDone())
        {
            double[] p = new double[6];
            int segtype = pi.currentSegment(p);
            if (segtype == PathIterator.SEG_MOVETO)
            {
                lastPoint = new Point2D.Double(p[0], p[1]);
                firstPoint = lastPoint;
            }
            if (segtype == PathIterator.SEG_LINETO && lastPoint != null)
            {
                Point2D.Double newPoint = new Point2D.Double(p[0], p[1]);
                contour.add(new Line2D.Double(lastPoint, newPoint));
                lastPoint = newPoint;
            }
            if (segtype == PathIterator.SEG_CLOSE && firstPoint != null)
            {
                contour.add(new Line2D.Double(lastPoint, firstPoint));
            }
            pi.next();
        }
        return contour;
    }

    private static List<Line2D.Double> splitAtIntersection(Line2D.Double line1, Line2D.Double line2)
    {
        if (!line1.intersectsLine(line2))
            return null;

        double p1x = line1.getX1(), p1y = line1.getY1(), d1x = line1.getX2() - p1x, d1y = line1.getY2() - p1y;
        double p2x = line2.getX1(), p2y = line2.getY1(), d2x = line2.getX2() - p2x, d2y = line2.getY2() - p2y;

        double det = d2x * d1y - d2y * d1x;
        if (det == 0)
        {
            /*- lines (partially) overlap, indicate 0, 1 or 2 (!) cross points
             situations:
             X============X        X============X        X============X        X=======X      X====X  
                    X---------X       X------X           X----X                X-------X           X----X
             a. 2 intersections    b. 2 intersections    c. 1 intersection     d. 0 inters.   e. 0 inters.
             */
            Point2D p1s = line1.getP1(), p1e = line1.getP2(), p2s = line2.getP1(), p2e = line2.getP2();
            List<Line2D.Double> lines = new ArrayList<>();
            if ((p1s.equals(p2s) && p1e.equals(p2e)) || (p1s.equals(p2e) && p1e.equals(p2s)))
                return lines; // situation d.
            if (p1s.equals(p2s) && line1.ptLineDist(p2e) > 0 && line2.ptLineDist(p1e) > 0)
                return null; // situation e.
            if (p1e.equals(p2e) && line1.ptLineDist(p2s) > 0 && line2.ptLineDist(p1s) > 0)
                return null; // situation e.
            if (p1s.equals(p2e) && line1.ptLineDist(p2s) > 0 && line2.ptLineDist(p1e) > 0)
                return null; // situation e.
            if (p1e.equals(p2s) && line1.ptLineDist(p2e) > 0 && line2.ptLineDist(p1s) > 0)
                return null; // situation e.

            // situation a, b or c; create an ordered list of 4 points, based on distance
            SortedMap<Double, Point2D> pointMap = new TreeMap<>();
            pointMap.put(0.0, p1s);
            pointMap.put(p1s.distance(p1e), p1e);
            pointMap.put(p1s.distance(p2s), p2s);
            pointMap.put(p1s.distance(p2e), p2e);
            List<Point2D> ptList = new ArrayList<>(pointMap.values());
            for (int i = 0; i < ptList.size() - 1; i++)
                lines.add(new Line2D.Double(ptList.get(i), ptList.get(i + 1)));
            return lines;
        }
        else
        {
            double z = (d2x * (p2y - p1y) + d2y * (p1x - p2x)) / det;
            if (Math.abs(z) < 10.0 * Math.ulp(1.0) || Math.abs(z - 1.0) < 10.0 * Math.ulp(1.0))
            {
                return null; // intersection at end point
            }
            Point2D.Double cross = new Point2D.Double(p1x + z * d1x, p1y + z * d1y);
            List<Line2D.Double> lines = new ArrayList<>();
            if (cross.distance(line1.getP1()) > 0)
                lines.add(new Line2D.Double(line1.getP1(), cross));
            if (cross.distance(line1.getP2()) > 0)
                lines.add(new Line2D.Double(cross, line1.getP2()));
            if (cross.distance(line2.getP1()) > 0)
                lines.add(new Line2D.Double(line2.getP1(), cross));
            if (cross.distance(line2.getP2()) > 0)
                lines.add(new Line2D.Double(cross, line2.getP2()));
            return lines;
        }
    }

    private static void print(Path2D.Double shape)
    {
        PathIterator pi = shape.getPathIterator(null);
        Point2D.Double pf = null;
        while (!pi.isDone())
        {
            double[] p = new double[6];
            int segtype = pi.currentSegment(p);
            if (segtype == PathIterator.SEG_MOVETO || segtype == PathIterator.SEG_LINETO)
            {
                System.out.println(p[0] + "\t" + p[1]);
                if (pf == null)
                {
                    pf = new Point2D.Double(p[0], p[1]);
                }
            }
            pi.next();
        }
        System.out.println(pf.x + "\t" + pf.y);
        System.out.println();
    }

    private static void print(List<Line2D.Double> lines)
    {
        if (lines.size() == 0)
        {
            System.out.println("<<none>>");
            return;
        }
        for (Line2D.Double line : lines)
        {
            System.out.println(line.x1 + "\t" + line.y1);
            System.out.println(line.x2 + "\t" + line.y2);
            System.out.println();
        }
    }

    private static String p(Line2D.Double l)
    {
        return "[(" + l.x1 + "," + l.y1 + ")->(" + l.x2 + "," + l.y2 + ")]";
    }

    private static String p(List<Line2D.Double> c)
    {
        String s = "contour [";
        for (Line2D.Double l : c)
        {
            s += p(l) + "  ";
        }
        s += "]";
        return s;
    }

}
