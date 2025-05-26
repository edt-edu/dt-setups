import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Desktop;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.Map;
import java.util.HashMap;
import java.io.File;

// --- ProcessContext Record ---
// A record to hold all components related to a single running process.
record ProcessContext(Process process, ExecutorService executor, JTextArea outputArea, JButton startButton, JButton stopButton) {}

// --- Global Maps for Process Management ---
// Maps to hold the AtomicReference for each process and their corresponding JTextAreas.
Map<String, AtomicReference<ProcessContext>> processRefs = new HashMap<>();
Map<String, JTextArea> outputAreas = new HashMap<>();
Map<String, JButton> startButtons = new HashMap<>();
Map<String, JButton> stopButtons = new HashMap<>();

// --- JTable and Model for Status Tab ---
DefaultTableModel processStatusTableModel;
JTable processStatusTable;

// --- New Enum for Process Configuration ---
enum ProcessConfig {
    ISLAND_1_CONTROLLER("Island 1 Controller", new String[]{"./start_controller.sh", "1"}, null),
    ISLAND_2_CONTROLLER("Island 2 Controller", new String[]{"./start_controller.sh", "2"}, null),
    FACTORY_SCADA_BACKEND("Factory Scada Backend", new String[]{"./gradlew", "bootRun", "--args=\"--configuration.path=/home/se-rechnerpool2/fischertechnik/mbdo-impl/RWTH_Setup/scripts/factoryscada_rwth.yml\""}, "/home/se-rechnerpool2/fischertechnik/mbdo-impl/REN_Setups/physical-impl/factoryscada/backend"),
    FACTORY_SCADA_FRONTEND("Factory Scada Frontend", new String[]{"npm", "run", "start"}, "/home/se-rechnerpool2/fischertechnik/mbdo-impl/REN_Setups/physical-impl/factoryscada/frontend");

    private final String processName;
    private final String[] command;
    private final String workingDirectory;

    ProcessConfig(String processName, String[] command, String workingDirectory) {
        this.processName = processName;
        this.command = command;
        this.workingDirectory = workingDirectory;
    }

    public String getProcessName() {
        return processName;
    }

    public String[] getCommand() {
        return command;
    }

    public String getWorkingDirectory() {
        return workingDirectory;
    }
}

/**
 * Updates the status information in the JTable on the "Status" tab.
 */
void updateProcessStatusUI() {
    SwingUtilities.invokeLater(() -> {
        // Clear existing rows
        processStatusTableModel.setRowCount(0);

        // Add current process statuses
        processRefs.forEach((processName, processRef) -> {
            String status;
            ProcessContext context = processRef.get();
            if (context != null && context.process() != null && context.process().isAlive()) {
                status = "Running";
            } else {
                status = "Stopped";
            }
            processStatusTableModel.addRow(new Object[]{processName, status});
        });
    });
}

/**
 * Starts a new process and manages its output in the specified JTextArea.
 *
 * @param processName The logical name of the process (e.g., "Island 1 Controller").
 * @param command The command array for the ProcessBuilder (e.g., "bash", "-c", "echo hello").
 * @param workingDirectory The working directory for the process, or null if default.
 * @param processRef The AtomicReference holding the ProcessContext for this process.
 * @param outputArea The JTextArea where the process output will be displayed.
 * @param startButton The start button associated with this process.
 * @param stopButton The stop button associated with this process.
 */
