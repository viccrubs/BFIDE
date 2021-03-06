package viccrubs.bfide.client.controller;

import com.google.gson.Gson;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import viccrubs.bfide.bfmachine.exception.*;
import viccrubs.bfide.client.MainClient;
import viccrubs.bfide.client.log.ApplicationLog;
import viccrubs.bfide.client.model.Log;
import viccrubs.bfide.client.model.LogType;
import viccrubs.bfide.client.socket.Connection;
import viccrubs.bfide.bfmachine.program.ProgramLanguage;
import viccrubs.bfide.client.undoredo.UndoRedoController;
import viccrubs.bfide.model.ProjectInfo;
import viccrubs.bfide.model.User;
import viccrubs.bfide.model.Version;
import viccrubs.bfide.model.request.*;
import viccrubs.bfide.model.response.*;
import viccrubs.bfide.util.DateUtil;

import java.io.IOException;
import java.util.function.Consumer;

public class MainWindowController  {
    private static final String LOG_OUT = "Logged out.";


    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
        this.userButton.setText(user.username);
    }

    public Connection getConnection() {
        return connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
        this.connection.setOnConnectionLost(this::logout);
    }

    public ProgramLanguage getLanguage() {
        return language;
    }

    public void setLanguage(ProgramLanguage language) {
        this.language = language;
        this.textLanguage.setText(language.toString());

    }

    public void setStage(Stage stage){
        this.appStage = stage;
    }

    public void setCurrentProjectName(String name){
        this.textCurrentProject.setText(name);
    }

    private User user;
    private Connection connection;
    private ProgramLanguage language;
    private ApplicationLog appLog = new ApplicationLog();
    private Stage appStage;
    private ProjectInfo currentProject;
    private Version currentVersion;
    private UndoRedoController undoController = new UndoRedoController();

    @FXML
    private MenuItem miSave;

    @FXML
    private MenuButton userButton;
    @FXML
    private Button btnRun;
    @FXML
    private Button btnRunWithoutInput;

    @FXML
    private TextArea textCode;

    @FXML
    private TextArea textInput;
    @FXML
    private TextArea textOutput;
    @FXML
    private Text textLanguage;
    @FXML
    private Text textStatus;
    @FXML
    private Menu menuVersionControl;
    @FXML
    private Text textCurrentProject;
    @FXML
    private Button btnUndo;
    @FXML
    private Button btnRedo;



    @FXML
    public void initialize(){
        setLanguage(ProgramLanguage.BF);
        textStatus.setText(appLog.getLogList().get(appLog.getLogList().size()-1).getDescription());
        setCurrentProjectName("Get Started by Opening or Creating a New Project");
        textCode.textProperty().addListener(((observable, oldValue, newValue) -> {
            undoController.add(newValue);
            judgeUndoRedoAvailability();
        }));

    }

    public void judgeUndoRedoAvailability(){
        btnUndo.setDisable(!undoController.canUndo());
        btnRedo.setDisable(!undoController.canRedo());
    }

    public void redo(){
        undoController.redo().ifPresent(x->{
            textCode.setText(x);
            judgeUndoRedoAvailability();
        });
    }

    public void undo(){
        undoController.undo().ifPresent(x->{
            textCode.setText(x);
            judgeUndoRedoAvailability();
        });
    }

    public void openOpenDialog(){
        OpenProjectDialogController controller = OpenProjectDialogController.open();
        if (controller!=null){
            controller.setConnection(connection);
            controller.setCurrentProject(currentProject);
            controller.registerOnProjectSelect(this::setCurrentProject);
            controller.registerOnProjectCreate(this::openNewDialog);
        }

    }

    public void openNewDialog(){
        NewProjectDialogController controller = NewProjectDialogController.open(connection);
        if (controller!=null){
            controller.registerOnProjectCreated(this::setCurrentProject);
        }
    }

    public void setCurrentProject(ProjectInfo info){
        this.currentProject = info;
        this.setLanguage(info.language);
        this.setCurrentProjectName(info.projectName+"@ ");
        this.setCurrentVersion(info.versions[0]);
        menuVersionControl.getItems().clear();
        for(Version version: currentProject.versions){
            addVersionToMenu(version);
        }
        btnRun.setDisable(false);
        btnRunWithoutInput.setDisable(false);
        textCode.setEditable(true);
        textInput.setEditable(true);
        miSave.setDisable(false);

        textInput.clear();
        textInput.clear();
    }

    public void addVersionToMenu(Version... versions){
        for(Version version : versions){
            VersionMenuItem item = new VersionMenuItem(version);
            item.registerOnSelect(this::setCurrentVersion);
            menuVersionControl.getItems().add(item);
        }
    }


    public void setCurrentVersion(Version version){
        GetASpecificVersionResponse res = (GetASpecificVersionResponse)connection.sendRequest(new GetASpecificVersionRequest(currentProject, version));
        if (res!=null){
            undoController.initialize(res.content);
            textCode.setText(res.content);

            textCurrentProject.setText(textCurrentProject.getText().split("@")[0]+"@"+ DateUtil.format(res.version.version));
            currentVersion = res.version;
        }
    }


    public void showLogs(){
        appLog.showLogViewer();
    }

    public void addLog(String log){
        addLog(log, LogType.Notification);
    }

    public void addLog(String log, LogType type){
        appLog.addLog(new Log(log, type));
        textStatus.setText(log);
    }

    public void logout(){
        try {
            addLog(LOG_OUT);
            connection.close();
            FXMLLoader loader = new FXMLLoader(MainClient.class.getResource("/fxml/LoginPanel.fxml"));
            Scene scene = new Scene(loader.load());
            LoginPanelController controller = loader.getController();
            controller.setStage(appStage);
            appStage.setScene(scene);
            appStage.sizeToScene();
            appStage.setTitle("LoginPanel");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void onRun(){

        executeProgram(textInput.getText());
    }

    public void executeProgram(String input){
        if (textCode.getText().length()==0){
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setHeaderText(null);
            alert.setContentText("Please input something.");
            alert.showAndWait();
            return;
        }
        ExecuteProgramRequest request = new ExecuteProgramRequest(textCode.getText().replace("\r\n","\n"), input, language);
        ExecuteProgramResponse response = (ExecuteProgramResponse)connection.sendRequest(request);
        if (response.exceptionDescription != null){
            handleExecutionException(response.exception, response.exceptionDescription);
        }else{
            textOutput.setText(response.output);
        }

    }

    public void handleExecutionException(String type, String description){
        textOutput.setText("Exception "+type+" occurred."+System.lineSeparator()+"Message from server:"+System.lineSeparator()+"-----------------------------------"+System.lineSeparator()+description);
        addLog(type, LogType.Error);
    }


    public void onRunWithoutInput(){
        executeProgram("");
    }

    public void save(){
        SaveVersionResponse res = (SaveVersionResponse)connection.sendRequest(new SaveVersionRequest(textCode.getText().trim(), System.currentTimeMillis(), currentProject));
        if (res.success){
            addLog("Saved successfully.",LogType.Success);
            addVersionToMenu(res.latestVersion);
            setCurrentVersion(res.latestVersion);
        }else{
            addLog("Not saved. Nothing has been changed.");
        }
    }

    public void openAbout(){
        AboutController.open();
    }

    public void updateProjectInfo(){
        GetProjectInfoResponse res = (GetProjectInfoResponse)connection.sendRequest(new GetProjectInfoRequest(currentProject.projectName));
        setCurrentProject(res.info);
    }
}

class VersionMenuItem extends MenuItem{
    private final Version version;
    private Consumer<Version> eventOnSelect;
    public VersionMenuItem(Version version){
        super();
        this.version = version;
        this.setText(DateUtil.format(version.version));
        this.setOnAction(e->{
            if (eventOnSelect!=null){
                eventOnSelect.accept(this.version);
            }
        });
    }

    public void registerOnSelect(Consumer<Version> event){
        eventOnSelect = event;
    }


}
