package com.bigoblog.workcounter

import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.bigoblog.workcounter.database.WorkEntity
import com.bigoblog.workcounter.database.WorkInit
import com.bigoblog.workcounter.database.WorkInit.Companion.spItem
import com.bigoblog.workcounter.databinding.FragmentInfoBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.lang.Exception


class InfoFragment(private val workItem : WorkEntity) : Fragment() {
    private lateinit var mBinding : FragmentInfoBinding
    private var mActivity : MainActivity? = null

    //Variable para saber si este fragment tiene intenciones de editar el trabajo.
    private var goToEditIntent = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mBinding = FragmentInfoBinding.inflate(inflater, container, false)
        // Inflate the layout for this fragment
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        renderFragment()

        mBinding.buttonEdit.setOnClickListener {

            //Si se presiono para editar, entonces poner goToEditIntent en true (mirar onDestroy)


            //Conseguir los datos:
            val bundle = arguments
            val id = bundle?.getLong(getString(R.string.key_id))


            if(id == 0L){
                Toast.makeText(context, getString(R.string.error_edit), Toast.LENGTH_SHORT).show()
            }else{
                goToEditIntent = true
                val fragmentEdit = EditOrAddWorkFragment(id)
                //PRIMERO CERRAR EL FRAGMENT.
                mActivity?.onBackPressed()
                //UNA VEZ ESTE FRAGMENT CERRADO, EL CONTEXT ESTÁ EN EL ACTIVITY PRINCIPAL, DE AHÍ ABRIR EL FRAGMENT EDIT.
                mActivity?.openFragment(fragment = fragmentEdit)
            }
        }

        mBinding.buttonDelete.setOnClickListener {
            deleteWork()
        }

    }



    private fun renderFragment() {
        try {
            //Crear la activity
            mActivity = activity as MainActivity?
            mActivity?.supportActionBar?.title = getString(R.string.title_info)
            //Crear la opcion de back.
            mActivity?.supportActionBar?.setDisplayHomeAsUpEnabled(true)
            setHasOptionsMenu(true)


            //Renderizarlos en la pantalla:
            mBinding.tvAmount.text = "${getString(R.string.hint_amount)}: $${workItem.price}"
            mBinding.tvDate.text = workItem.date

            if (workItem.isGasOne) mBinding.tvType.text = getString(R.string.type_gas_text) +
                    spItem.getString(getString(R.string.key_gas_one))

            else mBinding.tvType.text = getString(R.string.type_gas_text) +
                    spItem.getString(getString(R.string.key_gas_two))

            mBinding.tvDescription.text = "${getString(R.string.description)}: ${workItem.commentary}"
            mBinding.tvKw.text = "${getString(R.string.gallons_used)}: ${workItem.gallonsUsed}"

        }catch (e : Exception){
            Toast.makeText(context, getString(R.string.error_render), Toast.LENGTH_SHORT).show()
            mActivity?.onBackPressed()
        }

    }

    //Opciones de cuando presione en back.
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
            android.R.id.home -> {
                mActivity?.onBackPressed()
                true
            }
            else -> onOptionsItemSelected(item)
        }
    }

    private fun deleteWork() {
       //Crear un dialog: Usar el contexto solo si no es null.
        val dialog = context?.let {
            MaterialAlertDialogBuilder(it)
                .setTitle(getString(R.string.title_delete_work))
                .setCancelable(true)
                .setPositiveButton("OK") { _, _ -> }
                .setNegativeButton(getString(R.string.cancel_text)) {_, _ ->}
                .show()

        }

        dialog?.getButton(DialogInterface.BUTTON_POSITIVE)?.setOnClickListener {
             mActivity?.deleteWork(workItem)
             dialog.dismiss()
             mActivity?.onBackPressed()
        }
    }



    //Esta función limpia los titulos, y prepara tod.o para que al volver al activity tod.o siga normal.
    private fun cleanFragment() {
        /*
        Si el modo de edición está activado,
        entonces no mostrar las vistas nuevamente del activity para ahorrar recursos
         */
        if(!goToEditIntent){
            mActivity?.showViews()
            mActivity?.supportActionBar?.title = getString(R.string.title)
        }
        //Quitar el menu bar.
        mActivity?.supportActionBar?.setDisplayHomeAsUpEnabled(false)
        setHasOptionsMenu(false)
    }

    override fun onDestroy() {
        cleanFragment()
        super.onDestroy()
    }



}


