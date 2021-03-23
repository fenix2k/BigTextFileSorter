package BigTextFileSorter;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class BigTextFileSorter {

    public static long MAX_PART_SIZE = 1024*1024*1024/4;
    public static String tmdDir = "_testdata/";
    public static String tmpFileNameTemplate = Paths.get(tmdDir, "tmpfile.tmp").toString();

    public static void main(String[] args) throws IOException {

        String filename = tmdDir + "testfile2.txt";
        String outputFilename = tmdDir + "testfile-out2.txt";

        System.out.println("Start sorting file " + filename);

        long fileSize = Files.size(Paths.get(filename));
        int partsCount = 6;
        long maxPartSize = fileSize / partsCount;

        List<String> fileList = splitBigTextFileAndSort(filename, MAX_PART_SIZE);
        //List<String> fileList = splitBigTextFileAndSort(filename, maxPartSize);

        String outputfile = mergeFilesWithSorting(fileList, outputFilename);

        System.out.println("Finished! Result file is " + outputfile);
    }

    /**
     * Объеденяет несколько отсортированных текстовых файлов в один.
     *
     * @param fileList   список файлов
     * @param outputFile путь к итоговому файлу
     * @return путь к итоговому файлу
     * @throws IOException
     */
    private static String mergeFilesWithSorting(List<String> fileList, String outputFile) throws IOException {

        String[] parts = fileList.toArray(new String[fileList.size()]);
        Queue<String> fileQueue = new ArrayDeque<>();
        List<String> newPartsList = new ArrayList<>();
        int counter = 0;

        System.out.println("Start merge files");

        for (int i = 0; i < parts.length; i++) {
            if (parts.length == 1)
                break;

            fileQueue.add(parts[i]);

            if (fileQueue.size() == 2) {
                String tmpFilePath = tmpFileNameTemplate + "0" + counter++;
                String f1 = fileQueue.poll();
                String f2 = fileQueue.poll();
                System.out.println("Merging files " + f1 + " and " + f2 + " to " + tmpFilePath);
                fileToFileMergeSorting(tmpFilePath, f1, f2);
                System.out.println("Merging files completed");
                Files.delete(Paths.get(f1));
                System.out.println("Delete file " + f1);
                Files.delete(Paths.get(f2));
                System.out.println("Delete file " + f2);
                newPartsList.add(tmpFilePath);
            } else if (i == parts.length - 1)
                newPartsList.add(fileQueue.poll());

            if (i == parts.length - 1) {
                parts = newPartsList.toArray(new String[newPartsList.size()]);
                newPartsList = new ArrayList<>();
                i = -1;
            }
        }
        if(parts.length == 0)
            return "";

        Path file = Paths.get(outputFile);
        if(Files.exists(file))
            Files.delete(file);
        System.out.println("Finish merging files");
        System.out.println("Rename result file to " + file);
        return Files.move(Paths.get(parts[0]), file).toString();
    }

    /**
     * Разбивает текстовый файл на parts частей и сортирует их содержимое
     *
     * @param filepath путь к текстовому файлу
     * @param maxPartSize максимальный размер части
     * @return список файлов
     * @throws IOException
     */
    private static List<String> splitBigTextFileAndSort(String filepath, long maxPartSize) throws IOException {

        long partSize = maxPartSize/2;

        List<String> fileList = new ArrayList<>();
        Queue<String[]> queue = new ArrayDeque<>();

        long currentOffset = 0;
        int counter = 0;

        System.out.println("Start to splitting big file " + filepath);
        System.out.println("Max size part of file is " + (partSize/1024/1024) * 2 + " Mb");

        while (true) {

            FileReadResult fileReadResult = readFile(filepath, currentOffset, partSize);
            Arrays.parallelSort(fileReadResult.getStrings());
            currentOffset = fileReadResult.getOffset();
            queue.add(fileReadResult.getStrings());

            if (queue.size() >= 2) {
                Path tmpFilePath = Paths.get(tmpFileNameTemplate + counter++);
                fileList.add(tmpFilePath.toString());
                arrayMergeSortingToFile(tmpFilePath.toString(), queue.poll(), queue.poll());
                System.out.println("Temporal file created " + tmpFilePath.toString());
            } else if (fileReadResult.isEOF()) {
                Path tmpFilePath = Paths.get(tmpFileNameTemplate + counter++);
                fileList.add(tmpFilePath.toString());
                Files.write(tmpFilePath, Arrays.asList(queue.poll()));
                System.out.println("Temporal file created " + tmpFilePath.toString());
            }

            if (fileReadResult.isEOF()) break;
        }
        System.out.println("File is splitted to " + fileList.size() + " parts");
        System.out.println("File list: " + fileList);
        return fileList;
    }

    /**
     * Получает 2 массива отсортированных строк и объеденяет их методом слияния.
     * Результат пишется в файл.
     *
     * @param filepath путы к целевому файлу
     * @param s1       массив тестовых строк
     * @param s2       массив тестовых строк
     * @throws IOException
     */
    private static void arrayMergeSortingToFile(String filepath, String[] s1, String[] s2)
            throws IOException {

        if (s1 == null || s1.length == 0)
            if (s2 != null)
                Files.write(Paths.get(filepath), Arrays.asList(s2));
        if (s2 == null || s2.length == 0)
            Files.write(Paths.get(filepath), Arrays.asList(s1));

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filepath))) {
            long length = s1.length + s2.length;

            int i = 0;
            int j = 0;

            for (int k = 0; k < length; k++) {

                if (i == s1.length) {
                    writer.write(s2[j] + System.lineSeparator());
                    j++;
                } else if (j == s2.length) {
                    writer.write(s1[i] + System.lineSeparator());
                    i++;
                } else if (s1[i].compareTo(s2[j]) < 0) {
                    writer.write(s1[i] + System.lineSeparator());
                    i++;
                } else {
                    writer.write(s2[j] + System.lineSeparator());
                    j++;
                }
            }
        }
    }

    /**
     * Принимает в качестве параметров путь к 2-м отсортированным текстовым файлам,
     * читает их построчно и сортирует строки методом слияния. Результат каждой итерации пишется в файл.
     *
     * @param newFilename путы к целевому файлу
     * @param f1          путь к первому отсортированному текстовому файлу
     * @param f2          путь ко второму отсортированному текстовому файлу
     */
    private static void fileToFileMergeSorting(String newFilename, String f1, String f2) {

        try (
                BufferedReader reader1 = new BufferedReader(new FileReader(f1));
                BufferedReader reader2 = new BufferedReader(new FileReader(f2));
                BufferedWriter writer = new BufferedWriter(new FileWriter(newFilename))
        ) {

            String s1 = null;
            String s2 = null;

            if (reader1.ready())
                s1 = reader1.readLine();
            if (reader1.ready())
                s2 = reader2.readLine();

            while (reader1.ready() || reader2.ready() || s1 != null || s2 != null) {

                if (reader1.ready() && s1 == null)
                    s1 = reader1.readLine();
                else if (reader2.ready() && s2 == null)
                    s2 = reader2.readLine();


                if (s2 == null || (s1 != null && s1.compareTo(s2) < 0)) {
                    writer.write(s1 + System.lineSeparator());
                    s1 = null;
                } else {
                    writer.write(s2 + System.lineSeparator());
                    s2 = null;
                }

            }

        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

    /**
     * Читает length байт текстового файла начиная с offset и преобразует в массив строк.
     *
     * @param filepath   путь к текстовому файлу
     * @param offset откуда начинать читать в байтах
     * @param length сколько читать в байтах
     * @return FileReadResult. Обёртка содержить массив строк, текущее смещение, признак конца файла.
     */
    private static FileReadResult readFile(String filepath, long offset, long length) {

        try (RandomAccessFile file = new RandomAccessFile(filepath, "r");) {
            long lastOffset;
            boolean isEOF = false;

            file.seek(offset);

            byte[] buffer = new byte[(int) length + 1];
            file.read(buffer, 0, (int) length);

            String[] stringList = new String(buffer).split(System.lineSeparator());

            if (offset + length < file.length()) {
                file.seek(offset + length - stringList[stringList.length - 1].getBytes().length + 1);
                stringList[stringList.length - 1] = file.readLine();
            }
            lastOffset = file.getFilePointer();
            if (lastOffset == file.length())
                isEOF = true;

            return new FileReadResult(stringList, lastOffset, isEOF);

        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return null;
    }


}
