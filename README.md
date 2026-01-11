# 📊 Smart Expense Tracker

Smart Expense Tracker is an **Android app** built in **Java** that helps users track daily expenses, categorize them, visualize spending patterns, and get basic insights. It uses **Room Database** for offline storage and **MPAndroidChart** for pie chart visualizations.

This project was developed as a hands-on app for learning Android app development, database integration, chart visualization, and AI-based insights.

---

## 🚀 Features

✔ Add expenses with **amount**, **category**, **date**, and **note**  
✔ View total expenses and category-wise breakdown  
✔ Visualize spending patterns with **pie charts**  
✔ Offline data storage using **Room Database**  
✔ Option to add AI-powered analysis (e.g., spending patterns, suggestions)  
✔ Simple & intuitive UI

---

## 🧠 AI Integration

The app supports **AI-powered insights** such as:
- Smart suggestions
- Spending analysis with natural language summaries

⚠️ If you’re getting **HTTP 429 Too Many Requests**, it is because the AI API is rate-limited or quota has been exceeded.  
Solution:
- Call AI endpoint only when needed (e.g., single button press)
- Cache AI response
- Add delay between requests

This prevents frequent unnecessary calls.

---

## 🗂 Tech Stack

| Component | Tech |
|-----------|------|
| Language | Java |
| UI | Android XML |
| Database | Room Persistence Library |
| Charts | MPAndroidChart |
| Build System | Gradle |
| Tools | Android Studio |
| Optional | OpenAI / Gemini API for AI |

---

## 🛠 How to Run

1. **Clone the repository**
   ```bash
   git clone https://github.com/DEVENDRAP7/Smart-Expense-Tracker.git

2. Open in Android Studio
    File → Open → Choose project folder

3. Build & Run
    Connect Android device or launch emulator
    Click Run

4. AI Setup (optional)
    Add your AI API key in a local.properties or secure config file
    Make sure to throttle requests to avoid HTTP 429 errors
