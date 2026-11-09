import { watches, collections, productImage, formatPrice } from './main.js';

// Все коллекции завода — карточка ведёт в каталог с фильтром по коллекции
const box = document.querySelector('#collections-list');
document.querySelector('#collections-count').textContent =
  `${collections.length} коллекций, ${watches.length} моделей`;

box.innerHTML = collections.map(c => {
  const preview = watches.find(w => w.id === c.preview);
  const tags = [c.gender, c.mechanism].filter(Boolean).join(' · ');
  return `
    <a class="collection-card" href="catalog.html?collection=${c.slug}">
      ${c.isNew ? '<span class="badge">Новинка</span>' : ''}
      <div class="card-img ${preview && preview.image ? 'has-photo' : ''}">${preview ? productImage(preview, 150) : ''}</div>
      <div class="card-body">
        <h3>${c.name}</h3>
        ${tags ? `<div class="muted small">${tags}</div>` : ''}
        <p class="small">${c.about}</p>
        <div class="collection-meta">
          <span>${c.count} ${plural(c.count)}</span>
          <span class="price">${c.priceFrom ? `от ${formatPrice(c.priceFrom)}` : 'Цена по запросу'}</span>
        </div>
      </div>
    </a>`;
}).join('');

function plural(n) {
  const a = n % 10, b = n % 100;
  if (a === 1 && b !== 11) return 'модель';
  if (a >= 2 && a <= 4 && (b < 12 || b > 14)) return 'модели';
  return 'моделей';
}
