package com.example.passi.goals

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import com.example.passi.core.data.Database
import com.example.passi.R
import com.example.passi.SharedViewModel

class GoalsFragment : Fragment() {

    companion object {
        fun newInstance() = GoalsFragment()
    }

    val model: SharedViewModel by viewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_goals, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val recyclerView: RecyclerView = requireView().findViewById(R.id.lista_giorni)
        val db = Database(requireContext())
        val goalsList = db.getGoalRows()
        goalsList.reverse()
        recyclerView.adapter = GoalsAdapter(goalsList)
    }

}