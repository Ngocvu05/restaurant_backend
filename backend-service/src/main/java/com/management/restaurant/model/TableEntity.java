package com.management.restaurant.model;

import com.management.restaurant.common.TableStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "tables")
public class TableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "table_name")
    private String tableName;
    private int capacity;

    @Enumerated(EnumType.STRING)
    private TableStatus status;

    @Column(name = "description")
    private String description;
}
