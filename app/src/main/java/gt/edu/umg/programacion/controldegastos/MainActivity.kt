package gt.edu.umg.programacion.controldegastos

import android.content.Intent
import gt.edu.umg.programacion.controldegastos.estadisticas.EstadisticasActivity
import gt.edu.umg.programacion.controldegastos.historial.HistorialActivity
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
import android.widget.ArrayAdapter

class MainActivity : AppCompatActivity() {

    private val movimientos = mutableListOf<Movimiento>()
    private val calculadora = CalculadoraFinanciera()
    private lateinit var dbHelper: MovimientoDbHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


// Obtenemos las referencias de los cuatro botones
        val btnIngreso = findViewById<Button>(R.id.btnIngreso)
        val btnGasto = findViewById<Button>(R.id.btnGasto)
        val btnHistorial = findViewById<Button>(R.id.btnHistorial)
        val btnEstadisticas = findViewById<Button>(R.id.btnEstadisticas)

// Quitamos el tinte automático aplicado por el tema de Android
        btnIngreso.backgroundTintList = null
        btnGasto.backgroundTintList = null
        btnHistorial.backgroundTintList = null
        btnEstadisticas.backgroundTintList = null

// Aplicamos nuestros fondos personalizados
        btnIngreso.setBackgroundResource(R.drawable.bg_boton_principal)

        btnGasto.setBackgroundResource(R.drawable.bg_boton_secundario)
        btnHistorial.setBackgroundResource(R.drawable.bg_boton_secundario)
        btnEstadisticas.setBackgroundResource(R.drawable.bg_boton_secundario)


        /*Aca se van a recuperar los movimientos guardados cada vez que se cierra la aplicacion*/
        dbHelper = MovimientoDbHelper(this)
        movimientos.addAll(dbHelper.obtenerMovimientos())

        btnEstadisticas.setOnClickListener {
            val intent = Intent(this, EstadisticasActivity::class.java)
            startActivity(intent)
        }

        btnHistorial.setOnClickListener {
            val intent = Intent(this, HistorialActivity::class.java)
            startActivity(intent)
        }

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

        // Cargamos el diseño personalizado del formulario.
        val formulario = layoutInflater.inflate(
            R.layout.dialog_movimiento,
            null
        )

        // Relacionamos los campos del formulario.
        val etDescripcion =
            formulario.findViewById<EditText>(R.id.etDescripcion)

        val etMonto =
            formulario.findViewById<EditText>(R.id.etMonto)

        val etCategoria =
            formulario.findViewById<EditText>(R.id.etCategoria)

        // Creamos un encabezado personalizado.
        val titulo = TextView(this).apply {
            text = if (tipo == TipoMovimiento.INGRESO) {
                "Registrar ingreso"
            } else {
                "Registrar gasto"
            }

            textSize = 22f
            setTextColor(android.graphics.Color.WHITE)
            setPadding(24, 24, 24, 24)
            setTypeface(null, android.graphics.Typeface.BOLD)

            // Fondo verde oscuro con esquinas superiores redondeadas.
            background = android.graphics.drawable.GradientDrawable().apply {
                setColor(android.graphics.Color.rgb(23, 63, 48))
                cornerRadii = floatArrayOf(
                    18f, 18f,
                    18f, 18f,
                    0f, 0f,
                    0f, 0f
                )
            }
        }

        // Construimos el diálogo.
        val dialogo = AlertDialog.Builder(this)
            .setCustomTitle(titulo)
            .setView(formulario)
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Guardar", null)
            .create()

