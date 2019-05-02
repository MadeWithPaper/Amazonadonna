package com.amazonadonna.view

import android.app.Activity
import android.os.Bundle
import android.provider.Settings
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.amazonadonna.view.R

import com.google.gson.JsonObject
import com.koushikdutta.async.future.FutureCallback
import com.koushikdutta.ion.Ion
import com.twilio.chat.CallbackListener
import com.twilio.chat.Channel
import com.twilio.chat.ChannelListener
import com.twilio.chat.ChatClient
import com.twilio.chat.ErrorInfo
import com.twilio.chat.Member
import com.twilio.chat.Message
import com.twilio.chat.StatusListener
import kotlinx.android.synthetic.main.activity_message_artisan.*

import java.util.ArrayList

class MessageArtisan : AppCompatActivity() {

    // Update this identity for each individual user, for instance after they login
    private val mIdentity = "CHAT_USER"
    private var mMessagesAdapter: MessagesAdapter? = null
    private val mMessages = ArrayList<Message>()
    private var mChatClient: ChatClient? = null
    private var mGeneralChannel: Channel? = null

    private val mChatClientCallback = object : CallbackListener<ChatClient>() {
        override fun onSuccess(chatClient: ChatClient) {
            mChatClient = chatClient
            loadChannels()
            Log.d(TAG, "Success creating Twilio Chat Client")
        }

        override fun onError(errorInfo: ErrorInfo) {
            Log.e(TAG, "Error creating Twilio Chat Client: " + errorInfo.message)
        }
    }

    private val mDefaultChannelListener = object : ChannelListener {


        override fun onMessageAdded(message: Message) {
            Log.d(TAG, "Message added")
            this@MessageArtisan.runOnUiThread {
                // need to modify user interface elements on the UI thread
                mMessages.add(message)
                mMessagesAdapter!!.notifyDataSetChanged()
            }

        }

        override fun onMessageUpdated(message: Message, updateReason: Message.UpdateReason) {
            Log.d(TAG, "Message updated: " + message.messageBody)
        }

        override fun onMessageDeleted(message: Message) {
            Log.d(TAG, "Message deleted")
        }

        override fun onMemberAdded(member: Member) {
            Log.d(TAG, "Member added: " + member.identity)
        }

        override fun onMemberUpdated(member: Member, updateReason: Member.UpdateReason) {
            Log.d(TAG, "Member updated: " + member.identity)
        }

        override fun onMemberDeleted(member: Member) {
            Log.d(TAG, "Member deleted: " + member.identity)
        }

        override fun onTypingStarted(channel: Channel, member: Member) {
            Log.d(TAG, "Started Typing: " + member.identity)
        }

        override fun onTypingEnded(channel: Channel, member: Member) {
            Log.d(TAG, "Ended Typing: " + member.identity)
        }

        override fun onSynchronizationChanged(channel: Channel) {

        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message_artisan)

        messageArtisanLayout.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(v: View, m: MotionEvent): Boolean {
                hideKeyboard(v)
                return true
            }
        })

        val layoutManager = LinearLayoutManager(this)
        // for a chat app, show latest at the bottom
        layoutManager.stackFromEnd = true
        messagesRecyclerView!!.layoutManager = layoutManager

        mMessagesAdapter = MessagesAdapter()
        messagesRecyclerView!!.adapter = mMessagesAdapter

        sendChatMessageButton!!.setOnClickListener {
            if (mGeneralChannel != null) {
                val messageBody = writeMessageEditText!!.text.toString()
                val options = Message.options().withBody(messageBody)
                Log.d(TAG, "Message created")
                mGeneralChannel!!.messages.sendMessage(options, object : CallbackListener<Message>() {
                    override fun onSuccess(message: Message) {
                        this@MessageArtisan.runOnUiThread {
                            // need to modify user interface elements on the UI thread
                            writeMessageEditText!!.setText("")
                        }
                    }
                })
            }
        }

        retrieveAccessTokenfromServer()
    }

    private fun hideKeyboard(view: View) {
        val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }


    private fun retrieveAccessTokenfromServer() {
        val deviceId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)

        val tokenURL = "$SERVER_TOKEN_URL?device=$deviceId&identity=$mIdentity"

        Ion.with(this)
                .load(tokenURL)
                .asJsonObject()
                .setCallback { e, result ->
                    if (e == null) {
                        val accessToken = result.get("token").asString

                        Log.d(TAG, "Retrieved access token from server: $accessToken")

                        title = mIdentity

                        val builder = ChatClient.Properties.Builder()
                        val props = builder.createProperties()
                        ChatClient.create(this@MessageArtisan, accessToken, props, mChatClientCallback)

                    } else {
                        Log.e(TAG, e.message, e)
//                        Toast.makeText(this@MainActivity,
//                                R.string.error_retrieving_access_token, Toast.LENGTH_SHORT)
//                                .show()
                    }
                }
    }

    private fun loadChannels() {
        mChatClient!!.channels.getChannel(DEFAULT_CHANNEL_NAME, object : CallbackListener<Channel>() {
            override fun onSuccess(channel: Channel?) {
                if (channel != null) {
                    Log.d(TAG, "Joining Channel: $DEFAULT_CHANNEL_NAME")
                    joinChannel(channel)
                } else {
                    Log.d(TAG, "Creating Channel: $DEFAULT_CHANNEL_NAME")

                    mChatClient!!.channels.createChannel(DEFAULT_CHANNEL_NAME,
                            Channel.ChannelType.PUBLIC, object : CallbackListener<Channel>() {
                        override fun onSuccess(channel: Channel?) {
                            if (channel != null) {
                                Log.d(TAG, "Joining Channel: $DEFAULT_CHANNEL_NAME")
                                joinChannel(channel)
                            }
                        }

                        override fun onError(errorInfo: ErrorInfo?) {
                            Log.e(TAG, "Error creating channel: " + errorInfo!!.message)
                        }
                    })
                }
            }

            override fun onError(errorInfo: ErrorInfo?) {
                Log.e(TAG, "Error retrieving channel: " + errorInfo!!.message)
            }

        })

    }

    private fun joinChannel(channel: Channel) {
        Log.d(TAG, "Joining Channel: " + channel.uniqueName)
        channel.join(object : StatusListener() {
            override fun onSuccess() {
                mGeneralChannel = channel
                Log.d(TAG, "Joined default channel")
                mGeneralChannel!!.addListener(mDefaultChannelListener)
            }

            override fun onError(errorInfo: ErrorInfo?) {
                Log.e(TAG, "Error joining channel: " + errorInfo!!.message)
            }
        })
    }


    internal inner class MessagesAdapter : RecyclerView.Adapter<MessagesAdapter.ViewHolder>() {

        internal inner class ViewHolder(var mMessageTextView: TextView) : RecyclerView.ViewHolder(mMessageTextView)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessagesAdapter.ViewHolder {
            val messageTextView = LayoutInflater.from(parent.context)
                    .inflate(R.layout.message_text_view, parent, false) as TextView
            return ViewHolder(messageTextView)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val message = mMessages[position]
            val messageText = String.format("%s: %s", message.author, message.messageBody)
            holder.mMessageTextView.text = messageText

        }

        override fun getItemCount(): Int {
            return mMessages.size
        }
    }

    companion object {
        /*
       Change this URL to match the token URL for your Twilio Function
    */
        internal val SERVER_TOKEN_URL = "https://YOUR_DOMAIN_HERE.twil.io/chat-token"
        internal val DEFAULT_CHANNEL_NAME = "general"
        internal val TAG = "TwilioChat"
    }


}
