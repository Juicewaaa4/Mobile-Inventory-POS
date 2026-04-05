package com.example.mobileinventory.ui

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.mobileinventory.ui.theme.*
import com.example.mobileinventory.viewmodel.MainViewModel
import kotlinx.coroutines.delay

@Composable
fun LoginScreen(navController: NavController, viewModel: MainViewModel) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // Staggered entrance animations
    var showLogo by remember { mutableStateOf(false) }
    var showTitle by remember { mutableStateOf(false) }
    var showCard by remember { mutableStateOf(false) }

    val loginError by viewModel.loginError.collectAsState()
    
    LaunchedEffect(loginError) {
        if (loginError != null) {
            Toast.makeText(context, loginError, Toast.LENGTH_LONG).show()
            viewModel.clearLoginError()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.authRepository.auth.signOut()
        viewModel._userRole.value = null
        delay(100)
        showLogo = true
        delay(200)
        showTitle = true
        delay(150)
        showCard = true
    }

    val userRole by viewModel.userRole.collectAsState()
    LaunchedEffect(userRole) {
        if (userRole == "admin") {
            navController.navigate("admin_dashboard") { popUpTo("login") { inclusive = true } }
        } else if (userRole == "cashier") {
            navController.navigate("cashier_dashboard") { popUpTo("login") { inclusive = true } }
        }
    }

    // Gradient background
    val gradientBrush = Brush.verticalGradient(
        colors = listOf(
            LoginGradientStart,
            LoginGradientEnd,
            GreenSecondary.copy(alpha = 0.85f)
        ),
        startY = 0f,
        endY = 2000f
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradientBrush)
    ) {
        // Subtle decorative circles in the background
        Box(
            modifier = Modifier
                .size(200.dp)
                .offset(x = (-50).dp, y = (-30).dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.06f))
        )
        Box(
            modifier = Modifier
                .size(140.dp)
                .offset(x = 280.dp, y = 60.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.04f))
        )
        Box(
            modifier = Modifier
                .size(100.dp)
                .offset(x = 30.dp, y = 650.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.03f))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(72.dp))

            // Logo with glow effect
            AnimatedVisibility(
                visible = showLogo,
                enter = fadeIn(animationSpec = tween(600)) + slideInVertically(
                    initialOffsetY = { -40 },
                    animationSpec = tween(600, easing = FastOutSlowInEasing)
                )
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .shadow(24.dp, CircleShape, ambientColor = Color.White.copy(alpha = 0.3f))
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Inventory2,
                        contentDescription = null,
                        modifier = Modifier.size(44.dp),
                        tint = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Title with animation
            AnimatedVisibility(
                visible = showTitle,
                enter = fadeIn(animationSpec = tween(500)) + slideInVertically(
                    initialOffsetY = { -30 },
                    animationSpec = tween(500, easing = FastOutSlowInEasing)
                )
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "Inventory",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontSize = 30.sp,
                            letterSpacing = (-0.5).sp
                        ),
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "Point of Sale & Stock Manager",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            letterSpacing = 1.sp
                        ),
                        color = Color.White.copy(alpha = 0.75f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            // Login Card — Premium look
            AnimatedVisibility(
                visible = showCard,
                enter = fadeIn(animationSpec = tween(600)) + slideInVertically(
                    initialOffsetY = { 60 },
                    animationSpec = tween(600, easing = FastOutSlowInEasing)
                )
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                        .shadow(
                            elevation = 20.dp,
                            shape = RoundedCornerShape(topStart = 36.dp, topEnd = 36.dp),
                            ambientColor = Color.Black.copy(alpha = 0.15f)
                        ),
                    shape = RoundedCornerShape(topStart = 36.dp, topEnd = 36.dp),
                    colors = CardDefaults.cardColors(containerColor = LoginCardBackground),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 28.dp, vertical = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Decorative accent line
                        Box(
                            modifier = Modifier
                                .width(40.dp)
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(GreenPrimary.copy(alpha = 0.3f))
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            "Welcome Back",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                letterSpacing = (-0.3).sp
                            ),
                            fontWeight = FontWeight.Bold,
                            color = GreenOnBackground
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "Sign in to your account",
                            style = MaterialTheme.typography.bodyMedium,
                            color = GreenOnSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(36.dp))

                        // Email field
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Email Address") },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Email,
                                    contentDescription = null,
                                    tint = GreenPrimary.copy(alpha = 0.7f)
                                )
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GreenPrimary,
                                unfocusedBorderColor = GreenOutline,
                                focusedLabelColor = GreenPrimary,
                                cursorColor = GreenPrimary
                            ),
                            textStyle = androidx.compose.ui.text.TextStyle(color = Color.Black)
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        // Password field
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Password") },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = GreenPrimary.copy(alpha = 0.7f)
                                )
                            },
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            trailingIcon = {
                                val image = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector = image,
                                        contentDescription = "Toggle password",
                                        tint = GreenOnSurfaceVariant
                                    )
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GreenPrimary,
                                unfocusedBorderColor = GreenOutline,
                                focusedLabelColor = GreenPrimary,
                                cursorColor = GreenPrimary
                            ),
                            textStyle = androidx.compose.ui.text.TextStyle(color = Color.Black)
                        )

                        Spacer(modifier = Modifier.height(36.dp))

                        // Login Button — gradient style
                        Button(
                            onClick = {
                                if (email.isNotBlank() && password.isNotBlank()) {
                                    isLoading = true
                                    viewModel.authRepository.auth.signInWithEmailAndPassword(email, password)
                                        .addOnCompleteListener { task ->
                                            isLoading = false
                                            if (task.isSuccessful) {
                                                val user = task.result.user
                                                if (user != null) viewModel.fetchUserRole(user.uid)
                                            } else {
                                                Toast.makeText(context, "Login Failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                                            }
                                        }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(14.dp),
                            enabled = !isLoading,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = GreenPrimary,
                                contentColor = Color.White,
                                disabledContainerColor = GreenPrimary.copy(alpha = 0.5f)
                            ),
                            elevation = ButtonDefaults.buttonElevation(
                                defaultElevation = 6.dp,
                                pressedElevation = 2.dp
                            )
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = Color.White,
                                    strokeWidth = 2.5.dp
                                )
                            } else {
                                Text(
                                    "Sign In",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        // Divider with branding
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Divider(
                                modifier = Modifier.weight(1f),
                                color = GreenOutline.copy(alpha = 0.5f)
                            )
                            Text(
                                " Inventory ",
                                style = MaterialTheme.typography.labelSmall,
                                color = GreenOnSurfaceVariant.copy(alpha = 0.5f)
                            )
                            Divider(
                                modifier = Modifier.weight(1f),
                                color = GreenOutline.copy(alpha = 0.5f)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            "Powered by Firebase • Multi-Tenant",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 11.sp,
                                letterSpacing = 0.8.sp
                            ),
                            color = GreenOnSurfaceVariant.copy(alpha = 0.4f),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "© 2026",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = GreenOnSurfaceVariant.copy(alpha = 0.3f)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAccountScreen(navController: NavController, viewModel: MainViewModel) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var role by remember { mutableStateOf("cashier") }
    var isLoading by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val currentStoreId by viewModel.storeId.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Create Account",
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = GreenPrimary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        "New Team Member",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Create a new user account for your team. They will be linked to your store automatically.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email Address") },
                leadingIcon = {
                    Icon(
                        Icons.Default.Email,
                        contentDescription = null,
                        tint = GreenPrimary.copy(alpha = 0.7f)
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GreenPrimary,
                    unfocusedBorderColor = GreenOutline,
                    focusedLabelColor = GreenPrimary,
                    cursorColor = GreenPrimary
                )
            )
            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password (min 6 characters)") },
                leadingIcon = {
                    Icon(
                        Icons.Default.Lock,
                        contentDescription = null,
                        tint = GreenPrimary.copy(alpha = 0.7f)
                    )
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    val image = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = image,
                            contentDescription = "Toggle password",
                            tint = GreenOnSurfaceVariant
                        )
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GreenPrimary,
                    unfocusedBorderColor = GreenOutline,
                    focusedLabelColor = GreenPrimary,
                    cursorColor = GreenPrimary
                )
            )
            Spacer(modifier = Modifier.height(24.dp))

            // Role selection card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        "Assign Role",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Cashier option
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (role == "cashier")
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                            else MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = role == "cashier",
                                onClick = { role = "cashier" },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = GreenPrimary
                                )
                            )
                            Column(modifier = Modifier.padding(start = 4.dp)) {
                                Text("Cashier", fontWeight = FontWeight.Medium)
                                Text(
                                    "POS access only",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Admin option
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (role == "admin")
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                            else MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = role == "admin",
                                onClick = { role = "admin" },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = GreenPrimary
                                )
                            )
                            Column(modifier = Modifier.padding(start = 4.dp)) {
                                Text("Admin", fontWeight = FontWeight.Medium)
                                Text(
                                    "Full access — inventory & accounts",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            Button(
                onClick = {
                    isLoading = true
                    viewModel.authRepository.createAccount(email, password, role, currentStoreId) { success, msg ->
                        isLoading = false
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        if (success) navController.popBackStack()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(14.dp),
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = GreenPrimary,
                    contentColor = Color.White,
                    disabledContainerColor = GreenPrimary.copy(alpha = 0.5f)
                ),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 4.dp,
                    pressedElevation = 1.dp
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.5.dp
                    )
                } else {
                    Text(
                        "Create Account",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.3.sp
                    )
                }
            }
        }
    }
}
