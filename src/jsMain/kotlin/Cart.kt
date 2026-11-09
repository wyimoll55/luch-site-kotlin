import kotlinx.browser.document
import kotlinx.browser.localStorage
import org.w3c.dom.HTMLElement

/** Корзина хранится в браузере (localStorage) — список id выбранных моделей. */
object Cart {
    private const val KEY = "luch-cart"

    fun items(): List<String> = try {
        val raw = localStorage.getItem(KEY)
        if (raw == null) emptyList() else JSON.parse<Array<String>>(raw).toList()
    } catch (e: Throwable) {
        emptyList()
    }

    fun save(ids: List<String>) {
        try {
            localStorage.setItem(KEY, JSON.stringify(ids.toTypedArray()))
        } catch (e: Throwable) {
            // браузер запретил хранилище — корзина просто не запомнится
        }
        updateCount()
    }

    fun contains(id: String): Boolean = id in items()

    /** Добавить или убрать модель. Возвращает true, если модель теперь в корзине. */
    fun toggle(id: String): Boolean {
        val list = items()
        val wasIn = id in list
        save(if (wasIn) list - id else list + id)
        return !wasIn
    }

    fun remove(id: String) = save(items() - id)

    /** Цифра на значке корзины в шапке. */
    fun updateCount() {
        val badge = document.querySelector(".cart-count") as? HTMLElement ?: return
        val n = items().size
        badge.textContent = n.toString()
        badge.hidden = n == 0
    }
}
