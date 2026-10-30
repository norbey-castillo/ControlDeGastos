package gt.edu.umg.programacion.controldegastos.historial

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import gt.edu.umg.programacion.controldegastos.R
import gt.edu.umg.programacion.controldegastos.data.MovimientoDbHelper
import gt.edu.umg.programacion.core.model.Movimiento
import gt.edu.umg.programacion.core.model.TipoMovimiento
import android.widget.Button

// Actividad encargada de mostrar y filtrar el historial.
class HistorialActivity : AppCompatActivity() {

    private lateinit var dbHelper: MovimientoDbHelper
    private lateinit var adapter: MovimientoAdapter

    private lateinit var rvHistorial: RecyclerView
    private lateinit var tvHistorialVacio: TextView
    private lateinit var tvTotalMovimientos: TextView
    private lateinit var spFiltroTipo: Spinner
    private lateinit var spFiltroCategoria: Spinner

    private var movimientos = listOf<Movimiento>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_historial)

        // Quitamos el color automático del tema de Android
        val btnVolver = findViewById<Button>(R.id.btnVolverHistorial)

        btnVolver.backgroundTintList = null
        btnVolver.setBackgroundResource(R.drawable.bg_boton_secundario)

        // Permite regresar al dashboard principal.
        findViewById<android.widget.Button>(
            R.id.btnVolverHistorial
        ).setOnClickListener {
            finish()
        }

        // Inicializa el administrador de SQLite.
        dbHelper = MovimientoDbHelper(this)

        // Relaciona las vistas del XML con Kotlin.
        rvHistorial = findViewById(R.id.rvHistorial)
        tvHistorialVacio = findViewById(R.id.tvHistorialVacio)
        tvTotalMovimientos = findViewById(R.id.tvTotalMovimientos)
        spFiltroTipo = findViewById(R.id.spFiltroTipo)
        spFiltroCategoria = findViewById(R.id.spFiltroCategoria)

        // Configura la lista vertical de movimientos.
        rvHistorial.layoutManager = LinearLayoutManager(this)

        // Inicializa el adaptador sin movimientos.
        adapter = MovimientoAdapter(emptyList())
        rvHistorial.adapter = adapter

        configurarFiltros()
    }

    override fun onResume() {
        super.onResume()

        // Recupera los movimientos cada vez que se muestra la pantalla.
        cargarMovimientos()
    }

    // Consulta los registros almacenados en SQLite.
    private fun cargarMovimientos() {
        movimientos = dbHelper.obtenerMovimientos()

        configurarCategorias()
        aplicarFiltros()
    }

    // Configura las opciones para filtrar por tipo.
    private fun configurarFiltros() {
        val tipos = listOf(
            "Todos",
            "Ingresos",
            "Gastos"
        )


        val spinnerAdapter = ArrayAdapter(
            this,
            R.layout.spinner_item,
            tipos
        )

        spinnerAdapter.setDropDownViewResource(
            R.layout.spinner_dropdown_item
        )

        spFiltroTipo.adapter = spinnerAdapter


        spFiltroTipo.onItemSelectedListener =
            object : android.widget.AdapterView.OnItemSelectedListener {

                override fun onItemSelected(
                    parent: android.widget.AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    aplicarFiltros()
                }

                override fun onNothingSelected(
                    parent: android.widget.AdapterView<*>?
                ) {}
            }
    }

    // Obtiene las categorías existentes y las coloca en el segundo Spinner.
    private fun configurarCategorias() {
        val categorias = listOf("Todas") +
                movimientos.map { it.categoria }
                    .distinct()
                    .sorted()


        val spinnerAdapter = ArrayAdapter(
            this,
            R.layout.spinner_item,
            categorias
        )

        spinnerAdapter.setDropDownViewResource(
            R.layout.spinner_dropdown_item
        )

        spFiltroCategoria.adapter = spinnerAdapter


        spFiltroCategoria.onItemSelectedListener =
            object : android.widget.AdapterView.OnItemSelectedListener {

                override fun onItemSelected(
                    parent: android.widget.AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    aplicarFiltros()
                }

                override fun onNothingSelected(
                    parent: android.widget.AdapterView<*>?
                ) {}
            }
    }

    // Aplica los filtros seleccionados por el usuario.
    private fun aplicarFiltros() {

        if (!::adapter.isInitialized) return

        val tipoSeleccionado =
            spFiltroTipo.selectedItem?.toString() ?: "Todos"

        val categoriaSeleccionada =
            spFiltroCategoria.selectedItem?.toString() ?: "Todas"

        val resultado = movimientos.filter { movimiento ->

            val coincideTipo = when (tipoSeleccionado) {
                "Ingresos" ->
                    movimiento.tipo == TipoMovimiento.INGRESO

                "Gastos" ->
                    movimiento.tipo == TipoMovimiento.GASTO

                else -> true
            }

            val coincideCategoria =
                categoriaSeleccionada == "Todas" ||
                        movimiento.categoria == categoriaSeleccionada

            coincideTipo && coincideCategoria
        }

        // Actualiza la lista del adaptador.
        adapter.actualizarDatos(resultado)

        // Actualiza el contador y el mensaje de lista vacía.
        tvTotalMovimientos.text =
            "Movimientos encontrados: ${resultado.size}"

        tvHistorialVacio.visibility =
            if (resultado.isEmpty()) View.VISIBLE else View.GONE

        rvHistorial.visibility =
            if (resultado.isEmpty()) View.GONE else View.VISIBLE
    }
}
