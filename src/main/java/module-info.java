module com.example.c2p {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.annotation;

    opens com.example.c2p to javafx.graphics, javafx.fxml;
    opens com.example.c2p.model to com.fasterxml.jackson.databind;
    exports com.example.c2p;
}
