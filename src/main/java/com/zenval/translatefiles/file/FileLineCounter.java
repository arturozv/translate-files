package com.zenval.translatefiles.file;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;

/**
 * Counts lines on a file
 */
public class FileLineCounter {
    private static final Logger logger = LoggerFactory.getLogger(FileLineCounter.class);

    private FileLineCounter() {
    }

    /**
     * Counts lines in a file
     *
     * @param file file to count lines
     * @return file line count or null if there's an exception
     */
    public static Long getFileLineCount(File file) {
        Long count = null;

        if(file.length() == 0) {
            return 0l;
        }

        try {
            LineNumberReader lnr = new LineNumberReader(new FileReader(file));
            lnr.skip(Long.MAX_VALUE); //go to the end of the file
            count = lnr.getLineNumber() + 1l; //Add 1 because line index starts at 0
            lnr.close();
        } catch (IOException e) {
            logger.error("Error counting lines for file: " + file.getName(), e);
        }
        return count;
    }
}
