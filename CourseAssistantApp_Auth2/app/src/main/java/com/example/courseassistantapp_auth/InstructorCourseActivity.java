package com.example.courseassistantapp_auth;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class InstructorCourseActivity extends AppCompatActivity {

    TextView courseCode, courseName, courseState;

    private FirebaseAuth auth;
    private LinearLayout coursesContainer;
    private FirebaseDatabase database;
    private DatabaseReference coursesReference;
    private SimpleDateFormat dateFormat;

    private Spinner stateSpinner;
    private EditText startDateEditText, endDateEditText;
    private Button applyFilterButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_instructor_course);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("Courses");
        }


        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        coursesReference = database.getReference("Courses");

        dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()); // Date format


        coursesContainer = findViewById(R.id.courses_container);

        if (auth.getCurrentUser() != null) {
            String currentUserEmail = auth.getCurrentUser().getEmail();

            coursesReference.orderByChild("startDate").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    coursesContainer.removeAllViews();
                    List<DataSnapshot> userCourses = new ArrayList<>();

                    for (DataSnapshot courseSnapshot : dataSnapshot.getChildren()) {
                        List<String> assignees = (List<String>) courseSnapshot.child("assignees").getValue();

                        if (assignees != null && assignees.contains(currentUserEmail)) {
                            userCourses.add(courseSnapshot);
                        }
                    }

                    Collections.reverse(userCourses);

                    for (DataSnapshot courseSnapshot : userCourses) {
                        String courseCode = courseSnapshot.child("courseCode").getValue(String.class);
                        String courseName = courseSnapshot.child("courseName").getValue(String.class);
                        String startDateStr = courseSnapshot.child("startDate").getValue(String.class);

                        String courseState;
                        try {
                            Date startDate = dateFormat.parse(startDateStr);
                            if (new Date().after(startDate)) {
                                courseState = "Complete";
                            } else {
                                courseState = "Attending";
                            }
                        } catch (ParseException e) {
                            courseState = "Unknown";
                        }

                        addCourseCard(courseCode, courseName, courseState);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Toast.makeText(InstructorCourseActivity.this, "Error fetching courses: " + databaseError.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        } else {
            Toast.makeText(InstructorCourseActivity.this, "No user logged in.", Toast.LENGTH_SHORT).show();
        }

        stateSpinner = findViewById(R.id.state_spinner);
        startDateEditText = findViewById(R.id.start_date_edit_text);
        //endDateEditText = findViewById(R.id.end_date_edit_text);
        applyFilterButton = findViewById(R.id.apply_filter_button);

        // State spinner setup
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.course_states_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        stateSpinner.setAdapter(adapter);

        // Date selection
        startDateEditText.setOnClickListener(view -> {
            showDatePicker(startDateEditText);
        });


        applyFilterButton.setOnClickListener(view -> {
            applyFilters();
        });

    }

    private void addCourseCard(String courseCode, String courseName, String courseState) {

        CardView card = new CardView(this);


        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        layoutParams.setMargins(16, 16, 16, 16);
        card.setLayoutParams(layoutParams);

        card.setRadius(8f);
        card.setElevation(4f);
        card.setPadding(32, 32, 32, 32);
        card.setBackgroundResource(R.drawable.border);

        LinearLayout cardLayout = new LinearLayout(this);
        cardLayout.setOrientation(LinearLayout.VERTICAL);

        TextView codeTextView = new TextView(this);
        codeTextView.setText("Course Code: " + courseCode);
        codeTextView.setPadding(16, 16, 16, 16);
        codeTextView.setTextSize(18);

        TextView nameTextView = new TextView(this);
        nameTextView.setText("Course Name: " + courseName);
        nameTextView.setPadding(16, 16, 16, 16);
        nameTextView.setTextSize(18);

        TextView stateTextView = new TextView(this);
        stateTextView.setText("State: " + courseState);
        stateTextView.setPadding(16, 16, 16, 16);
        stateTextView.setTextSize(18);

        cardLayout.addView(codeTextView);
        cardLayout.addView(nameTextView);
        cardLayout.addView(stateTextView);

        card.addView(cardLayout);

        card.setOnClickListener(v -> {
            Intent intent = new Intent(InstructorCourseActivity.this, CourseDetailsActivity.class);
            intent.putExtra("courseCode", courseCode);
            intent.putExtra("courseName", courseName);
            startActivity(intent);
        });

        coursesContainer.addView(card);
    }


    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.courses_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();
        if (id == R.id.cm_refresh) {
            startActivity(getIntent());
            finish();
            overridePendingTransition(0, 0);
        } else if (id == R.id.cm_create_course) {
            System.out.println("add course");
            Intent intent = new Intent(InstructorCourseActivity.this, CreateCourseActivity.class);
            startActivity(intent);

        }else if (id == R.id.cm_reports) {

            Intent intent = new Intent(InstructorCourseActivity.this, InstructorReportsActivity.class);
            startActivity(intent);

        }
        else if (id == R.id.cm_profile) {

            Intent intent = new Intent(InstructorCourseActivity.this, UserProfileActivity.class);
            startActivity(intent);

        }
        else if (id == R.id.cm_post) {

            Intent intent = new Intent(InstructorCourseActivity.this, PostsActivity.class);
            startActivity(intent);

        }
        else {
            Toast.makeText(InstructorCourseActivity.this, "Something went wrong.", Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }


    private void showDatePicker(EditText target) {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (DatePicker view, int year, int month, int dayOfMonth) -> {
                    String dateStr = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth);
                    target.setText(dateStr);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void applyFilters() {
        String startDateStr = startDateEditText.getText().toString();

        if (startDateStr.isEmpty()) {
            Toast.makeText(this, "Please select a start date.", Toast.LENGTH_SHORT).show();
            return; // Tarih yoksa, işlemi iptal et
        }

        try {
            Date filterStartDate = dateFormat.parse(startDateStr);

            coursesReference.orderByChild("startDate").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    coursesContainer.removeAllViews(); // Eski verileri temizle
                    List<DataSnapshot> filteredCourses = new ArrayList<>();

                    for (DataSnapshot courseSnapshot : dataSnapshot.getChildren()) {
                        String courseCode = courseSnapshot.child("courseCode").getValue(String.class);
                        String courseName = courseSnapshot.child("courseName").getValue(String.class);
                        String courseState = courseSnapshot.child("state").getValue(String.class);
                        String courseStartDateStr = courseSnapshot.child("startDate").getValue(String.class);

                        // Kursun başlangıç tarihi filtre tarihinden sonra mı?
                        if (courseStartDateStr != null && !courseStartDateStr.isEmpty()) {
                            Date courseStartDate = null;
                            try {
                                courseStartDate = dateFormat.parse(courseStartDateStr);
                            } catch (ParseException e) {
                                throw new RuntimeException(e);
                            }
                            if (courseStartDate.after(filterStartDate) || courseStartDate.equals(filterStartDate)) {

                                filteredCourses.add(courseSnapshot);
                            }
                        }
                    }

                    // Filtrelenen kursları ekleme
                    for (DataSnapshot course : filteredCourses) {
                        String courseCode = course.child("courseCode").getValue(String.class);
                        String courseName = course.child("courseName").getValue(String.class);
                        String courseState = course.child("state").getValue(String.class);

                        addCourseCard(courseCode, courseName, courseState);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Toast.makeText(InstructorCourseActivity.this, "Error fetching courses: " + databaseError.getMessage(), Toast.LENGTH_LONG).show();
                }
            });

        } catch (ParseException e) {
            Toast.makeText(this, "Invalid date format.", Toast.LENGTH_SHORT).show();
        }
    }
}