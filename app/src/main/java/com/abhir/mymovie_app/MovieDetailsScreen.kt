package com.abhir.mymovie_app

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase


@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovieDetailScreen(navController: NavController, movie: Movie) {


    val database = FirebaseDatabase.getInstance().reference
    var isFavorite by remember { mutableStateOf(false) }
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    // Check if the movie is a favorite
    LaunchedEffect(movie?.id) {
        if (movie != null) {
            database.child("favorites").child(userId).child(movie.id.toString())
                .get().addOnSuccessListener {
                    isFavorite = it.exists()
                }
        }
    }

    // Movie details UI
//    movie?.let { currentMovie ->
        Scaffold(
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Movie Image
                Image(
                    painter = rememberAsyncImagePainter(model = "https://image.tmdb.org/t/p/w500${movie.poster_path}"),
                    contentDescription = movie.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Movie Title
                Text(
                    text = movie.title.toString(),
                    style = MaterialTheme.typography.headlineMedium
                )

                // Movie Rating
                Text(
                    text = "Rating: ${movie.vote_average}",
                    style = MaterialTheme.typography.bodyLarge
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Movie Description
                Text(
                    text = movie.overview.toString(),
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Favorite Toggle
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isFavorite) "Added to Favorites" else "Mark as Favorite",
                        style = MaterialTheme.typography.bodyLarge
                    )

                    Switch(
                        checked = isFavorite,
                        onCheckedChange = { checked ->
                            isFavorite = checked
                            if (checked) {
                                database.child("favorites").child(userId)
                                    .child(movie.id.toString()).setValue(movie)
                            } else {
                                database.child("favorites").child(userId)
                                    .child(movie.id.toString()).removeValue()
                            }
                        }
                    )
                }
            }
        }
//    }
}
