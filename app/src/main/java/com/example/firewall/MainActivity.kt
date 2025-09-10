
package com.example.firewall

import android.content.Intent
import android.net.VpnService
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.firewall.data.AppInfo
import com.example.firewall.service.FirewallVpnService
import com.example.firewall.ui.theme.FirewallAppTheme
import com.example.firewall.viewmodel.MainViewModel
import com.google.accompanist.drawablepainter.rememberDrawablePainter

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    private val vpnPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            startVpnService()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FirewallAppTheme {
                val apps by viewModel.apps.collectAsState()
                var isFirewallRunning by remember { mutableStateOf(false) }

                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    FirewallScreen(
                        isRunning = isFirewallRunning,
                        onToggleClick = {
                            if (isFirewallRunning) {
                                stopVpnService()
                            } else {
                                requestVpnPermission()
                            }
                            isFirewallRunning = !isFirewallRunning
                        },
                        apps = apps
                    )
                }
            }
        }
    }

    private fun requestVpnPermission() {
        val intent = VpnService.prepare(this)
        if (intent != null) {
            vpnPermissionLauncher.launch(intent)
        } else {
            startVpnService()
        }
    }

    private fun startVpnService() {
        val intent = Intent(this, FirewallVpnService::class.java).apply {
            action = FirewallVpnService.ACTION_START
        }
        startService(intent)
    }

    private fun stopVpnService() {
        val intent = Intent(this, FirewallVpnService::class.java).apply {
            action = FirewallVpnService.ACTION_STOP
        }
        startService(intent)
    }
}

@Composable
fun FirewallScreen(
    isRunning: Boolean,
    onToggleClick: () -> Unit,
    apps: List<AppInfo>
) {
    var showSystemApps by remember { mutableStateOf(false) }
    val filteredApps = if (showSystemApps) apps else apps.filter { !it.isSystemApp }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = if (isRunning) "Firewall is ON" else "Firewall is OFF", style = MaterialTheme.typography.headlineMedium)
        Button(onClick = onToggleClick) {
            Text(text = if (isRunning) "Deactivate Firewall" else "Activate Firewall")
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Text(text = "Show system apps")
            Switch(checked = showSystemApps, onCheckedChange = { showSystemApps = it })
        }
        AppList(apps = filteredApps)
    }
}

@Composable
fun AppList(apps: List<AppInfo>) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(apps, key = { it.packageName }) {
            AppRow(app = it)
        }
    }
}

@Composable
fun AppRow(app: AppInfo) {
    var mobileDataBlocked by remember { mutableStateOf(false) }
    var wifiBlocked by remember { mutableStateOf(false) }
    var overallBlocked by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = rememberDrawablePainter(drawable = app.icon),
            contentDescription = "${app.name} icon",
            modifier = Modifier.size(40.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = app.name, modifier = Modifier.weight(1f))
        Checkbox(checked = mobileDataBlocked, onCheckedChange = { mobileDataBlocked = it })
        Checkbox(checked = wifiBlocked, onCheckedChange = { wifiBlocked = it })
        Checkbox(checked = overallBlocked, onCheckedChange = { overallBlocked = it })
    }
}

@Preview(showBackground = true)
@Composable
fun FirewallScreenPreview() {
    FirewallAppTheme {
        FirewallScreen(isRunning = false, onToggleClick = {}, apps = emptyList())
    }
}
