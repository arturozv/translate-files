package com.zenval.translatefiles.file;

import java.io.File;
import java.util.List;

public class Files {

    private List<String> paths;

    private Files(List<String> paths) {
        this.paths = paths;
    }

    public List<String> getPaths() {
        return paths;
    }

    @Override
    public String toString() {
        return "{" + paths + '}';
    }

    public static final class Factory {
        private Factory() {
        }

        public static Files newInstance(List<String> paths) throws InvalidFileException {
            Files files = new Files(paths);

            //validate files
            for (String path : paths) {
                File file = new File(path);
                FileValidator.validate(file);
            }

            return files;
        }
    }
}
