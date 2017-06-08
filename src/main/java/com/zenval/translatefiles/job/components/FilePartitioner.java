package com.zenval.translatefiles.job.components;

import com.zenval.translatefiles.dto.Files;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class FilePartitioner implements Partitioner {
    public static final String FILE_ID_KEY = "fileId";

    private Files files;

    @Autowired
    public FilePartitioner(Files files) {
        this.files = files;
    }

    @Override
    public Map<String, ExecutionContext> partition(int gridSize) {
        Map<String, ExecutionContext> partitionMap = new HashMap<>();

        if (files != null) {
            for (String path : files.getPaths()) {
                ExecutionContext context = new ExecutionContext();
                context.put(FILE_ID_KEY, path);
                partitionMap.put(path, context);
            }
        }
        return partitionMap;
    }
}
