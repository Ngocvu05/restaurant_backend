package com.management.search_service.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.indices.ExistsRequest;
import co.elastic.clients.elasticsearch.indices.IndexSettings;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.io.StringReader;

@Component
@RequiredArgsConstructor
@Slf4j
public class ElasticsearchIndexConfig {
    private final ElasticsearchClient elasticsearchClient;

    @EventListener(ApplicationReadyEvent.class)
    public void setupIndices() {
        createDishesIndexWithCustomAnalyzers();
    }

    private void createDishesIndexWithCustomAnalyzers() {
        String indexName = "dishes";

        try {
            // Check if index exists
            boolean exists = elasticsearchClient.indices()
                    .exists(ExistsRequest.of(e -> e.index(indexName)))
                    .value();

            if (!exists) {
                log.info("Creating index: {}", indexName);

                // Index settings with custom analyzers
                String settingsJson = """
                        {
                          "analysis": {
                            "analyzer": {
                              "autocomplete_analyzer": {
                                "type": "custom",
                                "tokenizer": "standard",
                                "filter": ["lowercase", "autocomplete_filter"]
                              },
                              "autocomplete_search_analyzer": {
                                "type": "custom",
                                "tokenizer": "standard",
                                "filter": ["lowercase"]
                              },
                              "vietnamese_analyzer": {
                                "type": "custom",
                                "tokenizer": "standard",
                                "filter": ["lowercase", "asciifolding"]
                              }
                            },
                            "filter": {
                              "autocomplete_filter": {
                                "type": "edge_ngram",
                                "min_gram": 2,
                                "max_gram": 10
                              }
                            }
                          },
                          "number_of_shards": 1,
                          "number_of_replicas": 1
                        }
                        """;

                // Mappings
                String mappingsJson = """
                        {
                          "properties": {
                            "dishId": {"type": "long"},
                            "name": {
                              "type": "text",
                              "analyzer": "autocomplete_analyzer",
                              "search_analyzer": "autocomplete_search_analyzer",
                              "fields": {
                                "keyword": {"type": "keyword"},
                                "standard": {
                                  "type": "text",
                                  "analyzer": "standard"
                                },
                                "vietnamese": {
                                  "type": "text",
                                  "analyzer": "vietnamese_analyzer"
                                }
                              }
                            },
                            "description": {
                              "type": "text",
                              "analyzer": "vietnamese_analyzer",
                              "fields": {
                                "standard": {
                                  "type": "text",
                                  "analyzer": "standard"
                                }
                              }
                            },
                            "price": {"type": "scaled_float", "scaling_factor": 100},
                            "isAvailable": {"type": "boolean"},
                            "category": {
                              "type": "text",
                              "analyzer": "standard",
                              "fields": {
                                "keyword": {"type": "keyword"}
                              }
                            },
                            "imageUrls": {"type": "keyword"},
                            "averageRating": {"type": "half_float"},
                            "totalReviews": {"type": "integer"},
                            "orderCount": {"type": "integer"},
                            "createdAt": {"type": "date"},
                            "updatedAt": {"type": "date"},
                            "suggest": {
                              "type": "completion",
                              "analyzer": "simple",
                              "preserve_separators": true,
                              "preserve_position_increments": true,
                              "max_input_length": 50
                            }
                          }
                        }
                        """;

                elasticsearchClient.indices().create(c -> c
                        .index(indexName)
                        .settings(IndexSettings.of(s -> s
                                .withJson(new StringReader(settingsJson))
                        ))
                        .mappings(m -> m
                                .withJson(new StringReader(mappingsJson))
                        )
                );

                log.info("Successfully created index: {}", indexName);
            } else {
                log.info("Index {} already exists", indexName);
            }
        } catch (Exception e) {
            log.error("Failed to create index {}: {}", indexName, e.getMessage(), e);
        }
    }

    /**
     * Create index for search analytics
     */
    public void createSearchAnalyticsIndex() {
        String indexName = "search_analytics";

        try {
            boolean exists = elasticsearchClient.indices()
                    .exists(ExistsRequest.of(e -> e.index(indexName)))
                    .value();

            if (!exists) {
                String mappingsJson = """
                        {
                          "properties": {
                            "keyword": {
                              "type": "keyword"
                            },
                            "searchCount": {
                              "type": "long"
                            },
                            "resultCount": {
                              "type": "long"
                            },
                            "timestamp": {
                              "type": "date"
                            },
                            "userId": {
                              "type": "keyword"
                            },
                            "clickedDishIds": {
                              "type": "long"
                            },
                            "sessionId": {
                              "type": "keyword"
                            }
                          }
                        }
                        """;

                elasticsearchClient.indices().create(c -> c
                        .index(indexName)
                        .mappings(m -> m
                                .withJson(new StringReader(mappingsJson))
                        )
                );

                log.info("Successfully created search analytics index");
            }
        } catch (Exception e) {
            log.error("Failed to create search analytics index: {}", e.getMessage(), e);
        }
    }
}