package com.dblhargrove.logic.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dblhargrove.logic.subscription.SubscriptionManager
import com.dblhargrove.network.SupabaseClient.client
import com.dblhargrove.portfolio.PortfolioViewModel
import com.dblhargrove.util.log
import com.dblhargrove.util.LogLevel
import com.revenuecat.purchases.kmp.Purchases
import com.revenuecat.purchases.kmp.PurchasesConfiguration
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class LoginScreenViewModel : ViewModel() {


    private val auth = client.auth

    fun signup(email: String, password: String, callback: (String) -> Unit = {}) {

            viewModelScope.launch {
                try {
                    auth.signUpWith(Email, redirectUrl = "supabase-example://sign-up") {
                        this.email = email
                        this.password = password
                    }
                    callback("Check your email")
                } catch (e: Exception) {
                    callback("Failed")
                }
            }

    }

    fun signIn(
        email: String,
        password: String,
        onNavigate : () -> Unit) {
        val portfolioViewModel = PortfolioViewModel()
        viewModelScope.launch {
            val mainScope = MainScope()

            try {
                log(LogLevel.DEBUG,"SignInDebug", "Starting sign-in process")

                auth.signInWith(Email) {
                    this.email = email
                    this.password = password
                }

                log(LogLevel.DEBUG,"SignInDebug", "Sign-in attempt completed")

                val userId = auth.currentUserOrNull()?.id
                if (userId != null) {
                    log(LogLevel.DEBUG,"SignInDebug", "User signed in successfully: $userId")

                    PurchasesConfiguration(apiKey ="goog_OPWJCmprAzIPYYZtiVsSClVaUGL"){
                        appUserId = userId
                    }

                    Purchases.sharedInstance.logIn(
                        newAppUserID = userId,
                        onSuccess = { customerInfo, created ->
                            val entitlementId = "premium"
                            val isActive = customerInfo.entitlements[entitlementId]?.isActive == true
                            SubscriptionManager.hasPremium = isActive
                            println("RevenueCat: Premium status after login: $isActive")
                        },
                        onError = { error ->
                            println("RevenueCat login failed: ${error.message}")
                        }
                    )

                    log(LogLevel.DEBUG,"SignInDebug", "Database initialized")

                    portfolioViewModel.loadPortfoliosFromSupabase() {
                        log(LogLevel.DEBUG,"SignInDebug", "Finished loading portfolios")

                        mainScope.launch(Dispatchers.Main) {
                            onNavigate()
                        }
                    }


                } else {
                    log(LogLevel.ERROR,"SignInError", "User ID is null after sign-in")
                }

            } catch (e: Exception) {
                log(LogLevel.ERROR,"SignInError", "Sign-in failed: ${e.message}")
            }
        }
    }


}
