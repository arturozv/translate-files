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
        int files = 8;
        int maxLines = 10000000;

        for (int file = 1; file <= files; file++) {
            try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("file" + file), "utf-8"))) {
                StringBuffer stringBuffer = new StringBuffer();
                for (int word = 1; word <= maxLines - file; word++) {
                    String text = "file" + file + "word" + word;
                    if (word != maxLines - file) {
                        text += "\n";
                    }
                    stringBuffer.append(text);

                    if (word % 1000 == 0) {
                        writer.write(stringBuffer.toString());
                        stringBuffer = new StringBuffer();
                    }
                }
                if (stringBuffer.length() > 0) {
                    writer.write(stringBuffer.toString());
                }
            }
        }
    }
}
