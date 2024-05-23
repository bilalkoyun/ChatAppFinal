package com.bilalkoyun49m.chatapp

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.bilalkoyun49m.chatapp.adapters.MessageAdapter
import com.bilalkoyun49m.chatapp.models.Message

class ChatDetailActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var recyclerView: RecyclerView
    private lateinit var messageInput: EditText
    private lateinit var sendButton: Button
    private lateinit var adapter: MessageAdapter
    private val messages = mutableListOf<Message>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_detail)

        // Firebase Auth ve Firestore'u başlat
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val chatTitle = findViewById<TextView>(R.id.chatTitle)
        recyclerView = findViewById(R.id.messagesListView)
        messageInput = findViewById(R.id.messageInput)
        sendButton = findViewById(R.id.sendButton)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        adapter = MessageAdapter(this, messages)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        val chatId = intent.getStringExtra("chatId")
        if (chatId != null) {
            chatTitle.text = "Chat with $chatId"
            loadMessages(chatId)
        } else {
            chatTitle.text = "Chat with Unknown"
        }

        sendButton.setOnClickListener {
            val message = messageInput.text.toString()
            if (message.isNotEmpty()) {
                sendMessage(chatId, message)
                messageInput.text.clear()
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun loadMessages(chatId: String) {
        val currentUser = auth.currentUser?.email ?: return

        // Kullanıcının aldığı mesajları yükle
        db.collection("messages")
            .whereEqualTo("to", currentUser)
            .whereEqualTo("from", chatId)
            .orderBy("timestamp")
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.e("ChatDetailActivity", "Mesajlar yüklenirken hata oluştu", e)
                    return@addSnapshotListener
                }

                messages.clear()
                for (doc in snapshots!!) {
                    val message = doc.getString("text")
                    val from = doc.getString("from")
                    message?.let { messages.add(Message(it, from == currentUser)) }
                }
                adapter.notifyDataSetChanged()
                recyclerView.scrollToPosition(messages.size - 1)
            }

        // Kullanıcının gönderdiği mesajları yükle
        db.collection("messages")
            .whereEqualTo("to", chatId)
            .whereEqualTo("from", currentUser)
            .orderBy("timestamp")
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.e("ChatDetailActivity", "Mesajlar yüklenirken hata oluştu", e)
                    return@addSnapshotListener
                }

                for (doc in snapshots!!) {
                    val message = doc.getString("text")
                    val from = doc.getString("from")
                    message?.let { messages.add(Message(it, from == currentUser)) }
                }
                adapter.notifyDataSetChanged()
                recyclerView.scrollToPosition(messages.size - 1)
            }
    }

    private fun sendMessage(chatId: String?, message: String) {
        val currentUser = auth.currentUser?.email ?: return
        if (chatId == null) return

        val messageData = hashMapOf(
            "from" to currentUser,
            "to" to chatId,
            "text" to message,
            "timestamp" to System.currentTimeMillis()
        )
        db.collection("messages")
            .add(messageData)
            .addOnSuccessListener {
                Log.d("ChatDetailActivity", "Mesaj başarıyla gönderildi")
            }
            .addOnFailureListener { e ->
                Log.e("ChatDetailActivity", "Mesaj gönderilirken hata oluştu", e)
            }
    }
}
