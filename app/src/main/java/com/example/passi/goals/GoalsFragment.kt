package com.example.passi.goals

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.passi.R
import com.example.passi.entities.GoalRow
import java.util.*

class GoalsFragment : Fragment() {

    companion object {
        fun newInstance() = GoalsFragment()
    }

    private lateinit var viewModel: GoalsViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_goals, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(GoalsViewModel::class.java)

        val recyclerView: RecyclerView = requireView().findViewById(R.id.lista_giorni)

        val g1 = GoalRow(Date(), 100, 1000, 250, 1245)
        val g2 = GoalRow(Date(), 2000, 1000, 450, 52000)
        val g3 = GoalRow(Date(), 3500, 1500, 278, 210)
        val g4 = GoalRow(Date(), 500, 1000, 26, 52)

        val goalsList: Array<GoalRow> = arrayOf(g1,g2,g3,g4)
        recyclerView.adapter = GoalsAdapter(goalsList)
    }

}