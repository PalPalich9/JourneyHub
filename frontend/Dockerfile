# Используем базовый образ Node.js
FROM node:20

# Устанавливаем рабочую директорию
WORKDIR /app

# Копируем package.json и устанавливаем зависимости
COPY package.json package-lock.json* ./
RUN npm install

# Копируем исходный код
COPY . .

# Сборка фронтенда
RUN npm run build

# Устанавливаем serve для запуска статического контента
RUN npm install -g serve

# Запуск фронтенда на порту 5173
CMD ["serve", "-s", "dist", "-l", "5173"]