package TwoFactorManager;

import com.google.zxing.*;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class QRUtils {
    public static void main(String[] args){
        encodeQRCode("Hello World");
    }
    private static String path = "./tmpQR.png";
    /**
     * 生成二维码
     * @param text 内容，可以是链接或者文本
     */
    public static void encodeQRCode(String text) {
        encodeQRCode(text, path, null, null, null);
    }

    /**
     * 生成二维码
     * @param text 内容，可以是链接或者文本
     * @param path 生成的二维码位置
     * @param width 宽度，默认300
     * @param height 高度，默认300
     * @param format 生成的二维码格式，默认png
     */
    public static void encodeQRCode(String text, String path, Integer width, Integer height, String format) {
        try {

            File file = new File(path);
            // 判断目标文件所在的目录是否存在
            if(!file.getParentFile().exists()) {
                // 如果目标文件所在的目录不存在，则创建父目录
                if(!file.getParentFile().mkdirs()) {
                    return;
                }
            }
            // 宽
            if (width == null) {
                width = 300;
            }
            // 高
            if (height == null) {
                height = 300;
            }
            // 图片格式
            if (format == null) {
                format = "png";
            }

            // 设置字符集编码
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            // 生成二维码矩阵
            BitMatrix bitMatrix = new MultiFormatWriter().encode(text, BarcodeFormat.QR_CODE, width, height, hints);
            // 二维码路径
            Path outputPath = Paths.get(path);
            // 写入文件
            MatrixToImageWriter.writeToPath(bitMatrix, format, outputPath);
        } catch (WriterException | IOException e) {
            e.printStackTrace();
        }
    }
}
