package ph.edu.dlsu.lbycpa2.vpms.controllers;

import ph.edu.dlsu.lbycpa2.vpms.models.Room;
import ph.edu.dlsu.lbycpa2.vpms.models.Staff;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

public class ResourceAllocator {

    private final Map<String, Staff> staffMap = new HashMap<>();  // key = name
    private final Map<String, Room> roomMap = new HashMap<>();    // key = roomNumber
    private final Map<String, String> assignments = new HashMap<>(); // room → staff

    private static final Path STAFF_FILE = Paths.get(
            "src/main/java/ph/edu/dlsu/lbycpa2/vpms/data/staff.txt"
    );

    public ResourceAllocator() {
        loadStaff();
        loadRooms();
    }

    /** Load staff from staff.txt */
    private void loadStaff() {
        try {
            List<String> lines = Files.readAllLines(STAFF_FILE, StandardCharsets.UTF_8);
            for (String line : lines) {
                String[] parts = line.split(",");
                if (parts.length == 3) {
                    String name = parts[0].trim();
                    String rank = parts[1].trim();
                    String specialty = parts[2].trim();
                    staffMap.put(name.toLowerCase(), new Staff(name, rank, specialty));
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load staff.txt", e);
        }
    }

    /** For now, define sample rooms */
    private void loadRooms() {
        roomMap.put("101", new Room("101", "Surgeon"));
        roomMap.put("102", new Room("102", "Cardiologist"));
        roomMap.put("103", new Room("103", "NONE"));
        roomMap.put("201", new Room("201", "Pediatrician"));
    }

    public String assign(String staffName, String roomNumber) {

        // Validate staff
        Staff s = staffMap.get(staffName.toLowerCase());
        if (s == null) {
            return "❌ Staff not found in directory.";
        }

        // Validate room
        Room r = roomMap.get(roomNumber);
        if (r == null) {
            return "❌ Room does not exist.";
        }

        // Check if room already assigned
        if (assignments.containsKey(roomNumber)) {
            return "❌ Room " + roomNumber + " is already assigned to "
                    + assignments.get(roomNumber) + ".";
        }

        // Check specialty match
        if (!r.getSpecialty().equalsIgnoreCase("NONE") &&
                !r.getSpecialty().equalsIgnoreCase(s.getSpecialty())) {

            return "❌ Room requires a " + r.getSpecialty()
                    + " but staff is a " + s.getSpecialty() + ".";
        }

        // Assign
        assignments.put(roomNumber, s.getName());
        return "✅ Assigned " + s.getName() + " to Room " + roomNumber;
    }

    public Map<String, String> getAssignments() {
        return assignments;
    }
}
