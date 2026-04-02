#!/bin/bash

# Elasticsearch Advanced Search - Test Commands
# Đảm bảo service đang chạy ở localhost:8083

BASE_URL="http://localhost:8083/api/v1/search/advanced"

echo "🔍 ELASTICSEARCH ADVANCED SEARCH TESTING"
echo "========================================"

# 1. FUZZY SEARCH TEST
echo -e "\n1️⃣ Testing Fuzzy Search..."
echo "Searching for 'pho' with AUTO fuzziness:"
curl -s -X GET "${BASE_URL}/fuzzy?q=pho&maxEdits=AUTO" \
  -H "X-User-Id: user123" \
  -H "X-Session-Id: session456" | jq '.'

echo -e "\nSearching for 'chiken' (typo) with maxEdits=2:"
curl -s -X GET "${BASE_URL}/fuzzy?q=chiken&maxEdits=2" | jq '.[] | {name: .dish.name, score: .score}'

# 2. AUTOCOMPLETE TEST
echo -e "\n2️⃣ Testing Autocomplete..."
echo "Autocomplete for 'ph':"
curl -s -X GET "${BASE_URL}/autocomplete?prefix=ph" | jq '.'

echo -e "\nAutocomplete for 'bun':"
curl -s -X GET "${BASE_URL}/autocomplete?prefix=bun" | jq '.'

# 3. AGGREGATIONS TEST
echo -e "\n3️⃣ Testing Search with Aggregations..."
echo "Search 'chicken' with price range 30000-100000:"
curl -s -X GET "${BASE_URL}/aggregations?q=chicken&minPrice=30000&maxPrice=100000" | jq '{
  totalHits: .totalHits,
  categoryCounts: .categoryCounts,
  priceRanges: .priceRanges,
  averageRating: .averageRating
}'

echo -e "\nFaceted search with category filter:"
curl -s -X GET "${BASE_URL}/aggregations?category=Main%20Course" | jq '{
  totalHits: .totalHits,
  categoryCounts: .categoryCounts,
  averageRating: .averageRating
}'

# 4. CUSTOM SCORING TEST
echo -e "\n4️⃣ Testing Custom Scoring (Ranked Search)..."
echo "Search 'noodles' with custom scoring:"
curl -s -X GET "${BASE_URL}/ranked?q=noodles" | jq '.[] | {
  name: .dish.name,
  rating: .dish.averageRating,
  orderCount: .dish.orderCount,
  score: .score
}' | head -20

# 5. MORE LIKE THIS TEST
echo -e "\n5️⃣ Testing More Like This..."
echo "Find similar dishes to dish ID 1:"
curl -s -X GET "${BASE_URL}/similar/1?size=5" | jq '.[] | {
  id: .dishId,
  name: .name,
  category: .category
}'

# 6. TRACK CLICK EVENT
echo -e "\n6️⃣ Testing Click Tracking..."
echo "Tracking click on dish 123 at position 2:"
curl -s -X POST "${BASE_URL}/track-click?keyword=pho&dishId=123&position=2" \
  -H "X-Session-Id: session456" \
  -H "Content-Type: application/json"
echo "✓ Click tracked"

# 7. ANALYTICS - TOP SEARCHES
echo -e "\n7️⃣ Testing Analytics - Top Searches..."
echo "Top 10 searches from last 7 days:"
curl -s -X GET "${BASE_URL}/analytics/top?limit=10&days=7" | jq '.[] | {
  keyword: .keyword,
  searchCount: .searchCount,
  resultCount: .resultCount,
  lastSearched: .lastSearched
}'

# 8. ANALYTICS - ZERO RESULTS
echo -e "\n8️⃣ Testing Analytics - Zero Result Searches..."
echo "Keywords with zero results:"
curl -s -X GET "${BASE_URL}/analytics/zero-results?limit=10&days=7" | jq '.[] | {
  keyword: .keyword,
  searchCount: .searchCount
}'

# 9. ANALYTICS - TRENDING
echo -e "\n9️⃣ Testing Analytics - Trending Searches..."
echo "Trending keywords:"
curl -s -X GET "${BASE_URL}/analytics/trending?limit=10" | jq '.[] | {
  keyword: .keyword,
  searchCount: .searchCount,
  growthRate: .averageClickPosition
}'

# 10. ANALYTICS - CTR
echo -e "\n🔟 Testing Analytics - Click-Through Rate..."
echo "CTR for last 7 days:"
curl -s -X GET "${BASE_URL}/analytics/ctr?days=7" | jq '.'

# 11. GEOSPATIAL - NEARBY (if implemented)
echo -e "\n🗺️ Testing Geospatial Search..."
echo "Finding restaurants within 5km of Saigon Center:"
GEO_URL="http://localhost:8083/api/v1/geo"
curl -s -X GET "${GEO_URL}/nearby?lat=10.762622&lon=106.660172&distance=5km" | jq '.[] | {
  name: .name,
  address: .address,
  distance: .distanceFromUser
}' 2>/dev/null || echo "Geospatial API not configured"

# PERFORMANCE TEST
echo -e "\n⚡ Performance Testing..."
echo "Running 10 searches to measure response time:"

for i in {1..10}; do
  KEYWORDS=("pho" "banh mi" "spring rolls" "fried rice" "noodles")
  KEYWORD=${KEYWORDS[$RANDOM % ${#KEYWORDS[@]}]}

  START=$(date +%s%3N)
  curl -s -X GET "${BASE_URL}/fuzzy?q=${KEYWORD}" > /dev/null
  END=$(date +%s%3N)

  DURATION=$((END - START))
  echo "Search #${i} (${KEYWORD}): ${DURATION}ms"
done

# BULK SEARCH TEST
echo -e "\n📦 Bulk Search Test..."
echo "Searching multiple keywords concurrently:"

declare -a PIDS
KEYWORDS=("pho" "banh mi" "spring rolls" "bun" "com")

for keyword in "${KEYWORDS[@]}"; do
  curl -s -X GET "${BASE_URL}/fuzzy?q=${keyword}" > /dev/null &
  PIDS+=($!)
done

# Wait for all background jobs
for pid in "${PIDS[@]}"; do
  wait $pid
done
echo "✓ All concurrent searches completed"

# STRESS TEST (optional)
echo -e "\n🏋️ Stress Test (100 requests)..."
read -p "Run stress test? (y/n): " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
  START_TIME=$(date +%s)

  for i in {1..100}; do
    curl -s -X GET "${BASE_URL}/fuzzy?q=test${i}" > /dev/null &

    # Limit concurrent requests
    if (( i % 10 == 0 )); then
      wait
    fi
  done
  wait

  END_TIME=$(date +%s)
  DURATION=$((END_TIME - START_TIME))
  echo "✓ 100 requests completed in ${DURATION} seconds"
  echo "Average: $((DURATION * 10))ms per request"
fi

# VALIDATE INDEX HEALTH
echo -e "\n🏥 Checking Elasticsearch Index Health..."
curl -s -X GET "http://localhost:9200/_cat/indices/dishes?v" 2>/dev/null || echo "Cannot connect to Elasticsearch"

echo -e "\n✅ Testing completed!"
echo "========================================"
echo "Check logs for any errors"
echo "View analytics at: ${BASE_URL}/analytics/top"