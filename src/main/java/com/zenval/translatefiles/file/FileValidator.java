package com.zenval.translatefiles.file;

import java.io.File;
import java.io.RandomAccessFile;

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

        } else if (endsWithEmptyLines(file)) {
            throw new InvalidFileException("File " + file.getName() + " has empty lines!!");
        }
    }

    static boolean endsWithEmptyLines(File file) throws InvalidFileException {
        try {

            RandomAccessFile fileHandler = new RandomAccessFile(file, "r");
            long fileLength = fileHandler.length() - 1;
            if (fileLength < 0) {
                fileHandler.close();
                return true;
            }
            fileHandler.seek(fileLength);
            byte readByte = fileHandler.readByte();
            fileHandler.close();

            if (readByte == 0xA || readByte == 0xD) {
                return true;
            }
            return false;

        } catch (Exception e) {
            throw new InvalidFileException("Error validating file " + file.getName());
        }
    }
}
