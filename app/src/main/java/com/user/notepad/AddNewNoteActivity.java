package com.user.notepad;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AddNewNoteActivity extends AppCompatActivity {
    EditText msgBox;
    ImageView postImage, camera;
    private Uri image;
    private String downloadUrl;
    Button save;
    private Toolbar toolbar;
    private Bitmap photo;
    BottomNavigationView bottomNavigationView;

    private long countPost = 0;
    private String saveCurrentDate, saveCurrentTime;
    private ProgressDialog loadingBar;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    private Bitmap mImageBitmap;
    private String mCurrentPhotoPath;
    private static final int CAMERA_REQUEST = 1888;
    private static final int MY_CAMERA_PERMISSION_CODE = 100;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_note);

        loadingBar = new ProgressDialog(this);
        loadingBar.setTitle("Adding Notes!");
        loadingBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        loadingBar.setMessage("Please Wait ! Until Your Note Will Be Saved!");
        loadingBar.setCanceledOnTouchOutside(true);


        toolbar = (Toolbar) findViewById(R.id.app_bar_add_new_note);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Add New Note");

        postImage = findViewById(R.id.imageAddPost);
        msgBox = findViewById(R.id.messageAddPost);
        save = findViewById(R.id.addPostBtn);
        camera = findViewById(R.id.camera);

        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(AddNewNoteActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
                {
                    ActivityCompat.requestPermissions(AddNewNoteActivity.this, new String[]
                            {
                                    Manifest.permission.CAMERA
                            }
                            , 100
                    );
                }
                else
                {
                    Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(cameraIntent, 100);
                }
            }
        });


        findViewById(R.id.delete_add_img)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        image = null;
                        postImage.setImageDrawable(null);
                    }
                });

        Calendar callForDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MM-yyyy");
        saveCurrentDate = currentDate.format(callForDate.getTime());

        Calendar callForTime = Calendar.getInstance();
        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm:ss");
        saveCurrentTime = currentTime.format(callForTime.getTime());



        postImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(galleryIntent, 101);
            }
        });


        FirebaseDatabase.getInstance().getReference().child("Posts").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists())
                {
                    countPost = snapshot.getChildrenCount();
                }
                else
                {
                    countPost = 0;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        WifiManager manager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = manager.getConnectionInfo();
        String address = info.getMacAddress();

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadingBar.show();
                if (image != null)
                {
                    final StorageReference imageRef = FirebaseStorage.getInstance().getReference().child("Posts Images").child(UUID.randomUUID().toString());

                    UploadTask uploadTask = imageRef.putFile(image);
                    Task<Uri> uriTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                        @Override
                        public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                            if (!task.isSuccessful())
                            {
                                throw task.getException();
                            }
                            return imageRef.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                @Override
                                public void onComplete(@NonNull Task<Uri> task) {
                                    if (task.isSuccessful())
                                    {
                                        downloadUrl = task.getResult().toString();
                                        String message = msgBox.getText().toString();
                                        if (!message.isEmpty())
                                        {
                                            Map map = new HashMap();
                                            map.put("message", msgBox.getText().toString());
                                            map.put("image", downloadUrl);
                                            map.put("type", "textimage");
                                            map.put("uid", address);
                                            map.put("counter", countPost);
                                            map.put("date", saveCurrentDate);
                                            map.put("time", saveCurrentTime);
                                            FirebaseDatabase.getInstance().getReference().child("Posts").child(address).push().setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        loadingBar.dismiss();
                                                        startActivity(new Intent(AddNewNoteActivity.this, AllNotesActivity.class));
                                                        finish();
                                                    } else {
                                                        loadingBar.dismiss();
                                                        Toast.makeText(AddNewNoteActivity.this, task.getException().toString(), Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });
                                        }
                                        else
                                        {
                                            if (downloadUrl.isEmpty())
                                            {
                                                loadingBar.dismiss();
                                                Toast.makeText(AddNewNoteActivity.this, "Select an Image First!", Toast.LENGTH_SHORT).show();
                                                return;
                                            }
                                            else {
                                                Map map = new HashMap();
                                                map.put("image", downloadUrl);
                                                map.put("type", "image");
                                                map.put("uid", address);
                                                map.put("counter", countPost);
                                                map.put("date", saveCurrentDate);
                                                map.put("time", saveCurrentTime);
                                                FirebaseDatabase.getInstance().getReference().child("Posts").child(address).push().setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            loadingBar.dismiss();
                                                            startActivity(new Intent(AddNewNoteActivity.this, AllNotesActivity.class));
                                                            finish();
                                                        } else {
                                                            loadingBar.dismiss();
                                                            Toast.makeText(AddNewNoteActivity.this, task.getException().toString(), Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                });
                                            }
                                        }
                                    }
                                    else
                                    {
                                        loadingBar.dismiss();
                                        Toast.makeText(AddNewNoteActivity.this, task.getException().toString(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                    }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            if (task.isSuccessful())
                            {
                                Uri downloadUri = task.getResult();
                            }
                            else
                            {
                                loadingBar.dismiss();
                                Toast.makeText(AddNewNoteActivity.this, task.getException().toString(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
                else
                {
                    String msg = msgBox.getText().toString();
                    if (msg.isEmpty()) {
                        loadingBar.dismiss();
                        msgBox.setError("Required");
                        return;
                    }
                    else {
                        Map map = new HashMap();
                        map.put("message", msg);
                        map.put("type", "text");
                        map.put("uid", address);
                        map.put("counter", countPost);
                        map.put("date", saveCurrentDate);
                        map.put("time", saveCurrentTime);
                        FirebaseDatabase.getInstance().getReference().child("Posts").child(address).push().setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    loadingBar.dismiss();
                                    startActivity(new Intent(AddNewNoteActivity.this, AllNotesActivity.class));
                                    finish();
                                } else {
                                    loadingBar.dismiss();
                                    Toast.makeText(AddNewNoteActivity.this, task.getException().toString(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                }
            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_CAMERA_PERMISSION_CODE)
        {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                Toast.makeText(this, "camera permission granted", Toast.LENGTH_LONG).show();
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, 100);
            }
            else
            {
                Toast.makeText(this, "camera permission denied", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 101)
        {
            if (resultCode == RESULT_OK)
            {
                image = data.getData();
                postImage.setImageURI(image);
            }
        }
        if (requestCode == 100 && resultCode == RESULT_OK)
        {
            photo = (Bitmap) data.getExtras().get("data");
            postImage.setImageBitmap(photo);
            image = getImageUri(this,photo);
        }
    }

    private Uri getImageUri(Context context, Bitmap photo)
    {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        photo.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), photo, "Title", null);
        return Uri.parse(path);
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item)
    {
        int id = item.getItemId();
        if (id == android.R.id.home)
        {
            startActivity(new Intent(AddNewNoteActivity.this, MainActivity.class));
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

}