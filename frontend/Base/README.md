# MedArchive - Frontend

## 📱 Поддерживаемые платформы

- **Android** (5.0+)
- **iOS** (13.0+)
- **Desktop** (Windows, macOS, Linux)

## 🛠 Технологический стек
### Основной
- **Kotlin Multiplatform** - кроссплатформенная разработка
- **Jetpack Compose** / **Compose Multiplatform** - декларативный UI

### Дополнительные библиотеки
- **Navigation Compose** - навигация между экранами

## 📁 Структура проекта
```
composeApp/
├── src/
│   ├── commonMain/           # Общий код для всех платформ
│   │   ├── kotlin/           # Common ViewModel, Models, UseCases
│   │   └── composeResources/ # Ресурсы (изображения, шрифты)
│   │       └── drawable/
│   ├── androidMain/          # Android специфичный код
│   │   ├── kotlin/
│   │   └── res/              # Android ресурсы
│   ├── iosMain/              # iOS специфичный код
│   │   └── kotlin/
│   └── desktopMain/          # Desktop специфичный код
│       └── kotlin/
└── build.gradle.kts          # Build конфигурация
```

## Будущий дизайн(пытаемся сделать похожий)
```
https://www.figma.com/design/LgBBzsneLEdm99ZX8JsxUK/Finance-Management-Mobile-App-UI-UX-Kit-for-Budget-Tracker-Financial-Prototype-Design--Community-?node-id=7020-3430&t=vpIsDub8zd9K4ONN-1
```
