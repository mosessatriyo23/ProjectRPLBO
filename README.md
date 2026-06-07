<div align="center">

```
██████╗ ██╗███╗   ██╗███╗   ██╗ █████╗  ██████╗██╗     ███████╗
██╔══██╗██║████╗  ██║████╗  ██║██╔══██╗██╔════╝██║     ██╔════╝
██████╔╝██║██╔██╗ ██║██╔██╗ ██║███████║██║     ██║     █████╗  
██╔═══╝ ██║██║╚██╗██║██║╚██╗██║██╔══██║██║     ██║     ██╔══╝  
██║     ██║██║ ╚████║██║ ╚████║██║  ██║╚██████╗███████╗███████╗
╚═╝     ╚═╝╚═╝  ╚═══╝╚═╝  ╚═══╝╚═╝  ╚═╝ ╚═════╝╚══════╝╚══════╝
```

**A smart, AI-powered task management desktop app — built with JavaFX.**

![Java](https://img.shields.io/badge/Java-17-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![JavaFX](https://img.shields.io/badge/JavaFX-17-0066CC?style=for-the-badge&logo=java&logoColor=white)
![SQLite](https://img.shields.io/badge/SQLite-003B57?style=for-the-badge&logo=sqlite&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white)
![Claude AI](https://img.shields.io/badge/Claude_AI-CC785C?style=for-the-badge&logo=anthropic&logoColor=white)

</div>

---

## 🍍 What is Pinnacle?

**Pinnacle** is a full-featured desktop task management application developed as part of the **Object-Oriented Software Engineering** course at **Universitas Kristen Duta Wacana (UKDW)**. It goes beyond a typical CRUD app — Pinnacle integrates **Claude AI** to help users analyze tasks, get productivity suggestions, and auto-detect priority.

Built with a clean multi-layer architecture that strictly separates business logic, data access, and presentation — engineered for maintainability and extensibility.

---

## ✨ Features

### 📋 Task Management
- Create, edit, and delete tasks with title, description, deadline, and category
- Real-time task status tracking (Active / Completed / Archived)
- Priority flagging with visual indicators (⭐)
- Persistent local storage via SQLite

### 🗂️ Category System
- Create and manage custom task categories
- Assign tasks to categories dynamically
- Category-based task filtering and overview

### 🤖 AI Assistant (Claude API)
- **Task Analysis** — get smart suggestions on how to approach a task effectively
- **Auto-Priority Detection** — AI recommends whether a task should be marked as priority
- **Category Description Generator** — AI writes category descriptions based on the name
- **Description Improver** — AI refines existing category descriptions

### 📊 Dashboard & Overview
- Home dashboard with task summary, priority ring, and category list
- Interactive calendar with deadline markers
- Real-time clock display

### 🔐 Auth & Session
- User registration and login
- Session management with `SessionHelper`
- Per-user data isolation

---

## 🏗️ Architecture

```
src/
├── controller/         # JavaFX controllers (UI logic)
│   ├── ToDoListController.java
│   ├── SemuaTugasController.java
│   ├── KategoriController.java
│   ├── PrioritasController.java
│   ├── TambahTugasController.java
│   ├── EditTugasController.java
│   ├── FormKategoriController.java
│   ├── EditKategoriController.java
│   └── CustomDialogController.java
│
├── dao/                # Data Access Objects (database layer)
│   ├── TaskDao.java            (interface)
│   ├── TaskDAOManager.java     (implementation)
│   ├── KategoriDao.java        (interface)
│   └── KategoriDAOManager.java (implementation)
│
├── model/              # Domain models
│   ├── Task.java
│   └── Kategori.java
│
└── util/               # Utilities
    ├── AiService.java      (Claude API integration)
    ├── DatabaseUtil.java   (SQLite connection)
    ├── DialogUtil.java     (custom dialog factory)
    └── SessionHelper.java  (user session)
```

---

## 🛠️ Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 17 |
| UI Framework | JavaFX 17 + FXML |
| Build Tool | Apache Maven |
| Database | SQLite (via JDBC) |
| AI Integration | Anthropic Claude API (`claude-haiku-4-5`) |
| IDE | IntelliJ IDEA + Scene Builder |
| Security | BCrypt password hashing |

---

## 🚀 Getting Started

### Prerequisites
- Java 17+
- Maven 3.8+
- IntelliJ IDEA (recommended)

### Setup

```bash
# 1. Clone the repository
git clone https://github.com/YOUR_USERNAME/To-DoListFix.git
cd To-DoListFix

# 2. Set your Anthropic API key (for AI features)
# Windows
set ANTHROPIC_API_KEY=your_api_key_here

# macOS/Linux
export ANTHROPIC_API_KEY=your_api_key_here

# 3. Build and run
mvn clean javafx:run
```

> **Note:** AI features require a valid Anthropic API key. Without it, the app runs normally but AI suggestions will be disabled.

---

## 🖼️ Screenshots

> _Add screenshots of your app here_

| Home Dashboard | Task Management | AI Assistant |
|---|---|---|
| `screenshot_home.png` | `screenshot_tasks.png` | `screenshot_ai.png` |

---

## 🎓 Course Context

This project was built for the **Object-Oriented Software Engineering (RPLBO)** course at **UKDW**, demonstrating:

- **OOP Principles** — inheritance, encapsulation, and polymorphism across a structured class hierarchy
- **Design Patterns** — DAO pattern for data access abstraction, MVC separation with JavaFX
- **Software Engineering Practices** — multi-branch Git workflow, layered architecture, interface-based programming

---

## 👤 Developer

**Moses Satriyo Gentur Pinandhito**
Informatics — Universitas Kristen Duta Wacana
NIM: 71231016
Role: AI Engineer & Backend Developer

---

## 📄 License

This project is developed for academic purposes as part of coursework at UKDW.

---

<div align="center">
  <sub>Built with ☕ Java, 🍍 Pinnacle spirit, and a little help from 🤖 Claude AI</sub>
</div>
