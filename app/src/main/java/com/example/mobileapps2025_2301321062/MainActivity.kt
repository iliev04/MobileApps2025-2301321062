package com.example.mobileapps2025_2301321062

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.mobileapps2025_2301321062.data.mockEvents
import com.example.mobileapps2025_2301321062.model.Event
import com.example.mobileapps2025_2301321062.ui.screens.EventDetailsScreen
import com.example.mobileapps2025_2301321062.ui.screens.EventListScreen
import com.example.mobileapps2025_2301321062.ui.screens.GenerateTicketScreen
import com.example.mobileapps2025_2301321062.ui.screens.MyTicketsScreen
import com.example.mobileapps2025_2301321062.ui.screens.TicketDetailsScreen
import com.example.mobileapps2025_2301321062.ui.theme.MobileApps20252301321062Theme

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
            val ticket = dbHelper.getAllTickets().find { it.id == ticketId }
            val event = events.find { it.id == ticket?.eventId }
            if (ticket != null) {
                TicketDetailsScreen(navController, ticket, event, dbHelper)
            }
        }
    }
}
