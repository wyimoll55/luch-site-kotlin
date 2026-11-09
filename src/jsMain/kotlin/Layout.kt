import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.Element
import org.w3c.dom.HTMLElement
import kotlin.js.Date

// Шапка и подвал — одинаковые на всех страницах

data class MenuItem(val href: String, val label: String)

/** «Интернет-магазин» с выпадающим списком. */
val shopMenu = listOf(
    MenuItem("index.html", "Главная"),
    MenuItem("catalog.html", "Каталог"),
    MenuItem("collections.html", "Коллекции"),
    MenuItem("#", "Спецзаказ"),
    MenuItem("about.html", "О компании"),
    MenuItem("contacts.html", "Контакты"),
)

/** Отдельные кнопки в шапке. */
val topMenu = listOf(
    MenuItem("#", "Оплата и доставка"),
    MenuItem("#", "Новости и акции"),
    MenuItem("#", "Музей"),
)

fun renderHeader() {
    val current = window.location.pathname.substringAfterLast('/').ifEmpty { "index.html" }
    val dropdown = shopMenu.joinToString("") {
        """<li><a href="${it.href}" class="${if (it.href == current) "current" else ""}">${it.label}</a></li>"""
    }
    val buttons = topMenu.joinToString("") { """<a class="pill" href="${it.href}">${it.label}</a>""" }

    byId("header").innerHTML = """
    <div class="container header-inner">
      <button class="burger pill" aria-label="Меню">☰</button>
      <nav class="nav">
        <div class="dropdown">
          <button class="pill" id="shop-btn">Интернет-магазин <span class="arrow">▾</span></button>
          <ul class="dropdown-menu">$dropdown</ul>
        </div>
        $buttons
      </nav>
      <a class="cart-link" href="cart.html" aria-label="Корзина">
        <svg viewBox="0 0 24 24" width="26" height="26" fill="none" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round">
          <path d="M2 3h3l2.4 11.2a1.5 1.5 0 0 0 1.5 1.2h8.8a1.5 1.5 0 0 0 1.5-1.1L21 7H6"/>
          <circle cx="9.5" cy="20" r="1.4"/><circle cx="17.5" cy="20" r="1.4"/>
        </svg>
        <span class="cart-count" hidden>0</span>
      </a>
      <form class="search pill" action="catalog.html">
        <input name="q" placeholder="Поиск" aria-label="Поиск" />
        <svg class="search-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" aria-hidden="true"><circle cx="10.5" cy="10.5" r="6.5"/><line x1="15.5" y1="15.5" x2="21" y2="21"/></svg>
      </form>
      <a href="index.html" class="logo"><img src="images/logo.svg" alt="Луч" /></a>
    </div>"""

    // Выпадающее меню: открывается при наведении (CSS) и по нажатию — для телефона
    val dd = document.querySelector(".dropdown") as HTMLElement
    glass(dd.querySelector(".dropdown-menu") as HTMLElement)
    byId("shop-btn").addEventListener("click", { e ->
        e.stopPropagation()
        dd.classList.toggle("open")
    })
    document.addEventListener("click", { dd.classList.remove("open") })

    // Мобильное меню
    (document.querySelector(".burger") as HTMLElement).addEventListener("click", {
        (document.querySelector(".nav") as HTMLElement).classList.toggle("open")
    })
}

fun renderFooter() {
    byId("footer").innerHTML = """
    <div class="container footer-inner">
      <div class="footer-legal">
        <p class="footer-company">ОАО "Минский часовой завод"</p>
        <p>
          220043, г. Минск, пр. Независимости, 95; УНП 100230391<br>
          Свидетельство о государственной регистрации ОАО "Минский часовой завод" №100230391<br>
          от 24 марта 2016, выданное Минским горисполкомом.<br>
          Регистрационный номер интернет-магазина luch.by в торговом реестре РБ №157658<br>
          от 06.04.2016, выдан Администрацией Первомайского района.
        </p>
        <p class="footer-policies">
          <a href="#">Политика конфиденциальности</a> |
          <a href="#">Политика в отношении обработки cookies</a> |<br>
          <a href="#">Политика видеонаблюдения</a> |
          <a href="#">Порядок подачи покупателем обращений</a>
        </p>
        <p class="footer-note">
          По вопросам защиты прав потребителей можно обратиться в администрацию Первомайского района г.Минска<br>
          по адресу: г.Минск, пер. К. Чорного, 5.
        </p>
        <p class="footer-note">
          Номер контактного телефона, адрес электронной почты лица, уполномоченного продавцом рассматривать обращения<br>
          покупателей о нарушении их прав, предусмотренных законодательством о защите<br>
          прав потребителей: <a href="tel:+375447033645">+375 44 703-36-45</a>, <a href="mailto:shop@luch.by">shop@luch.by</a>.
        </p>
        <p class="footer-copy">© 2013 — ${Date().getFullYear()} ОАО "Минский часовой завод". Все права защищены</p>
        <p class="footer-copy muted">Учебный проект команды «Солнечные», БГУИР · версия на Kotlin/JS</p>
      </div>

      <div class="footer-shop">
        <h3>Интернет магазин:</h3>
        <p>
          Tel: <a href="tel:7187">7187</a><br>
          Tel: <a href="tel:+375292725156">+375 (29) 272-51-56</a> (МТС)<br>
          Tel: <a href="tel:+375293157526">+375 (29) 315-75-26</a> (А1)
        </p>
        <p class="footer-hours">ПН-ВС: с 10:00 до 20:00</p>
        <p><a class="footer-mail" href="mailto:shop@luch.by">shop@luch.by</a></p>
      </div>
    </div>"""
}

/** Кнопки «В корзину» работают на любой странице. */
fun listenCartButtons() {
    document.addEventListener("click", { e ->
        val btn = (e.target as? Element)?.closestOf(".cart-btn") ?: return@addEventListener
        val id = btn.data("id") ?: return@addEventListener
        e.preventDefault()
        val added = Cart.toggle(id)
        btn.classList.toggle("in-cart", added)
        btn.textContent = cartButtonText(added)
        toast(if (added) "Добавлено в корзину" else "Убрано из корзины")
    })
}
