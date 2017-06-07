package com.zenval.translatefiles.dto;

import java.util.ArrayList;
import java.util.List;

public class BatchGroup {
    private Long line;
    private List<Translation> translations = new ArrayList<>();
    private Long expectedWordCount;

    public BatchGroup(Long line, Long expectedWordCount) {
        this.line = line;
        this.expectedWordCount = expectedWordCount;
    }

    public Long getLine() {
        return line;
    }

    public List<Translation> getTranslations() {
        return translations;
    }

    public Long getExpectedWordCount() {
        return expectedWordCount;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("BatchGroup{");
        sb.append("line=").append(line);
        sb.append(", expectedWordCount=").append(expectedWordCount);
        sb.append(", translations=").append(translations);
        sb.append('}');
        return sb.toString();
    }
}
