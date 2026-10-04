# ExamForge - Requirements & Setup Guide

## System Requirements

### Software
- JDK 17 or later
- MySQL 8.0 or later
- MySQL JDBC Connector (mysql-connector-j-8.x.jar)
- OS: Windows 10/11, Linux, or macOS

### Hardware
- RAM: 4 GB minimum
- Disk: 500 MB free space
- Display: 1024x768 minimum resolution

---

## Setup Instructions

### Step 1: Install MySQL
Download and install MySQL 8.0 from https://dev.mysql.com/downloads/mysql/

### Step 2: Create Database
Open MySQL Workbench or terminal and run:
```sql
source C:/Users/himes/OneDrive/Desktop/SEM3/ExamForge/sql/schema.sql
```
Or copy-paste the contents of `sql/schema.sql` into your MySQL client and execute.

### Step 3: Download MySQL JDBC Driver
Download from: https://dev.mysql.com/downloads/connector/j/
Extract the .jar file and place it in the `lib/` folder.

### Step 4: Configure Database Credentials
Edit `src/com/examforge/config/DatabaseConfig.java` if your MySQL username/password differs from:
- Username: root
- Password: root
- Database: examforge

### Step 5: Compile and Run
Option A - Using build scripts:
```
build.bat
run.bat
```

Option B - Using IDE (IntelliJ/Eclipse):
1. Import as Java project
2. Add mysql-connector-j jar to classpath
3. Run com.examforge.main.ExamForgeApp

Option C - Command line:
```
javac -d out -sourcepath src src/com/examforge/main/ExamForgeApp.java
java -cp "out;lib/*" com.examforge.main.ExamForgeApp
```

---

## Default Login Credentials

| Username  | Password   | Role    |
|-----------|------------|---------|
| admin     | admin123   | Teacher |
| student1  | student123 | Student |

---

## Features Implemented

### Teacher Module
1. Login/Logout
2. Question Bank Management (Add, Edit, Deactivate, Filter)
3. Blueprint Creation (Sections, Constraints, Exposure Policy)
4. Blueprint Validation (Structure + Cross-constraint)
5. Feasibility Analysis (Mark combinations, constraint intersection)
6. Paper Generation (Constraint-aware selection + optimization)
7. Paper Validation (Independent validator)
8. Paper Finalization (Transaction-safe with exposure recording)
9. Exposure Analysis (Usage tracking, levels, penalties)
10. Reports & History

### Student Module
1. Login/Logout
2. Random Practice Quiz
3. Balanced Practice Quiz (even difficulty distribution)
4. Weak-Area Practice Quiz (focuses on weak topics)
5. Quiz Submission with Scoring
6. Results Viewing with detailed answer review
7. Performance Analysis

---

## Java Concepts Demonstrated

- Classes & Objects: 50+ classes across all layers
- Encapsulation: Private fields with getters/setters in all models
- Inheritance: Exception hierarchy (ExamForgeException base)
- Polymorphism: Strategy pattern for paper generation
- Abstraction: DAO interfaces, PaperGenerationStrategy interface
- Interfaces: 6 DAO interfaces + 1 Strategy interface
- Packages: 13 organized packages
- Exception Handling: 5 custom exception types with error codes
- JDBC: PreparedStatement, ResultSet, transactions, auto-generated keys
- MySQL: 10 tables with foreign keys, indexes
- Swing GUI: CardLayout, GridBagLayout, JTabbedPane, JTable, JDialog
- Multithreading: SwingWorker for async login, generation, feasibility

---

## Project File Count: 66 Java source files + 1 SQL schema + 2 batch scripts
