module org.example.smith_number {
    requires javafx.controls;
    requires javafx.fxml;


    opens org.example.smith_number to javafx.fxml;
    exports org.example.smith_number;
}