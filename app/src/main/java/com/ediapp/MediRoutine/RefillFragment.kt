package com.ediapp.MediRoutine

import com.ediapp.MediRoutine.model.Action
import android.app.DatePickerDialog
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class RefillViewType {
    CALENDAR, LIST
}

@Composable
fun RefillFragment() {
    var currentDate by remember { mutableStateOf(Calendar.getInstance()) }
    val context = LocalContext.current
    val dbHelper = DatabaseHelper(context)
    var actions by remember { mutableStateOf<List<Action>>(emptyList()) }
    var showDialog by remember { mutableStateOf(false) }
    var dateForDialog by remember { mutableStateOf<Calendar?>(null) }
    var showDeleteDayDialog by remember { mutableStateOf(false) }
    var dayToDelete by remember { mutableStateOf<Int?>(null) }

    val monthFormat = remember { SimpleDateFormat("yyyy년 MM월", Locale.KOREAN) }
    val monthFormatForQuery = remember { SimpleDateFormat("yyyy-MM", Locale.getDefault()) }

    fun refetchActions() {
        val monthStr = monthFormatForQuery.format(currentDate.time)
        actions = dbHelper.getAllActions(month = monthStr, orderBy = "act_registered_at", orderDirection = "DESC")
    }

    LaunchedEffect(currentDate) {
        refetchActions()
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = {
                dateForDialog = null
                showDialog = true
            }) {
                Icon(Icons.Filled.Add, contentDescription = "Add")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        val newCal = currentDate.clone() as Calendar
                        newCal.add(Calendar.MONTH, -1)
                        currentDate = newCal
                    }) {
                        Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Previous Month")
                    }
                    Text(
                        text = monthFormat.format(currentDate.time),
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = {
                        currentDate = Calendar.getInstance()
                    }) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowUp,
                            contentDescription = "이번달",
                            tint = Color.DarkGray
                        )
                    }
                }
            }

            LazyColumn(modifier = Modifier.padding(top = 16.dp)) {
                items(actions) { action ->
                    RefillCard(action) {
                        dbHelper.deleteAction(action.id)
                        refetchActions()
                    }
                }
            }
        }
    }

    if (showDialog) {
        AddRefillDialog(
            initialCalendar = dateForDialog,
            onDismiss = { showDialog = false },
            onConfirm = { date ->
                val newId = dbHelper.addDoAction(date)
                refetchActions()
                Toast.makeText(context, "추가 되었습니다 (ID: $newId)", Toast.LENGTH_SHORT).show()
                showDialog = false
            }
        )
    }

    if (showDeleteDayDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDayDialog = false },
            title = { Text("삭제 확인") },
            text = { Text("정말로 이 항목을 삭제 하시겠습니까?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        dayToDelete?.let { day ->
                            val cal = (currentDate.clone() as Calendar).apply { set(Calendar.DAY_OF_MONTH, day) }
                            val datePrefix = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)

                            actions.filter { it.actRegisteredAt!!.startsWith(datePrefix) }.forEach {
                                dbHelper.deleteAction(it.id)
                            }
                            refetchActions()
                        }
                        showDeleteDayDialog = false
                        dayToDelete = null
                    }
                ) {
                    Text("삭제")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteDayDialog = false
                        dayToDelete = null
                    }
                ) {
                    Text("취소")
                }
            }
        )
    }
}

@Composable
fun AddRefillDialog(
    initialCalendar: Calendar? = null,
    onDismiss: () -> Unit,
    onConfirm: (Date) -> Unit
) {
    val context = LocalContext.current
    val calendar = remember { initialCalendar ?: Calendar.getInstance() }

    var year by remember { mutableStateOf(calendar.get(Calendar.YEAR)) }
    var month by remember { mutableStateOf(calendar.get(Calendar.MONTH)) }
    var dayOfMonth by remember { mutableStateOf(calendar.get(Calendar.DAY_OF_MONTH)) }

    var hour by remember { mutableStateOf(calendar.get(Calendar.HOUR_OF_DAY).toString()) }

    val datePickerDialog = DatePickerDialog(
        context,
        { _, selectedYear, selectedMonth, selectedDayOfMonth ->
            year = selectedYear
            month = selectedMonth
            dayOfMonth = selectedDayOfMonth
        }, year, month, dayOfMonth
    ).apply {
        datePicker.maxDate = System.currentTimeMillis()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "복용입력") },
        text = {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "$year-${month + 1}-$dayOfMonth",
                        fontWeight = FontWeight.Bold, fontSize=20.sp)

                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = { datePickerDialog.show() }) {
                        Text("날짜 변경", color = Color.White)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = hour,
                    onValueChange = { newValue ->
                        val filtered = newValue.filter { it.isDigit() }
                        if (filtered.isEmpty() || (filtered.toIntOrNull() ?: -1) in 0..23) {
                            hour = filtered
                        }
                    },
                    label = { Text("시간 (0-23)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val finalHour = hour.toIntOrNull()
                    if (finalHour == null || finalHour !in 0..23) {
                        Toast.makeText(context, "시간을 0에서 23 사이로 입력해주세요.", Toast.LENGTH_SHORT).show()
                    } else {
                        val finalCalendar = Calendar.getInstance().apply {
                            set(year, month, dayOfMonth, finalHour, 0, 0)
                        }
                        if (finalCalendar.time.after(Date())) {
                            Toast.makeText(context, "미래 날짜는 선택할 수 없습니다.", Toast.LENGTH_SHORT).show()
                        } else {
                            onConfirm(finalCalendar.time)
                        }
                    }
                }
            ) {
                Text("확인")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소")
            }
        }
    )
}

@Composable
fun RefillCard(action: Action, onDelete: () -> Unit) {
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = getDayOfWeek(action.actRegisteredAt),
                modifier = Modifier.weight(1f)
            )
            Column(modifier = Modifier.weight(2f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${action.actMessage}",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { showDeleteConfirmDialog = true }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Delete")
                    }
                }
                Text("${action.actRegisteredAt}")
            }
        }
    }

    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("삭제 확인") },
            text = { Text("정말로 이 항목을 삭제 하시겠습니까?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteConfirmDialog = false
                    }
                ) {
                    Text("삭제")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteConfirmDialog = false }
                ) {
                    Text("취소")
                }
            }
        )
    }
}

private fun getDayOfWeek(dateString: String?): String {
    if (dateString.isNullOrEmpty()) return ""
    return try {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val date = dateFormat.parse(dateString)
        val dayOfWeekSdf = SimpleDateFormat("E", Locale.getDefault())
        date?.let { dayOfWeekSdf.format(it) } ?: ""
    } catch (e: Exception) {
        ""
    }
}
