package edu.perphy.enger.data;

/**
 * Created by perphy on 2016/4/26 0026.
 * 词典信息类
 */
public class Dict {
    long id;
    String dictId, version, author, bookName, date, description;
    String email, idxOffsetBits, synWordCount, website, dictType;
    int wordCount, idxFileSize;
    char contentType;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getDictId() {
        return dictId;
    }

    public void setDictId(String dictId) {
        this.dictId = dictId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getBookName() {
        return bookName;
    }

    public void setBookName(String bookName) {
        this.bookName = bookName;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getIdxOffsetBits() {
        return idxOffsetBits;
    }

    public void setIdxOffsetBits(String idxOffsetBits) {
        this.idxOffsetBits = idxOffsetBits;
    }

    public String getSynWordCount() {
        return synWordCount;
    }

    public void setSynWordCount(String synWordCount) {
        this.synWordCount = synWordCount;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getDictType() {
        return dictType;
    }

    public void setDictType(String dictType) {
        this.dictType = dictType;
    }

    public int getWordCount() {
        return wordCount;
    }

    public void setWordCount(int wordCount) {
        this.wordCount = wordCount;
    }

    public int getIdxFileSize() {
        return idxFileSize;
    }

    public void setIdxFileSize(int idxFileSize) {
        this.idxFileSize = idxFileSize;
    }

    public char getContentType() {
        return contentType;
    }

    public void setContentType(char contentType) {
        this.contentType = contentType;
    }
}
