package com.example.respmonitor.gui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.respmonitor.database.JournalDao
import com.example.respmonitor.database.JournalEntry
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class JournalViewModel(private val dao: JournalDao) : ViewModel() {
    val entries = dao.getAllEntries()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addEntry(entry: JournalEntry) {
        viewModelScope.launch {
            dao.insertEntry(entry)
        }
    }

    fun deleteEntry(entry: JournalEntry) {
        viewModelScope.launch {
            dao.deleteEntry(entry)
        }
    }
}