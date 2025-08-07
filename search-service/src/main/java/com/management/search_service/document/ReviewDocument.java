package com.management.search_service.document;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(indexName = "reviews")
@Setting(replicas = 0, shards = 1)
public class ReviewDocument {
    @Id
    private String id;

    @Field(type = FieldType.Long)
    private Long reviewId;

    @Field(type = FieldType.Long)
    private Long dishId;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String customerName;

    @Field(type = FieldType.Keyword)
    private String customerEmail;

    @Field(type = FieldType.Keyword)
    private String customerAvatar;

    @Field(type = FieldType.Integer)
    private Integer rating;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String comment;

    @Field(type = FieldType.Boolean)
    private Boolean isActive;

    @Field(type = FieldType.Boolean)
    private Boolean isVerified;

    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}
