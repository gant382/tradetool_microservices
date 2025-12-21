#!/bin/bash
# Final comprehensive fix for all remaining compilation errors

echo "=== Phase 3 Final Fix Script ==="

# 1. Ensure Assert.isValidUUID(String, String) exists - rebuild ws-api
echo "Step 1: Rebuilding callcard-ws-api..."
cd callcard-ws-api && /c/apache-maven-3.9.6/bin/mvn clean install -DskipTests && cd ..

# 2. Rebuild callcard-entity with all entity fixes
echo "Step 2: Rebuilding callcard-entity..."
cd callcard-entity && /c/apache-maven-3.9.6/bin/mvn clean install -DskipTests && cd ..

# 3. Build callcard-components
echo "Step 3: Building callcard-components..."
/c/apache-maven-3.9.6/bin/mvn clean compile -pl callcard-components -am -DskipTests 2>&1 | tee build_phase3_final_attempt.log

echo "=== Error count ==="
grep "^\[ERROR\]" build_phase3_final_attempt.log | wc -l

echo "=== Build result ==="
tail -20 build_phase3_final_attempt.log
