package TwoFactorManager;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;

public class GlobalMenu extends ContextMenu {
    private static GlobalMenu INSTANCE = null;

    private GlobalMenu(String num) {
        MenuItem editMenuItem = new MenuItem("编辑");
        MenuItem delMenuItem = new MenuItem("删除");

        getItems().addAll(editMenuItem, delMenuItem);
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