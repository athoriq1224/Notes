package com.example.notes.ui.add

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.notes.R
import com.example.notes.data.model.Priority
import com.example.notes.data.model.ToDoData
import com.example.notes.fragment.SharedViewModel
import com.example.notes.viewmodel.ToDoViewModel
import kotlinx.android.synthetic.main.fragment_add.*
import kotlinx.android.synthetic.main.fragment_add.view.*


class AddFragment : Fragment() {
    private val mToDoViewModel: ToDoViewModel by viewModels()
    private val mSharedViewModel: SharedViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_add, container, false)
        view.spinner_priorities.onItemSelectedListener = mSharedViewModel.listener

        return view
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.add_fragment_menu,menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.menu_add){
            insertDataToDb()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun insertDataToDb() {
        val mTitle =edt_title.text.toString()
        val mPriority = spinner_priorities.selectedItem.toString()
        val mDescription = edt_description.text.toString()

        val validation =mSharedViewModel.verifyDataFromUser(mTitle,mDescription)
        if(validation){
            //insert data to database

            val newData = ToDoData(
                    0,
                    mTitle,
                    mSharedViewModel.parsePriority(mPriority),
                    mDescription
            )
            mToDoViewModel.insertData(newData)
            Toast.makeText(requireContext(), "Successfully added!", Toast.LENGTH_SHORT).show()
            //navigation back
            findNavController().navigate(R.id.action_addFragment_to_listFragment)
        }else{
            Toast.makeText(requireContext(), "Please fill out the fields", Toast.LENGTH_SHORT)
                    .show()
        }
    }
//     fun verifyDataFromUser(title: String, description: String): Boolean {
//        return !(title.isEmpty() || description.isEmpty())
//    }
//
//
//     fun parsePriority(priority: String): Priority {
//        return when(priority){
//            "High Priority" -> {
//                Priority.HIGH
//            }
//            "Medium Priority" -> {
//                Priority.MEDIUM
//            }
//            "Low Priority" -> {
//                Priority.LOW
//            }
//            else -> Priority.LOW
//        }
//
//    }

}