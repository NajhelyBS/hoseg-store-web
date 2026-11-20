/* =====================================================================
   HÖSÉG Web · Nosotros
   Hace aparecer con suavidad las secciones al entrar en pantalla
   ([data-revelar], estilos en nosotros.css). El conteo animado de las
   cifras no está aquí: lo hace home.js, que la página también carga.
   Sin JS, o con "reducir movimiento" activado, todo queda visible.
   ===================================================================== */
(() => {
    'use strict';

    const menosMovimiento = window.matchMedia('(prefers-reduced-motion: reduce)').matches;
    const principal = document.querySelector('.hs-nosotros');
    const elementos = document.querySelectorAll('[data-revelar]');

    if (!principal || !elementos.length || menosMovimiento || !('IntersectionObserver' in window)) {
        return;
    }

    principal.classList.add('is-animado');

    const observador = new IntersectionObserver((entradas) => {
        entradas
            .filter((entrada) => entrada.isIntersecting)
            .forEach((entrada) => {
                entrada.target.classList.add('is-visible');
                observador.unobserve(entrada.target);
            });
    }, { threshold: 0.15, rootMargin: '0px 0px -8% 0px' });

    elementos.forEach((elemento) => observador.observe(elemento));
})();