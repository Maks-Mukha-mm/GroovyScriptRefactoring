import javax.swing.*;
import java.awt.*;
import java.io.File;

public class SimulationGUI extends JFrame {

    private JTextField modelNameField;
    private JTextField dataFileField;
    private JTextField scriptFileField;
    private JTextArea scriptEditor;
    private JTextArea resultsArea;
    private JLabel statusLabel;

    private JButton browseDataButton;
    private JButton browseScriptButton;
    private JButton runModelButton;
    private JButton runScriptButton;

    public SimulationGUI() {
        setTitle("Simulation Model Controller");
        setSize(900, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        initComponents();
        setLocationRelativeTo(null);
    }

    private void initComponents() {
        JPanel modelPanel = new JPanel(new BorderLayout(5, 5));
        JLabel modelLabel = new JLabel("Model Class Name:");
        modelNameField = new JTextField();
        modelPanel.add(modelLabel, BorderLayout.WEST);
        modelPanel.add(modelNameField, BorderLayout.CENTER);

        JPanel dataPanel = new JPanel(new BorderLayout(5, 5));
        JLabel dataLabel = new JLabel("Data File:");
        dataFileField = new JTextField();
        browseDataButton = new JButton("Browse");
        dataPanel.add(dataLabel, BorderLayout.WEST);
        dataPanel.add(dataFileField, BorderLayout.CENTER);
        dataPanel.add(browseDataButton, BorderLayout.EAST);

        JPanel scriptPanel = new JPanel(new BorderLayout(5, 5));
        JLabel scriptLabel = new JLabel("Script File:");
        scriptFileField = new JTextField();
        browseScriptButton = new JButton("Browse");
        scriptPanel.add(scriptLabel, BorderLayout.WEST);
        scriptPanel.add(scriptFileField, BorderLayout.CENTER);
        scriptPanel.add(browseScriptButton, BorderLayout.EAST);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        runModelButton = new JButton("Run Model");
        runScriptButton = new JButton("Run Script");
        buttonPanel.add(runModelButton);
        buttonPanel.add(runScriptButton);

        JPanel editorPanel = new JPanel(new BorderLayout(5, 5));
        JLabel editorLabel = new JLabel("Script Editor:");
        scriptEditor = new JTextArea(10, 70);
        JScrollPane editorScrollPane = new JScrollPane(scriptEditor);
        editorPanel.add(editorLabel, BorderLayout.NORTH);
        editorPanel.add(editorScrollPane, BorderLayout.CENTER);

        JPanel resultsPanel = new JPanel(new BorderLayout(5, 5));
        JLabel resultsLabel = new JLabel("Results (TSV):");
        resultsArea = new JTextArea(20, 70);
        resultsArea.setEditable(false);
        JScrollPane resultsScrollPane = new JScrollPane(resultsArea);
        resultsPanel.add(resultsLabel, BorderLayout.NORTH);
        resultsPanel.add(resultsScrollPane, BorderLayout.CENTER);

        JPanel statusPanel = new JPanel(new BorderLayout());
        statusLabel = new JLabel("Status: Waiting for actions.");
        statusPanel.add(statusLabel, BorderLayout.WEST);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        mainPanel.add(modelPanel);
        mainPanel.add(Box.createVerticalStrut(15));
        mainPanel.add(dataPanel);
        mainPanel.add(Box.createVerticalStrut(15));
        mainPanel.add(scriptPanel);
        mainPanel.add(Box.createVerticalStrut(15));
        mainPanel.add(buttonPanel);
        mainPanel.add(Box.createVerticalStrut(15));
        mainPanel.add(editorPanel);
        mainPanel.add(Box.createVerticalStrut(15));
        mainPanel.add(resultsPanel);
        mainPanel.add(Box.createVerticalStrut(15));
        mainPanel.add(statusPanel);

        add(mainPanel);

        browseDataButton.addActionListener(e -> browseFile(dataFileField));
        browseScriptButton.addActionListener(e -> browseFile(scriptFileField));
        runModelButton.addActionListener(e -> runModel());
        runScriptButton.addActionListener(e -> runScript());
    }

    private void browseFile(JTextField targetField) {
        JFileChooser fileChooser = new JFileChooser();
        String userDir = System.getProperty("user.dir");
        fileChooser.setCurrentDirectory(new File(userDir));
        fileChooser.setMultiSelectionEnabled(false);

        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            targetField.setText(selectedFile.getAbsolutePath());
        }
    }

    private void runModel() {
        String modelName = modelNameField.getText().trim();
        String dataFile = dataFileField.getText().trim();

        if (modelName.isEmpty() || dataFile.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please specify a model name and select a data file.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }


        statusLabel.setText("Status: Running model...");

        String results = "";


        try {
            Controller ctl = new Controller(modelName);
            ctl.readDataFrom(dataFile).runModel();
            results = ctl.getResultsAsTsv();
        } catch (Exception ex) {
            Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
            statusLabel.setText("Status: Error - " + cause.getMessage());
            JOptionPane.showMessageDialog(this, cause.toString(), "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        final String finalResults = results;


        SwingUtilities.invokeLater(() -> {
            runModelButton.setEnabled(true);
            runScriptButton.setEnabled(true);

                resultsArea.setText(finalResults);
                statusLabel.setText("Status: Model ran successfully.");
        });
    }

    private void runScript() {
        String modelName = modelNameField.getText().trim();
        String dataFile = dataFileField.getText().trim();
        String scriptFile = scriptFileField.getText().trim();
        String scriptCode = scriptEditor.getText().trim();

        if (modelName.isEmpty() || dataFile.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please specify a model name and select a data file.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }


        statusLabel.setText("Status: Running model and script...");

        String results = "";


        try {
            Controller ctl = new Controller(modelName);
            ctl.readDataFrom(dataFile).runModel();
            if (!scriptFile.isEmpty()) {
                ctl.runScriptFromFile(scriptFile);
            }
            if (!scriptCode.isEmpty()) {
                ctl.runScript(scriptCode);
            }
            results = ctl.getResultsAsTsv();
        } catch (Exception ex) {
            Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
            statusLabel.setText("Status: Error - " + cause.getMessage());
            JOptionPane.showMessageDialog(this, cause.toString(), "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        final String finalResults = results;


        SwingUtilities.invokeLater(() -> {
            runModelButton.setEnabled(true);
            runScriptButton.setEnabled(true);

                resultsArea.setText(finalResults);
                statusLabel.setText("Status: Script ran successfully.");

        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            SimulationGUI gui = new SimulationGUI();
            gui.setVisible(true);
        });
    }
}
