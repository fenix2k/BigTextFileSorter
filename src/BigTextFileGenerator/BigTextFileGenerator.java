package BigTextFileGenerator;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

public class BigTextFileGenerator {

    private static int minLineLength = 1;
    private static int maxLineLength = 100;
    private static long linesCount = 40000000;

    public static void main(String[] args) {

        if (args.length > 1) {
            linesCount = Integer.parseInt(args[0]);
            maxLineLength = Integer.parseInt(args[1]);
        }
        if (linesCount <= 0) {
            System.out.println("Lines count must be > 0");
            return;
        }
        if (maxLineLength <= 0) {
            System.out.println("Max line length must be > 0");
            return;
        }

        String filepath = "bigfile.txt";

        String alphaNumericString = "abcdefghijklmnopqrstuvxyz"
                + "0123456789";

        StringBuilder line = new StringBuilder();
        Random random = new Random();

        System.out.println("Start");
        long startTime = System.currentTimeMillis();

        try (FileWriter fileWriter = new FileWriter(filepath)) {
            int k = 0;
            for (int i = 0; i < linesCount; i++) {
                long lineLength = (long) ((Math.random() * (maxLineLength - minLineLength)) + minLineLength);

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
