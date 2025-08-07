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
@Document(indexName = "users")
@Setting(replicas = 0, shards = 1)
public class UserDocument {
    @Id
    private String id;

    @Field(type = FieldType.Long)
    private Long userId;

    @Field(type = FieldType.Keyword)
    private String username;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String fullName;

    @Field(type = FieldType.Keyword)
    private String email;

    @Field(type = FieldType.Keyword)
    private String phoneNumber;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String address;

    @Field(type = FieldType.Keyword)
    private String roleName;

    @Field(type = FieldType.Keyword)
    private String status;

    @Field(type = FieldType.Keyword)
    private String avatarUrl;

    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}
