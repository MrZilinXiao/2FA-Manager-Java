package TwoFactorManager;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AddKey {
    String itemName = "";
    String secretKey = "";
    String totpURL = "";
    String dynPassword = "";
    ProgressBar p = new ProgressBar(0);

    TextField dynPassField = new TextField();
    TextField nameTextField = new TextField();
    Label keyTip = new Label("");
    Thread t = null;

    private boolean addKeyToDB(){
        Statement st = Main.statement;
        assert !itemName.equals("") && !secretKey.equals("");
        try{
            st.executeUpdate("INSERT INTO `keys` (`name`, `key`) VALUES ('" + this.itemName + "','" + this.secretKey + "')");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return true;
    }

    private String getParam(String param){
        String pattern = "(^|&|\\?)"+ param +"=([^&]*)(&|$)";
        // String testURL = "otpauth://totp/GitHub:GodKillerXiao?secret=fzfnmv2hrbin3ci5&issuer=GitHub";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(totpURL);
        return m.find() ? m.group(0) : null;
    }
    public Stage addKey(){
        Stage addKeyStage = new Stage();
        addKeyStage.setResizable(false);


        addKeyStage.setTitle("2FA Manager--添加密匙");
        GridPane addKeyPane = new GridPane();
        addKeyPane.setAlignment(Pos.CENTER);
        addKeyPane.setHgap(10);
        addKeyPane.setVgap(10);
        addKeyPane.setPadding(new Insets(25,25,25,25));
        Scene addKeyScene = new Scene(addKeyPane, 450, 275);

        addKeyStage.setScene(addKeyScene);
        Text addKeyTitle = new Text("欢迎添加秘钥");
        addKeyTitle.setFont(Font.font("Tahoma", FontWeight.NORMAL, 20));
        addKeyPane.add(addKeyTitle, 0, 0, 2, 1);
        Label userName = new Label("秘钥:");
        TextField secretField = new TextField();
        secretField.setPromptText("链接/秘钥均可");
        addKeyPane.add(userName, 0, 1);
        addKeyPane.add(secretField, 1, 1);
        addKeyPane.add(keyTip, 2, 1);

        secretField.setPrefWidth(400);
        nameTextField.setPrefWidth(400);
        dynPassField.setPrefWidth(400);
        keyTip.setPrefWidth(300);


        Label secretName = new Label("名称:");

        nameTextField.setPromptText("输入链接时，此项会自动填充");
        addKeyPane.add(secretName, 0, 2);
        addKeyPane.add(nameTextField, 1, 2);
        Label dynLabel = new Label("动态密码:");

        addKeyPane.add(dynLabel, 0, 3);
        addKeyPane.add(dynPassField, 1, 3);
        addKeyPane.add(p, 1,4);

        dynPassField.setPromptText("输入链接/密匙后，此项会开始更新");
        dynPassField.setDisable(true);

        Button submitBtn = new Button("添加");
        submitBtn.setDisable(true);
        HBox hbBtn = new HBox(10);
        hbBtn.setAlignment(Pos.BOTTOM_RIGHT);
        hbBtn.getChildren().add(submitBtn);
        addKeyPane.add(hbBtn, 1, 4);
        submitBtn.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                if (mouseEvent.getButton() == MouseButton.PRIMARY && mouseEvent.getClickCount() == 1) {
                    addKeyToDB();
                    addKeyStage.close();
                }
            }
        });
        secretField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String s, String t1) {
                if(t1.equals("")){ // empty
                    submitBtn.setDisable(true);
                } else{
                    if(t1.startsWith("otpauth://totp/")){ //   if a link provided
                        totpURL = t1;
                        itemName = t1.substring(t1.indexOf("otpauth://totp/") + "otpauth://totp/".length(), t1.indexOf("?"));

                        nameTextField.setText(itemName);

                        secretKey = getParam("secret");
                        assert secretKey != null;
                        if(secretKey.endsWith("&")){
                            secretKey = secretKey.substring(secretKey.indexOf("=") + 1, secretKey.length() - 1);
                        }else{
                            secretKey = secretKey.substring(secretKey.indexOf("=") + 1);
                        }
                    }else{                              // a secret key provided
                        secretKey = t1;
                        itemName = nameTextField.getText();
                    }
                    try {
                        refreshCode();
                        keyTip.setText("");
                    } catch (InvalidKeyException | NoSuchAlgorithmException | IllegalArgumentException e) {
                        // e.printStackTrace();
                        keyTip.setText("密匙有误！");
                        submitBtn.setDisable(true);
                    }
                    if(t == null){
                        Task pWorker = createWorker();  // 进度条线程
                        p.progressProperty().unbind();
                        p.progressProperty().bind(pWorker.progressProperty());
                        t = new Thread(pWorker);
                        t.start();
                    }
                    submitBtn.setDisable(false);

                }

            }
        });

        return addKeyStage;
    }

    private void refreshCode() throws InvalidKeyException, NoSuchAlgorithmException {
        int dynPasswordInt = KeyGen.verify_code(secretKey, (new Date().getTime() / 1000L) / 30L);
        String dynPassWord = Integer.toString(dynPasswordInt);
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
