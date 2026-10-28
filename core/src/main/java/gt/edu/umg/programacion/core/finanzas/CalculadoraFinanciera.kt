package gt.edu.umg.programacion.core.finanzas

import gt.edu.umg.programacion.core.model.Movimiento
import gt.edu.umg.programacion.core.model.TipoMovimiento

/*Aca en este archivo lo que vamos a poner son los calculos
  financieros de la aplicacion
 */



class CalculadoraFinanciera {

    fun calcularIngresos(movimientos: List<Movimiento>): Double {
        return movimientos
            .filter { it.tipo == TipoMovimiento.INGRESO }
            .sumOf { it.monto }
    }

    fun calcularGastos(movimientos: List<Movimiento>): Double {
        return movimientos
            .filter { it.tipo == TipoMovimiento.GASTO }
            .sumOf { it.monto }
    }

    fun calcularSaldo(movimientos: List<Movimiento>): Double {
        return calcularIngresos(movimientos) -
                calcularGastos(movimientos)
    }

}