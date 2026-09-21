package app.docreader;

import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public final class TextDecoder {
    public static String decode(byte[] data) throws CharacterCodingException {
        Charset encoding = StandardCharsets.UTF_8;
        int start = 0;
        if (data.length >= 3 && data[0] == (byte) 0xef && data[1] == (byte) 0xbb && data[2] == (byte) 0xbf) start = 3;
        else if (data.length >= 2 && data[0] == (byte) 0xff && data[1] == (byte) 0xfe) {
            encoding = StandardCharsets.UTF_16LE; start = 2;
        } else if (data.length >= 2 && data[0] == (byte) 0xfe && data[1] == (byte) 0xff) {
            encoding = StandardCharsets.UTF_16BE; start = 2;
        }
        String text = encoding.newDecoder().decode(ByteBuffer.wrap(data, start, data.length - start)).toString();
        if (text.indexOf('\0') >= 0) throw new CharacterCodingException();
        return text;
    }
    private TextDecoder() {}
}
