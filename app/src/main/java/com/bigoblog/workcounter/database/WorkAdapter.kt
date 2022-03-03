package com.bigoblog.workcounter.database

import android.content.Context
import android.content.res.Resources
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bigoblog.workcounter.OnClickListener
import com.bigoblog.workcounter.R
import com.bigoblog.workcounter.databinding.ItemHistoryBinding

class WorkAdapter(private var workList: MutableList<WorkEntity>, val listener : OnClickListener) :
    RecyclerView.Adapter<WorkAdapter.WorkHolder>() {


    lateinit var context : Context
    private val itemSystem = Resources.getSystem()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WorkHolder {
        context = parent.context
        val view = LayoutInflater.from(context).inflate(R.layout.item_history, parent, false)
        return WorkHolder(view)
    }

    override fun onBindViewHolder(holder: WorkHolder, position: Int) {
        val currentWork = workList[position]
        holder.render(currentWork)
    }

    override fun getItemCount(): Int = workList.size


    fun addWork(work: WorkEntity) {
      //Agregar al array y al recyclerview.
        workList.add(work)
        notifyItemInserted(workList.size-1)
    }


    fun setWorks(works : MutableList<WorkEntity>) {
        workList = works
        notifyDataSetChanged()
    }

    fun deleteWork(work: WorkEntity) {
        //Sacar la position:
       val position = workList.indexOf(work)
        //Eliminarlo del array y notificar al recyclerview.
          workList.remove(work)
          notifyItemRemoved(position)
    }

    fun updateWork(work: WorkEntity) {
      val index = workList.indexOf(work)
        //Actualizar la tienda:
        if(index != -1){
            workList[index] = work
            notifyItemChanged(index)
            Toast.makeText(context, "Trabajo actualizado con éxito", Toast.LENGTH_SHORT).show()
        }else{
            Toast.makeText(context, "El trabajo no se pudo actualizar", Toast.LENGTH_SHORT).show()
        }

    }


    inner class WorkHolder(view : View) : RecyclerView.ViewHolder(view){
        private val mBinding = ItemHistoryBinding.bind(view)


        fun render(workItem: WorkEntity) {
            //Crear el context para acceder a los métodos normales.
            val mContext = itemView.context

            //Sacar los resultados:
            val amount = workItem.amount
            val date = workItem.date


            mBinding.tvAmount.text = "Cantidad: $amount"
            mBinding.tvDate.text = date

            //Pintar del color dependiendo de la cantidad.
            when(amount) {
                in 0 .. 10 ->  mBinding.cvHistory.setCardBackgroundColor(mContext.resources.getColor(R.color.red1))
                in 11 .. 20 -> mBinding.cvHistory.setCardBackgroundColor(mContext.resources.getColor(R.color.yellow1))
                in 21 .. 30 -> mBinding.cvHistory.setCardBackgroundColor(mContext.resources.getColor(R.color.green1))
                in 31 .. 1000 -> mBinding.cvHistory.setCardBackgroundColor(mContext.resources.getColor(R.color.green2))
            }

            //Comprobar el tipo:
            if(workItem.isThick){
                mBinding.tvType.text = mContext.getString(R.string.type_grueso)
            }else{
                mBinding.tvType.text = mContext.getString(R.string.type_fine)
            }
            //Ubicar su listener:
            mBinding.cvHistory.setOnClickListener {  listener.setOnClickListener(workItem)  }

        }

    }

}