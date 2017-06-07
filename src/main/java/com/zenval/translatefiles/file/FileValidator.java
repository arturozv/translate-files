package com.zenval.translatefiles.file;

import java.io.File;
import java.io.FileReader;
import java.io.LineNumberReader;

/**
 * File validation
 */
public class FileValidator {

    private FileValidator() {
    }

    /**
     * Validates a file:
     * - is not null
     * - exists
     * - is not a directory
     * - is readable
     * - is not empty
     * @param file
     * @throws InvalidFileException
     */
    public static void validate(File file) throws InvalidFileException {
        if (file == null) {
            throw new InvalidFileException("File is null");

        } else if (!file.exists()) {
            throw new InvalidFileException("File " + file.getName() + " does not exist");

        } else if (file.isDirectory()) {
            throw new InvalidFileException("File " + file.getName() + " is a directory!");

        } else if (!file.canRead()) {
            throw new InvalidFileException("File " + file.getName() + " is not readable");

        } else if (file.length() == 0) {
            throw new InvalidFileException("File " + file.getName() + " is empty!!");

        } else if (hasEmptyLines(file)) {
            throw new InvalidFileException("File " + file.getName() + " has empty lines!!");
        }
    }

    static boolean hasEmptyLines(File file) throws InvalidFileException {
        try {

            LineNumberReader lnr = new LineNumberReader(new FileReader(file));
            lnr.skip(Long.MAX_VALUE); //go to the end of the file
            if (lnr.readLine() == null) {
                return true;
            }

        } catch (java.io.IOException e) {
            throw new InvalidFileException("Error validating file " + file.getName());
        }
        return false;
    }
}
