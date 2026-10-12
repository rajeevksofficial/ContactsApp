package com.contacts.app.adapter

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.contacts.app.R
import com.contacts.app.databinding.ItemContactBinding
import com.contacts.app.model.Contact
import kotlin.math.abs

class ContactAdapter(
    private val onItemClick: (Contact) -> Unit,
    private val onCallClick: (Contact) -> Unit,
    private val onSmsClick: (Contact) -> Unit,
    private val onFavoriteClick: (Contact) -> Unit
) : RecyclerView.Adapter<ContactAdapter.ContactViewHolder>() {

    private val contacts = mutableListOf<Contact>()

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

    fun submitList(newList: List<Contact>) {
        val diffCallback = object : DiffUtil.Callback() {
            override fun getOldListSize(): Int = contacts.size
            override fun getNewListSize(): Int = newList.size

            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return contacts[oldItemPosition].id == newList[newItemPosition].id
            }

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return contacts[oldItemPosition] == newList[newItemPosition]
            }
        }
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        contacts.clear()
        contacts.addAll(newList)
        diffResult.dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val binding = ItemContactBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ContactViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        holder.bind(contacts[position])
    }

    override fun getItemCount(): Int = contacts.size

    inner class ContactViewHolder(
        private val binding: ItemContactBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(contact: Contact) {
            val context = itemView.context

            binding.tvContactName.text = contact.name
            binding.tvContactPhone.text = contact.formattedPhone
            binding.tvAvatarInitials.text = contact.initials

            // Avatar background color deterministic based on name
            val colorIndex = abs(contact.name.hashCode()) % avatarColors.size
            val avatarColor = ContextCompat.getColor(context, avatarColors[colorIndex])
            binding.layoutAvatar.backgroundTintList = ColorStateList.valueOf(avatarColor)

            // Category badge
            if (contact.category.isNotBlank()) {
                binding.tvCategoryBadge.visibility = View.VISIBLE
                binding.tvCategoryBadge.text = contact.category
            } else {
                binding.tvCategoryBadge.visibility = View.GONE
            }

            // Favorite star icon & tint
            if (contact.isFavorite) {
                binding.btnItemFavorite.setImageResource(R.drawable.ic_star)
                binding.btnItemFavorite.imageTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(context, R.color.favorite_gold)
                )
            } else {
                binding.btnItemFavorite.setImageResource(R.drawable.ic_star_border)
                binding.btnItemFavorite.imageTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(context, R.color.text_muted)
                )
            }

            // Click listeners
            binding.root.setOnClickListener { onItemClick(contact) }
            binding.btnItemCall.setOnClickListener { onCallClick(contact) }
            binding.btnItemSms.setOnClickListener { onSmsClick(contact) }
            binding.btnItemFavorite.setOnClickListener { onFavoriteClick(contact) }
        }
    }
}
