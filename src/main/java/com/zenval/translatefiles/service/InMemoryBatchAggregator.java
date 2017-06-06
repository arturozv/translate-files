package com.zenval.translatefiles.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;

public class InMemoryBatchAggregator implements BatchAggregator {
    private static final Logger logger = LoggerFactory.getLogger(InMemoryBatchAggregator.class);


    private BlockingQueue<QueueItem> queue = new ArrayBlockingQueue<>(1_000_000);
    final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
    Map<Long, List<String>> wordsByLineNumber = new HashMap<>();


    public InMemoryBatchAggregator start() {
        executorService.scheduleAtFixedRate(() -> {
            int toDrain = queue.size();
            if (toDrain > 0) {
                List<QueueItem> toProcess = new ArrayList<>();
                queue.drainTo(toProcess, toDrain);

                if (toProcess.size() > 0) {
                    logger.debug("aggregating {} words", toProcess.size());

                    Map<Long, List<String>> wordsByLineNumber = groupTextByLine(toProcess);
                    addToMainMap(wordsByLineNumber, this.wordsByLineNumber);
                }
            }
        }, 1, 1, TimeUnit.SECONDS);

        return this;
    }

    Map<Long, List<String>> groupTextByLine(List<QueueItem> toProcess) {
        return toProcess.stream().collect(groupingBy(QueueItem::getLine, mapping(QueueItem::getText, toList())));
    }

    void addToMainMap(Map<Long, List<String>> from, Map<Long, List<String>> to) {
        from.forEach((k, v) -> to.merge(k, v, (list1, list2) -> Stream.concat(list1.stream(), list2.stream()).collect(Collectors.toList())));
    }

    @Override
    public void enqueue(String text, Long line, String fileId) {
        queue.add(new QueueItem(text, line, fileId));
    }

    public class QueueItem {
        private String text;
        private Long line;
        private String fileId;

        public QueueItem(String text, Long line, String fileId) {
            this.text = text;
            this.line = line;
            this.fileId = fileId;
        }

        public String getText() {
            return text;
        }

        public Long getLine() {
            return line;
        }

        public String getFileId() {
            return fileId;
        }
    }
}
