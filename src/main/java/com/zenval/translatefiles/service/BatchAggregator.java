package com.zenval.translatefiles.service;

import com.zenval.translatefiles.dto.BatchGroup;
import com.zenval.translatefiles.dto.Translation;

public interface BatchAggregator {

    /**
     * Aggregate translations to write the batched file
     *
     * @param translations translations
     */
    void aggregate(Translation... translations);

    /**
     * Register a callback that will be called when a line is processed across all the files
     *
     * @param callback Callback to register
     */
    void registerCallback(Callback callback);

    /**
     * Register a file and it's line count to be able to generate the batches by line
     *
     * @param fileId    file id (path)
     * @param lineCount number of lines of the file
     */
    void registerFileLength(String fileId, Long lineCount);

    /**
     * Get the total lines of all files together
     *
     * @return total lines of all files together
     */
    long getTotalLines();


    interface Callback {
        /**
         * Called when a line is processed across all the files
         *
         * @param batchGroup batch group with all the translations for a line.
         */
        void onLineComplete(BatchGroup batchGroup);
    }
}
