package com.example.demo;
public class CourseInfo {
    private String courseName;
    private String hoursCompleted;
    private String totalHours;
    private String teacherName;

    public CourseInfo(String courseName, String hoursCompleted, String totalHours, String teacherName) {
        this.courseName = courseName;
        this.hoursCompleted = hoursCompleted;
        this.totalHours = totalHours;
        this.teacherName = teacherName;
    }

    public CourseInfo() {

    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public String getHoursCompleted() {
        return hoursCompleted;
    }

    public void setHoursCompleted(String hoursCompleted) {
        this.hoursCompleted = hoursCompleted;
    }

    public String getTotalHours() {
        return totalHours;
    }

    public void setTotalHours(String totalHours) {
        this.totalHours = totalHours;
    }

    public String getTeacherName() {
        return teacherName;
    }

    public void setTeacherName(String teacherName) {
        this.teacherName = teacherName;
    }
}
