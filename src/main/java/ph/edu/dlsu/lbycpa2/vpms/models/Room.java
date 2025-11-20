package ph.edu.dlsu.lbycpa2.vpms.models;

public class Room {
    private final String roomNumber;
    private final String specialty; // "Surgeon", "Radiologist", etc.

    public Room(String roomNumber, String specialty) {
        this.roomNumber = roomNumber;
        this.specialty = specialty;
    }

    public String getRoomNumber() { return roomNumber; }
    public String getSpecialty() { return specialty; }

    @Override
    public String toString() {
        return "Room " + roomNumber + " (" + specialty + ")";
    }
}
