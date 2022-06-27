package cambio.simulator.orchestration.adapters;

import cambio.simulator.CLI;
import cambio.simulator.ExperimentStartupConfig;
import cambio.simulator.export.ReportCollector;
import cambio.simulator.misc.RNGStorage;
import cambio.simulator.models.MiSimModel;
import cambio.simulator.parsing.ParsingException;
import com.google.gson.JsonParseException;
import desmoj.core.simulator.Experiment;
import org.apache.commons.cli.ParseException;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

public class OrchestrationMain {
    /**
     * Main entry point of the program. Pass "-h" to see arguments.
     *
     * <p>
     * This method will <b>always</b> call {@link System#exit(int)}! Be aware of that if you call it from other code. If
     * you want to avoid this behavior, consider calling {@link #runExperiment(String[])} or {@link
     * #runExperiment(ExperimentStartupConfig)} instead.
     *
     * <p>
     * Exit code meanings are as follows:
     *
     * <table>
     *     <tr>
     *         <th>Exit Code</th>
     *         <th>Description</th>
     *        </tr>
     *     <tr>
     *        <td>0</td>
     *        <td>Success</td>
     *     </tr>
     *     <tr>
     *        <td>1</td>
     *        <td>Invalid arguments</td>
     *     </tr>
     *     <tr>
     *        <td>2</td>
     *        <td>Exception during parsing of models.</td>
     *     </tr>
     *     <tr>
     *        <td>16</td>
     *        <td>Exception during running of experiment.</td>
     *     </tr>
     *     <tr>
     *         <td>512</td>
     *         <td>Unexpected or unknown exception occurred.</td>
     *     </tr>
     * </table>
     *
     * @param args program options, see {@link ExperimentStartupConfig}
     * @see #mainVarargs(String...)
     * @see #runExperiment(String)
     * @see #runExperiment(ExperimentStartupConfig)
     */
    public static void main(final String[] args) {

        ExperimentStartupConfig startupConfig = parseArgsToConfig(args);

        try {

            //---------------------------------------Experiment execution-----------------------------------------------

            Experiment experiment = runExperiment(args);

            //-------------------------------------------Error handling-------------------------------------------------

            if (experiment.hasError()) {
                System.out.println("[INFO] Simulation failed.");
                System.exit(16);
            } else {
                System.out.println("[INFO] Simulation finished successfully.");
                System.exit(0);
            }
        } catch (ParsingException | JsonParseException e) {
            if (startupConfig.debugOutputOn()) {
                e.printStackTrace();
            } else {
                System.out.println("[ERROR] " + e.getMessage());
            }
            System.exit(2);
        } catch (Exception e) {
            if (startupConfig.debugOutputOn()) {
                e.printStackTrace();
            }

            System.exit(512);
        }
    }

    /**
     * Varargs variant of {@link #main(String[])}.
     *
     *
     * <p>
     * This method will <b>always</b> call {@link System#exit(int)}! Be aware of that if you call it from other code. If
     * you want to avoid this behavior, consider calling {@link #runExperiment(String[])} or {@link
     * #runExperiment(ExperimentStartupConfig)} instead.
     *
     * <p>
     * For exit code meanings, see {@link #main(String[])}.
     *
     * @param args program options, see {@link ExperimentStartupConfig#ExperimentStartupConfig(String, String, String,
     *             String, boolean, boolean, boolean)}
     * @see #main(String[])
     * @see #runExperiment(String)
     * @see #runExperiment(ExperimentStartupConfig)
     */
    public static void mainVarargs(final String... args) {
        main(args);
    }


    private static ExperimentStartupConfig parseArgsToConfig(String[] args) {
        // trim whitespaces from arguments to please apache cli
        String[] argsTrimmed = Arrays.stream(args).map(String::trim).toArray(String[]::new);
        try {
            return CLI.parseArguments(OrchestrationStartupConfig.class, argsTrimmed);
        } catch (ParseException e) {
            System.err.println("[ERROR] " + e.getMessage());
            System.exit(1);
        }
        return null;
    }


    private static Experiment runExperiment(String[] args) {

        ExperimentStartupConfig startupConfig = parseArgsToConfig(args);

        Experiment experiment = runExperiment(startupConfig);

        MiSimModel miSimModel = (MiSimModel) experiment.getModel();
        // Pure MiSim command line output
        ReportCollector.getInstance().printReport(miSimModel);

        DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH-mm-ss.SSSZ");
        String dateString = format.format(new Date());
        String currentRunName = miSimModel.getExperimentMetaData().getExperimentName() + "_" + dateString;
        ExtendedReporter.createReport(currentRunName, miSimModel);

        return experiment;

    }

    /**
     * Starts an experiment, and uses the given string as cli arguments (splits on spaces). Use spaces only to separate
     * arguments and not inside a value.
     *
     * @param cliString the cli argument string
     * @see #main(String[])
     * @see #mainVarargs(String...)
     * @see #runExperiment(ExperimentStartupConfig)
     */
    public static Experiment runExperiment(final String cliString) {
        return runExperiment(cliString.replaceAll("\\s*", " ").split(" "));
    }


    /**
     * Starts an experiment with the given {@link ExperimentStartupConfig}.
     *
     * @param startupConfig the experiment startup configuration
     * @see #runExperiment(String)
     * @see #main(String[])
     * @see #mainVarargs(String...)
     */
    public static Experiment runExperiment(final ExperimentStartupConfig startupConfig) {
        Experiment experiment = new OrchestrationExperimentCreator().createSimulationExperiment(startupConfig);
        System.out.printf("[INFO] Starting simulation at approximately %s%n", java.time.LocalDateTime.now());
        experiment.start();
        experiment.finish();

        RNGStorage.reset();

        return experiment;
    }
}
