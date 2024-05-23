package com.bilalkoyun49m.chatapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ChatActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var listView: ListView
    private lateinit var noMessagesText: TextView
    private lateinit var addMessageButton: FloatingActionButton
    private lateinit var adapter: ArrayAdapter<String>
    private val conversations = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chats)

        // Initialize Firebase Auth and Firestore
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        listView = findViewById(R.id.chatsListView)
        noMessagesText = findViewById(R.id.noMessagesText)
        addMessageButton = findViewById(R.id.addMessageButton)

        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, conversations)
        listView.adapter = adapter

        loadConversations()

        listView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            val intent = Intent(this, ChatDetailActivity::class.java)
            intent.putExtra("chatId", conversations[position])
            startActivity(intent)
        }

        addMessageButton.setOnClickListener {
            showAddMessageDialog()
        }
    }

    private fun loadConversations() {
        val currentUser = auth.currentUser?.email ?: return
        db.collection("messages")
            .whereEqualTo("from", currentUser)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    return@addSnapshotListener
                }

                conversations.clear()
                for (doc in snapshots!!) {
                    val to = doc.getString("to")
                    to?.let {
                        if (!conversations.contains(it)) {
                            conversations.add(it)
                        }
                    }
                }
                adapter.notifyDataSetChanged()
                noMessagesText.visibility = if (conversations.isEmpty()) TextView.VISIBLE else TextView.GONE
            }

        db.collection("messages")
            .whereEqualTo("to", currentUser)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    return@addSnapshotListener
                }

                for (doc in snapshots!!) {
                    val from = doc.getString("from")
                    from?.let {
                        if (!conversations.contains(it)) {
                            conversations.add(it)
                        }
                    }
                }
                adapter.notifyDataSetChanged()
                noMessagesText.visibility = if (conversations.isEmpty()) TextView.VISIBLE else TextView.GONE
            }
    }

    private fun showAddMessageDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_message, null)
        val emailEditText = dialogView.findViewById<EditText>(R.id.emailEditText)
        val messageEditText = dialogView.findViewById<EditText>(R.id.messageEditText)

        AlertDialog.Builder(this)
            .setTitle("Send Message")
            .setView(dialogView)
            .setPositiveButton("Send") { _, _ ->
                val email = emailEditText.text.toString()
                val message = messageEditText.text.toString()
                sendMessage(email, message)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun sendMessage(email: String, message: String) {
        val currentUser = auth.currentUser?.email ?: return
        val messageData = hashMapOf(
            "from" to currentUser,
            "to" to email,
            "text" to message,
            "timestamp" to System.currentTimeMillis()
        )
        db.collection("messages")
            .add(messageData)
            .addOnSuccessListener {
                Log.d("ChatActivity", "Message sent successfully")
            }
            .addOnFailureListener { e ->
                Log.e("ChatActivity", "Error sending message", e)
            }
    }
}
