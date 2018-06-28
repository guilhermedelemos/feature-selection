package io.github.guilhermedelemos.fs.gui.view;

import io.github.guilhermedelemos.fs.gui.controller.MainController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import java.io.IOException;

public class MainView {

    private Stage stage;
    private Application mainApp;
    /*private TextField textFieldDatasetAprendizado;
    private TextField textFieldDatasetTeste;
    private TextField textFieldGeracoes;
    private TextField textFieldCrossover;
    private TextField textFieldMutacao;
    private ComboBox<String> comboboxAlgoritmo;
    private Button buttonExecutar;*/

    public MainView() {
        super();
    }

    public MainView(Application mainApp, Stage stage) {
        super();
        this.mainApp = mainApp;
        this.stage = stage;
    }

    public void exibir() {
        try {
            MainController controller = new MainController();
            controller.setMainApp(this.mainApp);
            controller.setPrimaryStage(this.stage);

            Parent root = FXMLLoader.load(getClass().getResource("/view/MainView.fxml"));
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
