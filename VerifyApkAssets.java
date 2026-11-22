import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.stream.Stream;

/** Verify the signed archive by exact, case-sensitive ZIP name, without extracting it. */
public final class VerifyApkAssets {
    public static void main(String[] args) throws IOException {
        Path root = Paths.get(args[1]);
        Set<String> names = new HashSet<>();
        int count = 0;
        try (ZipFile apk = new ZipFile(args[0]); Stream<Path> paths = Files.walk(root)) {
            Enumeration<? extends ZipEntry> entries = apk.entries();
            while (entries.hasMoreElements()) {
                String name = entries.nextElement().getName();
                if (name.indexOf('\\') >= 0 || !names.add(name))
                    throw new IOException("Invalid or duplicate APK entry: " + name);
            }
            Iterator<Path> files = paths.filter(Files::isRegularFile).iterator();
            while (files.hasNext()) {
                Path file = files.next();
                String relative = root.relativize(file).toString().replace(File.separatorChar, '/');
                String key = "assets/" + relative;
                ZipEntry entry = apk.getEntry(key);
                if (entry == null || !key.equals(entry.getName()))
                    throw new IOException("Missing exact APK entry: " + key);
                if ((key.endsWith(".mp4") || key.endsWith(".jpg") || key.endsWith(".jpeg")) && entry.getMethod() != ZipEntry.STORED)
                    throw new IOException("Media must support direct reads: " + key);
                byte[] expected = Files.readAllBytes(file);
                try (InputStream actual = apk.getInputStream(entry)) {
                    int offset = 0, read;
                    byte[] buffer = new byte[8192];
                    while ((read = actual.read(buffer)) != -1) {
                        for (int i = 0; i < read; i++) {
                            if (offset >= expected.length || expected[offset++] != buffer[i])
                                throw new IOException("Unexpected contents: " + key);
                        }
                    }
                    if (offset != expected.length) throw new IOException("Truncated asset: " + key);
                }
                count++;
            }
        }
        if (count == 0) throw new IOException("No assets to verify");
        System.out.println("Verified " + count + " exact asset paths and contents in signed APK.");
    }
}
