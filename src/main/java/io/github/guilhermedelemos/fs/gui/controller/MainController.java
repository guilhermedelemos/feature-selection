package io.github.guilhermedelemos.fs.gui.controller;

import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

public class MainController {

    private Application mainApp;
    private Stage primaryStage;

    @FXML
    private TextField textFieldDatasetAprendizado;
    @FXML
    private TextField textFieldDatasetTeste;
    @FXML
    private TextField textFieldGeracoes;
    @FXML
    private TextField textFieldCrossover;
    @FXML
    private TextField textFieldMutacao;
    @FXML
    private ComboBox<String> comboboxAlgoritmo;
    @FXML
    private Button buttonExecutar;
    @FXML
    private Button buttonBuscarDatasetAprendizado;
    @FXML
    private Button buttonBuscarDatasetTeste;

    public MainController() {
        super();
    }

    @FXML
    private void initialize() {
        //
    }

    public void setMainApp(Application mainApp) {
        this.mainApp = mainApp;
    }

    @FXML
    private void handleExecutar() {
        
    }

    @FXML
    private void handleLocalizarDatasetTreinamento() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Resource File");
        //fileChooser.showOpenDialog(this.primaryStage);
        File file = fileChooser.showOpenDialog(this.primaryStage);
        if (file != null) {
            textFieldDatasetAprendizado.setText(file.getAbsolutePath());
        }
    }

    @FXML
    private void handleLocalizarDatasetTeste() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Resource File");
        //fileChooser.showOpenDialog(this.primaryStage);
        File file = fileChooser.showOpenDialog(this.primaryStage);
        if (file != null) {
            textFieldDatasetTeste.setText(file.getAbsolutePath());
        }
    }

    public Application getMainApp() {
        return mainApp;
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }
}
