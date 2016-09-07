package com.nxt.testwallet.file;

import android.content.Context;

import com.nxt.testwallet.R;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

/**
 *  on 4/29/2015.
 */
public class FileReader {
    public String loadAssets(Context context){
        InputStream is = context.getResources().openRawResource(R.raw.assets);
        Writer writer = new StringWriter();
        char[] buffer = new char[1024];
        try {
            Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            int n;
            while ((n = reader.read(buffer)) != -1) {
                writer.write(buffer, 0, n);
            }

            is.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }



        return writer.toString();
    }
}
