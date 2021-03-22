package BigTextFileSorter;

public class FileReadResult {
    private final String[] strings;
    private final long offset;
    private final boolean isEOF;

    public FileReadResult(String[] strings, long offset, boolean isEOF) {
        this.strings = strings;
        this.offset = offset;
        this.isEOF = isEOF;
    }

    public String[] getStrings() {
        return strings;
    }

    public long getOffset() {
        return offset;
    }

    public boolean isEOF() {
        return isEOF;
    }
}
