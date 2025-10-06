package com.invi.finerc.ui.component

import android.view.View
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.fragment.app.FragmentActivity
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import java.util.Calendar

@Composable
fun TimePickerDialogSample(
    initialTimeMillis: Long,
    onTimeSelected: (Long) -> Unit
) {
    val context = LocalContext.current
    val activity = context as? FragmentActivity

    var showDialog by remember { mutableStateOf(false) }

    if (showDialog && activity != null) {
        val timePicker = remember {
            MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setHour(Calendar.getInstance().apply { timeInMillis = initialTimeMillis }.get(Calendar.HOUR_OF_DAY))
                .setMinute(Calendar.getInstance().apply { timeInMillis = initialTimeMillis }.get(Calendar.MINUTE))
                .build()
        }

        DisposableEffect(Unit) {
            timePicker.show(activity.supportFragmentManager, "TimePicker")
            val listener = { _: Int, _: Int ->
                val cal = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, timePicker.hour)
                    set(Calendar.MINUTE, timePicker.minute)
                }
                onTimeSelected(cal.timeInMillis)
                showDialog = false
            }
            timePicker.addOnPositiveButtonClickListener(listener as (View) -> Unit)

            onDispose {
                timePicker.dismiss()
            }
        }
    }

    Button(onClick = { showDialog = true }) {
        Text("Select Time")
    }
}
