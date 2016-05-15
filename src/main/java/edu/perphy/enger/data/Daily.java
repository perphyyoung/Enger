package edu.perphy.enger.data;

import org.json.JSONArray;

/**
 * Created by perphy on 2016/4/12 0012.
 * 每日一句实体类
 */
public class Daily {
    private long id;
    private String sid; //每日一句ID
    private String tts; //音频地址
    private String english; //英文内容(content)
    private String chinese; //中文内容(note)
    private String love; //喜欢个数
    private String comment; //词霸小编(translation)
    private String picture; //图片地址
    private String picture2; //大图片地址
    private String caption; //标题
    private String date; //时间(dateline)
    private String s_pv; //浏览数
    private String sp_pv; //语音评测浏览数
    private JSONArray tags; //相关标签
    private String share; //分享图片(fenxiang_img)
    private boolean starred;

    public Daily() { }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setSid(String sid) {
        this.sid = sid;
    }

    public String getTts() {
        return tts;
    }

    public void setTts(String tts) {
        this.tts = tts;
    }

    public String getEnglish() {
        return english;
    }

    public void setEnglish(String english) {
        this.english = english;
    }

    public String getChinese() {
        return chinese;
    }

    public void setChinese(String chinese) {
        this.chinese = chinese;
    }

    public void setLove(String love) {
        this.love = love;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public void setPicture2(String picture2) {
        this.picture2 = picture2;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setS_pv(String s_pv) {
        this.s_pv = s_pv;
    }

    public void setSp_pv(String sp_pv) {
        this.sp_pv = sp_pv;
    }

    public void setTags(JSONArray tags) {
        this.tags = tags;
    }

    public void setShare(String share) {
        this.share = share;
    }

    public void setStarred(boolean starred) {
        this.starred = starred;
    }

    public boolean isStarred() {
        return starred;
    }
}
