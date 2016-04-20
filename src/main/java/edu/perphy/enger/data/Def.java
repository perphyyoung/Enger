package edu.perphy.enger.data;

/**
 * Created by perphy on 2016/4/14 0014.
 * 释义类
 */
public class Def {
    private boolean internal;
    private String dictName, def;

    public String getDictName() {
        return dictName;
    }

    public void setDictName(String dictName) {
        this.dictName = dictName;
    }

    public String getDef() {
        return def;
    }

    public void setDef(String def) {
        this.def = def;
    }

    public boolean isInternal() {
        return internal;
    }

    public void setInternal(boolean internal) {
        this.internal = internal;
    }
}
