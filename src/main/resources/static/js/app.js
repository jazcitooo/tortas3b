// =====================================================================
// Tortas y Chocolates 3B - JavaScript común a todas las páginas
// =====================================================================

document.addEventListener('DOMContentLoaded', () => {

    // --- Menú desplegable en teléfonos ---
    const botonMenu = document.querySelector('.menu-boton');
    const menu = document.getElementById('menu-principal');
    if (botonMenu && menu) {
        botonMenu.addEventListener('click', () => {
            const abierto = menu.classList.toggle('abierto');
            botonMenu.setAttribute('aria-expanded', abierto);
        });
    }

    // --- Marcar en el menú la sección actual ---
    document.querySelectorAll('.menu a').forEach(enlace => {
        const ruta = new URL(enlace.href).pathname;
        const actual = window.location.pathname;
        if (ruta === actual || (ruta !== '/' && actual.startsWith(ruta))) {
            enlace.classList.add('activo');
        }
    });

    // --- Confirmación antes de acciones peligrosas ---
    // Uso en HTML: <form ... data-confirmar="¿Desactivar este cliente?">
    const modal = document.getElementById('modal-confirmar');
    let formularioPendiente = null;

    document.querySelectorAll('form[data-confirmar]').forEach(form => {
        form.addEventListener('submit', evento => {
            if (form.dataset.confirmado === 'si' || !modal) {
                return; // ya confirmado (o la página no tiene ventana): se envía
            }
            evento.preventDefault();
            formularioPendiente = form;
            document.getElementById('modal-texto').textContent = form.dataset.confirmar;
            modal.hidden = false;
            document.getElementById('modal-cancelar').focus();
        });
    });

    if (modal) {
        document.getElementById('modal-cancelar').addEventListener('click', () => {
            modal.hidden = true;
            formularioPendiente = null;
        });
        document.getElementById('modal-aceptar').addEventListener('click', () => {
            modal.hidden = true;
            if (formularioPendiente) {
                formularioPendiente.dataset.confirmado = 'si';
                formularioPendiente.requestSubmit();
            }
        });
    }

    // --- Los mensajes de éxito desaparecen solos después de 6 segundos ---
    document.querySelectorAll('.alerta-exito').forEach(alerta => {
        setTimeout(() => alerta.remove(), 6000);
    });
});
