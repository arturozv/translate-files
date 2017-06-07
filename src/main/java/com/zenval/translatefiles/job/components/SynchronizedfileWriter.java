package com.zenval.translatefiles.job.components;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStream;
import org.springframework.batch.item.ItemWriter;

import java.util.List;

public class SynchronizedFileWriter<T> implements ItemWriter<T>, ItemStream {
    private static final Logger logger = LoggerFactory.getLogger(SynchronizedFileWriter.class);

    private int closeCount;
    private boolean isOpen;
    private boolean isClosed;

    public SynchronizedFileWriter(int closeCount){
        this.closeCount = closeCount;
    }

    private ItemWriter<T> itemWriter;
    private boolean isStream = false;

    public void setItemWriter(ItemWriter<T> itemWriter) {
        this.itemWriter = itemWriter;
        if (itemWriter instanceof ItemStream) {
            isStream = true;
        }
    }

    @Override
    public synchronized void close() {
        logger.info("closing writer attempt {}", closeCount);

        if(!isClosed && closeCount-- == 0){
            logger.info("closing writer");
            isClosed = true;
            if (isStream) {
                ((ItemStream) itemWriter).close();
            }
        }
    }

    public void closeAfterStep() {


    }

    @Override
    public synchronized void open(ExecutionContext executionContext) {
        logger.info("opening writer");
        if (isStream && !isOpen) {
            ((ItemStream) itemWriter).open(new ExecutionContext());
            isOpen = true;
        }
    }

    @Override
    public synchronized void update(ExecutionContext executionContext) {
    }

    @Override
    public synchronized void write(List<? extends T> items) throws Exception {
        itemWriter.write(items);
    }
}
