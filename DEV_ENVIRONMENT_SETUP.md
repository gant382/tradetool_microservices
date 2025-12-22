# CallCard Microservice - Development Environment Setup Guide

Complete guide for setting up a development environment for the CallCard Microservice (tradetool_middleware) project.

## Table of Contents

1. [System Requirements](#system-requirements)
2. [Required Software Installation](#required-software-installation)
3. [IDE Configuration](#ide-configuration)
4. [Database Setup](#database-setup)
5. [Maven Configuration](#maven-configuration)
6. [Environment Variables](#environment-variables)
7. [Project Setup](#project-setup)
8. [Git Workflow](#git-workflow)
9. [Code Style and Formatting](#code-style-and-formatting)
10. [Pre-commit Hooks](#pre-commit-hooks)
11. [Debugging Setup](#debugging-setup)
12. [Recommended Plugins and Tools](#recommended-plugins-and-tools)
13. [Troubleshooting](#troubleshooting)

---

## System Requirements

### Minimum Requirements
- **OS**: Windows 10/11, macOS 10.15+, or Linux (Ubuntu 18.04+)
- **RAM**: 8 GB (16 GB recommended for smooth development)
- **Disk Space**: 10 GB free space (for JDK, Maven, dependencies, and databases)
- **Network**: Internet connectivity for Maven repository downloads

### Recommended Specifications
- **OS**: Windows 11 or Ubuntu 22.04 LTS
- **RAM**: 16 GB or more
- **CPU**: Intel i7/i9 or equivalent (multi-core processor)
- **Disk**: SSD with at least 20 GB free space

---

## Required Software Installation

### 1. Java Development Kit (JDK)

**Version Required**: Java 8 (JDK 1.8.0_301 or later)

#### Windows Installation

1. Download JDK 8 from [Oracle Java SE](https://www.oracle.com/java/technologies/javase/javase8-archive-downloads.html)
   - Select `jdk-8u[version]-windows-x64.exe` (for 64-bit systems)
   - Or use OpenJDK: [AdoptOpenJDK](https://adoptopenjdk.net/) or [Eclipse Temurin](https://adoptium.net/)

2. Run the installer and follow the installation wizard:
   ```
   JDK Installation Path: C:\Program Files\Java\jdk1.8.0_[version]
   ```

3. Set JAVA_HOME environment variable (see [Environment Variables](#environment-variables) section)

#### macOS Installation

Using Homebrew (recommended):
```bash
brew tap adoptopenjdk/openjdk
brew install adoptopenjdk8
export JAVA_HOME=$(/usr/libexec/java_home -v 1.8)
```

#### Linux Installation (Ubuntu)

```bash
sudo apt-get update
sudo apt-get install openjdk-8-jdk
export JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64
```

#### Verify Installation

```bash
java -version
# Expected output: openjdk version "1.8.0_[version]" or similar

javac -version
# Expected output: javac 1.8.0_[version]
```

### 2. Apache Maven

**Version Required**: Maven 3.6.3 or later

#### Windows Installation

1. Download Maven from [Apache Maven](https://maven.apache.org/download.cgi)
   - Select `apache-maven-3.8.6-bin.zip` (or latest stable)

2. Extract to a directory:
   ```
   C:\apache-maven-3.8.6
   ```

3. Add to PATH (see [Environment Variables](#environment-variables) section)

#### macOS Installation

Using Homebrew:
```bash
brew install maven
```

#### Linux Installation

```bash
sudo apt-get update
sudo apt-get install maven
```

#### Verify Installation

```bash
mvn -version
# Expected output: Apache Maven 3.8.6 (or newer)
```

### 3. Microsoft SQL Server 2008 or Later

The project uses Microsoft SQL Server for data persistence.

#### Windows Installation (Development)

**Option 1: SQL Server Express (Recommended for Development)**

1. Download from [SQL Server Express](https://www.microsoft.com/en-us/sql-server/sql-server-express)
2. Run installer with defaults
3. Enable SQL Server Authentication during setup (Mixed Mode Authentication)
4. Default instance name: `SQLEXPRESS`
5. Default port: `1433`

**Option 2: Docker Container (Alternative)**

```bash
docker run -e 'ACCEPT_EULA=Y' -e 'SA_PASSWORD=Your@Password123' `
    -p 1433:1433 --name sqlserver `
    -d mcr.microsoft.com/mssql/server:2019-latest
```

#### Verify Installation

Use SQL Server Management Studio (SSMS) or command line:
```bash
sqlcmd -S localhost\SQLEXPRESS -U sa -P <password>
```

### 4. Git

**Version Required**: Git 2.30 or later

#### Windows Installation

Download from [Git for Windows](https://git-scm.com/download/win)
- Choose defaults during installation
- Enable Git Bash

#### macOS/Linux Installation

```bash
# macOS
brew install git

# Ubuntu
sudo apt-get install git
```

#### Verify Installation

```bash
git --version
# Expected output: git version 2.30.0 (or newer)
```

### 5. IDE (Integrated Development Environment)

Choose one of the recommended options below.

---

## IDE Configuration

### Option A: IntelliJ IDEA (Recommended)

IntelliJ IDEA is the primary IDE recommended for this project.

#### Installation

1. Download from [JetBrains IntelliJ IDEA](https://www.jetbrains.com/idea/download/)
   - Community Edition is free and sufficient
   - Ultimate Edition recommended for enterprise features

2. Install and launch IntelliJ IDEA

#### Initial Setup

1. **Configure JDK**:
   - Go to: `File` > `Project Structure` > `Project`
   - Select `JDK 1.8` as the Project SDK
   - If not present, click `Add SDK` > `JDK` and browse to your JDK installation

2. **Configure Maven**:
   - Go to: `File` > `Settings` > `Build, Execution, Deployment` > `Build Tools` > `Maven`
   - Set Maven home path: `C:\apache-maven-3.8.6` (or your Maven directory)
   - Ensure "Use default settings" or configure `settings.xml` (see [Maven Configuration](#maven-configuration))

3. **Import Project**:
   - Open the `tradetool_middleware` directory as a project
   - IntelliJ will automatically detect it as a Maven project
   - Wait for indexing to complete
   - Let Maven download all dependencies (first time may take 5-10 minutes)

4. **Configure Run Configurations**:

   **For Spring Boot Application**:
   - Go to: `Run` > `Edit Configurations`
   - Click `+` > `Spring Boot`
   - Set name: `CallCard Server`
   - Main class: `com.saicon.games.callcard.ws.CallCardServerApplication`
   - Working directory: `$MODULE_DIR$`
   - Use classpath of module: `CallCard_Server_WS`
   - Add environment variables (see [Environment Variables](#environment-variables))
   - Click `OK`

   **For Maven Build**:
   - Go to: `Run` > `Edit Configurations`
   - Click `+` > `Maven`
   - Set name: `Build All Modules`
   - Working directory: `$PROJECT_DIR$`
   - Command line: `clean install`
   - Click `OK`

#### Code Inspection Settings

1. Go to: `File` > `Settings` > `Editor` > `Code Style`

2. Configure Java Code Style:
   - Scheme: `Project` (to apply to this project)
   - Line separator: `Unix (\n)`
   - Indentation: `4 spaces` (not tabs)
   - Continuation indent: `8 spaces`

3. Enable inspections:
   - Go to: `File` > `Settings` > `Editor` > `Inspections`
   - Search for: `Unused declaration`
   - Search for: `Unreachable code`
   - Search for: `SQL dialect`

#### Useful Plugins for IntelliJ IDEA

Install from `File` > `Settings` > `Plugins`:

1. **SonarLint** - Code quality analysis
   - Search for "SonarLint" and install
   - Configure with SonarQube if available

2. **CheckStyle-IDEA** - Code style verification
   - Helps maintain consistent code style

3. **Spotbugs** - Bug detection
   - Identifies potential bugs in Java code

4. **Maven Helper** - Maven integration improvements
   - Provides helpful Maven commands in IDE

5. **Database Navigator** - Database management
   - Connects to SQL Server and manages schema

### Option B: Eclipse IDE

#### Installation

1. Download from [Eclipse IDE](https://www.eclipse.org/downloads/)
   - Choose "Eclipse IDE for Enterprise Java Developers"

2. Extract and launch

#### Initial Setup

1. **Configure JDK**:
   - Go to: `Window` > `Preferences` > `Java` > `Installed JREs`
   - Click `Add...` and browse to JDK 1.8 installation directory

2. **Configure Maven**:
   - Go to: `Window` > `Preferences` > `Maven` > `Installations`
   - Click `Add...` and browse to Maven installation directory

3. **Import Project**:
   - Go to: `File` > `Import...` > `Maven` > `Existing Maven Projects`
   - Browse to `tradetool_middleware` directory
   - Select all modules with checkboxes
   - Click `Finish`
   - Wait for project indexing and dependency resolution

4. **Configure Run Configuration**:
   - Go to: `Run` > `Run Configurations...`
   - Create new `Maven Build`:
     - Goals: `spring-boot:run`
     - Base directory: `${workspace_loc:/CallCard_Server_WS}`
     - Working directory: `${workspace_loc:/CallCard_Server_WS}`

### Option C: Visual Studio Code

#### Installation

1. Download from [Visual Studio Code](https://code.visualstudio.com/)

2. Install recommended extensions:
   - "Extension Pack for Java" (by Microsoft)
   - "Maven for Java" (by Microsoft)
   - "Spring Boot Extension Pack" (by Pivotal)
   - "Database Client" (for SQL Server integration)

#### Initial Setup

1. Open the `tradetool_middleware` folder in VS Code

2. VS Code will prompt to configure:
   - Select JDK 1.8 when prompted
   - Maven will be auto-detected

3. Create `.vscode/launch.json` for debugging (see [Debugging Setup](#debugging-setup))

---

## Database Setup

### SQL Server Database Configuration

#### Step 1: Create Database

Using SQL Server Management Studio (SSMS) or sqlcmd:

**Using SSMS**:
1. Connect to your SQL Server instance
2. Right-click on `Databases` > `New Database`
3. Database name: `callcard_db`
4. Initial size: 100 MB
5. Click `OK`

**Using sqlcmd**:
```bash
sqlcmd -S localhost\SQLEXPRESS -U sa -P <your_sa_password>
> CREATE DATABASE callcard_db;
> GO
> USE callcard_db;
> GO
> EXIT
```

#### Step 2: Create Database User

**Using SSMS**:
1. Expand `Security` > `Logins`
2. Right-click `New Login`
3. Login name: `callcard_user`
4. Authentication: `SQL Server Authentication`
5. Password: Set a strong password (e.g., `CallCard@Dev123`)
6. Uncheck `Enforce password policy` (for development only)
7. Go to `User Mapping` tab
8. Select `callcard_db` and check all roles
9. Click `OK`

**Using sqlcmd**:
```bash
sqlcmd -S localhost\SQLEXPRESS -U sa -P <sa_password>
> USE callcard_db;
> CREATE LOGIN callcard_user WITH PASSWORD = 'CallCard@Dev123';
> CREATE USER callcard_user FOR LOGIN callcard_user;
> EXEC sp_addrolemember 'db_owner', 'callcard_user';
> GO
> EXIT
```

#### Step 3: Run Database Migration Scripts

**Option 1: Using Flyway (if configured)**

Flyway scripts should be in: `CallCard_Server_WS/src/main/resources/db/migration/`

Scripts will auto-run on application startup.

**Option 2: Manual Migration**

1. Locate migration scripts in the project:
   ```
   CallCard_Server_WS/src/main/resources/db/migration/
   ```

2. Execute scripts in order using SSMS or sqlcmd:
   ```bash
   sqlcmd -S localhost\SQLEXPRESS -U callcard_user -P CallCard@Dev123 `
          -d callcard_db -i .\V001__initial_schema.sql
   ```

#### Step 4: Verify Database Connection

Using a simple test SQL command:
```bash
sqlcmd -S localhost\SQLEXPRESS -U callcard_user -P CallCard@Dev123 -d callcard_db
> SELECT DB_NAME() AS [Current Database];
> GO
```

Expected output:
```
Current Database
callcard_db
```

### Connection String

For application configuration, the JDBC connection string is:

```
jdbc:sqlserver://localhost\SQLEXPRESS:1433;databaseName=callcard_db;user=callcard_user;password=CallCard@Dev123;
```

Or with encrypted connection:
```
jdbc:sqlserver://localhost\SQLEXPRESS:1433;databaseName=callcard_db;user=callcard_user;password=CallCard@Dev123;encrypt=true;trustServerCertificate=true;
```

---

## Maven Configuration

### Maven settings.xml

Maven configuration is typically located at:
- **Windows**: `C:\Users\<username>\.m2\settings.xml`
- **macOS/Linux**: `~/.m2/settings.xml`

#### Create or Update settings.xml

If the file doesn't exist, create it:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0
                              http://maven.apache.org/xsd/settings-1.0.0.xsd">

    <!-- Local Repository Location -->
    <localRepository>C:\Users\<username>\.m2\repository</localRepository>
    <!-- Or on macOS/Linux: ~/.m2/repository -->

    <!-- Enable Offline Mode (optional) -->
    <offline>false</offline>

    <!-- Server Credentials (if using private repositories) -->
    <servers>
        <server>
            <id>central</id>
            <username>your_username</username>
            <password>your_password</password>
        </server>
    </servers>

    <!-- Mirror Configuration -->
    <mirrors>
        <!-- Use Aliyun mirror for faster downloads in Asia -->
        <mirror>
            <id>aliyun</id>
            <mirrorOf>central</mirrorOf>
            <name>Aliyun Maven Mirror</name>
            <url>https://maven.aliyun.com/repository/public</url>
        </mirror>
        <!-- Or use default Central Repository -->
        <mirror>
            <id>central</id>
            <mirrorOf>*</mirrorOf>
            <name>Maven Central Repository</name>
            <url>https://repo.maven.apache.org/maven2</url>
        </mirror>
    </mirrors>

    <!-- Profiles for Different Environments -->
    <profiles>
        <profile>
            <id>dev</id>
            <properties>
                <maven.compiler.source>1.8</maven.compiler.source>
                <maven.compiler.target>1.8</maven.compiler.target>
                <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
            </properties>
        </profile>

        <profile>
            <id>prod</id>
            <properties>
                <maven.compiler.source>1.8</maven.compiler.source>
                <maven.compiler.target>1.8</maven.compiler.target>
                <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
                <skipTests>true</skipTests>
            </properties>
        </profile>
    </profiles>

    <!-- Active Profiles -->
    <activeProfiles>
        <activeProfile>dev</activeProfile>
    </activeProfiles>

</settings>
```

### Maven Commands for Development

#### Building the Project

```bash
# Clean and build all modules
mvn clean install

# Build without running tests (faster)
mvn clean install -DskipTests

# Build with testing and code coverage
mvn clean install -Dargline="-Xmx1024m"

# Build specific module
mvn clean install -pl callcard-service -am

# Rebuild after configuration changes
mvn clean compile
```

#### Running the Application

```bash
# Run Spring Boot application with embedded server
mvn spring-boot:run -pl CallCard_Server_WS

# Run with custom JVM arguments
mvn spring-boot:run -pl CallCard_Server_WS -Dspring-boot.run.arguments="--server.port=8081"

# Run with debug mode
mvn spring-boot:run -pl CallCard_Server_WS -Dspring-boot.run.jvmArguments="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005"
```

#### Testing

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=CallCardServiceTest

# Run with code coverage (JaCoCo)
mvn clean test jacoco:report

# Skip tests during build
mvn clean install -DskipTests

# Run integration tests only
mvn verify -Dgroups=integration
```

#### Dependency Management

```bash
# Show dependency tree
mvn dependency:tree

# Find outdated dependencies
mvn versions:display-dependency-updates

# Update specific dependency version
mvn versions:set-property -Dproperty=cxf.version -DnewVersion=3.5.10

# Resolve dependency conflicts
mvn dependency:resolve
```

---

## Environment Variables

### Windows (System-wide)

1. Open **System Properties**:
   - Right-click `This PC` or `My Computer` > `Properties`
   - Click `Advanced system settings`
   - Click `Environment Variables` button

2. Under "System variables", click `New`:

   **JAVA_HOME**:
   - Variable name: `JAVA_HOME`
   - Variable value: `C:\Program Files\Java\jdk1.8.0_[version]`

   **MAVEN_HOME**:
   - Variable name: `MAVEN_HOME`
   - Variable value: `C:\apache-maven-3.8.6`

   **Path** (Edit existing):
   - Add: `C:\Program Files\Java\jdk1.8.0_[version]\bin`
   - Add: `C:\apache-maven-3.8.6\bin`

3. Click `OK` and close all windows

4. Open new Command Prompt and verify:
   ```bash
   echo %JAVA_HOME%
   echo %MAVEN_HOME%
   mvn -version
   ```

### macOS/Linux (Shell Profile)

Edit `~/.bash_profile` or `~/.zshrc` (depending on your shell):

```bash
# Java
export JAVA_HOME=$(/usr/libexec/java_home -v 1.8)  # macOS
# export JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64  # Linux

# Maven
export MAVEN_HOME=/opt/maven  # or wherever Maven is installed
export PATH="$JAVA_HOME/bin:$MAVEN_HOME/bin:$PATH"
```

Save file and reload:
```bash
source ~/.bash_profile  # or ~/.zshrc
```

Verify:
```bash
echo $JAVA_HOME
echo $MAVEN_HOME
mvn -version
```

### Application Environment Variables

Create `.env` file in project root or set in IDE run configuration:

```properties
# Database Configuration
DB_HOST=localhost\SQLEXPRESS
DB_PORT=1433
DB_NAME=callcard_db
DB_USER=callcard_user
DB_PASSWORD=CallCard@Dev123

# Spring Boot
SPRING_PROFILES_ACTIVE=dev
SPRING_DATASOURCE_URL=jdbc:sqlserver://localhost\SQLEXPRESS:1433;databaseName=callcard_db;user=callcard_user;password=CallCard@Dev123;encrypt=true;trustServerCertificate=true;
SPRING_DATASOURCE_USERNAME=callcard_user
SPRING_DATASOURCE_PASSWORD=CallCard@Dev123
SPRING_DATASOURCE_DRIVER_CLASS_NAME=com.microsoft.sqlserver.jdbc.SQLServerDriver

# Server Configuration
SERVER_PORT=8080
SERVER_SERVLET_CONTEXT_PATH=/callcard

# Logging
LOGGING_LEVEL_ROOT=INFO
LOGGING_LEVEL_COM_SAICON=DEBUG
LOGGING_FILE=logs/callcard.log

# Application
APP_NAME=CallCard Microservice
APP_VERSION=1.0.0

# Feature Flags
FEATURE_CACHE_ENABLED=true
FEATURE_METRICS_ENABLED=true
FEATURE_SWAGGER_ENABLED=true
```

---

## Project Setup

### Step 1: Clone Repository

```bash
# Navigate to your development directory
cd C:\Users\<username>\dev

# Clone the repository
git clone https://github.com/saicon/tradetool_middleware.git

# Navigate to project directory
cd tradetool_middleware

# Verify current branch
git branch -a
```

### Step 2: Build Project

```bash
# Navigate to project root
cd tradetool_middleware

# Clean and build all modules
mvn clean install

# Expected output (successful build):
# [INFO] CallCard Microservice Parent ......................... SUCCESS [  1.234 s]
# [INFO] CallCard Entity Layer ................................ SUCCESS [  5.234 s]
# [INFO] CallCard WS API Layer ................................ SUCCESS [  3.234 s]
# [INFO] CallCard Components Layer ............................ SUCCESS [  8.234 s]
# [INFO] CallCard Service Layer ............................... SUCCESS [  9.234 s]
# [INFO] CallCard Server WS ................................... SUCCESS [ 12.234 s]
# [INFO] ------------------------------------------------------------------------
# [INFO] BUILD SUCCESS
```

### Step 3: Verify Installation

```bash
# Run tests
mvn test

# Check dependency resolution
mvn dependency:resolve

# Generate project documentation (optional)
mvn javadoc:javadoc
```

### Step 4: Run Application

```bash
# Start the application
mvn spring-boot:run -pl CallCard_Server_WS

# Expected output:
# . ____ _ __ _ _
# / \\ / ___'_ __ _ _(_)_ __ __ _ \ \ \ \
# ( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
# \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
# '  |____| .__|_| |_|_| |_|\__, | / / / /
# =========|_|==============|___/=/_/_/_/
# 2023-12-22 10:15:30.123  INFO 12345 --- [main] c.s.g.c.w.CallCardServerApplication     : Started CallCardServerApplication in 5.234 seconds
```

### Step 5: Verify Web Services

Once the application is running, verify the web services:

**SOAP Web Service WSDL**:
```
http://localhost:8080/callcard/cxf/CallCardService?wsdl
```

**Actuator Health Check**:
```
http://localhost:8080/callcard/actuator/health
```

**Swagger API Documentation** (if enabled):
```
http://localhost:8080/callcard/swagger-ui.html
```

---

## Git Workflow

### Branching Strategy

The project follows Git Flow branching model:

```
main (production)
  ↑
release/*
  ↑
develop (integration)
  ↑
feature/*, bugfix/*, hotfix/*
```

### Common Git Workflows

#### Creating Feature Branch

```bash
# Update develop branch
git checkout develop
git pull origin develop

# Create feature branch
git checkout -b feature/callcard-implementation

# Make changes...
git add .
git commit -m "Add CallCard implementation"
git push origin feature/callcard-implementation

# Create Pull Request on GitHub/GitLab
```

#### Updating Local Branch

```bash
# Fetch latest changes
git fetch origin

# Update current branch
git pull origin <branch-name>

# Or rebase (for cleaner history)
git rebase origin/<branch-name>
```

#### Merging and Cleanup

```bash
# Merge feature to develop (via Pull Request recommended)
git checkout develop
git pull origin develop
git merge --no-ff feature/callcard-implementation
git push origin develop

# Delete feature branch locally
git branch -d feature/callcard-implementation

# Delete feature branch remotely
git push origin --delete feature/callcard-implementation
```

#### Viewing History

```bash
# View commit history
git log --oneline --graph --all

# View changes in current branch
git log origin/develop..HEAD

# View status
git status

# View diff
git diff
git diff --cached
```

### Git Configuration

```bash
# Configure your identity
git config --global user.name "Your Name"
git config --global user.email "your.email@example.com"

# Set default editor
git config --global core.editor "code"  # VS Code

# Configure line endings (Windows)
git config --global core.autocrlf true

# Configure line endings (macOS/Linux)
git config --global core.autocrlf input
```

---

## Code Style and Formatting

### Code Style Guidelines

1. **Indentation**: 4 spaces (no tabs)
2. **Line Length**: Maximum 120 characters
3. **Naming Conventions**:
   - Classes: PascalCase (e.g., `CallCardService`)
   - Methods: camelCase (e.g., `getCallCardById`)
   - Constants: UPPER_SNAKE_CASE (e.g., `DEFAULT_TIMEOUT`)
   - Variables: camelCase (e.g., `userId`)

4. **Javadoc**:
   - Document all public classes and methods
   - Include `@param`, `@return`, `@throws` tags
   - Example:
     ```java
     /**
      * Retrieves a CallCard by ID.
      *
      * @param cardId the ID of the CallCard to retrieve
      * @return the CallCard if found, null otherwise
      * @throws IllegalArgumentException if cardId is null or empty
      */
     public CallCard getCallCardById(String cardId) {
         // implementation
     }
     ```

### Automatic Code Formatting

#### IntelliJ IDEA

1. Open IDE Settings: `File` > `Settings` > `Editor` > `Code Style` > `Java`
2. Choose Scheme: `Project`
3. Configure:
   - Line separator: `Unix (\n)`
   - Indentation: `Spaces`, `4`
   - Keep on one line: Uncheck unnecessary options

4. Format code:
   - Select code or file
   - Right-click > `Reformat Code`
   - Or: `Ctrl+Alt+L` (Windows) / `Cmd+Alt+L` (macOS)

5. Organize imports:
   - Right-click > `Optimize Imports`
   - Or: `Ctrl+Alt+O` (Windows) / `Cmd+Alt+O` (macOS)

#### Eclipse IDE

1. Go to: `Window` > `Preferences` > `Java` > `Code Style` > `Formatter`
2. Click `New` to create custom profile
3. Configure formatter rules
4. Format code:
   - Select code
   - Right-click > `Source` > `Format`
   - Or: `Ctrl+Shift+F`

#### VS Code

Create `.vscode/settings.json`:
```json
{
    "editor.defaultFormatter": "redhat.java",
    "[java]": {
        "editor.formatOnSave": true,
        "editor.defaultFormatter": "redhat.java"
    },
    "editor.tabSize": 4,
    "editor.insertSpaces": true,
    "editor.formatOnPaste": true
}
```

### Checkstyle Configuration

Create `checkstyle.xml` in project root:

```xml
<?xml version="1.0"?>
<!DOCTYPE module PUBLIC
    "-//Checkstyle//DTD Checkstyle Configuration 1.3//EN"
    "https://checkstyle.org/dtds/configuration_1_3.dtd">
<module name="Checker">
    <property name="charset" value="UTF-8"/>
    <property name="severity" value="warning"/>

    <module name="LineLength">
        <property name="max" value="120"/>
        <property name="ignorePattern" value="^package.*|^import.*|a href|href|http://|https://"/>
    </module>

    <module name="FileTabCharacter">
        <property name="eachLine" value="true"/>
    </module>

    <module name="TreeWalker">
        <module name="ConstantName">
            <property name="format" value="^[A-Z][A-Z0-9]*(_[A-Z0-9]+)*$"/>
        </module>

        <module name="LocalVariableName">
            <property name="format" value="^[a-z]([a-zA-Z0-9]*)?$"/>
        </module>

        <module name="PackageName">
            <property name="format" value="^[a-z]+(\.[a-z][a-z0-9]*)*$"/>
        </module>

        <module name="MethodName">
            <property name="format" value="^[a-z][a-zA-Z0-9]*$"/>
        </module>

        <module name="TypeName">
            <property name="format" value="^[A-Z][a-zA-Z0-9]*$"/>
        </module>

        <module name="TodoComment"/>
        <module name="UpperEll"/>
    </module>
</module>
```

Run checkstyle in Maven:
```bash
mvn checkstyle:check
```

---

## Pre-commit Hooks

### Setup Git Hooks

Create `.git/hooks/pre-commit` (executable):

#### Windows (PowerShell)

Create `.git/hooks/pre-commit.ps1`:
```powershell
# Prevent commits to main branch
$branch = git rev-parse --abbrev-ref HEAD
if ($branch -eq "main") {
    Write-Host "Cannot commit directly to main branch"
    exit 1
}

# Run tests before commit
Write-Host "Running tests before commit..."
mvn test -q

if ($LASTEXITCODE -ne 0) {
    Write-Host "Tests failed. Commit aborted."
    exit 1
}

# Run code quality checks
Write-Host "Running code quality checks..."
mvn checkstyle:check -q

if ($LASTEXITCODE -ne 0) {
    Write-Host "Code quality issues found. Commit aborted."
    exit 1
}

Write-Host "Pre-commit checks passed!"
exit 0
```

Then create wrapper `.git/hooks/pre-commit`:
```bash
#!/bin/sh
powershell.exe -ExecutionPolicy Bypass -File "./.git/hooks/pre-commit.ps1"
```

#### macOS/Linux

Create `.git/hooks/pre-commit` (executable):
```bash
#!/bin/bash
set -e

# Prevent commits to main branch
BRANCH=$(git rev-parse --abbrev-ref HEAD)
if [ "$BRANCH" = "main" ]; then
    echo "Cannot commit directly to main branch"
    exit 1
fi

# Run tests before commit
echo "Running tests before commit..."
mvn test -q || { echo "Tests failed. Commit aborted."; exit 1; }

# Run code quality checks
echo "Running code quality checks..."
mvn checkstyle:check -q || { echo "Code quality issues found. Commit aborted."; exit 1; }

# Format code
echo "Formatting code..."
mvn spotless:apply -q

echo "Pre-commit checks passed!"
exit 0
```

Make hook executable:
```bash
chmod +x .git/hooks/pre-commit
```

### Husky (Alternative - Node-based)

If using Husky for better cross-platform support:

```bash
# Install husky
npm install husky --save-dev

# Install npm-run-all for parallel script execution
npm install npm-run-all --save-dev

# Setup husky
npx husky install

# Add pre-commit hook
npx husky add .husky/pre-commit "mvn test && mvn checkstyle:check"
```

---

## Debugging Setup

### IntelliJ IDEA Debugging

1. **Set Breakpoint**:
   - Click on line number in editor left margin
   - Red circle indicates breakpoint

2. **Run in Debug Mode**:
   - Go to: `Run` > `Debug` (or press `Shift+F9`)
   - Or click debug icon in run configuration

3. **Debug Controls**:
   - **F8**: Step over
   - **F7**: Step into
   - **Shift+F8**: Step out
   - **F9**: Resume execution
   - **Ctrl+F8**: Toggle breakpoint

4. **Inspect Variables**:
   - Hover over variable to see value
   - Use `Evaluate Expression` (Alt+F9)
   - Use `Watches` panel to track specific variables

### Remote Debugging

#### Enable Remote Debugging in Application

In `CallCard_Server_WS`, modify Spring Boot startup:

```bash
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005"
```

Or in application.properties:
```properties
spring.boot.jvm.args=-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005
```

#### IntelliJ IDEA Remote Configuration

1. Go to: `Run` > `Edit Configurations`
2. Click `+` > `Remote`
3. Set name: `CallCard Remote Debug`
4. Host: `localhost`
5. Port: `5005`
6. Click `OK`
7. Click debug icon to start remote debugging

#### Eclipse Remote Configuration

1. Go to: `Run` > `Debug Configurations...`
2. Right-click `Remote Java Application` > `New`
3. Name: `CallCard Remote`
4. Host: `localhost`
5. Port: `5005`
6. Click `Debug`

### Log-Based Debugging

#### Enable Debug Logging

Set environment variable:
```bash
LOGGING_LEVEL_COM_SAICON=DEBUG
LOGGING_LEVEL_HIBERNATE=DEBUG
```

Or in `application.properties`:
```properties
logging.level.com.saicon=DEBUG
logging.level.org.hibernate.SQL=DEBUG
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE
```

#### View Logs

Logs are typically at: `logs/callcard.log`

```bash
# Real-time log viewing (Windows)
Get-Content -Path logs/callcard.log -Wait

# Real-time log viewing (macOS/Linux)
tail -f logs/callcard.log
```

---

## Recommended Plugins and Tools

### IntelliJ IDEA Plugins

Install from: `File` > `Settings` > `Plugins` > `Marketplace`

1. **SonarLint** (v6.0+)
   - Real-time code quality analysis
   - Identifies bugs and code smells

2. **CheckStyle-IDEA** (v5.54+)
   - Code style verification
   - Enforces project conventions

3. **Spotbugs** (v4.0+)
   - Finds potential bugs in code
   - Analyzes bytecode for common patterns

4. **Maven Helper** (v3.1.8+)
   - Convenient Maven command shortcuts
   - Dependency tree visualization
   - Quick run/debug configurations

5. **Database Navigator** (v4.7+)
   - Connect to SQL Server
   - Browse and manage database schema
   - Run SQL queries

6. **RestClient** (v1.17+)
   - Test REST APIs directly in IDE
   - Send HTTP requests and view responses

7. **Kubernetes** (by JetBrains)
   - If deploying to Kubernetes

8. **EnvFile** (v6.0+)
   - Load environment variables from .env files

### IDE Extensions (VS Code)

1. **Extension Pack for Java** (by Microsoft)
   - Language support, debugging, testing

2. **Spring Boot Extension Pack** (by Pivotal)
   - Spring Boot specific features

3. **Maven for Java** (by Microsoft)
   - Maven project support

4. **REST Client** (by Huachao Mao)
   - Test APIs from VS Code

5. **SQL Server** (mssql) (by Microsoft)
   - SQL Server/Azure SQL Server support

### Standalone Tools

1. **Postman** (API Testing)
   ```
   Download: https://www.postman.com/downloads/
   Use for: Testing SOAP and REST APIs
   ```

2. **SQL Server Management Studio (SSMS)**
   ```
   Download: https://docs.microsoft.com/en-us/sql/ssms/download-sql-server-management-studio-ssms
   Use for: Database management and queries
   ```

3. **Git Extensions** (Version Control GUI)
   ```
   Download: https://gitextensions.github.io/
   Use for: Visual Git repository management
   ```

4. **Insomnia** (API Testing)
   ```
   Download: https://insomnia.rest/
   Use for: REST API testing alternative to Postman
   ```

5. **PlantUML** (Documentation)
   ```
   Download: https://plantuml.com/download
   Use for: Creating architecture and sequence diagrams
   ```

---

## Troubleshooting

### Maven Build Issues

#### Problem: "Maven not found" or "mvn command not recognized"

**Solution**:
```bash
# Verify Maven installation
mvn -version

# Add to PATH if not found
# Windows: Edit System Environment Variables
# Add to PATH: C:\apache-maven-3.8.6\bin

# macOS/Linux: Add to ~/.bash_profile or ~/.zshrc
export MAVEN_HOME=/opt/maven
export PATH="$MAVEN_HOME/bin:$PATH"
```

#### Problem: "Could not find goal 'spring-boot:run'"

**Solution**:
```bash
# Ensure Spring Boot plugin is in parent POM
# Check pom.xml for:
# <plugin>
#     <groupId>org.springframework.boot</groupId>
#     <artifactId>spring-boot-maven-plugin</artifactId>
# </plugin>

# Update plugin version
mvn -U clean install
```

#### Problem: "OutOfMemoryError" during build

**Solution**:
```bash
# Increase Maven heap size
set MAVEN_OPTS=-Xmx1024m  # Windows
export MAVEN_OPTS=-Xmx1024m  # macOS/Linux

# Run build
mvn clean install
```

### Database Connection Issues

#### Problem: "Cannot connect to database"

**Solution**:
1. Verify SQL Server is running:
   ```bash
   sqlcmd -S localhost\SQLEXPRESS -U sa
   ```

2. Check connection string in `application.properties`:
   ```properties
   spring.datasource.url=jdbc:sqlserver://localhost\SQLEXPRESS:1433;databaseName=callcard_db
   spring.datasource.username=callcard_user
   spring.datasource.password=CallCard@Dev123
   ```

3. Verify database and user exist:
   ```bash
   sqlcmd -S localhost\SQLEXPRESS -U sa -P <password>
   > SELECT DB_NAME() AS [Current Database];
   > GO
   ```

#### Problem: "Authentication failed"

**Solution**:
1. Verify credentials:
   - Username: `callcard_user`
   - Password: Matches what's in application.properties

2. Reset password:
   ```bash
   sqlcmd -S localhost\SQLEXPRESS -U sa -P <sa_password>
   > USE callcard_db;
   > ALTER LOGIN callcard_user WITH PASSWORD = 'NewPassword123';
   > GO
   ```

### IDE Configuration Issues

#### Problem: IntelliJ IDEA doesn't recognize Maven project

**Solution**:
1. Go to: `File` > `Invalidate Caches` > Invalidate and Restart
2. Go to: `File` > `Project Structure`
3. Verify JDK is set correctly
4. Re-import project: `File` > `Open` > Select `tradetool_middleware`

#### Problem: Missing Spring Boot run configuration

**Solution**:
1. Go to: `Run` > `Edit Configurations`
2. Click `+` > `Spring Boot`
3. Main class: `com.saicon.games.callcard.ws.CallCardServerApplication`
4. Use classpath: `CallCard_Server_WS`
5. Click `OK` and run

### Java Version Issues

#### Problem: "Unsupported class version 52.0"

**Solution**:
This means compiled code is for Java 8 but running on older version.
```bash
# Verify Java version
java -version

# Should output: openjdk version "1.8.0_xxx"

# If different, set JAVA_HOME to correct version
set JAVA_HOME=C:\Program Files\Java\jdk1.8.0_[version]
```

### Testing Issues

#### Problem: Tests fail with database connection error

**Solution**:
1. Ensure test database exists and is accessible
2. Check `application-test.properties` settings
3. Run tests with verbose output:
   ```bash
   mvn test -X
   ```

#### Problem: "Cannot find test database"

**Solution**:
Create in-memory H2 database for testing in `application-test.properties`:
```properties
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
```

---

## Support and Resources

### Documentation Files in Project

- `pom.xml` - Project dependencies and build configuration
- `MIGRATION_GUIDE.md` - Database migration information
- `BACKUP_AND_RECOVERY.md` - Backup strategies

### External Resources

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Apache CXF Documentation](https://cxf.apache.org/)
- [Maven Documentation](https://maven.apache.org/guides/)
- [JPA/Hibernate Documentation](https://hibernate.org/orm/documentation/)
- [SQL Server Documentation](https://docs.microsoft.com/en-us/sql/)

### Getting Help

When encountering issues:
1. Check project documentation files
2. Review build logs carefully
3. Check IDE console for error messages
4. Enable debug logging for detailed information
5. Search project history in Git for similar issues

---

## Quick Start Checklist

Use this checklist to verify your setup is complete:

- [ ] JDK 1.8 installed and JAVA_HOME set
- [ ] Maven 3.6.3+ installed and in PATH
- [ ] Git installed and configured
- [ ] IDE installed (IntelliJ IDEA, Eclipse, or VS Code)
- [ ] SQL Server 2008+ running
- [ ] callcard_db database created
- [ ] callcard_user database user created
- [ ] Project cloned from Git repository
- [ ] `mvn clean install` completed successfully
- [ ] Spring Boot application starts with `mvn spring-boot:run -pl CallCard_Server_WS`
- [ ] http://localhost:8080/callcard/actuator/health returns 200 OK
- [ ] SOAP WSDL accessible at http://localhost:8080/callcard/cxf/CallCardService?wsdl
- [ ] IDE autocomplete and debugging working
- [ ] Git workflow and pre-commit hooks configured

---

**Document Version**: 1.0.0
**Last Updated**: 2025-12-22
**Status**: Ready for Development
