import './main.js';

// Форма пока никуда не отправляет — просто показывает сообщение.
// Позже сюда можно подключить бэкенд.
const form = document.querySelector('#contact-form');
form.addEventListener('submit', e => {
  e.preventDefault();
  document.querySelector('#form-msg').textContent =
    `Спасибо, ${form.elements['name'].value}! Мы ответим на ${form.elements['email'].value}.`;
  form.reset();
});
