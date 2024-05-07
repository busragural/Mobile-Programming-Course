package com.example.courseassistantapp_auth;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class ForumActivity extends AppCompatActivity {
    private Spinner topicSpinner;
    private Spinner courseCodeSpinner;
    private EditText messageEditText, courseCodeEditText, recipentEditText;
    private Button submitButton;
    private FirebaseDatabase database;
    private DatabaseReference forumReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_forum);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        topicSpinner = findViewById(R.id.forum_topic_spinner);
        courseCodeEditText = findViewById(R.id.forum_course_code_edit_text);
        messageEditText = findViewById(R.id.forum_message_edit_text);
        recipentEditText = findViewById(R.id.forum_reciever);
        submitButton = findViewById(R.id.forum_submit_button);

        // Spinner için verileri ayarlama
        ArrayAdapter<CharSequence> topicAdapter = ArrayAdapter.createFromResource(this,
                R.array.forum_topics, android.R.layout.simple_spinner_item);
        topicAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        topicSpinner.setAdapter(topicAdapter);


        // Firebase bağlantısı ve veri referansı
        database = FirebaseDatabase.getInstance();
        forumReference = database.getReference("Forum");

        submitButton.setOnClickListener(v -> {
            submitMessage(); // Butona basıldığında mesajı gönder
        });
    }

    private void submitMessage() {
        String selectedTopic = topicSpinner.getSelectedItem().toString();
        String courseCode = courseCodeEditText.getText().toString(); // Değişiklik
        String reciever = recipentEditText.getText().toString();
        String messageText = messageEditText.getText().toString();

        if (messageText.trim().isEmpty()) {
            Toast.makeText(this, "Please enter a message.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (courseCode.trim().isEmpty()) {
            Toast.makeText(this, "Please enter a course code.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (reciever.trim().isEmpty()) {
            Toast.makeText(this, "Please enter a recipient.", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseAuth auth = FirebaseAuth.getInstance();
        String userEmail = auth.getCurrentUser().getEmail();

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String currentTime = dateFormat.format(new Date());

        DatabaseReference newMessageReference = forumReference.push();
        newMessageReference.setValue(new ForumMessage(selectedTopic, courseCode, userEmail, reciever, messageText, currentTime))
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Message submitted.", Toast.LENGTH_SHORT).show();

                    Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:" + reciever));
                    emailIntent.putExtra(Intent.EXTRA_SUBJECT, selectedTopic);
                    emailIntent.putExtra(Intent.EXTRA_TEXT, messageText);

                    if (emailIntent.resolveActivity(getPackageManager()) != null) {
                        startActivity(emailIntent); // E-posta uygulamasını aç
                    } else {
                        Toast.makeText(this, "No email application found.", Toast.LENGTH_SHORT).show();
                    }
                messageEditText.setText("");
                    recipentEditText.setText("");

                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error submitting message.", Toast.LENGTH_SHORT).show();
                });
    }



}