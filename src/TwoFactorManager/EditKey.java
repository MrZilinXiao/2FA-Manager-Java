package TwoFactorManager;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.Optional;

public class EditKey {
    String keyNum = "";
    String secretKey = "";

    TextField idField = new TextField();
    TextField nameTextField = new TextField();
    TextField dynPassField = new TextField();
    ProgressBar p = new ProgressBar();

    Thread t = null;


    EditKey(String keyNum){
        this.keyNum = keyNum;
        assert !keyNum.equals("");
        try{
            Database.execQuery("SELECT * FROM keys WHERE id = " + keyNum);
            idField.setText(keyNum);
            nameTextField.setText(Database.getRs().getString("name"));
            secretKey = Database.getRs().getString("key");
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
        Database.execQuery("UPDATE `keys` SET name = '" + newName +"' WHERE id = " + keyNum);
    }
    private void delRecord(){
        Database.execQuery(String.format("DELETE FROM `keys` WHERE id = %s", keyNum));
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
        Scene editKeyScene = new Scene(editKeyPane, 450, 475);
        editKeyScene.getStylesheets().add(getClass().getResource("Pic.css").toExternalForm());
        Label idLabel = new Label("序号:");
        idField.setEditable(false);
        editKeyPane.add(idLabel, 0, 0);
        editKeyPane.add(idField, 1, 0);

        Label secretName = new Label("名称:");
        editKeyPane.add(secretName, 0, 1);
        editKeyPane.add(nameTextField, 1, 1);

        Label dynLabel = new Label("动态密码:");
        dynPassField.setEditable(false);
        editKeyPane.add(dynLabel, 0,2);
        editKeyPane.add(dynPassField, 1, 2);

        QRUtils.encodeQRCode("otpauth://totp/" + nameTextField.getText() +"?secret=" + secretKey);

        Image image = new Image("file:./tmpQR.png");
        ImageView imageView = new ImageView(image);
        HBox imageBox = new HBox(imageView);
        imageBox.setAlignment(Pos.CENTER);
        Label QRLabel = new Label("需要转移？");
        editKeyPane.add(QRLabel, 0, 3);
        editKeyPane.add(imageBox, 1,3);

        HBox buttonBox = new HBox();
        buttonBox.setSpacing(10);
        Button editBtn = new Button("编辑");
        Button delBtn = new Button("删除");
        buttonBox.getChildren().addAll(editBtn, delBtn);
        editKeyPane.add(buttonBox, 1, 4);
        editKeyPane.add(p, 0,4);

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
