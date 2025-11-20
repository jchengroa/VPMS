package ph.edu.dlsu.lbycpa2.vpms.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;

public class DynamicResourceAllocationController {

    @FXML private ComboBox<String> cmbStaff;
    @FXML private TextField txtRoom;
    @FXML private Button btnAssign;
    @FXML private ListView<String> listAssignments;

    private ResourceAllocator allocator;

    @FXML
    public void initialize() {
        allocator = new ResourceAllocator();
        btnAssign.setOnAction(e -> handleAssign());
    }

    private void handleAssign() {
        String staff = cmbStaff.getValue();
        String room = txtRoom.getText().trim();

        if (staff.isEmpty() || room.isEmpty()) {
            listAssignments.getItems().add("❗ Please fill in both fields.");
            return;
        }

        String result = allocator.assign(staff, room);
        listAssignments.getItems().add(result);

        cmbStaff.getSelectionModel().clearSelection();
        txtRoom.clear();
    }
}