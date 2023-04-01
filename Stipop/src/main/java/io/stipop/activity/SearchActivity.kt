@file:Suppress("RedundantOverride")

package io.stipop.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.AbsListView
import androidx.recyclerview.widget.LinearLayoutManager
import io.stipop.*
import io.stipop.adapter.KeywordAdapter
import io.stipop.adapter.StickerAdapter
import io.stipop.extend.RecyclerDecoration
import io.stipop.model.SPSticker
import kotlinx.android.synthetic.main.activity_search_stick.*
import org.json.JSONObject
import java.io.IOException

class SearchActivity: Activity() {

    lateinit var context: Context

    private lateinit var keywordAdapter: KeywordAdapter
    private lateinit var stickerAdapter: StickerAdapter

    var keywords = ArrayList<JSONObject>()
    private var stickerData = ArrayList<SPSticker>()

    private var lastItemVisibleFlag = false

    var page = 1
    var totalPage = 1

    override fun onBackPressed() {
        super.onBackPressed()
    }

    @SuppressLint("ObsoleteSdkInt")
    override fun onCreate(savedInstanceState: Bundle?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_stick)

        this.context = this

        val drawable = cont.background as GradientDrawable
        drawable.setColor(Color.parseColor(Config.themeBackgroundColor))

        val drawable2 = bar.background as GradientDrawable
        drawable2.setColor(Color.parseColor(Config.themeGroupedContentBackgroundColor)) // solid  color
        drawable2.cornerRadius = Utils.dpToPx(Config.searchbarRadius.toFloat())

        searchIV.setImageResource(Config.getSearchbarResourceId(context))
        eraseIV.setImageResource(Config.getEraseResourceId(context))

        titleTV.setTextColor(Config.getSearchTitleTextColor(context))
        keywordET.setTextColor(Config.getSearchTitleTextColor(context))


        searchIV.setIconDefaultsColor()
        eraseIV.setIconDefaultsColor()


        val gd = GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM, intArrayOf(Color.parseColor(Config.themeBackgroundColor), Color.TRANSPARENT)
        )

        shadowV.background = gd

        clearTextLL.setOnClickListener {
            keywordET.setText("")
        }

        keywordET.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun afterTextChanged(p0: Editable?) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                val keyword = Utils.getString(keywordET)

                page = 1
                search(keyword)
            }
        })

        keywordAdapter = KeywordAdapter(keywords)
        keywordAdapter.setOnItemClickListener(object : KeywordAdapter.OnItemClickListener {
            override fun onItemClick(position: Int) {
                if (position > keywords.size) {
                    return
                }

                val item = keywords[position]
                val keyword = Utils.getString(item, "keyword")
                page = 1
                search(keyword)
            }
        })

        stickerAdapter = StickerAdapter(context, R.layout.item_sticker, stickerData)

        val mLayoutManager = LinearLayoutManager(this)
        mLayoutManager.orientation = LinearLayoutManager.HORIZONTAL

        keywordRV.layoutManager = mLayoutManager
        keywordRV.addItemDecoration(RecyclerDecoration(10))
        keywordRV.adapter = keywordAdapter

        stickerGV.numColumns = Config.searchNumOfColumns
        stickerGV.adapter = stickerAdapter
        stickerGV.setOnScrollListener(object : AbsListView.OnScrollListener {
            override fun onScrollStateChanged(absListView: AbsListView?, scrollState: Int) {
                if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE && lastItemVisibleFlag && totalPage > page) {
                    page += 1

                    search(Utils.getString(keywordET))
                }
            }

            override fun onScroll(view: AbsListView?, firstVisibleItem: Int, visibleItemCount: Int, totalItemCount: Int) {
                lastItemVisibleFlag = (totalItemCount > 0) && (firstVisibleItem + visibleItemCount >= totalItemCount)
            }

        })

        stickerGV.setOnItemClickListener { adapterView, view, i, l ->
            val sticker = stickerData[i]

            Stipop.send(sticker.stickerId, sticker.keyword) { result ->
                if (result) {
                    Stipop.instance!!.delegate.onStickerSelected(sticker)

                    finish()
                }
            }
        }

        if (Config.searchTagsHidden) {
            tagLL.visibility = View.GONE
        } else {
            tagLL.visibility = View.VISIBLE

            getKeyword()
        }

        search("")
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun getKeyword() {
        keywords.clear()

        APIClient.get(this, APIClient.APIPath.SEARCH_KEYWORD.rawValue, null) { response: JSONObject?, e: IOException? ->
            // println(response)

            if (null != response) {

                if (!response.isNull("body")) {
                    val body = response.getJSONObject(java.lang.String.valueOf("body"))

                    val keywordList = body.getJSONArray("keywordList")

                    for (i in 0 until keywordList.length()) {
                        keywords.add(keywordList.get(i) as JSONObject)
                    }

                }

            }

            keywordAdapter.notifyDataSetChanged()

        }

    }

    private fun search(keyword: String) {

        val params = JSONObject()
        params.put("userId", Stipop.userId)
        params.put("lang", Stipop.lang)
        params.put("countryCode", Stipop.countryCode)
        params.put("limit", 36)
        params.put("pageNumber", page)
        params.put("q", keyword)

        APIClient.get(this, APIClient.APIPath.SEARCH.rawValue, params) { response: JSONObject?, e: IOException? ->
            // println(response)

            if (null != response) {

                if (!response.isNull("body")) {
                    val body = response.getJSONObject(java.lang.String.valueOf("body"))

                    if (!body.isNull("pageMap")) {
                        val pageMap = body.getJSONObject("pageMap")
                        totalPage = Utils.getInt(pageMap, "pageCount")
                    }

                    if (!body.isNull("stickerList")) {
                        val stickerList = body.getJSONArray("stickerList")

                        if (stickerList.length() < 1) {
                            return@get
                        }

                        if (page == 1) {
                            stickerData.clear()
                        }

                        for (i in 0 until stickerList.length()) {
                            stickerData.add(SPSticker(stickerList.get(i) as JSONObject))
                        }

                    }

                }

            }

            stickerAdapter.notifyDataSetChanged()

        }

    }


}