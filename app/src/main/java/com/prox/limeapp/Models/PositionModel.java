package com.prox.limeapp.Models;

public class PositionModel {
    String position;
    String teacherName;
    String teacherProfile;
    String positionId;
    boolean isNewsSource;
    String schoolId;

    public String getPosition() {
        return position;
    }

    public String getSchoolId() {
        return schoolId;
    }

    public void setSchoolId(String schoolId) {
        this.schoolId = schoolId;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getTeacherName() {
        return teacherName;
    }

    public void setTeacherName(String teacherName) {
        this.teacherName = teacherName;
    }

    public String getTeacherProfile() {
        return teacherProfile;
    }

    public void setTeacherProfile(String teacherProfile) {
        this.teacherProfile = teacherProfile;
    }

    public String getPositionId() {
        return positionId;
    }

    public void setPositionId(String positionId) {
        this.positionId = positionId;
    }

    public boolean isNewsSource() {
        return isNewsSource;
    }

    public void setNewsSource(boolean newsSource) {
        isNewsSource = newsSource;
    }
}
