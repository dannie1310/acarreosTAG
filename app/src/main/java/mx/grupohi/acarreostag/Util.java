package mx.grupohi.acarreostag;

import android.content.ContentValues;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;

/**
 * Creado por JFEsquivel on 04/10/2016.
 */

public class Util {
    public static boolean isNetworkStatusAvialable (Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null)
        {
            NetworkInfo netInfos = connectivityManager.getActiveNetworkInfo();
            if(netInfos != null)
                if(netInfos.isConnected())
                    return true;
        }
        return false;
    }

    public static String getQuery(ContentValues values) throws UnsupportedEncodingException
    {
        StringBuilder result = new StringBuilder();
        boolean first = true;

        for (Map.Entry<String, Object> entry : values.valueSet())
        {
            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(String.valueOf(entry.getValue()), "UTF-8"));
        }
        return result.toString();
    }

    public static JSONObject JsonHttp(URL url, ContentValues values) throws JSONException {

        String response = null;
        try {

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");

            OutputStream os = conn.getOutputStream();

            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(os));
            bw.write(Util.getQuery(values));
            bw.flush();

            int statusCode = conn.getResponseCode();

            Log.i("Status Code",String.valueOf(statusCode));

            InputStream is = conn.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();
            String line, toAppend;

            toAppend = br.readLine() + "\n";
            sb.append(toAppend);
            while ((line = br.readLine()) != null) {
                toAppend = line + "\n";
                sb.append(toAppend);
            }

            is.close();
            response = sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return  new JSONObject(response);
    }

    static Integer getIdCamion(String string) {

        try {
            String cadena= string.substring(0,4);
            Integer result = Integer.valueOf(cadena);
            if (result != null) {
                return result;
            } else {
                return null;
            }
        } catch (Exception e) {
            return null;
        }
    }
}
