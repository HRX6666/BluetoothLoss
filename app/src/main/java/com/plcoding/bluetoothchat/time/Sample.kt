package com.plcoding.bluetoothchat.time

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import java.time.LocalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Sample() {
    val timePickerState =
        rememberTimePickerState(
            is24Hour = true,
            initialHour = 12,
            initialMinute = 0
        )
    var isShowTimePicker by remember { mutableStateOf(false) }
    var startTime by remember { mutableStateOf(LocalTime.of(9, 0, 0)) }

    AnimatedVisibility(visible = isShowTimePicker) {
        TimePickerDialog(
            state = timePickerState,
            title = {
                Text(text = "Select Time")
            },
            onDismissRequest = { isShowTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    timePickerState.let {
                        val selectedTime = LocalTime.of(it.hour, it.minute)
                        startTime = selectedTime
                    }
                    isShowTimePicker = false
                }) {
                    Text(text = "OK")
                }
            },
            contentDescription = TimePickerDialogContentDescription(
                toggleKeyboardButton = "Currently in clock mode, click to switch",
                toggleScheduleButton = "Currently in keyboard mode, click to switch"
            )
        )
    }
}
