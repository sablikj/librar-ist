package pt.ulisboa.tecnico.cmov.librarist.screens

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.navigation.NavHostController

@Composable
fun SearchScreen(navController: NavHostController) {
    Text(
        text = "Book Search Screen",
        color = Color.Black,
        fontSize = MaterialTheme.typography.headlineLarge.fontSize,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center
    )
}