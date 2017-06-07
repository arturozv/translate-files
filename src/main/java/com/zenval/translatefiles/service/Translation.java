package com.zenval.translatefiles.service;

public class Translation {
    private String text;
    private String translated;
    private Long line;
    private String fileId;

    public Translation(String text, String translated, Long line, String fileId) {
        this.text = text;
        this.translated = translated;
        this.line = line;
        this.fileId = fileId;
    }

    public String getTranslated() {
        return translated;
    }

    public String getText() {
        return text;
    }

    public Long getLine() {
        return line;
    }

    public String getFileId() {
        return fileId;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("{");
        sb.append("fileId='").append(fileId).append('\'');
        sb.append(", line=").append(line);
        sb.append(", text='").append(text).append('\'');
        sb.append(", translated='").append(translated).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
