package com.example.courseassistantapp_auth;


import android.os.Bundle;

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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class InstructorReportsActivity extends AppCompatActivity {


    private RecyclerView reportsRecyclerView;
    private ReportAdapter reportAdapter;
    private List<ForumMessage> reportList;
    private FirebaseAuth auth;
    private DatabaseReference forumReference;
    private DatabaseReference coursesReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_instructor_reports);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("Reports");
        }

        reportsRecyclerView = findViewById(R.id.reports_recycler_view);
        reportsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        reportList = new ArrayList<>();
        reportAdapter = new ReportAdapter(reportList);
        reportsRecyclerView.setAdapter(reportAdapter);

        auth = FirebaseAuth.getInstance();
        forumReference = FirebaseDatabase.getInstance().getReference("Forum");
        coursesReference = FirebaseDatabase.getInstance().getReference("Courses");

        loadInstructorReports();





    }

    private void loadInstructorReports() {
        String userEmail = auth.getCurrentUser().getEmail();

        // Get all courses that the current user is assigned to
        coursesReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Set<String> instructorCourseCodes = new HashSet<>();
                for (DataSnapshot courseSnapshot : dataSnapshot.getChildren()) {
                    if (courseSnapshot.hasChild("assignees")) {
                        for (DataSnapshot assigneeSnapshot : courseSnapshot.child("assignees").getChildren()) {
                            String assigneeEmail = assigneeSnapshot.getValue(String.class);
                            if (userEmail.equals(assigneeEmail)) {
                                // Use the courseCode field instead of the key
                                String courseCode = courseSnapshot.child("courseCode").getValue(String.class);
                                if (courseCode != null) {
                                    instructorCourseCodes.add(courseCode);
                                }
                            }
                        }
                    }
                }

                // Get all reports and filter by instructorCourseCodes
                forumReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        reportList.clear();
                        for (DataSnapshot reportSnapshot : dataSnapshot.getChildren()) {
                            ForumMessage report = reportSnapshot.getValue(ForumMessage.class);
                            if (instructorCourseCodes.contains(report.courseCode)) {
                                reportList.add(report);
                            }
                        }

                        // Sort the reports by timestamp (descending order)
                        Collections.sort(reportList, new Comparator<ForumMessage>() {
                            @Override
                            public int compare(ForumMessage r1, ForumMessage r2) {
                                return r2.timestamp.compareTo(r1.timestamp);
                            }
                        });

                        reportAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


}
