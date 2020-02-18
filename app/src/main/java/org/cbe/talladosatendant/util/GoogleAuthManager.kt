package org.cbe.talladosatendant.util

import android.content.Context
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.util.ExponentialBackOff
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.SheetsScopes
import org.cbe.talladosatendant.R

class GoogleAuthManager() {
    private lateinit var client: GoogleSignInClient

    companion object{
        const val REQUEST_SIGN_IN = 1
    }

    fun getClient() : GoogleSignInClient{
        return this.client
    }

    fun setUpSheetApiAuthClient(context: Context) {
        val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            // .requestScopes(Scope(SheetsScopes.SPREADSHEETS_READONLY))
            .requestScopes(Scope(SheetsScopes.SPREADSHEETS))
            .build()
        client = GoogleSignIn.getClient(context, signInOptions)

    }

    fun buildSheetService(account : GoogleSignInAccount,context : Context) : Sheets?{
        val scopes = listOf(SheetsScopes.SPREADSHEETS)
        val credential = GoogleAccountCredential
            .usingOAuth2(context, scopes)
            .setBackOff(ExponentialBackOff())
        credential.selectedAccount = account.account

        Log.i("GOOGLE AUTH MANAGER","${credential.selectedAccount.name}")

        val jsonFactory = JacksonFactory.getDefaultInstance()
        // GoogleNetHttpTransport.newTrustedTransport()
        val httpTransport =  AndroidHttp.newCompatibleTransport()
        val service = Sheets.Builder(httpTransport, jsonFactory, credential)
            .setApplicationName(context.getString(R.string.app_name))
            .build()

        return service
    }

    fun signOutClient(context: Context){
        if(isUserSigned(context)) {
            client.signOut().addOnCompleteListener { task ->
                Log.i("GOOGLEAUTHMANAGER", "Client logged out!")
            }
        }
    }

    fun isUserSigned(context: Context) : Boolean{
        return GoogleSignIn.getLastSignedInAccount(context) != null
    }

}