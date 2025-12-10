package com.management.search_service.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;

import java.math.BigDecimal;
import java.util.List;

@Document(indexName = "restaurants")
@Setting(settingPath = "elasticsearch-settings.json")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RestaurantDocument {
    @Id
    private String id;

    @Field(type = FieldType.Text)
    private String name;

    @Field(type = FieldType.Text)
    private String address;

    @GeoPointField
    private GeoPoint location; // {lat: 10.762622, lon: 106.660172}

    @Field(type = FieldType.Keyword)
    private String district;

    @Field(type = FieldType.Keyword)
    private String city;

    // Distance from user (calculated, not stored)
    private Double distanceFromUser;

    // Nested dishes
    @Field(type = FieldType.Nested)
    private List<DishInfo> dishes;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GeoPoint {
        private double lat;
        private double lon;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DishInfo {
        private String name;
        private String description;
        private BigDecimal price;
    }
}
