import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.Element
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.url.URLSearchParams
import kotlin.math.ceil

/**
 * Группа фильтров слева. В одной группе работает «ИЛИ», между группами — «И».
 * key — поле модели в watches.json, options — галочки.
 */
data class FilterGroup(
    val key: String,
    val title: String,
    val options: List<String>,
    val collapsed: Boolean = false,
    val labels: Map<String, String> = emptyMap(),
    val value: ((Watch) -> String?)? = null,     // своё правило вместо поля модели
) {
    fun valueOf(w: Watch): String? {
        val rule = value
        return if (rule != null) rule(w) else w.field(key)
    }
}

data class Flag(val key: String, val title: String)

object CatalogPage {
    private val groups = mapOf(
        "wrist" to listOf(
            FilterGroup("gender", "Тип часов", listOf("Женские", "Мужские", "Детские", "Унисекс")),
            FilterGroup("winding", "Тип механизма", listOf("Кварцевый", "Механические (автоподзавод)", "Механические (ручной завод)")),
            FilterGroup("movementBrand", "Механизм", listOf("Луч, РБ", "Miyota, Япония", "Seiko, Япония", "Ronda, Швейцария", "Sellita, Швейцария", "Восток, РФ")),
            FilterGroup("caseMaterial", "Материал корпуса", listOf("Керамика", "Дамасская сталь", "Дерево", "Латунь", "Нержавеющая сталь", "Пластик", "Сталь", "Титан")),
            FilterGroup("glassType", "Стекло", listOf("Сапфировое", "Минеральное"), collapsed = true),
            FilterGroup("waterRes", "Водонепроницаемость", listOf("3 бар", "5 бар", "10 бар"), collapsed = true),
            FilterGroup("chrono", "Хронограф", listOf("Есть"), collapsed = true, value = { if (it.chrono) "Есть" else null }),
        ),
        "interior" to listOf(
            FilterGroup("type", "Тип часов", listOf("Будильник", "Настольные часы"), labels = mapOf("Будильник" to "Будильники")),
        ),
        "accessories" to listOf(
            FilterGroup("width", "Размер (ширина), мм", listOf("18", "20", "22", "24")),
            FilterGroup("type", "Тип изделия", listOf("Браслет", "Браслет для часов", "Картхолдер", "Колье", "Кольцо", "Набор платков",
                "Обложка для паспорта", "Платок женский", "Платок-ремень", "Ремешок", "Ролл для часов", "Серьги")),
            FilterGroup("color", "Цвет", listOf("Серебристый", "Золотистый", "Чёрный", "Коричневый", "Бежевый"), collapsed = true),
            FilterGroup("material", "Материал", listOf("Металл", "Кожа", "Шёлк", "Текстиль"), collapsed = true),
        ),
    )
    private val flags = listOf(Flag("isNew", "Новинки"), Flag("limited", "Лимитированная серия"))

    private const val PAGE_SIZE = 24          // моделей на одной странице

    // ---------- Состояние ----------
    private var tab = "wrist"
    private var collectionSlug = ""
    private var query = ""
    private var page = 1
    private var sortValue = ""                // "" — популярности, new, desc, asc
    private val checkedFlags = mutableSetOf<String>()
    private val checked = mutableMapOf<String, MutableSet<String>>()
    private val collapsed = mutableMapOf<String, Boolean>()

    private lateinit var fromInput: HTMLInputElement
    private lateinit var toInput: HTMLInputElement

    private val currentGroups get() = groups[tab] ?: emptyList()
    private fun inTab(w: Watch) = w.category == tab
    private fun maxPrice(): Int = Store.watches.filter { inTab(it) }.maxOfOrNull { it.priceByn ?: 0 } ?: 0

    fun render() {
        val params = URLSearchParams(window.location.search)
        query = (params.get("q") ?: "").trim().lowercase()
        collectionSlug = params.get("collection") ?: ""
        tab = params.get("tab") ?: "wrist"
        Store.watches.firstOrNull { it.collectionSlug == collectionSlug }?.let { tab = it.category }

        (document.querySelector(".search input") as? HTMLInputElement)?.value = query
        fromInput = byId("price-from") as HTMLInputElement
        toInput = byId("price-to") as HTMLInputElement

        listenEvents()
        resetPrice()
        renderSort()
        renderTabs()
        renderFilters()
        renderList()
    }

