package com.user.notepad;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.squareup.picasso.Picasso;

public class AllNotesActivity extends AppCompatActivity {
    private RecyclerView postList;
    Toolbar toolbar;
    private DatabaseReference PostsRef;
    private FirebaseRecyclerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_notes);

        toolbar = (Toolbar) findViewById(R.id.appBarAllNotes);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("All Notes");


        PostsRef = FirebaseDatabase.getInstance().getReference().child("Posts");


        postList = (RecyclerView) findViewById(R.id.all_users_post_list);
        postList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        postList.setLayoutManager(linearLayoutManager);

        DisplayAllUsersPosts();


    }


    private void DisplayAllUsersPosts() {

        WifiManager manager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = manager.getConnectionInfo();
        String address = info.getMacAddress();

        Query query = FirebaseDatabase.getInstance().getReference().child("Posts").child(address).orderByChild("date");
        FirebaseRecyclerOptions<Posts> options = new FirebaseRecyclerOptions.Builder<Posts>().setQuery(query, Posts.class).build();
        adapter = new FirebaseRecyclerAdapter<Posts, PostsViewHolder>(options) {

            @NonNull
            @Override
            public PostsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_posts_layout, parent, false);
                return new PostsViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull final PostsViewHolder holder, int position, @NonNull final Posts model) {
                String type = model.getType().toString();
                final String PostKey = getRef(position).getKey();


                holder.postDateAndTime.setText("Date: " + model.getDate().toString() + "    Time: " + model.getTime().toString());

                if (model.getImage() == null)
                {
                    holder.postImage.setVisibility(View.GONE);
                    holder.postDescription.setText(model.getMessage().toString());
                }
                if (model.getImage() != null && model.getMessage() != null)
                {
                    holder.postDescription.setText(model.getMessage().toString());
                    Picasso.get().load(model.getImage().toString()).into(holder.postImage);
                }
                if (model.getMessage() == null)
                {
                    holder.postDescription.setVisibility(View.GONE);
                    Picasso.get().load(model.getImage().toString()).into(holder.postImage);
                }

                holder.postImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v)
                    {
                        Intent intent = new Intent(AllNotesActivity.this, ViewPostImageActivity.class);
                        intent.putExtra("url", model.getImage().toString());
                        startActivity(intent);
                    }
                });
                holder.delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        FirebaseDatabase.getInstance().getReference().child("Posts")
                                .child(address).child(PostKey)
                                .removeValue()
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful())
                                        {
                                            startActivity(new Intent(AllNotesActivity.this, AllNotesActivity.class));
                                            finish();
                                        }
                                        else
                                        {
                                            Toast.makeText(AllNotesActivity.this, task.getException().getMessage().toString(), Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                    }
                });
                holder.edit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(AllNotesActivity.this, EditNoteActivity.class);
                        if (model.getImage() == null)
                        {
                            i.putExtra("pid", PostKey);
                            i.putExtra("pmessage", model.getMessage().toString());
                        }
                        else if (model.getMessage() == null)
                        {
                            i.putExtra("pid", PostKey);
                            i.putExtra("pimage", model.getImage().toString());
                        }
                        else
                        {
                            i.putExtra("pid", PostKey);
                            i.putExtra("pmessage", model.getMessage().toString());
                            i.putExtra("pimage", model.getImage().toString());
                        }
                        startActivity(i);
                    }
                });
            }
        };
        postList.setAdapter(adapter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
    }


    public static class PostsViewHolder extends RecyclerView.ViewHolder {
        View mView;
        TextView postDateAndTime,postDescription;
        ImageView postImage;
        ImageView delete, edit;

        public PostsViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
            postDateAndTime = mView.findViewById(R.id.postDateAndTime);
            postDescription = mView.findViewById(R.id.postDescription);
            postImage = mView.findViewById(R.id.postImage);
            delete = mView.findViewById(R.id.delete_post);
            edit = mView.findViewById(R.id.edit_post);
        }

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item)
    {
        int id = item.getItemId();
        if (id == android.R.id.home)
        {
            startActivity(new Intent(AllNotesActivity.this, MainActivity.class));
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}