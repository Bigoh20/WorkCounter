package com.bigoblog.workcounter

import android.content.DialogInterface
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.*
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bigoblog.workcounter.database.WorkAdapter
import com.bigoblog.workcounter.database.WorkEntity
import com.bigoblog.workcounter.database.WorkInit.Companion.database
import com.bigoblog.workcounter.database.WorkInit.Companion.spItem
import com.bigoblog.workcounter.databinding.ActivityMainBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.lang.NumberFormatException
import java.text.DecimalFormat


class MainActivity : AppCompatActivity(), OnClickListener {
    private lateinit var mBinding : ActivityMainBinding
    private lateinit var mLayoutManager : RecyclerView.LayoutManager
    lateinit var mAdapter : WorkAdapter
    private var showMenu = true
    private var menu : Menu? = null
    private var soundSuccess1 : MediaPlayer? = null
    private var soundSuccess2 : MediaPlayer? = null
    private var soundSuccess3 : MediaPlayer? = null
    private var soundWrong1 : MediaPlayer? = null
    //Crear la lista del work.

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)
        mBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        supportActionBar?.title = getString(R.string.title)

        setupAdapter()
        setupSounds()


        mBinding.fabAdd.setOnClickListener {
            //Abrir fragment para agregar un nuevo trabajo.
            val fragmentAdd = EditOrAddWorkFragment()
            openFragment(fragmentAdd)
            soundSuccess2?.start()
            modifyMenu(true) //Ocultar el menú
        }

    }

    override fun onResume() {
        super.onResume()
        setupSharedPreferencesData()
    }



    /*
    PUBLIC METHODS:
     */

    fun openFragment(fragment: Fragment, arguments : Bundle? = null) {

    //Pasar argumentos a fragment en caso de enviarle algo.
        if(arguments != null)  fragment.arguments = arguments

        //Ocultar el button y el rv.
        with(mBinding){
            fabAdd.visibility = View.GONE
            recyclerviewRecord.visibility = View.GONE
        }
        //Abrir el fragment:
        val fragmentManager = supportFragmentManager

        fragmentManager.beginTransaction().apply {
            replace(R.id.cl_main, fragment)
            addToBackStack(null)
            commit()
        }
    }

    //Mostrar las vistas de nuevo, pero con la opcion de seleccionar algunas en caso necesario.
    fun showViews(fab1 : Boolean = true, rv1 : Boolean = true, menu : Boolean = true) {
        if(fab1) mBinding.fabAdd.visibility = View.VISIBLE
        if(rv1) mBinding.recyclerviewRecord.visibility = View.VISIBLE
        if(menu) modifyMenu(false) //Mostrar el menu
    }

    fun addWork(work : WorkEntity){
        doAsync {
//            database.getDao().insertWork(work)
            uiThread {
                mAdapter.addWork(work)
            }
        }
        scrollRecyclerviewTo(toLastIndex = true)
        Toast.makeText(applicationContext, getString(R.string.created_successfully), Toast.LENGTH_SHORT).show()
        soundSuccess3?.start()

    }


    fun deleteWork(work : WorkEntity){
        doAsync {
            database.getDao().deleteWork(work)
            uiThread {
                mAdapter.deleteWork(work)
            }
        }
        Toast.makeText(applicationContext, getString(R.string.deleted_successfully), Toast.LENGTH_SHORT).show()
        soundSuccess3?.start()

    }

    fun updateWork(work : WorkEntity){
         doAsync {
             database.getDao().updateWork(work)
             uiThread {
                 mAdapter.updateWork(work)
             }
         }
        soundSuccess3?.start()
    }

    /*
     PRIVATE METHODS:
      */

    private fun setupSharedPreferencesData(){
        //Conseguir los textos de los precios almacenados en sharedPreferences
        val ecoPrice = spItem.getString(getString(R.string.key_price_eco))
        val superPrice = spItem.getString(getString(R.string.key_price_super))

        //Si están vacíos inicializarlos:
        if(ecoPrice.isEmpty()) spItem.putString(getString(R.string.key_price_eco), "2.56")
        if(superPrice.isEmpty()) spItem.putString(getString(R.string.key_price_super), "3.57")
    }
    private fun setupAdapter() {

        //Instancia del linearlayout & inicializar el adapter.
        mLayoutManager = LinearLayoutManager(this)
        mAdapter = WorkAdapter(mutableListOf(), this)

        //Asignarle los valores al recyclerview.
            with(mBinding.recyclerviewRecord){
            setHasFixedSize(true)
            layoutManager = mLayoutManager
            adapter = mAdapter
        }
        //Inicializar los datos.
        doAsync {
            val works = database.getDao().getAllWorks()
            uiThread {
                mAdapter.setWorks(works)
            }
        }
    }

    private fun setupSounds(){
         soundSuccess1 = MediaPlayer.create(this, R.raw.button_selected)
         soundSuccess2 = MediaPlayer.create(this, R.raw.sounds_button_press)
         soundSuccess3 = MediaPlayer.create(this, R.raw.button_success_3)
         soundWrong1 = MediaPlayer.create(this, R.raw.wrong_selection)
    }

       private fun scrollRecyclerviewTo(toLastIndex : Boolean){
           if(toLastIndex) {
               doAsync {
                   val lastIndex = database.getDao().getAllWorks().size - 1
                   uiThread {
                       mBinding.recyclerviewRecord.scrollToPosition(lastIndex)
                   }
               }
           }else{
               //Caso contrario, scrollear al inicio
               mBinding.recyclerviewRecord.scrollToPosition(0)
           }
    }

    private fun modifyMenu(hideMenu : Boolean){
        if(hideMenu){
            showMenu = false
            onPrepareOptionsMenu(menu)
        }else{
            showMenu = true
            onPrepareOptionsMenu(menu)
        }
    }

    private fun renderData(view: View?){
        //Este método renderiza la información en base a todos los elementos, es decir, estadisticas del total y las muestra en un dialog
        //Recuperar los textview's:
        val tvAmount = view?.findViewById<TextView>(R.id.tv_total_amount)
        val tvKw = view?.findViewById<TextView>(R.id.tv_total_gallons)
        val tvEco = view?.findViewById<TextView>(R.id.tv_total_eco)
        val tvSuper = view?.findViewById<TextView>(R.id.tv_total_super)

        //Escribir valores solo en 2 decimales:
        val df = DecimalFormat("#.00")
        //Recuperar los valores.
        doAsync {
            val allWorks = database.getDao().getAllWorks()
            uiThread {
                var totalPrice = 0.0
                var totalGallons = 0.0
                var totalEco = 0.0
                var totalSuper = 0.0


                for(currentWork in allWorks){

                    totalPrice += currentWork.price
                    totalGallons += currentWork.gallonsUsed

                    //Sumar el precio al eco o super, dependiendo de cual sea:
                    if(currentWork.isEco) totalEco += currentWork.price
                    else totalSuper += currentWork.price

                }


                //Asignarles los valores a los tv.
                tvAmount?.text = "Dinero gastado: $${df.format(totalPrice)}"
                tvKw?.text = "Galones rellenados: ${df.format(totalGallons)}"
                tvEco?.text = "Dinero en eco: $${df.format(totalEco)}"
                tvSuper?.text = "Dinero en super: $${df.format(totalSuper)}"
            }
        }
    }


    /*
    OVERRIDE METHODS:
     */
    override fun setOnClickListener(work : WorkEntity) {
        //Crear un Bundle por cada setOnClickListener, para que así todos los info fragment sean diferentes.
        val mBundle = Bundle()

        /*
        Configuración de argumentos para la vista de más información:
         */
        mBundle.putLong(getString(R.string.key_id), work.id)
        modifyMenu(true) //Ocultar el menú

        //Iniciar el Fragment y pasarle la tienda:
        val infoFragment = InfoFragment(work)
        openFragment(infoFragment, mBundle)
        soundSuccess1?.start()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.top_menu, menu)
        this.menu = menu
        return true
    }


    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        if(showMenu){
            invalidateOptionsMenu()
            menu?.findItem(R.id.downItem)?.isVisible = true
            menu?.findItem(R.id.upItem)?.isVisible = true
            menu?.findItem(R.id.infoItem)?.isVisible = true
            menu?.findItem(R.id.settingsItem)?.isVisible = true

        }else{
            invalidateOptionsMenu()
            menu?.findItem(R.id.downItem)?.isVisible = false
            menu?.findItem(R.id.upItem)?.isVisible = false
            menu?.findItem(R.id.infoItem)?.isVisible = false
            menu?.findItem(R.id.settingsItem)?.isVisible = false

        }
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

            return when (item.itemId) {
                R.id.downItem -> {
                    scrollRecyclerviewTo(toLastIndex = true)
                    soundSuccess1?.start()
                    true
                }
                R.id.upItem -> {
                    scrollRecyclerviewTo(toLastIndex = false)
                    soundSuccess1?.start()
                    true
                }
                R.id.infoItem -> {

                    //Crear la vista del dialog.
                    val view = layoutInflater.inflate(R.layout.info_dialog, null)

                    //Crear el dialog para la vista estadística:
                         MaterialAlertDialogBuilder(this)
                        .setTitle(getString(R.string.full_info))
                        .setView(view)
                        .setPositiveButton("Aceptar") { _, _ ->
                            //Cerrar el dialog automáticamente.
                        }
                        .setCancelable(true)
                        .show()

                    //Renderizar los datos.
                    renderData(view)
                    soundSuccess2?.start()

                    true
                }
                R.id.settingsItem -> {

                    val view = layoutInflater.inflate(R.layout.config_dialog, null)

                    val dialog = MaterialAlertDialogBuilder(this)
                        .setTitle(getString(R.string.title_config))
                        .setView(view)
                        .setPositiveButton("Aceptar") {_, _ ->}
                        .setNegativeButton("Cancelar") {_, _ ->} //Cerrar el dialog.
                        .setCancelable(false)
                        .show()

                    //Recuperar los editText
                    val tietEco = view.findViewById<TextInputEditText>(R.id.tiet_eco_price)
                    val tietSuper = view.findViewById<TextInputEditText>(R.id.tiet_super_price)



                    //Recuperar los textos de sharedPreferences
                    val priceEco = spItem.getString(getString(R.string.key_price_eco))
                    val priceSuper = spItem.getString(getString(R.string.key_price_super))

                    tietEco.setText(priceEco)
                    tietSuper.setText(priceSuper)

                    soundSuccess2?.start()

                    dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener {

                        try {
                            //Conseguir los textos:
                            val ecoGas = tietEco.text.toString()
                            val superGas = tietSuper.text.toString()


                            //Comprobar que los campos no estén vacíos:
                            if (ecoGas.isEmpty() || superGas.isEmpty()) {

                                Toast.makeText(
                                    this, getString(R.string.error_empty_text),
                                    Toast.LENGTH_SHORT
                                ).show()
                                soundWrong1?.start()

                            } else if (ecoGas.toDouble() <= 0.0 || superGas.toDouble() <= 0.0) {

                                Toast.makeText(
                                    this, getString(R.string.error_zero_text),
                                    Toast.LENGTH_SHORT
                                ).show()
                                soundWrong1?.start()

                            } else {
                                spItem.putString(getString(R.string.key_price_eco), ecoGas)
                                spItem.putString(getString(R.string.key_price_super), superGas)

                                Toast.makeText(
                                    applicationContext,
                                    getString(R.string.success_update), Toast.LENGTH_SHORT
                                )
                                    .show()

                                soundSuccess1?.start()
                                dialog.dismiss()
                            }
                        }catch (e : NumberFormatException){

                            Toast.makeText(this, getString(R.string.syntax_error),
                                Toast.LENGTH_SHORT).show()
                            soundWrong1?.start()

                        }
                    }


                    true
                }
                else -> super.onOptionsItemSelected(item)
            }
    }



}