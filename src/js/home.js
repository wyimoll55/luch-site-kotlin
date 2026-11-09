import './main.js';

// ---------- Слайдер ----------
// Слайд — это либо картинка (image), либо цветной фон с подписью (bg).
// Чтобы добавить слайд, допишите объект в этот массив.
const slides = [
  { image: 'images/slide-new.jpg', alt: 'Новые коллекции', link: 'catalog.html' },
  { image: 'images/slide-brutalism.jpg', alt: 'Новая коллекция «Брутализм»', link: 'catalog.html' },
  { bg: 'linear-gradient(135deg, #8db9dc, #bcd4e9 45%, #ccd6d1)', title: 'Эстетика 2000-х', text: 'Хром, глянец и прозрачность', link: 'catalog.html', dark: true },
  { bg: 'linear-gradient(135deg, #3b2a22, #8b1414 50%, #d9935a)', title: 'Китайский стиль', text: 'Символизм, природа и каллиграфия', link: 'catalog.html' },
];

const track = document.querySelector('#slides');
const dotsBox = document.querySelector('#dots');

track.innerHTML = slides.map(s => s.image
  ? `<a class="slide" href="${s.link}"><img src="${s.image}" alt="${s.alt}" /></a>`
  : `<a class="slide slide-text ${s.dark ? 'on-light' : ''}" href="${s.link}" style="background:${s.bg}">
       <span class="slide-label">Новая коллекция</span>
       <span class="slide-title">${s.title}</span>
       <span class="slide-text-small">${s.text}</span>
     </a>`
).join('');

dotsBox.innerHTML = slides.map((_, i) => `<button aria-label="Слайд ${i + 1}"></button>`).join('');
const dots = [...dotsBox.children];

let index = 0;
function show(i) {
  index = (i + slides.length) % slides.length;   // по кругу
  track.style.transform = `translateX(-${index * 100}%)`;
  dots.forEach((d, n) => d.classList.toggle('active', n === index));
}

document.querySelector('.prev').addEventListener('click', () => { show(index - 1); restart(); });
document.querySelector('.next').addEventListener('click', () => { show(index + 1); restart(); });
dots.forEach((d, n) => d.addEventListener('click', () => { show(n); restart(); }));

// Автопрокрутка каждые 5 секунд
let timer;
function restart() {
  clearInterval(timer);
  timer = setInterval(() => show(index + 1), 5000);
}

show(0);
restart();

// ---------- Человек с часами ровно со слайдером ----------
// Верх и низ картинки совпадают с верхом и низом слайдера на компьютере
const man = document.querySelector('.hero-man');
const slider = document.querySelector('#slider');
function alignMan() {
  if (window.innerWidth <= 800) { man.style.height = ''; return; }
  man.style.height = slider.offsetHeight + 'px';
}
window.addEventListener('resize', alignMan);
man.complete ? alignMan() : man.addEventListener('load', alignMan);
alignMan();
