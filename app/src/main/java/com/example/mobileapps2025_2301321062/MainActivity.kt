package com.example.mobileapps2025_2301321062

import android.graphics.Bitmap
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.mobileapps2025_2301321062.ui.theme.MobileApps20252301321062Theme
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import java.util.UUID
import kotlin.random.Random

data class Event(
    val id: String,
    val name: String,
    val date: String,
    val location: String,
    val price: String,
    val description: String
)

data class Ticket(
    val id: String,
    val eventId: String,
    val seat: String,
    val qrCode: String,
    val eventName: String,
    val eventDate: String
)

val mockEvents = listOf(
    Event("1", "Concert A", "2023-11-20", "Stadium 1", "$50", "A great concert"),
    Event("2", "Theater B", "2023-11-25", "Theater Hall 2", "$30", "A nice play"),
    Event("3", "Conference C", "2023-12-01", "Convention Center", "$100", "Tech conference")
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        val dbHelper = DatabaseHelper(this)
        var events = dbHelper.getAllEvents()
        if (events.isEmpty()) {
            mockEvents.forEach { dbHelper.insertEvent(it) }
            events = dbHelper.getAllEvents()
        }

        setContent {
            MobileApps20252301321062Theme {
                TicketMasterApp(events = events, dbHelper = dbHelper)
            }
        }
    }
}

@Composable
fun TicketMasterApp(events: List<Event>, dbHelper: DatabaseHelper) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "eventList") {
        composable("eventList") {
            EventListScreen(navController, events)
        }
        composable("eventDetails/{eventId}") { backStackEntry ->
            val eventId = backStackEntry.arguments?.getString("eventId")
            val event = events.find { it.id == eventId }
            if (event != null) {
                EventDetailsScreen(navController, event)
            }
        }
        composable("generateTicket/{eventId}") { backStackEntry ->
            val eventId = backStackEntry.arguments?.getString("eventId")
            val event = events.find { it.id == eventId }
            if (event != null) {
                GenerateTicketScreen(navController, event, dbHelper)
            }
        }
        composable("myTickets") {
            MyTicketsScreen(navController, dbHelper)
        }
        composable("ticketDetails/{ticketId}") { backStackEntry ->
            val ticketId = backStackEntry.arguments?.getString("ticketId")
            // Fetching all tickets here is not efficient for production but works for this scope
            val ticket = dbHelper.getAllTickets().find { it.id == ticketId }
            val event = events.find { it.id == ticket?.eventId }
            if (ticket != null) {
                TicketDetailsScreen(navController, ticket, event, dbHelper)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventListScreen(navController: NavHostController, events: List<Event>) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Events", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(events) { event ->
                EventCard(event = event) {
                    navController.navigate("eventDetails/${event.id}")
                }
            }
            item {
                Spacer(modifier = Modifier.height(16.dp))
                ActionButton(
                    text = "My Tickets",
                    emoji = "🎫",
                    onClick = { navController.navigate("myTickets") }
                )
            }
        }
    }
}

