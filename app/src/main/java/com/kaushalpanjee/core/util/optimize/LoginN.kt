//package com.kaushalpanjee.core.util.optimize
//
//@AndroidEntryPoint
//class LoginFragment :
//    BaseFragment<FragmentLoginBinding>(FragmentLoginBinding::inflate) {
//
//    private val vm: CommonViewModel by activityViewModels()
//
//    private var showPassword = true
//    private var isApiCalled = false
//
//    private var token = ""
//    private var saltPassword = ""
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//
//        initListeners()
//        observeToken()
//        handleBackPress()
//    }
//
//    // ------------------------------------------------------------------------
//    // LISTENERS
//    // ------------------------------------------------------------------------
//    private fun initListeners() {
//
//        // ✔ Trigger Token API on first username entry
//        binding.etEmail.addTextChangedListener {
//            if (!it.isNullOrEmpty() && !isApiCalled) {
//                isApiCalled = true
//                safeTokenApiCall()
//            }
//        }
//
//        binding.tvRegister.setOnClickListener {
//            navigate(LoginFragmentDirections.actionLoginFragmentToRegisterFragment())
//        }
//
//        binding.tvAboutUnnati.setOnClickListener {
//            navigate(LoginFragmentDirections.actionLoginFragmentToAboutUnnatiFragment())
//        }
//
//        setupPasswordFieldSecurity()
//
//        binding.tvLogin.setOnClickListener {
//            onLoginClicked()
//        }
//
//        binding.tvForgotPassword.setOnClickListener {
//            navigate(LoginFragmentDirections.actionLoginFragmentToForgotPassViaAadhaarFragment())
//        }
//
//        // ✔ Password visibility toggle
//        binding.etPassword.onRightDrawableClicked {
//            togglePasswordVisibility()
//        }
//    }
//
//    // ------------------------------------------------------------------------
//    // PASSWORD FIELD HARDENING
//    // ------------------------------------------------------------------------
//    private fun setupPasswordFieldSecurity() {
//
//        binding.etPassword.apply {
//
//            // Disable long press menu
//            setOnLongClickListener { true }
//            customSelectionActionModeCallback = DisabledSelection()
//
//            // Clear clipboard
//            setOnFocusChangeListener { v, hasFocus ->
//                if (hasFocus) {
//                    val clipboard = v.context.getSystemService(Context.CLIPBOARD_SERVICE)
//                            as android.content.ClipboardManager
//                    clipboard.setPrimaryClip(android.content.ClipData.newPlainText("", ""))
//                }
//            }
//
//            // Prevent drag & drop
//            setOnDragListener { _, _ -> true }
//
//            // Disable selection
//            setTextIsSelectable(false)
//            isLongClickable = false
//        }
//    }
//
//    private class DisabledSelection : android.view.ActionMode.Callback {
//        override fun onCreateActionMode(mode: android.view.ActionMode?, menu: android.view.Menu?) = false
//        override fun onPrepareActionMode(mode: android.view.ActionMode?, menu: android.view.Menu?) = false
//        override fun onActionItemClicked(mode: android.view.ActionMode?, item: android.view.MenuItem?) = false
//        override fun onDestroyActionMode(mode: android.view.ActionMode?) {}
//    }
//
//    // ------------------------------------------------------------------------
//    // LOGIN CLICK
//    // ------------------------------------------------------------------------
//    private fun onLoginClicked() {
//
//        val username = binding.etEmail.text.toString().trim()
//        val password = binding.etPassword.text.toString().trim()
//
//        if (username.isEmpty() || password.isEmpty()) {
//            showSnackBar("Please enter id and password")
//            return
//        }
//
//        try {
//            // ✔ Handle language
//            val lang = AppUtil.getSavedLanguagePreference(requireContext())
//            AppUtil.changeAppLanguage(requireContext(), lang)
//
//            // ✔ Hashing process
//            val sha = AppUtil.sha512Hash(password)
//            val finalPass = AppUtil.sha512Hash(sha + saltPassword)
//
//            vm.getLoginAPI(
//                LoginReq(
//                    username,
//                    finalPass,
//                    AppUtil.getAndroidId(requireContext()),
//                    BuildConfig.VERSION_NAME,
//                    ""
//                )
//            )
//            observeLogin()
//
//        } catch (e: Exception) {
//            CrashlyticsUtil.logException(e)
//            showSnackBar("Something went wrong")
//        }
//    }
//
//    // ------------------------------------------------------------------------
//    // OBSERVE TOKEN RESPONSE
//    // ------------------------------------------------------------------------
//    private fun collectTokenResponse() {
//        lifecycleScope.launch {
//            collectLatestLifecycleFlow(vm.getToken) { res ->
//                when (res) {
//
//                    is Resource.Loading -> showProgressBar()
//
//                    is Resource.Error -> {
//                        hideProgressBar()
//                        showSnackBar(res.error?.message ?: "Something went wrong")
//                        CrashlyticsUtil.logMessage("Token API failed")
//                        res.error?.throwable?.let { CrashlyticsUtil.logException(it) }
//                    }
//
//                    is Resource.Success -> {
//                        hideProgressBar()
//                        val data = res.data ?: return@collectLatestLifecycleFlow
//
//                        when (data.responseCode) {
//
//                            200 -> {
//                                try {
//                                    token = AESCryptography.decryptIntoString(
//                                        data.authToken,
//                                        AppConstant.Constants.ENCRYPT_KEY,
//                                        AppConstant.Constants.ENCRYPT_IV_KEY
//                                    )
//
//                                    saltPassword = AESCryptography.decryptIntoString(
//                                        data.passString,
//                                        AppConstant.Constants.ENCRYPT_KEY,
//                                        AppConstant.Constants.ENCRYPT_IV_KEY
//                                    )
//
//                                } catch (e: Exception) {
//                                    CrashlyticsUtil.logException(e)
//                                    showSnackBar("Decryption failed!")
//                                }
//                            }
//
//                            301 -> showUpdateDialog()
//                            else -> showSnackBar(data.responseDesc)
//                        }
//                    }
//                }
//            }
//        }
//    }
//
//
//    // ------------------------------------------------------------------------
//    // OBSERVE LOGIN RESPONSE
//    // ------------------------------------------------------------------------
//    private fun observeLogin() {
//        lifecycleScope.launch {
//            collectLatestLifecycleFlow(vm.getLoginAPI) { res ->
//
//                when (res) {
//
//                    is Resource.Loading -> showProgressBar()
//
//                    is Resource.Error -> {
//                        hideProgressBar()
//                        showSnackBar(res.error?.message ?: "Login failed")
//                        CrashlyticsUtil.logMessage("Login API failed")
//                        res.error?.throwable?.let { CrashlyticsUtil.logException(it) }
//                    }
//
//                    is Resource.Success -> {
//                        hideProgressBar()
//
//                        val data = res.data ?: return@collectLatestLifecycleFlow
//
//                        when (data.responseCode) {
//
//                            200 -> handleLoginSuccess(data)
//                            203 -> {
//                                showSnackBar(data.responseDesc)
//                                safeTokenApiCall()
//                            }
//
//                            301 -> showUpdateDialog()
//                            else -> showSnackBar(data.responseDesc)
//                        }
//                    }
//                }
//            }
//        }
//    }
//
//    private fun handleLoginSuccess(res: com.kaushalpanjee.common.model.response.LoginResponse) {
//        try {
//            val decryptedToken = AESCryptography.decryptIntoString(
//                res.appCode,
//                AppConstant.Constants.ENCRYPT_KEY,
//                AppConstant.Constants.ENCRYPT_IV_KEY
//            )
//
//            if (token != decryptedToken) {
//                showSnackBar("Session expired")
//                return
//            }
//
//            AppUtil.saveTokenPreference(requireContext(), "Bearer ${res.appCode}")
//            userPreferences.updateUserId(binding.etEmail.text.toString())
//            AppUtil.saveLoginStatus(requireContext(), true)
//
//            navigateAndClearStack(R.id.mainHomePage)
//
//        } catch (e: Exception) {
//            CrashlyticsUtil.logException(e)
//            showSnackBar("Unable to login")
//        }
//    }
//
//    // ------------------------------------------------------------------------
//    // TOKEN RETRY
//    // ------------------------------------------------------------------------
//    private fun safeTokenApiCall() {
//        try {
//            vm.getToken(
//                AppUtil.getAndroidId(requireContext()),
//                BuildConfig.VERSION_NAME
//            )
//        } catch (e: Exception) {
//            CrashlyticsUtil.logException(e)
//        }
//    }
//
//    // ------------------------------------------------------------------------
//    // PASSWORD VISIBILITY
//    // ------------------------------------------------------------------------
//    private fun togglePasswordVisibility() {
//        showPassword = !showPassword
//
//        val icon = if (showPassword) {
//            R.drawable.close_eye
//        } else {
//            R.drawable.ic_open_eye
//        }
//
//        binding.etPassword.setRightDrawablePassword(
//            showPassword.not(),
//            null, null,
//            ContextCompat.getDrawable(requireContext(), icon),
//            null
//        )
//    }
//
//    // ------------------------------------------------------------------------
//    // BACK PRESS EXIT
//    // ------------------------------------------------------------------------
//    private fun handleBackPress() {
//        requireActivity().onBackPressedDispatcher.addCallback(
//            viewLifecycleOwner,
//            object : OnBackPressedCallback(true) {
//                private var lastPress = 0L
//                override fun handleOnBackPressed() {
//                    val now = System.currentTimeMillis()
//                    if (now - lastPress < 2000) {
//                        isEnabled = false
//                        requireActivity().finish()
//                    } else {
//                        lastPress = now
//                        showSnackBar("Press back again to exit")
//                    }
//                }
//            }
//        )
//    }
//
//
//}
//}
