package com.example.courseassistantapp_auth;

import static com.example.courseassistantapp_auth.R.*;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Space;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SeeStudentsActivity extends AppCompatActivity {

    private DatabaseReference coursesReference;
    private FirebaseAuth auth;
    private LinearLayout studentInfoLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_see_students);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("Courses");
            actionBar.hide();
        }


        studentInfoLayout = findViewById(R.id.student_info_layout);  // LinearLayout in the XML

        String courseCode = getIntent().getStringExtra("courseCode");
        if (courseCode == null) {
            Toast.makeText(this, "Error: No course code provided", Toast.LENGTH_LONG).show();
            return;
        }

        coursesReference = FirebaseDatabase.getInstance().getReference("Courses");

        auth = FirebaseAuth.getInstance();
        String userEmail = auth.getCurrentUser().getEmail();

        fetchGroupInfo(coursesReference, courseCode, userEmail);  // Load the group information
    }

    private void fetchGroupInfo(DatabaseReference coursesReference, String courseCode, String userEmail) {
        coursesReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String courseKey = null;

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) { // Find the matching course
                    Course course = snapshot.getValue(Course.class);
                    if (course != null && course.getCourseCode().equals(courseCode)) {
                        courseKey = snapshot.getKey(); // Get the unique key
                        break;
                    }
                }

                if (courseKey == null) { // If no match is found
                    Toast.makeText(SeeStudentsActivity.this, "Course not found with courseCode " + courseCode, Toast.LENGTH_SHORT).show();
                    return;
                }

                DatabaseReference courseReference = coursesReference.child(courseKey);

                courseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @SuppressLint("ResourceAsColor")
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Course course = dataSnapshot.getValue(Course.class);

                        if (course != null) {
                            int assigneeIndex = course.getAssignees() != null
                                    ? course.getAssignees().indexOf(userEmail)
                                    : -1;

                            if (assigneeIndex == -1) {
                                Toast.makeText(SeeStudentsActivity.this, "Assignee not found in this course", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            String groupKey = "Group_" + (assigneeIndex + 1);
                            DataSnapshot groupSnapshot = dataSnapshot.child("groups").child(groupKey);

                            if (!groupSnapshot.exists()) {
                                Toast.makeText(SeeStudentsActivity.this, "Group not found", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            // Loop through the students and create a layout with a delete button
                            for (DataSnapshot studentSnapshot : groupSnapshot.getChildren()) {
                                String studentID = studentSnapshot.getKey();
                                String studentEmail = studentSnapshot.getValue(String.class);

                                // Öğrenci bilgilerini ve düğmeyi tutmak için `LinearLayout`
                                LinearLayout studentItemLayout = new LinearLayout(SeeStudentsActivity.this);
                                studentItemLayout.setOrientation(LinearLayout.HORIZONTAL);

                                TextView studentTextView = new TextView(SeeStudentsActivity.this);
                                studentTextView.setText(studentID + ": " + studentEmail);
                                studentTextView.setTextSize(16);
                                studentTextView.setTextColor(R.color.black);
                                //studentTextView.setBackgroundResource(R.drawable.border);
                                studentTextView.setPadding(16,16,16,16);

                                studentItemLayout.addView(studentTextView);

                                Space space = new Space(SeeStudentsActivity.this);
                                LinearLayout.LayoutParams spaceParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);  // Boşluğu itmek için
                                space.setLayoutParams(spaceParams);

                                studentItemLayout.addView(space);

                                // Öğrenciyi silmek için `Button`
                                ImageButton deleteButton = new ImageButton(SeeStudentsActivity.this);
                                deleteButton.setImageResource(R.drawable.baseline_delete_24);  // Drawable kaynağını belirtin


                                LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.WRAP_CONTENT,
                                        LinearLayout.LayoutParams.WRAP_CONTENT
                                );
                                deleteButton.setLayoutParams(buttonParams);
                                deleteButton.setBackgroundResource(android.R.color.transparent);

                                deleteButton.setOnClickListener(v -> {
                                    courseReference.child("groups").child(groupKey).child(studentID).removeValue().addOnCompleteListener(task -> {
                                        if (task.isSuccessful()) {
                                            studentInfoLayout.removeView(studentItemLayout);
                                            Toast.makeText(SeeStudentsActivity.this, "Student removed successfully", Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(SeeStudentsActivity.this, "Failed to remove student", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                });

                                studentItemLayout.addView(deleteButton);
                                studentInfoLayout.addView(studentItemLayout);
                            }

                        } else {
                            Toast.makeText(SeeStudentsActivity.this, "Course not found", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Toast.makeText(SeeStudentsActivity.this, "Error fetching course data: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(SeeStudentsActivity.this, "Error fetching course data: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}