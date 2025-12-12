package org.opentrafficsim.demo.mirova.scenariomanagement;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
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
    public void addScenario(final String name, final Class<? extends ScenarioGenerator> scenarioClass) {
        this.scenarios.put(name, new ScenarioEntry(scenarioClass));
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
     * @throws InterruptedException
     * @throws ExecutionException
     * @throws SecurityException
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    public void runAll(final int parallelThreads, final boolean enableGUI) throws InterruptedException, ExecutionException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {

        System.out.println("Starting ScenarioManager with " + parallelThreads + " parallel threads...");
        ExecutorService pool = Executors.newFixedThreadPool(parallelThreads);
        List<Future<?>> futures = new ArrayList<>();

        for (Map.Entry<String, ScenarioEntry> entry : this.scenarios.entrySet()) {

            String scenarioName = entry.getKey();
            Class<? extends ScenarioGenerator> genClass = entry.getValue().generatorClass;
            List<ScenarioParameters> variations = entry.getValue().parameterVariations;

            File scenarioFolder = new File(this.outputRoot, scenarioName);
            scenarioFolder.mkdirs();

            for (ScenarioParameters paramsVariation : variations) {

              // Create unique folder for this variation
              File variationFolder = new File(scenarioFolder, "variation_" + UUID.randomUUID().toString());
              variationFolder.mkdirs();

              // Save runParams as a text file in variationFolder
              File paramsFile = new File(variationFolder, "runParams.txt");
              try (FileWriter writer = new FileWriter(paramsFile)) {
                writer.write(paramsVariation.toString());
                } catch (IOException e) {
                    e.printStackTrace();
                }

                for (int run = 0; run < this.replications; run++) {

                    // → create NEW ScenarioGenerator instance
                    ScenarioGenerator generator =
                        genClass.getDeclaredConstructor().newInstance();

                    ScenarioParameters defaultParams = generator.getDefaultParameters();

                    // copy parameters
                    ScenarioParameters runParams = paramsVariation.copy();
                    long seed = defaultParams.getSeed() + run;
                    defaultParams.setSeed(seed);

                    // build output folder
                    File runFolder = new File(variationFolder, "run_seed_" + seed);
                    runFolder.mkdirs();

                    generator.setOutputDirectory(runFolder);

                    // Create SimulationScript
                    ScenarioSimulationScript script =
                            generator.buildSimulationScript(defaultParams.copy().applyOverridesFrom(runParams));

                    script.setGuiEnabled(false);

                    System.out.println("[RUN] " + scenarioName + " | seed=" + seed);

                    futures.add(pool.submit(() -> {
                        try {
                            script.start();
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

      System.out.println("All scenarios completed.");

    }
        /** Internal structure to hold scenario and its parameter variations. */
        class ScenarioEntry {
            Class<? extends ScenarioGenerator> generatorClass;
            List<ScenarioParameters> parameterVariations = new ArrayList<>();

            /** Constructor.
             * @param clazz scenario generator class
             */
            ScenarioEntry(final Class<? extends ScenarioGenerator> clazz) {
                this.generatorClass = clazz;
            }
        }

    }

