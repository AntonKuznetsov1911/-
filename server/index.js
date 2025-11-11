const express = require('express');
const cors = require('cors');
const path = require('path');

const app = express();
const PORT = process.env.PORT || 3000;

// Middleware
app.use(cors());
app.use(express.json());
app.use(express.static(path.join(__dirname, '../public')));

// Простое хранилище для лидерборда (в production использовать БД)
let leaderboard = [];

// Роуты
app.get('/', (req, res) => {
    res.sendFile(path.join(__dirname, '../public/index.html'));
});

// API для получения лидерборда
app.get('/api/leaderboard', (req, res) => {
    const topScores = leaderboard
        .sort((a, b) => b.score - a.score)
        .slice(0, 10);
    res.json(topScores);
});

// API для отправки счёта
app.post('/api/score', (req, res) => {
    const { userId, username, score } = req.body;

    if (!userId || !score) {
        return res.status(400).json({ error: 'Missing required fields' });
    }

    // Проверяем, есть ли уже запись для этого пользователя
    const existingIndex = leaderboard.findIndex(entry => entry.userId === userId);

    if (existingIndex !== -1) {
        // Обновляем, если новый счёт больше
        if (score > leaderboard[existingIndex].score) {
            leaderboard[existingIndex].score = score;
            leaderboard[existingIndex].username = username;
        }
    } else {
        // Добавляем новую запись
        leaderboard.push({ userId, username, score });
    }

    res.json({ success: true, message: 'Score saved' });
});

// Здоровье сервера
app.get('/health', (req, res) => {
    res.json({ status: 'ok', timestamp: new Date().toISOString() });
});

// Запуск сервера
app.listen(PORT, () => {
    console.log(`🚀 Server running on http://localhost:${PORT}`);
    console.log(`📱 Telegram Mini App ready!`);
    console.log(`\nДля тестирования откройте: http://localhost:${PORT}`);
});

// Обработка ошибок
app.use((err, req, res, next) => {
    console.error(err.stack);
    res.status(500).json({ error: 'Something went wrong!' });
});

module.exports = app;
