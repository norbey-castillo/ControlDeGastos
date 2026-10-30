
package gt.edu.umg.programacion.controldegastos.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import gt.edu.umg.programacion.core.model.Movimiento
import gt.edu.umg.programacion.core.model.TipoMovimiento

class MovimientoDbHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "control_gastos.db"
        private const val DATABASE_VERSION = 1

        private const val TABLA = "movimientos"
        private const val COL_ID = "id"
        private const val COL_DESCRIPCION = "descripcion"
        private const val COL_MONTO = "monto"
        private const val COL_TIPO = "tipo"
        private const val COL_CATEGORIA = "categoria"
        private const val COL_FECHA = "fecha"
    }

    /*Creamos la tabla, columnas y definimos los tipos de datos*/
    override fun onCreate(db: SQLiteDatabase) {
        val crearTabla = """
            CREATE TABLE $TABLA (
                $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_DESCRIPCION TEXT NOT NULL,
                $COL_MONTO REAL NOT NULL,
                $COL_TIPO TEXT NOT NULL,
                $COL_CATEGORIA TEXT NOT NULL,
                $COL_FECHA INTEGER NOT NULL
            )
        """.trimIndent()

        db.execSQL(crearTabla)
    }

    override fun onUpgrade(
        db: SQLiteDatabase,
        oldVersion: Int,
        newVersion: Int
    ) {
        // Aquí agregaremos futuras migraciones de la base de datos.
    }
    /*ACA BASICAMENTE ESTA LA FUNCIONALIDAD DEL CRUD*/

    /*CON ESTA FUNCION SE INSERTA UN NUEVO REGISTRO EN LA TABLA (CREATE) */
    fun insertarMovimiento(movimiento: Movimiento): Long {
        val valores = ContentValues().apply {
            put(COL_DESCRIPCION, movimiento.descripcion)
            put(COL_MONTO, movimiento.monto)
            put(COL_TIPO, movimiento.tipo.name)
            put(COL_CATEGORIA, movimiento.categoria)
            put(COL_FECHA, movimiento.fecha)
        }

        return writableDatabase.insert(TABLA, null, valores)
    }

    /*CON ESTA FUNCION ACTUALIZAMOS UN REGISTRO EXISTENTE (UPDATE)*/
    fun actualizarMovimiento(movimiento: Movimiento): Int {
        //Prepara los nuevos valores que se almacenaran.
        val valores = ContentValues().apply {
            put("descripcion", movimiento.descripcion)
            put("monto", movimiento.monto)
            put("tipo", movimiento.tipo.name)
            put("categoria", movimiento.categoria)
            put("fecha", movimiento.fecha)
        }

        //Actualiza unicamente el registro cuyo ID coincida.
        return writableDatabase.update(
            TABLA,
            valores,
            "id=?",
            arrayOf(movimiento.id.toString()))
    }

    /*CON ESTA FUNCION SE RECUPERAN TODOS LOS REGISTROS DE LA TABLA (READ)*/
    fun obtenerMovimientos(): List<Movimiento> {
        val lista = mutableListOf<Movimiento>()

        val consulta = """
            SELECT * FROM $TABLA
            ORDER BY $COL_FECHA DESC, $COL_ID DESC
        """.trimIndent()

        readableDatabase.rawQuery(consulta, null).use { cursor ->
            while (cursor.moveToNext()) {
                val movimiento = Movimiento(
                    id = cursor.getLong(cursor.getColumnIndexOrThrow(COL_ID)),
                    descripcion = cursor.getString(
                        cursor.getColumnIndexOrThrow(COL_DESCRIPCION)
                    ),
                    monto = cursor.getDouble(
                        cursor.getColumnIndexOrThrow(COL_MONTO)
                    ),
                    tipo = TipoMovimiento.valueOf(
                        cursor.getString(
                            cursor.getColumnIndexOrThrow(COL_TIPO)
                        )
                    ),
                    categoria = cursor.getString(
                        cursor.getColumnIndexOrThrow(COL_CATEGORIA)
                    ),
                    fecha = cursor.getLong(
                        cursor.getColumnIndexOrThrow(COL_FECHA)
                    )
                )

                lista.add(movimiento)
            }
        }

        return lista
    }

    /*CON ESTA FUNCION SE ELIMINAN LOS REGISTROS DE LA TABLA (DELETE)*/
    fun eliminarMovimiento(id: Long): Int{

        //WritableDatabase permite realizar modificaciones.
        //El metodo delete elimina la fila cuyo ID coincida.
        return writableDatabase.delete(TABLA, "id=?", arrayOf(id.toString()))
    }

}
