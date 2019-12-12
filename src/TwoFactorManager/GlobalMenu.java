package TwoFactorManager;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.stage.Stage;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.Optional;

public class GlobalMenu extends ContextMenu {
    private static GlobalMenu INSTANCE = null;
    private static String num = "";

    private GlobalMenu() {
        MenuItem copyMenuItem = new MenuItem("复制到剪贴板");
        MenuItem addMenuItem = new MenuItem("添加");
        MenuItem editMenuItem = new MenuItem("编辑");
        MenuItem delMenuItem = new MenuItem("删除");
        copyMenuItem.setOnAction(e -> {
            Database.execQuery("SELECT * FROM keys WHERE id = " + num);
            try {
                String secretKey = Database.getRs().getString("key");
                setClipboardString(KeyGen.genCode(secretKey));
            } catch (SQLException | NoSuchAlgorithmException | InvalidKeyException ex) {
                ex.printStackTrace();
            }

        });
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
        getItems().addAll(copyMenuItem ,addMenuItem, editMenuItem, delMenuItem);
    }

    /**
     * 获取实例
     * @return GlobalMenu
     */
    public static GlobalMenu getInstance(String num)
    {
        if (INSTANCE == null) {
            INSTANCE = new GlobalMenu();
        }
        GlobalMenu.num = num;
        return INSTANCE;
    }

    public static void setClipboardString(String text) {
        // 获取系统剪贴板
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        // 封装文本内容
        Transferable trans = new StringSelection(text);
        // 把文本内容设置到系统剪贴板
        clipboard.setContents(trans, null);
    }
}