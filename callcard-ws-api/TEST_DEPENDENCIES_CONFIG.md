# CallCard Integration Test Suite - Dependencies Configuration

This document provides the necessary Maven dependencies and configuration for the integration test suite.

## Add to pom.xml - Dependencies Section

```xml
<!-- Test Dependencies -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
    <exclusions>
        <exclusion>
            <groupId>org.junit.vintage</groupId>
            <artifactId>junit-vintage-engine</artifactId>
        </exclusion>
    </exclusions>
</dependency>

<!-- JUnit 5 -->
<dependency>
    <groupId>org.junit.jupiter</groupId>
    <artifactId>junit-jupiter-api</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.junit.jupiter</groupId>
    <artifactId>junit-jupiter-engine</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.junit.jupiter</groupId>
    <artifactId>junit-jupiter-params</artifactId>
    <scope>test</scope>
</dependency>

<!-- H2 Embedded Database (for test environments) -->
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>test</scope>
</dependency>

<!-- Mockito for mocking -->
<dependency>
    <groupId>org.mockito</groupId>
    <artifactId>mockito-core</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.mockito</groupId>
    <artifactId>mockito-junit-jupiter</artifactId>
    <scope>test</scope>
</dependency>

<!-- AssertJ for fluent assertions (optional but recommended) -->
<dependency>
    <groupId>org.assertj</groupId>
    <artifactId>assertj-core</artifactId>
    <scope>test</scope>
</dependency>

<!-- REST Assured for REST API testing (optional) -->
<dependency>
    <groupId>io.rest-assured</groupId>
    <artifactId>rest-assured</artifactId>
    <scope>test</scope>
</dependency>

<!-- JSON Path for JSON assertions (optional) -->
<dependency>
    <groupId>com.jayway.jsonpath</groupId>
    <artifactId>json-path</artifactId>
    <scope>test</scope>
</dependency>

<!-- WireMock for mocking HTTP endpoints (optional) -->
<dependency>
    <groupId>com.github.tomakehurst</groupId>
    <artifactId>wiremock-jre8</artifactId>
    <scope>test</scope>
</dependency>

<!-- Hamcrest matchers (optional but recommended) -->
<dependency>
    <groupId>org.hamcrest</groupId>
    <artifactId>hamcrest</artifactId>
    <scope>test</scope>
</dependency>
```

## Build Plugins Configuration

Add to pom.xml - `<build>` section:

```xml
<build>
    <plugins>
        <!-- Surefire Plugin for running tests -->
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-surefire-plugin</artifactId>
            <version>2.22.2</version>
            <configuration>
                <includes>
                    <include>**/*Test.java</include>
                    <include>**/*Tests.java</include>
                    <include>**/*IntegrationTest.java</include>
                </includes>
                <excludes>
                    <exclude>**/*Abstract*.java</exclude>
                </excludes>
                <parallel>methods</parallel>
                <threadCount>4</threadCount>
            </configuration>
        </plugin>

        <!-- JaCoCo Plugin for code coverage -->
        <plugin>
            <groupId>org.jacoco</groupId>
            <artifactId>jacoco-maven-plugin</artifactId>
            <version>0.8.8</version>
            <executions>
                <execution>
                    <goals>
                        <goal>prepare-agent</goal>
                    </goals>
                </execution>
                <execution>
                    <id>report</id>
                    <phase>test</phase>
                    <goals>
                        <goal>report</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>

        <!-- Failsafe Plugin for integration tests -->
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-failsafe-plugin</artifactId>
            <version>2.22.2</version>
            <configuration>
                <includes>
                    <include>**/*IntegrationTest.java</include>
                </includes>
            </configuration>
            <executions>
                <execution>
                    <goals>
                        <goal>integration-test</goal>
                        <goal>verify</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

## Test Profiles Configuration

Add to parent pom.xml (if using profiles):

```xml
<profiles>
    <!-- Test Profile -->
    <profile>
        <id>test</id>
        <activation>
            <activeByDefault>true</activeByDefault>
        </activation>
        <properties>
            <spring.profiles.active>test</spring.profiles.active>
        </properties>
    </profile>

    <!-- Integration Test Profile -->
    <profile>
        <id>integration-test</id>
        <build>
            <plugins>
                <plugin>
                    <groupId>org.apache.maven.plugins</groupId>
                    <artifactId>maven-failsafe-plugin</artifactId>
                    <executions>
                        <execution>
                            <goals>
                                <goal>integration-test</goal>
                                <goal>verify</goal>
                            </goals>
                        </execution>
                    </executions>
                </plugin>
            </plugins>
        </build>
    </profile>

    <!-- Coverage Report Profile -->
    <profile>
        <id>coverage</id>
        <build>
            <plugins>
                <plugin>
                    <groupId>org.jacoco</groupId>
                    <artifactId>jacoco-maven-plugin</artifactId>
                    <executions>
                        <execution>
                            <goals>
                                <goal>prepare-agent</goal>
                            </goals>
                        </execution>
                        <execution>
                            <id>report</id>
                            <phase>test</phase>
                            <goals>
                                <goal>report</goal>
                            </goals>
                        </execution>
                    </executions>
                </plugin>
            </plugins>
        </build>
    </profile>
