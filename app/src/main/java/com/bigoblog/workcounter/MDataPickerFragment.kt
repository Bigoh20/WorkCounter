package com.bigoblog.workcounter

import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.DatePicker
import androidx.fragment.app.DialogFragment
import java.util.*

class MDataPickerFragment(val listener : (day : Int, month : Int, year : Int) -> Unit) : DialogFragment(), DatePickerDialog.OnDateSetListener {


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        //Crear el calendario con la fecha actual.
        val c = Calendar.getInstance()

        //Conseguir los valores del calendario.
        val day = c.get(Calendar.DAY_OF_MONTH)
        val month = c.get(Calendar.MONTH)
        val year = c.get(Calendar.YEAR)

        //Crear la instancia de la interfaz picker
        //USAR EL CONTEXTO, luego la ruta del diseño (no obligatorio), el listner usando this, y los parametros de donde inicia el picker
        val picker = DatePickerDialog(activity as Context, R.style.datePickerTheme, this, year, month, day)
        //Limitar la fecha maxima a hoy, porque obviamente no se irá más pa' delante.
        picker.datePicker.maxDate = c.timeInMillis //Recuperar el tiempo en millis con ese método <--
        return picker

    }


    override fun onDateSet(p0: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
          listener(dayOfMonth, month, year)
    }
}