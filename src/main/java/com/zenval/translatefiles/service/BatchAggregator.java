package com.zenval.translatefiles.service;

public interface BatchAggregator {

    /**
     * enqueue a translated text to be processed for the batch file
     * @param text translated text to be processed
     * @param line line of file which text belongs
     * @param fileId file which text belongs
     */
    void enqueue(String text, Long line, String fileId);

    /**
     * Register a file and it's line count to be able to generate the batches by line
     * @param fileId file id (path)
     * @param lineCount number of lines of the file
     */
    void registerFileLength(String fileId, Long lineCount);
}
