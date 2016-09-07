package com.nxt.nxtvault;

import android.content.Context;

import com.google.gson.Gson;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;


/**
 *  on 11/22/2014.
 */

public class StateManager {
    Gson gson = new Gson();

    private Context context;

    Object syncLock = new Object();

    public StateManager(Context context){
        this.context = context;
    }

    public <T> T loadState(String filename, Class<T> clazz){
        synchronized (syncLock) {
            T result = null;

            try {
                FileInputStream fin = context.openFileInput(filename);

                StringBuffer fileContent = new StringBuffer("");

                byte[] buffer = new byte[1024];
                int n;
                while ((n = fin.read(buffer)) != -1) {
                    fileContent.append(new String(buffer, 0, n));
                }

                result = gson.fromJson(fileContent.toString(), clazz);
            } catch (IOException e) {
                e.printStackTrace();
            }

            return result;
        }
    }

    public <T> void saveState(String filename, T obj){
        synchronized (syncLock) {
            try {
                FileOutputStream fos = context.openFileOutput(filename, Context.MODE_PRIVATE);

                String json = gson.toJson(obj);

                fos.write(json.getBytes());

                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
