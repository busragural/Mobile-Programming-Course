package com.example.courseassistantapp_auth;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {

    private List<Post> posts;

    public PostAdapter(List<Post> posts) {
        this.posts = posts;
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.post_item, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        Post post = posts.get(position);
        holder.bind(post);
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    class PostViewHolder extends RecyclerView.ViewHolder {
        private TextView authorTextView;
        private TextView contentTextView;
        private TextView timestampTextView;
        private RecyclerView commentListRecyclerView;
        private EditText commentContentInput;
        private Button commentSendButton;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            authorTextView = itemView.findViewById(R.id.post_author);
            contentTextView = itemView.findViewById(R.id.post_content);
            timestampTextView = itemView.findViewById(R.id.post_timestamp);


            commentListRecyclerView = itemView.findViewById(R.id.comment_list);
            commentContentInput = itemView.findViewById(R.id.comment_content_input);
            commentSendButton = itemView.findViewById(R.id.comment_send_button);
        }

        public void bind(Post post) {
            authorTextView.setText("By: " + post.authorEmail);
            contentTextView.setText(post.content);
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
            String dateFormatted = sdf.format(new Date(post.timestamp));
            timestampTextView.setText(dateFormatted);


            DatabaseReference commentsReference = FirebaseDatabase.getInstance()
                    .getReference("Posts")
                    .child(post.postId)
                    .child("comments");

            List<Comment> comments = new ArrayList<>();
            CommentAdapter commentAdapter = new CommentAdapter(comments);

            commentListRecyclerView.setAdapter(commentAdapter);
            commentListRecyclerView.setLayoutManager(new LinearLayoutManager(itemView.getContext()));

            commentsReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    comments.clear();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Comment comment = snapshot.getValue(Comment.class);
                        comments.add(comment);
                    }
                    commentAdapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Toast.makeText(itemView.getContext(), "Error loading comments: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

            commentSendButton.setOnClickListener(v -> {
                String content = commentContentInput.getText().toString().trim();

                if (content.isEmpty()) {
                    Toast.makeText(itemView.getContext(), "Comment cannot be empty", Toast.LENGTH_SHORT).show();
                    return;
                }

                String authorEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
                long timestamp = System.currentTimeMillis();

                Comment newComment = new Comment(authorEmail, content, timestamp);
                String commentId = commentsReference.push().getKey();

                commentsReference.child(commentId).setValue(newComment).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        commentContentInput.setText("");
                        Toast.makeText(itemView.getContext(), "Comment sent successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(itemView.getContext(), "Failed to send comment", Toast.LENGTH_SHORT).show();
                    }
                });
            });
        }
    }
}