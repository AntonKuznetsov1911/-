// Инициализация Telegram Web App
let tg = window.Telegram.WebApp;
tg.expand();
tg.ready();

// Игровые переменные
let score = 0;
let totalClicks = 0;
let clickPower = 1;
let autoClickRate = 0;
let highScore = 0;
let lastSecondClicks = 0;
let clicksThisSecond = 0;

// Улучшения
let upgrades = {
    1: { owned: 0, cost: 10, effect: 1, type: 'auto' },
    2: { owned: 0, cost: 25, effect: 2, type: 'power' },
    3: { owned: 0, cost: 100, effect: 5, type: 'auto' }
};

// Элементы DOM
const clickButton = document.getElementById('clickButton');
const scoreDisplay = document.getElementById('score');
const totalClicksDisplay = document.getElementById('totalClicks');
const cpsDisplay = document.getElementById('cps');
const highScoreDisplay = document.getElementById('highScore');
const usernameDisplay = document.getElementById('username');

// Загрузка данных
function loadGameData() {
    const savedData = localStorage.getItem('telegramGameData');
    if (savedData) {
        const data = JSON.parse(savedData);
        score = data.score || 0;
        totalClicks = data.totalClicks || 0;
        clickPower = data.clickPower || 1;
        autoClickRate = data.autoClickRate || 0;
        highScore = data.highScore || 0;
        upgrades = data.upgrades || upgrades;
    }
    updateDisplay();
}

// Сохранение данных
function saveGameData() {
    const data = {
        score,
        totalClicks,
        clickPower,
        autoClickRate,
        highScore,
        upgrades
    };
    localStorage.setItem('telegramGameData', JSON.stringify(data));
}

// Обработка клика
clickButton.addEventListener('click', function(e) {
    handleClick(e);
});

function handleClick(e) {
    score += clickPower;
    totalClicks++;
    clicksThisSecond++;

    // Анимация кнопки
    clickButton.classList.add('click-animation');
    setTimeout(() => clickButton.classList.remove('click-animation'), 300);

    // Плавающий текст
    createFloatingText(`+${clickPower}`, e.clientX, e.clientY);

    // Вибрация (если поддерживается Telegram)
    if (tg.HapticFeedback) {
        tg.HapticFeedback.impactOccurred('light');
    }

    updateDisplay();
    saveGameData();
}

// Создание плавающего текста
function createFloatingText(text, x, y) {
    const element = document.createElement('div');
    element.className = 'floating-text';
    element.textContent = text;
    element.style.left = x + 'px';
    element.style.top = y + 'px';
    document.body.appendChild(element);

    setTimeout(() => {
        element.remove();
    }, 1000);
}

// Покупка улучшений
function buyUpgrade(id, baseCost) {
    const upgrade = upgrades[id];
    const cost = Math.floor(baseCost * Math.pow(1.5, upgrade.owned));

    if (score >= cost) {
        score -= cost;
        upgrade.owned++;

        if (upgrade.type === 'auto') {
            autoClickRate += upgrade.effect;
        } else if (upgrade.type === 'power') {
            clickPower += upgrade.effect;
        }

        // Обновление кнопки улучшения
        const newCost = Math.floor(baseCost * Math.pow(1.5, upgrade.owned));
        const button = document.querySelector(`#upgrade${id} .upgrade-button`);
        button.textContent = `Купить (${newCost})`;

        // Вибрация
        if (tg.HapticFeedback) {
            tg.HapticFeedback.notificationOccurred('success');
        }

        updateDisplay();
        saveGameData();
    } else {
        // Вибрация ошибки
        if (tg.HapticFeedback) {
            tg.HapticFeedback.notificationOccurred('error');
        }
    }
}

// Обновление дисплея
function updateDisplay() {
    scoreDisplay.textContent = score.toLocaleString();
    totalClicksDisplay.textContent = totalClicks.toLocaleString();
    highScoreDisplay.textContent = highScore.toLocaleString();

    // Обновление статуса кнопок улучшения
    Object.keys(upgrades).forEach(id => {
        const upgrade = upgrades[id];
        const baseCost = id === '1' ? 10 : id === '2' ? 25 : 100;
        const cost = Math.floor(baseCost * Math.pow(1.5, upgrade.owned));
        const button = document.querySelector(`#upgrade${id} .upgrade-button`);

        if (button) {
            button.disabled = score < cost;
        }
    });

    // Обновление рекорда
    if (score > highScore) {
        highScore = score;
    }
}

// Автоклик
setInterval(() => {
    if (autoClickRate > 0) {
        score += autoClickRate;
        updateDisplay();
        saveGameData();
    }
}, 1000);

// Подсчет кликов в секунду
setInterval(() => {
    lastSecondClicks = clicksThisSecond;
    cpsDisplay.textContent = (lastSecondClicks + autoClickRate).toFixed(1);
    clicksThisSecond = 0;
}, 1000);

// Автосохранение
setInterval(() => {
    saveGameData();
}, 5000);

// Инициализация пользователя
if (tg.initDataUnsafe && tg.initDataUnsafe.user) {
    const user = tg.initDataUnsafe.user;
    usernameDisplay.textContent = user.first_name || user.username || 'Игрок';
} else {
    usernameDisplay.textContent = 'Игрок';
}

// Настройка темы Telegram
document.body.style.backgroundColor = tg.themeParams.bg_color || '#ffffff';
document.body.style.color = tg.themeParams.text_color || '#000000';

// Загрузка данных при старте
loadGameData();

// Кнопка в Telegram для сброса игры
if (tg.MainButton) {
    tg.MainButton.setText('Сбросить игру');
    tg.MainButton.onClick(() => {
        if (confirm('Вы уверены, что хотите сбросить игру?')) {
            localStorage.removeItem('telegramGameData');
            location.reload();
        }
    });
    tg.MainButton.show();
}

console.log('Игра загружена!');
console.log('Telegram WebApp версия:', tg.version);
