package com.vespa.baek.cafeoma.main.view.memo.data;

import java.util.Date;

public class Memo {

    private String title;
    private String contents;
    private String date;

    public Memo() {
    }

    public Memo(String title, String contents) {
        this.title = title;
        this.contents = contents;
    }

    public Memo(String title, String contents, String date) {
        this.title = title;
        this.contents = contents;
        this.date = date;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContents() {
        return contents;
    }

    public void setContents(String contents) {
        this.contents = contents;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
