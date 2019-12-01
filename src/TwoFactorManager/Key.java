package TwoFactorManager;

import javafx.beans.property.SimpleStringProperty;

public class Key {
    private SimpleStringProperty num;//序号
    private SimpleStringProperty name;
    private SimpleStringProperty dynPassWord;

    public Key() {};
    public Key(String num, String name, String dynPassWord) {
        this.num = new SimpleStringProperty(num);
        this.name = new SimpleStringProperty(name);
        this.dynPassWord = new SimpleStringProperty(dynPassWord);
    }

    public String getNum() {
        return num.get();
    }
    public String getName() {
        return name.get();
    }
    public String getDynPassWord() {
        return dynPassWord.get();
    }
}
