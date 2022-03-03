package com.bigoblog.workcounter

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
import com.bigoblog.workcounter.databinding.ActivityMainBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread



class MainActivity : AppCompatActivity(), OnClickListener {
    private lateinit var mBinding : ActivityMainBinding
    private lateinit var mLayoutManager : RecyclerView.LayoutManager
    lateinit var mAdapter : WorkAdapter
    private var showMenu = true
    private var menu : Menu? = null
    private var soundSuccess1 : MediaPlayer? = null
    private var soundSuccess2 : MediaPlayer? = null
    private var soundSuccess3 : MediaPlayer? = null
    //Crear la lista del work.

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)
        mBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        supportActionBar?.title = getString(R.string.main_title)

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
        //Este método renderiza la información en base a todos los elementos, es decir, estadisticas del total.
        //Recuperar los textview's:
        val tvAmount = view?.findViewById<TextView>(R.id.tv_total_amount)
        val tvKw = view?.findViewById<TextView>(R.id.tv_total_kw)
        val tvFine = view?.findViewById<TextView>(R.id.tv_total_fine)
        val tvThick = view?.findViewById<TextView>(R.id.tv_total_thick)

        //Recuperar los valores.
        doAsync {
            val allWorks = database.getDao().getAllWorks()
            uiThread {
                var totalAmount = 0
                var totalKw = 0
                var totalFine = 0
                var totalThick = 0

                for(currentWork in allWorks){

                    totalAmount += currentWork.amount
                    totalKw += currentWork.kbUsed

                    if(currentWork.isThick) totalThick += currentWork.amount
                    else totalFine += currentWork.amount

                }

                //Asignarles los valores a los tv.
                tvAmount?.text = "Sacos molidos: $totalAmount"
                tvKw?.text = "Kw gastados: $totalKw"
                tvFine?.text = "Finos molidos: $totalFine"
                tvThick?.text = "Gruesos molidos: $totalThick"
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

        }else{
            invalidateOptionsMenu()
            menu?.findItem(R.id.downItem)?.isVisible = false
            menu?.findItem(R.id.upItem)?.isVisible = false
            menu?.findItem(R.id.infoItem)?.isVisible = false
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
                else -> super.onOptionsItemSelected(item)
            }
    }



}