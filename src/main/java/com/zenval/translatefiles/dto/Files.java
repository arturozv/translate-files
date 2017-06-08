package com.zenval.translatefiles.dto;

import com.zenval.translatefiles.file.FileLineCounter;
import com.zenval.translatefiles.file.FileValidator;
import com.zenval.translatefiles.file.InvalidFileException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Files {
    private static final Logger logger = LoggerFactory.getLogger(Files.class);

    public final static String TRANSLATED_EXTENSION = ".translated";

    private List<String> paths;
    private Map<String, Long> lineCount;
    private Long totalLinesCount;

    private Files(List<String> paths, Map<String, Long> lineCount, Long totalLinesCount) {
        this.paths = paths;
        this.lineCount = lineCount;
        this.totalLinesCount = totalLinesCount;
    }

    public List<String> getPaths() {
        return paths;
    }


    @Override
    public String toString() {
        return "{" + paths + '}';
    }

    public void deleteTmp() {
        paths.forEach(path -> new File(path + TRANSLATED_EXTENSION).delete());
    }

    public long getTotalLinesCount() {
        return totalLinesCount;
    }

    public static final class Factory {
        private Factory() {
        }

        public static Files newInstance(List<String> paths) throws InvalidFileException {
            Map<String, Long> lineCount = new HashMap<>(paths.size());
            Long totalLinesCount = 0l;

            //validate files
            for (String path : paths) {
                File file = new File(path);
                FileValidator.validate(file);
                Long count = FileLineCounter.getFileLineCount(file);
                lineCount.put(path, count);
                totalLinesCount += count;
            }

            logger.info("Preparing to process {} files with {} number of lines", paths.size(), totalLinesCount);

            return new Files(paths, lineCount, totalLinesCount);
        }
    }
}
