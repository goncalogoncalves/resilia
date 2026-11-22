package com.goncalogoncalves.resilia.feature.places

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.goncalogoncalves.resilia.core.domain.model.ContentBundle
import com.goncalogoncalves.resilia.core.domain.model.Place
import com.goncalogoncalves.resilia.core.domain.model.PlaceType
import com.goncalogoncalves.resilia.core.domain.repository.ContentRepository
import com.goncalogoncalves.resilia.core.domain.repository.PlacesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlacesViewModel @Inject constructor(
    content: ContentRepository,
    private val repository: PlacesRepository,
) : ViewModel() {

    /** null until loaded, so the edit screen does not flash an empty form. */
    val places: StateFlow<List<Place>?> = repository.places
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val bundle: StateFlow<ContentBundle?> = flow { emit(content.load()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun save(place: Place, done: () -> Unit) {
        viewModelScope.launch {
            repository.save(place)
            done()
        }
    }

    fun delete(id: Long, done: () -> Unit) {
        viewModelScope.launch {
            repository.delete(id)
            done()
        }
    }
}

/** Distances are measured from the first place of type "home" that has coordinates (decision 5). */
internal fun List<Place>.home(): Place? = firstOrNull { it.type == PlaceType.HOME && it.position != null }
