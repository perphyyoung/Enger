package edu.perphy.enger.data;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

import edu.perphy.enger.db.NoteHelper;

/**
 * Created by perphy on 2016/4/7 0007.
 * 笔记类
 */
public class Note implements Serializable{
    private long id;
    private String title, content, starred, createTime, modifyTime;

    public Note() {
    }

    public Note(JSONObject obj) throws JSONException {
        this.title = (String) obj.get(NoteHelper.COL_TITLE);
        this.content = (String) obj.get(NoteHelper.COL_CONTENT);
        this.starred = (String) obj.get(NoteHelper.COL_STAR);
        this.createTime = (String) obj.get(NoteHelper.COL_CREATE_TIME);
        this.modifyTime = (String) obj.get(NoteHelper.COL_MODIFY_TIME);
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getModifyTime() {
        return modifyTime;
    }

    public void setModifyTime(String modifyTime) {
        this.modifyTime = modifyTime;
    }

    public String getStarred() {
        return starred;
    }

    public void setStarred(String starred) {
        this.starred = starred;
    }
}
