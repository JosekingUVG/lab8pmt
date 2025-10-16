# Lab8PM - Aplicación de Galería de Fotos con Pexels API

Aplicación Android desarrollada en Kotlin con Jetpack Compose que consume la API de Pexels para mostrar y buscar fotografías.
## Video explicativo: 
youtube.com/watch?v=FwFdbY8D9jo&feature=youtu.be 
## 📱 Características

- ✅ Búsqueda de fotos en tiempo real con debounce
- ✅ Scroll infinito (paginación automática)
- ✅ Vista de detalles de cada foto
- ✅ Perfil de usuario con foto y nombre personalizables
- ✅ Compartir fotos
- ✅ Navegación entre pantallas
- 🔜 Favoritos con Room Database (próximamente)
- 🔜 Historial de búsquedas (próximamente)

## 🏗️ Arquitectura

El proyecto sigue una arquitectura MVVM limpia y organizada:

```
com.example.lab8pm/
├── MainActivity.kt
├── data/
│   ├── models/          # Modelos de datos y entidades
│   ├── network/         # Retrofit API
│   └── local/           # Room Database (preparado)
├── viewmodels/          # ViewModels
├── navigation/          # Navegación Compose
└── ui/
    ├── screens/         # Pantallas de la app
    └── theme/           # Tema y estilos
```

## 🛠️ Tecnologías Utilizadas

- **Kotlin** - Lenguaje de programación
- **Jetpack Compose** - UI declarativa
- **Navigation Compose** - Navegación entre pantallas
- **Retrofit** - Cliente HTTP para API REST
- **Gson** - Serialización JSON
- **Coil** - Carga de imágenes
- **Room** - Base de datos local (preparado)
- **Coroutines & Flow** - Programación asíncrona
- **Material 3** - Diseño moderno

## 📦 Dependencias

```kotlin
// Retrofit & Networking
implementation("com.squareup.retrofit2:retrofit:2.9.0")
implementation("com.squareup.retrofit2:converter-gson:2.9.0")
implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

// Room Database
implementation("androidx.room:room-runtime:2.8.2")
implementation("androidx.room:room-ktx:2.8.2")
kapt("androidx.room:room-compiler:2.8.2")

// Navigation
implementation("androidx.navigation:navigation-compose:2.7.6")

// Coil para imágenes
implementation("io.coil-kt:coil-compose:2.5.0")

// ViewModel
implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")

// Coroutines
implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
```

## 🚀 Configuración

1. Clona el repositorio
2. Abre el proyecto en Android Studio
3. Sincroniza Gradle
4. La API key de Pexels ya está incluida en el código
5. Ejecuta la aplicación

## 📱 Pantallas

### HomeScreen
- Barra de búsqueda con debounce
- Grid de fotos en 2 columnas
- Scroll infinito
- Botón de perfil

### DetailsScreen
- Imagen en alta resolución
- Información del fotógrafo
- Botón para ver perfil del fotógrafo
- Botón para compartir

### ProfileScreen
- Avatar personalizable
- Nombre de usuario editable
- Selector de foto de galería

## 🔄 Flujo de Navegación

```
HomeScreen → DetailsScreen
    ↓
ProfileScreen
```

## 📝 API Utilizada

- **Pexels API**: https://www.pexels.com/api/
- Endpoints utilizados:
  - `GET /v1/search` - Búsqueda de fotos
  - `GET /v1/photos/{id}` - Detalles de foto específica

## 🎯 Próximas Mejoras

- [ ] Implementar favoritos con Room
- [ ] Historial de búsquedas
- [ ] Modo offline
- [ ] Filtros de búsqueda
- [ ] Temas claro/oscuro
- [ ] Animaciones de transición
- [ ] Tests unitarios

## 👨‍💻 Desarrollador

Proyecto desarrollado como parte del Lab 8 PM

---

**Nota**: Este proyecto es con fines educativos.
