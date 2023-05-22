package com.karomap.autotime.ui.activities

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.provider.Settings
import android.provider.Settings.Global.AUTO_TIME
import android.provider.Settings.Global.AUTO_TIME_ZONE
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.karomap.autotime.R
import com.karomap.autotime.ui.composables.rememberBooleanSharedPreference
import com.karomap.autotime.ui.theme.IAmNotADeveloperTheme
import com.karomap.autotime.util.allBars
import com.karomap.autotime.xposed.isModuleActive
import com.karomap.autotime.xposed.isPreferencesReady

class MainActivity : ComponentActivity() {
    @SuppressLint("WorldReadableFiles")
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            IAmNotADeveloperTheme {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = {
                                Text(stringResource(R.string.app_name))
                            },
                            windowInsets = WindowInsets.allBars.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top)
                        )
                    }
                ) { padding ->
                    Column(
                        modifier = Modifier
                            .padding(padding)
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .windowInsetsPadding(WindowInsets.allBars.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom)),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        var testResult by remember { mutableStateOf<List<Boolean>?>(null) }
                        if (isPreferencesReady()) {
                            @Suppress("DEPRECATION") var autoTime by rememberBooleanSharedPreference(
                                mode = Context.MODE_WORLD_READABLE,
                                key = AUTO_TIME,
                                defaultValue = true
                            )
                            @Suppress("DEPRECATION") var autoTimezone by rememberBooleanSharedPreference(
                                mode = Context.MODE_WORLD_READABLE,
                                key = AUTO_TIME_ZONE,
                                defaultValue = true
                            )

                            ListItem(headlineText = {
                                Text(stringResource(R.string.enable_auto_time))
                            }, modifier = Modifier.clickable {
                                autoTime = !autoTime
                            }, trailingContent = {
                                Switch(
                                    checked = autoTime,
                                    onCheckedChange = { autoTime = it }
                                )
                            })
                            ListItem(headlineText = {
                                Text(stringResource(R.string.enable_auto_timezone))
                            }, modifier = Modifier.clickable {
                                autoTimezone = !autoTimezone
                            }, trailingContent = {
                                Switch(
                                    checked = autoTimezone,
                                    onCheckedChange = { autoTimezone = it }
                                )
                            })
                        } else {
                            Text(
                                stringResource(R.string.unable_to_save_settings),
                                modifier = Modifier.padding(horizontal = 20.dp)
                            )
                        }
                        Spacer(Modifier.height(20.dp))
                        Button(onClick = {
                            val result = mutableListOf<Boolean>()
                            result.add(
                                Settings.Global.getInt(
                                    contentResolver,
                                    AUTO_TIME,
                                    0
                                ) == 1
                            )
                            result.add(
                                Settings.Global.getInt(
                                    contentResolver,
                                    AUTO_TIME_ZONE,
                                    0
                                ) == 1
                            )
                            testResult = result
                        }) {
                            Text(stringResource(R.string.test))
                        }
                        Spacer(Modifier.height(20.dp))
                        if (isModuleActive) {
                            Text(
                                stringResource(R.string.description),
                                modifier = Modifier.padding(horizontal = 20.dp)
                            )
                        } else {
                            Text(
                                stringResource(R.string.module_not_activated),
                                modifier = Modifier.padding(horizontal = 20.dp),
                                color = MaterialTheme.colorScheme.error
                            )
                        }

                        if (testResult?.size == 2) {
                            fun getString(on: String, off: String, input: List<Boolean>) =
                                input.map { if (it) on else off }.toTypedArray()

                            AlertDialog(onDismissRequest = { testResult = null }, confirmButton = {
                                Button(onClick = { testResult = null }) {
                                    Text(stringResource(android.R.string.ok))
                                }
                            }, title = {
                                Text(stringResource(R.string.test))
                            }, text = {
                                Column {
                                    Text(
                                        stringResource(
                                            R.string.dialog_test_content, *getString(
                                                stringResource(R.string.on),
                                                stringResource(R.string.off),
                                                testResult ?: listOf(false, false)
                                            )
                                        )
                                    )
                                }
                            })
                        }
                    }
                }
            }
        }
    }
}