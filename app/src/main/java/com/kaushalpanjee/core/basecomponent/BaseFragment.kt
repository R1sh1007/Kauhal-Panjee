package com.kaushalpanjee.core.basecomponent

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.viewbinding.ViewBinding
import com.google.android.material.snackbar.Snackbar
import com.kaushalpanjee.R
import com.kaushalpanjee.core.util.AppUtil
import com.kaushalpanjee.core.util.UserPreferences
import com.kaushalpanjee.core.util.log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

abstract class BaseFragment<VB : ViewBinding>(
    private val bindingInflater: (LayoutInflater) -> VB
) : Fragment() {

    // VIEW BINDING
    private var _binding: VB? = null
    protected val binding: VB
        get() = _binding ?: throw IllegalStateException("Accessing binding outside view lifecycle")

    // DEPENDENCIES
    @Inject
    lateinit var userPreferences: UserPreferences

    // PROGRESS DIALOG (SAFE)
    private val progressDialog: AlertDialog? by lazy {
        AppUtil.getProgressDialog(requireContext())
    }

    // LIFECYCLE
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = bindingInflater(inflater)

        // Secure screen for sensitive fragments
        requireActivity().window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null      // avoid memory leak
    }

    // PROGRESS DIALOG
    fun showProgressBar() {
        val dialog = progressDialog ?: return   // if null → exit safely

        if (isAdded && !requireActivity().isFinishing && !dialog.isShowing) {
            dialog.show()
        }
    }


    fun hideProgressBar() {
        val dialog = progressDialog ?: return

        if (dialog.isShowing && isAdded && !requireActivity().isFinishing) {
            dialog.dismiss()
        }
    }


    // SNACKBAR
    fun showSnackBar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).apply {
            view.background = ContextCompat.getDrawable(requireContext(), R.drawable.shape_rectangle_grey)
            view.elevation = 0f
            show()
        }
    }

    // KEYBOARD HELPERS
    fun hideKeyboard() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val windowToken = requireActivity().currentFocus?.windowToken
        imm.hideSoftInputFromWindow(windowToken, 0)
    }

    // FLOW COLLECTOR (Reusable, Clean)
    protected fun <T> collectLatestLifecycleFlow(flow: Flow<T>, action: suspend (T) -> Unit) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                flow.collectLatest(action)
            }
        }
    }

    // IMAGE COMPRESSION UTIL
    fun compressImageFile(file: File): File? {
        if (!file.exists()) return null

        val bitmap = BitmapFactory.decodeFile(file.absolutePath)
        val resizedBitmap = bitmap.resize(maxSize = 1000)
        val compressed = resizedBitmap?.toFile(requireContext())

        if (compressed != null) {
            log("ImageCompression", "Compressed to: ${(compressed.length() / 1024)} KB")
        }

        return compressed
    }

    // EXTENSIONS FOR BITMAP
    private fun Bitmap.resize(maxSize: Int): Bitmap? {
        val ratio = width.toFloat() / height.toFloat()
        val height = maxSize
        val width = (height * ratio).toInt()
        return Bitmap.createScaledBitmap(this, width, height, true)
    }

    private fun Bitmap.toFile(context: Context): File? {
        return try {
            val file = File(context.cacheDir, "${System.currentTimeMillis()}.jpeg")
            val fos = FileOutputStream(file)
            compress(Bitmap.CompressFormat.JPEG, 70, fos)
            fos.flush()
            fos.close()
            file
        } catch (e: Exception) {
            log("ImageError", e.toString())
            null
        }
    }

    fun showUpdateDialog(
        context: Context,
        packageName: String
    ) {
        AlertDialog.Builder(context)
            .setTitle("Update Available")
            .setMessage("A new version is available. Please update to continue.")
            .setPositiveButton("Update") { dialog, _ ->

                openPlayStore(context, packageName)
                dialog.dismiss()

            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(false)
            .show()
    }

    protected fun openPlayStore(context: Context, packageName: String) {
        try {
            val intent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("market://details?id=$packageName")
            )
            intent.setPackage("com.android.vending")
            context.startActivity(intent)

        } catch (e: Exception) {
            val intent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
            )
            context.startActivity(intent)
        }
    }

}

