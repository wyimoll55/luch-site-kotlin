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

// На GitHub (переменная CI) берём уже установленный там Yarn, а не скачиваем его:
// загрузка с github.com/yarnpkg иногда обрывается с ошибкой 504.
// На своём компьютере всё скачивается автоматически, как обычно.
if (System.getenv("CI") != null) {
    plugins.withType<org.jetbrains.kotlin.gradle.targets.js.yarn.YarnPlugin> {
        the<org.jetbrains.kotlin.gradle.targets.js.yarn.YarnRootExtension>().download = false
    }
}
