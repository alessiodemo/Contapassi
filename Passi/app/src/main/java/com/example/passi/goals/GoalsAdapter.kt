package com.example.passi.goals

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.passi.R
import com.example.passi.entities.GoalRow


class GoalsAdapter(private val goalsList: Array<GoalRow>) :
    RecyclerView.Adapter<GoalsAdapter.GoalsViewHolder>() {

    class GoalsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val goalsGiorno: TextView = itemView.findViewById(R.id.giornoOb)
        private val goalsMese: TextView = itemView.findViewById(R.id.meseOb)
        private val goalsPassi: TextView = itemView.findViewById(R.id.passiOb)
        private val goalsObiettivo: TextView = itemView.findViewById(R.id.obiettivoOb)
        private val goalsCheck: ImageView = itemView.findViewById(R.id.checkOb)
        private val goalsCalorie: TextView = itemView.findViewById(R.id.kcalOb)
        private val goalsDistanza: TextView = itemView.findViewById(R.id.kmOb)

        fun bind(row: GoalRow) {
            goalsGiorno.text = row.giorno.toString()
            goalsMese.text = row.mese
            goalsPassi.text = row.passi.toString()
            goalsObiettivo.text = "su ${row.obiettivo}"
            if(row.done){
                goalsCheck.setImageResource(R.drawable.check_circle)
            } else {
                goalsCheck.setImageResource(R.drawable.cancel_circle)
            }
            goalsCalorie.text = row.kcal.toString()
            goalsDistanza.text = row.distanza.toString()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GoalsViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.daily_row_stats, parent, false)

        return GoalsViewHolder(view)
    }

    // Returns size of data list
    override fun getItemCount(): Int {
        return goalsList.size
    }

    // Displays data at a certain position
    override fun onBindViewHolder(holder: GoalsViewHolder, position: Int) {
        holder.bind(goalsList[position])
    }
}
