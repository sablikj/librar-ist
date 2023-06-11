package pt.ulisboa.tecnico.cmov.librarist

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.google.android.libraries.places.api.Places
import pt.ulisboa.tecnico.cmov.librarist.navigation.BottomBarScreen
import pt.ulisboa.tecnico.cmov.librarist.navigation.BottomNavGraph
import java.util.Locale


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Initialize the Places SDK
    Places.initialize(LocalContext.current, BuildConfig.GOOGLE_API_KEY)

    Scaffold(
        topBar = {
            when(currentRoute){
                "mapScreen", "searchScreen"-> {
                    CenterAlignedTopAppBar(
                        title = {
                            Text(
                                stringResource(id = R.string.app_name),
                                style = MaterialTheme.typography.headlineLarge,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        },
                        actions = {LanguageSwitchButton()
                        },
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(MaterialTheme.colorScheme.primary)
                    )
                }
            }
        },
        bottomBar = { BottomBar(navController = navController) }
    ) { innerPadding ->
        Box(modifier = Modifier
            .padding(innerPadding)
            .fillMaxWidth()) {
            BottomNavGraph(navController = navController)
        }
    }
}

@Composable
fun BottomBar(navController: NavHostController){
    val screens = listOf(
        BottomBarScreen.Map,
        BottomBarScreen.BookSearch,
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val bottomBarDestination = screens.any { it.route == currentDestination?.route }
    if (bottomBarDestination) {
        NavigationBar() {
            screens.forEach { screen ->
                AddItem(
                    screen = screen,
                    currentDestination = currentDestination,
                    navController = navController
                )
            }
        }
    }
}


@Composable
fun RowScope.AddItem(
    screen: BottomBarScreen,
    currentDestination: NavDestination?,
    navController: NavHostController
){
    NavigationBarItem(
        label = {
            Text(text = stringResource(id = screen.title))
        },
        icon = {
            Icon(painter = painterResource(id = screen.icon),
                contentDescription = "Navigation Icon"
            )
        },
        selected = currentDestination?.hierarchy?.any{
            it.route == screen.route
        } == true, // If route from current dst matches route passed from screen, make it selected
        onClick = {
            navController.navigate(screen.route){
                // Back button always returns main page - Launches
                popUpTo(navController.graph.findStartDestination().id)
                launchSingleTop = true
            }
        }
    )
}

@Composable
fun LanguageSwitchButton() {
    val context = LocalContext.current
    var expanded by remember { mutableStateOf(false) }

    // Load saved language from SharedPreferences or use system default if not found
    var language by remember {
        mutableStateOf(
            context.getSharedPreferences("preferences", Context.MODE_PRIVATE)
                .getString("language", Locale.getDefault().language)
                ?: Locale.getDefault().language
        )
    }

    LaunchedEffect(language) {
        val locale = Locale(language)
        Locale.setDefault(locale)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        context.createConfigurationContext(config)
        context.resources.updateConfiguration(config, context.resources.displayMetrics)
    }

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false }
    ) {
        DropdownMenuItem(
            contentPadding = PaddingValues(2.dp),
            text = {
                Text("\uD83C\uDDEC\uD83C\uDDE7 English")
            },
            onClick = {
                language = "en"
                expanded = false
            }
        )
        DropdownMenuItem(
            contentPadding = PaddingValues(2.dp),
            text = {
                Text("\uD83C\uDDE8\uD83C\uDDFF Čeština")
            },
            onClick = {
                language = "cs"
                expanded = false
            }
        )
    }

    Button(onClick = { expanded = true }) {
        if(language == "en"){
            Text(text = "\uD83C\uDDEC\uD83C\uDDE7")
        } else if (language == "cs"){
            Text(text = "\uD83C\uDDE8\uD83C\uDDFF")
        }
    }

    // Save selected language to SharedPreferences
    context.getSharedPreferences("preferences", Context.MODE_PRIVATE).edit()
        .putString("language", language).apply()

    // Recreate activity after language is changed
    if (language != Locale.getDefault().language) {
        val activity = context as? Activity
        activity?.recreate()
    }
}