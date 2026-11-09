import { watches, getCart, saveCart, productImage, formatPrice, toast } from './main.js';

// Страница корзины: список, удаление, итог. Заказ не отправляется — это учебный проект.
const box = document.querySelector('#cart');

function render() {
  const items = getCart().map(id => watches.find(w => w.id === id)).filter(Boolean);

  if (!items.length) {
    box.innerHTML = `
      <div class="cart-empty">
        <p>В корзине пока пусто.</p>
        <a class="btn" href="catalog.html">Перейти в каталог</a>
      </div>`;
    return;
  }

  const total = items.reduce((sum, w) => sum + (w.priceByn || 0), 0);
  box.innerHTML = `
    <div class="cart-list">
      ${items.map(w => `
        <div class="cart-item">
          <a class="cart-thumb ${w.image ? 'has-photo' : ''}" href="product.html?id=${w.id}">${productImage(w, 90)}</a>
          <div class="cart-info">
            <a href="product.html?id=${w.id}" class="cart-name">${w.name}</a>
            <div class="muted small">${w.collection}</div>
          </div>
          <div class="cart-price">${formatPrice(w.priceByn)}</div>
          <button class="cart-remove" data-id="${w.id}" aria-label="Убрать">✕</button>
        </div>`).join('')}
    </div>
    <div class="cart-total">
      <span>Итого: <b>${formatPrice(total)}</b></span>
      <button class="btn" id="checkout">Оформить заказ</button>
    </div>`;
}

box.addEventListener('click', e => {
  const rm = e.target.closest('.cart-remove');
  if (rm) {
    saveCart(getCart().filter(id => id !== rm.dataset.id));
    toast('Убрано из корзины');
    render();
  }
  if (e.target.id === 'checkout') toast('Это учебный проект — заказ не отправляется');
});

render();
