CLASSPATH_ROOT="libs/apiguardian-api-1.1.2.jar:libs/jason-3.1.jar:libs/junit-jupiter-api-5.9.2.jar:libs/junit-jupiter-engine-5.9.2.jar:libs/junit-platform-commons-1.9.2.jar:libs/junit-platform-console-standalone-1.9.2.jar:libs/junit-platform-engine-1.9.2.jar:libs/junit-platform-launcher-1.9.2.jar:libs/opentest4j-1.2.0.jar"
CLASSPATH_TEST="../libs/apiguardian-api-1.1.2.jar:../libs/jason-3.1.jar:../libs/junit-jupiter-api-5.9.2.jar:../libs/junit-jupiter-engine-5.9.2.jar:../libs/junit-platform-commons-1.9.2.jar:../libs/junit-platform-console-standalone-1.9.2.jar:../libs/junit-platform-engine-1.9.2.jar:../libs/junit-platform-launcher-1.9.2.jar:../libs/opentest4j-1.2.0.jar"
SOURCES=`find src -name *.java`
TEST_SOURCES=`find test -name *.java`
echo "Found files:" &&
echo $SOURCES &&
echo "Test sources:" &&
echo $TEST_SOURCES
javac $SOURCES $TEST_SOURCES -d test/build -cp $CLASSPATH_ROOT &&
cd test
java -jar ../libs/junit-platform-console-standalone-1.9.2.jar -cp $CLASSPATH_TEST:build/ --scan-class-path