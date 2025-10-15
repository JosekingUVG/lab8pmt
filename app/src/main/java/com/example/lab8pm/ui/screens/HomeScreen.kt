package com.example.lab8pm.ui.screens

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.lab8pm.viewmodels.HomeViewModel
import com.example.lab8pm.viewmodels.UserProfileViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce

private const val TAG_SCREEN = "HomeScreen"

/**
 * Pantalla principal que muestra la lista de fotos
 * Incluye barra de búsqueda y scroll infinito
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavHostController,
    homeViewModel: HomeViewModel = viewModel(),
    userProfileViewModel: UserProfileViewModel = viewModel()
) {
    // Estado local de la búsqueda
    var query by rememberSaveable { mutableStateOf("nature") }

    // Observar estados del ViewModel
    val photos by homeViewModel.photos.collectAsState()
    val loading by homeViewModel.loading.collectAsState()
    val error by homeViewModel.error.collectAsState()
    val userProfile = userProfileViewModel.userProfile

    // Efecto para búsqueda inicial y debounce
    LaunchedEffect(Unit) {
        Log.d(TAG_SCREEN, "Initial load q='$query'")
        homeViewModel.searchPhotos(query)

        // Debounce para evitar búsquedas mientras el usuario escribe
        snapshotFlow { query }
            .debounce(500)
            .collectLatest { text ->
                Log.d(TAG_SCREEN, "debounced collect: '$text'")
                if (text.isBlank()) {
                    homeViewModel.clear()
                } else {
                    homeViewModel.searchPhotos(text)
                }
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Fotos")
                        Spacer(modifier = Modifier.height(8.dp))

                        // Campo de búsqueda
                        TextField(
                            value = query,
                            onValueChange = { query = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Buscar...") },
                            singleLine = true,
                            trailingIcon = {
                                Row {
                                    // Botón para limpiar
                                    IconButton(onClick = { query = "" }) {
                                        Icon(
                                            imageVector = Icons.Default.Clear,
                                            contentDescription = "Limpiar"
                                        )
                                    }
                                    // Botón de búsqueda manual
                                    IconButton(onClick = { homeViewModel.searchPhotos(query) }) {
                                        Icon(
                                            imageVector = Icons.Default.Search,
                                            contentDescription = "Buscar"
                                        )
                                    }
                                }
                            }
                        )
                    }
                },
                actions = {
                    // Botón de perfil
                    IconButton(onClick = { navController.navigate("profile") }) {
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Indicador de carga inicial
            if (photos.isEmpty() && loading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                return@Box
            }

            // Mensaje de error
            if (photos.isEmpty() && error != null) {
                Text(
                    text = error ?: "Error",
                    modifier = Modifier.align(Alignment.Center)
                )
                return@Box
            }

            // Contenido principal
            Column(modifier = Modifier.fillMaxSize()) {
                Text(
                    text = "Resultados: ${photos.size}",
                    modifier = Modifier.padding(8.dp),
                    style = MaterialTheme.typography.bodyMedium
                )

                // Grid de fotos
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(photos) { index, photo ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    navController.navigate("details/${photo.id}")
                                }
                        ) {
                            AsyncImage(
                                model = photo.src.medium,
                                contentDescription = photo.photographer,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(150.dp)
                            )
                            Text(
                                text = photo.photographer,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(4.dp)
                            )
                        }

                        // Cargar siguiente página al llegar al final
                        if (index == photos.lastIndex && photos.isNotEmpty() && !loading) {
                            LaunchedEffect(photos.size) {
                                Log.d(TAG_SCREEN, "Reached end -> cargar siguiente página")
                                homeViewModel.loadNextPage()
                            }
                        }
                    }
                }

                // Indicador de carga al paginar
                if (loading && photos.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }
}