package TwoFactorManager;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.stage.Stage;

import java.util.Optional;

public class GlobalMenu extends ContextMenu {
    private static GlobalMenu INSTANCE = null;

    private GlobalMenu(String num) {
        MenuItem addMenuItem = new MenuItem("添加");
        MenuItem editMenuItem = new MenuItem("编辑");
        MenuItem delMenuItem = new MenuItem("删除");
        addMenuItem.setOnAction(e -> {
            AddKey addKeyWindow = new AddKey();
            Stage addKeyStage = addKeyWindow.addKey();
            addKeyStage.show();
            addKeyStage.setOnHiding(windowEvent -> {
                System.out.println("Adding Complete");
                Main.refreshKeys();  // refresh TableView when finishing adding
            });
        });
        editMenuItem.setOnAction(e -> {
            EditKey edit = new EditKey(num);
            Stage editKeyStage = edit.EditKey();
            editKeyStage.show();
            editKeyStage.setOnHiding(windowEvent->{
                System.out.println("Editing complete");
                Main.refreshKeys();
            });
        });
        delMenuItem.setOnAction(e -> {
            Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION, "确认删除吗？");
            Optional<ButtonType> result = confirmation.showAndWait();
            if(result.isPresent() && result.get() == ButtonType.OK){
                EditKey.delRecord(num);
                Main.refreshKeys();
            }
        });
        getItems().addAll(addMenuItem, editMenuItem, delMenuItem);
    }

    /**
     * 获取实例
     * @return GlobalMenu
     */
    public static GlobalMenu getInstance(String num)
    {
        if (INSTANCE == null) {
            INSTANCE = new GlobalMenu(num);
        }
        return INSTANCE;
    }
}