package org.asciidoctor.asciidoclet;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.Charset;

/**
 * A handful of utilties borrowed from Guava and other sources
 *
 * @author John Ericksen
 */
public class Resources {

    private static final int EOF = -1;

    private Resources() {
        // Private utility constructor
    }

    public static InputStream getResource(String resourceName) {
        return Resources.class.getClassLoader().getResourceAsStream(resourceName);
    }

    public static void copy(InputStream src, OutputStream dest) throws IOException {
        byte[] buffer = new byte[256];
        int size = 0;
        while(EOF != (size = src.read(buffer))) {
            dest.write(buffer, 0, size);
        }
    }

    public static BufferedReader newReader(File file, Charset charset) throws FileNotFoundException {
        return new BufferedReader(new InputStreamReader(new FileInputStream(file), charset));
    }

    public static String toString(File inputFile) throws IOException{
        StringBuilder contentBuilder = new StringBuilder();
        BufferedReader reader = new BufferedReader(new FileReader(inputFile));
        String line;
        while ((line = reader.readLine()) != null){
            contentBuilder.append(line).append("\n");
        }
        return contentBuilder.toString();
    }

    public static File createTempDir() {
        File baseDir = new File(System.getProperty("java.io.tmpdir"));
        String baseName = System.currentTimeMillis() + "-";

        for(int counter = 0; counter < 10000; ++counter) {
            File tempDir = new File(baseDir, baseName + counter);
            if (tempDir.mkdir()) {
                return tempDir;
            }
        }

        throw new IllegalStateException("Failed to create directory within 10000 attempts (tried " + baseName + "0 to " + baseName + 9999 + ')');
    }
}
