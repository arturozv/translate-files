package com.zenval.translatefiles;

import org.junit.Ignore;
import org.junit.Test;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;


@Ignore
public class GenerateTestFiles {

    @Test
    public void generateTestFiles() throws Exception {
        int maxLines = 100;
        for (int file = 1; file <= 10; file++) {
            try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("file" + file), "utf-8"))) {
                for (int word = 1; word <= maxLines - file; word++) {
                    String text = "file" + file + "word" + word;
                    if (word != maxLines - file) {
                        text += "\n";
                    }
                    writer.write(text);
                }
            }
        }
    }
}
