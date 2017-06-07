package com.zenval.translatefiles.dto;

import java.util.ArrayList;
import java.util.List;

public class BatchGroup {
    private Long line;
    private List<String> words = new ArrayList<>();
    private Long expectedWordCount;

    public BatchGroup(Long line, Long expectedWordCount) {
        this.line = line;
        this.expectedWordCount = expectedWordCount;
    }

    public Long getLine() {
        return line;
    }

    public List<String> getWords() {
        return words;
    }

    public Long getExpectedWordCount() {
        return expectedWordCount;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("BatchGroup{");
        sb.append("line=").append(line);
        sb.append(", expectedWordCount=").append(expectedWordCount);
        sb.append(", words=").append(words);
        sb.append('}');
        return sb.toString();
    }
}
