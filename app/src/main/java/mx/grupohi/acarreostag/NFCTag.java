package mx.grupohi.acarreostag;

import android.content.Context;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.renderscript.Element;
import android.util.Log;
import android.widget.Toast;


import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;

import static java.nio.charset.StandardCharsets.US_ASCII;

/**
 * Created by Usuario on 29/09/2016.
 */

public class NFCTag {

    private Tag NFCTag;
    Context context;
     private byte[] ky= hexStringToByteArray("773A7DB80405F9669069773A7DB80405");
    private byte[] pw= hexStringToByteArray("773A7DB80405");
    private byte[] def= hexStringToByteArray("FFFFFFFFFFFFFF078069FFFFFFFFFFFF");

    public NFCTag(Tag NFCTag, Context context) {
        this.context=context;
        this.NFCTag = NFCTag;
    }

    void write(String text, Tag tag){

        MifareClassic mfc = MifareClassic.get(tag);

        try {
            mfc.connect();
            int x = 0;
            int y = 0;
            int iw;
            int z = 1;
            int block;
            int auxBlock = 2;
            boolean auth = false;
            byte[] value = text.getBytes();
            System.out.println(value.length);
            if (value.length <= 752) {
                while (x != value.length) {
                    if (y < 16) {
                        auth = mfc.authenticateSectorWithKeyA(y, MifareClassic.KEY_DEFAULT);
                        if (auth) {
                            byte[] toWrite = new byte[MifareClassic.BLOCK_SIZE];

                            for (block = 0; block < auxBlock; block++) {

                                for (iw = 0; iw < MifareClassic.BLOCK_SIZE; iw++) {
                                    if (x < value.length) {
                                        toWrite[iw] = value[x];
                                        x++;

                                    } else {
                                        toWrite[iw] = 0;
                                    }
                                }
                                mfc.writeBlock(z + block, toWrite);
                                toWrite = new byte[MifareClassic.BLOCK_SIZE];
                            }
                            if (z == 1) {
                                z = z + block + 1;
                                auxBlock = auxBlock + 1;
                            } else {
                                z = z + block + 1;
                            }
                        }
                        y = y + 1;
                    }
                }

                Toast.makeText(context, context.getString(R.string.tag_configurado), Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(context, context.getString(R.string.error_tag_capacidad_almacenamiento), Toast.LENGTH_LONG).show();
            }
            mfc.close();


        } catch (Exception fe) {
            Toast.makeText(context, context.getString(R.string.error_tag_comunicacion), Toast.LENGTH_LONG).show();
            fe.printStackTrace();
        }
    }

    boolean writeID(Tag tag, int sector, int bloque, String mensaje){
        MifareClassic mfc = MifareClassic.get(tag);
        int x=0;
        int auxBloque=0;
        try {
            mfc.connect();
            boolean auth = false;
            byte[] value =  mensaje.getBytes();
            auth = mfc.authenticateSectorWithKeyA(sector, pw);
            if (auth) {
             while (x != value.length) {
                 byte[] toWrite = new byte[MifareClassic.BLOCK_SIZE];
                 for (int iw = 0; iw < MifareClassic.BLOCK_SIZE; iw++) {
                     if (x < value.length) {
                         toWrite[iw] = value[x];
                         x++;
                     } else {
                         toWrite[iw] = 0;
                     }
                 }
                 mfc.writeBlock(bloque + auxBloque, toWrite);
                 auxBloque += 1;
                 toWrite = new byte[MifareClassic.BLOCK_SIZE];
             }
            }
            mfc.close();
            return true;
        } catch (Exception fe) {
            fe.printStackTrace();
            return false;
        }
    }

    String concatenar(String idCamion, String idProyecto){
        String resultado="";
        String aux =idCamion;
        String aux1=idProyecto;
        for(int i=idCamion.length(); i<4;i++){
            aux= 0 + aux;
        }
        for(int i=idProyecto.length(); i<4;i++){
            aux1= 0 + aux1;
        }
        resultado= aux+aux1;
        return resultado;
    }

    String read(Tag tag) {
        int y = 0;
        int j = 1;
        int z = 1;
        byte[] toRead = null;
        int block;
        int auxBlock = 2;
        String aux = "";
        MifareClassic mf = MifareClassic.get(tag);
        try {
            mf.connect();
            boolean auth = false;
            for (y = 0; y < 16; y++) {
                auth = mf.authenticateSectorWithKeyA(y, MifareClassic.KEY_DEFAULT);
                if (auth) {
                    for (block = 0; block < auxBlock; block++) {
                        toRead = mf.readBlock(block + z);
                        if (toRead != null) {
                            byte[] limpio = new byte[toRead.length];
                            for (int i = 0; i < toRead.length; i++) {
                                if (toRead[i] != 0) {
                                    limpio[i] += toRead[i];
                                } else {
                                    limpio[i] += ' ';
                                }
                            }
                            String s = new String(limpio);
                            aux += s;
                        }
                    }
                    if (z == 1) {
                        z += block + 1;
                        auxBlock = auxBlock + 1;
                    } else {
                        z += block + 1;
                    }
                }
            }
            mf.close();
        } catch (Exception e) {
            e.printStackTrace();
            return context.getString(R.string.error_conexion_tag);
        }
        //System.out.println("Mensaje tag:  " + aux);
        return aux;
    }

     String readSector (Tag tag, int sector) {
        int z = 1;
        byte[] toRead=null;
        int block;
         int auxBlock;
        if (sector == 0) {
            auxBlock = 2;
        }
        else {
            auxBlock = 3;
            z=sector*4;
        }
        String aux="";
        MifareClassic mf = MifareClassic.get(tag);
        try {
            mf.connect();
            boolean auth = false;
            auth = mf.authenticateSectorWithKeyA(sector, pw);
            if (auth) {
                for (block = 0; block < auxBlock; block++) {
                    toRead = mf.readBlock(block + z);
                    byte[] limpio=new byte[toRead.length];
                    if (toRead != null) {

                        for (int i = 0; i < toRead.length; i++) {
                            if (toRead[i] != 0) {
                                limpio[i] += toRead[i];
                            } else {
                                limpio[i] += ' ';
                            }
                        }
                        String s= new String(limpio);
                        aux += s;
                        System.out.println("aqui: array: s: "+ s);
                    }
                }
            }
            mf.close();
        } catch (Exception e) {
            e.printStackTrace();
            return context.getString(R.string.error_conexion_tag);
        }
         //System.out.println("Mensaje tag:  " + aux);
        return  aux;
    }

    void clean(Tag tag){
        MifareClassic mfc = MifareClassic.get(tag);
        try {
            mfc.connect();
            int x = 0;
            int y = 0;
            int iw;
            int z = 1;
            int block;
            int auxBlock = 2;
            boolean auth = false;
            for (y=0; y < 16; y++) {
                auth = mfc.authenticateSectorWithKeyA(y, MifareClassic.KEY_DEFAULT);
                if (auth) {
                    byte[] toWrite = new byte[MifareClassic.BLOCK_SIZE];

                    for (block = 0; block < auxBlock; block++) {

                        for (iw = 0; iw < MifareClassic.BLOCK_SIZE; iw++) {
                            toWrite[iw] = 0;
                        }
                        mfc.writeBlock(z + block, toWrite);
                        toWrite = new byte[MifareClassic.BLOCK_SIZE];
                    }
                    if (z == 1) {
                        z = z + block + 1;
                        auxBlock = auxBlock + 1;
                    } else {
                        z = z + block + 1;
                    }
                }
            }
            Toast.makeText(context, context.getString(R.string.tag_configurado), Toast.LENGTH_LONG).show();
            mfc.close();
        } catch (Exception fe) {
            Toast.makeText(context, context.getString(R.string.error_tag_comunicacion), Toast.LENGTH_LONG).show();
            fe.printStackTrace();
        }
    }

    String idTag(Tag tag){
        byte[] toRead=null;
        byte[] send= new byte[4];
        String aux="";
        MifareClassic mf = MifareClassic.get(tag);
        try {
            mf.connect();
            boolean auth = false;

            auth = mf.authenticateSectorWithKeyA(0, MifareClassic.KEY_DEFAULT);
            if (auth) {
                toRead = mf.readBlock(0);
                System.out.println(toRead.toString());
                for(int i=0; i<4; i++) {
                    // System.out.println("id: "+ i + " num: " + toRead[i]);
                    send[i]=toRead[i];
                }
                aux=byteArrayToHexString(send);
            }
            mf.close();
        } catch (Exception e) {
            e.printStackTrace();
            return context.getString(R.string.error_conexion_tag);
        }
        //System.out.println("Mensaje tag:  " + aux);
        return  aux;
    }

    public static String byteArrayToHexString(byte[] byteArray){
        return String.format("%0" + (byteArray.length * 2) + "X", new BigInteger(1,byteArray));
    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }
    void changeKey(Tag tag, int sector, int bloque) {
        MifareClassic mf = MifareClassic.get(tag);
        try {
            mf.connect();
            boolean auth = false;
            auth = mf.authenticateSectorWithKeyA(sector, MifareClassic.KEY_DEFAULT);
            if (auth) {
                 mf.writeBlock(bloque + 3,ky);
             }
            mf.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
