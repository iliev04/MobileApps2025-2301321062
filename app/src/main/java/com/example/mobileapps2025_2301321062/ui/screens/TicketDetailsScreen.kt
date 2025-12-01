package com.example.mobileapps2025_2301321062.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.mobileapps2025_2301321062.DatabaseHelper
import com.example.mobileapps2025_2301321062.model.Event
import com.example.mobileapps2025_2301321062.model.Ticket
import com.example.mobileapps2025_2301321062.ui.components.ActionButton
import com.example.mobileapps2025_2301321062.utils.generateQRCode

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
