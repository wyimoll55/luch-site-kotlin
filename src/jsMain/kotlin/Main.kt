import kotlinx.browser.document
import kotlinx.browser.window

/**
 * Точка входа. Каждая HTML-страница подключает один и тот же скрипт luch.js,
 * а в <body data-page="..."> указано, какую страницу нужно собрать.
 */
fun main() {
    renderHeader()
    renderFooter()
    Cart.updateCount()
    listenCartButtons()

    val page = document.body?.getAttribute("data-page") ?: "home"

    // Сначала загружаем каталог из JSON, потом рисуем страницу
    loadJson("data/watches.json") { watches ->
        Store.watches = watches.unsafeCast<Array<Watch>>().toList()
        loadJson("data/collections.json") { collections ->
            Store.collections = collections.unsafeCast<Array<WatchCollection>>().toList()
            when (page) {
                "home" -> HomePage.render()
                "catalog" -> CatalogPage.render()
                "product" -> ProductPage.render()
                "collections" -> CollectionsPage.render()
                "cart" -> CartPage.render()
                "contacts" -> ContactsPage.render()
            }
        }
    }
}

/** Скачать JSON-файл и передать результат дальше. */
fun loadJson(url: String, onLoaded: (dynamic) -> Unit) {
    window.fetch(url).then { response ->
        response.json().then { data -> onLoaded(data) }
    }
}
