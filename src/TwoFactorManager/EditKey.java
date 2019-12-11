package TwoFactorManager;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import javax.naming.ldap.LdapName;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.util.Date;
import java.util.Optional;

public class EditKey {
    String keyNum = "";
    String secretKey = "";
    // Time addTime = null;

    TextField idField = new TextField();
    TextField nameTextField = new TextField();
    TextField dynPassField = new TextField();
    ProgressBar p = new ProgressBar();

    Thread t = null;


    EditKey(String keyNum){
        this.keyNum = keyNum;
        Statement st = Main.statement;
        ResultSet rs = Main.rs;
        assert !keyNum.equals("");
        try{
            st.executeQuery("SELECT * FROM keys WHERE id = " + keyNum);
            idField.setText(keyNum);
            nameTextField.setText(rs.getString("name"));
            secretKey = rs.getString("key");
            // addTime = rs.getTime("addTime");

            Task pWorker = createWorker();  // 进度条线程
            p.progressProperty().unbind();
            p.progressProperty().bind(pWorker.progressProperty());
            t = new Thread(pWorker);
            t.start();


            dynPassField.setText(KeyGen.genCode(secretKey));

        } catch (SQLException | InvalidKeyException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    private void updateDB(String newName){
        Statement st = Main.statement;
        try{
            st.executeQuery("UPDATE `keys` SET name = '" + newName +"' WHERE id = " + keyNum);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    private void delRecord(){
        Statement st = Main.statement;
        try{
            st.executeQuery(String.format("DELETE FROM `keys` WHERE id = %s", keyNum));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Stage EditKey(){
        Stage editKeyStage = new Stage();
        editKeyStage.setResizable(false);

        editKeyStage.setTitle("2FA Manager--编辑密匙");
        GridPane editKeyPane = new GridPane();
        editKeyPane.setAlignment(Pos.CENTER);
        editKeyPane.setHgap(10);
        editKeyPane.setVgap(10);
        editKeyPane.setPadding(new Insets(25,25,25,25));
        Scene editKeyScene = new Scene(editKeyPane, 450, 275);
        editKeyScene.getStylesheets().add(getClass().getResource("Pic.css").toExternalForm());
        Label idLabel = new Label("序号:");
        idField.setDisable(true);
        editKeyPane.add(idLabel, 0, 0);
        editKeyPane.add(idField, 1, 0);

        Label secretName = new Label("名称:");
        editKeyPane.add(secretName, 0, 1);
        editKeyPane.add(nameTextField, 1, 1);

        Label dynLabel = new Label("动态密码:");
        dynPassField.setDisable(true);
        editKeyPane.add(dynLabel, 0,2);
        editKeyPane.add(dynPassField, 1, 2);


        HBox buttonBox = new HBox();
        Button editBtn = new Button("编辑");
        Button delBtn = new Button("删除");
        buttonBox.getChildren().addAll(editBtn, delBtn);
        editKeyPane.add(buttonBox, 1, 3);
        editKeyPane.add(p, 0,3);

        editBtn.setOnMouseClicked(e->{
            updateDB(nameTextField.getText());
            editKeyStage.close();
        });

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION, "确认删除吗？");
        delBtn.setOnMouseClicked(e->{
            Optional<ButtonType> result = confirmation.showAndWait();
            if(result.isPresent() && result.get() == ButtonType.OK){
                delRecord();
                editKeyStage.close();
            }
        });

        buttonBox.setAlignment(Pos.BOTTOM_RIGHT);

        nameTextField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String s, String t1) {
                editBtn.setDisable(nameTextField.getText().equals(""));
            }
        });

        editKeyStage.setScene(editKeyScene);

        return editKeyStage;
    }

    private void refreshCode() throws InvalidKeyException, NoSuchAlgorithmException {
        String dynPassWord = KeyGen.genCode(secretKey);
        dynPassField.setText("");
        dynPassField.setText(dynPassWord);
    }

    private Task createWorker(){
        return new Task() {
            @Override
            protected Object call() throws Exception {
                while(true){
                    for (long i = Main.getTimeIndex();
                         i <= Main.timeGap; i = Main.getTimeIndex()) {
                        updateProgress((1.0 * i) / Main.timeGap, 1);
                        Thread.sleep(Main.pauseTime);
                        if(i == Main.timeGap) refreshCode();
                    }
                }
            }
        };
    }
}
