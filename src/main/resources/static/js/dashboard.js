// Refresco periódico del Dashboard vía API REST cada 60 segundos
document.addEventListener('DOMContentLoaded', () => {
    function actualizarDashboard() {
        fetch('/api/dashboard')
            .then(res => {
                if (!res.ok) throw new Error('Error al consultar métricas');
                return res.json();
            })
            .then(data => {
                if (data.pedidosPendientes !== undefined) {
                    const elPend = document.getElementById('val-pedidos-pendientes');
                    if (elPend) elPend.textContent = data.pedidosPendientes;
                }
                if (data.pedidosHoy !== undefined) {
                    const elHoy = document.getElementById('val-pedidos-hoy');
                    if (elHoy) elHoy.textContent = data.pedidosHoy;
                }
                if (data.ventasHoy !== undefined) {
                    const elVentas = document.getElementById('val-ventas-hoy');
                    if (elVentas) {
                        const formateado = new Intl.NumberFormat('es-PY').format(data.ventasHoy);
                        elVentas.textContent = formateado + ' Gs.';
                    }
                }
                if (data.stockBajo !== undefined) {
                    const elStock = document.getElementById('val-stock-bajo');
                    if (elStock) elStock.textContent = data.stockBajo;
                }
                if (data.productosActivos !== undefined) {
                    const elProd = document.getElementById('val-productos-activos');
                    if (elProd) elProd.textContent = data.productosActivos;
                }
            })
            .catch(err => console.debug('Auto-refresh dashboard omitido:', err));
    }

    // Intervalo de 60 segundos (60000 ms)
    setInterval(actualizarDashboard, 60000);
});
