package com.example.courseassistantapp_auth;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class PostsActivity extends AppCompatActivity {
    private DatabaseReference postsReference;
    private FirebaseAuth auth;
    private RecyclerView postListRecyclerView;
    private EditText postContentInput;
    private Button postSendButton;
    private List<Post> posts = new ArrayList<>();
    private PostAdapter postAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_posts);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("Posts");
            actionBar.hide();
        }

        postsReference = FirebaseDatabase.getInstance().getReference("Posts");
        auth = FirebaseAuth.getInstance();

        postContentInput = findViewById(R.id.post_content_input);
        postSendButton = findViewById(R.id.post_send_button);
        postListRecyclerView = findViewById(R.id.post_list);

        postAdapter = new PostAdapter(posts);
        postListRecyclerView.setAdapter(postAdapter);
        postListRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        postsReference.orderByChild("timestamp").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                posts.clear(); // Mevcut listeyi temizle
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    // `postId` dahil edilerek yeni Post oluşturulmalı
                    String postId = snapshot.getKey();
                    Post post = snapshot.getValue(Post.class);

                    // `postId` eksikse, doğru şekilde atayın
                    if (post != null) {
                        post.postId = postId;
                    }

                    posts.add(0, post); // Azalan sırada ekle
                }
                postAdapter.notifyDataSetChanged(); // RecyclerView'i güncelle
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(PostsActivity.this, "Error loading posts: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        postSendButton.setOnClickListener(v -> {
            String content = postContentInput.getText().toString().trim();

            if (content.isEmpty()) {
                Toast.makeText(this, "Post content cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            String authorEmail = auth.getCurrentUser().getEmail();
            long timestamp = System.currentTimeMillis();

            // Benzersiz gönderi kimliğini oluştur
            String postId = postsReference.push().getKey();

            // PostId ile yeni Post oluştur
            Post newPost = new Post(postId, authorEmail, content, timestamp);

            // Firebase'e gönderiyi kaydet
            postsReference.child(postId).setValue(newPost).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    postContentInput.setText(""); // Başarılı olunca giriş alanını temizle
                    Toast.makeText(this, "Post sent successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Failed to send post", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}