    // ---------- Фильтрация и сортировка ----------
    private fun filtered(): List<Watch> {
        val from = fromInput.value.toDoubleOrNull() ?: 0.0
        val to = toInput.value.toDoubleOrNull() ?: Double.MAX_VALUE
        val priceTouched = from > 0 || to < maxPrice()

        val list = Store.watches.filter { w ->
            inTab(w) &&
                (collectionSlug.isEmpty() || w.collectionSlug == collectionSlug) &&
                (query.isEmpty() || w.name.lowercase().contains(query) || w.collection.lowercase().contains(query)) &&
                checkedFlags.all { w.flag(it) } &&
                currentGroups.all { g -> matches(g, w) } &&
                (w.priceByn?.let { it.toDouble() >= from && it.toDouble() <= to } ?: !priceTouched)
        }

        return when (sortValue) {
            "asc" -> list.sortedBy { it.priceByn ?: Int.MAX_VALUE }
            "desc" -> list.sortedByDescending { it.priceByn ?: -1 }
            "new" -> list.sortedByDescending { it.isNew }
            else -> list
        }
    }

    /** Модель подходит под группу: в группе ничего не отмечено или отмечено её значение. */
    private fun matches(g: FilterGroup, w: Watch): Boolean {
        val selected = checked[g.key]
        if (selected.isNullOrEmpty()) return true
        val v = g.valueOf(w) ?: return false
        return v in selected
    }

    // ---------- Отрисовка ----------
    private fun renderFilters() {
        val base = Store.watches.filter { inTab(it) }

        val flagsHtml = flags.joinToString("") { f ->
            """<label class="check-row flag">
                 <input type="checkbox" data-flag="${f.key}" ${if (f.key in checkedFlags) "checked" else ""} />
                 <span>${f.title}</span>
               </label>"""
        }

        val groupsHtml = currentGroups.joinToString("") { g ->
            val isCollapsed = collapsed[g.key] ?: g.collapsed
            val opts = g.options.joinToString("") { o ->
                val n = base.count { g.valueOf(it) == o }
                val isChecked = checked[g.key]?.contains(o) == true
                """<label class="check-row ${if (n > 0) "" else "empty"}">
                     <input type="checkbox" data-group="${g.key}" value="$o" ${if (isChecked) "checked" else ""} ${if (n > 0) "" else "disabled"} />
                     <span>${g.labels[o] ?: o}</span><em>$n</em>
                   </label>"""
            }
            """<div class="filter-group ${if (isCollapsed) "collapsed" else ""}">
                 <button class="group-head" data-toggle="${g.key}">
                   <span>${g.title}</span><i class="group-sign">${if (isCollapsed) "+" else "−"}</i>
                 </button>
                 <div class="group-body">$opts</div>
               </div>"""
        }

        byId("filter-groups").innerHTML = flagsHtml + groupsHtml
    }

    private fun renderTabs() {
        queryAll(".tab-btn").forEach { it.classList.toggle("active", it.data("tab") == tab) }
    }

    private fun renderList() {
        val list = filtered()

        val c = Store.collections.firstOrNull { it.slug == collectionSlug }
        byId("active-collection").innerHTML = if (c != null)
            """<span class="chip">Коллекция: ${c.name} <button id="clear-collection" aria-label="Убрать">✕</button></span>
               <p class="muted small">${c.about}</p>"""
        else ""

        byId("count").textContent = (if (query.isNotEmpty()) "Поиск «$query». " else "") + "Найдено: ${list.size}"

        // Страницы по 24 модели
        val pages = maxOf(1, ceil(list.size / PAGE_SIZE.toDouble()).toInt())
        page = minOf(page, pages)
        byId("catalog-list").innerHTML = if (list.isNotEmpty())
            list.drop((page - 1) * PAGE_SIZE).take(PAGE_SIZE).joinToString("") { productCard(it) }
        else """<p class="muted">Ничего не найдено. Попробуйте изменить фильтры.</p>"""

        byId("pager").style.setProperty("display", if (list.size > PAGE_SIZE) "" else "none")
        byId("page-num").textContent = page.toString()
        byId("page-num").title = "Страница $page из $pages"
        (byId("prev") as HTMLButtonElement).disabled = page == 1
        (byId("next") as HTMLButtonElement).disabled = page == pages
    }

