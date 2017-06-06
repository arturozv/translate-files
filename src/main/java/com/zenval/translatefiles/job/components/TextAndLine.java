package com.zenval.translatefiles.job.components;

/**
 * Created by arturo on 06/06/17.
 */
public class TextAndLine {
    private String text;
    private long line;

    public TextAndLine(String text, int line) {
        this.text = text;
        this.line = line;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public long getLine() {
        return line;
    }

    public void setLine(int line) {
        this.line = line;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("{");
        sb.append("t='").append(text).append('\'');
        sb.append(", l=").append(line);
        sb.append('}');
        return sb.toString();
    }
}
