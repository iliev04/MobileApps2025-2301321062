package com.example.mobileapps2025_2301321062

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.mobileapps2025_2301321062.model.Event
import com.example.mobileapps2025_2301321062.model.Ticket

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "TicketMaster.db"
        private const val DATABASE_VERSION = 2

        // Events Table
        const val TABLE_EVENTS = "events"
        const val COLUMN_EVENT_ID = "id"
        const val COLUMN_EVENT_NAME = "name"
        const val COLUMN_EVENT_DATE = "date"
        const val COLUMN_EVENT_LOCATION = "location"
        const val COLUMN_EVENT_PRICE = "price"
        const val COLUMN_EVENT_DESCRIPTION = "description"

        // Tickets Table
        const val TABLE_TICKETS = "tickets"
        const val COLUMN_TICKET_ID = "id"
        const val COLUMN_TICKET_EVENT_ID = "event_id"
        const val COLUMN_TICKET_SEAT = "seat"
        const val COLUMN_TICKET_QR = "qr_code"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createEventsTable = ("CREATE TABLE " + TABLE_EVENTS + "("
                + COLUMN_EVENT_ID + " TEXT PRIMARY KEY,"
                + COLUMN_EVENT_NAME + " TEXT,"
                + COLUMN_EVENT_DATE + " TEXT,"
                + COLUMN_EVENT_LOCATION + " TEXT,"
                + COLUMN_EVENT_PRICE + " TEXT,"
                + COLUMN_EVENT_DESCRIPTION + " TEXT" + ")")
        db.execSQL(createEventsTable)

        val createTicketsTable = ("CREATE TABLE " + TABLE_TICKETS + "("
                + COLUMN_TICKET_ID + " TEXT PRIMARY KEY,"
                + COLUMN_TICKET_EVENT_ID + " TEXT,"
                + COLUMN_TICKET_SEAT + " TEXT,"
                + COLUMN_TICKET_QR + " TEXT,"
                + "FOREIGN KEY(" + COLUMN_TICKET_EVENT_ID + ") REFERENCES " + TABLE_EVENTS + "(" + COLUMN_EVENT_ID + ")" + ")")
        db.execSQL(createTicketsTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_EVENTS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_TICKETS")
        onCreate(db)
    }

    fun insertEvent(event: Event) {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_EVENT_ID, event.id)
            put(COLUMN_EVENT_NAME, event.name)
            put(COLUMN_EVENT_DATE, event.date)
            put(COLUMN_EVENT_LOCATION, event.location)
            put(COLUMN_EVENT_PRICE, event.price)
            put(COLUMN_EVENT_DESCRIPTION, event.description)
        }
        db.insert(TABLE_EVENTS, null, values)
        db.close()
    }

    fun getAllEvents(): List<Event> {
        val eventList = ArrayList<Event>()
        val selectQuery = "SELECT * FROM $TABLE_EVENTS"
        val db = this.readableDatabase
        val cursor = db.rawQuery(selectQuery, null)

        if (cursor.moveToFirst()) {
            do {
                val event = Event(
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EVENT_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EVENT_NAME)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EVENT_DATE)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EVENT_LOCATION)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EVENT_PRICE)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EVENT_DESCRIPTION))
                )
                eventList.add(event)
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return eventList
    }

    fun insertTicket(ticketId: String, eventId: String, seat: String, qrCode: String) {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_TICKET_ID, ticketId)
            put(COLUMN_TICKET_EVENT_ID, eventId)
            put(COLUMN_TICKET_SEAT, seat)
            put(COLUMN_TICKET_QR, qrCode)
        }
        db.insert(TABLE_TICKETS, null, values)
        db.close()
    }

    fun updateTicket(ticketId: String, newSeat: String) {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_TICKET_SEAT, newSeat)
        }
        db.update(TABLE_TICKETS, values, "$COLUMN_TICKET_ID = ?", arrayOf(ticketId))
        db.close()
    }

    fun updateEventDate(eventId: String, newDate: String) {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_EVENT_DATE, newDate)
        }
        db.update(TABLE_EVENTS, values, "$COLUMN_EVENT_ID = ?", arrayOf(eventId))
        db.close()
    }

    fun deleteTicket(ticketId: String) {
        val db = this.writableDatabase
        db.delete(TABLE_TICKETS, "$COLUMN_TICKET_ID = ?", arrayOf(ticketId))
        db.close()
    }

    fun getAllTickets(): List<Ticket> {
        val ticketList = ArrayList<Ticket>()
        val selectQuery = "SELECT t.*, e.${COLUMN_EVENT_NAME}, e.${COLUMN_EVENT_DATE} FROM $TABLE_TICKETS t JOIN $TABLE_EVENTS e ON t.$COLUMN_TICKET_EVENT_ID = e.$COLUMN_EVENT_ID"
        val db = this.readableDatabase
        val cursor = db.rawQuery(selectQuery, null)

        if (cursor.moveToFirst()) {
            do {
                val ticket = Ticket(
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TICKET_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TICKET_EVENT_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TICKET_SEAT)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TICKET_QR)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EVENT_NAME)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EVENT_DATE))
                )
                ticketList.add(ticket)
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return ticketList
    }

    fun getTakenSeats(eventId: String): List<String> {
        val seats = ArrayList<String>()
        val db = this.readableDatabase
        val cursor = db.query(TABLE_TICKETS, arrayOf(COLUMN_TICKET_SEAT), "$COLUMN_TICKET_EVENT_ID = ?", arrayOf(eventId), null, null, null)
        if (cursor.moveToFirst()) {
            do {
                seats.add(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TICKET_SEAT)))
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return seats
    }
}
