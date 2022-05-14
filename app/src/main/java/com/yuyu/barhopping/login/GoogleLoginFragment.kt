package com.yuyu.barhopping.login

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentGoogleLoginBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner

        val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.server_client_id))
//            .requestId()
//            .requestEmail()
            .requestProfile()
            .build()

        mGoogleSignInClient = GoogleSignIn.getClient(requireActivity(), googleSignInOptions)

        binding.signInButton.setOnClickListener {
            signIn()
        }

        viewModel.navigateToMap.observe(viewLifecycleOwner) {
            if(it) {
                findNavController().navigate(GoogleLoginFragmentDirections.navigateToMapFragment())
            }
        }

//        val acct = GoogleSignIn.getLastSignedInAccount(requireContext())
//        if (acct != null) {
//            val user = User(
//                acct.id ?: "",
//                acct.givenName+acct.familyName,
//                "",
//                acct.photoUrl.toString(),
//                "",
//                "",
//                ""
//            )
//        }

        return binding.root
    }

    fun signIn() {
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

    fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        Log.v("yy", "")
        try {
            val account = completedTask.getResult(ApiException::class.java)

            Log.v("yy", "account = ${account.idToken}")
            Log.v("yy", "account = ${account.id}")
            Log.v("yy", "account = ${account.displayName}")

            account?.let {
                val user = User(
                    it.id ?: "",
                    it.displayName ?: "",
                    "",
                    it.photoUrl.toString(),
                    "",
                    "",
                    null
                )
                viewModel.checkUser(user.id, user)
            }
        } catch (e: ApiException) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
            Log.w(TAG, "exception=" + e.message);
        }
    }

    companion object {
        const val RC_SIGN_IN = 1
    }
}