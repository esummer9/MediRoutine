package com.ediapp.MediRoutine

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun DataListFragment() {
    var searchQuery by remember { mutableStateOf("") }
    val context = LocalContext.current
    val dbHelper = DatabaseHelper(context)
    val actions = dbHelper.getAllActions()

    Column(modifier = Modifier.padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("검색 키워드") },
                modifier = Modifier.weight(1f)
            )
            Button(
                onClick = { /* TODO: Implement search action */ },
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Text("검색")
            }
        }

        LazyColumn(modifier = Modifier.padding(top = 16.dp)) {
            items(actions) { action ->
                ActionCard(action)
            }
        }
    }
}

@Composable
fun ActionCard(action: Action) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = getDayOfWeek(action.actCreatedAt),
                modifier = Modifier.weight(1f)
            )
            Column(modifier = Modifier.weight(2f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${action.actMessage}",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { /* TODO: Implement delete */ }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Delete")
                    }
                }
                Text("복용: ${action.actCreatedAt}")
            }
        }
    }
}

private fun getDayOfWeek(dateString: String?): String {
    if (dateString == null) return ""
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    val date = sdf.parse(dateString)
    val dayOfWeekSdf = SimpleDateFormat("EEE", Locale.getDefault())
    return date?.let { dayOfWeekSdf.format(it) } ?: ""
}
