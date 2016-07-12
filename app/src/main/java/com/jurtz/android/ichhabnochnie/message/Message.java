package com.jurtz.android.ichhabnochnie.message;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by marcel on 27.05.16.
 */
public class Message {
    private String text;
    private String date;
    private String author;

    public Message(String text, String date, String author) {
        this.text = text;
        this.date = date;
        this.author = author;
    }

    public Message(String text, Date date, String author) {
        this.text = text;
        this.author = author;
        this.date = getStringFromDate(date);
    }

    public String getText() {
        return text;
    }

    public String getDate() {
        return date;
    }

    public String getAuthor() {
        return author;
    }

    public static String getStringFromDate(Date date) {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        String d = df.format(date);
        return d;
    }
}
