package com.example.passi.goals

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.example.passi.R
import com.example.passi.SharedViewModel
import com.example.passi.core.data.AppDatabase
import com.example.passi.core.data.StepRepository
import kotlinx.coroutines.launch

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val repository  = StepRepository(AppDatabase.getInstance(requireContext()).stepDao())


        viewLifecycleOwner.lifecycleScope.launch {
            val recyclerView: RecyclerView = requireView().findViewById(R.id.lista_giorni)
            val goalsList = repository.getGoalRows()
            goalsList.reverse()
            recyclerView.adapter = GoalsAdapter(goalsList)
        }
    }

}