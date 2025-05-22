package edu.scau.imagemanagementsystem.model;

/**
 * 封装批量重命名操作所需的参数。
 */
public class BatchRenameParams {
    private final String prefix; // 文件名前缀
    private final int startNumber; // 起始编号
    private final int numDigits; // 编号位数

    /**
     * 构造一个 BatchRenameParams 对象。
     *
     * @param prefix      文件名前缀
     * @param startNumber 起始编号
     * @param numDigits   编号位数，用于数字部分的零填充
     */
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