package edu.perphy.enger.data;

/**
 * Created by perphy on 2016/4/18 0018.
 * 复习实体类
 */
public class Review {
    private long id;
    private String word, def, dateAdd, dateReivew;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public String getDef() {
        return def;
    }

    public void setDef(String def) {
        this.def = def;
    }

    public String getDateAdd() {
        return dateAdd;
    }

    public void setDateAdd(String dateAdd) {
        this.dateAdd = dateAdd;
    }

    public String getDateReivew() {
        return dateReivew;
    }

    public void setDateReivew(String dateReivew) {
        this.dateReivew = dateReivew;
    }
}
