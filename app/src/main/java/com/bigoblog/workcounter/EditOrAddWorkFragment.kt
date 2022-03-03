package com.bigoblog.workcounter

import android.media.MediaPlayer
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.doOnTextChanged
import com.bigoblog.workcounter.database.WorkEntity
import com.bigoblog.workcounter.database.WorkInit.Companion.database
import com.bigoblog.workcounter.databinding.FragmentAddBinding
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.lang.NumberFormatException


class EditOrAddWorkFragment(val id : Long? = 0L) : Fragment() {


    private lateinit var mBinding : FragmentAddBinding
    private var mActivity : MainActivity? = null
    //Crear la variable para saber si está seleccionado el tipo.
    private var isThick : Boolean? = null
    //Sonido que se reproducirá cuando algo va mal:
    private var soundWrong1 : MediaPlayer? = null
    private var isEditMode = false
    private var allFieldsOk = false
    private lateinit var work : WorkEntity //Variable en caso de que sea el modo de edición

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        mBinding = FragmentAddBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

     
        setupIsEditMode()
        setupActionBar()
        setupListeners()
        soundWrong1 = MediaPlayer.create(context, R.raw.wrong_selection)
      
    }

    private fun setupIsEditMode() {
        //Inicializar isEditMode si es que fue desde ahí. 
        if(id != null && id != 0L){
            doAsync {
                work = database.getDao().getWorkById(id) //conseguir el trabajo ya existente.
                uiThread {
                    renderData(work)
                }
            }
            isEditMode = true
        }    }

    private fun setupListeners() {
        mBinding.TIETDate.setOnClickListener { showDialogPicker() }
        mBinding.buttonCreate.setOnClickListener { createOrUpdateWork() }

        //Crear el listener del botón del tipo de afrecho.
        mBinding.buttonGroup.addOnButtonCheckedListener { group, checkedId, isChecked ->
            if (isChecked) {
                when (checkedId) {
                    R.id.button_fine -> isThick = false
                    R.id.button_thick -> isThick = true
                }
            } else {
                if (group.checkedButtonId == View.NO_ID) isThick = null
            }
        }
        

    }

    private fun renderData(work : WorkEntity) {
        mBinding.TIETAmount.setText(work.amount.toString())
        mBinding.TIETDate.setText(work.date)
        if(work.isThick) mBinding.buttonGroup.check(R.id.button_thick)
        else mBinding.buttonGroup.check(R.id.button_fine)


        mBinding.TIETDescription.setText(work.commentary)
        mBinding.TIETWats.setText(work.kbUsed.toString())
    }

    private fun setupActionBar() {
        mActivity = activity as? MainActivity

        //Cambiar el titulo
        if(isEditMode)  mActivity?.supportActionBar?.title = getString(R.string.title_edit)
        else mActivity?.supportActionBar?.title = getString(R.string.title_add)

        mActivity?.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setHasOptionsMenu(true)
    }

    private fun createOrUpdateWork(){

        try {
            //Solo checar en caso de que sea una nueva tienda.
            allFieldsOk = true

            //Recuperar los valores:
            val amount = mBinding.TIETAmount.text.toString()
            val date = mBinding.TIETDate.text.toString()
            var description = mBinding.TIETDescription.text.toString()
            var kwUsed = mBinding.TIETWats.text.toString()

            //Comprobar los campos sabiendo si están vacíos.

            if (amount.isEmpty()) {
                mBinding.TILAmount.error = getString(R.string.error_empty_text)
                allFieldsOk = false
            } else {
                mBinding.TILAmount.error = null
            }

            if (date.isEmpty()) {
                mBinding.TILDate.error = getString(R.string.error_empty_text)
                allFieldsOk = false
            } else {
                mBinding.TILDate.error = null
            }

            if (isThick == null) {
                allFieldsOk = false
                Toast.makeText(context, "Selecciona el tipo de afrecho", Toast.LENGTH_SHORT).show()
            }

            if (description.isEmpty()) {
                description = "Descripción vacía."
            }

            //Si no hay kw especificados, por defecto dividirlo entre 3 al total.
            if (kwUsed.isEmpty() && amount.isNotEmpty()) {
                kwUsed = (amount.toInt() / 3).toString()
            }
            //SI está ok, crear el objeto y subirlo o actualizarlo a la base de datos.
            if (allFieldsOk) {
                //@isThick nunca será nulo porque en ese caso esta condición no se cumpliría.

                if (isEditMode) {

                    //Actualizar el trabajo ya existente para no perder su ID
                    work.amount = amount.toDouble().toInt()
                    work.date = date
                    work.isThick = isThick!!
                    work.commentary = description
                    work.kbUsed = kwUsed.toDouble().toInt()

                    mActivity?.updateWork(work)

                } else {
                    //Crear un nuevo trabajo que tendrá otro ID:
                    val work2 = WorkEntity(
                        amount = amount.toDouble().toInt(),
                        date = date,
                        isThick = isThick!!,
                        commentary = description,
                        kbUsed = kwUsed.toDouble().toInt()
                    )
                    doAsync {
                        //Agregar el trabajo desde aquí y no desde addWork(work2) porque se le debe asignar un ID.
                        work2.id = database.getDao().insertWork(work2)
                        uiThread {
                            //Esta solo es parte gráfica:
                            mActivity?.addWork(work2)
                        }
                    }

                }

                mActivity?.onBackPressed() //Volver al activity
            } else {
                //Caso erroneo por falta de datos:
                soundWrong1?.start()
            }
        }catch (e : NumberFormatException){
            Toast.makeText(context, getString(R.string.syntax_error), Toast.LENGTH_SHORT).show()
            soundWrong1?.start()
        }
    }

    private fun showDialogPicker() {
         val dayPicker = MDataPickerFragment{ day, month, year -> onDateSelected(day, month, year)}
        dayPicker.show(parentFragmentManager, "daypicker")
    }

    private fun onDateSelected(day : Int, month : Int, year : Int){
          //Una vez seleccionado el campo crear un string:
          val date = "$day/${month+1}/$year"
          //Rellenar el edittext.
        mBinding.TIETDate.setText("Fecha seleccionada: $date")
    }




    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        //Comprobar que cuando le de atrás solo regresa a home.
        return when(item.itemId){
            android.R.id.home -> {
                //Volver al menú principal:
                mActivity?.onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }

    }

    override fun onDestroy() {

        //Mostrar nuevamente everything:
        mActivity?.showViews()
        mActivity?.supportActionBar?.title = getString(R.string.main_title)

        //Ubicar que ya no tiene opciones.
        mActivity?.supportActionBar?.setDisplayHomeAsUpEnabled(false)
        setHasOptionsMenu(false)

        super.onDestroy()
    }

}