document.addEventListener('DOMContentLoaded', () => {
    // Helper reutilizable de paginación en cliente
    function crearPaginador({ filasSelector, wrapId, infoId, numId, prevId, nextId, porPagina = 10, filtroFn = () => true }) {
        const todas = Array.from(document.querySelectorAll(filasSelector));
        const wrap = document.getElementById(wrapId);
        if (!todas.length || !wrap) return { reiniciar: () => {} };

        const info = document.getElementById(infoId);
        const num = document.getElementById(numId);
        const btnPrev = document.getElementById(prevId);
        const btnNext = document.getElementById(nextId);
        let pagina = 1;

        function render() {
            const visibles = todas.filter(filtroFn);
            const totalPaginas = Math.max(1, Math.ceil(visibles.length / porPagina));
            if (pagina > totalPaginas) pagina = totalPaginas;

            const inicio = (pagina - 1) * porPagina;
            const fin = inicio + porPagina;

            todas.forEach(tr => tr.hidden = true);
            visibles.slice(inicio, fin).forEach(tr => tr.hidden = false);

            wrap.hidden = visibles.length <= porPagina;
            if (info) {
                const desde = visibles.length ? inicio + 1 : 0;
                const hasta = Math.min(fin, visibles.length);
                info.textContent = `Mostrando ${desde}–${hasta} de ${visibles.length}`;
            }
            if (num) num.textContent = `${pagina} / ${totalPaginas}`;
            if (btnPrev) btnPrev.disabled = pagina <= 1;
            if (btnNext) btnNext.disabled = pagina >= totalPaginas;
        }

        if (btnPrev) btnPrev.addEventListener('click', () => { if (pagina > 1) { pagina--; render(); } });
        if (btnNext) btnNext.addEventListener('click', () => { pagina++; render(); });

        render();
        return { reiniciar: () => { pagina = 1; render(); } };
    }

    // 1. Inventario: filtro + paginación de 10 en 10
    let soloCritico = false;
    const pagInv = crearPaginador({
        filasSelector: '.hs-inv-row',
        wrapId: 'inv-paginacion',
        infoId: 'inv-paginacion-info',
        numId: 'inv-paginacion-num',
        prevId: 'inv-prev',
        nextId: 'inv-next',
        porPagina: 10,
        filtroFn: tr => !soloCritico || tr.dataset.critico === 'true'
    });

    const invTabs = document.querySelectorAll('[data-inv-filter]');
    invTabs.forEach(btn => {
        btn.addEventListener('click', () => {
            invTabs.forEach(t => t.classList.remove('is-active'));
            btn.classList.add('is-active');
            soloCritico = btn.dataset.invFilter === 'CRITICO';
            pagInv.reiniciar();
        });
    });

    // 2. Proveedores: paginación de órdenes (8 en 8)
    crearPaginador({
        filasSelector: '.hs-prov-row',
        wrapId: 'prov-paginacion',
        infoId: 'prov-paginacion-info',
        numId: 'prov-paginacion-num',
        prevId: 'prov-prev',
        nextId: 'prov-next',
        porPagina: 8
    });

    // 3. Autocompletado de RUC SUNAT al registrar taller/proveedor
    const rucInput = document.getElementById('provRuc');
    const nombreInput = document.getElementById('provNombre');
    const avisoRuc = document.getElementById('avisoRucProv');
    if (rucInput && nombreInput) {
        rucInput.addEventListener('input', async () => {
            const ruc = rucInput.value.trim();
            if (ruc.length === 11 && /^[0-9]{11}$/.test(ruc)) {
                if (avisoRuc) avisoRuc.textContent = 'Consultando SUNAT...';
                try {
                    const res = await fetch('/api/documento/RUC/' + ruc);
                    if (res.ok) {
                        const data = await res.json();
                        if (data.nombreCompleto) nombreInput.value = data.nombreCompleto;
                        if (avisoRuc) avisoRuc.textContent = 'RUC verificado (' + data.fuente + ').';
                    } else if (avisoRuc) {
                        avisoRuc.textContent = '';
                    }
                } catch (_) {
                    if (avisoRuc) avisoRuc.textContent = '';
                }
            }
        });
    }
});
