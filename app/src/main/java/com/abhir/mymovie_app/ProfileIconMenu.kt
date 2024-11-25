//package com.abhir.mymovie_app
//
//import android.widget.Toast
//import androidx.compose.foundation.Image
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.*
//import androidx.compose.material3.*
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.remember
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.res.painterResource
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.tooling.preview.Preview
//import androidx.compose.ui.unit.dp
//import androidx.navigation.NavController
//import coil.compose.rememberImagePainter
//import com.google.firebase.auth.FirebaseAuth
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun MainScreen(navController: NavController, auth: FirebaseAuth) {
//    val user = auth.currentUser
//    val username = user?.displayName ?: "User" // Fallback if the username is not set
//    val profileImageUri = user?.photoUrl?.toString() // Get profile image URL if available
//
//    // Profile Icon and Menu
//    Scaffold(
//        topBar = {
//            TopAppBar(
//                title = {
//                    Text(text = "Movies App")
//                },
//                actions = {
//                    // Profile Icon
//                    IconButton(onClick = { /* Show the Menu */ }) {
//                        // Show the profile image or icon
//                        if (profileImageUri != null) {
//                            // If the user has a profile image
//                            Image(
//                                painter = rememberImagePainter(profileImageUri),
//                                contentDescription = "Profile Image",
//                                modifier = Modifier.size(40.dp)
//                            )
//                        } else {
//                            // Default profile icon if no profile image
//                            Icon(
//                                painter = painterResource(id = R.drawable.defaultimage),
//                                contentDescription = "Profile",
//                                modifier = Modifier.size(40.dp)
//                            )
//                        }
//                    }
//                }
//            )
//        },
//        content = { paddingValues ->
//            Column(
//                modifier = Modifier
//                    .padding(paddingValues)
//                    .fillMaxSize(),
//                horizontalAlignment = Alignment.CenterHorizontally,
//                verticalArrangement = Arrangement.Center
//            ) {
//                // Main screen content goes here (e.g., movie list)
//
//                // Example button to navigate to the User Profile Screen
//                Button(onClick = { navController.navigate("edit_profile") }) {
//                    Text("Go to User Profile")
//                }
//            }
//        }
//    )
//
//    // Display the Menu when profile icon is clicked
//    DropdownMenu(
//        expanded =  /* state to control dropdown visibility */,
//        onDismissRequest = { /* close the dropdown */ }
//    ) {
//        DropdownMenuItem(
//            onClick = {
//                // Navigate to the profile screen
//                navController.navigate("edit_profile")
//            }
//        ) {
//            Row(
//                modifier = Modifier.padding(8.dp),
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                // Display the user's profile image
//                if (profileImageUri != null) {
//                    Image(
//                        painter = rememberImagePainter(profileImageUri),
//                        contentDescription = "Profile Image",
//                        modifier = Modifier.size(30.dp)
//                    )
//                } else {
//                    Icon(
//                        painter = painterResource(id = R.drawable.ic_default_profile),
//                        contentDescription = "Profile",
//                        modifier = Modifier.size(30.dp)
//                    )
//                }
//                Spacer(modifier = Modifier.width(8.dp))
//                Text(text = username, fontWeight = FontWeight.Bold)
//            }
//        }
//
//        // Sign Out Button
//        DropdownMenuItem(
//            onClick = {
//                // Sign out the user
//                auth.signOut()
//                // Navigate to the login screen
//                navController.navigate("login") {
//                    popUpTo("login") { inclusive = true } // Clear the back stack
//                }
//            }
//        ) {
//            Text("Sign Out")
//        }
//    }
//}
