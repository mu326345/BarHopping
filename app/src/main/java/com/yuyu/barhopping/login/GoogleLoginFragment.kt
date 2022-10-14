package com.yuyu.barhopping.login

import android.content.ContentValues.TAG
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.preference.PreferenceManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.yuyu.barhopping.Application
import com.yuyu.barhopping.MainActivity
import com.yuyu.barhopping.R
import com.yuyu.barhopping.data.User
import com.yuyu.barhopping.databinding.FragmentGoogleLoginBinding
import com.yuyu.barhopping.factory.ViewModelFactory


class GoogleLoginFragment : Fragment() {

    private lateinit var binding: FragmentGoogleLoginBinding
    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private val viewModel by viewModels<GoogleLoginViewModel> {
        ViewModelFactory((context?.applicationContext as Application).repository)
    }
    private lateinit var prefs: SharedPreferences
    var userId = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        (activity as MainActivity).hideBottomNav()
        binding = FragmentGoogleLoginBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner

        prefs = PreferenceManager.getDefaultSharedPreferences(requireContext())

        if(checkUserSignInBefore()) { // prefs有資料
            binding.signInButton.visibility = View.GONE
            val handler = Handler()
            handler.postDelayed(Runnable {
                viewModel.checkUser(userId, null)
            }, 1500)
        } else {
            binding.progress.visibility = View.GONE
            binding.signInButton.setOnClickListener {
                signIn()
            }
        }

        val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.server_client_id))
//            .requestId()
//            .requestEmail()
            .requestProfile()
            .build()

        mGoogleSignInClient = GoogleSignIn.getClient(requireActivity(), googleSignInOptions)

        viewModel.navigateToMap.observe(viewLifecycleOwner) {
            if(it != null && it == true) {
                findNavController().navigate(GoogleLoginFragmentDirections.navigateToMapFragment())
                viewModel.navigateNull()
            }
        }

        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        (activity as MainActivity).showBottomNav()
    }

    private fun signIn() {
        val signInIntent = mGoogleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
            Log.v("yy", "signin")
        }
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)

            account?.let {
                val user = User(
                    it.id ?: "",
                    it.displayName ?: "",
                    "",
                    it.photoUrl.toString(),
                    null,
                    null,
                    null
                )
                prefs.edit().putString("userId", it.id).apply()
                viewModel.checkUser(user.id, user)
            }
        } catch (e: ApiException) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
            Log.w(TAG, "exception=" + e.message);
        }
    }

    private fun checkUserSignInBefore(): Boolean {
        userId = prefs.getString("userId", "").toString()
        Log.v("yy", "userId = $userId")
        if(userId.isEmpty()) {
            return false
        } else {
            return true
        }
    }

    companion object {
        const val RC_SIGN_IN = 1
    }
}