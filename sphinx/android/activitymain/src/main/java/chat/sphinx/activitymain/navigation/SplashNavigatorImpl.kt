package chat.sphinx.activitymain.navigation

import chat.sphinx.splash.navigation.SplashNavigator
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject

@ViewModelScoped
class SplashNavigatorImpl @Inject constructor(
    navigationDriver: MainNavigationDriver
): SplashNavigator(navigationDriver) {

}