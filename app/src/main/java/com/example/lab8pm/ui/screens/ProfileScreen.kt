package com.example.lab8pm.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.lab8pm.viewmodels.UserProfileViewModel
import kotlinx.coroutines.launch

/**
 * Pantalla de perfil de usuario
 * Permite editar nombre y foto de perfil con persistencia en Room
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavHostController,
    userProfileViewModel: UserProfileViewModel = viewModel(
        viewModelStoreOwner = navController.getBackStackEntry("home")
    )
) {
    val context = LocalContext.current
    val userProfile = userProfileViewModel.userProfile

    // Estados locales temporales para edición
    var tempName by rememberSaveable { mutableStateOf(userProfile.name) }
    var tempPhotoUri by remember { mutableStateOf<Uri?>(userProfile.photoUri) }

    // Estado para mostrar Snackbar
    val snackbarHostState = remember { SnackbarHostState() }

    // Launcher para seleccionar imagen de galería
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            if (uri != null) tempPhotoUri = uri
        }
    )

    // Sincronizar con los cambios del ViewModel
    LaunchedEffect(userProfile.name, userProfile.photoUri) {
        tempName = userProfile.name
        tempPhotoUri = userProfile.photoUri
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Perfil de Usuario") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                actions = {
                    // Mostrar foto de perfil actual en la barra
                    IconButton(onClick = { /* Ya estamos en profile */ }) {
                        if (userProfile.photoUri != null) {
                            AsyncImage(
                                model = userProfile.photoUri,
                                contentDescription = "Perfil",
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = "Perfil",
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Avatar / Foto de perfil
            if (tempPhotoUri != null) {
                AsyncImage(
                    model = tempPhotoUri,
                    contentDescription = "Foto de perfil",
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                )
            } else {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "Avatar",
                    modifier = Modifier.size(120.dp)
                )
            }

            // Campo de texto para el nombre
            OutlinedTextField(
                value = tempName,
                onValueChange = { tempName = it },
                label = { Text("Nombre de usuario") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Botón para seleccionar foto
            Button(
                onClick = { launcher.launch("image/*") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Seleccionar Foto")
            }

            // Botón para guardar cambios
            Button(
                onClick = {
                    userProfileViewModel.updateProfile(tempName, tempPhotoUri)

                    // Mostrar confirmación
                    kotlinx.coroutines.GlobalScope.launch {
                        snackbarHostState.showSnackbar(
                            message = "Perfil guardado correctamente",
                            duration = SnackbarDuration.Short
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Guardar Cambios")
            }

            Spacer(modifier = Modifier.weight(1f))

            // Información adicional
            Text(
                text = "Los cambios se guardan en la base de datos local",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}