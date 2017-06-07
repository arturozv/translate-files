package com.zenval.translatefiles.job.components;

import com.zenval.translatefiles.service.Translation;

import org.springframework.batch.item.ItemWriter;

import java.util.List;

public class BatchFileWriter implements ItemWriter<Translation> {

    @Override
    public void write(List<? extends Translation> items) throws Exception {

    }
}