</profiles>
```

## Test Execution Maven Commands

### Basic Commands

```bash
# Run all tests
mvn clean test

# Run with coverage
mvn clean test jacoco:report

# Run integration tests only
mvn clean verify -P integration-test

# Run specific test class
mvn test -Dtest=CallCardIntegrationTest

# Run with specific profile
mvn test -Ptest

# Run all with coverage
mvn clean verify -P coverage
```

### Advanced Commands

```bash
# Run tests in parallel
mvn test -DthreadCount=4 -DparallelOptimized=true

# Run with detailed logging
mvn test -X

# Skip tests during build
mvn clean install -DskipTests

# Run tests and generate report
mvn clean test jacoco:report

# Run specific test method
mvn test -Dtest=CallCardIntegrationTest#testGetCallCardsFromTemplate

# Run tests matching pattern
mvn test -Dtest=CallCard*

# Run tests and fail on first failure
mvn test -X -fail-at-end

# Run tests with specific spring profile
mvn test -Dspring.profiles.active=test
```

## IDE Configuration

### IntelliJ IDEA

1. **Run Tests**: Right-click test class → Run Tests
2. **Coverage**: Right-click test class → Run Tests with Coverage
3. **Debug**: Right-click test method → Debug Tests
4. **Configure**: Settings → Build, Execution, Deployment → Build Tools → Maven

### Eclipse

1. **Run Tests**: Right-click project → Run As → Maven Test
2. **Coverage**: Right-click project → Coverage As → Maven Test
3. **Debug**: Right-click test → Debug As → JUnit Test

### VS Code

```bash
# Install Extension: Maven for Java
# Then: View → Command Palette → Maven: Run Tests
```

## CI/CD Integration

### GitHub Actions Example

```yaml
name: Integration Tests

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest

    steps:
      - uses: actions/checkout@v2

      - name: Set up JDK 1.8
        uses: actions/setup-java@v2
        with:
          java-version: '1.8'
          distribution: 'adopt'

      - name: Run tests
        run: mvn clean test

      - name: Generate coverage report
        run: mvn jacoco:report

      - name: Upload coverage to Codecov
        uses: codecov/codecov-action@v2
```

### Jenkins Pipeline Example

```groovy
pipeline {
    agent any

    stages {
        stage('Checkout') {
            steps {
                git 'https://github.com/saicon/callcard-ws-api.git'
            }
        }

        stage('Build') {
            steps {
                sh 'mvn clean compile'
            }
        }

        stage('Test') {
            steps {
                sh 'mvn test'
            }
        }

        stage('Coverage') {
            steps {
                sh 'mvn jacoco:report'
                publishHTML([
                    reportDir: 'target/site/jacoco',
                    reportFiles: 'index.html',
                    reportName: 'JaCoCo Coverage'
                ])
            }
        }
    }

    post {
        always {
            junit 'target/surefire-reports/*.xml'
        }
    }
}
```

### GitLab CI Example

```yaml
stages:
  - test

test:
  stage: test
  image: maven:3.8-jdk-8
  script:
    - mvn clean test jacoco:report
  artifacts:
    reports:
      junit: target/surefire-reports/*.xml
    paths:
      - target/site/jacoco/
  coverage: '/Coverage: \d+\.\d+%/'
```

## Code Coverage Configuration

### JaCoCo Target Configuration

```xml
<!-- In maven-jacoco-plugin configuration -->
<executions>
    <execution>
        <id>coverage-check</id>
        <phase>test</phase>
        <goals>
            <goal>check</goal>
        </goals>
        <configuration>
            <rules>
                <rule>
                    <element>PACKAGE</element>
                    <includesList>
                        <includes>com.saicon.games.callcard*</includes>
                    </includesList>
                    <limits>
                        <limit>
                            <counter>LINE</counter>
                            <value>COVEREDRATIO</value>
                            <minimum>0.80</minimum>
                        </limit>
                    </limits>
                </rule>
            </rules>
        </configuration>
    </execution>
</executions>
```

## Troubleshooting

### Maven Not Found
```bash
# Install Maven
mvn --version

# If not installed, download from maven.apache.org
# Add to PATH environment variable
```

### Test Dependency Conflicts
```bash
# Analyze dependency tree
mvn dependency:tree

# Resolve conflicts in pom.xml <dependencyManagement>
```

### H2 Database Connection Issues
```bash
# Check H2 configuration in application-test.yml
# Ensure driver is in classpath: com.h2database:h2

# Test connection
mvn test -Dtest=CallCardValidationIntegrationTest
```

### Spring Context Not Initializing
```bash
# Check TestApplicationConfiguration.java
# Verify @ComponentScan paths
# Check active profiles: spring.profiles.active=test
```

## Next Steps

1. **Add Dependencies**: Copy dependency blocks to callcard-ws-api/pom.xml
2. **Configure Plugins**: Add build plugin configuration
3. **Run Tests**: `mvn clean test`
4. **Review Coverage**: Open `target/site/jacoco/index.html`
5. **Setup CI/CD**: Choose GitHub Actions, Jenkins, or GitLab CI
6. **Configure IDE**: Setup test runner in your IDE

---

**Last Updated**: December 22, 2025
