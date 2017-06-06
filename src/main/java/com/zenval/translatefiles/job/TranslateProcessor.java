package com.zenval.translatefiles.job;

import com.zenval.translatefiles.service.BatchAggregator;
import com.zenval.translatefiles.service.TranslateService;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;

public class TranslateProcessor implements ItemProcessor<TextAndLine, String> {

    private TranslateService translateService;
    private BatchAggregator batchAggregator;

    private final String fileId;

    @Autowired
    public TranslateProcessor(String fileId, TranslateService translateService, BatchAggregator batchAggregator) {
        this.translateService = translateService;
        this.fileId = fileId;
        this.batchAggregator = batchAggregator;
    }

    @Override
    public String process(TextAndLine item) throws Exception {
        batchAggregator.enqueue(item, fileId);
        return translateService.translate(item.getText(), "en", "sv");
    }
}
