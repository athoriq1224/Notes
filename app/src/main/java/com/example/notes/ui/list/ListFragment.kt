package com.example.notes.ui.list

import android.os.Bundle
import android.view.*
import android.widget.SearchView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.room.Query
import com.example.notes.R
import com.example.notes.adapter.ListAdapter
import com.example.notes.data.model.ToDoData
import com.example.notes.databinding.FragmentListBinding
import com.example.notes.fragment.SharedViewModel
import com.example.notes.viewmodel.ToDoViewModel
import com.google.android.material.snackbar.Snackbar
import jp.wasabeef.recyclerview.animators.LandingAnimator
import kotlinx.android.synthetic.main.fragment_list.*
import kotlinx.android.synthetic.main.fragment_list.view.*

class ListFragment : Fragment(),SearchView.OnQueryTextListener {
    private val mSharedViewModel: SharedViewModel by viewModels()
    private val mToDoViewModel: ToDoViewModel by viewModels()
    private val adapter: ListAdapter by lazy { ListAdapter() }

    private var _binding: FragmentListBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //databinding
        _binding = FragmentListBinding.inflate(inflater,container,false)
        binding.lifecycleOwner = this
        binding.mSharedViewModel = mSharedViewModel

        setupRecyclerView()
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_list, container, false)

        val rvTodo = view.rv_todo
        rvTodo.apply {
            layoutManager = StaggeredGridLayoutManager(2,GridLayoutManager.VERTICAL)
            adapter = adapter
        }
        mToDoViewModel.getAllData.observe(viewLifecycleOwner, Observer { data ->
            mSharedViewModel.checkIfDatabaseEmpty(data)
            adapter.setData(data)
        })

        setHasOptionsMenu(true)
        return view
    }


//    private fun showEmptyDateBaseViews(emptyDataBase: Boolean?) {
//        if (emptyDataBase!!){
//            img_No_Data.visibility = View.VISIBLE
//            tv_No_Data.visibility =View.VISIBLE
//        }else{
//            img_No_Data.visibility = View.INVISIBLE
//            tv_No_Data.visibility = View.INVISIBLE
//
//        }
//
//    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.list_fragment_menu,menu)

        val search = menu.findItem(R.id.menu_search)
        val searchView = search.actionView as? SearchView
        searchView?.isSubmitButtonEnabled = true
        searchView?.setOnQueryTextListener(this)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.menu_delete_all -> confirmDeleteAllData()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun confirmDeleteAllData() {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Delete Everything?")
                .setMessage("Are you sure want to remove everything?")
                .setPositiveButton("Yes"){_,_->
                    mToDoViewModel.deleteAllData()
                    Toast.makeText(
                            requireContext(),
                            "Successfully Removed Everything",
                            Toast.LENGTH_SHORT
                    ).show()
                }
                .setNegativeButton("No", null)
                .create()
                .show()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private fun swipeToDelete(recyclerView: RecyclerView) {
        val swipeToDeleteCallback = object : SwipeToDelete() {

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val deletedItem = adapter.dataList[viewHolder.adapterPosition]
                // Delete Item
                mToDoViewModel.deleteItem(deletedItem)
                adapter.notifyItemRemoved(viewHolder.adapterPosition)

                // Restore Deleted Item
                restoreDeletedData(viewHolder.itemView, deletedItem)
            }
        }

        val itemTouchHelper = ItemTouchHelper(swipeToDeleteCallback)
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    private fun setupRecyclerView(){
        val rvTodo = binding.rvTodo
        rvTodo.adapter = adapter

        rvTodo.layoutManager =
                StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        rvTodo.itemAnimator = LandingAnimator().apply { addDuration = 300
        }
        // Swipe to Delete
        swipeToDelete(rvTodo)
    }

    private fun restoreDeletedData(view: View, deletedItem: ToDoData) {
        val snackBar = Snackbar.make(
                view, "Deleted: '${deletedItem.title}'",
                Snackbar.LENGTH_LONG
        )
        snackBar.setAction("Undo") {
            mToDoViewModel.insertData(deletedItem)
        }
        snackBar.show()
    }
    private fun restoreDeleteData(view: View,deletedItem: ToDoData){
        val snackBar = Snackbar.make(view,"Delete: '${deletedItem.title}'",Snackbar.LENGTH_LONG)
        snackBar.setAction("Undo"){
            mToDoViewModel.insertData(deletedItem)
        }
        snackBar.show()
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        if (query != null) {
            searchThroughDatabase(query)
        }
        return true
    }

    private fun searchThroughDatabase(query: String) {
        val searchQuery = "%$query"

        mToDoViewModel.searchDatabase(searchQuery).observe(this, Observer { list->
            list?.let {
                adapter.setData(it)
            }
        })
    }

    override fun onQueryTextChange(query: String?): Boolean {
        if (query != null){
            searchThroughDatabase(query)
        }
        return true
    }

}