import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.Element
import org.w3c.dom.HTMLFormElement
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.url.URLSearchParams

/** Страница одной модели: product.html?id=97460638 */
object ProductPage {
    private fun dash(v: String?) = if (v.isNullOrEmpty()) "—" else v

    fun render() {
        val id = URLSearchParams(window.location.search).get("id")
        val w = Store.watch(id)
        val root = byId("product")

        if (w == null) {
            root.innerHTML = """
              <h1>Товар не найден</h1>
              <p><a class="btn" href="catalog.html">Вернуться в каталог</a></p>"""
            return
        }

        document.title = "${w.name} — Луч"
        val inCart = Cart.contains(w.id)

        val specs = if (w.category == "accessories") """
              <tr><td>Тип изделия</td><td>${dash(w.type)}</td></tr>
              <tr><td>Материал</td><td>${dash(w.material)}</td></tr>
              <tr><td>Цвет</td><td>${dash(w.color)}</td></tr>
              ${if (w.width != null) "<tr><td>Ширина</td><td>${w.width} мм</td></tr>" else ""}"""
        else """
              <tr><td>Механизм</td><td>${dash(w.mechanism)}</td></tr>
              <tr><td>Корпус</td><td>${dash(w.case)}</td></tr>
              <tr><td>Стекло</td><td>${dash(w.glass)}</td></tr>
              <tr><td>Водозащита</td><td>${dash(w.water)}</td></tr>
              <tr><td>Для кого</td><td>${dash(w.gender)}</td></tr>"""

        root.innerHTML = """
          <a href="catalog.html?collection=${w.collectionSlug}" class="back">← ${w.collection}</a>
          <div class="product">
            <div class="product-img ${if (w.image != null) "has-photo" else ""}">${productImage(w, 380)}</div>
            <div class="product-info">
              <a class="muted" href="catalog.html?collection=${w.collectionSlug}">Коллекция ${w.collection}</a>
              <h1>${w.name}</h1>
              <div class="price big">${formatPrice(w.priceByn)}</div>
              <table class="specs">
                ${if (w.article != "—") "<tr><td>Артикул</td><td>${w.article}</td></tr>" else ""}
                $specs
              </table>
              <button class="btn cart-btn ${if (inCart) "in-cart" else ""}" data-id="${w.id}">${cartButtonText(inCart)}</button>
              ${if (w.url != null) """<a class="btn light" href="${w.url}" target="_blank" rel="noopener">Смотреть на luch.by</a>""" else ""}
              <p class="muted small" style="margin-top:12px">Адреса магазинов — на странице <a href="contacts.html">«Контакты»</a>.</p>
            </div>
          </div>
          <h2 class="section-title">Из той же коллекции</h2>
          <div class="grid" id="similar"></div>"""

        val similar = Store.watches.filter { it.collectionSlug == w.collectionSlug && it.id != w.id }
        byId("similar").innerHTML = if (similar.isNotEmpty())
            similar.joinToString("") { productCard(it) }
        else """<p class="muted">Других моделей в коллекции нет.</p>"""
    }
}

/** Все коллекции завода — карточка ведёт в каталог с фильтром по коллекции. */
object CollectionsPage {
    fun render() {
        byId("collections-count").textContent =
            "${Store.collections.size} коллекций, ${Store.watches.size} моделей"

        byId("collections-list").innerHTML = Store.collections.joinToString("") { c ->
            val preview = Store.watch(c.preview)
            val tags = listOfNotNull(c.gender, c.mechanism).joinToString(" · ")
            """<a class="collection-card" href="catalog.html?collection=${c.slug}">
                 ${if (c.isNew == true) """<span class="badge">Новинка</span>""" else ""}
                 <div class="card-img ${if (preview?.image != null) "has-photo" else ""}">${if (preview != null) productImage(preview, 150) else ""}</div>
                 <div class="card-body">
                   <h3>${c.name}</h3>
                   ${if (tags.isNotEmpty()) """<div class="muted small">$tags</div>""" else ""}
                   <p class="small">${c.about}</p>
                   <div class="collection-meta">
                     <span>${c.count} ${plural(c.count)}</span>
                     <span class="price">${if (c.priceFrom != null) "от ${formatPrice(c.priceFrom)}" else "Цена по запросу"}</span>
                   </div>
                 </div>
               </a>"""
        }
    }

    /** 1 модель, 2 модели, 5 моделей */
    private fun plural(n: Int): String {
        val a = n % 10
        val b = n % 100
        return when {
            a == 1 && b != 11 -> "модель"
            a in 2..4 && (b < 12 || b > 14) -> "модели"
            else -> "моделей"
        }
    }
}

/** Корзина: список, удаление, итог. Заказ не отправляется — это учебный проект. */
object CartPage {
    fun render() {
        val box = byId("cart")
        box.addEventListener("click", { e ->
            val target = e.target as? Element ?: return@addEventListener
            val rm = target.closestOf(".cart-remove")
            if (rm != null) {
                Cart.remove(rm.data("id") ?: "")
                toast("Убрано из корзины")
                draw()
            }
            if (target.id == "checkout") toast("Это учебный проект — заказ не отправляется")
        })
        draw()
    }

    private fun draw() {
        val box = byId("cart")
        val items = Cart.items().mapNotNull { Store.watch(it) }

        if (items.isEmpty()) {
            box.innerHTML = """
              <div class="cart-empty">
                <p>В корзине пока пусто.</p>
                <a class="btn" href="catalog.html">Перейти в каталог</a>
              </div>"""
            return
        }

        val total = items.sumOf { it.priceByn ?: 0 }
        val rows = items.joinToString("") { w ->
            """<div class="cart-item">
                 <a class="cart-thumb ${if (w.image != null) "has-photo" else ""}" href="product.html?id=${w.id}">${productImage(w, 90)}</a>
                 <div class="cart-info">
                   <a href="product.html?id=${w.id}" class="cart-name">${w.name}</a>
                   <div class="muted small">${w.collection}</div>
                 </div>
                 <div class="cart-price">${formatPrice(w.priceByn)}</div>
                 <button class="cart-remove" data-id="${w.id}" aria-label="Убрать">✕</button>
               </div>"""
        }
        box.innerHTML = """
          <div class="cart-list">$rows</div>
          <div class="cart-total">
            <span>Итого: <b>${formatPrice(total)}</b></span>
            <button class="btn" id="checkout">Оформить заказ</button>
          </div>"""
    }
}

/** Форма обратной связи: пока никуда не отправляет — показывает сообщение. */
object ContactsPage {
    fun render() {
        val form = byId("contact-form") as HTMLFormElement
        form.addEventListener("submit", { e ->
            e.preventDefault()
            val name = (form.querySelector("[name=name]") as HTMLInputElement).value
            val email = (form.querySelector("[name=email]") as HTMLInputElement).value
            byId("form-msg").textContent = "Спасибо, $name! Мы ответим на $email."
            form.reset()
        })
    }
}
