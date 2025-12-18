package org.opentrafficsim.demo.mirova.scenariomanagement;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.djunits.value.vdouble.scalar.Time;
import org.djutils.data.Table;
import org.djutils.data.csv.CsvData;
import org.djutils.data.serialization.TextSerializationException;
import org.djutils.io.CompressedFileWriter;
import org.opentrafficsim.kpi.sampling.SamplerData;
import org.opentrafficsim.road.network.RoadNetwork;
import org.opentrafficsim.road.network.sampling.RoadSampler;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.object.detector.LoopDetector;

/**
 * ScenarioOutputConfiguration
 * ---------------------------
 *
 * Encapsulates all settings required for recording simulation output.
 * A ScenarioGenerator returns an instance of this class, which is then
 * interpreted by the ScenarioManager to set up RoadSampler, custom
 * output files, and extended data types.
 *
 * The class supports:
 *  - Registering extended KPIs (ExtendedDataXXX)
 *  - Selecting which lanes/paths to monitor
 *  - Starting/stopping recording at specific times
 *  - Defining output directories and filenames
 *  - Arbitrary custom output functions
 *
 */
public class ScenarioOutputConfiguration {

    /** Collects RoadSamplers configured for this scenario. */
    protected final List<RoadSampler> samplers = new ArrayList<>();

    protected final List<LoopDetector> detectors = new ArrayList<>();

    /** Optional custom writers (e.g., KPI summaries). */
    protected final List<Runnable> customWriters = new ArrayList<>();

    /** List of extended data types to register in the sampler. */
    private final List<Object> extendedDataTypes = new ArrayList<>();

    /** List of lanes for which the scenario wants to record KPIs. */
    private final List<Lane> monitoredLanes = new ArrayList<>();

    /** Actions to configure the sampler after creation. */
    private final List<Consumer<RoadSampler>> samplerPostProcessors = new ArrayList<>();

    /** Output directory for generated files. */
    private String outputDirectory = null;

    /** Optional override for CSV output filename. */
    private String outputFilename = "trajectory_data.csv";

    /** Time at which sampling starts (default: 0s). */
    private Time recordingStartTime = Time.instantiateSI(0);

    /** Time at which sampling stops (default: entire simulation). */
    private Time recordingEndTime = null;

    /** Reference to the road network (set during scenario setup). */
    private RoadNetwork roadNetwork = null;

    // ----------------------------------------------------------------------
    // Configuration API
    // ----------------------------------------------------------------------

    /**
     * Register an extended data type (e.g. ExtendedDataRelaxedHeadway).
     * @param extendedData instance of the extended data type to register
     * @return this ScenarioOutputConfiguration for chaining
     */
    public ScenarioOutputConfiguration addExtendedDataType(final Object extendedData) {
        this.extendedDataTypes.add(extendedData);
        return this;
    }

    /**
     * register multiple road samplers
     * @param samplers list of RoadSamplers to add
     * @return this ScenarioOutputConfiguration for chaining
     */
    public ScenarioOutputConfiguration addRoadSamplers(final List<RoadSampler> samplers) {
        this.samplers.addAll(samplers);
        return this;
    }

    /**
     * Register a lane whose vehicle trajectories should be sampled.
     * @param lane lane to monitor
     * @return this ScenarioOutputConfiguration for chaining
     */
    public ScenarioOutputConfiguration addMonitoredLane(final Lane lane) {
        this.monitoredLanes.add(lane);
        return this;
    }

    /**
     * Register multiple lanes whose vehicle trajectories should be sampled.
     * @param lanes list of lanes to monitor
     * @return this ScenarioOutputConfiguration for chaining
     */
    public ScenarioOutputConfiguration addMonitoredLanes(final List<Lane> lanes) {
        this.monitoredLanes.addAll(lanes);
        return this;
    }

    /**
     * Add a custom sampler post-processor.
     * Allows configuring start/stop recording per lane or adding GraphPath objects.
     * @param config consumer that receives the RoadSampler for configuration
     * @return this ScenarioOutputConfiguration for chaining
     */
    public ScenarioOutputConfiguration modifySampler(final Consumer<RoadSampler> config) {
        this.samplerPostProcessors.add(config);
        return this;
    }

    /**
     * Set output directory for file export.
     * @param dir output directory path
     * @return this ScenarioOutputConfiguration for chaining
     */
    public ScenarioOutputConfiguration setOutputDirectory(final String dir) {
        this.outputDirectory = dir;
        return this;
    }

    /**
     * Set output file name.
     * @param filename output file name
     * @return this ScenarioOutputConfiguration for chaining
     */
    public ScenarioOutputConfiguration setOutputFilename(final String filename) {
        this.outputFilename = filename;
        return this;
    }

    /**
     * Configure start time for KPI recording.
     * @param start start time
     * @return this ScenarioOutputConfiguration for chaining
     */
    public ScenarioOutputConfiguration setRecordingStart(final Time start) {
        this.recordingStartTime = start;
        return this;
    }

    /**
     * Configure end time for KPI recording.
     * @param end end time
     * @return this ScenarioOutputConfiguration for chaining
     */
    public ScenarioOutputConfiguration setRecordingEnd(final Time end) {
        this.recordingEndTime = end;
        return this;
    }

    // ----------------------------------------------------------------------
    // Accessors for ScenarioManager
    // ----------------------------------------------------------------------

    // -------------------------------------------------------------

    /**
     * Returns the list of extended data types to register.
     * @return list of extended data types
     */
    public List<Object> getExtendedDataTypes() {
        return this.extendedDataTypes;
    }

    /**
     * Returns the list of lanes to monitor.
     * @return list of monitored lanes
     */
    public List<Lane> getMonitoredLanes() {
        return this.monitoredLanes;
    }

