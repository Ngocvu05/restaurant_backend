#     Control + Shift + V to open preview mode
# 🎓 Elasticsearch Deep Dive - Giải thích chi tiết

## 📚 Table of Contents
1. [Elasticsearch Core Concepts](#1-core-concepts)
2. [Index & Mapping Configuration](#2-index-mapping)
3. [Analyzers & Tokenizers](#3-analyzers)
4. [Query Types Deep Dive](#4-query-types)
5. [Scoring & Relevance](#5-scoring)
6. [Aggregations Explained](#6-aggregations)
7. [Workflow Complete Examples](#7-workflow)

---

## 1. Elasticsearch Core Concepts {#1-core-concepts}

### 1.1 Document-Oriented Database

Elasticsearch lưu trữ data dưới dạng **JSON documents**.

```json
{
  "dishId": 1,
  "name": "Phở Bò",
  "description": "Traditional Vietnamese beef noodle soup",
  "price": 45000,
  "category": "Noodles",
  "averageRating": 4.5
}
```

**So sánh với SQL:**
- Document = Row (hàng)
- Field = Column (cột)
- Index = Database Table (bảng)
- Type = ~~Table~~ (deprecated từ ES 7.x)

### 1.2 Inverted Index - Trái tim của Full-Text Search

**Cách SQL search:**
```sql
SELECT * FROM dishes WHERE name LIKE '%pho%';
-- Scan toàn bộ table → O(n) → CHẬM
```

**Cách Elasticsearch search:**

Elasticsearch tạo **Inverted Index** khi index document:

```
Original Text: "Phở Bò Hà Nội"

After Tokenization:
"phở" → [doc1, doc5, doc8]
"bò"  → [doc1, doc3, doc8]
"hà"  → [doc1, doc2]
"nội" → [doc1, doc2, doc9]

Search "phở bò" → Intersection[doc1, doc8] → O(1) → NHANH
```

**Workflow:**
1. **Indexing time**: Text → Analyzer → Tokens → Inverted Index
2. **Search time**: Query → Same Analyzer → Tokens → Lookup Index → Results

---

## 2. Index & Mapping Configuration {#2-index-mapping}

### 2.1 Index Settings - Cấu hình cơ bản

```json
{
  "settings": {
    "number_of_shards": 1,      // Số phân mảnh (chia nhỏ data)
    "number_of_replicas": 1,    // Số bản sao (high availability)
    "analysis": {
      "analyzer": { ... }       // Custom analyzers
    }
  }
}
```

**Giải thích:**

#### Shards (Phân mảnh)
```
Index "dishes" có 1 million documents

1 Shard:  [1M documents] → Chậm khi search
3 Shards: [333K] [333K] [334K] → Nhanh hơn (parallel search)

Rule of thumb: 1 shard ≈ 20-40GB data
```

#### Replicas (Bản sao)
```
Primary Shard: [Data]
Replica 1:     [Data Copy] → Node 2
Replica 2:     [Data Copy] → Node 3

Lợi ích:
- High availability (node die vẫn ok)
- Read scaling (3 copies → 3x read throughput)
```

### 2.2 Mapping - Schema Definition

```json
{
  "mappings": {
    "properties": {
      "name": {
        "type": "text",              // Full-text search
        "analyzer": "standard",       // Cách phân tích text
        "fields": {
          "keyword": {               // Multi-field
            "type": "keyword"        // Exact match
          }
        }
      },
      "price": {
        "type": "scaled_float",      // Số thực tối ưu
        "scaling_factor": 100        // Lưu 45.50 thành 4550
      }
    }
  }
}
```

**Field Types chi tiết:**

| Type | Use Case | Example |
|------|----------|---------|
| `text` | Full-text search, analyzed | "Phở Bò" → ["phở", "bò"] |
| `keyword` | Exact match, sorting, aggregations | "Main Course" (không phân tích) |
| `integer` | Số nguyên | 123 |
| `long` | Số nguyên lớn | 9223372036854775807 |
| `float` | Số thực | 3.14159 |
| `scaled_float` | Số thực tối ưu | 45.50 → 4550 (x100) |
| `date` | Ngày tháng | "2025-10-20T14:30:00" |
| `boolean` | True/False | true |
| `geo_point` | Tọa độ địa lý | {lat: 10.76, lon: 106.66} |

---

## 3. Analyzers & Tokenizers {#3-analyzers}

### 3.1 Analyzer Workflow

```
Input Text: "Phở Bò Hà Nội 45k"
    ↓
Character Filters (optional)
    ↓ "Phở Bò Hà Nội 45k" (remove HTML, map characters)
    ↓
Tokenizer (required)
    ↓ ["Phở", "Bò", "Hà", "Nội", "45k"]
    ↓
Token Filters (optional)
    ↓ lowercase: ["phở", "bò", "hà", "nội", "45k"]
    ↓ asciifolding: ["pho", "bo", "ha", "noi", "45k"]
    ↓ remove_digits: ["pho", "bo", "ha", "noi"]
    ↓
Final Tokens: ["pho", "bo", "ha", "noi"]
```

### 3.2 Built-in Analyzers

#### Standard Analyzer (Default)
```json
Input:  "Phở Bò-Hà Nội"
Output: ["phở", "bò", "hà", "nội"]

Components:
- Tokenizer: standard (split by whitespace, punctuation)
- Filters: lowercase
```

#### Simple Analyzer
```json
Input:  "Phở123Bò"
Output: ["phở", "bò"]

Components:
- Tokenizer: lowercase (split on non-letter)
```

#### Whitespace Analyzer
```json
Input:  "Phở Bò"
Output: ["Phở", "Bò"]

Components:
- Tokenizer: whitespace (split only on whitespace)
```

### 3.3 Custom Analyzer - Autocomplete

```json
{
  "analysis": {
    "analyzer": {
      "autocomplete_analyzer": {
        "type": "custom",
        "tokenizer": "standard",
        "filter": ["lowercase", "autocomplete_filter"]
      }
    },
    "filter": {
      "autocomplete_filter": {
        "type": "edge_ngram",
        "min_gram": 2,
        "max_gram": 10
      }
    }
  }
}
```

**Edge N-gram giải thích:**

```
Input: "phở"

min_gram=2, max_gram=10

Tokens generated:
"ph"   (2 chars)
"phở"  (3 chars)

When user types "ph" → match "phở"
```

**Ví dụ thực tế:**
```
Index time:
"Phở Bò" → lowercase → "phở bò"
         → edge_ngram → ["ph", "phở", "phở ", "phở b", "phở bò",
                         "bò"]

Search time (user types "phở b"):
"phở b" → lowercase → "phở b"
        → Match với token "phở b" trong index
        → Return "Phở Bò"
```

### 3.4 Vietnamese Analyzer

```json
{
  "analyzer": {
    "vietnamese_analyzer": {
      "type": "custom",
      "tokenizer": "standard",
      "filter": ["lowercase", "asciifolding"]
    }
  }
}
```

**ASCII Folding:**
```
Input:  "Phở Bò"
Output: "pho bo"

Purpose: 
- User gõ "pho" vẫn tìm được "Phở"
- User gõ "bo" vẫn tìm được "Bò"
```

---

## 4. Query Types Deep Dive {#4-query-types}

### 4.1 Match Query - Cơ bản nhất

```json
{
  "query": {
    "match": {
      "name": "phở bò"
    }
  }
}
```

**Workflow:**
1. Analyze query: "phở bò" → ["phở", "bò"]
2. Search inverted index: OR logic
3. Documents matching "phở" OR "bò"
4. Score by relevance

**Scoring:**
```
Doc1: "Phở Bò Hà Nội"     → Contains both → Score: 2.5
Doc2: "Phở Gà"             → Contains "phở" → Score: 1.2
Doc3: "Bún Bò Huế"         → Contains "bò"  → Score: 1.0
```

### 4.2 Multi-Match Query - Search nhiều fields

```java
.multiMatch(m -> m
        .query("phở")
    .fields("name^3", "description^2", "category")
)
```

**Field Boosting:**
```
"name^3"        → Match trong name × 3
"description^2" → Match trong description × 2
"category"      → Match trong category × 1

Example:
Doc1: name="Phở Bò"               → Score: 3.0 × 3 = 9.0
Doc2: description="món phở ngon"  → Score: 2.0 × 2 = 4.0
Doc3: category="Noodles"          → Score: 0
```

**Why boost?**
- `name` quan trọng nhất → boost x3
- `description` quan trọng vừa → boost x2
- `category` ít quan trọng → no boost

### 4.3 Fuzzy Search - Chịu lỗi chính tả

```java
.fuzziness("AUTO")      // Tự động điều chỉnh
.prefixLength(2)        // 2 ký tự đầu phải đúng
.maxExpansions(50)      // Giới hạn biến thể
```

**Levenshtein Distance:**
```
"pho" vs "phở"  → Distance = 1 (thay 'o' thành 'ở')
"pho" vs "photo" → Distance = 2 (thêm 't', 'o')

Fuzziness = 1: chấp nhận 1 sai khác
Fuzziness = 2: chấp nhận 2 sai khác
```

**Fuzziness "AUTO":**
```
Query length 1-2:   fuzziness = 0 (không chịu lỗi)
Query length 3-5:   fuzziness = 1
Query length 6+:    fuzziness = 2
```

**prefixLength:**
```
prefixLength = 2

"pho"  → "ph" phải đúng → match "phở", "phone"
"pho"  → NOT match "apo" (vì "ap" ≠ "ph")
```

**maxExpansions:**
```
Giới hạn số biến thể để tránh query chậm

"test" với fuzziness=2 có thể match:
- test, text, best, rest, fest, ... (hàng trăm từ)

maxExpansions=50 → chỉ check 50 từ đầu tiên
```

### 4.4 Bool Query - Kết hợp nhiều điều kiện

```java
.bool(b -> b
        .must(...)      // AND logic, contribute to score
    .filter(...)    // AND logic, NO scoring
    .should(...)    // OR logic, contribute to score
    .mustNot(...)   // NOT logic, exclude docs
)
```

**Ví dụ thực tế:**

```java
.bool(b -> b
        // MUST: Phải chứa "phở"
        .must(m -> m.match(mm -> mm.field("name").query("phở")))

        // FILTER: Giá 30k-50k (không ảnh hưởng score)
        .filter(f -> f.range(r -> r
        .field("price")
        .gte(JsonData.of(30000))
        .lte(JsonData.of(50000))
        ))

        // SHOULD: Bonus nếu có "bò" hoặc "gà"
        .should(s -> s.match(m -> m.field("description").query("bò")))
        .should(s -> s.match(m -> m.field("description").query("gà")))

        // MUST_NOT: Loại bỏ món hết hàng
        .mustNot(mn -> mn.term(t -> t
        .field("isAvailable")
        .value(false)
    ))
            )
```

**Scoring logic:**
```
Final Score = must_score + should_score

Doc1: "Phở Bò" (45k, available)
- must (phở):   +2.0
- filter (price): matched (no score)
- should (bò):  +1.0
- Total: 3.0

Doc2: "Phở Gà" (48k, available)
- must (phở):   +2.0
- filter (price): matched
- should (gà):  +0.8
- Total: 2.8

Doc3: "Phở Hải Sản" (60k, available)
- must (phở):   +2.0
- filter (price): NOT matched → EXCLUDED

Doc4: "Phở Đặc Biệt" (40k, unavailable)
- mustNot: → EXCLUDED
```

### 4.5 Prefix Query - Autocomplete

```java
.prefix(p -> p
        .field("name")
    .value("ph")
)
```

**So sánh các query types cho autocomplete:**

```
User types: "ph"

1. Prefix Query:
   - Match: "Phở", "Phone", "Photo"
   - Fast: yes
   - Typo tolerant: no

2. Match Phrase Prefix:
   - Match: "Phở Bò", "Phone Book"
   - Fast: medium
   - Phrase aware: yes

3. Completion Suggester (tốt nhất):
   - Match: từ suggest field
   - Fast: very fast (in-memory)
   - Purpose-built: yes
```

### 4.6 Range Query - Lọc theo khoảng

```java
.range(r -> r
        .field("price")
    .gte(JsonData.of(30000))  // Greater than or equal
        .lt(JsonData.of(50000))   // Less than
        )
```

**Operators:**
```
gte:  >=  (greater than or equal)
gt:   >   (greater than)
lte:  <=  (less than or equal)
lt:   <   (less than)

Example:
price >= 30000 AND price < 50000
→ 30000, 35000, 49999 ✓
→ 29999, 50000 ✗
```

---

## 5. Scoring & Relevance {#5-scoring}

### 5.1 TF-IDF Algorithm (Classic)

**TF (Term Frequency):**
```
"phở" xuất hiện bao nhiêu lần trong document?

Doc1: "Phở Bò Phở Đặc Biệt"  → TF(phở) = 2
Doc2: "Phở Gà"                → TF(phở) = 1
```

**IDF (Inverse Document Frequency):**
```
"phở" xuất hiện trong bao nhiêu documents?

Total docs: 100
Docs containing "phở": 20

IDF = log(100/20) = 0.7

Rare terms → Higher IDF → More important
```

**Final Score:**
```
Score = TF × IDF × field_boost

Doc1: "Phở Bò" in name field (boost=3)
TF = 1, IDF = 0.7
Score = 1 × 0.7 × 3 = 2.1
```

### 5.2 BM25 Algorithm (Modern, Default)

BM25 cải tiến TF-IDF:

```
score = IDF × (TF × (k1 + 1)) / (TF + k1 × (1 - b + b × (fieldLength / avgFieldLength)))

Where:
- k1 = 1.2 (saturation parameter)
- b = 0.75 (length normalization)
```

**Khác biệt:**
```
TF-IDF: "phở" xuất hiện 10 lần → score tăng gấp 10
BM25:   "phở" xuất hiện 10 lần → score tăng ít hơn (saturation)

Why? Để tránh spam (nhồi từ khóa)
```

### 5.3 Function Score - Custom Scoring

```java
.functionScore(fs -> fs
        .query(baseQuery)  // Base relevance score
    .functions(...)    // Modify score
    .scoreMode(Sum)    // How to combine functions
    .boostMode(Multiply) // How to combine with base
)
```

**Workflow:**
```
1. Base Query Score:
   "phở bò" match → Score = 2.5

2. Apply Functions:
   Function 1 (rating):     +1.5
   Function 2 (order count): +0.8
   Function 3 (freshness):  +0.2
   
3. Combine Functions (scoreMode=Sum):
   function_score = 1.5 + 0.8 + 0.2 = 2.5

4. Combine with Base (boostMode=Multiply):
   final_score = 2.5 × 2.5 = 6.25
```

**Score Modes:**
```
multiply: score = func1 × func2 × func3
sum:      score = func1 + func2 + func3
avg:      score = (func1 + func2 + func3) / 3
max:      score = max(func1, func2, func3)
min:      score = min(func1, func2, func3)
```

**Boost Modes:**
```
multiply: final = base_score × function_score
replace:  final = function_score (ignore base)
sum:      final = base_score + function_score
avg:      final = (base_score + function_score) / 2
max:      final = max(base_score, function_score)
min:      final = min(base_score, function_score)
```

### 5.4 Field Value Factor

```java
.fieldValueFactor(fvf -> fvf
        .field("averageRating")  // Use this field value
    .factor(0.5)             // Multiply by 0.5
    .modifier(Log1p)         // Apply log(1 + value)
    .missing(3.0)            // Default if field missing
)
```

**Modifiers explained:**

```
Original value: orderCount = 100

none:   100
log:    log(100) = 2.0
log1p:  log(1 + 100) = 2.0 (safer, avoid log(0))
log2p:  log(2 + 100) = 2.0
ln:     ln(100) = 4.6
ln1p:   ln(1 + 100) = 4.6
ln2p:   ln(2 + 100) = 4.6
square: 100² = 10000
sqrt:   √100 = 10
reciprocal: 1/100 = 0.01
```

**Why log?**
```
orderCount differences:
10 vs 20:    Gap = 10
100 vs 110:  Gap = 10 (same numerical gap)

After log1p:
log(11) vs log(21):   0.34 difference
log(101) vs log(111): 0.09 difference

Result: Diminishing returns for very popular items
```

### 5.5 Decay Functions - Gaussian, Exponential, Linear

**Gauss Decay - Freshness boost:**

```java
.gauss(g -> g
        .field("updatedAt")
    .origin(FieldValue.of("now"))  // Optimal point
        .scale("30d")                  // Half-life
    .decay(0.5)                    // Decay factor
)
```

**How it works:**

```
Graph:
Score
  1.0 |●
      |  ●
  0.5 |    ● (decay point at scale)
      |      ●●
  0.0 |________●●●________
      0   30d   60d   90d
         (scale)

Document updated:
- Today:    score = 1.0    (100%)
- 30d ago:  score = 0.5    (50%)
- 60d ago:  score = 0.125  (12.5%)
```

**Parameters:**
```
origin: Điểm tối ưu (score = 1.0)
scale:  Khoảng cách để score giảm xuống decay
decay:  Mức giảm tại scale distance

offset: Vùng không giảm điểm (optional)
```

**Decay function shapes:**

```
Gaussian (smooth):
1.0 |●---__
    |      ---__
0.5 |          ●--__
    |              ---●

Exponential (aggressive):
1.0 |●__
    |   ---__
0.5 |       ●--__
    |           ---●

Linear (steady):
1.0 |●----
    |     ----
0.5 |         ●----
    |             ----●
```

---

## 6. Aggregations Explained {#6-aggregations}

### 6.1 Terms Aggregation - Group by

```java
.aggregations("categories", a -> a
        .terms(t -> t
        .field("category.keyword")
        .size(20)
    )
            )
```

**SQL equivalent:**
```sql
SELECT category, COUNT(*) as count
FROM dishes
GROUP BY category
ORDER BY count DESC
LIMIT 20;
```

**Result:**
```json
{
  "aggregations": {
    "categories": {
      "buckets": [
        {"key": "Noodles", "doc_count": 45},
        {"key": "Rice", "doc_count": 32},
        {"key": "Appetizer", "doc_count": 18}
      ]
    }
  }
}
```

### 6.2 Range Aggregation - Bucketing

```java
.aggregations("price_ranges", a -> a
        .range(r -> r
        .field("price")
        .ranges(range -> range.to("50000"))           // < 50k
        .ranges(range -> range.from("50000").to("100000")) // 50k-100k
        .ranges(range -> range.from("100000"))        // > 100k
        )
        )
```

**Result:**
```json
{
  "price_ranges": {
    "buckets": [
      {"key": "*-50000", "doc_count": 25},
      {"key": "50000-100000", "doc_count": 40},
      {"key": "100000-*", "doc_count": 15}
    ]
  }
}
```

### 6.3 Metric Aggregations

```java
// Average
.aggregations("avg_rating", a -> a
        .avg(avg -> avg.field("averageRating"))
        )

// Sum
        .aggregations("total_revenue", a -> a
        .sum(sum -> sum.field("price"))
        )

// Min/Max
        .aggregations("price_stats", a -> a
        .stats(stats -> stats.field("price"))
        )
```

**Stats aggregation returns:**
```json
{
  "price_stats": {
    "count": 80,
    "min": 15000,
    "max": 250000,
    "avg": 67500,
    "sum": 5400000
  }
}
```

### 6.4 Sub-Aggregations - Nested

```java
.aggregations("categories", a -> a
        .terms(t -> t.field("category.keyword"))
        .aggregations("avg_price_per_category", sub -> sub
        .avg(avg -> avg.field("price"))
        )
        )
```

**Result:**
```json
{
  "categories": {
    "buckets": [
      {
        "key": "Noodles",
        "doc_count": 45,
        "avg_price_per_category": {
          "value": 45000
        }
      },
      {
        "key": "Rice",
        "doc_count": 32,
        "avg_price_per_category": {
          "value": 38000
        }
      }
    ]
  }
}
```

---

## 7. Complete Workflow Examples {#7-workflow}

### 7.1 Fuzzy Search Workflow

**Step 1: User input**
```
User types: "pho bo"
```

**Step 2: Build query**
```java
elasticsearchClient.search(s -> s
        .index("dishes")
    .query(q -> q
        .multiMatch(m -> m
        .query("pho bo")
            .fields("name^3", "description^2")
            .fuzziness("AUTO")  // Allow typos
        )
                )
                )
```

**Step 3: Elasticsearch processing**
```
1. Analyze query:
   "pho bo" → ["pho", "bo"]

2. Apply fuzziness:
   "pho" → match "phở", "pho", "photo" (distance ≤ 1)
   "bo"  → match "bò", "bo", "box" (distance ≤ 1)

3. Search inverted index:
   Documents containing: "phở" OR "bò" OR variations

4. Score documents:
   - TF-IDF/BM25 base score
   - Field boost (name^3, description^2)
   - Proximity bonus (if terms near each other)

5. Sort by score (descending)
```

**Step 4: Return results**
```json
[
  {
    "dish": {
      "name": "Phở Bò",
      "description": "...",
      "price": 45000
    },
    "score": 8.5,
    "highlights": {
      "name": ["<em>Phở</em> <em>Bò</em>"]
    }
  }
]
```

### 7.2 Custom Scoring Workflow

**Goal:** Promote popular, high-rated, fresh dishes

**Query:**
```java
.functionScore(fs -> fs
        // Base query: text match
        .query(multiMatchQuery("noodles"))

        // Function 1: Boost by rating
        .functions(fn -> fn
        .fieldValueFactor(fvf -> fvf
        .field("averageRating")
            .factor(0.5)
            .modifier(Log1p)
        )
                )

                // Function 2: Boost by popularity
                .functions(fn -> fn
        .fieldValueFactor(fvf -> fvf
        .field("orderCount")
            .factor(0.1)
            .modifier(Log1p)
        )
                )

                // Function 3: Boost by freshness
                .functions(fn -> fn
        .gauss(g -> g
        .field("updatedAt")
            .origin(FieldValue.of("now"))
        .scale("30d")
            .decay(0.5)
        )
                )

                .scoreMode(Sum)       // Add all function scores
    .boostMode(Multiply)  // Multiply with base score
)
```

**Scoring calculation:**

```
Document: "Phở Bò"
- Text match score: 2.0

Function 1 (rating = 4.5):
  log1p(4.5) × 0.5 = 0.86

Function 2 (orderCount = 150):
  log1p(150) × 0.1 = 0.52

Function 3 (updated 10 days ago):
  Gauss decay = 0.95

Total function score:
  Sum: 0.86 + 0.52 + 0.95 = 2.33

Final score:
  Multiply: 2.0 × 2.33 = 4.66
```

**Comparison:**

```
Without custom scoring:
  Doc1: "Phở Bò" (rating 4.5, 150 orders)  → 2.0
  Doc2: "Mì Ý" (rating 3.0, 5 orders)      → 2.1

With custom scoring:
  Doc1: "Phở Bò"  → 4.66 ✓ (promoted)
  Doc2: "Mì Ý"    → 2.5
```

### 7.3 Aggregations Workflow

**Goal:** Faceted search với filters

**Query:**
```java
.query(boolQuery()
    .must(matchQuery("chicken"))
        .filter(rangeQuery("price").gte(30000).lte(100000))
        )
        .aggregations("categories", termsAgg("category.keyword"))
        .aggregations("price_ranges", rangeAgg("price")
    .range(to(50000))
        .range(from(50000).to(100000))
        )
        .aggregations("avg_rating", avgAgg("averageRating"))
```

**Response structure:**
```json
{
  "hits": {
    "total": { "value": 45 },
    "hits": [
      { "_source": { "name": "Chicken Rice" } },
      // ... more results
    ]
  },
  "aggregations": {
    "categories": {
      "buckets": [
        { "key": "Main Course", "doc_count": 30 },
        { "key": "Appetizer", "doc_count": 15 }
      ]
    },
    "price_ranges": {
      "buckets": [
        { "key": "*-50000", "doc_count": 18 },
        { "key": "50000-100000", "doc_count": 27 }
      ]
    },
    "avg_rating": {
      "value": 4.3
    }
  }
}
```

**Frontend usage:**
```javascript
// Display filters dynamically
response.aggregations.categories.buckets.forEach(bucket => {