import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

public class BigTextFileGenerator {

    private static int minLineLength = 10;
    private static int maxLineLength = 10;
    private static long linesCount = 10;

    public static void main(String[] args) {

        String alphaNumericString = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
                + "abcdefghijklmnopqrstuvxyz"
                + "0123456789";

        StringBuilder line = new StringBuilder();
        Random random = new Random();

        System.out.println("Start");
        long startTime = System.currentTimeMillis();

        try (FileWriter fileWriter = new FileWriter("testfile4.txt")) {
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
                //fileWriter.write(line.toString());
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

        return;
    }
}
