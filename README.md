# 📝 Notes App

<div align="center">

Простое и элегантное Android-приложение для управления заметками, созданное на Java

[![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=java&logoColor=white)](https://java.com)
[![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)](https://android.com)
[![License](https://img.shields.io/badge/License-MIT-blue.svg?style=for-the-badge)](LICENSE)

</div>

## ✨ Особенности

- 🎯 **Простой интерфейс** - интуитивно понятное управление заметками
- 💾 **Локальное сохранение** - все данные хранятся на устройстве
- 📤 **Экспорт заметок** - возможность сохранения в внешнее хранилище
- 🔄 **Восстановление состояния** - автоматическое восстановление последней открытой заметки
- 🎨 **Современный дизайн** - чистый и приятный Material Design интерфейс
- ⚡ **Быстрая работа** - оптимизированная производительность

## 🛠 Технические детали

### Архитектура приложения
- **Язык:** Java
- **Минимальная версия Android:** API 21 (Android 5.0)
- **Архитектура:** MVC (Model-View-Controller)
- **Хранение данных:** Внутреннее хранилище + JSON для состояния

### Основные компоненты
- `MainActivity` - главный экран со списком заметок
- `CreateNoteActivity` - создание и редактирование заметок
- `NotesAdapter` - адаптер для RecyclerView
- `Note` - модель данных заметки
- `AppStateManager` - менеджер состояния приложения

### Разрешения
```xml
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"/>
<uses-permission android:name="android.permission.MANAGE_EXTERNAL_STORAGE"/>
```

## 📸 Скриншоты

<div align="center">

<img src="https://github.com/Yusi-21/Contacts/raw/main/app/src/main/res/drawable/screenshot_1.jpg" width="30%" alt="screen1"/>

<img src="https://github.com/Yusi-21/Contacts/raw/main/app/src/main/res/drawable/screenshot_2.jpg" width="30%" alt="screen2"/>

*Логин, главный экран, поиск контактов*
</div>

## ⚙ Установка
1. Клонируйте репозиторий:
```bash
git clone https://github.com/Yusi-21/Notes.git
```
2. Откройте проект в Android Studio
3. Соберите и запустите приложение
