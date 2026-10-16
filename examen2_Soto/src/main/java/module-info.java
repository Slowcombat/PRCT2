module co.edu.poli.examen2_Soto {
    requires javafx.controls;
    requires javafx.fxml;
	requires java.sql;
	requires javafx.graphics;
	requires io.github.cdimascio.dotenv.java;

    opens co.edu.poli.examen2_Soto.vista to javafx.fxml, javafx.graphics;
    opens co.edu.poli.examen2_Soto.controlador to javafx.fxml;
    exports co.edu.poli.examen2_Soto.controlador;
    
}
