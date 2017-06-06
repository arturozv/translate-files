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
import java.util.function.Function;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;

public class InMemoryBatchAggregator implements BatchAggregator {
    private static final Logger logger = LoggerFactory.getLogger(InMemoryBatchAggregator.class);

    private BlockingQueue<QueueItem> queue = new ArrayBlockingQueue<>(1_000_000);
    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
    private Map<Long, BatchItem> wordsByLineNumber = new HashMap<>();
    private Map<String, Long> lineCountByFile = new HashMap<>();

    public InMemoryBatchAggregator() {
    }

    public InMemoryBatchAggregator start() {
        executorService.scheduleAtFixedRate(() -> {
            int toDrain = queue.size();
            if (toDrain > 0) {
                List<QueueItem> toProcess = new ArrayList<>();
                queue.drainTo(toProcess, toDrain);

                if (toProcess.size() > 0) {
                    logger.debug("aggregating {} words", toProcess.size());

                    Map<Long, List<QueueItem>> wordsByLineNumber = groupByLine(toProcess);
                    addToMainMap(wordsByLineNumber, this.wordsByLineNumber);
                }
            }
        }, 1, 1, TimeUnit.SECONDS);

        return this;
    }

    Map<Long, List<QueueItem>> groupByLine(List<QueueItem> toProcess) {
        return toProcess.stream().collect(groupingBy(QueueItem::getLine, mapping(Function.identity(), toList())));
    }

    void addToMainMap(Map<Long, List<QueueItem>> from, Map<Long, BatchItem> to) {
        for (Map.Entry<Long, List<QueueItem>> entry : from.entrySet()) {
            Long lineNumber = entry.getKey();
            List<QueueItem> words = entry.getValue();

            BatchItem batchItem = to.get(lineNumber);

            if (batchItem == null) {
                //lineCountByFile.get()
                //batchItem = new BatchItem(lineNumber);
            }

            //batchItem.getWords().addAll(words);
        }
    }

    @Override
    public void enqueue(String text, Long line, String fileId) {
        queue.add(new QueueItem(text, line, fileId));
    }

    @Override
    public void registerFileLength(String fileId, Long lineCount) {
        lineCountByFile.put(fileId, lineCount);
    }

    public class BatchItem {
        private Long line;
        private List<String> words = new ArrayList<>();
        private Long expected;

        public BatchItem(Long line, Long expected) {
            this.line = line;
            this.expected = expected;
        }

        public Long getLine() {
            return line;
        }

        public List<String> getWords() {
            return words;
        }

        public Long getExpected() {
            return expected;
        }

        @Override
        public String toString() {
            final StringBuffer sb = new StringBuffer("BatchItem{");
            sb.append("line=").append(line);
            sb.append(", words=").append(words);
            sb.append(", expected=").append(expected);
            sb.append('}');
            return sb.toString();
        }
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
