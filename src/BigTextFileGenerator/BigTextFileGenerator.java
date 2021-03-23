package BigTextFileGenerator;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

public class BigTextFileGenerator {

    private static int minLineLength = 100;
    private static int maxLineLength = 100;
    private static long linesCount = 10000000;

    public static void main(String[] args) {

        String filepath = "_testdata/testfile2.txt";

        String alphaNumericString = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
                + "abcdefghijklmnopqrstuvxyz"
                + "0123456789";

        StringBuilder line = new StringBuilder();
        Random random = new Random();

        System.out.println("Start");
        long startTime = System.currentTimeMillis();

        try (FileWriter fileWriter = new FileWriter(filepath)) {
            int k = 0;
            for (int i = 0; i < linesCount; i++) {
                long lineLength = (long) ((Math.random() * (maxLineLength - minLineLength)) + minLineLength);
                //line.setLength(0);
                random
                        .ints(0, alphaNumericString.length())
                        .limit(lineLength)
                        .forEach(num -> line.append(alphaNumericString.charAt(num)));

                line.append(System.lineSeparator());
                k++;
                if (k == 100 || i == linesCount - 1) {
                    fileWriter.write(line.toString());
                    k = 0;
                    line.setLength(0);
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        long endTime = System.currentTimeMillis();

        System.out.println("Work time: " + (endTime - startTime) + " ms");

    }
}
