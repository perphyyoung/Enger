package edu.perphy.enger.data;

/**
 * Created by perphy on 2016/4/15 0015.
 * 单词类
 */
public class Word {
    String word;
    int offset, length;

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }
}
