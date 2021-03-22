package BigTextFileSorter;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class BigTextFileSorter {

    public static void main(String[] args) throws IOException {

        String filename = "_testdata/testfile4.txt";
        Path filePath = Paths.get(filename);
        long fileSize = Files.size(filePath);
        int partsCount = 6;
        long partSize = fileSize / partsCount;

        List<String> tempFiles = new ArrayList<>();
        Queue<String[]> queue = new ArrayDeque<>();
        long currentOffset = 0;
        int counter = 0;
        String tmpFileNameTemplate = "_testdata/tempsortedfile.tmp";

        while (true) {

            FileReadResult fileReadResult = readFile(filePath, currentOffset, partSize);
            Arrays.parallelSort(fileReadResult.getStrings());
            currentOffset = fileReadResult.getOffset();
            queue.add(fileReadResult.getStrings());

            if(queue.size() >= 2) {
                Path tmpFilePath = Paths.get(tmpFileNameTemplate + counter++);
                tempFiles.add(tmpFilePath.toString());
                mergeSortingToFile(tmpFilePath, queue.poll(), queue.poll());
            }
            else if(fileReadResult.isEOF()) {
                Path tmpFilePath = Paths.get(tmpFileNameTemplate + counter++);
                tempFiles.add(tmpFilePath.toString());
                writeStringsToFile(tmpFilePath, Arrays.asList(queue.poll()));
            }

            if(fileReadResult.isEOF()) break;
        }

        System.out.println(tempFiles.toString());

        String[] parts = tempFiles.toArray(new String[tempFiles.size()]);
        Queue<String> fileQueue = new ArrayDeque<>();
        List<String> newPartsList = new ArrayList<>();
        counter = 0;
        for (int i = 0; i < parts.length; i++) {

            fileQueue.add(parts[i]);

            if(fileQueue.size() == 2) {
                String tmpFilePath = tmpFileNameTemplate + "0" + counter++;
                fileToFileMergeSorting(tmpFilePath, fileQueue.poll(), fileQueue.poll());
                newPartsList.add(tmpFilePath);
            }
            else if(i == parts.length -1)
                newPartsList.add(fileQueue.poll());

            if(parts.length == 1)
                break;
            if(i == parts.length -1) {
                parts = newPartsList.toArray(new String[newPartsList.size()]);
                i = -1;
            }
        }

    }

    private static void fileToFileMergeSorting(String newFilename, String f1, String f2) {

        try(
            BufferedReader reader1 = new BufferedReader(new FileReader(f1));
            BufferedReader reader2 = new BufferedReader(new FileReader(f2));
            BufferedWriter writer = new BufferedWriter(new FileWriter(newFilename))) {

            String s1 = "";
            String s2 = "";
            boolean s1Read = true;
            boolean s2Read = true;

            while (reader1.ready() || reader2.ready()) {
                if(reader1.ready() && (s1Read || !reader2.ready()))
                    s1 = reader1.readLine();
                if(reader2.ready() && (s2Read || !reader1.ready()))
                    s2 = reader2.readLine();

                if(reader1.ready() && reader2.ready()) {
                    if (s1.compareTo(s2) < 0) {
                        writer.write(s1 + System.lineSeparator());
                        s1Read = true;
                        s2Read = false;
                    } else {
                        writer.write(s2 + System.lineSeparator());
                        s1Read = false;
                        s2Read = true;
                    }
                }
            }

        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

    private static FileReadResult readFile(Path path, long offset, long length)
            throws IOException {
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

    private static void writeStringsToFile(Path filepath, List<String> strings)
            throws IOException {

        Files.write(filepath, strings);
        /*FileWriter fileWriter = new FileWriter(filepath.toString());
        fileWriter.write(Arrays.toString(strings));
        fileWriter.close();*/
    }

    private static void mergeSortingToFile(Path filepath, String[] s1, String[] s2)
            throws IOException {
        if (s1 == null || s1.length == 0)
            if(s2 != null)
                writeStringsToFile(filepath, Arrays.asList(s2));
        if (s2 == null || s2.length == 0)
            writeStringsToFile(filepath, Arrays.asList(s1));

        long length = s1.length + s2.length;
        List<String> buffer = new ArrayList<>();

        int i = 0;
        int j = 0;

        for (int k = 0; k < length; k++) {

            if (i == s1.length) {
                buffer.add(s2[j]);
                j++;
            }
            else if (j == s2.length) {
                buffer.add(s1[i]);
                i++;
            }
            else if (s1[i].compareTo(s2[j]) < 0) {
                buffer.add(s1[i]);
                i++;
            }
            else {
                buffer.add(s2[j]);
                j++;
            }

            if(buffer.size() == 1000 || k == length-1) {
                writeStringsToFile(filepath, buffer);
                buffer.clear();
            }

        }

    }
}
