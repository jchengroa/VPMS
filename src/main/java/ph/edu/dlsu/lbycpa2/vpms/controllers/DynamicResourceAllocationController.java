package ph.edu.dlsu.lbycpa2.vpms.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDateTime;

public class DynamicResourceAllocationController {

    @FXML private ComboBox<String> cmbStaff;
    @FXML private ComboBox<String> cmbRoom;
    @FXML private ListView<String> listAssignments;
    @FXML private Button btnAssign;

    private ResourceAllocator allocator;

    private static final Path STAFF_FILE = Paths.get("src", "main", "java",
            "ph", "edu", "dlsu", "lbycpa2", "vpms", "data", "staff.txt");

    private static final Path ROOM_FILE = Paths.get("src", "main", "java",
            "ph", "edu", "dlsu", "lbycpa2", "vpms", "data", "rooms.txt");

    private static final Path ASSIGN_FILE = Paths.get("src", "main", "java",
            "ph", "edu", "dlsu", "lbycpa2", "vpms", "data", "assignments.txt");

    @FXML
    public void initialize() {
        allocator = new ResourceAllocator();

        loadStaffFile();
        loadRoomFile();

        btnAssign.setOnAction(e -> handleAssign());
    }

    private void loadStaffFile() {
        try {
            if (Files.exists(STAFF_FILE)) {
                Files.lines(STAFF_FILE, StandardCharsets.UTF_8)
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .forEach(s -> cmbStaff.getItems().add(s));
            }
        } catch (IOException e) {
            listAssignments.getItems().add("Error loading staff: " + e.getMessage());
        }
    }

    private void loadRoomFile() {
        try {
            if (Files.exists(ROOM_FILE)) {
                Files.lines(ROOM_FILE, StandardCharsets.UTF_8)
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .forEach(s -> cmbRoom.getItems().add(s));
            }
        } catch (IOException e) {
            listAssignments.getItems().add("Error loading rooms: " + e.getMessage());
        }
    }

    private void saveAssignmentToFile(String staff, String room) {
        try (BufferedWriter writer = Files.newBufferedWriter(
                ASSIGN_FILE,
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND
        )) {
            writer.write(staff + " - " + room + " - " + LocalDateTime.now());
            writer.newLine();

        } catch (IOException e) {
            listAssignments.getItems().add("Error saving assignment: " + e.getMessage());
        }
    }

    private void handleAssign() {
        String staffLine = cmbStaff.getValue(); // e.g., "John Smith - Senior - Cardiologist"
        String roomLine = cmbRoom.getValue();   // e.g., "Room 201 - Cardiologist"

        if (staffLine == null || roomLine == null) {
            listAssignments.getItems().add("Please pick both staff and room.");
            return;
        }

        // Extract staff info: name, rank, specialty
        String[] staffParts = staffLine.split("\\s-\\s");
        if (staffParts.length < 3) {
            listAssignments.getItems().add("Invalid staff format.");
            return;
        }
        String staffName = staffParts[0].trim();
        String staffRank = staffParts[1].trim();
        String staffSpecialty = staffParts[2].trim();

        // Extract room info
        String[] roomParts = roomLine.split("\\s-\\s");
        if (roomParts.length < 2) {
            listAssignments.getItems().add("Invalid room format.");
            return;
        }
        String roomNumber = roomParts[0].trim().replace("Room ", "");
        String roomSpecialty = roomParts[1].trim();

        // Check if room already assigned
        if (allocator.getAssignments().containsKey(roomNumber)) {
            listAssignments.getItems().add("Room " + roomNumber + " is already assigned.");
            return;
        }

        // Check specialty match (allow "NONE" rooms)
        if (!roomSpecialty.equalsIgnoreCase("NONE") && !roomSpecialty.equalsIgnoreCase("ICU") &&
                !roomSpecialty.equalsIgnoreCase(staffSpecialty)) {
            listAssignments.getItems().add("Room " + roomNumber + " requires a " + roomSpecialty
                    + " but staff specialty is " + staffSpecialty + ".");
            return;
        }

        // Assign (store staffLine for display)
        allocator.getAssignments().put(roomNumber, staffLine);

        // Save assignment
        saveAssignmentToFile(staffLine, roomLine);

        // Display full informative message
        String message = String.format("%s (%s - %s) has been assigned to room %s (%s).",
                staffName, staffRank, staffSpecialty, roomNumber, roomSpecialty);
        listAssignments.getItems().add(message);

        // Clear selections
        cmbStaff.getSelectionModel().clearSelection();
        cmbRoom.getSelectionModel().clearSelection();
    }


}
