package ph.edu.dlsu.lbycpa2.vpms.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import ph.edu.dlsu.lbycpa2.vpms.models.Staff;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.TreeSet;

public class InsertStaffDirectoryController {
    @FXML private TextField txtName;
    @FXML private ComboBox<String> cmbRank;
    @FXML private ComboBox<String> cmbSpecialty;
    @FXML private Button btnConfirm;
    @FXML private Button btnCancel;
    @FXML private ListView<String> listOutput;

    private final TreeSet<Staff> bst = new TreeSet<>((a, b) -> {
        int cmp = a.getName().compareToIgnoreCase(b.getName());
        return cmp != 0 ? cmp : a.toString().compareToIgnoreCase(b.toString());
    });

    private static final Path FILE_PATH = Paths.get("src", "main", "java",
            "ph", "edu", "dlsu", "lbycpa2", "vpms", "data", "staff.txt");

    private static final String[] VALID_RANKS = {
            "Intern", "Junior", "Senior", "Nurse","Consultant", "Head of Department", "CEO"
    };
    private static final String[] VALID_SPECIALTIES = {
            "Cardiologist", "Neurologist", "Pediatrician", "Surgeon",
            "Anesthesiologist", "Radiologist", "Oncologist", "Dermatologist",
            "General Practitioner", "NONE"
    };

    @FXML
    public void initialize() {
        cmbRank.getItems().addAll(VALID_RANKS);
        cmbSpecialty.getItems().addAll(VALID_SPECIALTIES);

        btnConfirm.setOnAction(e -> confirmStaff());
        btnCancel.setOnAction(e -> cancelStaff());

        loadStaffFromFile();
    }

    // FILE HANDLING
    private void loadStaffFromFile() {
        try {
            if (Files.notExists(FILE_PATH.getParent())) {
                try {
                    Files.createDirectories(FILE_PATH.getParent());
                } catch (IOException ex) {
                    listOutput.getItems().add("Could not create directories: " + ex.getMessage());
                    return;
                }
            }

            if (Files.notExists(FILE_PATH)) {
                // create empty file
                Files.createFile(FILE_PATH);
                listOutput.getItems().add("Created staff file at: " + FILE_PATH.toString());
                return;
            }

            // Read file (each line in format: Name - Role - Rank)
            Files.lines(FILE_PATH, StandardCharsets.UTF_8)
                    .map(String::trim)
                    .filter(line -> !line.isEmpty())
                    .forEach(line -> {
                        Staff s = parseLineToStaff(line);
                        if (s != null) {
                            bst.add(s);
                        } else {
                            listOutput.getItems().add("Skipped malformed line: " + line);
                        }
                    });

            listOutput.getItems().add("Loaded " + bst.size() + " staff from file.");

        } catch (IOException e) {
            listOutput.getItems().add("Error loading staff file: " + e.getMessage());
        }
    }

    private Staff parseLineToStaff(String line) {
        // expected delimiter: " - " (space-dash-space)
        String[] parts = line.split("\\s-\\s", 3); // limit 3 parts
        if (parts.length == 0) return null;

        String name = parts[0].trim();
        String role = parts.length >= 2 ? parts[1].trim() : "";
        String rank = parts.length >= 3 ? parts[2].trim() : "";

        if (name.isEmpty()) return null;
        return new Staff(name, role, rank);
    }

    private void appendStaffToFile(Staff s) {
        // append in UTF-8, create file if missing
        try (BufferedWriter writer = Files.newBufferedWriter(
                FILE_PATH,
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND
        )) {
            writer.write(s.toString());
            writer.newLine();
        } catch (IOException ex) {
            listOutput.getItems().add("Error writing to file: " + ex.getMessage());
        }
    }

    // UI ACTIONS
    private void confirmStaff() {
        String name = txtName.getText().trim();
        String rank = cmbRank.getValue();
        String specialty = cmbSpecialty.getValue();

        listOutput.getItems().clear();

        // Validation
        if (name.isEmpty()) {
            listOutput.getItems().add("Error: Name is required.");
            return;
        }
        if (rank == null) {
            listOutput.getItems().add("Error: Please select a position.");
            return;
        }
        if (specialty == null) {
            listOutput.getItems().add("Error: Please select a specialty.");
            return;
        }

        Staff s = new Staff(name, rank, specialty);

        if (bst.contains(s)) {
            listOutput.getItems().add("Staff already exists: " + s);
            return;
        }

        bst.add(s);
        appendStaffToFile(s);

        listOutput.getItems().add("Inserted: " + s);
        txtName.clear();
        cmbRank.setValue(null);
        cmbSpecialty.setValue(null);
    }

    @FXML
    private void cancelStaff() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ph/edu/dlsu/lbycpa2/vpms/SD-View.fxml"));
            Parent root = loader.load();

            Stage newStage = new Stage();
            newStage.setScene(new Scene(root));
            newStage.setTitle("Staff Directory");
            newStage.show();

            ((Stage) btnCancel.getScene().getWindow()).close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
