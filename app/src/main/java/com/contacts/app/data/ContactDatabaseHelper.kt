package com.contacts.app.data

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.contacts.app.model.Contact
import java.util.Locale

class ContactDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "contacts.db"
        private const val DATABASE_VERSION = 1

        const val TABLE_CONTACTS = "contacts"
        const val COL_ID = "id"
        const val COL_NAME = "name"
        const val COL_PHONE = "phone"
        const val COL_SECONDARY_PHONE = "secondary_phone"
        const val COL_EMAIL = "email"
        const val COL_CATEGORY = "category"
        const val COL_NOTES = "notes"
        const val COL_IS_FAVORITE = "is_favorite"
        const val COL_AVATAR_COLOR = "avatar_color_index"
        const val COL_CREATED_AT = "created_at"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createQuery = """
            CREATE TABLE $TABLE_CONTACTS (
                $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_NAME TEXT NOT NULL,
                $COL_PHONE TEXT NOT NULL,
                $COL_SECONDARY_PHONE TEXT,
                $COL_EMAIL TEXT,
                $COL_CATEGORY TEXT NOT NULL,
                $COL_NOTES TEXT,
                $COL_IS_FAVORITE INTEGER NOT NULL DEFAULT 0,
                $COL_AVATAR_COLOR INTEGER NOT NULL DEFAULT 0,
                $COL_CREATED_AT INTEGER NOT NULL
            )
        """.trimIndent()
        db.execSQL(createQuery)

        // Seed default sample contacts so user has immediately usable contacts
        seedInitialContacts(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_CONTACTS")
        onCreate(db)
    }

    private fun seedInitialContacts(db: SQLiteDatabase) {
        val samples = listOf(
            Contact(
                name = "Alice Morgan",
                phone = "555-019-2834",
                secondaryPhone = "555-019-8821",
                email = "alice.morgan@example.com",
                category = "Friends",
                notes = "Met at Tech Meetup 2026",
                isFavorite = true,
                avatarColorIndex = 0
            ),
            Contact(
                name = "David Miller",
                phone = "555-014-9923",
                secondaryPhone = "",
                email = "david.miller@corporate.com",
                category = "Work",
                notes = "Senior Project Architect",
                isFavorite = false,
                avatarColorIndex = 1
            ),
            Contact(
                name = "Emma Watson",
                phone = "555-017-4321",
                secondaryPhone = "",
                email = "emma.watson@gmail.com",
                category = "Family",
                notes = "Sister • Birthday on Nov 14",
                isFavorite = true,
                avatarColorIndex = 2
            ),
            Contact(
                name = "Liam Wilson",
                phone = "555-012-7744",
                secondaryPhone = "555-012-1100",
                email = "liam.w@startup.io",
                category = "Work",
                notes = "Design Team Lead",
                isFavorite = false,
                avatarColorIndex = 3
            ),
            Contact(
                name = "Sophia Davis",
                phone = "555-018-6543",
                secondaryPhone = "",
                email = "sophia.davis@outlook.com",
                category = "Friends",
                notes = "College friend",
                isFavorite = true,
                avatarColorIndex = 4
            ),
            Contact(
                name = "Uncle Robert",
                phone = "555-015-3210",
                secondaryPhone = "",
                email = "",
                category = "Family",
                notes = "Home line: (555) 015-3211",
                isFavorite = false,
                avatarColorIndex = 5
            )
        )

        for (contact in samples) {
            val cv = ContentValues().apply {
                put(COL_NAME, contact.name)
                put(COL_PHONE, contact.phone)
                put(COL_SECONDARY_PHONE, contact.secondaryPhone)
                put(COL_EMAIL, contact.email)
                put(COL_CATEGORY, contact.category)
                put(COL_NOTES, contact.notes)
                put(COL_IS_FAVORITE, if (contact.isFavorite) 1 else 0)
                put(COL_AVATAR_COLOR, contact.avatarColorIndex)
                put(COL_CREATED_AT, contact.createdAt)
            }
            db.insert(TABLE_CONTACTS, null, cv)
        }
    }

    fun insertContact(contact: Contact): Long {
        val db = writableDatabase
        val cv = ContentValues().apply {
            put(COL_NAME, contact.name.trim())
            put(COL_PHONE, contact.phone.trim())
            put(COL_SECONDARY_PHONE, contact.secondaryPhone.trim())
            put(COL_EMAIL, contact.email.trim())
            put(COL_CATEGORY, contact.category.trim())
            put(COL_NOTES, contact.notes.trim())
            put(COL_IS_FAVORITE, if (contact.isFavorite) 1 else 0)
            put(COL_AVATAR_COLOR, contact.avatarColorIndex)
            put(COL_CREATED_AT, System.currentTimeMillis())
        }
        return db.insert(TABLE_CONTACTS, null, cv)
    }

    fun updateContact(contact: Contact): Boolean {
        val db = writableDatabase
        val cv = ContentValues().apply {
            put(COL_NAME, contact.name.trim())
            put(COL_PHONE, contact.phone.trim())
            put(COL_SECONDARY_PHONE, contact.secondaryPhone.trim())
            put(COL_EMAIL, contact.email.trim())
            put(COL_CATEGORY, contact.category.trim())
            put(COL_NOTES, contact.notes.trim())
            put(COL_IS_FAVORITE, if (contact.isFavorite) 1 else 0)
            put(COL_AVATAR_COLOR, contact.avatarColorIndex)
        }
        return db.update(TABLE_CONTACTS, cv, "$COL_ID = ?", arrayOf(contact.id.toString())) > 0
    }

    fun toggleFavorite(id: Long, isFavorite: Boolean): Boolean {
        val db = writableDatabase
        val cv = ContentValues().apply {
            put(COL_IS_FAVORITE, if (isFavorite) 1 else 0)
        }
        return db.update(TABLE_CONTACTS, cv, "$COL_ID = ?", arrayOf(id.toString())) > 0
    }

    fun deleteContact(id: Long): Boolean {
        val db = writableDatabase
        return db.delete(TABLE_CONTACTS, "$COL_ID = ?", arrayOf(id.toString())) > 0
    }

    fun getContactById(id: Long): Contact? {
        val db = readableDatabase
        val cursor = db.query(TABLE_CONTACTS, null, "$COL_ID = ?", arrayOf(id.toString()), null, null, null)
        cursor.use {
            if (it.moveToFirst()) {
                return cursorToContact(it)
            }
        }
        return null
    }

    fun getContactsFiltered(
        query: String = "",
        categoryFilter: String? = null,
        favoritesOnly: Boolean = false
    ): List<Contact> {
        val list = mutableListOf<Contact>()
        val db = readableDatabase

        val whereClauses = mutableListOf<String>()
        val whereArgs = mutableListOf<String>()

        if (favoritesOnly) {
            whereClauses.add("$COL_IS_FAVORITE = 1")
        }

        if (!categoryFilter.isNullOrBlank() && !categoryFilter.equals("All", ignoreCase = true)) {
            whereClauses.add("$COL_CATEGORY = ?")
            whereArgs.add(categoryFilter)
        }

        if (query.isNotBlank()) {
            val q = "%${query.trim()}%"
            whereClauses.add("($COL_NAME LIKE ? OR $COL_PHONE LIKE ? OR $COL_SECONDARY_PHONE LIKE ? OR $COL_EMAIL LIKE ?)")
            whereArgs.add(q)
            whereArgs.add(q)
            whereArgs.add(q)
            whereArgs.add(q)
        }

        val selection = if (whereClauses.isEmpty()) null else whereClauses.joinToString(" AND ")
        val selectionArgs = if (whereArgs.isEmpty()) null else whereArgs.toTypedArray()

        // Order by favorites first, then alphabetical by name
        val orderBy = "$COL_IS_FAVORITE DESC, $COL_NAME COLLATE NOCASE ASC"

        val cursor = db.query(TABLE_CONTACTS, null, selection, selectionArgs, null, null, orderBy)
        cursor.use {
            if (it.moveToFirst()) {
                do {
                    list.add(cursorToContact(it))
                } while (it.moveToNext())
            }
        }
        return list
    }

    fun getContactsCount(): Int {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT COUNT(*) FROM $TABLE_CONTACTS", null)
        cursor.use {
            if (it.moveToFirst()) {
                return it.getInt(0)
            }
        }
        return 0
    }

    private fun cursorToContact(cursor: Cursor): Contact {
        return Contact(
            id = cursor.getLong(cursor.getColumnIndexOrThrow(COL_ID)),
            name = cursor.getString(cursor.getColumnIndexOrThrow(COL_NAME)),
            phone = cursor.getString(cursor.getColumnIndexOrThrow(COL_PHONE)),
            secondaryPhone = cursor.getString(cursor.getColumnIndexOrThrow(COL_SECONDARY_PHONE)) ?: "",
            email = cursor.getString(cursor.getColumnIndexOrThrow(COL_EMAIL)) ?: "",
            category = cursor.getString(cursor.getColumnIndexOrThrow(COL_CATEGORY)),
            notes = cursor.getString(cursor.getColumnIndexOrThrow(COL_NOTES)) ?: "",
            isFavorite = cursor.getInt(cursor.getColumnIndexOrThrow(COL_IS_FAVORITE)) == 1,
            avatarColorIndex = cursor.getInt(cursor.getColumnIndexOrThrow(COL_AVATAR_COLOR)),
            createdAt = cursor.getLong(cursor.getColumnIndexOrThrow(COL_CREATED_AT))
        )
    }
}
