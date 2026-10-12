package com.contacts.app

import com.contacts.app.model.Contact
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ContactsUnitTest {

    @Test
    fun testInitialsGeneration_twoWords() {
        val contact = Contact(name = "Alice Morgan", phone = "555-019-2834")
        assertEquals("AM", contact.initials)
    }

    @Test
    fun testInitialsGeneration_singleWord() {
        val contact = Contact(name = "David", phone = "555-014-9923")
        assertEquals("DA", contact.initials)
    }

    @Test
    fun testInitialsGeneration_multipleWords() {
        val contact = Contact(name = "Robert De Niro", phone = "555-015-3210")
        assertEquals("RD", contact.initials)
    }

    @Test
    fun testInitialsGeneration_emptyName() {
        val contact = Contact(name = "", phone = "555-000-0000")
        assertEquals("?", contact.initials)
    }

    @Test
    fun testInitialsGeneration_withExtraSpaces() {
        val contact = Contact(name = "   Emma    Watson   ", phone = "555-017-4321")
        assertEquals("EW", contact.initials)
    }

    @Test
    fun testPhoneNumberFormatting_standardTenDigits() {
        val formatted = Contact.formatPhoneNumber("5550192834")
        assertEquals("(555) 019-2834", formatted)
    }

    @Test
    fun testPhoneNumberFormatting_withHyphens() {
        val formatted = Contact.formatPhoneNumber("555-019-2834")
        assertEquals("(555) 019-2834", formatted)
    }

    @Test
    fun testPhoneNumberFormatting_international() {
        val formatted = Contact.formatPhoneNumber("+15550192834")
        assertEquals("+15550192834", formatted)
    }

    @Test
    fun testPhoneNumberFormatting_shortNumber() {
        val formatted = Contact.formatPhoneNumber("911")
        assertEquals("911", formatted)
    }

    @Test
    fun testContactCopy_toggleFavorite() {
        val contact = Contact(
            id = 1L,
            name = "Liam Wilson",
            phone = "555-012-7744",
            category = "Work",
            isFavorite = false
        )
        val updated = contact.copy(isFavorite = true)
        assertFalse(contact.isFavorite)
        assertTrue(updated.isFavorite)
        assertEquals(contact.id, updated.id)
        assertEquals(contact.name, updated.name)
    }

    @Test
    fun testContactDefaults() {
        val contact = Contact(name = "Test User", phone = "1234567890")
        assertEquals("Friends", contact.category)
        assertFalse(contact.isFavorite)
        assertEquals("", contact.secondaryPhone)
        assertEquals("", contact.email)
        assertEquals("", contact.notes)
    }
}
