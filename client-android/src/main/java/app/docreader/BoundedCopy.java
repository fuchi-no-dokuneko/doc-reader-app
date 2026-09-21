package app.docreader;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public final class BoundedCopy {
    public static final class TooLarge extends IOException {}

    public static long copy(InputStream in, OutputStream out, long limit) throws IOException {
        byte[] buffer = new byte[16384];
        long total = 0;
        int count;
        while ((count = in.read(buffer)) != -1) {
            total += count;
            if (total > limit) throw new TooLarge();
            if (Thread.currentThread().isInterrupted()) throw new IOException("Cancelled");
            out.write(buffer, 0, count);
        }
        return total;
    }
    private BoundedCopy() {}
}
