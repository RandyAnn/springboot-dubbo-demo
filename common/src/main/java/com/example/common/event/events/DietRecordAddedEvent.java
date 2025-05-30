package com.example.common.event.events;

import com.example.common.event.DomainEvent;

import java.time.LocalDate;

/**
 * 饮食记录添加事件
 * 当用户添加饮食记录时发布此事件，用于通知其他服务进行相应处理
 */
public class DietRecordAddedEvent extends DomainEvent {
    
    /**
     * 用户ID
     */
    private final Long userId;
    
    /**
     * 饮食记录ID
     */
    private final Long dietRecordId;
    
    /**
     * 记录日期
     */
    private final LocalDate recordDate;
    
    /**
     * 餐次类型
     */
    private final String mealType;
    
    /**
     * 构造函数
     * 
     * @param userId 用户ID
     * @param dietRecordId 饮食记录ID
     * @param recordDate 记录日期
     * @param mealType 餐次类型
     */
    public DietRecordAddedEvent(Long userId, Long dietRecordId, LocalDate recordDate, String mealType) {
        super(userId.toString(), 1L, "diet-service");
        this.userId = userId;
        this.dietRecordId = dietRecordId;
        this.recordDate = recordDate;
        this.mealType = mealType;
    }
    
    /**
     * 获取用户ID
     * 
     * @return 用户ID
     */
    public Long getUserId() {
        return userId;
    }
    
    /**
     * 获取饮食记录ID
     * 
     * @return 饮食记录ID
     */
    public Long getDietRecordId() {
        return dietRecordId;
    }
    
    /**
     * 获取记录日期
     * 
     * @return 记录日期
     */
    public LocalDate getRecordDate() {
        return recordDate;
    }
    
    /**
     * 获取餐次类型
     * 
     * @return 餐次类型
     */
    public String getMealType() {
        return mealType;
    }
    
    @Override
    public String toString() {
        return "DietRecordAddedEvent{" +
                "userId=" + userId +
                ", dietRecordId=" + dietRecordId +
                ", recordDate=" + recordDate +
                ", mealType='" + mealType + '\'' +
                ", eventId='" + getEventId() + '\'' +
                ", timestamp=" + getTimestamp() +
                '}';
    }
}
