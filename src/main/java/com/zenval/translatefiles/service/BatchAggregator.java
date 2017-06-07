package com.zenval.translatefiles.service;

import com.zenval.translatefiles.dto.BatchGroup;
import com.zenval.translatefiles.dto.Translation;

public interface BatchAggregator {

    void aggregate(Translation... translations);


    void registerCallback(Callback callback);

    /**
     * Register a file and it's line count to be able to generate the batches by line
     *
     * @param fileId    file id (path)
     * @param lineCount number of lines of the file
     */
    void registerFileLength(String fileId, Long lineCount);

    interface Callback {
        void onLineComplete(BatchGroup batchGroup);
    }
}
