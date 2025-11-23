package ph.edu.dlsu.lbycpa2.vpms.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class DynamicResourceAllocationController {

    @FXML private ComboBox<String> cmbStaff;
    @FXML private ComboBox<String> cmbRoom;
    @FXML private ListView<String> listAssignments;
    @FXML private Button btnAssign;

    private ResourceAllocator allocator;

    private static final Path STAFF_FILE = Paths.get("src/main/java/ph/edu/dlsu/lbycpa2/vpms/data/staff.txt");
    private static final Path ROOM_FILE = Paths.get("src/main/java/ph/edu/dlsu/lbycpa2/vpms/data/rooms.txt");
    private static final Path ASSIGN_TRACKER_FILE = Paths.get("src/main/java/ph/edu/dlsu/lbycpa2/vpms/data/assignment tracker.txt");
    private static final Path ASSIGN_FILE = Paths.get("src/main/java/ph/edu/dlsu/lbycpa2/vpms/data/assignments.txt");

    @FXML
    public void initialize() {
        allocator = new ResourceAllocator();

        loadStaffFile();
        loadRoomFile();
        loadAssignmentsFile();
        refreshAssignmentList();

        btnAssign.setOnAction(e -> handleAssign());
    }

    private void loadStaffFile() {
        try {
            if (Files.exists(STAFF_FILE)) {
                Files.lines(STAFF_FILE, StandardCharsets.UTF_8)
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .forEach(cmbStaff.getItems()::add);
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
                        .forEach(cmbRoom.getItems()::add);
            }
        } catch (IOException e) {
            listAssignments.getItems().add("Error loading rooms: " + e.getMessage());
        }
    }

    private void loadAssignmentsFile() {
        if (!Files.exists(ASSIGN_FILE)) return;

        try {
            List<String> lines = Files.readAllLines(ASSIGN_FILE, StandardCharsets.UTF_8);
            for (String line : lines) {
                // Each line format: "Staff Name to Room Number"
                if (line.contains(" to Room ")) {
                    String[] parts = line.split(" to Room ");
                    if (parts.length == 2) {
                        String staff = parts[0].trim();
                        String room = parts[1].trim();
                        // rebuild linked list history with allocator.assign
                        allocator.assign(staff, room);
                    }
                }
            }
        } catch (IOException e) {
            listAssignments.getItems().add("Error loading assignments: " + e.getMessage());
        }
    }


    private void trackAssignmentToFile(String staff, String room) {
        try (BufferedWriter writer = Files.newBufferedWriter(
                ASSIGN_TRACKER_FILE,
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


    private void saveAssignmentToFile(String staff, String room) {
        Map<String, String> latest = allocator.buildLatestAssignmentMap();

        try (BufferedWriter writer = Files.newBufferedWriter(
                ASSIGN_FILE,
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING
        )) {
            for (Map.Entry<String, String> e : latest.entrySet()) {
                writer.write(e.getKey() + " to Room " + e.getValue());
                writer.newLine();
            }
        } catch (IOException e) {
            listAssignments.getItems().add("Error saving assignment overview: " + e.getMessage());
        }
    }

    private void handleAssign() {
        String staffLine = cmbStaff.getValue();
        String roomLine = cmbRoom.getValue();

        if (staffLine == null || roomLine == null) {
            listAssignments.getItems().add("Please pick both staff and room.");
            return;
        }

        // Extract staff
        String[] staffParts = staffLine.split("\\s-\\s");
        String staffName = staffParts[0].trim();
        String staffRank = staffParts.length > 1 ? staffParts[1].trim() : "";
        String staffSpecialty = staffParts.length > 2 ? staffParts[2].trim() : "";

        // Extract room
        String[] roomParts = roomLine.split("\\s-\\s");
        String roomNumber = roomParts[0].replace("Room ", "").trim();
        String roomSpecialty = roomParts.length > 1 ? roomParts[1].trim() : "";

        // Check current occupant of the room (latest)
        String currentOccupant = allocator.findRoomOccupant(roomNumber);
        if (currentOccupant != null && !currentOccupant.equalsIgnoreCase(staffName)) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Room Already Assigned");
            confirm.setHeaderText(null);
            confirm.setContentText(
                    currentOccupant + " is currently assigned to room " + roomNumber +
                            ".\nDo you wish to proceed?");

            if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
                return; // // user cancelled
            }
        }

        // Check if staff is already assigned
        String existingRoom = allocator.findStaffRoom(staffName);
        boolean staffWasReassigned = false;
        String previousRoom = null;

        if (existingRoom != null && !existingRoom.equals(roomNumber)) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Staff Already Assigned");
            confirm.setHeaderText(null);
            confirm.setContentText(
                    staffName + " is currently assigned to Room " + existingRoom +
                            ".\nDo you want to reassign them to Room " + roomNumber + "?"
            );

            if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
                return; // user cancelled
            }

            staffWasReassigned = true;
            previousRoom = existingRoom;
        }

        // Attempt assignment (this appends to history and also validates specialty)
        String result = allocator.assign(staffName, roomNumber);

        if (!result.startsWith("Assigned")) {
            listAssignments.getItems().add(result);
            return;
        }

        // Append to tracker (history log)
        String note = "";
        if (staffWasReassigned) {
            note = " (REASSIGNED FROM Room " + previousRoom + ")";
        } else if (currentOccupant != null && !currentOccupant.equalsIgnoreCase(staffName)) {
            note = " (REASSIGNED)";
        }
        trackAssignmentToFile(staffLine + note, roomLine);

        // Rewrite the assignments overview file with the latest per-staff mapping
        saveAssignmentToFile(staffLine, roomLine);

        // Friendly UI message
        String message = String.format(
                "%s (%s - %s) has been assigned to room %s (%s).%s",
                staffName, staffRank, staffSpecialty,
                roomNumber, roomSpecialty,
                note
        );
        listAssignments.getItems().add(message);

        // Refresh UI from history (shows history lines)
        refreshAssignmentList();

        cmbStaff.getSelectionModel().clearSelection();
        cmbRoom.getSelectionModel().clearSelection();
    }


    private void refreshAssignmentList() {
        listAssignments.getItems().setAll(allocator.getAssignments());
    }
}
