// Общий код для всех страниц: шапка, подвал, рисование часов, карточка товара
import '../css/style.css';
import watches from '../data/watches.json';
import collections from '../data/collections.json';

// Данные собраны с официального сайта luch.by (коллекции, артикулы, цены в USD)
export { watches, collections };

// Знак белорусского рубля: Б с горизонтальной чертой (рисуем SVG — в шрифтах такого знака нет)
export const BYN = '<svg class="byn" viewBox="0 0 20 28" aria-label="руб."><path d="M17 3H6v22h5a6 6 0 0 0 0-12H6M1.5 19.5h9" fill="none" stroke="currentColor" stroke-width="2.6"/></svg>';

// Цена в рублях: «775 Б» или «Цена по запросу»
export const formatPrice = byn =>
  byn ? `${byn.toLocaleString('ru-RU')} ${BYN}` : 'Цена по запросу';

// ---------- Корзина (хранится в браузере) ----------
const CART_KEY = 'luch-cart';
export function getCart() {
  try { return JSON.parse(localStorage.getItem(CART_KEY)) || []; } catch { return []; }
}
export function saveCart(ids) {
  try { localStorage.setItem(CART_KEY, JSON.stringify(ids)); } catch {}
  updateCartCount();
}

// Цифра на значке корзины в шапке
export function updateCartCount() {
  const badge = document.querySelector('.cart-count');
  if (!badge) return;
  const n = getCart().length;
  badge.textContent = n;
  badge.hidden = n === 0;
}

// Размытие фона под прозрачными окошками (задаём из JS — при сборке из CSS оно пропадало)
export function glass(el) {
  el.style.backdropFilter = 'blur(20px) saturate(1.2)';
  el.style.webkitBackdropFilter = 'blur(20px) saturate(1.2)';
}
export function toggleCart(id) {
  const cart = getCart();
  const inCart = cart.includes(id);
  saveCart(inCart ? cart.filter(x => x !== id) : [...cart, id]);
  return !inCart;
}

// Всплывающее сообщение внизу экрана
export function toast(text) {
  let t = document.querySelector('.toast');
  if (!t) { t = document.createElement('div'); t.className = 'toast'; document.body.append(t); }
  t.textContent = text;
  t.classList.add('show');
  clearTimeout(t.timer);
  t.timer = setTimeout(() => t.classList.remove('show'), 1800);
}

// Кнопки «В корзину» работают на любой странице
document.addEventListener('click', e => {
  const btn = e.target.closest('.cart-btn');
  if (!btn) return;
  e.preventDefault();
  const added = toggleCart(btn.dataset.id);
  btn.classList.toggle('in-cart', added);
  btn.textContent = added ? 'В корзине ✓' : 'В корзину';
  toast(added ? 'Добавлено в корзину' : 'Убрано из корзины');
});

// ---------- Шапка и подвал ----------
// Меню как в макете: «Интернет-магазин» с выпадающим списком + отдельные кнопки
const shopMenu = [
  { href: 'index.html', label: 'Главная' },
  { href: 'catalog.html', label: 'Каталог' },
  { href: 'collections.html', label: 'Коллекции' },
  { href: '#', label: 'Спецзаказ' },
  { href: 'about.html', label: 'О компании' },
  { href: 'contacts.html', label: 'Контакты' },
];
const topMenu = [
  { href: '#', label: 'Оплата и доставка' },
  { href: '#', label: 'Новости и акции' },
  { href: '#', label: 'Музей' },
];

function renderHeader() {
  const current = location.pathname.split('/').pop() || 'index.html';
  const dropdown = shopMenu
    .map(p => `<li><a href="${p.href}" class="${p.href === current ? 'current' : ''}">${p.label}</a></li>`)
    .join('');

  document.querySelector('#header').innerHTML = `
    <div class="container header-inner">
      <button class="burger pill" aria-label="Меню">☰</button>
      <nav class="nav">
        <div class="dropdown">
          <button class="pill" id="shop-btn">Интернет-магазин <span class="arrow">▾</span></button>
          <ul class="dropdown-menu">${dropdown}</ul>
        </div>
        ${topMenu.map(p => `<a class="pill" href="${p.href}">${p.label}</a>`).join('')}
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
    </div>`;

  // Выпадающее меню
  const dd = document.querySelector('.dropdown');

  // Размытие фона под меню задаём здесь, а не в CSS: при сборке для GitHub оно из CSS пропадало
  glass(dd.querySelector('.dropdown-menu'));
  document.querySelector('#shop-btn').addEventListener('click', e => {
    e.stopPropagation();
    dd.classList.toggle('open');
  });
  document.addEventListener('click', () => dd.classList.remove('open'));

  // Мобильное меню
  document.querySelector('.burger').addEventListener('click', () =>
    document.querySelector('.nav').classList.toggle('open'));
}

