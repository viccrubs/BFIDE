package viccrubs.bfide.client.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.MenuButton;
import javafx.scene.control.TextArea;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import viccrubs.bfide.client.MainClient;
import viccrubs.bfide.client.log.ApplicationLog;
import viccrubs.bfide.client.models.Log;
import viccrubs.bfide.client.models.LogType;
import viccrubs.bfide.client.socket.Connection;
import viccrubs.bfide.bfmachine.ProgramLanguage;
import viccrubs.bfide.models.User;
import viccrubs.bfide.models.requests.RunProgramRequest;
import viccrubs.bfide.models.response.RunResultResponse;

import java.io.IOException;

public class MainWindowController  {

    private static final String SWITCH_LANGUAGE = "Language switched to ";
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

    private User user;
    private Connection connection;
    private ProgramLanguage language;
    private boolean programRunning;
    private ApplicationLog appLog = new ApplicationLog();
    private Stage appStage;


    @FXML
    private MenuButton userButton;

    @FXML
    private Button btnRun;

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

    public void switchLanguage(){
        if (language.equals(ProgramLanguage.BF)){
            setLanguage(ProgramLanguage.Ook);

        }else{
            setLanguage(ProgramLanguage.BF);
        }
        addLog(SWITCH_LANGUAGE+language.toString()+".");
    }

    @FXML
    public void initialize(){
        setLanguage(ProgramLanguage.BF);
        textStatus.setText(appLog.getLogList().get(appLog.getLogList().size()-1).getDescription());
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

    public void executeProgram(){
        RunProgramRequest request = new RunProgramRequest(textCode.getText(), textInput.getText(), language);
        RunResultResponse response = (RunResultResponse)connection.sendRequest(request);
        textOutput.setText(response.result.output);
    }

    public void openAbout(){
        AboutController.open();
    }
}
