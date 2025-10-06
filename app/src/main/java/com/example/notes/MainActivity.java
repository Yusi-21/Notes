package com.example.notes;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements
        NotesAdapter.OnNoteClickListener, NotesAdapter.OnNoteLongClickListener {

//    public DrawerLayout drawer;
//    public ActionBarDrawerToggle toggle;
    private static final int STORAGE_PERMISSION_CODE = 1001;
    private NotesAdapter adapter;
    private List<Note> notes = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initRecyclerView();
        setupClickListeners();
        loadNotes();
        checkStoragePermission();
        requestStoragePermission();

//        drawer = findViewById(R.id.drawer_layout);
//        toggle = new ActionBarDrawerToggle(MainActivity.this, drawer, R.string.drawer_open,
//                R.string.drawer_close);
//        drawer.addDrawerListener(toggle);
//
//        toggle.syncState();
//        // to make the Navigation drawer icon always appear on the action bar
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.recycler_notes);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NotesAdapter(notes, this, this);
        recyclerView.setAdapter(adapter);
    }

    private void setupClickListeners() {
        FloatingActionButton fab = findViewById(R.id.create_icon);
        fab.setOnClickListener(v -> startActivity(new Intent(this, CreateNoteActivity.class)));

        findViewById(R.id.home_icon).setOnClickListener(v ->
                Toast.makeText(this, "You are already in Home page", Toast.LENGTH_SHORT).show());

        findViewById(R.id.setting_icon).setOnClickListener(v ->
                Toast.makeText(this, "Settings clicked", Toast.LENGTH_SHORT).show());
    }

    private void loadNotes() {
        notes.clear();
        File[] files = getFilesDir().listFiles();
        if (files != null) {
            for (File file : files) {
                notes.add(new Note(file.getName(), readFile(file.getName())));
            }
        }
        adapter.notifyDataSetChanged(); // уведомлять о изменении данных
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
    public void onNoteClick(Note note) { // открывем заметку которую нам нужны (note1/note2/note3...)
        AppStateManager.saveState(this, note.getTitle());
        Intent intent = new Intent(this, CreateNoteActivity.class);
        intent.putExtra("filename", note.getTitle());
        startActivity(intent);
    }

    @Override
    public void onNoteLongClick(Note note) {
        showDeleteConfirmationDialog(note);
    }

    private void showDeleteConfirmationDialog(Note note) {
        new AlertDialog.Builder(this)
                .setTitle("❗Please confirm, if you want \n     to delete this note!")
                .setMessage("Delete " + note.getTitle() + "?")
                .setPositiveButton("🗑 Delete", (d, w) -> deleteNote(note))
                // d-DialogInterface который передаётся в метод onClick , w = ID кнопки
                .setNegativeButton("❌ Cancel", null)
                .show();
    }

    private void deleteNote(Note note) {
        if (deleteFile(note.getTitle())) {
            loadNotes();
            Toast.makeText(this, note.getTitle() + " successfully removed", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        ArrayList<String> noteNames = new ArrayList<>();
        for (Note note : notes) {
            noteNames.add(note.getTitle());
        }
        outState.putStringArrayList("notes_list", noteNames);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        ArrayList<String> noteNames = savedInstanceState.getStringArrayList("notes_list");
        if (noteNames != null) {
            notes.clear();
            for (String name : noteNames) {
                notes.add(new Note(name, readFile(name)));
            }
            adapter.notifyDataSetChanged();
        }
    }

    private void checkStoragePermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    STORAGE_PERMISSION_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void requestStoragePermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            // Показываем объяснение перед запросом, если нужно
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                showPermissionExplanation();
            } else {
                // Непосредственный запрос разрешения
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        STORAGE_PERMISSION_CODE);
            }
        }
    }

    private void showPermissionExplanation() {
        new AlertDialog.Builder(this)
                .setTitle("Storage Permission Needed")
                .setMessage("This permission is required to export notes to your device storage")
                .setPositiveButton("OK", (dialog, which) -> {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            STORAGE_PERMISSION_CODE);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

//    // Обработка нажатия на иконку меню в ActionBar для открытия и закрытия Drawer
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        if (toggle.onOptionsItemSelected(item)) {
//            return true;
//        }
//        return super.onOptionsItemSelected(item);
//    }

}