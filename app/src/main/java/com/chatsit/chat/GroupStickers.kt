package com.chatsit.chat

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.chatsit.chat.group.GroupChatActivity
import io.stipop.Stipop
import io.stipop.StipopDelegate
import io.stipop.extend.StipopImageView
import io.stipop.model.SPPackage
import io.stipop.model.SPSticker


class GroupStickers : AppCompatActivity(), StipopDelegate {

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.NormalDayTheme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stickers)

        val stipopIV = findViewById<StipopImageView>(R.id.stipopIV)

        Stipop.connect(this, stipopIV, "1234", "en", "US", this)

        Stipop.showSearch()

    }

    override fun onStickerSelected(sticker: SPSticker): Boolean {

        val i = Intent(this, GroupChatActivity::class.java)
        i.putExtra("group", intent.getStringExtra("id"))
        i.putExtra("uri", sticker.stickerImg.toString())
        startActivity(i)
        finish()

        return true
    }

    override fun canDownload(spPackage: SPPackage): Boolean {

        return true
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val i = Intent(this, GroupChatActivity::class.java)
        i.putExtra("id", intent.getStringExtra("id"))
        startActivity(i)
        finish()
    }

}
