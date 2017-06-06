package com.zenval.translatefiles.service;

public interface BatchAggregator {
    void enqueue(String text, Long line, String fileId);
    void registerFileLength(String fileId, Long lineCount);
}
