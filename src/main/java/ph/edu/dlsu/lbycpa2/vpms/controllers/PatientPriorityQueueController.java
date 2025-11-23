package ph.edu.dlsu.lbycpa2.vpms.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.PriorityQueue;
import java.util.Comparator;

public class PatientPriorityQueueController {

    @FXML private TextField txtPatient;
    @FXML private TextField txtPriority;
    @FXML private TextField txtEmergency;
    @FXML private Button btnAdd;
    @FXML private Button btnServe;
    @FXML private ListView<String> listQueue;

    private static final Path PATIENT_FILE = Paths.get("src/main/java/ph/edu/dlsu/lbycpa2/vpms/data/patients.txt");


    private static class Patient {
        String name;
        String emergency;
        int priority;

        Patient(String name, String emergency, int priority) {
            this.name = name;
            this.emergency = emergency;
            this.priority = priority;
        }

        @Override
        public String toString() {
            return name + " (P=" + priority + ", Emergency=" + emergency + ")";
        }
    }


    // Max-heap: higher priority number = served first
    private final PriorityQueue<Patient> pq = new PriorityQueue<>(
            Comparator.comparingInt((Patient p) -> p.priority).reversed()
    );

    @FXML
    public void initialize() {
        btnAdd.setOnAction(e -> addPatient());
        btnServe.setOnAction(e -> servePatient());
    }

    private void addPatient() {
        String name = txtPatient.getText().trim();
        if (name.isEmpty()) {
            listQueue.getItems().add("Patient name cannot be empty.");
            return;
        }

        int priority;
        try {
            priority = Integer.parseInt(txtPriority.getText().trim());
            if (priority < 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            listQueue.getItems().add("Invalid priority. Must be a positive integer.");
            return;
        }

        String emergency = txtEmergency.getText().trim();
        if (emergency.isEmpty()) {
            listQueue.getItems().add("Emergency name cannot be empty.");
            return;
        }

        Patient patient = new Patient(name, emergency, priority);
        pq.add(patient);
        listQueue.getItems().add("Added: " + patient);

        txtPatient.clear();
        txtEmergency.clear();
        txtPriority.clear();
    }

    private void servePatient() {
        if (pq.isEmpty()) {
            listQueue.getItems().add("Queue is empty.");
            return;
        }

        Patient served = pq.poll();
        listQueue.getItems().add("Served: " + served);

        // Append served patient to patient.txt
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(PATIENT_FILE.toFile(), true))) {
            writer.write(served.name + "," + served.emergency + "," + served.priority);
            writer.newLine();
        } catch (IOException e) {
            listQueue.getItems().add("Error writing to patient.txt: " + e.getMessage());
        }
    }
}
