package com.example.diet.event;

import com.example.shared.event.DomainEvent;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 饮食记录添加事件
 * 当用户添加饮食记录时发布此事件，用于通知其他服务进行相应处理
 */
@Getter
@NoArgsConstructor  // Jackson反序列化需要
public class DietRecordAddedEvent extends DomainEvent {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 饮食记录ID
     */
    private Long dietRecordId;

    /**
     * 记录日期
     */
    private LocalDate recordDate;

    /**
     * 餐次类型
     */
    private String mealType;

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