@Composable
fun EventCard(event: Event, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = event.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Date: ${event.date}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Location: ${event.location}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Price: ${event.price}", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailsScreen(navController: NavHostController, event: Event) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(event.name, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = event.description,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            ActionButton(
                text = "Generate Ticket",
                emoji = "🎟️",
                onClick = { navController.navigate("generateTicket/${event.id}") }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenerateTicketScreen(navController: NavHostController, event: Event, dbHelper: DatabaseHelper) {
    var seat by remember { mutableStateOf("") }
    var qrCodeBitmap by remember { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(Unit) {
        val takenSeats = dbHelper.getTakenSeats(event.id)
        var newSeat = "Seat ${Random.nextInt(1, 1000)}"
        while (takenSeats.contains(newSeat)) {
            newSeat = "Seat ${Random.nextInt(1, 1000)}"
        }
        seat = newSeat

        val ticketId = UUID.randomUUID().toString()
        val qrContent = "TicketID: $ticketId, Event: ${event.name}, Seat: $seat"
        
        dbHelper.insertTicket(ticketId, event.id, seat, qrContent)
        qrCodeBitmap = generateQRCode(qrContent)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Your Ticket", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (seat.isNotEmpty()) {
                Text(
                    text = event.name,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Date: ${event.date}", style = MaterialTheme.typography.bodyLarge)
                Text(text = "Location: ${event.location}", style = MaterialTheme.typography.bodyLarge)
                Text(text = "Seat: $seat", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

                Spacer(modifier = Modifier.height(32.dp))

                if (qrCodeBitmap != null) {
                    Image(
                        bitmap = qrCodeBitmap!!.asImageBitmap(),
                        contentDescription = "QR Code",
                        modifier = Modifier.size(250.dp)
                    )
                }
            } else {
                Text("Generating Ticket...")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyTicketsScreen(navController: NavHostController, dbHelper: DatabaseHelper) {
    var tickets by remember { mutableStateOf(emptyList<Ticket>()) }
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                tickets = dbHelper.getAllTickets()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Tickets", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(tickets) { ticket ->
                TicketCard(ticket = ticket) {
                     navController.navigate("ticketDetails/${ticket.id}")
                }
            }
        }
    }
}

@Composable
fun TicketCard(ticket: Ticket, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = ticket.eventName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Date: ${ticket.eventDate}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Seat: ${ticket.seat}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TicketDetailsScreen(navController: NavHostController, ticket: Ticket, event: Event?, dbHelper: DatabaseHelper) {
    val qrCodeBitmap = remember(ticket.qrCode) { generateQRCode(ticket.qrCode) }
    
    var showUpdateDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var newSeat by remember { mutableStateOf(ticket.seat) }
    var newDate by remember { mutableStateOf(event?.date ?: "") }

    if (showUpdateDialog) {
        AlertDialog(
            onDismissRequest = { showUpdateDialog = false },
            title = { Text("Update Ticket") },
            text = {
                Column {
                    OutlinedTextField(
                        value = newSeat,
                        onValueChange = { newSeat = it },
                        label = { Text("Seat Number") }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newDate,
                        onValueChange = { newDate = it },
                        label = { Text("Date (YYYY-MM-DD)") }
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        dbHelper.updateTicket(ticket.id, newSeat)
                        if (event != null) {
                            dbHelper.updateEventDate(event.id, newDate)
                        }
                        showUpdateDialog = false
                        navController.popBackStack() // Go back to refresh
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showUpdateDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Ticket") },
            text = { Text("Are you sure you want to delete this ticket? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        dbHelper.deleteTicket(ticket.id)
                        showDeleteDialog = false
                        navController.popBackStack() // Go back to list
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ticket Details", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = ticket.eventName,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Date: ${ticket.eventDate}", style = MaterialTheme.typography.bodyLarge)
            if (event != null) {
                Text(text = "Location: ${event.location}", style = MaterialTheme.typography.bodyLarge)
            }
            Text(text = "Seat: ${ticket.seat}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

            Spacer(modifier = Modifier.height(32.dp))

            if (qrCodeBitmap != null) {
                Image(
                    bitmap = qrCodeBitmap.asImageBitmap(),
                    contentDescription = "QR Code",
                    modifier = Modifier.size(250.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            ActionButton(
                text = "Update Ticket",
                emoji = "✏️",
                onClick = { showUpdateDialog = true }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = { showDeleteDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(text = "🗑️", fontSize = 28.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(text = "Delete Ticket", fontSize = 18.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

fun generateQRCode(text: String): Bitmap? {
    return try {
        val writer = QRCodeWriter()
        val bitMatrix = writer.encode(text, BarcodeFormat.QR_CODE, 512, 512)
        val width = bitMatrix.width
        val height = bitMatrix.height
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        for (x in 0 until width) {
            for (y in 0 until height) {
                bitmap.setPixel(x, y, if (bitMatrix.get(x, y)) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
            }
        }
        bitmap
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

@Composable
fun ActionButton(
    text: String,
    emoji: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
            .fillMaxWidth()
            .height(60.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(text = emoji, fontSize = 28.sp)
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = text, fontSize = 18.sp, fontWeight = FontWeight.Medium)
        }
    }
}
