# AI-Powered Customer Support Chatbot

A professional full-stack Java application that utilizes **Stanford CoreNLP** for deep-learning-based sentiment analysis and features a **premium React UI**.

## 🚀 Key Features
- **ML Sentiment Analysis**: Uses Stanford CoreNLP to detect sentiment (Positive, Negative, Neutral) with high accuracy.
- **Premium UI**: Modern glassmorphic design with smooth Framer Motion animations and Lucide icons.
- **Persistent History**: Stores all chat interactions and sentiment data in a MySQL database.
- **Real-time Feedback**: Dynamic sentiment badges and responsive bot replies.

## 🛠️ Technologies Used
- **Frontend**: React 18, TypeScript, Vite, Framer Motion, Lucide React, Axios.
- **Backend**: Java 17+, Spring Boot 3, Spring Data JPA, **Stanford CoreNLP**.
- **Database**: MySQL.

## 📂 Project Structure
```
ai-support-chatbot/
├── backend/                 # Spring Boot backend
│   ├── src/main/java/com/example/aisupportchatbot/
│   │   ├── controller/      # REST Endpoints
│   │   ├── model/           # JPA Entities
│   │   ├── repository/      # Data Access
│   │   ├── service/         # Sentiment Analysis Logic
│   │   └── AiSupportChatbotApplication.java
│   └── pom.xml              # Maven dependencies (CoreNLP, Lombok, MySQL)
├── frontend/                # React frontend
│   ├── src/
│   │   ├── components/      # ChatWindow component
│   │   ├── services/        # API service (Axios)
│   │   ├── App.tsx          # Main entry
│   │   └── index.css        # Premium Design System
│   └── package.json
└── database/
    └── schema.sql           # MySQL Table Definition
```

## ⚙️ Setup Instructions

### 1. Database Setup
1. Ensure MySQL is running.
2. Create the database:
   ```sql
   CREATE DATABASE chatbot_db;
   ```
3. Run the schema script:
   ```bash
   mysql -u root -p chatbot_db < database/schema.sql
   ```

### 2. Backend Setup
1. Navigate to the `backend` folder.
2. Update `src/main/resources/application.properties` with your MySQL credentials:
   ```properties
   spring.datasource.username=root
   spring.datasource.password=your_password
   ```
3. Run the application (requires Maven):
   ```bash
   mvn spring-boot:run
   ```
   *Note: On the first run, Maven will download the Stanford CoreNLP models (~400MB), which may take a few minutes.*

### 3. Frontend Setup
1. Navigate to the `frontend` folder.
2. Install dependencies:
   ```bash
   npm install
   ```
3. Start the development server:
   ```bash
   npm run dev
   ```

## 🧪 Testing the System
1. Open `http://localhost:5173` in your browser.
2. Send a message like: *"I am extremely happy with this product!"*
3. Observe the **POSITIVE** sentiment badge and the bot's response.
4. Try a negative message like: *"This service is terrible and slow."*
5. Observe the **NEGATIVE** sentiment badge.

## 📝 License
MIT License. Created with ❤️ for AI support automation.