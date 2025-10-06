package com.example.notes;

import android.content.Context;

import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileOutputStream;

public class AppStateManager {
    private static final String STATE_FILE = "app_state.json";

    public static void saveState(Context context, String lastOpenedNote) {
        JSONObject state = new JSONObject();
        try {
            state.put("last_opened_note", lastOpenedNote);
            FileOutputStream fos = context.openFileOutput(STATE_FILE, Context.MODE_PRIVATE);
            fos.write(state.toString().getBytes());
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String loadLastOpenedNote(Context context) {
        try {
            FileInputStream fis = context.openFileInput(STATE_FILE);
            byte[] buffer = new byte[fis.available()];
            fis.read(buffer);
            fis.close();
            JSONObject state = new JSONObject(new String(buffer));
            return state.getString("last_opened_note");
        } catch (Exception e) {
            return null;
        }
    }
}