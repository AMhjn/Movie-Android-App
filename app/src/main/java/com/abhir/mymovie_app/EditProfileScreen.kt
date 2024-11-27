package com.abhir.mymovie_app

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

@Composable
fun EditProfileScreen(navController: NavController, auth: FirebaseAuth) {
    val context = LocalContext.current
    val currentUser = auth.currentUser
    val database = FirebaseDatabase.getInstance().reference
    val userId = currentUser?.uid

    // Mutable states for user details
    var profileImageUri by remember { mutableStateOf<Uri?>(null) }
    var profileImageDownloadUrl by remember { mutableStateOf<String?>(null) }
    var username by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }
    var isUploading by remember { mutableStateOf(false) }

    // Default profile image
    val defaultProfileImage = painterResource(id = R.drawable.defaultimage)

    // Image picker launcher
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            if (uri != null) {
                profileImageUri = uri
            }
        }
    )

    // Load user profile from the database
    LaunchedEffect(userId) {
        if (userId != null) {
            database.child("userprofile").child(userId).addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    username = snapshot.child("username").getValue(String::class.java) ?: ""
                    age = snapshot.child("age").getValue(String::class.java) ?: ""
                    bio = snapshot.child("bio").getValue(String::class.java) ?: ""
                    profileImageDownloadUrl =
                        snapshot.child("profileImageUri").getValue(String::class.java)
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(context, "Failed to load profile: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding() // Automatically adjust for keyboard
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Profile Image Display
        if (profileImageUri == null && profileImageDownloadUrl == null) {
            Image(
                painter = defaultProfileImage,
                contentDescription = "Default Profile Image",
                modifier = Modifier.size(150.dp),
                contentScale = ContentScale.Crop
            )
        } else {
            Image(
                painter = rememberAsyncImagePainter(
                    profileImageUri ?: Uri.parse(profileImageDownloadUrl)
                ),
                contentDescription = "Profile Image",
                modifier = Modifier
                    .size(200.dp)
                    .padding(8.dp),
                contentScale = ContentScale.Crop
            )
        }

        Button(onClick = { launcher.launch("image/*") }, enabled = !isUploading) {
            Text("Select Profile Image")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (profileImageUri != null) {
                    isUploading = true
                    uploadProfileImage(
                        userId.orEmpty(),
                        profileImageUri!!,
                        onSuccess = { downloadUrl ->
                            profileImageDownloadUrl = downloadUrl
                            isUploading = false
                            Toast.makeText(context, "Image uploaded successfully!", Toast.LENGTH_SHORT).show()
                        },
                        onError = { error ->
                            isUploading = false
                            Toast.makeText(context, "Image upload failed: $error", Toast.LENGTH_SHORT).show()
                        }
                    )
                } else {
                    Toast.makeText(context, "No image selected", Toast.LENGTH_SHORT).show()
                }
            },
            enabled = !isUploading
        ) {
            if (isUploading) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary)
            } else {
                Text("Upload Profile Image")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Username Input
        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username") },
            modifier = Modifier.fillMaxWidth()
        )

        // Age Input
        OutlinedTextField(
            value = age,
            onValueChange = { age = it },
            label = { Text("Age") },
            modifier = Modifier.fillMaxWidth()
        )

        // Bio Input
        OutlinedTextField(
            value = bio,
            onValueChange = { bio = it },
            label = { Text("Bio") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Save Profile Button
        Button(
            onClick = {

                isSaving = true
                if (userId != null) {
                    val userProfile = mapOf(
                        "username" to username,
                        "age" to age,
                        "bio" to bio,
                        "profileImageUri" to profileImageDownloadUrl
                    )
                    database.child("userprofile").child(userId).setValue(userProfile)
                        .addOnCompleteListener { task ->
                            isSaving = false
                            if (task.isSuccessful) {
                               UserProfileChangeRequest.Builder()
                                    .setDisplayName(username) // Set display name in Auth
                                    .build()
                                Toast.makeText(context, "Profile saved successfully!", Toast.LENGTH_SHORT).show()
                                navController.navigate("home")
                            } else {
                                isSaving = false
                                Toast.makeText(context, "Failed to save profile: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isSaving
        ) {
            if (isSaving) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary)
            } else {
                Text("Save Profile")
            }
        }
    }
}

fun uploadProfileImage(
    userId: String,
    fileUri: Uri,
    onSuccess: (String) -> Unit,
    onError: (String) -> Unit
) {
    val storageRef = FirebaseStorage.getInstance().reference
    val profilePicRef = storageRef.child("profile_images/$userId.jpg")

    profilePicRef.putFile(fileUri)
        .addOnSuccessListener {
            profilePicRef.downloadUrl.addOnSuccessListener { uri ->
                onSuccess(uri.toString())
            }.addOnFailureListener { exception ->
                onError(exception.message.orEmpty())
            }
        }
        .addOnFailureListener { exception ->
            onError(exception.message.orEmpty())
        }
}
