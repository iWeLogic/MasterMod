package com.iwelogic.minecraft.mods.ui.detail_skin

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.iwelogic.minecraft.mods.R
import com.iwelogic.minecraft.mods.databinding.FragmentDetailsSkinBinding
import com.iwelogic.minecraft.mods.ui.base.storage.StorageFragment
import com.iwelogic.minecraft.mods.ui.details.DetailsFragmentDirections
import com.iwelogic.minecraft.mods.utils.AddHelper
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DetailsSkinFragment : StorageFragment<DetailsSkinViewModel>() {

    var adView: NativeAdView? = null
    var currentNativeAd: NativeAd? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val binding: FragmentDetailsSkinBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_details_skin, container, false)
        binding.lifecycleOwner = this
        viewModel = ViewModelProvider(this).get(DetailsSkinViewModel::class.java)
        viewModel.item.value = DetailsSkinFragmentArgs.fromBundle(requireArguments()).data
        Log.w("myLog", "onCreateView: " + viewModel.item.value)
        viewModel.checkIsFileExist()
        binding.viewModel = viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        refreshAd(view)
        viewModel.openHelp.observe(viewLifecycleOwner) {
            if (findNavController().currentDestination?.id == R.id.detailsFragment) {
                findNavController().navigate(DetailsSkinFragmentDirections.actionDetailsSkinFragmentToHelpFragment())
            }
        }
    }

    private fun refreshAd(view: View) {
        adView?.parent?.let {
            (it as ViewGroup).removeAllViews()
            view.findViewById<FrameLayout>(R.id.ad_frame)?.addView(adView)
        } ?: run {
            context?.let {
                val builder = AdLoader.Builder(it, it.getString(R.string.ad_native))

                builder.forNativeAd { nativeAd ->
                    activity?.let {
                        if (requireActivity().isDestroyed || requireActivity().isFinishing || requireActivity().isChangingConfigurations) {
                            nativeAd.destroy()
                            return@forNativeAd
                        }
                        currentNativeAd?.destroy()
                        currentNativeAd = nativeAd
                        if (adView == null) {
                            adView = layoutInflater.inflate(R.layout.layout_native_ad, view as FrameLayout, false) as NativeAdView
                            nativeAd.images.firstOrNull()?.let {
                                adView?.findViewById<ImageView>(R.id.imageAd)?.setImageDrawable(it.drawable)
                            }
                        }
                        AddHelper.populateUnifiedNativeAdView(nativeAd, adView!!)
                        adView?.parent?.let { parent ->
                            (parent as ViewGroup).removeAllViews()
                        }
                        view.findViewById<FrameLayout>(R.id.ad_frame)?.addView(adView)
                    }
                }

                val adLoader = builder.withAdListener(object : AdListener() {
                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {

                    }
                }).build()

                adLoader.loadAd(AdRequest.Builder().build())
            }
        }
    }

    override fun onDestroy() {
        currentNativeAd?.destroy()
        super.onDestroy()
    }
}
