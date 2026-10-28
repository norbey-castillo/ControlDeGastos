package gt.edu.umg.programacion.core.model

data class Movimiento(
    val id: Long = 0,
    val descripcion: String,
    val monto: Double,
    val tipo: TipoMovimiento,
    val categoria: String,
    val fecha: Long = System.currentTimeMillis()
)

enum class TipoMovimiento {
    INGRESO,
    GASTO
}