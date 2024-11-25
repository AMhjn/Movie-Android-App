package com.abhir.mymovie_app

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch


class FavoritesViewModel : ViewModel() {
    private val _favorites = MutableStateFlow<List<Movie>>(emptyList()) // Use StateFlow
    val favorites: StateFlow<List<Movie>> = _favorites
    private val database = FirebaseDatabase.getInstance().reference
    private val auth = FirebaseAuth.getInstance()

    init {
        fetchFavorites()
    }

    fun removeFavorite(movie: Movie) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val movieRef = database.child("favorites").child(userId).child(movie.id.toString())

        // Update the local list immediately
        _favorites.value = _favorites.value?.filter { it.id != movie.id }!!

        // Remove the movie from Firebase
        movieRef.removeValue().addOnFailureListener { exception ->
            // Handle failure: revert local state if needed
            Log.e("FavoritesViewModel", "Failed to remove favorite: ${exception.message}")
        }
    }

    fun fetchFavorites() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        database.child("favorites").child(userId)
            .get()
            .addOnSuccessListener { snapshot ->
                val favoriteMovies = snapshot.children.mapNotNull { it.getValue(Movie::class.java) }
                _favorites.value = favoriteMovies // Update StateFlow
            }
            .addOnFailureListener { exception ->
                Log.e("FavoritesViewModel", "Failed to fetch favorites: ${exception.message}")
            }
    }
}

