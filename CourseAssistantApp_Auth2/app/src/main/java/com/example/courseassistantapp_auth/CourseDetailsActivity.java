package com.example.courseassistantapp_auth;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Iterator;

public class CourseDetailsActivity extends AppCompatActivity {

    private TextView courseNameTextView;
    private TextView courseCodeTextView;
    private TextView courseDateTextView;
    private TextView groupCountTextView;
    private LinearLayout assigneesContainer; // to show assignees


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_course_details);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("Course Details");

        }

        courseNameTextView = findViewById(R.id.tv_course_name);
        courseCodeTextView = findViewById(R.id.tv_course_code);
        courseDateTextView = findViewById(R.id.tv_course_date);
        groupCountTextView = findViewById(R.id.tv_course_group_number);
        assigneesContainer = findViewById(R.id.assignees_container); // Assignees için alan


        String courseCode = getIntent().getStringExtra("courseCode");
        if (courseCode == null) {
            Toast.makeText(this, "Error: No course code provided", Toast.LENGTH_LONG).show();
            return;
        }

        fetchCourseData(courseCode);
    }


    private void fetchCourseData(String courseCode) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Courses");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean found = false;
                int index = 0;

                Iterable<DataSnapshot> snapshots = dataSnapshot.getChildren();
                Iterator<DataSnapshot> iterator = snapshots.iterator();

                while (iterator.hasNext() && !found) {
                    DataSnapshot snapshot = iterator.next();
                    Course course = snapshot.getValue(Course.class);

                    if (course != null && course.getCourseCode().equals(courseCode)) {
                        courseNameTextView.setText(course.getCourseName());
                        courseCodeTextView.setText(course.getCourseCode());
                        courseDateTextView.setText(course.getStartDate());
                        groupCountTextView.setText(String.valueOf(course.getGroupCount()));

                        ArrayList<String> assignees = course.getAssignees();
                        assigneesContainer.removeAllViews();


                        if (assignees != null) {

                            TextView textViewLabel = new TextView(CourseDetailsActivity.this);
                            textViewLabel.setText("Instructors");
                            textViewLabel.setTextSize(19);
                            int labelColor = ContextCompat.getColor(CourseDetailsActivity.this, R.color.blue);
                            textViewLabel.setTextColor(labelColor);
                            textViewLabel.setPadding(0, 6, 0, 4);
                            assigneesContainer.addView(textViewLabel);
                            assigneesContainer.setBackgroundResource(R.drawable.border);
                            assigneesContainer.setPadding(16, 16, 16, 16);


                            int i = 1;
                            for (String email : assignees) {

                                TextView textView = new TextView(CourseDetailsActivity.this);
                                textView.setText("Instructor " + i + ": " + email);
                                i += 1;
                                textView.setTextSize(20);
                                int textColor = ContextCompat.getColor(CourseDetailsActivity.this, R.color.black);
                                textView.setTextColor(textColor);


                                assigneesContainer.addView(textView);
                            }
                        }

                        found = true; // Stop the loop if course is found
                    }

                    index++; // Increment index to avoid infinite loop
                }

                if (!found) {
                    Toast.makeText(CourseDetailsActivity.this, "Course not found", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(CourseDetailsActivity.this, "Error: " + databaseError.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }


    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.course_details_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();

        String courseCode = courseCodeTextView.getText().toString();
        if (id == R.id.details_edit) {
            Intent intent = new Intent(CourseDetailsActivity.this, EditCourseActivity.class);
            intent.putExtra("courseCode", courseCode);
            startActivity(intent);

        } else if (id == R.id.details_students) {

            Intent intent = new Intent(CourseDetailsActivity.this, SeeStudentsActivity.class);
            intent.putExtra("courseCode", courseCode);
            startActivity(intent);

        } else if (id == R.id.details_delete) {
            new AlertDialog.Builder(this)
                    .setTitle("Delete Course")
                    .setMessage("Are you sure you want to delete this course?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        // delete
                        deleteCourse(courseCode);
                    })
                    .setNegativeButton("No", (dialog, which) -> {
                        dialog.dismiss();
                    })
                    .show();


        } else {
            Toast.makeText(CourseDetailsActivity.this, "Something went wrong.", Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }

    private void deleteCourse(String courseCode) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Courses");
        reference.orderByChild("courseCode").equalTo(courseCode).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    snapshot.getRef().removeValue(); // Kursu sil
                }
                Toast.makeText(CourseDetailsActivity.this, "Course deleted successfully", Toast.LENGTH_LONG).show();
                finish();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(CourseDetailsActivity.this, "Error deleting course: " + error.getMessage(), Toast.LENGTH_LONG).show();

            }
        });
    }

}