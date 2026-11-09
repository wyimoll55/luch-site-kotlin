# Сайт «Луч» на Kotlin

Учебный проект команды «Солнечные», БГУИР. Тот же сайт, что и HTML-версия, но вся логика
написана на **Kotlin/JS**: код на Kotlin компилируется в JavaScript и работает в браузере.

- Версия на Kotlin: https://wyimoll55.github.io/luch-site/kotlin/
- Версия на HTML/JS: https://wyimoll55.github.io/luch-site/

## Из чего состоит

| Файл | Что делает |
|---|---|
| `src/jsMain/kotlin/Main.kt` | Точка входа: загружает каталог из JSON и запускает нужную страницу |
| `src/jsMain/kotlin/Model.kt` | Модель данных: часы (`Watch`) и коллекции (`WatchCollection`) |
| `src/jsMain/kotlin/Cart.kt` | Корзина в памяти браузера (localStorage) |
| `src/jsMain/kotlin/Layout.kt` | Шапка с выпадающим меню и подвал |
| `src/jsMain/kotlin/Ui.kt` | Цена в рублях, карточка товара, часы, нарисованные в SVG |
| `src/jsMain/kotlin/HomePage.kt` | Главная: слайдер с автопрокруткой |
| `src/jsMain/kotlin/CatalogPage.kt` | Каталог: вкладки, фильтры, цена «от–до», сортировка, страницы |
| `src/jsMain/kotlin/OtherPages.kt` | Товар, коллекции, корзина, форма обратной связи |
| `src/jsMain/resources/` | HTML-страницы, стили, картинки и данные (JSON) |

## Как запустить у себя

Нужен JDK 17 или новее (есть в IntelliJ IDEA). В папке `kotlin-site`:

```
gradlew.bat jsBrowserDevelopmentRun
```

(на macOS/Linux — `./gradlew jsBrowserDevelopmentRun`). Первый запуск скачивает Kotlin и
занимает несколько минут, потом сайт откроется на http://localhost:8080.

Или в IntelliJ IDEA: File → Open → папка `kotlin-site`, справа панель Gradle →
`kotlin browser` → `jsBrowserDevelopmentRun`.
