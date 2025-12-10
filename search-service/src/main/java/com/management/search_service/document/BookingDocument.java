package com.management.search_service.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Document(indexName = "bookings")
@Setting(settingPath = "elasticsearch-settings.json")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingDocument {
    @Id
    private String id;

    @Field(type = FieldType.Long)
    private Long bookingId;

    @Field(type = FieldType.Long)
    private Long userId;

    @Field(type = FieldType.Keyword)
    private String username;

    @Field(type = FieldType.Long)
    private Long tableId;

    @Field(type = FieldType.Text)
    private String tableName;

    @Field(type = FieldType.Date, format = DateFormat.date_time)
    private LocalDateTime bookingTime;

    @Field(type = FieldType.Integer)
    private Integer numberOfGuests;

    @Field(type = FieldType.Keyword)
    private String status;

    @Field(type = FieldType.Text)
    private String note;

    @Field(type = FieldType.Double)
    private BigDecimal totalAmount;

    @Field(type = FieldType.Long)
    private Long version;

    @Field(type = FieldType.Date, format = DateFormat.date_time)
    private LocalDateTime createdAt;

    @Field(type = FieldType.Date, format = DateFormat.date_time)
    private LocalDateTime updatedAt;
}