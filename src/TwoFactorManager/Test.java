package TwoFactorManager;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Test {

    private static String getParam(String param){
        String pattern = "(^|&|\\?)"+ param +"=([^&]*)(&|$)";
        String testURL = "otpauth://totp/GitHub:GodKillerXiao?secret=fzfnmv2hrbin3ci5&issuer=GitHub";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(testURL);
        return m.find() ? m.group(0) : null;
    }
    public static void main(String[] args) {
        String hit = getParam("issuer");
        assert hit != null;
        if(hit.endsWith("&")){
            hit = hit.substring(hit.indexOf("=") + 1, hit.length() - 1);
        }else{
            hit = hit.substring(hit.indexOf("=") + 1);
        }
        System.out.println(hit);
    }
}
