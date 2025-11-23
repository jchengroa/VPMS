package ph.edu.dlsu.lbycpa2.vpms.controllers;

import ph.edu.dlsu.lbycpa2.vpms.models.Room;
import ph.edu.dlsu.lbycpa2.vpms.models.Staff;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

public class ResourceAllocator {

    private static final Path STAFF_FILE = Paths.get(
            "src/main/java/ph/edu/dlsu/lbycpa2/vpms/data/staff.txt");

    private static final Path ROOM_FILE = Paths.get(
            "src/main/java/ph/edu/dlsu/lbycpa2/vpms/data/rooms.txt");

    private static final Path ASSIGN_TRACKER_FILE = Paths.get(
            "src/main/java/ph/edu/dlsu/lbycpa2/vpms/data/assignment tracker.txt");


    private final Map<String, Staff> staffMap = new HashMap<>();
    private final Map<String, Room> roomMap = new HashMap<>();

    // LINKED LIST IMPLEMENTATION (history)
    private AssignmentNode head = null;

    private static class AssignmentNode {
        String room;
        String staff;
        AssignmentNode next;

        AssignmentNode(String room, String staff) {
            this.room = room;
            this.staff = staff;
            this.next = null;
        }
    }

    public ResourceAllocator() {
        loadStaff();
        loadRooms();
        loadAssignments();
    }

    // LOAD STAFF FILE
    private void loadStaff() {
        try {
            if (Files.exists(STAFF_FILE)) {
                for (String line : Files.readAllLines(STAFF_FILE, StandardCharsets.UTF_8)) {
                    String[] parts = line.split("\\s-\\s");
                    if (parts.length == 3) {
                        staffMap.put(
                                parts[0].trim().toLowerCase(),
                                new Staff(parts[0].trim(), parts[1].trim(), parts[2].trim())
                        );
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Could not load staff.txt: " + e.getMessage());
        }
    }

    // LOAD ROOMS FILE
    private void loadRooms() {
        try {
            if (Files.exists(ROOM_FILE)) {
                for (String line : Files.readAllLines(ROOM_FILE, StandardCharsets.UTF_8)) {
                    String[] parts = line.split("\\s-\\s");
                    if (parts.length == 2) {
                        String roomNum = parts[0].trim().replace("Room ", "");
                        roomMap.put(roomNum, new Room(roomNum, parts[1].trim()));
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Could not load rooms.txt: " + e.getMessage());
        }
    }

    private void loadAssignments() {
        if (!Files.exists(ASSIGN_TRACKER_FILE)) {
            return;
        }

        try {
            for (String line : Files.readAllLines(ASSIGN_TRACKER_FILE, StandardCharsets.UTF_8)) {
                if (line.contains(" has been assigned to ")) {
                    String[] parts = line.split(" has been assigned to ");
                    if (parts.length == 2) {
                        String staffName = parts[0].trim();
                        String roomNumber = parts[1].trim();
                        addAssignmentHistory(roomNumber, staffName);
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Could not load assignment tracker.txt: " + e.getMessage());
        }
    }

    // Helper: Append a history node (linked-list insertion at end)
    private void addAssignmentHistory(String room, String staff) {
        AssignmentNode newNode = new AssignmentNode(room, staff);
        if (head == null) {
            head = newNode;
            return;
        }
        AssignmentNode current = head;
        while (current.next != null) current = current.next;
        current.next = newNode;
    }

    public void saveCurrentAssignments() {
        Map<String, String> latestAssignments = buildLatestAssignmentMap();
        List<String> lines = new ArrayList<>();
        for (Map.Entry<String, String> entry : latestAssignments.entrySet()) {
            lines.add(entry.getKey() + " to Room " + entry.getValue());
        }

    }


    // Helper: return the latest room for a staff (scan entire list, last match wins)
    public String findStaffRoom(String staffName) {
        AssignmentNode current = head;
        String latestRoom = null;
        while (current != null) {
            if (current.staff.equalsIgnoreCase(staffName)) {
                latestRoom = current.room;
            }
            current = current.next;
        }
        return latestRoom; // null if not found
    }

    // Helper: return the latest staff occupying a room (last match wins)
    public String findRoomOccupant(String roomNumber) {
        AssignmentNode current = head;
        String latestStaff = null;
        while (current != null) {
            if (current.room.equalsIgnoreCase(roomNumber)) {
                latestStaff = current.staff;
            }
            current = current.next;
        }
        return latestStaff; // null if not found
    }

    // Helper: check whether room is currently occupied (based on latest scan)
    public boolean isRoomCurrentlyAssigned(String roomNumber) {
        return findRoomOccupant(roomNumber) != null;
    }

    // Helper: check whether staff currently assigned (based on latest scan)
    public boolean isStaffCurrentlyAssigned(String staffName) {
        return findStaffRoom(staffName) != null;
    }

    // Build a map of current assignments from history
    public Map<String, String> buildLatestAssignmentMap() {
        Map<String, String> latestByStaff = new LinkedHashMap<>(); // LinkedHashMap preserves insertion order
        AssignmentNode current = head;
        while (current != null) {
            // overwrite previous entry for the staff; later entries are newer
            latestByStaff.put(current.staff, current.room);
            current = current.next;
        }
        return latestByStaff;
    }

    public String assign(String staffName, String roomNumber) {
        if (staffName == null || roomNumber == null) {
            return "Invalid input.";
        }

        Staff staff = staffMap.get(staffName.toLowerCase());
        if (staff == null) {
            return "Staff not found in directory.";
        }

        Room room = roomMap.get(roomNumber);
        if (room == null) {
            return "Room does not exist.";
        }

        // 1) If staff already assigned to same room
        String currentRoom = findStaffRoom(staffName);
        if (currentRoom != null && currentRoom.equals(roomNumber)) {
            return staffName + " is already assigned to Room " + roomNumber + ".";
        }

        // 2) Specialty check
        if (!room.getSpecialty().equalsIgnoreCase("NONE") &&
                !room.getSpecialty().equalsIgnoreCase("ICU") &&
                !room.getSpecialty().equalsIgnoreCase(staff.getSpecialty())) {

            return "Room requires a " + room.getSpecialty() +
                    " but staff is a " + staff.getSpecialty() + ".";
        }

        // 3) Append to history
        addAssignmentHistory(roomNumber, staff.getName());

        // 4) Persist the latest assignment to file
        saveCurrentAssignments();

        return "Assigned " + staff.getName() + " to Room " + roomNumber;
    }


    public List<String> getAssignments() {
        List<String> result = new ArrayList<>();
        AssignmentNode current = head;
        while (current != null) {
            result.add(current.staff + " has been assigned to " + current.room);
            current = current.next;
        }
        return result;
    }

}
