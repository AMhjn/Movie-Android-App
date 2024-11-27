import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import coil.compose.rememberAsyncImagePainter
import com.abhir.mymovie_app.EditProfileScreen
import com.abhir.mymovie_app.FavoriteScreen
import com.abhir.mymovie_app.MovieScreen
import com.abhir.mymovie_app.LoginScreen
import com.abhir.mymovie_app.Movie
import com.abhir.mymovie_app.MovieDetailScreen
import com.abhir.mymovie_app.RegistrationScreen
import com.abhir.mymovie_app.auth
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun MyMovieApp() {
    val navController = rememberNavController() // Set up NavController
    MainScreenWithDrawer(navController = navController)
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreenWithDrawer(navController: NavHostController) {
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val auth = FirebaseAuth.getInstance()
    val userId = auth.currentUser?.uid;



    // Observe the current destination from NavController
    val currentDestination by navController.currentBackStackEntryFlow
        .collectAsState(initial = navController.currentBackStackEntry)



    // Check if the drawer should be visible
    val isDrawerVisible = currentDestination?.destination?.route !in listOf("login", "registration")

    if (isDrawerVisible) {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                Surface(
                    modifier = Modifier.fillMaxHeight(),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 4.dp
                ) {
                    DrawerContent(navController, drawerState, auth)
                }
            },
            scrimColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.32f),
            content = {
                MainContent(navController, drawerState, scope)
            }
        )
    } else {
        MainContent(navController, drawerState, scope) // Render only the main content
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainContent(
    navController: NavHostController,
    drawerState: DrawerState,
    scope: CoroutineScope
) {

    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser

    var dropdownExpanded by remember { mutableStateOf(false) }




    Column(modifier = Modifier.padding()) {
        TopAppBar(
            title = { Text("My Movie App") },
            navigationIcon = {
                if (navController.currentBackStackEntry?.destination?.route !in listOf("login", "registration")) {
                    IconButton(onClick = {
                        scope.launch {
                            drawerState.open()
                        }
                    }) {
                        Icon(Icons.Filled.Menu, contentDescription = "Open Drawer")
                    }
                }
            },
            actions = {
                // Profile Icon
                if (currentUser != null) {
                    IconButton(onClick = { dropdownExpanded = true }) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "Profile"
                        )
                    }

                    // Dropdown menu
                    DropdownMenu(
                        expanded = dropdownExpanded,
                        onDismissRequest = { dropdownExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = {
                                var username by remember { mutableStateOf("User...") }
                                var profileImageUri by remember { mutableStateOf<String?>(null) }

                                LaunchedEffect(Unit) {
                                    fetchUserDataFromDB(
                                        onSuccess = { fetchedName, fetchedUri ->
                                            username = fetchedName
                                            profileImageUri = fetchedUri
                                        },
                                        onError = { error ->
                                            username = "User"

                                        }
                                    )
                                }

                                Row {
                                    // Profile Image
                                    if (profileImageUri != null) {
                                        Image(
                                            painter = rememberAsyncImagePainter(profileImageUri),
                                            contentDescription = "Profile Image",
                                            modifier = Modifier
                                                .size(24.dp)
                                                .padding(end = 8.dp)
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.AccountCircle,
                                            contentDescription = "Default Profile Image",
                                            modifier = Modifier
                                                .size(24.dp)
                                                .padding(end = 8.dp)
                                        )
                                    }

                                    // User's Name
                                    Text(text = username)
                                }
                            },
                            onClick = {
                                dropdownExpanded = false
                                navController.navigate("edit_profile")
                            }
                        )


                        Divider()
                        DropdownMenuItem(
                            text = { Text("Sign Out") },
                            onClick = {
                                auth.signOut()
                                navController.navigate("login") {
                                    popUpTo("login") { inclusive = true } // Clear backstack
                                }
                                dropdownExpanded = false
                            }
                        )
                    }
                }
            }
        )
        NavHost(
            navController = navController,
            startDestination = "login"
        ) {
            composable("home") { MovieScreen("popular", navController) }
            composable("now_playing") { MovieScreen("now_playing", navController) }
            composable("top_rated") { MovieScreen("top_rated", navController) }
            composable("edit_profile") { EditProfileScreen(navController, auth) }
            composable("login") { LoginScreen(navController, FirebaseAuth.getInstance()) }
            composable("registration") { RegistrationScreen(navController, FirebaseAuth.getInstance()) }
            composable("favourite") { FavoriteScreen(navController) }
            composable(
                "movieDetail/{movieJSON}",
                arguments = listOf(navArgument("movieJSON") { type = NavType.StringType })
            ) { backStackEntry ->
                val movieJson = backStackEntry.arguments?.getString("movieJSON")
                val movie = Gson().fromJson(movieJson, Movie::class.java)
                MovieDetailScreen(navController, movie)
            }
        }
    }
}



@Composable
fun DrawerContent(
    navController: NavController,
    drawerState: DrawerState,
    auth: FirebaseAuth
) {
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .padding(top = 30.dp)
            .width(150.dp)
            // Occupy full height
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .weight(1f) // Push content to fill available space except for the "Log Out" button
        ) {

            Text("Now Playing", modifier = Modifier.clickable {
                navController.navigate("now_playing")
                scope.launch { drawerState.close() }
            })
            Spacer(modifier = Modifier.padding(8.dp))
            Text("Popular", modifier = Modifier.clickable {
                navController.navigate("home")
                scope.launch { drawerState.close() }
            })
            Spacer(modifier = Modifier.padding(8.dp))
            Text("Top Rated", modifier = Modifier.clickable {
                navController.navigate("top_rated")
                scope.launch { drawerState.close() }
            })
            Spacer(modifier = Modifier.padding(8.dp))
            Text("Favourite", modifier = Modifier.clickable {
                navController.navigate("favourite")
                scope.launch { drawerState.close() }
            })
        }

    }
}

@Composable
fun DrawerMenuItem(title: String, onClick: () -> Unit) {
    // Menu item with padding, ripple effect, and improved spacing
    Text(
        text = title,
        style = MaterialTheme.typography.bodyLarge,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
            .clickable { onClick() },
        color = MaterialTheme.colorScheme.onSurface
    )
}


// Fetching Username and profile Pic uri from firebase realtime database from userId
fun fetchUserDataFromDB(
    onSuccess: (String, String?) -> Unit,
    onError: (String) -> Unit
) {
    val currentUser = FirebaseAuth.getInstance().currentUser
    val userId = currentUser?.uid

    if (userId == null) {
        onError("User not logged in")
        return
    }

    val database = FirebaseDatabase.getInstance().reference

    // Fetch the username from the database
    database.child("userprofile").child(userId).get()
        .addOnSuccessListener { snapshot ->
            val username = snapshot.child("username").getValue(String::class.java)
            val picUrl = snapshot.child("profileImageUri").getValue(String::class.java)
            if (username != null) {
                    onSuccess(username, picUrl)

            } else {
                onError("User")
            }


        }
        .addOnFailureListener { exception ->
            onError(exception.message ?: "Error fetching username and Pic")
        }
}