void startProcess(String processName, String[] command, String workingDirectory, AtomicReference<ProcessContext> processRef, JTextArea outputArea, JButton startButton, JButton stopButton) {
    // Retrieve the current ProcessContext.
    ProcessContext currentContext = processRef.get();

    // Check if a process is already running for this context.
    if (currentContext != null && currentContext.process() != null && currentContext.process().isAlive()) {
        SwingUtilities.invokeLater(() -> outputArea.append(processName + " is already running.\n"));
        return;
    }

    SwingUtilities.invokeLater(() -> {
        outputArea.setText(""); // Clear previous output
        outputArea.append("Starting " + processName + "...\n");
    });

    try {
        var processBuilder = new ProcessBuilder(command); // Use the passed command array
        if (workingDirectory != null && !workingDirectory.isEmpty()) {
            File cwd = new File(workingDirectory);
            if (cwd.exists() && cwd.isDirectory()) {
                processBuilder.directory(cwd);
            } else {
                SwingUtilities.invokeLater(() -> outputArea.append("Warning: Working directory " + workingDirectory + " does not exist or is not a directory for " + processName + ".\n"));
            }
        }
        processBuilder.redirectErrorStream(true);
        Process process = processBuilder.start();

        ExecutorService executor = Executors.newSingleThreadExecutor();
        // Update the AtomicReference with the new ProcessContext
        processRef.set(new ProcessContext(process, executor, outputArea, startButton, stopButton));

        executor.submit(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    final String textToAppend = line;
                    SwingUtilities.invokeLater(() -> outputArea.append(textToAppend + "\n"));
                }
            } catch (IOException e) {
                // Check if the process was forcibly stopped.
                if (!(e.getMessage() != null && (e.getMessage().toLowerCase().contains("stream closed") || e.getMessage().toLowerCase().contains("pipe closed")))) {
                    SwingUtilities.invokeLater(() -> outputArea.append("Error reading " + processName + " output: " + e.getMessage() + "\n"));
                }
            } finally {
                try {
                    // Get the latest context to ensure we're interacting with the correct process.
                    Process p = processRef.get() != null ? processRef.get().process() : null;
                    if (p != null) {
                        int exitCode = p.waitFor();
                        SwingUtilities.invokeLater(() -> {
                            outputArea.append("\n" + processName + " exited with code: " + exitCode + "\n");
                            // If process exited on its own, update button states
                            if (!startButton.isEnabled()) { // only if it was running
                                startButton.setEnabled(true);
                                stopButton.setEnabled(false);
                            }
                            updateProcessStatusUI(); // Update status table
                        });
                        // Clear the process context as it has exited naturally.
                        processRef.set(null);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    SwingUtilities.invokeLater(() -> outputArea.append("\n" + processName + " reading interrupted.\n"));
                }
            }
        });

        SwingUtilities.invokeLater(() -> {
            startButton.setEnabled(false);
            stopButton.setEnabled(true);
            updateProcessStatusUI(); // Update status table
        });

    } catch (IOException e) {
        SwingUtilities.invokeLater(() -> outputArea.append("Error starting " + processName + ": " + e.getMessage() + "\n"));
        // Clean up executor and process context if starting failed.
        if (currentContext != null && currentContext.executor() != null && !currentContext.executor().isShutdown()) {
            currentContext.executor().shutdownNow();
        }
        processRef.set(null); // Ensure process is marked as not running
        SwingUtilities.invokeLater(() -> {
            startButton.setEnabled(true);
            stopButton.setEnabled(false);
            updateProcessStatusUI(); // Update status table
        });
    }
}

/**
 * Stops a running process.
 *
 * @param processName The logical name of the process.
 * @param processRef The AtomicReference holding the ProcessContext for this process.
 * @param outputArea The JTextArea where status updates will be displayed.
 * @param startButton The start button associated with this process.
 * @param stopButton The stop button associated with this process.
 */
