package com.contacts.app

import android.Manifest
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.contacts.app.adapter.ContactAdapter
import com.contacts.app.data.ContactDatabaseHelper
import com.contacts.app.databinding.ActivityMainBinding
import com.contacts.app.databinding.DialogAddEditContactBinding
import com.contacts.app.databinding.DialogContactDetailsBinding
import com.contacts.app.databinding.DialogQuickDialerBinding
import com.contacts.app.model.Contact
import com.contacts.app.util.PhoneCaller
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlin.math.abs

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var dbHelper: ContactDatabaseHelper
    private lateinit var contactAdapter: ContactAdapter

    // Filter states
    private var currentFilterType: FilterType = FilterType.ALL
    private var currentSearchQuery: String = ""

    private enum class FilterType {
        ALL, FAVORITES, FAMILY, FRIENDS, WORK
    }

    private val avatarColors = intArrayOf(
        R.color.avatar_blue,
        R.color.avatar_purple,
        R.color.avatar_pink,
        R.color.avatar_emerald,
        R.color.avatar_amber,
        R.color.avatar_cyan,
        R.color.avatar_rose,
        R.color.avatar_indigo
    )

    companion object {
        private const val PERMISSION_REQUEST_CALL = 101
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = ContactDatabaseHelper(this)

        setupRecyclerView()
        setupSearch()
        setupFilterChips()
        setupActionButtons()

        // Check Call permission on start
        requestCallPermissionIfMissing()

        loadContacts()
    }

    private fun requestCallPermissionIfMissing() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CALL_PHONE),
                PERMISSION_REQUEST_CALL
            )
        }
    }

    private fun setupRecyclerView() {
        contactAdapter = ContactAdapter(
            onItemClick = { contact -> showContactDetailsDialog(contact) },
            onCallClick = { contact -> PhoneCaller.callPhoneNumber(this, contact.phone) },
            onSmsClick = { contact -> PhoneCaller.sendSms(this, contact.phone) },
            onFavoriteClick = { contact ->
                val newStatus = !contact.isFavorite
                dbHelper.toggleFavorite(contact.id, newStatus)
                loadContacts()
                val msg = if (newStatus) "${contact.name} added to favorites" else "${contact.name} removed from favorites"
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
            }
        )

        binding.rvContacts.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = contactAdapter
        }
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                currentSearchQuery = s?.toString()?.trim() ?: ""
                binding.btnClearSearch.visibility = if (currentSearchQuery.isNotEmpty()) View.VISIBLE else View.GONE
                loadContacts()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.btnClearSearch.setOnClickListener {
            binding.etSearch.text?.clear()
        }
    }

    private fun setupFilterChips() {
        binding.chipAll.setOnClickListener { selectFilter(FilterType.ALL) }
        binding.chipFavorites.setOnClickListener { selectFilter(FilterType.FAVORITES) }
        binding.chipFamily.setOnClickListener { selectFilter(FilterType.FAMILY) }
        binding.chipFriends.setOnClickListener { selectFilter(FilterType.FRIENDS) }
        binding.chipWork.setOnClickListener { selectFilter(FilterType.WORK) }
    }

    private fun selectFilter(filter: FilterType) {
        currentFilterType = filter
        updateChipStyles()
        loadContacts()
    }

    private fun updateChipStyles() {
        val chips = listOf(
            binding.chipAll to (currentFilterType == FilterType.ALL),
            binding.chipFavorites to (currentFilterType == FilterType.FAVORITES),
            binding.chipFamily to (currentFilterType == FilterType.FAMILY),
            binding.chipFriends to (currentFilterType == FilterType.FRIENDS),
            binding.chipWork to (currentFilterType == FilterType.WORK)
        )

        for ((chip, isSelected) in chips) {
            if (isSelected) {
                chip.setBackgroundResource(R.drawable.bg_chip_selected)
                chip.setTextColor(ContextCompat.getColor(this, R.color.white))
            } else {
                chip.setBackgroundResource(R.drawable.bg_chip_unselected)
                val textColor = if (chip == binding.chipFavorites) {
                    R.color.favorite_gold_light
                } else {
                    R.color.text_secondary
                }
                chip.setTextColor(ContextCompat.getColor(this, textColor))
            }
        }
    }

    private fun setupActionButtons() {
        binding.fabAddContact.setOnClickListener {
            showAddEditContactDialog(null)
        }

        binding.btnDialpad.setOnClickListener {
            showQuickDialerDialog()
        }
    }

    private fun loadContacts() {
        val categoryFilter: String? = when (currentFilterType) {
            FilterType.FAMILY -> "Family"
            FilterType.FRIENDS -> "Friends"
            FilterType.WORK -> "Work"
            else -> null
        }
        val favoritesOnly = currentFilterType == FilterType.FAVORITES

        val contacts = dbHelper.getContactsFiltered(
            query = currentSearchQuery,
            categoryFilter = categoryFilter,
            favoritesOnly = favoritesOnly
        )

        contactAdapter.submitList(contacts)

        val totalCount = dbHelper.getContactsCount()
        binding.tvContactCount.text = getString(R.string.contacts_count, totalCount)

        if (contacts.isEmpty()) {
            binding.rvContacts.visibility = View.GONE
            binding.layoutEmpty.visibility = View.VISIBLE
        } else {
            binding.rvContacts.visibility = View.VISIBLE
            binding.layoutEmpty.visibility = View.GONE
        }
    }

    // -------------------------------------------------------------
    // Contact Details Modal Dialog
    // -------------------------------------------------------------
    private fun showContactDetailsDialog(contact: Contact) {
        val dialogBinding = DialogContactDetailsBinding.inflate(layoutInflater)
        val dialog = MaterialAlertDialogBuilder(this)
            .setView(dialogBinding.root)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        // Populate avatar
        dialogBinding.dialogTvAvatarInitials.text = contact.initials
        val colorIndex = abs(contact.name.hashCode()) % avatarColors.size
        dialogBinding.dialogAvatar.backgroundTintList = ColorStateList.valueOf(
            ContextCompat.getColor(this, avatarColors[colorIndex])
        )

        // Name & Category
        dialogBinding.dialogTvName.text = contact.name
        dialogBinding.dialogTvCategory.text = contact.category
        dialogBinding.dialogIvFavorite.visibility = if (contact.isFavorite) View.VISIBLE else View.GONE

        // Phones
        dialogBinding.dialogTvPhone.text = contact.formattedPhone
        if (contact.secondaryPhone.isNotBlank()) {
            dialogBinding.layoutSecondaryPhone.visibility = View.VISIBLE
            dialogBinding.dialogTvSecondaryPhone.text = Contact.formatPhoneNumber(contact.secondaryPhone)
            dialogBinding.layoutSecondaryPhone.setOnClickListener {
                PhoneCaller.callPhoneNumber(this, contact.secondaryPhone)
            }
        } else {
            dialogBinding.layoutSecondaryPhone.visibility = View.GONE
        }

        // Email
        if (contact.email.isNotBlank()) {
            dialogBinding.layoutEmail.visibility = View.VISIBLE
            dialogBinding.dialogTvEmail.text = contact.email
            dialogBinding.layoutEmail.setOnClickListener {
                PhoneCaller.sendEmail(this, contact.email)
            }
        } else {
            dialogBinding.layoutEmail.visibility = View.GONE
        }

        // Notes
        if (contact.notes.isNotBlank()) {
            dialogBinding.layoutNotes.visibility = View.VISIBLE
            dialogBinding.dialogTvNotes.text = contact.notes
        } else {
            dialogBinding.layoutNotes.visibility = View.GONE
        }

        // Call button
        dialogBinding.btnDialogCall.setOnClickListener {
            PhoneCaller.callPhoneNumber(this, contact.phone)
        }

        // SMS button
        dialogBinding.btnDialogSms.setOnClickListener {
            PhoneCaller.sendSms(this, contact.phone)
        }

        // Share button
        dialogBinding.btnDialogShare.setOnClickListener {
            PhoneCaller.shareContact(this, contact)
        }

        // Edit button
        dialogBinding.btnDialogEdit.setOnClickListener {
            dialog.dismiss()
            showAddEditContactDialog(contact)
        }

        // Delete button
        dialogBinding.btnDialogDelete.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Delete Contact")
                .setMessage("Are you sure you want to delete ${contact.name}?")
                .setPositiveButton("Delete") { _, _ ->
                    dbHelper.deleteContact(contact.id)
                    loadContacts()
                    dialog.dismiss()
                    Toast.makeText(this, "${contact.name} deleted", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        dialogBinding.btnDialogClose.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    // -------------------------------------------------------------
    // Add / Edit Contact Modal Dialog
    // -------------------------------------------------------------
    private fun showAddEditContactDialog(existingContact: Contact?, prefilledPhone: String? = null) {
        val formBinding = DialogAddEditContactBinding.inflate(layoutInflater)
        val dialog = MaterialAlertDialogBuilder(this)
            .setView(formBinding.root)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val isEditMode = existingContact != null
        if (isEditMode) {
            formBinding.dialogFormTitle.text = getString(R.string.title_edit_contact)
            formBinding.etFormName.setText(existingContact!!.name)
            formBinding.etFormPhone.setText(existingContact.phone)
            formBinding.etFormSecondaryPhone.setText(existingContact.secondaryPhone)
            formBinding.etFormEmail.setText(existingContact.email)
            formBinding.etFormNotes.setText(existingContact.notes)
            formBinding.cbFormFavorite.isChecked = existingContact.isFavorite

            when (existingContact.category) {
                "Family" -> formBinding.rbFamily.isChecked = true
                "Friends" -> formBinding.rbFriends.isChecked = true
                "Work" -> formBinding.rbWork.isChecked = true
                else -> formBinding.rbOther.isChecked = true
            }
        } else {
            formBinding.dialogFormTitle.text = getString(R.string.title_add_contact)
            if (!prefilledPhone.isNullOrBlank()) {
                formBinding.etFormPhone.setText(prefilledPhone)
            }
            formBinding.rbFriends.isChecked = true
        }

        formBinding.btnFormCancel.setOnClickListener {
            dialog.dismiss()
        }

        formBinding.btnFormSave.setOnClickListener {
            val name = formBinding.etFormName.text.toString().trim()
            val phone = formBinding.etFormPhone.text.toString().trim()
            val secondaryPhone = formBinding.etFormSecondaryPhone.text.toString().trim()
            val email = formBinding.etFormEmail.text.toString().trim()
            val notes = formBinding.etFormNotes.text.toString().trim()
            val isFavorite = formBinding.cbFormFavorite.isChecked

            val category = when (formBinding.rgFormCategory.checkedRadioButtonId) {
                R.id.rbFamily -> "Family"
                R.id.rbFriends -> "Friends"
                R.id.rbWork -> "Work"
                else -> "Other"
            }

            if (name.isBlank()) {
                formBinding.etFormName.error = "Name is required"
                formBinding.etFormName.requestFocus()
                return@setOnClickListener
            }

            if (phone.isBlank()) {
                formBinding.etFormPhone.error = "Phone number is required"
                formBinding.etFormPhone.requestFocus()
                return@setOnClickListener
            }

            if (isEditMode) {
                val updated = existingContact!!.copy(
                    name = name,
                    phone = phone,
                    secondaryPhone = secondaryPhone,
                    email = email,
                    category = category,
                    notes = notes,
                    isFavorite = isFavorite
                )
                dbHelper.updateContact(updated)
                Toast.makeText(this, "Contact updated", Toast.LENGTH_SHORT).show()
            } else {
                val newContact = Contact(
                    name = name,
                    phone = phone,
                    secondaryPhone = secondaryPhone,
                    email = email,
                    category = category,
                    notes = notes,
                    isFavorite = isFavorite
                )
                dbHelper.insertContact(newContact)
                Toast.makeText(this, "Contact saved", Toast.LENGTH_SHORT).show()
            }

            loadContacts()
            dialog.dismiss()
        }

        dialog.show()
    }

    // -------------------------------------------------------------
    // Quick Dialpad Dialog
    // -------------------------------------------------------------
    private fun showQuickDialerDialog() {
        val dialerBinding = DialogQuickDialerBinding.inflate(layoutInflater)
        val dialog = MaterialAlertDialogBuilder(this)
            .setView(dialerBinding.root)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val digitsBuilder = StringBuilder()

        val updateDisplay = {
            dialerBinding.tvDialerDisplay.text = digitsBuilder.toString()
            dialerBinding.btnDialerBackspace.visibility =
                if (digitsBuilder.isNotEmpty()) View.VISIBLE else View.INVISIBLE
        }
        updateDisplay()

        val appendDigit = { digit: String ->
            digitsBuilder.append(digit)
            updateDisplay()
        }

        // Key bindings
        dialerBinding.key1.setOnClickListener { appendDigit("1") }
        dialerBinding.key2.setOnClickListener { appendDigit("2") }
        dialerBinding.key3.setOnClickListener { appendDigit("3") }
        dialerBinding.key4.setOnClickListener { appendDigit("4") }
        dialerBinding.key5.setOnClickListener { appendDigit("5") }
        dialerBinding.key6.setOnClickListener { appendDigit("6") }
        dialerBinding.key7.setOnClickListener { appendDigit("7") }
        dialerBinding.key8.setOnClickListener { appendDigit("8") }
        dialerBinding.key9.setOnClickListener { appendDigit("9") }
        dialerBinding.keyStar.setOnClickListener { appendDigit("*") }
        dialerBinding.keyHash.setOnClickListener { appendDigit("#") }

        dialerBinding.key0.setOnClickListener { appendDigit("0") }
        dialerBinding.key0.setOnLongClickListener {
            appendDigit("+")
            true
        }

        dialerBinding.btnDialerBackspace.setOnClickListener {
            if (digitsBuilder.isNotEmpty()) {
                digitsBuilder.deleteCharAt(digitsBuilder.length - 1)
                updateDisplay()
            }
        }
        dialerBinding.btnDialerBackspace.setOnLongClickListener {
            digitsBuilder.clear()
            updateDisplay()
            true
        }

        // Action: Call
        dialerBinding.btnDialerCall.setOnClickListener {
            val number = digitsBuilder.toString().trim()
            if (number.isNotEmpty()) {
                PhoneCaller.callPhoneNumber(this, number)
            } else {
                Toast.makeText(this, "Please enter a phone number", Toast.LENGTH_SHORT).show()
            }
        }

        // Action: Add to Contacts
        dialerBinding.btnDialerAddToContacts.setOnClickListener {
            val number = digitsBuilder.toString().trim()
            dialog.dismiss()
            showAddEditContactDialog(null, prefilledPhone = number)
        }

        dialerBinding.btnDialerClose.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }
}
