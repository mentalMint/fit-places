package ru.nsu.fit.web.utilities;

import ru.nsu.fit.web.Main;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class KeyReader {
    public static String readKey(String fileName) throws IOException {
        String key;
        try (InputStream keyStream = Main.class.getResourceAsStream(fileName)) {
            if (keyStream == null) {
                System.err.println("Can't find geocoding-key.txt");
                throw new NullPointerException();
            }
            ByteArrayOutputStream result = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            for (int length; (length = keyStream.read(buffer)) != -1; ) {
                result.write(buffer, 0, length);
            }
            key = result.toString(StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IOException(e);
        }
        return key;
    }
}
