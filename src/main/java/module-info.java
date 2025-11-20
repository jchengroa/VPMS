module ph.edu.dlsu.lbycpa2.vpms {
    requires javafx.controls;
    requires javafx.fxml;


    opens ph.edu.dlsu.lbycpa2.vpms to javafx.fxml;
    exports ph.edu.dlsu.lbycpa2.vpms;
    exports ph.edu.dlsu.lbycpa2.vpms.controllers;
    opens ph.edu.dlsu.lbycpa2.vpms.controllers to javafx.fxml;
    exports ph.edu.dlsu.lbycpa2.vpms.models;
    opens ph.edu.dlsu.lbycpa2.vpms.models to javafx.fxml;
}