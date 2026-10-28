
package gt.edu.umg.programacion.controldegastos

import gt.edu.umg.programacion.controldegastos.data.MovimientoDbHelper
import android.os.Bundle
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import gt.edu.umg.programacion.core.finanzas.CalculadoraFinanciera
import gt.edu.umg.programacion.core.model.Movimiento
import gt.edu.umg.programacion.core.model.TipoMovimiento
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private val movimientos = mutableListOf<Movimiento>()
    private val calculadora = CalculadoraFinanciera()
    private lateinit var dbHelper: MovimientoDbHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        /*Aca se van a recuperar los movimientos guardados cada vez que se cierra la aplicacion*/
        dbHelper = MovimientoDbHelper(this)
        movimientos.addAll(dbHelper.obtenerMovimientos())

        val btnIngreso = findViewById<Button>(R.id.btnIngreso)
        val btnGasto = findViewById<Button>(R.id.btnGasto)

        btnIngreso.setOnClickListener {
            mostrarFormulario(TipoMovimiento.INGRESO)
        }

        btnGasto.setOnClickListener {
            mostrarFormulario(TipoMovimiento.GASTO)
        }

        actualizarDashboard()
    }
/*Esta funcion es la que vamos a usar para mostrar el formulario en pantalla*/
    private fun mostrarFormulario(tipo: TipoMovimiento) {
        val formulario = layoutInflater.inflate(
            R.layout.dialog_movimiento,
            null
        )

        val etDescripcion =
            formulario.findViewById<EditText>(R.id.etDescripcion)

        val etMonto =
            formulario.findViewById<EditText>(R.id.etMonto)

        val etCategoria =
            formulario.findViewById<EditText>(R.id.etCategoria)

        val dialogo = AlertDialog.Builder(this)
            .setTitle(
                if (tipo == TipoMovimiento.INGRESO) {
                    "Registrar ingreso"
                } else {
                    "Registrar gasto"
                }
            )
            .setView(formulario)
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Guardar", null)
            .create()

        dialogo.setOnShowListener {
            dialogo.getButton(AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener {
                    val descripcion =
                        etDescripcion.text.toString().trim()

                    val montoTexto =
                        etMonto.text.toString().trim().replace(",", ".")

                    val categoria =
                        etCategoria.text.toString().trim()

                    val monto = montoTexto.toDoubleOrNull()

                    if (
                        descripcion.isBlank() ||
                        categoria.isBlank() ||
                        monto == null ||
                        !monto.isFinite() ||
                        monto <= 0
                    ) {
                        Toast.makeText(
                            this,
                            "Completa los campos y escribe un monto válido.",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        val movimiento = Movimiento(
                            descripcion = descripcion,
                            monto = monto,
                            tipo = tipo,
                            categoria = categoria
                        )

                        val resultado = dbHelper.insertarMovimiento(movimiento)

                        if (resultado != -1L){
                            movimientos.clear()
                            movimientos.addAll(dbHelper.obtenerMovimientos())

                            actualizarDashboard()

                            Toast.makeText(
                                this,
                                "Movimiento guardado correctamente.",
                                Toast.LENGTH_LONG
                            ).show()

                            dialogo.dismiss()
                        }else{
                            Toast.makeText(
                                this,
                                "No se pudo guardar el movimiento.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
        }

        dialogo.show()
    }

    private fun actualizarDashboard() {
        val ingresos = calculadora.calcularIngresos(movimientos)
        val gastos = calculadora.calcularGastos(movimientos)
        val saldo = calculadora.calcularSaldo(movimientos)

        findViewById<TextView>(R.id.tvIngresos).text =
            formatearMoneda(ingresos)

        findViewById<TextView>(R.id.tvGastos).text =
            formatearMoneda(gastos)

        findViewById<TextView>(R.id.tvSaldo).text =
            formatearMoneda(saldo)

        val contenedor =
            findViewById<LinearLayout>(R.id.contenedorMovimientos)

        val mensajeVacio =
            findViewById<TextView>(R.id.tvMovimientosVacios)

        contenedor.removeViews(
            1,
            (contenedor.childCount - 1).coerceAtLeast(0)
        )

        mensajeVacio.visibility =
            if (movimientos.isEmpty()) View.VISIBLE else View.GONE

        for (movimiento in movimientos.asReversed()) {
            val fila = LayoutInflater.from(this).inflate(
                R.layout.item_movimiento,
                contenedor,
                false
            )

            fila.findViewById<TextView>(
                R.id.tvDescripcionMovimiento
            ).text = movimiento.descripcion

            fila.findViewById<TextView>(
                R.id.tvCategoriaMovimiento
            ).text = movimiento.categoria

            val tvMonto = fila.findViewById<TextView>(
                R.id.tvMontoMovimiento
            )

            val signo =
                if (movimiento.tipo == TipoMovimiento.INGRESO) "+" else "−"

            tvMonto.text = "$signo ${formatearMoneda(movimiento.monto)}"

            tvMonto.setTextColor(
                if (movimiento.tipo == TipoMovimiento.INGRESO) {
                    Color.rgb(33, 134, 83)
                } else {
                    Color.rgb(195, 77, 67)
                }
            )

            contenedor.addView(fila)
        }
    }

    private fun formatearMoneda(monto: Double): String {
        return String.format(Locale.US, "Q %.2f", monto)
    }
}
