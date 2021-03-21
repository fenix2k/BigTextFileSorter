import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Queue;

public class BigTextFileSorter {

    public static void main(String[] args) throws IOException {

        String filename = "testfile4.txt";
        Path filePath = Paths.get(filename);
        long fileSize = Files.size(filePath);
        int partsCount = 6;
        long partSize = fileSize / partsCount;

        Queue<String[]> queue = new ArrayDeque<>();
        long currentOffset = 0;
        while (true) {

            FileReadResult fileReadResult = readFile(filePath, currentOffset, partSize);
            Arrays.parallelSort(fileReadResult.getStrings());
            currentOffset = fileReadResult.getOffset();
            queue.add(fileReadResult.getStrings());

            if(queue.size() >= 2) {
                mergeSortingToFile(queue.poll(), queue.poll());
            }

            if(fileReadResult.isEOF()) break;
        }

        return;

    }

    private static FileReadResult readFile(Path path, long offset, long length) throws IOException {
        long lastOffset;
        boolean isEOF = false;
        RandomAccessFile file = new RandomAccessFile(path.toString(), "r");
        file.seek(offset);

        byte[] buffer = new byte[(int) length + 1];
        file.read(buffer, 0, (int) length);

        String[] stringList = new String(buffer).split(System.lineSeparator());

        if (offset + length < file.length()) {
            file.seek(offset + length - stringList[stringList.length - 1].getBytes().length + 1);
            stringList[stringList.length - 1] = file.readLine();
        }
        lastOffset = file.getFilePointer();
        if(lastOffset == file.length())
            isEOF = true;

        file.close();

        return new FileReadResult(stringList, lastOffset, isEOF);
    }

    private static Path mergeSortingToFile(Path filepath, String[] s1, String[] s2) {


        return filepath;
    }

    private static String mergeSorting(String s1, String s2) {
        if (s1 == null && s2 != null) return s2;
        if (s2 == null && s1 != null) return s1;

        if (s1.length() == 0) return s2;
        if (s2.length() == 0) return s1;

        Character[] resultStr = new Character[s1.length() + s2.length()];

        int i = 0;
        int j = 0;
        for (int k = 0; k < resultStr.length; k++) {

            if (i == s1.length()) {
                resultStr[k] = s2.charAt(j);
                j++;
            } else if (j == s2.length()) {
                resultStr[k] = s1.charAt(i);
                i++;
            }

            if (s1.charAt(i) > s2.charAt(j)) {
                resultStr[k] = s1.charAt(i);
                i++;
            } else {
                resultStr[k] = s2.charAt(j);
                j++;
            }

        }

        return resultStr.toString();
    }
}
