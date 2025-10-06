package com.example.notes;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaScannerConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;

public class CreateNoteActivity extends AppCompatActivity {
    private static final int STORAGE_PERMISSION_CODE = 1001;
    private EditText editName, etContent;
    private String existingFileName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_note);

        initViews();
        loadNote(savedInstanceState);
        setupClickListeners();
    }

    private void initViews() {
        editName = findViewById(R.id.editName);
        etContent = findViewById(R.id.etContent);
    }

    private void loadNote(Bundle savedInstanceState) {
        existingFileName = getIntent().getStringExtra("filename"); // если note существует

        if (savedInstanceState != null) { // текущий note
            editName.setText(savedInstanceState.getString("current_note_name"));
            etContent.setText(savedInstanceState.getString("current_note_content"));
        } else if (existingFileName != null) { // если хотим изменить note
            editName.setText(existingFileName.replace(".txt", ""));
            etContent.setText(readFile(existingFileName));
        }
    }

    private void setupClickListeners() {
        findViewById(R.id.back_btn).setOnClickListener(v -> finish());
        findViewById(R.id.help_icon).setOnClickListener(v ->
                Toast.makeText(this, "Help icon clicked", Toast.LENGTH_SHORT).show());
        findViewById(R.id.save_icon).setOnClickListener(v -> saveNote());
        findViewById(R.id.export_icon).setOnClickListener(v -> exportNoteToExternalStorage());
    }

    private void saveNote() {
        String fileName = editName.getText().toString() + ".txt";
        String content = etContent.getText().toString();

        try (FileOutputStream fos = openFileOutput(fileName, MODE_PRIVATE)) { // Если файл уже существует, он перезаписывается
            fos.write(content.getBytes());
            Toast.makeText(this, "Your note has been saved ✅", Toast.LENGTH_SHORT).show();
            finish();
        } catch (IOException e) {
            Toast.makeText(this, "Error saving file", Toast.LENGTH_SHORT).show();
        }
    }

    private String readFile(String fileName) {
        try (InputStream is = openFileInput(fileName)) {
            byte[] buffer = new byte[is.available()];
            is.read(buffer);
            return new String(buffer);
        } catch (IOException e) {
            return "";
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("current_note_name", editName.getText().toString());
        outState.putString("current_note_content", etContent.getText().toString());
    }

    @SuppressLint("NewApi")
    private void exportNoteToExternalStorage() {
        if (hasStoragePermission()) {
            performExport();
        } else {
            requestStoragePermission();
        }
    }

    private boolean hasStoragePermission() {
        // Для Android 10 и ниже достаточно обычного разрешения
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            return ContextCompat.checkSelfPermission(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        }

        // Для Android 11+ нужно специальное разрешение
        return Environment.isExternalStorageManager();
    }

    @SuppressLint("NewApi")
    private void requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Для Android 11+ - специальный intent для управления всеми файлами
            try {
                Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                startActivity(intent);
            } catch (Exception e) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                startActivity(intent);
            }
        } else {
            // Для версий до Android 11 - стандартный запрос разрешения
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    STORAGE_PERMISSION_CODE);
        }
    }

//    private boolean checkStoragePermission() {
//        if (ContextCompat.checkSelfPermission(this,
//                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(this,
//                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
//                    STORAGE_PERMISSION_CODE);
//            return false;
//        }
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
//                !Environment.isExternalStorageManager()) {
//            Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
//            startActivity(intent);
//            Toast.makeText(this, "Please grant full storage access", Toast.LENGTH_LONG).show();
//            return false;
//        }
//
//        return true;
//    }

    private void performExport() {
        // Дополнительная проверка перед экспортом
        if (!hasStoragePermission()) {
            Toast.makeText(this, "Storage permission not granted", Toast.LENGTH_SHORT).show();
            return;
        }

        String fileName = editName.getText().toString() + ".txt";
        String content = etContent.getText().toString();

        try {
            File dir = getExportDirectory();
            if (dir == null) return;

            File file = new File(dir, fileName);
            writeToFile(file, content);
            scanFile(file);
            showExportSuccess(file);
        } catch (IOException e) {
            showExportError(e);
        }
    }

    private File getExportDirectory() {
        File dir;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            dir = new File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "NotesApp");
        } else {
            dir = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOCUMENTS), "NotesApp");
        }

        if (!dir.exists() && !dir.mkdirs()) {
            Toast.makeText(this, "Cannot create directory", Toast.LENGTH_SHORT).show();
            return null;
        }
        return dir;
    }

    private void writeToFile(File file, String content) throws IOException {
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(content);
        }
    }

    private void scanFile(File file) {
        MediaScannerConnection.scanFile(this,
                new String[]{file.getAbsolutePath()},
                null,
                null);
    }

    private void showExportSuccess(File file) {
        Toast.makeText(this, "Exported to: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
    }

    private void showExportError(Exception e) {
        Toast.makeText(this, "Export failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                performExport(); // выполнение экспорта
            } else {
                Toast.makeText(this,
                        "Permission is required to export notes",
                        Toast.LENGTH_LONG).show();
            }
        }
    }
}