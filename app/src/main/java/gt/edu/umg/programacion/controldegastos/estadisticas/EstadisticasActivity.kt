package gt.edu.umg.programacion.controldegastos.estadisticas

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import gt.edu.umg.programacion.controldegastos.R
import gt.edu.umg.programacion.controldegastos.data.MovimientoDbHelper
import gt.edu.umg.programacion.core.finanzas.CalculadoraFinanciera
import gt.edu.umg.programacion.core.model.Movimiento
import gt.edu.umg.programacion.core.model.TipoMovimiento
import java.util.Locale

// Actividad encargada de mostrar las estadísticas financieras.
class EstadisticasActivity : AppCompatActivity() {

    private lateinit var dbHelper: MovimientoDbHelper

    private val calculadora = CalculadoraFinanciera()

    private lateinit var contenedorCategorias: LinearLayout
    private lateinit var tvSinEstadisticas: TextView


    /*Inicializacion de la actividad y componentes visuales*/
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_estadisticas)

        // Inicializa el administrador de SQLite.
        dbHelper = MovimientoDbHelper(this)

        // Relaciona los elementos del XML con Kotlin.
        contenedorCategorias =
            findViewById(R.id.contenedorCategorias)

        tvSinEstadisticas =
            findViewById(R.id.tvSinEstadisticas)
    }


    /*Actualizacion de estadisticas al regresar a la pantalla.*/
    override fun onResume() {
        super.onResume()

        // Actualiza las estadísticas cada vez que se abre la pantalla.
        cargarEstadisticas()
    }

    // Recupera los movimientos y calcula los resultados financieros.
    private fun cargarEstadisticas() {

        val movimientos = dbHelper.obtenerMovimientos()

        // Calcula ingresos, gastos y saldo.
        val ingresos = calculadora.calcularIngresos(movimientos)
        val gastos = calculadora.calcularGastos(movimientos)
        val saldo = calculadora.calcularSaldo(movimientos)

        // Muestra los resultados en pantalla.
        findViewById<TextView>(
            R.id.tvEstadisticaIngresos
        ).text = formatearMoneda(ingresos)

        findViewById<TextView>(
            R.id.tvEstadisticaGastos
        ).text = formatearMoneda(gastos)

        findViewById<TextView>(
            R.id.tvEstadisticaSaldo
        ).text = formatearMoneda(saldo)

        // Calcula los gastos agrupados por categoría.
        mostrarGastosPorCategoria(movimientos, gastos)
    }

    // Agrupa los gastos y calcula qué porcentaje representa cada categoría.
    private fun mostrarGastosPorCategoria(
        movimientos: List<Movimiento>,
        totalGastos: Double
    ) {

        // Limpia los resultados anteriores.
        contenedorCategorias.removeAllViews()

        // Filtra únicamente los movimientos de tipo gasto.
        val gastos = movimientos.filter {
            it.tipo == TipoMovimiento.GASTO
        }

        // Agrupa los gastos por categoría y suma sus montos.
        val gastosPorCategoria = gastos
            .groupBy { it.categoria }
            .mapValues { (_, lista) ->
                lista.sumOf { it.monto }
            }
            .toList()
            .sortedByDescending { it.second }

        // Muestra un mensaje si no existen gastos.
        tvSinEstadisticas.visibility =
            if (gastosPorCategoria.isEmpty()) {
                View.VISIBLE
            } else {
                View.GONE
            }

        // Crea una fila para cada categoría.
        for ((categoria, monto) in gastosPorCategoria) {

            // Calcula el porcentaje que representa la categoría.
            val porcentaje =
                if (totalGastos > 0) {
                    (monto / totalGastos) * 100
                } else {
                    0.0
                }

            val fila = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(16, 16, 16, 16)
                setBackgroundColor(Color.WHITE)

                val parametros = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )

                parametros.bottomMargin = 12
                layoutParams = parametros
            }

            // Muestra el nombre de la categoría.
            val tvCategoria = TextView(this).apply {
                text = categoria
                textSize = 16f
                setTextColor(Color.rgb(23, 61, 43))
            }

            // Muestra el monto gastado y su porcentaje.
            val tvDetalle = TextView(this).apply {
                text = "${formatearMoneda(monto)}  -  " +
                        String.format(Locale.US, "%.1f%%", porcentaje)

                textSize = 14f
                setTextColor(Color.rgb(102, 117, 107))
                setPadding(0, 8, 0, 0)
            }

            // Agrega los textos a la fila.
            fila.addView(tvCategoria)
            fila.addView(tvDetalle)

            // Agrega la fila al contenedor principal.
            contenedorCategorias.addView(fila)
        }
    }

    // Convierte un número a formato de moneda guatemalteca.
    private fun formatearMoneda(monto: Double): String {
        return String.format(Locale.US, "Q %.2f", monto)
    }
}
