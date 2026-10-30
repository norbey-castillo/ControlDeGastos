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
import android.widget.Button

import android.content.res.ColorStateList
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.widget.ProgressBar


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

        // Aplicamos el fondo personalizado al botón de regreso.
        val btnVolver = findViewById<Button>(R.id.btnVolverEstadisticas)

        btnVolver.backgroundTintList = null
        btnVolver.setBackgroundResource(R.drawable.bg_boton_secundario)

        // Inicializa el administrador de SQLite.
        dbHelper = MovimientoDbHelper(this)

        // Relaciona los elementos del XML con Kotlin.
        contenedorCategorias =
            findViewById(R.id.contenedorCategorias)

        tvSinEstadisticas =
            findViewById(R.id.tvSinEstadisticas)

        // Permite regresar al dashboard principal.
        findViewById<Button>(
            R.id.btnVolverEstadisticas
        ).setOnClickListener {
            finish()
        }
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
        mostrarGastosPorCategoria(movimientos, gastos,)
    }

    // Agrupa los gastos y calcula qué porcentaje representa cada categoría.

    // Agrupa los gastos y muestra su distribución por categoría.

    // Agrupa los gastos y muestra su distribución por categoría.
    private fun mostrarGastosPorCategoria(
        movimientos: List<Movimiento>,
        totalGastos: Double
    ) {

        // Eliminamos las tarjetas anteriores para evitar duplicados.
        contenedorCategorias.removeAllViews()

        // Obtenemos únicamente los movimientos que son gastos.
        val gastos = movimientos.filter {
            it.tipo == TipoMovimiento.GASTO
        }

        // Agrupamos los gastos por categoría y sumamos sus montos.
        val gastosPorCategoria = gastos
            .groupBy { it.categoria }
            .mapValues { (_, lista) ->
                lista.sumOf { it.monto }
            }
            .toList()
            .sortedByDescending { it.second }

        // Mostramos un mensaje si no existen gastos registrados.
        tvSinEstadisticas.visibility =
            if (gastosPorCategoria.isEmpty()) {
                View.VISIBLE
            } else {
                View.GONE
            }

        // Creamos una tarjeta para cada categoría.
        for ((categoria, monto) in gastosPorCategoria) {

            // Calculamos el porcentaje de participación.
            val porcentaje = if (totalGastos > 0) {
                (monto / totalGastos) * 100
            } else {
                0.0
            }

            // TARJETA PRINCIPAL
            val tarjeta = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(dp(18), dp(16), dp(18), dp(16))

                // Fondo blanco con esquinas redondeadas.
                background = GradientDrawable().apply {
                    setColor(Color.WHITE)
                    cornerRadius = dp(18).toFloat()
                }

                // Sombra suave.
                elevation = dp(3).toFloat()

                val parametros = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )

                parametros.bottomMargin = dp(14)
                layoutParams = parametros
            }

            // FILA SUPERIOR: CATEGORÍA Y MONTO
            val filaSuperior = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
            }

            // Nombre de la categoría.
            val tvCategoria = TextView(this).apply {
                text = categoria
                textSize = 16f
                setTextColor(Color.rgb(23, 63, 48))
                setTypeface(null, android.graphics.Typeface.BOLD)

                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                )
            }

            // Monto gastado.
            val tvMonto = TextView(this).apply {
                text = formatearMoneda(monto)
                textSize = 15f
                setTextColor(Color.rgb(36, 132, 93))
                setTypeface(null, android.graphics.Typeface.BOLD)
                gravity = Gravity.END
            }

            // Agregamos ambos textos a la fila.
            filaSuperior.addView(tvCategoria)
            filaSuperior.addView(tvMonto)

            // DETALLE DEL PORCENTAJE
            val tvPorcentaje = TextView(this).apply {
                text = String.format(
                    Locale.US,
                    "%.1f%% del total de gastos",
                    porcentaje
                )

                textSize = 13f
                setTextColor(Color.rgb(113, 128, 120))
                setPadding(0, dp(10), 0, dp(10))
            }

            // BARRA DE PROGRESO
            val barra = ProgressBar(
                this,
                null,
                android.R.attr.progressBarStyleHorizontal
            ).apply {

                // El porcentaje se representa en una escala de 0 a 100.
                max = 100
                progress = porcentaje.toInt().coerceIn(0, 100)

                // Color verde de la barra.
                progressTintList = ColorStateList.valueOf(
                    Color.rgb(36, 132, 93)
                )

                // Fondo gris claro de la barra.
                progressBackgroundTintList = ColorStateList.valueOf(
                    Color.rgb(226, 234, 228)
                )

                isIndeterminate = false
            }

            // Altura de la barra y ancho completo.
            val parametrosBarra = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(8)
            )

            // Agregamos los elementos a la tarjeta.
            tarjeta.addView(filaSuperior)
            tarjeta.addView(tvPorcentaje)
            tarjeta.addView(barra, parametrosBarra)

            // Agregamos la tarjeta al contenedor principal.
            contenedorCategorias.addView(tarjeta)
        }
    }

    // Convierte unidades dp a píxeles según la pantalla.
    private fun dp(valor: Int): Int {
        return (valor * resources.displayMetrics.density).toInt()
    }

    // Convierte un número a formato de moneda guatemalteca.
    private fun formatearMoneda(monto: Double): String {
        return String.format(Locale.US, "Q %.2f", monto)
    }
}
