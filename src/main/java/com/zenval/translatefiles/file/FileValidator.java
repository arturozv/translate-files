package com.zenval.translatefiles.file;

import java.io.File;

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
    static void validate(File file) throws InvalidFileException {
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
        }
    }
}
