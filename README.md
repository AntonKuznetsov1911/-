# 🎮 Telegram Mini App - Игра Кликер

Простая игра-кликер, разработанная как Telegram Mini App с использованием Telegram WebApp API.

## 🌟 Особенности

- 💎 Интерактивная игра-кликер
- 🎨 Адаптивный дизайн с поддержкой темы Telegram
- 🤖 Система автоматических кликов
- ⚡ Улучшения и апгрейды
- 📊 Отслеживание статистики
- 💾 Автоматическое сохранение прогресса
- 📱 Поддержка тактильной обратной связи (вибрация)
- 🏆 Система рекордов

## 🚀 Быстрый старт

### Установка зависимостей

```bash
npm install
```

### Запуск сервера

```bash
npm start
```

Сервер запустится на `http://localhost:3000`

## 📁 Структура проекта

```
.
├── public/              # Клиентская часть
│   ├── index.html      # Основной HTML файл
│   ├── style.css       # Стили игры
│   └── game.js         # Логика игры
├── server/             # Серверная часть
│   └── index.js        # Express сервер
├── package.json        # Зависимости проекта
└── README.md          # Документация
```

## 🎯 Как играть

1. **Кликайте** на большую кнопку с алмазом, чтобы набрать очки
2. **Покупайте улучшения** для увеличения эффективности:
   - 🤖 **Авто-клик** - автоматически добавляет очки каждую секунду
   - ⚡ **Мощность клика** - увеличивает количество очков за клик
   - 🚀 **Турбо режим** - значительно увеличивает автоматический доход
3. **Отслеживайте статистику** - следите за кликами в секунду и общим количеством кликов
4. **Побейте свой рекорд** - старайтесь набрать максимальное количество очков!

## 🔧 Интеграция с Telegram

### Создание бота

1. Найдите [@BotFather](https://t.me/BotFather) в Telegram
2. Создайте нового бота командой `/newbot`
3. Следуйте инструкциям для настройки имени и username

### Настройка Mini App

1. Используйте команду `/newapp` в BotFather
2. Выберите своего бота
3. Введите название приложения
4. Загрузите иконку (необязательно)
5. Укажите URL вашего приложения (для разработки можно использовать ngrok)
6. Добавьте описание

### Тестирование локально с ngrok

```bash
# Установите ngrok (если ещё не установлен)
# https://ngrok.com/download

# Запустите туннель
ngrok http 3000

# Используйте полученный HTTPS URL в настройках Mini App
```

## 🌐 Деплой

### Vercel

```bash
npm install -g vercel
vercel
```

### Heroku

```bash
# Создайте Procfile
echo "web: node server/index.js" > Procfile

# Деплой
git push heroku main
```

### Railway

1. Подключите репозиторий к [Railway](https://railway.app)
2. Railway автоматически определит Node.js проект
3. Приложение будет развернуто автоматически

## 🎨 Кастомизация

### Изменение темы

Редактируйте `public/style.css` для изменения цветов и стилей. Приложение автоматически адаптируется под тему Telegram.

### Добавление новых улучшений

В файле `public/game.js` добавьте новые улучшения в объект `upgrades`:

```javascript
upgrades = {
    4: { owned: 0, cost: 500, effect: 10, type: 'auto' }
};
```

И добавьте HTML в `public/index.html`:

```html
<div class="upgrade-item" id="upgrade4">
    <div class="upgrade-info">
        <span class="upgrade-name">🔥 Новое улучшение</span>
        <span class="upgrade-desc">Описание</span>
    </div>
    <button class="upgrade-button" onclick="buyUpgrade(4, 500)">
        Купить (500)
    </button>
</div>
```

## 📊 API

### Получение лидерборда

```http
GET /api/leaderboard
```

### Отправка счёта

```http
POST /api/score
Content-Type: application/json

{
  "userId": "123456",
  "username": "Player",
  "score": 1000
}
```

## 🔐 Безопасность

Для production окружения рекомендуется:

1. Валидировать данные от Telegram через `initData`
2. Использовать базу данных вместо in-memory хранилища
3. Добавить rate limiting
4. Проверять подлинность запросов от Telegram

Пример валидации:

```javascript
const crypto = require('crypto');

function validateTelegramWebAppData(initData, botToken) {
    // Реализация валидации согласно документации Telegram
    // https://core.telegram.org/bots/webapps#validating-data-received-via-the-mini-app
}
```

## 📚 Ресурсы

- [Telegram Mini Apps Documentation](https://core.telegram.org/bots/webapps)
- [Telegram Bot API](https://core.telegram.org/bots/api)
- [Express.js Documentation](https://expressjs.com/)

## 🤝 Разработка

### Локальная разработка

```bash
# Установка зависимостей
npm install

# Запуск в режиме разработки
npm run dev
```

### Тестирование

Откройте `http://localhost:3000` в браузере для тестирования игры.

Для тестирования как Telegram Mini App используйте:
- Telegram Web или Desktop версию
- Или мобильное приложение с ngrok URL

## 📝 Лицензия

MIT

## 🎉 Начало работы

1. Клонируйте репозиторий
2. Установите зависимости: `npm install`
3. Запустите сервер: `npm start`
4. Откройте браузер: `http://localhost:3000`
5. Наслаждайтесь игрой!

---

Создано для Telegram Mini Apps 🚀
