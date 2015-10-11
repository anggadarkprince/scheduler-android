package com.sketchproject.scheduler.model;

/**
 * Scheduler Android App
 * Created by Angga on 10/8/2015.
 */
public class ScheduleItem {
    private int id;
    private String event;
    private String description;
    private String date;
    private String day;
    private String time;
    private String leftDate;
    private String leftMonth;

    public ScheduleItem() {}

    public ScheduleItem(int id, String event, String description, String date, String day, String time, String leftMonth, String leftDate){
        this.id = id;
        this.event = event;
        this.description = description;
        this.date = date;
        this.day = day;
        this.time = time;
        this.leftDate = leftDate;
        this.leftMonth = leftMonth;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getLeftDate() {
        return leftDate;
    }

    public void setLeftDate(String leftDate) {
        this.leftDate = leftDate;
    }

    public String getLeftMonth() {
        return leftMonth;
    }

    public void setLeftMonth(String leftMonth) {
        this.leftMonth = leftMonth;
    }

}
