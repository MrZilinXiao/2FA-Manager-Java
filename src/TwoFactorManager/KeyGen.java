package TwoFactorManager;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

import org.apache.commons.codec.binary.Base32;


public class KeyGen {
    private int window_size = 1;

    public static void main(String[] args) throws InvalidKeyException, NoSuchAlgorithmException, InterruptedException {
        String testKey = "fzfnmv2hrbin3ci5";
        while(true){
            long t = (new Date().getTime() / 1000L) / 30L;
            System.out.println(verify_code(testKey, t));
            Thread.sleep(1000);
        }
    }

    public boolean check_code(String secret, long code, long timeMsec) {
        // convert unix msec time into a 30 second "window"
        // this is per the TOTP spec (see the RFC for details)
        long t = (timeMsec / 1000L) / 30L;
        // window是用来检验之前生成的6位码
        // 可以用这个window_size来调整允许6位码生效的时间

        for (int i = -window_size; i <= window_size; ++i) {
            long hash;
            try {
                hash = verify_code(secret, t + i);
            }catch (Exception e) {
                // Yes, this is bad form - but
                // the exceptions thrown would be rare and a static configuration problem
                e.printStackTrace();
                throw new RuntimeException(e.getMessage());
                //return false;
            }
            if (hash == code) {
                return true;
            }
        }
        // The validation code is invalid.
        return false;
    }
    /**
     * 生成验证码
     * @param key
     * @param t
     * @return
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException
     */

    public static int verify_code(String key, long t) throws NoSuchAlgorithmException, InvalidKeyException {
        Base32 codec = new Base32();
        byte[] decodedKey = codec.decode(key);

        byte[] data = new byte[8];
        long value = t;
        for (int i = 8; i-- > 0; value >>>= 8) {
            data[i] = (byte) value;
        }
        SecretKeySpec signKey = new SecretKeySpec(decodedKey, "HmacSHA1");
        Mac mac = Mac.getInstance("HmacSHA1");
        mac.init(signKey);
        byte[] hash = mac.doFinal(data);
        int offset = hash[20 - 1] & 0xF;
        // We're using a long because Java hasn't got unsigned int.
        long truncatedHash = 0;
        for (int i = 0; i < 4; ++i) {
            truncatedHash <<= 8;
            // We are dealing with signed bytes:
            // we just keep the first byte.
            truncatedHash |= (hash[offset + i] & 0xFF);
        }
        truncatedHash &= 0x7FFFFFFF;
        truncatedHash %= 1000000;
        return (int) truncatedHash;
    }
}