function renderFooter() {
  document.querySelector('#footer').innerHTML = `
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
        <p class="footer-copy">© 2013 — ${new Date().getFullYear()} ОАО "Минский часовой завод". Все права защищены</p>
        <p class="footer-copy muted">Учебный проект команды «Солнечные», БГУИР</p>
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
    </div>`;
}

// ---------- Рисуем циферблат через SVG ----------
// Фото пока нет, поэтому часы рисуются кодом по цветам из JSON.
// Когда появятся фото, можно заменить на <img src="...">.
export function watchSVG(w, size = 220) {
  const marks = Array.from({ length: 12 }, (_, i) => {
    const a = (i * 30 * Math.PI) / 180;
    const r1 = 78, r2 = i % 3 === 0 ? 66 : 71;
    return `<line x1="${100 + r1 * Math.sin(a)}" y1="${100 - r1 * Math.cos(a)}"
                  x2="${100 + r2 * Math.sin(a)}" y2="${100 - r2 * Math.cos(a)}"
                  stroke="${w.hand}" stroke-width="${i % 3 === 0 ? 3 : 1.5}" stroke-linecap="round"/>`;
  }).join('');

  // У однострелочника одна стрелка, у остальных две
  const hands = w.oneHand
    ? `<line x1="100" y1="100" x2="146" y2="58" stroke="${w.hand}" stroke-width="3" stroke-linecap="round"/>`
    : `<line x1="100" y1="100" x2="130" y2="78" stroke="${w.hand}" stroke-width="4" stroke-linecap="round"/>
       <line x1="100" y1="100" x2="100" y2="38" stroke="${w.hand}" stroke-width="2.5" stroke-linecap="round"/>`;

  // Корпус: будильник (звонки и ножки), настольные часы (подставка) или наручные (ремешок)
  const body = w.type === 'Будильник'
    ? `<line x1="52" y1="182" x2="40" y2="198" stroke="#8c8f94" stroke-width="7" stroke-linecap="round"/>
       <line x1="148" y1="182" x2="160" y2="198" stroke="#8c8f94" stroke-width="7" stroke-linecap="round"/>
       <path d="M30 52 A34 34 0 0 1 78 14 Z" fill="${w.dial === '#fff' ? '#c9ccd1' : '#9aa0a8'}"/>
       <path d="M170 52 A34 34 0 0 0 122 14 Z" fill="${w.dial === '#fff' ? '#c9ccd1' : '#9aa0a8'}"/>`
    : w.type === 'Настольные часы'
    ? `<rect x="60" y="178" width="80" height="18" rx="6" fill="#d9a7b4"/>`
    : `<rect x="82" y="0" width="36" height="30" rx="6" fill="#6b5f50"/>
       <rect x="82" y="170" width="36" height="30" rx="6" fill="#6b5f50"/>`;

  return `
    <svg viewBox="0 0 200 200" width="${size}" height="${size}" class="watch-svg" aria-hidden="true">
      ${body}
      <circle cx="100" cy="100" r="92" fill="#c9ccd1"/>
      <circle cx="100" cy="100" r="86" fill="${w.dial}"/>
      ${marks}
      <text x="100" y="128" text-anchor="middle" font-size="10" letter-spacing="3"
            fill="${w.hand}" font-family="Georgia, serif">ЛУЧ</text>
      ${hands}
      <circle cx="100" cy="100" r="4" fill="${w.hand}"/>
      ${w.type === 'Часы' || !w.type ? '<rect x="190" y="94" width="8" height="12" rx="2" fill="#a9adb3"/>' : ''}
    </svg>`;
}

// ---------- Карточка в каталоге (как в макете) ----------
// Есть фото — показываем фото, нет — рисуем часы кодом
export function productImage(w, size = 220) {
  return w.image
    ? `<img src="${w.image}" alt="${w.name}" loading="lazy" />`
    : watchSVG(w, size);
}

export function productCard(w) {
  const inCart = getCart().includes(w.id);
  return `
    <div class="pcard">
      <a class="pcard-img ${w.image ? 'has-photo' : ''}" href="product.html?id=${w.id}">
        ${w.isNew ? '<span class="badge">Новинка</span>' : ''}
        ${productImage(w, 190)}
      </a>
      <a class="pcard-name" href="product.html?id=${w.id}">${w.name}</a>
      <div class="pcard-price">${formatPrice(w.priceByn)}</div>
      <button class="cart-btn ${inCart ? 'in-cart' : ''}" data-id="${w.id}">${inCart ? 'В корзине ✓' : 'В корзину'}</button>
    </div>`;
}

renderHeader();
renderFooter();
updateCartCount();
