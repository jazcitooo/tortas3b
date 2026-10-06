// Lógica interactiva para el formulario de pedidos
document.addEventListener('DOMContentLoaded', () => {

    // --- 1. Alternar sección de Delivery según tipo de entrega ---
    const selectTipoEntrega = document.getElementById('tipoEntrega');
    const seccionDelivery = document.getElementById('seccion-delivery');
    const inputDeliveryDireccion = document.getElementById('deliveryDireccion');
    const inputDeliveryTelefono = document.getElementById('deliveryTelefono');

    function actualizarVisibilidadDelivery() {
        if (!selectTipoEntrega || !seccionDelivery) return;
        const esDelivery = selectTipoEntrega.value === 'DELIVERY';
        seccionDelivery.style.display = esDelivery ? 'block' : 'none';
        if (inputDeliveryDireccion) inputDeliveryDireccion.required = esDelivery;
        if (inputDeliveryTelefono) inputDeliveryTelefono.required = esDelivery;
    }

    if (selectTipoEntrega) {
        selectTipoEntrega.addEventListener('change', actualizarVisibilidadDelivery);
        actualizarVisibilidadDelivery();
    }

    // --- 2. Tabla dinámica de productos y cálculo de subtotales ---
    const contenedorFilas = document.getElementById('filas-productos');
    const btnAgregarProducto = document.getElementById('btn-agregar-producto');
    const elTotalGeneral = document.getElementById('total-general-texto');
    const templateFila = document.getElementById('template-fila-producto');

    function formatearGs(monto) {
        return new Intl.NumberFormat('es-PY').format(monto) + ' Gs.';
    }

    function recalcularTotales() {
        let total = 0;
        document.querySelectorAll('.fila-producto').forEach(fila => {
            const selectProd = fila.querySelector('.select-producto');
            const inputCant = fila.querySelector('.input-cantidad');
            const celdaSubtotal = fila.querySelector('.celda-subtotal');

            const optionSeleccionada = selectProd.options[selectProd.selectedIndex];
            const precio = optionSeleccionada ? parseFloat(optionSeleccionada.dataset.precio || 0) : 0;
            const cantidad = parseInt(inputCant.value || 0, 10);

            const subtotal = precio * cantidad;
            if (celdaSubtotal) {
                celdaSubtotal.textContent = formatearGs(subtotal);
            }
            total += subtotal;
        });

        if (elTotalGeneral) {
            elTotalGeneral.textContent = formatearGs(total);
        }
    }

    function vincularEventosFila(fila) {
        const selectProd = fila.querySelector('.select-producto');
        const inputCant = fila.querySelector('.input-cantidad');
        const btnEliminar = fila.querySelector('.btn-eliminar-fila');

        if (selectProd) selectProd.addEventListener('change', recalcularTotales);
        if (inputCant) inputCant.addEventListener('input', recalcularTotales);
        if (btnEliminar) {
            btnEliminar.addEventListener('click', () => {
                const totalFilas = document.querySelectorAll('.fila-producto').length;
                if (totalFilas > 1) {
                    fila.remove();
                    recalcularTotales();
                } else {
                    alert('El pedido debe incluir al menos un producto.');
                }
            });
        }
    }

    if (btnAgregarProducto && templateFila && contenedorFilas) {
        btnAgregarProducto.addEventListener('click', () => {
            const clon = templateFila.content.cloneNode(true);
            const nuevaFila = clon.querySelector('.fila-producto');
            contenedorFilas.appendChild(nuevaFila);
            vincularEventosFila(nuevaFila);
            recalcularTotales();
        });
    }

    // Vincular filas existentes al cargar
    document.querySelectorAll('.fila-producto').forEach(vincularEventosFila);
    recalcularTotales();
});
