package com.abhir.mymovie_app

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.gson.Gson
import kotlinx.coroutines.launch

val auth = FirebaseAuth.getInstance()
val userId = auth.currentUser?.uid ?: "unknownUser"

@Composable
fun MovieScreen(filter: String, navController: NavController) {
    val movies = remember { mutableStateListOf<Movie>() }
    val scope = rememberCoroutineScope()
    val API_KEY = "7a48d4ea8692a21a00fe40f0c75605c7"

    var favViewModel= FavoritesViewModel();

    // Fetch movies
    LaunchedEffect(Unit) {
        scope.launch {
            try {
                var response:MovieResponse;

                if(filter == "popular"){
                    response = TMDBApiClient.api.getPopularMovies(apiKey = API_KEY)
                }
                else if(filter =="now_playing"){
                    response = TMDBApiClient.api.getNowPlayingMovies(apiKey = API_KEY)
                }
                else{
                    response = TMDBApiClient.api.getTopRatedMovies(apiKey = API_KEY)
                }

                movies.addAll(response.results)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Static Text at the top
        Text(
            text = "Showing : $filter Movies",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp) // Add space below the Text
        )

        // LazyColumn for displaying movies
        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f) // Ensure LazyColumn takes the remaining space below Text
        ) {
            LazyColumn(
                contentPadding = PaddingValues(0.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(movies) { movie ->
                    MovieItem(movie, userId, favViewModel, navController)
                }
            }
        }
    }


}

@Composable
fun MovieItem(movie: Movie, userId: String, favoritesViewModel: FavoritesViewModel, navController: NavController) {
    val database = FirebaseDatabase.getInstance().reference
    var isFavorite by remember { mutableStateOf(false) }

    // Check if the movie is already in the favorites
    LaunchedEffect(movie.id) {
        database.child("favorites").child(userId).child(movie.id.toString())
            .get().addOnSuccessListener {
                isFavorite = it.exists()
            }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                val movieJson = Uri.encode(Gson().toJson(movie))
                navController.navigate("movieDetail/$movieJson")  }
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Display movie poster
        Image(
            painter = rememberAsyncImagePainter(model = "https://image.tmdb.org/t/p/w500${movie.poster_path}"),
            contentDescription = movie.title,
            modifier = Modifier.size(80.dp)
        )

        // Display movie title and overview
        Column(modifier = Modifier.weight(1f)) {
            Text(text = movie.title.toString(), style = MaterialTheme.typography.titleMedium)
            Text(
                text = movie.overview.toString(),
                style = MaterialTheme.typography.bodySmall,
                maxLines = 3
            )
        }

        // Favorite Toggle Switch
        Switch(
            checked = isFavorite,
            onCheckedChange = { checked ->
                isFavorite = checked
                if (checked) {
                    // Add to favorites: Save the full movie object
                    database.child("favorites")
                        .child(userId)
                        .child(movie.id.toString())
                        .setValue(movie) // Save the complete Movie object
                } else {
                    // Remove from favorites
                    database.child("favorites")
                        .child(userId)
                        .child(movie.id.toString())
                        .removeValue();
                    favoritesViewModel.removeFavorite(movie)

                }
            }
        )

    }
}



@Composable
fun FavoriteScreen(navController: NavController, favoritesViewModel: FavoritesViewModel = viewModel()) {
    val favoriteMovies by favoritesViewModel.favorites.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Static Text at the top
        Text(
            text = "Showing : Favourite Movies",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp) // Add space below the Text
        )

        // LazyColumn for displaying movies
        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f) // Ensure LazyColumn takes the remaining space below Text
        ) {
            LazyColumn(
                contentPadding = PaddingValues(0.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(favoriteMovies) { movie ->
                    MovieItem(movie, userId, favoritesViewModel, navController)
                }
            }
        }
    }
}
