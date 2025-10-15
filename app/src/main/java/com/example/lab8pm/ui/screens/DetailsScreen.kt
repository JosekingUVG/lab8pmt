package com.example.lab8pm.ui.screens

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.lab8pm.data.models.PexelsPhoto
import com.example.lab8pm.data.network.ApiClient
import com.example.lab8pm.viewmodels.HomeViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Pantalla de detalles de una foto específica
 * Muestra información completa y opciones para compartir
 */
@SuppressLint("UnrememberedGetBackStackEntry")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailsScreen(
    photoId: String,
    navController: NavHostController
) {
    // Obtener el ViewModel compartido de HomeScreen
    val homeEntry = remember(navController) {
        navController.getBackStackEntry("home")
    }
    val homeViewModel: HomeViewModel = viewModel(homeEntry)
    val photos by homeViewModel.photos.collectAsState()

    // Buscar la foto en la lista local primero
    val idInt = photoId.toIntOrNull()
    val localPhoto = remember(photos, photoId) {
        idInt?.let { id -> photos.find { it.id == id } }
    }

    // Estados para fetch por API si no está en local
    var fetchedPhoto by remember { mutableStateOf<PexelsPhoto?>(null) }
    var fetchingError by remember { mutableStateOf<String?>(null) }
    var isFetching by remember { mutableStateOf(false) }

    // Intentar obtener la foto por API si no está en la lista local
    LaunchedEffect(key1 = idInt, key2 = localPhoto) {
        if (localPhoto != null || idInt == null) return@LaunchedEffect

        isFetching = true
        fetchingError = null
        try {
            val fetched = withContext(Dispatchers.IO) {
                ApiClient.pexelsApi.getPhotoById(idInt)
            }
            fetchedPhoto = fetched
        } catch (t: Throwable) {
            t.printStackTrace()
            fetchingError = t.message ?: "Error al obtener foto por id"
        } finally {
            isFetching = false
        }
    }

    // Foto a mostrar (prioridad: local > fetched)
    val displayPhoto = localPhoto ?: fetchedPhoto

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalles de Foto") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            when {
                // Cargando desde API
                isFetching -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                // ID inválido
                idInt == null -> {
                    Text(
                        "ID inválido: $photoId",
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                // Error al cargar
                displayPhoto == null && fetchingError != null -> {
                    Text(
                        "Error: $fetchingError",
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                // No se encontró la foto
                displayPhoto == null -> {
                    Text(
                        "Foto no encontrada",
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                // Mostrar la foto
                else -> {
                    val photo = displayPhoto!!
                    val context = LocalContext.current

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Imagen principal
                        AsyncImage(
                            model = photo.src.large,
                            contentDescription = "Foto de ${photo.photographer}",
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(photo.width.toFloat() / photo.height.toFloat())
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Información de la foto
                        Text(
                            text = "Autor: ${photo.photographer}",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Resolución: ${photo.width} x ${photo.height}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "ID: ${photo.id}",
                            style = MaterialTheme.typography.bodySmall
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Botón para ver perfil del fotógrafo
                        TextButton(onClick = {
                            val url = photo.photographer_url
                            if (url.isNotBlank()) {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                context.startActivity(intent)
                            }
                        }) {
                            Text("Ver perfil del fotógrafo")
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Botón para compartir
                        Button(onClick = {
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, photo.src.large)
                                type = "text/plain"
                            }
                            val chooser = Intent.createChooser(sendIntent, "Compartir foto")
                            context.startActivity(chooser)
                        }) {
                            Text("Compartir")
                        }
                    }
                }
            }
        }
    }
}