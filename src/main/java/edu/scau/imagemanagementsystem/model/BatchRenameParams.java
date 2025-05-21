package edu.scau.imagemanagementsystem.model;

public class BatchRenameParams {
    private final String prefix;
    private final int startNumber;
    private final int numDigits;

    public BatchRenameParams(String prefix, int startNumber, int numDigits) {
        this.prefix = prefix;
        this.startNumber = startNumber;
        this.numDigits = numDigits;
    }

    public String getPrefix() {
        return prefix;
    }

    public int getStartNumber() {
        return startNumber;
    }

    public int getNumDigits() {
        return numDigits;
    }
}