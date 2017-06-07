package com.zenval.translatefiles;

import org.junit.Ignore;
import org.junit.Test;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;


@Ignore
public class GenerateFiles {

    @Test
    public void generateTestFiles() throws Exception {
        for (int file = 1; file <= 4; file++) {
            try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("file" + file), "utf-8"))) {
                for (int word = 1; word <= 16 - file; word++) {
                    writer.write("file" + file + "word" + word + "\n");
                }
            }
        }
    }
}
