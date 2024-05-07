package com.example.courseassistantapp_auth;

import java.util.ArrayList;

public class Course {
    private String courseName;
    private String courseCode;
    private String startDate;
    private int groupCount;

    private ArrayList<String> assignees;

    public Course() {
    }

    public Course(String courseName, String courseCode, String startDate, int groupCount) {
        this.courseName = courseName;
        this.courseCode = courseCode;
        this.startDate = startDate;
        this.groupCount = groupCount;
        this.assignees = new ArrayList<>();

    }

    // Getters and Setters
    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public int getGroupCount() {
        return groupCount;
    }

    public void setGroupCount(int groupCount) {
        this.groupCount = groupCount;
    }

    public ArrayList<String> getAssignees() {
        return assignees;
    }

    public void setAssignees(ArrayList<String> assignees) {
        this.assignees = assignees; // Assignees'i ayarlayın
    }


}
