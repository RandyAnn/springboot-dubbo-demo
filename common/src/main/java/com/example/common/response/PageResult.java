package com.example.common.response;

import java.io.Serializable;
import java.util.List;

/**
 * 通用分页结果类
 * @param <T> 数据类型
 */
public class PageResult<T> implements Serializable {
    private static final long serialVersionUID = 1L;

    private long total;       // 总记录数
    private List<T> records;  // 当前页数据
    private int current;      // 当前页码
    private int size;         // 页大小

    public PageResult() {
    }

    public PageResult(long total, List<T> records, int current, int size) {
        this.total = total;
        this.records = records;
        this.current = current;
        this.size = size;
    }

    /**
     * 创建分页结果
     */
    public static <T> PageResult<T> of(List<T> records, long total, int current, int size) {
        return new PageResult<>(total, records, current, size);
    }

    // getter and setter
    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public List<T> getRecords() {
        return records;
    }

    public void setRecords(List<T> records) {
        this.records = records;
    }

    public int getCurrent() {
        return current;
    }

    public void setCurrent(int current) {
        this.current = current;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }
}
