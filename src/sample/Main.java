package sample;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.Date;
interface item{
    void add();
    void delete();
}

class logger implements item{ // 日志类
    protected Date logTime;
    protected String logTitle;
    protected String logContent;
    @Override
    public void add() {
        // 方法体暂时空置
    }

    @Override
    public void delete() {
// 方法体暂时空置
    }
}

class key implements item{
    protected String id; // 作为数据库中的主键
    protected String title; // 密匙标题
    protected String publicKey; // 双方均持有的公钥
    protected key(String _id, String _title, String _publicKey){
        id = _id;
        title = _title;
        publicKey = _publicKey;
    }
    @Override
    public String toString(){
        return title;
    }

    @Override
    public void add() {
// 方法体暂时空置
    }

    @Override
    public void delete() {
// 方法体暂时空置
    }
}

class keyAppended extends key {
    protected Date timeAppended;
    public keyAppended(String _id, String _title, String _publicKey) {
        super(_id, _title, _publicKey);
    }
    public keyAppended(String _id, String _title, String _publicKey, Date _timeAppended) {
        super(_id, _title, _publicKey);
        timeAppended = _timeAppended;
    }
}

final class keyRemoved extends keyAppended{ // 添加后的密匙被删除 将存在回收站中
    protected Date timeRemoved;
    public keyRemoved(String _id, String _title, String _publicKey, Date _timeAppended, Date _timeRemoved) {
        super(_id, _title, _publicKey, _timeAppended);
        timeRemoved = _timeRemoved;
    }
}

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        BorderPane pane = new BorderPane();  // BorderPane

        HBox buttonBox = new HBox(); // 放置基本操作按钮
        Button importKeyBtn = new Button("导入密匙"); // 导入密匙
        Button exportKeyBtn = new Button("导出密匙"); // 导出密匙
        Button addKeyBtn = new Button("添加密匙"); // 添加单个密匙
        Button exitBtn = new Button("退出");  // 退出


        buttonBox.setPadding(new Insets(15,12,15,12));
        buttonBox.setSpacing(10);
        buttonBox.setStyle("-fx-background-color: #336699;");
        buttonBox.getChildren().addAll(importKeyBtn, exportKeyBtn, addKeyBtn, exitBtn);
        addKeyBtn.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 1) {
                    Stage addKeyStage = new Stage();
                    addKeyStage.setTitle("2FA Manager--添加密匙");
                    GridPane addKeyPane = new GridPane();
                    addKeyPane.setAlignment(Pos.CENTER);
                    addKeyPane.setHgap(10);
                    addKeyPane.setVgap(10);
                    addKeyPane.setPadding(new Insets(25,25,25,25));
                    Scene addKeyScene = new Scene(addKeyPane, 300, 275);
                    addKeyStage.setScene(addKeyScene);

                    Text addKeyTitle = new Text("欢迎添加密匙：");
                    addKeyTitle.setFont(Font.font("Tahoma", FontWeight.NORMAL, 20));
                    addKeyPane.add(addKeyTitle, 0, 0, 2, 1);

                    Label userName = new Label("TOTP链接:");
                    addKeyPane.add(userName, 0, 1);

                    TextField userTextField = new TextField();
                    addKeyPane.add(userTextField, 1, 1);

                    Button submitBtn = new Button("添加");
                    HBox hbBtn = new HBox(10);
                    hbBtn.setAlignment(Pos.BOTTOM_RIGHT);
                    hbBtn.getChildren().add(submitBtn);
                    addKeyPane.add(hbBtn, 1, 4);

                    addKeyStage.show();
                }
            }
        });

        exitBtn.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 1) {
                    System.exit(0);
                }
            }
        });

        VBox keyBox = new VBox();
        keyBox.setPadding(new Insets(10));
        keyBox.setSpacing(8);
        Text keyBoxTitle = new Text("已添加的密匙");
        keyBoxTitle.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        ObservableList<keyAppended> keyList = FXCollections.observableArrayList();
        ListView<keyAppended> keyListView = new ListView<>(keyList);
        keyListView.setItems(keyList);
        keyListView.setPrefSize(800,400);
        keyList.add(new keyAppended("123", "PLACEHOLDER", "ABCD"));
        keyBox.getChildren().addAll(keyBoxTitle, keyListView);

        primaryStage.setTitle("2FA Manager");
        pane.setTop(buttonBox);
        pane.setCenter(keyBox);

        primaryStage.setScene(new Scene(pane, 1000, 400));
        primaryStage.show();
    }



    public static void main(String[] args) {
        launch(args);
    }
}
