package com.prox.limeapp.Models;

public class ReportCardModel {

    String childId;
    String childName;
    String marks;
    String position;
    String cardImgUrl;
    String termInfo;
    String teacherName;
    String cardId;

    public String getCardId() {
        return cardId;
    }

    public void setCardId(String cardId) {
        this.cardId = cardId;
    }

    public String getTeacherName() {
        return teacherName;
    }

    public void setTeacherName(String teacherName) {
        this.teacherName = teacherName;
    }

    public String getChildId() {
        return childId;
    }

    public void setChildId(String childId) {
        this.childId = childId;
    }

    public String getChildName() {
        return childName;
    }

    public void setChildName(String childName) {
        this.childName = childName;
    }

    public String getMarks() {
        return marks;
    }

    public void setMarks(String marks) {
        this.marks = marks;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getCardImgUrl() {
        return cardImgUrl;
    }

    public void setCardImgUrl(String cardImgUrl) {
        this.cardImgUrl = cardImgUrl;
    }

    public String getTermInfo() {
        return termInfo;
    }

    public void setTermInfo(String termInfo) {
        this.termInfo = termInfo;
    }
}
