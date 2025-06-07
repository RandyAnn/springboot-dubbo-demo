package com.example.shared.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 通用分页结果类
 * @param <T> 数据类型
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> implements Serializable {
    private static final long serialVersionUID = 1L;

    private long total;       // 总记录数
    private List<T> records;  // 当前页数据
    private int current;      // 当前页码
    private int size;         // 页大小

    /**
     * 创建分页结果
     */
    public static <T> PageResult<T> of(List<T> records, long total, int current, int size) {
        return new PageResult<>(total, records, current, size);
    }
}
