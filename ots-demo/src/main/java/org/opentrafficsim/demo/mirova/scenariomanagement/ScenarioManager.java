package org.opentrafficsim.demo.mirova.scenariomanagement;

import java.io.File;
import java.util.*;
import java.util.concurrent.*;

/**
 * ScenarioManager — orchestrates multi-run scenario simulations with multiple seeds,
 * optional parameter variations, output handling and parallel execution.
 */
public class ScenarioManager {

    /** Path to the root output directory. */
    private final File outputRoot;

    /** Registered scenarios with their parameter variations. */
    private final Map<String, ScenarioEntry> scenarios = new LinkedHashMap<>();

    /** Number of replications (= different seeds) per scenario configuration. */
    private int replications = 1;

    /** Constructs a ScenarioManager with the given output root directory.
     * @param outputRoot the root output directory for all scenarios
     */
    public ScenarioManager(final File outputRoot) {
        this.outputRoot = outputRoot;
        if (!outputRoot.exists())
        {
            outputRoot.mkdirs();
            System.out.println("Created new output directory: " + outputRoot.getAbsolutePath());
        }
    }

    // ------------------------------------------------------------
    // Registration API
    // ------------------------------------------------------------

    /** Adds a scenario to the manager.
     * @param name name of the scenario
     * @param generator scenario generator instance
     * */
    public void addScenario(final String name, final ScenarioGenerator generator) {
        this.scenarios.put(name, new ScenarioEntry(generator));
    }

    /** Adds a parameter variation entry for the given scenario.
     * @param scenarioName name of the scenario
     * @param params parameter variation to add
     * */
    public void addParameterVariation(final String scenarioName, final ScenarioParameters params) {
        this.scenarios.get(scenarioName).parameterVariations.add(params);
    }

    /** Sets how many seeds to run per scenario-parameter set.
     * @param replications number of replications
     *
     *  */
    public void setReplications(final int replications) {
        this.replications = replications;
    }

    // ------------------------------------------------------------
    // Main execution logic
    // ------------------------------------------------------------

    /**
     * Runs all scenarios including parameter variations & replications.
     * @param parallelThreads number of parallel workers
     * @throws ExecutionException
     */
    public void runAll(final int parallelThreads) throws InterruptedException, ExecutionException {

        System.out.println("Starting ScenarioManager with " + parallelThreads + " parallel threads...");
        ExecutorService pool = Executors.newFixedThreadPool(parallelThreads);
        List<Future<?>> futures = new ArrayList<>();

        for (Map.Entry<String, ScenarioEntry> entry : this.scenarios.entrySet()) {

            String scenarioName = entry.getKey();
            ScenarioGenerator generator = entry.getValue().generator;
            List<ScenarioParameters> variations = entry.getValue().parameterVariations;
            System.out.println("Preparing scenario: " + scenarioName + " with "
                    + variations.size() + " parameter variations and "
                    + this.replications + " replications each.");

            File scenarioFolder = new File(this.outputRoot, scenarioName);
            scenarioFolder.mkdirs();

            // If no variations added -> use default parameters
            if (variations.isEmpty()) {
                variations.add(generator.getParameters());
            }

            for (ScenarioParameters params : variations) {
                for (int run = 0; run < this.replications; run++) {

                    long seed = params.getSeed() + run;

                    ScenarioParameters runParams = params.copy();
                    runParams.setSeed(seed);

                    File runFolder = new File(scenarioFolder, "run_seed_" + seed);
                    runFolder.mkdirs();

                    ScenarioOutputConfiguration outputConfig =
                            generator.configureOutput().setOutputDirectory(runFolder.getAbsolutePath());

                    ScenarioSimulationScript script =
                            generator.buildSimulationScript(runParams, outputConfig);

                    futures.add(pool.submit(() -> {
                        try {
                            System.out.println("[RUN] " + scenarioName + " | seed=" + seed);
                            script.start();
                            //SimulationRunner.run(script, runParams, true);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }));
                }
            }
        }

        // Wait for all to finish
        for (Future<?> f : futures) f.get();

        pool.shutdown();
        pool.awaitTermination(7, TimeUnit.DAYS);
    }

    // ------------------------------------------------------------
    // Helper structure
    // ------------------------------------------------------------

    /** Internal structure to hold scenario generator and its parameter variations. */
    private static class ScenarioEntry {
        ScenarioGenerator generator;
        List<ScenarioParameters> parameterVariations = new ArrayList<>();

        /** Constructor.
         * @param gen scenario generator
         *  */
        ScenarioEntry(final ScenarioGenerator gen) {
            this.generator = gen;
        }
    }
}
