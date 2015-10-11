package com.sketchproject.scheduler.model;

/**
 * Scheduler Android App
 * Created by Angga on 10/8/2015.
 */
public class NoteItem {
    private int id;
    private String title;
    private String label;
    private String note;

    public NoteItem() {}

    public NoteItem(int id, String title, String label, String note){
        this.id = id;
        this.title = title;
        this.label = label;
        this.note = note;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

}
