package com.user.notepad;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EditNoteActivity extends AppCompatActivity {

    EditText msgBox;
    ImageView postImage;
    private Uri image;
    private String downloadUrl;
    Button save;
    private Toolbar toolbar;
    private long countPost = 0;
    private String saveCurrentDate, saveCurrentTime;
    private ProgressDialog loadingBar;
    private String pmessage, pimage, pid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_note);

        msgBox = findViewById(R.id.messageEditPost);
        postImage = findViewById(R.id.imageEditPost);
        save  = findViewById(R.id.editPostBtn);



        loadingBar = new ProgressDialog(this);
        loadingBar.setTitle("Updating Notes!");
        loadingBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        loadingBar.setMessage("Please Wait ! Until Your Note Will Be Updated!");
        loadingBar.setCanceledOnTouchOutside(true);



        if (getIntent().getStringExtra("pmessage") != null)
        {
            pmessage = getIntent().getStringExtra("pmessage");
            msgBox.setText(pmessage);
        }
        if (getIntent().getStringExtra("pimage") != null)
        {
            pimage = getIntent().getStringExtra("pimage");
            Picasso.get().load(pimage).into(postImage);
        }
        pid = getIntent().getStringExtra("pid");

        toolbar = (Toolbar) findViewById(R.id.app_bar_edit_note);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Edit Note");

        Calendar callForDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MM-yyyy");
        saveCurrentDate = currentDate.format(callForDate.getTime());

        Calendar callForTime = Calendar.getInstance();
        SimpleDateFormat currentTime = new SimpleDateFormat("HH:MM:SS");
        saveCurrentTime = currentTime.format(callForTime.getTime());


        findViewById(R.id.delete_edit_img)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        image = null;
                        pimage = null;
                        postImage.setImageDrawable(null);
                    }
                });


        postImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(galleryIntent, 101);
            }
        });

        WifiManager manager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = manager.getConnectionInfo();
        String address = info.getMacAddress();

        FirebaseDatabase.getInstance().getReference().child("Posts").child(address).addValueEventListener(new ValueEventListener() {
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
                                            if (downloadUrl.isEmpty())
                                            {
                                                loadingBar.dismiss();
                                                Toast.makeText(EditNoteActivity.this, "Select an Image First!", Toast.LENGTH_SHORT).show();
                                                return;
                                            }
                                            else {
                                                Map map = new HashMap();
                                                map.put("message", msgBox.getText().toString());
                                                map.put("image", downloadUrl);
                                                map.put("type", "textimage");
                                                map.put("uid", address);
                                                map.put("counter", countPost);
                                                map.put("date", saveCurrentDate);
                                                map.put("time", saveCurrentTime);
                                                FirebaseDatabase.getInstance().getReference().child("Posts").child(address).child(pid).setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            loadingBar.dismiss();
                                                            startActivity(new Intent(EditNoteActivity.this, AllNotesActivity.class));
                                                            finish();
                                                        } else {
                                                            loadingBar.dismiss();
                                                            Toast.makeText(EditNoteActivity.this, task.getException().toString(), Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                });
                                            }
                                        }
                                        else
                                        {
                                            if (downloadUrl.isEmpty())
                                            {
                                                loadingBar.dismiss();
                                                Toast.makeText(EditNoteActivity.this, "Select an Image First!", Toast.LENGTH_SHORT).show();
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
                                                FirebaseDatabase.getInstance().getReference().child("Posts").child(address).child(pid).setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            loadingBar.dismiss();
                                                            startActivity(new Intent(EditNoteActivity.this, AllNotesActivity.class));
                                                            finish();
                                                        } else {
                                                            loadingBar.dismiss();
                                                            Toast.makeText(EditNoteActivity.this, task.getException().toString(), Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                });
                                            }
                                        }
                                    }
                                    else
                                    {
                                        loadingBar.dismiss();
                                        Toast.makeText(EditNoteActivity.this, task.getException().toString(), Toast.LENGTH_SHORT).show();
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
                                Toast.makeText(EditNoteActivity.this, task.getException().toString(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
                else
                {
                    String msg = msgBox.getText().toString();
                    if (msg.isEmpty() && pmessage.isEmpty()) {
                        loadingBar.dismiss();
                        msgBox.setError("Required");
                        return;
                    }
                    else {
                        Map map = new HashMap();
                        if (pimage == null)
                        {
                            map.put("message", msg);
                            map.put("type", "text");
                            map.put("uid", address);
                            map.put("counter", countPost);
                            map.put("date", saveCurrentDate);
                            map.put("time", saveCurrentTime);
                        }
                        else if (msg == null)
                        {
                            map.put("image", pimage);
                            map.put("type", "image");
                            map.put("uid", address);
                            map.put("counter", countPost);
                            map.put("date", saveCurrentDate);
                            map.put("time", saveCurrentTime);
                        }
                        else
                        {
                            map.put("message", msgBox.getText().toString());
                            map.put("image", pimage);
                            map.put("type", "textimage");
                            map.put("uid", address);
                            map.put("counter", countPost);
                            map.put("date", saveCurrentDate);
                            map.put("time", saveCurrentTime);
                        }
                        FirebaseDatabase.getInstance().getReference().child("Posts").child(address).child(pid).setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    loadingBar.dismiss();
                                    startActivity(new Intent(EditNoteActivity.this, AllNotesActivity.class));
                                    finish();
                                } else {
                                    loadingBar.dismiss();
                                    Toast.makeText(EditNoteActivity.this, task.getException().toString(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                }
            }
        });

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 101)
        {
            if (resultCode == RESULT_OK)
            {
                image = data.getData();
                postImage.setImageURI(image);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item)
    {
        int id = item.getItemId();
        if (id == android.R.id.home)
        {
            startActivity(new Intent(EditNoteActivity.this, AllNotesActivity.class));
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

}