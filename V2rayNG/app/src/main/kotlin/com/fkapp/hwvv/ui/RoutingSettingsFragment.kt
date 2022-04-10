package com.fkapp.hwvv.ui

import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import android.view.*
import android.view.MenuInflater
import androidx.activity.result.contract.ActivityResultContracts
import com.tbruyelle.rxpermissions.RxPermissions
import com.fkapp.hwvv.AppConfig
import com.fkapp.hwvv.R
import com.fkapp.hwvv.databinding.FragmentRoutingSettingsBinding
import com.fkapp.hwvv.extension.toast
import com.fkapp.hwvv.extension.v2RayApplication
import com.fkapp.hwvv.util.Utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.net.URL

class RoutingSettingsFragment : Fragment() {
    private lateinit var binding: FragmentRoutingSettingsBinding
    companion object {
        private const val routing_arg = "routing_arg"
    }

    val defaultSharedPreferences by lazy { PreferenceManager.getDefaultSharedPreferences(requireContext()) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        binding = FragmentRoutingSettingsBinding.inflate(layoutInflater)
        return binding.root// inflater.inflate(R.layout.fragment_routing_settings, container, false)
    }

    fun newInstance(arg: String): Fragment {
        val fragment = RoutingSettingsFragment()
        val bundle = Bundle()
        bundle.putString(routing_arg, arg)
        fragment.arguments = bundle
        return fragment
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val content = defaultSharedPreferences.getString(requireArguments().getString(routing_arg), "")
        binding.etRoutingContent.text = Utils.getEditable(content!!)

        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_routing, menu)
        return super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.save_routing -> {
            val content = binding.etRoutingContent.text.toString()
            defaultSharedPreferences.edit().putString(requireArguments().getString(routing_arg), content).apply()
            activity?.toast(R.string.toast_success)
            true
        }
        R.id.del_routing -> {
            binding.etRoutingContent.text = null
            true
        }
        R.id.scan_replace -> {
            scanQRcode(true)
            true
        }
        R.id.scan_append -> {
            scanQRcode(false)
            true
        }
        R.id.default_rules -> {
            setDefaultRules()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    private fun saveRouting() {
        val content = binding.etRoutingContent.text.toString()
        defaultSharedPreferences.edit().putString(requireArguments().getString(routing_arg), content).apply()
        activity?.toast(R.string.toast_success)
    }

    fun scanQRcode(forReplace: Boolean): Boolean {
//        try {
//            startActivityForResult(Intent("com.google.zxing.client.android.SCAN")
//                    .addCategory(Intent.CATEGORY_DEFAULT)
//                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP), requestCode)
//        } catch (e: Exception) {
        RxPermissions(requireActivity())
                .request(Manifest.permission.CAMERA)
                .subscribe {
                    if (it)
                        if (forReplace)
                            scanQRCodeForReplace.launch(Intent(activity, ScannerActivity::class.java))
                        else
                            scanQRCodeForAppend.launch(Intent(activity, ScannerActivity::class.java))
                    else
                        activity?.toast(R.string.toast_permission_denied)
                }
//        }
        return true
    }

    private val scanQRCodeForReplace = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == RESULT_OK) {
            val content = it.data?.getStringExtra("SCAN_RESULT")
            binding.etRoutingContent.text = Utils.getEditable(content!!)
        }
    }

    private val scanQRCodeForAppend = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == RESULT_OK) {
            val content = it.data?.getStringExtra("SCAN_RESULT")
            binding.etRoutingContent.text = Utils.getEditable("${binding.etRoutingContent.text},$content")
        }
    }

    fun setDefaultRules(): Boolean {
        var url = AppConfig.v2rayCustomRoutingListUrl
        when (requireArguments().getString(routing_arg)) {
            AppConfig.PREF_V2RAY_ROUTING_AGENT -> {
                url += AppConfig.TAG_AGENT
            }
            AppConfig.PREF_V2RAY_ROUTING_DIRECT -> {
                url += AppConfig.TAG_DIRECT
            }
            AppConfig.PREF_V2RAY_ROUTING_BLOCKED -> {
                url += AppConfig.TAG_BLOCKED
            }
        }

        activity?.toast(R.string.msg_downloading_content)
        GlobalScope.launch(Dispatchers.IO) {
            val content = try {
                URL(url).readText()
            } catch (e: Exception) {
                e.printStackTrace()
                ""
            }
            launch(Dispatchers.Main) {
                val routingList = if (TextUtils.isEmpty(content)) {
                    Utils.readTextFromAssets(activity?.v2RayApplication!!, "custom_routing_$tag")
                } else {
                    content
                }
                binding.etRoutingContent.text = Utils.getEditable(routingList)
                saveRouting()
                //toast(R.string.toast_success)
            }
        }
        return true
    }
}