void stopProcess(String processName, AtomicReference<ProcessContext> processRef, JTextArea outputArea, JButton startButton, JButton stopButton) {
    ProcessContext currentContext = processRef.get();

    if (currentContext == null || currentContext.process() == null || !currentContext.process().isAlive()) {
        SwingUtilities.invokeLater(() -> outputArea.append(processName + " is not running or already stopped.\n"));
        // Ensure buttons are in correct state if called spuriously
        startButton.setEnabled(true);
        stopButton.setEnabled(false);
        updateProcessStatusUI(); // Ensure status table is correct
        return;
    }

    SwingUtilities.invokeLater(() -> outputArea.append("Stopping " + processName + "...\n"));
    if (currentContext.process() != null) {
        currentContext.process().destroyForcibly(); // Terminate the process
    }

    if (currentContext.executor() != null && !currentContext.executor().isShutdown()) {
        currentContext.executor().shutdownNow(); // Stop the reading thread
        try {
            if (!currentContext.executor().awaitTermination(800, TimeUnit.MILLISECONDS)) {
                System.err.println("Executor for " + processName + " did not terminate in the specified time.");
            }
        } catch (InterruptedException e) {
            currentContext.executor().shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    // Give a moment for the reading thread to notice the stream closure and exit.
    SwingUtilities.invokeLater(() -> {
        outputArea.append(processName + " stopped by user.\n");
        startButton.setEnabled(true);
        stopButton.setEnabled(false);
        processRef.set(null); // Mark as stopped by clearing the context
        updateProcessStatusUI(); // Update status table
    });
}

void openFactoryScadaFrontend(){
    try {
        Desktop.getDesktop().browse(new URI("http://localhost:4200"));
    } catch (Exception ex) {
        // Handle exception, e.g., show an error dialog
        JOptionPane.showMessageDialog(null, "Could not open browser: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }
}

/**
 * Starts all registered processes.
 */
void startAllProcesses() {
    for (ProcessConfig config : ProcessConfig.values()) {
        String name = config.getProcessName();
        String[] command = config.getCommand();
        String workingDirectory = config.getWorkingDirectory();
        JTextArea associatedOutputArea = outputAreas.get(name);
        JButton associatedStartButton = startButtons.get(name);
        JButton associatedStopButton = stopButtons.get(name);
        AtomicReference<ProcessContext> processRef = processRefs.get(name); // Get the existing ref from map
        startProcess(name, command, workingDirectory, processRef, associatedOutputArea, associatedStartButton, associatedStopButton);
    }
}

/**
 * Stops all registered processes.
 */
void stopAllProcesses() {
    for (ProcessConfig config : ProcessConfig.values()) {
        String name = config.getProcessName();
        JTextArea associatedOutputArea = outputAreas.get(name);
        JButton associatedStartButton = startButtons.get(name);
        JButton associatedStopButton = stopButtons.get(name);
        AtomicReference<ProcessContext> processRef = processRefs.get(name); // Get the existing ref from map
        stopProcess(name, processRef, associatedOutputArea, associatedStartButton, associatedStopButton);
    }
}

/**
 * Sets up the main Swing UI with tabs for different processes.
 */
void setupUI() {
    JFrame frame = new JFrame("Control software overview");
    frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

    JTabbedPane tabs = new JTabbedPane();

    // --- Setup for "Status" tab with JTable ---
    String[] columnNames = {"Process Name", "Status"};
    processStatusTableModel = new DefaultTableModel(columnNames, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false; // Make cells non-editable
        }
    };
    processStatusTable = new JTable(processStatusTableModel);
    processStatusTable.setFillsViewportHeight(true); // Make the table fill the viewport height
    JScrollPane statusScrollPane = new JScrollPane(processStatusTable);
    JPanel statusPage = new JPanel(new BorderLayout());
    statusPage.add(statusScrollPane, BorderLayout.CENTER);
    tabs.add("Status", statusPage);

    // Add "Start All" and "Stop All" buttons to the Status tab
    JPanel statusButtonPanel = new JPanel(new FlowLayout());
    JButton startAllButton = new JButton("Start All");
    JButton stopAllButton = new JButton("Stop All");
    JButton openFactoryScadaFrontendButton = new JButton("Open FactoryScada Frontend");

    startAllButton.addActionListener(e -> startAllProcesses());
    stopAllButton.addActionListener(e -> stopAllProcesses());
    openFactoryScadaFrontendButton.addActionListener(e -> openFactoryScadaFrontend());

    statusButtonPanel.add(startAllButton);
    statusButtonPanel.add(stopAllButton);
    statusButtonPanel.add(openFactoryScadaFrontendButton);
    statusPage.add(statusButtonPanel, BorderLayout.SOUTH);

    // Loop through ProcessConfig enum to set up UI for each process
    for (ProcessConfig config : ProcessConfig.values()) {
        String processName = config.getProcessName();
        String[] command = config.getCommand();
        String workingDirectory = config.getWorkingDirectory();

        JTextArea outputArea = new JTextArea();
        outputArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(outputArea);
        JButton startButton = new JButton("Start " + processName);
        JButton stopButton = new JButton("Stop " + processName);
        AtomicReference<ProcessContext> processRef = new AtomicReference<>();

        outputAreas.put(processName, outputArea);
        startButtons.put(processName, startButton);
        stopButtons.put(processName, stopButton);
        processRefs.put(processName, processRef);

        startButton.addActionListener(e -> startProcess(processName, command, workingDirectory, processRef, outputArea, startButton, stopButton));
        stopButton.addActionListener(e -> stopProcess(processName, processRef, outputArea, startButton, stopButton));

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(startButton);
        buttonPanel.add(stopButton);

        // Special case for Factory Scada Frontend to add the "Open" button
        if (config == ProcessConfig.FACTORY_SCADA_FRONTEND) {
            JButton openButton = new JButton("Open " + processName);
            openButton.addActionListener(e -> openFactoryScadaFrontend());
            buttonPanel.add(openButton);
        }

        JPanel processPanel = new JPanel(new BorderLayout());
        processPanel.add(scrollPane, BorderLayout.CENTER);
        processPanel.add(buttonPanel, BorderLayout.SOUTH);
        tabs.add(processName.replace("Controller", "").trim(), processPanel); // Tab title might need adjustment
        // Initial button states
        startButton.setEnabled(true);
        stopButton.setEnabled(false);
    }

    frame.setContentPane(tabs);

    frame.addWindowListener(new WindowAdapter() {
        @Override
        public void windowClosing(WindowEvent windowEvent) {
            // Iterate through all managed processes and stop them
            processRefs.forEach((name, ref) -> {
                ProcessContext context = ref.get();
                if (context != null && context.process() != null && context.process().isAlive()) {
                    JTextArea associatedOutputArea = outputAreas.get(name);
                    JButton associatedStartButton = startButtons.get(name);
                    JButton associatedStopButton = stopButtons.get(name);
                    SwingUtilities.invokeLater(() -> associatedOutputArea.append("Window closing. Forcibly stopping " + name + "...\n"));
                    stopProcess(name, ref, associatedOutputArea, associatedStartButton, associatedStopButton);
                } else if (context != null && context.executor() != null && !context.executor().isShutdown()) {
                    // If process was already dead but executor somehow lingers
                    context.executor().shutdownNow();
                }
            });
        }
    });

    frame.setVisible(true);

    // Initial update of the status table
    updateProcessStatusUI();
}

// Call the setup method
System.setProperty("sun.java2d.uiScale", "2.0");
setupUI();