        // Configuramos los colores cuando el diálogo aparece.
        dialogo.setOnShowListener {

            // Fondo blanco y esquinas redondeadas.
            dialogo.window?.setBackgroundDrawableResource(
                R.drawable.bg_dialogo
            )

            // Botón cancelar.
            dialogo.getButton(AlertDialog.BUTTON_NEGATIVE)
                .setTextColor(android.graphics.Color.rgb(23, 63, 48))

            // Botón guardar.
            dialogo.getButton(AlertDialog.BUTTON_POSITIVE)
                .setTextColor(android.graphics.Color.rgb(36, 132, 93))

            // Configuramos la acción de guardar.
            dialogo.getButton(AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener {

                    val descripcion =
                        etDescripcion.text.toString().trim()

                    val montoTexto =
                        etMonto.text.toString().trim().replace(",", ".")

                    val categoria =
                        etCategoria.text.toString().trim()

                    val monto = montoTexto.toDoubleOrNull()

                    // Validamos los datos ingresados.
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

                        // Creamos el movimiento.
                        val movimiento = Movimiento(
                            descripcion = descripcion,
                            monto = monto,
                            tipo = tipo,
                            categoria = categoria
                        )

                        // Guardamos el movimiento en SQLite.
                        val resultado =
                            dbHelper.insertarMovimiento(movimiento)

                        if (resultado != -1L) {

                            // Actualizamos la lista de movimientos.
                            movimientos.clear()
                            movimientos.addAll(
                                dbHelper.obtenerMovimientos()
                            )

                            // Actualizamos el Dashboard.
                            actualizarDashboard()

                            Toast.makeText(
                                this,
                                "Movimiento guardado correctamente.",
                                Toast.LENGTH_LONG
                            ).show()

                            dialogo.dismiss()

                        } else {

                            Toast.makeText(
                                this,
                                "No se pudo guardar el movimiento.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
        }

        // Mostramos el diálogo.
        dialogo.show()
    }



    // Muestra el formulario con los datos actuales del movimiento.

    private fun mostrarFormularioEdicion(movimiento: Movimiento) {

        // Cargamos el diseño del formulario.
        val formulario = layoutInflater.inflate(
            R.layout.dialog_movimiento,
            null
        )

        // Relacionamos los campos.
        val etDescripcion =
            formulario.findViewById<EditText>(R.id.etDescripcion)

        val etMonto =
            formulario.findViewById<EditText>(R.id.etMonto)

        val etCategoria =
            formulario.findViewById<EditText>(R.id.etCategoria)

        // Cargamos los datos actuales del movimiento.
        etDescripcion.setText(movimiento.descripcion)
        etMonto.setText(movimiento.monto.toString())
        etCategoria.setText(movimiento.categoria)

        // Creamos el encabezado personalizado.
        val titulo = TextView(this).apply {
            text = "Editar movimiento"
            textSize = 22f
            setTextColor(android.graphics.Color.WHITE)
            setPadding(24, 24, 24, 24)
            setTypeface(null, android.graphics.Typeface.BOLD)

            background = android.graphics.drawable.GradientDrawable().apply {
                setColor(android.graphics.Color.rgb(23, 63, 48))
                cornerRadii = floatArrayOf(
                    18f, 18f,
                    18f, 18f,
                    0f, 0f,
                    0f, 0f
                )
            }
        }

        // Construimos el diálogo.
        val dialogo = AlertDialog.Builder(this)
            .setCustomTitle(titulo)
            .setView(formulario)
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Guardar cambios", null)
            .create()

        dialogo.setOnShowListener {

            // Aplicamos el fondo personalizado.
            dialogo.window?.setBackgroundDrawableResource(
                R.drawable.bg_dialogo
            )

            // Personalizamos los botones.
            dialogo.getButton(AlertDialog.BUTTON_NEGATIVE)
                .setTextColor(android.graphics.Color.rgb(23, 63, 48))

            dialogo.getButton(AlertDialog.BUTTON_POSITIVE)
                .setTextColor(android.graphics.Color.rgb(36, 132, 93))

            // Configuramos la acción de guardar cambios.
            dialogo.getButton(AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener {

                    val descripcion =
                        etDescripcion.text.toString().trim()

                    val montoTexto =
                        etMonto.text.toString().trim().replace(",", ".")

                    val categoria =
                        etCategoria.text.toString().trim()

                    val monto = montoTexto.toDoubleOrNull()

                    // Validamos los datos.
                    if (
                        descripcion.isBlank() ||
                        categoria.isBlank() ||
                        monto == null ||
                        !monto.isFinite() ||
                        monto <= 0
                    ) {

                        Toast.makeText(
                            this,
                            "Ingresa datos válidos.",
                            Toast.LENGTH_LONG
                        ).show()

                    } else {

                        // Conservamos el ID y tipo originales.
                        val movimientoActualizado = movimiento.copy(
                            descripcion = descripcion,
                            monto = monto,
                            categoria = categoria
                        )

                        // Actualizamos el registro en SQLite.
                        val resultado =
                            dbHelper.actualizarMovimiento(
                                movimientoActualizado
                            )

                        if (resultado > 0) {

                            // Recuperamos los datos actualizados.
                            movimientos.clear()
                            movimientos.addAll(
                                dbHelper.obtenerMovimientos()
                            )

                            // Actualizamos el Dashboard.
                            actualizarDashboard()

                            Toast.makeText(
                                this,
                                "Movimiento actualizado.",
                                Toast.LENGTH_SHORT
                            ).show()

                            dialogo.dismiss()

                        } else {

                            Toast.makeText(
                                this,
                                "No se pudo actualizar el movimiento.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
        }

        dialogo.show()
    }

    // Solicita confirmación antes de eliminar un movimiento.

    private fun confirmarEliminacion(movimiento: Movimiento) {

        // Creamos el encabezado personalizado.
        val titulo = TextView(this).apply {
            text = "Eliminar movimiento"
            textSize = 22f
            setTextColor(android.graphics.Color.WHITE)
            setPadding(24, 24, 24, 24)
            setTypeface(null, android.graphics.Typeface.BOLD)

            background = android.graphics.drawable.GradientDrawable().apply {
                setColor(android.graphics.Color.rgb(23, 63, 48))
                cornerRadii = floatArrayOf(
                    18f, 18f,
                    18f, 18f,
                    0f, 0f,
                    0f, 0f
                )
            }
        }

        // Construimos el diálogo de confirmación.
        val dialogo = AlertDialog.Builder(this)
            .setCustomTitle(titulo)
            .setMessage(
                "¿Deseas eliminar \"${movimiento.descripcion}\"?"
            )
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Eliminar") { _, _ ->

                // Ejecuta DELETE en SQLite.
                val resultado =
                    dbHelper.eliminarMovimiento(movimiento.id)

                if (resultado > 0) {

                    // Recupera los registros restantes.
                    movimientos.clear()
                    movimientos.addAll(
                        dbHelper.obtenerMovimientos()
                    )

                    // Actualiza los totales y movimientos.
                    actualizarDashboard()

                    Toast.makeText(
                        this,
                        "Movimiento eliminado.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .create()

        // Personalizamos la apariencia del diálogo.
        dialogo.setOnShowListener {

            dialogo.window?.setBackgroundDrawableResource(
                R.drawable.bg_dialogo
            )

            // Color del mensaje.
            dialogo.findViewById<TextView>(android.R.id.message)
                ?.setTextColor(
                    android.graphics.Color.rgb(23, 63, 48)
                )

            // Botón cancelar.
            dialogo.getButton(AlertDialog.BUTTON_NEGATIVE)
                .setTextColor(
                    android.graphics.Color.rgb(23, 63, 48)
                )

            // Botón eliminar en rojo.
            dialogo.getButton(AlertDialog.BUTTON_POSITIVE)
                .setTextColor(
                    android.graphics.Color.rgb(195, 77, 67)
                )
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

            //Permite eliminar un movimiento manteniendo presionada su fila.

            fila.setOnLongClickListener {

                // Opciones disponibles para el movimiento.
                val opciones = arrayOf("Editar", "Eliminar")

                // Creamos un encabezado personalizado.
                val titulo = TextView(this).apply {
                    text = movimiento.descripcion
                    textSize = 20f
                    setTextColor(android.graphics.Color.WHITE)
                    setPadding(24, 24, 24, 24)
                    setTypeface(null, android.graphics.Typeface.BOLD)

                    background = android.graphics.drawable.GradientDrawable().apply {
                        setColor(android.graphics.Color.rgb(23, 63, 48))
                        cornerRadii = floatArrayOf(
                            18f, 18f,
                            18f, 18f,
                            0f, 0f,
                            0f, 0f
                        )
                    }
                }

                // Adaptador personalizado para las opciones.
                val opcionesAdapter = object : ArrayAdapter<String>(
                    this,
                    android.R.layout.simple_list_item_1,
                    opciones
                ) {
                    override fun getView(
                        position: Int,
                        convertView: android.view.View?,
                        parent: android.view.ViewGroup
                    ): android.view.View {

                        val texto = super.getView(
                            position,
                            convertView,
                            parent
                        ) as TextView

                        texto.setTextColor(
                            if (position == 0) {
                                android.graphics.Color.rgb(23, 63, 48)
                            } else {
                                android.graphics.Color.rgb(195, 77, 67)
                            }
                        )

                        texto.textSize = 17f
                        texto.setPadding(24, 18, 24, 18)

                        return texto
                    }
                }

                // Construimos el menú personalizado.
                val dialogo = AlertDialog.Builder(this)
                    .setCustomTitle(titulo)
                    .setAdapter(opcionesAdapter) { _, seleccion ->

                        when (seleccion) {
                            0 -> mostrarFormularioEdicion(movimiento)
                            1 -> confirmarEliminacion(movimiento)
                        }
                    }
                    .create()

                // Aplicamos el fondo personalizado.
                dialogo.setOnShowListener {
                    dialogo.window?.setBackgroundDrawableResource(
                        R.drawable.bg_dialogo
                    )
                }

                dialogo.show()

                true
            }


        }
    }

    private fun formatearMoneda(monto: Double): String {
        return String.format(Locale.US, "Q %.2f", monto)
    }
}
