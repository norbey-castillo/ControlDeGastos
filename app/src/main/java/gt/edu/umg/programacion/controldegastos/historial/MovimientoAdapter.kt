
package gt.edu.umg.programacion.controldegastos.historial

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import gt.edu.umg.programacion.controldegastos.R
import gt.edu.umg.programacion.core.model.Movimiento
import gt.edu.umg.programacion.core.model.TipoMovimiento
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Adaptador encargado de mostrar los movimientos en el RecyclerView.
class MovimientoAdapter(
    private var movimientos: List<Movimiento>
) : RecyclerView.Adapter<MovimientoAdapter.MovimientoViewHolder>() {

    // Representa cada elemento individual de la lista.
    class MovimientoViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        val descripcion: TextView =
            itemView.findViewById(R.id.tvHistorialDescripcion)

        val categoria: TextView =
            itemView.findViewById(R.id.tvHistorialCategoria)

        val fecha: TextView =
            itemView.findViewById(R.id.tvHistorialFecha)

        val monto: TextView =
            itemView.findViewById(R.id.tvHistorialMonto)
    }

    // Crea una fila utilizando el diseño item_historial.xml.
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MovimientoViewHolder {

        val vista = LayoutInflater.from(parent.context).inflate(
            R.layout.item_historial,
            parent,
            false
        )

        return MovimientoViewHolder(vista)
    }

    // Asigna los datos de un movimiento a su respectiva fila.
    override fun onBindViewHolder(
        holder: MovimientoViewHolder,
        position: Int
    ) {

        val movimiento = movimientos[position]

        holder.descripcion.text = movimiento.descripcion
        holder.categoria.text = movimiento.categoria

        // Convierte la fecha almacenada en un formato legible.
        val formatoFecha = SimpleDateFormat(
            "dd/MM/yyyy",
            Locale.getDefault()
        )

        holder.fecha.text = formatoFecha.format(
            Date(movimiento.fecha)
        )

        // Diferencia visualmente los ingresos de los gastos.
        if (movimiento.tipo == TipoMovimiento.INGRESO) {
            holder.monto.text = "+ Q %.2f".format(movimiento.monto)
            holder.monto.setTextColor(Color.rgb(33, 134, 83))
        } else {
            holder.monto.text = "- Q %.2f".format(movimiento.monto)
            holder.monto.setTextColor(Color.rgb(195, 77, 67))
        }
    }

    // Devuelve la cantidad de movimientos que contiene la lista.
    override fun getItemCount(): Int {
        return movimientos.size
    }

    // Reemplaza los datos del adaptador y actualiza la lista visual.
    fun actualizarDatos(nuevosMovimientos: List<Movimiento>) {
        movimientos = nuevosMovimientos
        notifyDataSetChanged()
    }
}