    private fun renderSort() {
        queryAll("[data-sort]", byId("sort-dropdown")).forEach { it.classList.toggle("current", it.data("sort") == sortValue) }
    }

    private fun resetPrice() {
        fromInput.value = "0"
        toInput.value = maxPrice().toString()
    }

    private fun update() {
        page = 1
        renderList()
    }

    private fun goTo(p: Int) {
        page = p
        renderList()
        val top = byId("catalog-list").getBoundingClientRect().top + window.scrollY - 140
        val options: dynamic = js("({})")
        options.top = top
        options.behavior = "smooth"
        window.asDynamic().scrollTo(options)
    }

    // ---------- События ----------
    private fun listenEvents() {
        // Вкладки «Наручные / Интерьерные / Аксессуары»
        byId("tabs").addEventListener("click", { e ->
            val b = (e.target as? Element)?.closestOf(".tab-btn") ?: return@addEventListener
            val newTab = b.data("tab") ?: return@addEventListener
            if (newTab == tab) return@addEventListener
            tab = newTab
            collectionSlug = ""
            checked.clear(); checkedFlags.clear()
            resetPrice(); renderTabs(); renderFilters(); update()
        })

        // Свернуть / развернуть группу фильтров
        byId("filter-groups").addEventListener("click", { e ->
            val head = (e.target as? Element)?.closestOf("[data-toggle]") ?: return@addEventListener
            val key = head.data("toggle") ?: return@addEventListener
            val g = currentGroups.first { it.key == key }
            collapsed[key] = !(collapsed[key] ?: g.collapsed)
            renderFilters()
        })

        // Галочки
        byId("filter-groups").addEventListener("change", { e ->
            val t = e.target as? HTMLInputElement ?: return@addEventListener
            t.getAttribute("data-flag")?.let { if (t.checked) checkedFlags.add(it) else checkedFlags.remove(it) }
            t.getAttribute("data-group")?.let { key ->
                val set = checked.getOrPut(key) { mutableSetOf() }
                if (t.checked) set.add(t.value) else set.remove(t.value)
            }
            update()
        })

        // Убрать выбранную коллекцию
        byId("active-collection").addEventListener("click", { e ->
            if ((e.target as? Element)?.id != "clear-collection") return@addEventListener
            collectionSlug = ""
            window.history.replaceState(null, "", "catalog.html")
            update()
        })

        // Цена «От — До»
        fromInput.addEventListener("input", { update() })
        toInput.addEventListener("input", { update() })

        // «Сортировать по»: прозрачное окошко, как у меню
        val sortBox = byId("sort-dropdown")
        glass(byId("sort-menu"))
        byId("sort-btn").addEventListener("click", { e ->
            e.stopPropagation()
            sortBox.classList.toggle("open")
        })
        byId("sort-menu").addEventListener("click", { e ->
            val b = (e.target as? Element)?.closestOf("[data-sort]") ?: return@addEventListener
            sortValue = b.data("sort") ?: ""
            sortBox.classList.remove("open")
            renderSort(); update()
        })
        document.addEventListener("click", { sortBox.classList.remove("open") })

        // Сбросить фильтры
        byId("reset").addEventListener("click", {
            checked.clear(); checkedFlags.clear()
            sortValue = ""; renderSort()
            resetPrice(); renderFilters(); update()
        })

        // Страницы «← 1 →»
        byId("prev").addEventListener("click", { goTo(page - 1) })
        byId("next").addEventListener("click", { goTo(page + 1) })

        // Кнопка «Фильтры» на телефоне
        byId("filters-toggle").addEventListener("click", { byId("filters").classList.toggle("open") })
    }
}
