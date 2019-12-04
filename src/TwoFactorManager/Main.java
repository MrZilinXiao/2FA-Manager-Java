package TwoFactorManager;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class Main extends Application {
    public static final int pauseTime = 1000; // milliseconds
    public static final int timeGap = 30; // in second

    // SQL Related
    private static Connection conn = null;
    public static ResultSet rs = null;
    public static Statement statement = null;

    private List<Key> currList = null;

    // Some refreshable items
    private TableColumn<Key, String> firstColumn = null;
    private TableColumn<Key, String> secondColumn = null;
    private TableColumn<Key, String> thirdColumn = null;
    private TableView<Key> tableView = null;
    public static ProgressBar pTime = new ProgressBar();


    static long getTimeIndex(){
        return (System.currentTimeMillis() / 1000) % 31;
    }

    // private
    @Override
    public void init(){
        firstColumn = getColumn("序号", "num", 50, 50); //设置该列取值对应的属性名称。此处序号列要展示Key的num属性值
        secondColumn = getColumn("名称", "name", 180, 180);
        thirdColumn = getColumn("动态密码", "dynPassWord", 180,180);

        initSQL();
        currList = initKeys();
        ObservableList<Key> obList = FXCollections.observableArrayList(currList); //把清单对象转换成Javafx控件能够识别的数据对象
        tableView = new TableView<Key>(obList); //依据指定数据创建表格视图
        tableView.setRowFactory(keyTableView -> {
            TableRow<Key> row = new TableRow<>();
            row.setOnMouseClicked(mouseEvent -> {
                if(mouseEvent.getClickCount() == 2 && (!row.isEmpty())){
                    Key rowData = row.getItem();
                    EditKey e = new EditKey(rowData.getNum());
                    Stage editKeyStage = e.EditKey();
                    editKeyStage.show();
                    editKeyStage.setOnHiding(windowEvent->{
                        System.out.println("Editing complete");
                        refreshKeys();
                    });
                }
            });
            return row;
        });
    }

    private void initSQL(){
        try {
            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection("jdbc:sqlite:./code.db");
            statement = conn.createStatement();
        }catch (ClassNotFoundException | SQLException ex){
            System.out.println(ex.getMessage());
            ex.printStackTrace();
        }
    }

    private List<Key> initKeys(){
        List<Key> tmp = new java.util.ArrayList<>(Collections.emptyList());
        try{
            rs = statement.executeQuery("SELECT * FROM keys");
            while(rs.next()){
                String dynPassWord = KeyGen.genCode(rs.getString("key"));
                tmp.add(new Key(rs.getString("id"), rs.getString("name"), dynPassWord));
                System.out.println("Item " + rs.getString("id") + " with current Code " + dynPassWord);
            }
        }catch (SQLException ex){
            System.out.println(ex.getMessage());
            ex.printStackTrace();
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            e.printStackTrace();
        }
        return tmp;
    }

    private void refreshKeys(){
        currList = initKeys();
        tableView.setItems(FXCollections.observableArrayList(currList));
        System.out.println("Keys refreshed!");
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        GridPane pane = new GridPane();
        pane.setAlignment(Pos.TOP_LEFT);
        pane.setPadding(new Insets(10));
        pane.setHgap(10);
        pane.setVgap(10);

        HBox optionBox = new HBox();
        Button addBtn = new Button("添加密匙");
        Button exitBtn = new Button("退出");
        optionBox.setPadding(new Insets(15, 12, 15, 12));
//        optionBox.setStyle("-fx-background-color: rgb(118,120,101);");
//        addBtn.setStyle("-fx-background-color: #fdb047;");
//        exitBtn.setStyle("-fx-background-color: #fdb047;");

        Scene scene = new Scene(pane, 410, 350);
        scene.getStylesheets().add(getClass().getResource("Pic.css").toExternalForm());

        optionBox.getChildren().addAll(addBtn, exitBtn);
        optionBox.setSpacing(10);
        optionBox.setAlignment(Pos.CENTER_LEFT);

        addBtn.setOnAction(e -> {
            AddKey addKeyWindow = new AddKey();
            Stage addKeyStage = addKeyWindow.addKey();
            addKeyStage.show();
            addKeyStage.setOnHiding(windowEvent -> {
                System.out.println("Adding Complete");
                refreshKeys();  // refresh TableView when finishing adding
            });
        });//addWindow是添加密匙的类

        exitBtn.setOnAction(e -> {
            System.exit(0);
        });

        pane.add(optionBox, 0, 0);
        VBox vbox = new VBox();   // 放置表格

        VBox pBox = new VBox();

        pBox.getChildren().add(pTime);
        pane.add(pBox, 0, 2);

        pTime.setProgress(0);
        pTime.setPrefWidth(400);

        Task pWorker = createWorker();  // 进度条线程
        pTime.progressProperty().unbind();
        pTime.progressProperty().bind(pWorker.progressProperty());

        new Thread(pWorker).start();


        tableView.setPrefSize(400, 210);//设置表格视图的推荐宽高

        tableView.getColumns().addAll(firstColumn, secondColumn, thirdColumn);//将标题列加到表格视图
        //选中找不到右键是啥就写双击了
        tableView.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
		    			/*menuWindow menu = new menuWindow();
		    			menu.start();*/
                //menu界面
                System.out.println("yyy");
            }
        });


        vbox.getChildren().add(tableView);//把表格加到垂直箱子

        pane.add(vbox, 0, 1);

        primaryStage.setResizable(false);
        primaryStage.setTitle("2FA Manager");
        primaryStage.setScene(scene);
        primaryStage.show();


    }

    private Task createWorker(){
        return new Task() {
            @Override
            protected Object call() throws Exception {
                while(true){
                    for (long i = getTimeIndex();
                         i <= timeGap; i = getTimeIndex()) {
                        updateProgress((1.0 * i) / timeGap, 1);
                        Thread.sleep(pauseTime);
                        if(i == timeGap) refreshKeys();
                    }
                }
            }
        };
    }

    private TableColumn<Key, String> getColumn(String columnName, String propertyName, int width, int maxWidth) {
        TableColumn<Key, String> tableColumn = new TableColumn<>(columnName);
        tableColumn.setCellValueFactory(new PropertyValueFactory<>(propertyName));
        tableColumn.setMinWidth(width);
        tableColumn.setPrefWidth(width);
        tableColumn.setMaxWidth(maxWidth);
        return tableColumn;
    }



    public static void main(String[] args) {
        launch(args);
    }
}
