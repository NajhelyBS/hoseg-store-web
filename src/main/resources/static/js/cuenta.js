document.addEventListener('DOMContentLoaded', () => {
    // 1. Mostrar / ocultar contraseña en login, registro y cambio de clave
    document.querySelectorAll('[data-toggle-pass]').forEach(btn => {
        btn.addEventListener('click', () => {
            const input = document.getElementById(btn.dataset.togglePass);
            const icon = btn.querySelector('i');
            if (!input) return;
            const mostrar = input.type === 'password';
            input.type = mostrar ? 'text' : 'password';
            if (icon) {
                icon.classList.toggle('bi-eye', !mostrar);
                icon.classList.toggle('bi-eye-slash', mostrar);
            }
        });
    });

    // 2. Transición suave al cambiar entre pestañas de Mi Cuenta
    const panelTab = document.getElementById('cuenta-tab-panel');
    document.querySelectorAll('.hs-cuenta__pestanas a:not(.is-activa)').forEach(enlace => {
        enlace.addEventListener('click', (e) => {
            if (!panelTab || e.metaKey || e.ctrlKey) return;
            e.preventDefault();
            panelTab.classList.add('is-leaving');
            setTimeout(() => {
                window.location.assign(enlace.href);
            }, 160);
        });
    });

    // 3. Autocompletado de DNI (RENIEC) o RUC (SUNAT) en registro

    // 3. Autocompletado de DNI (RENIEC) o RUC (SUNAT) en registro

    // 3. Autocompletado de DNI (RENIEC) o RUC (SUNAT)
    const tipoSel = document.getElementById('tipoDoc');
    const numInput = document.getElementById('numDoc');
    const nombreInput = document.getElementById('nombre');
    const apellidoInput = document.getElementById('apellido');
    const dirInput = document.getElementById('direccion');

    if (tipoSel && numInput) {

        const estadoDoc = document.createElement('small');
        estadoDoc.className = 'form-text d-block mt-1';
        estadoDoc.style.fontSize = '12px';
        numInput.insertAdjacentElement('afterend', estadoDoc);

        let ultimaConsulta = '';

        function mostrarEstado(mensaje, color) {
            estadoDoc.textContent = mensaje;
            estadoDoc.style.color = color;
        }

        async function buscarDocumento() {

            const tipo = tipoSel.value;
            const num = numInput.value.trim();

            if (!((tipo === 'DNI' && num.length === 8) ||
                (tipo === 'RUC' && num.length === 11))) {
                return;
            }

            const clave = tipo + num;

            if (ultimaConsulta === clave) return;

            ultimaConsulta = clave;

            mostrarEstado('Consultando documento...', '#D69A35');

            try {

                const res = await fetch(
                    '/api/documento/' + tipo + '/' + encodeURIComponent(num)
                );

                if (!res.ok) {
                    mostrarEstado('No se encontraron datos.', '#C58A32');
                    ultimaConsulta = '';
                    return;
                }

                const data = await res.json();

                if (!data.nombres) {
                    mostrarEstado('No se encontraron datos.', '#C58A32');
                    ultimaConsulta = '';
                    return;
                }

                if (nombreInput) {
                    nombreInput.value = data.nombres;
                }

                if (apellidoInput) {
                    apellidoInput.value = data.apellidos || '';
                }

                if (data.direccion && dirInput && !dirInput.value) {
                    dirInput.value = data.direccion;
                }

                mostrarEstado('Documento validado correctamente.', '#27966A');

            } catch (error) {

                mostrarEstado('No se pudo realizar la consulta.', '#C58A32');
                ultimaConsulta = '';

                console.error('Error al consultar documento:', error);
            }
        }

        numInput.addEventListener('input', () => {
            ultimaConsulta = '';
            estadoDoc.textContent = '';

            const tipo = tipoSel.value;
            const len = numInput.value.trim().length;

            if ((tipo === 'DNI' && len === 8) ||
                (tipo === 'RUC' && len === 11)) {
                buscarDocumento();
            }
        });

        numInput.addEventListener('blur', buscarDocumento);

        tipoSel.addEventListener('change', () => {
            ultimaConsulta = '';
            estadoDoc.textContent = '';
        });
    }



    // 4. Botón para copiar código de comprobante o seguimiento en detalle de pedido
    document.querySelectorAll('[data-copy-target]').forEach(btn => {
        btn.addEventListener('click', () => {
            const target = document.getElementById(btn.dataset.copyTarget);
            const icon = btn.querySelector('i');
            if (!target || !navigator.clipboard) return;
            navigator.clipboard.writeText(target.textContent.trim()).then(() => {
                if (icon) {
                    icon.className = 'bi bi-check2';
                    setTimeout(() => { icon.className = 'bi bi-copy'; }, 1800);
                }
            });
        });
    });


    // 5. Indicador de coincidencia de contraseña en restablecer clave
    const pNueva = document.getElementById('nueva');
    const pConf = document.getElementById('confirmacion');
    const pMsg = document.getElementById('pass-match-msg');
    if (pNueva && pConf && pMsg) {
        const revisarCoincidencia = () => {
            const coinciden = pNueva.value.length > 0 && pNueva.value === pConf.value;
            pMsg.hidden = !coinciden;
        };
        pNueva.addEventListener('input', revisarCoincidencia);
        pConf.addEventListener('input', revisarCoincidencia);
    }


});
