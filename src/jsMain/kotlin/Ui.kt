import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.Element
import org.w3c.dom.HTMLElement
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

// ---------- Мелкие помощники для работы со страницей ----------

fun byId(id: String): HTMLElement = document.getElementById(id) as HTMLElement

fun queryAll(selector: String, root: Element? = null): List<HTMLElement> {
    val list = if (root != null) root.querySelectorAll(selector) else document.querySelectorAll(selector)
    return (0 until list.length).map { list.item(it) as HTMLElement }
}

/** Ближайший родитель (или сам элемент), подходящий под селектор. */
fun Element.closestOf(selector: String): HTMLElement? = this.asDynamic().closest(selector).unsafeCast<HTMLElement?>()

fun HTMLElement.data(name: String): String? = getAttribute("data-$name")

/** Размытие фона под прозрачным окошком. */
fun glass(el: HTMLElement) {
    el.style.setProperty("backdrop-filter", "blur(20px) saturate(1.2)")
    el.style.setProperty("-webkit-backdrop-filter", "blur(20px) saturate(1.2)")
}

// ---------- Цена в белорусских рублях ----------

/** Знак рубля: Б с горизонтальной чертой (рисуем SVG — в шрифтах такого знака нет). */
const val BYN = """<svg class="byn" viewBox="0 0 20 28" aria-label="руб."><path d="M17 3H6v22h5a6 6 0 0 0 0-12H6M1.5 19.5h9" fill="none" stroke="currentColor" stroke-width="2.6"/></svg>"""

fun formatPrice(byn: Int?): String {
    if (byn == null || byn == 0) return "Цена по запросу"
    val text = byn.asDynamic().toLocaleString("ru-RU") as String   // «1 044»
    return "$text $BYN"
}

// ---------- Всплывающее сообщение внизу экрана ----------

private var toastTimer = 0

fun toast(text: String) {
    val t = document.querySelector(".toast") as? HTMLElement ?: (document.createElement("div") as HTMLElement).also {
        it.className = "toast"
        document.body?.appendChild(it)
    }
    t.textContent = text
    t.classList.add("show")
    window.clearTimeout(toastTimer)
    toastTimer = window.setTimeout({ t.classList.remove("show") }, 1800)
}

// ---------- Рисуем часы через SVG (если у модели нет фото) ----------

fun watchSvg(w: Watch, size: Int = 220): String {
    val marks = (0 until 12).joinToString("") { i ->
        val a = i * 30 * PI / 180
        val r1 = 78.0
        val r2 = if (i % 3 == 0) 66.0 else 71.0
        """<line x1="${100 + r1 * sin(a)}" y1="${100 - r1 * cos(a)}" x2="${100 + r2 * sin(a)}" y2="${100 - r2 * cos(a)}"
              stroke="${w.hand}" stroke-width="${if (i % 3 == 0) 3 else 1.5}" stroke-linecap="round"/>"""
    }

    // У однострелочника одна стрелка, у остальных две
    val hands = if (w.oneHand)
        """<line x1="100" y1="100" x2="146" y2="58" stroke="${w.hand}" stroke-width="3" stroke-linecap="round"/>"""
    else
        """<line x1="100" y1="100" x2="130" y2="78" stroke="${w.hand}" stroke-width="4" stroke-linecap="round"/>
           <line x1="100" y1="100" x2="100" y2="38" stroke="${w.hand}" stroke-width="2.5" stroke-linecap="round"/>"""

    // Корпус: будильник (звонки и ножки), настольные часы (подставка) или наручные (ремешок)
    val bell = if (w.dial == "#fff") "#c9ccd1" else "#9aa0a8"
    val body = when (w.type) {
        "Будильник" -> """<line x1="52" y1="182" x2="40" y2="198" stroke="#8c8f94" stroke-width="7" stroke-linecap="round"/>
           <line x1="148" y1="182" x2="160" y2="198" stroke="#8c8f94" stroke-width="7" stroke-linecap="round"/>
           <path d="M30 52 A34 34 0 0 1 78 14 Z" fill="$bell"/>
           <path d="M170 52 A34 34 0 0 0 122 14 Z" fill="$bell"/>"""
        "Настольные часы" -> """<rect x="60" y="178" width="80" height="18" rx="6" fill="#d9a7b4"/>"""
        else -> """<rect x="82" y="0" width="36" height="30" rx="6" fill="#6b5f50"/>
           <rect x="82" y="170" width="36" height="30" rx="6" fill="#6b5f50"/>"""
    }
    val crown = if (w.type == null || w.type == "Часы")
        """<rect x="190" y="94" width="8" height="12" rx="2" fill="#a9adb3"/>""" else ""

    return """
    <svg viewBox="0 0 200 200" width="$size" height="$size" class="watch-svg" aria-hidden="true">
      $body
      <circle cx="100" cy="100" r="92" fill="#c9ccd1"/>
      <circle cx="100" cy="100" r="86" fill="${w.dial}"/>
      $marks
      <text x="100" y="128" text-anchor="middle" font-size="10" letter-spacing="3"
            fill="${w.hand}" font-family="Georgia, serif">ЛУЧ</text>
      $hands
      <circle cx="100" cy="100" r="4" fill="${w.hand}"/>
      $crown
    </svg>"""
}

/** Есть фото — показываем фото, нет — рисуем часы кодом. */
fun productImage(w: Watch, size: Int = 220): String =
    if (w.image != null) """<img src="${w.image}" alt="${w.name}" loading="lazy" />""" else watchSvg(w, size)

fun cartButtonText(inCart: Boolean) = if (inCart) "В корзине ✓" else "В корзину"

/** Карточка модели в каталоге — как в макете. */
fun productCard(w: Watch): String {
    val inCart = Cart.contains(w.id)
    return """
    <div class="pcard">
      <a class="pcard-img ${if (w.image != null) "has-photo" else ""}" href="product.html?id=${w.id}">
        ${if (w.isNew) """<span class="badge">Новинка</span>""" else ""}
        ${productImage(w, 190)}
      </a>
      <a class="pcard-name" href="product.html?id=${w.id}">${w.name}</a>
      <div class="pcard-price">${formatPrice(w.priceByn)}</div>
      <button class="cart-btn ${if (inCart) "in-cart" else ""}" data-id="${w.id}">${cartButtonText(inCart)}</button>
    </div>"""
}
