// Kotlin/JS: код на Kotlin компилируется в JavaScript и работает в браузере
plugins {
    kotlin("multiplatform") version "2.1.0"
}

repositories {
    mavenCentral()
}

kotlin {
    js {
        browser {
            commonWebpackConfig {
                outputFileName = "luch.js"   // готовый скрипт, который подключают HTML-страницы
            }
        }
        binaries.executable()
    }
}
