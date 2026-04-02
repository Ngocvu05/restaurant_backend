@echo off
setlocal

echo ===============================
echo 🛠️  Building all services...
echo ===============================

echo 🔧 Building user-service...
pushd backend-service
call gradlew.bat clean build -x test
popd

echo 🔧 Building search-service...
pushd search-service
call gradlew.bat clean build -x test
popd

echo 🔧 Building chat-service...
pushd chat-service
call gradlew.bat clean build -x test
popd

echo 🔧 Building discovery-service...
pushd discovery-service
call gradlew.bat clean build -x test
popd

echo 🔧 Building api-gateway...
pushd api-gateway
call gradlew.bat clean build -x test
popd

echo ===============================
echo 🐳 Starting Docker Compose...
echo ===============================
docker-compose -f docker-compose.yml up --build

endlocal
pause