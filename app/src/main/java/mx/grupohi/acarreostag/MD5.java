package mx.grupohi.acarreostag;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by JFEsquivel on 27/09/2016.
 */

public class MD5 {

    private String cadena;

    public MD5(String s) {
        this.cadena = s;
    }

    public String convert() {
        try {
            MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
            digest.update(cadena.getBytes());
            byte messageDigest[] = digest.digest();

            StringBuffer hexString = new StringBuffer();
            for (int i=0; i<messageDigest.length; i++)
                hexString.append(Integer.toHexString(0xFF & messageDigest[i]));
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }
}
