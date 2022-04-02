package com.iwelogic.minecraft.mods.bind

import android.text.method.LinkMovementMethod
import android.util.TypedValue
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.text.HtmlCompat
import androidx.databinding.BindingAdapter
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.resource.bitmap.*
import com.iwelogic.minecraft.mods.R
import com.iwelogic.minecraft.mods.utils.dp


object Base {

    @BindingAdapter("queryTextListener")
    @JvmStatic
    fun setOnQueryTextListener(searchView: SearchView, listener: SearchView.OnQueryTextListener) {
        searchView.setOnQueryTextListener(listener)
    }

    @BindingAdapter("query")
    @JvmStatic
    fun setOnQuery(searchView: SearchView, query: String) {
        searchView.setQuery(query, false)
        val txtSearch = searchView.findViewById(androidx.appcompat.R.id.search_src_text) as EditText
        txtSearch.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
        txtSearch.setTextColor(ContextCompat.getColor(searchView.context, R.color.titleText))
        txtSearch.setHintTextColor(ContextCompat.getColor(searchView.context, R.color.hintText))
        txtSearch.typeface = ResourcesCompat.getFont(searchView.context, R.font.minecraft_regular)
        val searchClose = searchView.findViewById(androidx.appcompat.R.id.search_close_btn) as ImageView
        searchClose.setImageResource(R.drawable.clear)
    }

    @BindingAdapter("image")
    @JvmStatic
    fun setImage(view: ImageView, image: String?) {
        val circularProgressDrawable = CircularProgressDrawable(view.context)
        circularProgressDrawable.strokeWidth = 6.dp(view.context).toFloat()
        circularProgressDrawable.centerRadius = 24.dp(view.context).toFloat()
        circularProgressDrawable.setColorSchemeColors(ContextCompat.getColor(view.context, R.color.titleText))
        circularProgressDrawable.start()
        val radius = (view.tag ?: "0").toString().toInt()
        image?.let {
            Glide.with(view.context).load(image).transform(
                when (view.scaleType) {
                    ImageView.ScaleType.CENTER_CROP -> if (radius == 0) CenterCrop() else MultiTransformation(CenterCrop(), RoundedCorners(radius.dp(view.context)))
                    ImageView.ScaleType.CENTER_INSIDE -> if (radius == 0) CenterInside() else MultiTransformation(CenterInside(), RoundedCorners(radius.dp(view.context)))
                    ImageView.ScaleType.FIT_CENTER -> if (radius == 0) FitCenter() else MultiTransformation(FitCenter(), RoundedCorners(radius.dp(view.context)))
                    else -> CircleCrop()
                }
            ).placeholder(circularProgressDrawable).into(view)
        }
    }

    @BindingAdapter("image")
    @JvmStatic
    fun setImage(view: ImageView, image: Int?) {
        val radius = (view.tag ?: "0").toString().toInt()
        image?.let {
            Glide.with(view.context).load(image).transform(
                when (view.scaleType) {
                    ImageView.ScaleType.CENTER_CROP -> if (radius == 0) CenterCrop() else MultiTransformation(CenterCrop(), RoundedCorners(radius.dp(view.context)))
                    ImageView.ScaleType.CENTER_INSIDE -> if (radius == 0) CenterInside() else MultiTransformation(CenterInside(), RoundedCorners(radius.dp(view.context)))
                    ImageView.ScaleType.FIT_CENTER -> if (radius == 0) FitCenter() else MultiTransformation(FitCenter(), RoundedCorners(radius.dp(view.context)))
                    else -> CircleCrop()
                }
            ).into(view)
        }
    }

    @BindingAdapter("html")
    @JvmStatic
    fun setHtml(view: TextView, html: String?) {
        view.text = HtmlCompat.fromHtml(html ?: "", HtmlCompat.FROM_HTML_MODE_LEGACY)
        view.movementMethod = LinkMovementMethod.getInstance()
    }
}