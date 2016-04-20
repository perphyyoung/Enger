package edu.perphy.enger.data;

/**
 * Created by perphy on 2016/4/12 0012.
 * 每日一句实体类
 */
public class Daily {
    private long id;
    private String content, note, date;
    private boolean starred;

    public Daily() {
    }

    public boolean isStarred() {
        return starred;
    }

    public void setStarred(boolean starred) {
        this.starred = starred;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