    /**
     * Returns the list of sampler post-processors.
     * @return list of sampler post-processors
     */
    public List<Consumer<RoadSampler>> getSamplerPostProcessors() {
        return this.samplerPostProcessors;
    }

    /**
     * Returns the output directory.
     * @return output directory
     */
    public String getOutputDirectory() {
        return this.outputDirectory;
    }

    /**
     * Returns the output filename.
     * @return output filename
     */
    public String getOutputFilename() {
        return this.outputFilename;
    }

    /** Returns the recording start time.
     * @return recording start time
     */
    public Time getRecordingStartTime() {
        return this.recordingStartTime;
    }

    /** Returns the recording end time.
     * @return recording end time
     */
    public Time getRecordingEndTime() {
        return this.recordingEndTime;
    }



    // -------------------------------------------------------------
    // Fluent builder-like API
    // -------------------------------------------------------------

    /** sets the road network.
     * @param roadNetwork road network
     * @return road network
     */
    public ScenarioOutputConfiguration setRoadNetwork(final RoadNetwork roadNetwork)
    {
        this.roadNetwork = roadNetwork;
        return this;
    }

    /** Returns the road network.
     * @return road network
     */
    public RoadNetwork getRoadNetwork()
    {
        return this.roadNetwork;
    }

    /** Adds a RoadSampler to the configuration.
     * @param sampler RoadSampler instance
     * @return this ScenarioOutputConfiguration for chaining
     */
    public ScenarioOutputConfiguration addSampler(final RoadSampler sampler) {
        this.samplers.add(sampler);
        return this;
    }

    /** Adds a LoopDetector to the configuration.
     * @param detector LoopDetector instance
     * @return this ScenarioOutputConfiguration for chaining
     */
    public ScenarioOutputConfiguration addLoopDetector(final LoopDetector detector)
    {
        this.detectors.add(detector);
        return this;
    }

    /** Adds multiple LoopDetectors to the configuration.
     * @param detectors list of LoopDetector instances
     * @return this ScenarioOutputConfiguration for chaining
     */
    public ScenarioOutputConfiguration addLoopDetectors(final List<LoopDetector> detectors)
    {
        this.detectors.addAll(detectors);
        return this;
    }

    /** Adds a custom output writer to the configuration.
     * @param writer Runnable that performs custom output writing
     * @return this ScenarioOutputConfiguration for chaining
     */
    public ScenarioOutputConfiguration addCustomWriter(final Runnable writer) {
        this.customWriters.add(writer);
        return this;
    }

    /** Returns the list of configured RoadSamplers.
     * @return list of RoadSamplers
     */
    public List<RoadSampler> getSamplers() {
        return this.samplers;
    }

    /** Returns the list of configured LoopDetectors.
     * @return list of LoopDetectors
     */
    public List<LoopDetector> getDetectors()
    {
        return this.detectors;
    }

    // -------------------------------------------------------------
    // Central method to write all outputs of this scenario
    // -------------------------------------------------------------

    /** Writes all configured outputs to files.
     * This includes sampler data and any custom writers.
     */
    public void writeAllOutputs() {
        if (this.outputDirectory == null) {
            System.err.println("[WARN] No output directory configured — skipping output writing.");
            return;
        }

        File outDir = new File(this.outputDirectory);
        if (!outDir.exists()) {
            boolean ok = outDir.mkdirs();
            if (!ok) {
                System.err.println("[ERROR] Could not create output directory: " + this.outputDirectory);
                return;
            }
        }

        System.out.println("[OUTPUT] Writing sampler output to: " + outDir.getAbsolutePath());

        // ---------------------------------------------------------
        // Write RoadSampler output
        // ---------------------------------------------------------
        for (RoadSampler sampler : this.samplers) {
            try {
                SamplerData<?> data = sampler.getSamplerData();

                String fileName = "sampler_" + sampler.toString().replace(" ", "_") + ".csv";
                File file = new File(outDir, fileName);

                data.writeToFile(file.getAbsolutePath());

                System.out.println("[OUTPUT]   -> wrote sampler data: " + file.getName());
            }
            catch (Exception ex) {
                System.err.println("[ERROR] Failed to write sampler output: " + ex.getMessage());
                ex.printStackTrace();
            }
        }

        /*
        try
        {
            LoopDetector detector = this.detectors.get(0);

            CompressedFileWriter zippedWriter = new CompressedFileWriter(new File(this.outputDirectory,
                    "detector.zip").getAbsolutePath());
            Table detectorData = detector.asTablePeriodicData(getRoadNetwork());
            CsvData.writeZippedData(zippedWriter, "detector_data.csv", "detector_data.csvm", detectorData);
            System.out.println("[OUTPUT]   -> wrote detector data: " + detector.getId() + "_data.csv");
        }
        catch (IOException | TextSerializationException | IndexOutOfBoundsException exception)
        {
            System.err.println("[ERROR] Failed to write loop detector output: " + exception.getMessage());
            exception.printStackTrace();
        }
        */

        // ---------------------------------------------------------
        // Write additional scenario-level custom exports (optional)
        // ---------------------------------------------------------
        for (Runnable r : this.customWriters) {
            try {
                r.run();
            }
            catch (Exception e) {
                System.err.println("[ERROR] Custom output writer failed: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }



    // ----------------------------------------------------------------------
    // Helper
    // ----------------------------------------------------------------------

    @Override
    public String toString() {
        return "ScenarioOutputConfiguration[" +
                "extendedDataTypes=" + this.extendedDataTypes.size() +
                ", monitoredLanes=" + this.monitoredLanes.size() +
                ", outputDirectory=" + this.outputDirectory +
                ", filename=" + this.outputFilename +
                "]";
    }
}
