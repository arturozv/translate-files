package com.zenval.translatefiles.job.components;

import com.zenval.translatefiles.dto.Files;
import com.zenval.translatefiles.file.FileLineCounter;
import com.zenval.translatefiles.service.BatchAggregator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

@Component
public class FilePartitioner implements Partitioner {
    private static final Logger logger = LoggerFactory.getLogger(FilePartitioner.class);
    public static final String FILE_KEY = "file";
    public static final String LINE_COUNT_KEY = "lineCount";
    public static final String FILE_ID_KEY = "fileId";

    private Files files;
    private BatchAggregator batchAggregator;

    @Autowired
    public FilePartitioner(Files files, BatchAggregator batchAggregator) {
        this.files = files;
        this.batchAggregator = batchAggregator;
    }

    @Override
    public Map<String, ExecutionContext> partition(int gridSize) {
        Map<String, ExecutionContext> partitionMap = new HashMap<>();

        if (files != null) {
            for (String path : files.getPaths()) {
                File file = new File(path);

                Long lineCount = FileLineCounter.getFileLineCount(file);

                logger.info("Preparing to process file: {}, lines: {}", path, lineCount);

                ExecutionContext context = new ExecutionContext();
                context.put(FILE_ID_KEY, path);
                context.put(FILE_KEY, file);
                context.put(LINE_COUNT_KEY, lineCount);

                partitionMap.put(path, context);

                batchAggregator.registerFileLength(path, lineCount);
            }
        }
        return partitionMap;
    }
}
