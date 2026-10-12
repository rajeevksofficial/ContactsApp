package com.contacts.app.util

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.contacts.app.model.Contact

object PhoneCaller {

    fun callPhoneNumber(context: Context, rawPhone: String) {
        val cleanPhone = rawPhone.trim()
        if (cleanPhone.isBlank()) {
            Toast.makeText(context, "No phone number available to call", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val hasCallPermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CALL_PHONE
            ) == PackageManager.PERMISSION_GRANTED

            if (hasCallPermission) {
                val callIntent = Intent(Intent.ACTION_CALL, Uri.parse("tel:$cleanPhone")).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(callIntent)
            } else {
                // Fallback to ACTION_DIAL (opens dialer with number ready, zero permissions required)
                val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$cleanPhone")).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(dialIntent)
            }
        } catch (e: Exception) {
            // Safe fallback to dialer
            try {
                val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$cleanPhone")).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(dialIntent)
            } catch (err: Exception) {
                Toast.makeText(context, "Could not open phone dialer: ${err.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun sendSms(context: Context, rawPhone: String) {
        val cleanPhone = rawPhone.trim()
        if (cleanPhone.isBlank()) {
            Toast.makeText(context, "No phone number available for SMS", Toast.LENGTH_SHORT).show()
            return
        }
        try {
            val smsIntent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:$cleanPhone")).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(smsIntent)
        } catch (e: Exception) {
            Toast.makeText(context, "Could not open messaging app: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    fun sendEmail(context: Context, email: String) {
        val cleanEmail = email.trim()
        if (cleanEmail.isBlank()) return
        try {
            val emailIntent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$cleanEmail")).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(emailIntent)
        } catch (e: Exception) {
            Toast.makeText(context, "Could not open email client: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    fun shareContact(context: Context, contact: Contact) {
        val shareText = buildString {
            append("Contact: ${contact.name}\n")
            append("Phone: ${contact.formattedPhone}\n")
            if (contact.secondaryPhone.isNotBlank()) {
                append("Secondary: ${contact.secondaryPhone}\n")
            }
            if (contact.email.isNotBlank()) {
                append("Email: ${contact.email}\n")
            }
            if (contact.notes.isNotBlank()) {
                append("Notes: ${contact.notes}\n")
            }
        }
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, shareText)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "Share Contact via")
        shareIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(shareIntent)
    }
}
