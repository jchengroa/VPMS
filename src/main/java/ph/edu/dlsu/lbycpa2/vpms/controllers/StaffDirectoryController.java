package ph.edu.dlsu.lbycpa2.vpms.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import ph.edu.dlsu.lbycpa2.vpms.models.Staff;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class StaffDirectoryController {

    @FXML private TextField txtName;
    @FXML private ComboBox<String> cmbRank;
    @FXML private ComboBox<String> cmbSpecialty;
    @FXML private Button btnInsert;
    @FXML private Button btnSearch;
    @FXML private Button btnInorder;
    @FXML private Button btnPreorder;
    @FXML private Button btnPostorder;
    @FXML private ListView<String> listOutput;


    // keep Staff objects sorted by name (case-insensitive)
    private final TreeSet<Staff> bst = new TreeSet<>((a, b) -> {
        int cmp = a.getName().compareToIgnoreCase(b.getName());
        if (cmp != 0) return cmp;
        // fallback to full string to avoid equality conflict in TreeSet comparator
        return a.toString().compareToIgnoreCase(b.toString());
    });

    // File path here to access staff data
    private static final Path FILE_PATH = Paths.get("src", "main", "java", "ph", "edu", "dlsu", "lbycpa2", "vpms", "data", "staff.txt");


    @FXML
    public void initialize() {
        btnInsert.setOnAction(e -> insertStaff());
        btnSearch.setOnAction(e -> searchStaff());
        btnInorder.setOnAction(e -> showInorder());
        btnPreorder.setOnAction(e -> showPreorder());
        btnPostorder.setOnAction(e -> showPostorder());

        loadStaffFromFile();
    }



    // FILE HANDLING
    private void loadStaffFromFile() {
        try {
            // ensure parent directories exist (helpful if file path folders are missing)
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


    // UI ACTIONS
    @FXML
    private void insertStaff() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/ph/edu/dlsu/lbycpa2/vpms/SD-Insert-View.fxml")
            );
            Parent newRoot = loader.load();

            // Get the current window from ANY UI element
            Stage currentStage = (Stage) btnInsert.getScene().getWindow();

            // Replace the scene in the same window
            currentStage.setScene(new Scene(newRoot));
            currentStage.setTitle("Insert New Staff");

        } catch (IOException e) {
            e.printStackTrace();
            listOutput.getItems().add("Failed to open insert page: " + e.getMessage());
        }
    }


    private void searchStaff() {
        String input = txtName.getText().trim().toLowerCase();

        listOutput.getItems().clear(); // refresh results

        if (input.isEmpty()) {
            listOutput.getItems().add("Enter name, rank, or specialty to search.");
            return;
        }

        listOutput.getItems().add("Searching for: " + input);

        // Perform all match types in ONE pass
        var matches = bst.stream()
                .filter(s -> {
                    String name = s.getName().toLowerCase();

                    // split into words (first name, last name, etc.)
                    String[] parts = name.split("\\s+");

                    // Name match rules:
                    // 1. Full name contains input
                    boolean fullNameMatch = name.contains(input);

                    // 2. Any word (first or last name) starts with or equals input
                    boolean partMatch = false;
                    for (String p : parts) {
                        if (p.startsWith(input) || p.equals(input)) {
                            partMatch = true;
                            break;
                        }
                    }

                    // Rank or Specialty match
                    boolean rankMatch = s.getRank().equalsIgnoreCase(input);
                    boolean specialtyMatch = s.getSpecialty().equalsIgnoreCase(input);

                    return fullNameMatch || partMatch || rankMatch || specialtyMatch;
                })
                .toList();

        if (matches.isEmpty()) {
            listOutput.getItems().add("No matching staff found.");
        } else {
            listOutput.getItems().add("=== Results ===");
            matches.forEach(s -> listOutput.getItems().add(s.toString()));
        }
    }



    private void searchByName(String name) {
        listOutput.getItems().clear();

        bst.stream()
                .filter(staff -> staff.getName().equalsIgnoreCase(name))
                .forEach(staff -> listOutput.getItems().add("Found: " + staff));
    }

    private void searchByRank(String rank) {
        listOutput.getItems().clear();

        bst.stream()
                .filter(staff -> staff.getRank().equalsIgnoreCase(rank))
                .forEach(staff -> listOutput.getItems().add("Match: " + staff));
    }

    private void searchBySpecialty(String specialty) {
        listOutput.getItems().clear();

        bst.stream()
                .filter(staff -> staff.getSpecialty().equalsIgnoreCase(specialty))
                .forEach(staff -> listOutput.getItems().add("Match: " + staff));
    }


    private void showInorder() {
        listOutput.getItems().clear();

        if (bst.isEmpty()) {
            listOutput.getItems().add("No staff available.");
            return;
        }

        listOutput.getItems().add("Inorder Traversal:");

        bst.forEach(s -> listOutput.getItems().add(s.toString()));
    }

    private java.util.List<Staff> asList() {
        return bst.stream().collect(Collectors.toList());
    }

    private void showPreorder() {
        listOutput.getItems().clear();

        var list = asList();
        if (list.isEmpty()) {
            listOutput.getItems().add("No staff available.");
            return;
        }

        listOutput.getItems().add("Preorder Traversal:");
        preorderHelper(list, 0, list.size() - 1);
    }

    private void preorderHelper(java.util.List<Staff> arr, int left, int right) {
        if (left > right) return;

        int mid = (left + right) / 2;

        listOutput.getItems().add(arr.get(mid).toString());

        preorderHelper(arr, left, mid - 1);
        preorderHelper(arr, mid + 1, right);
    }


    private void showPostorder() {
        listOutput.getItems().clear();

        var list = asList();
        if (list.isEmpty()) {
            listOutput.getItems().add("No staff available.");
            return;
        }

        listOutput.getItems().add("Postorder Traversal:");
        postorderHelper(list, 0, list.size() - 1);
    }

    private void postorderHelper(java.util.List<Staff> arr, int left, int right) {
        if (left > right) return;

        int mid = (left + right) / 2;

        postorderHelper(arr, left, mid - 1);
        postorderHelper(arr, mid + 1, right);

        listOutput.getItems().add(arr.get(mid).toString());
    }

}
