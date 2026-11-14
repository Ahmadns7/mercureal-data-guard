package com.mercureal.dataguard.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mercureal.dataguard.domain.*
import java.text.SimpleDateFormat
import java.util.*

private val dateFmt get() = SimpleDateFormat("d MMM yyyy, HH:mm", Locale.getDefault())

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataGuardApp(vm: MainViewModel, onOpenUsageAccess: () -> Unit) {
    val s by vm.state.collectAsStateWithLifecycle()
    var tab by remember { mutableIntStateOf(0) }
    MaterialTheme {
        Scaffold(
            topBar = { TopAppBar(title = { Text("MercuReal Data Guard") }) },
            bottomBar = {
                NavigationBar {
                    listOf("Usage", "Bundles", "Settings").forEachIndexed { i, t ->
                        NavigationBarItem(selected = tab == i, onClick = { tab = i }, label = { Text(t) }, icon = {})
                    }
                }
            },
        ) { pad ->
            Box(Modifier.padding(pad).padding(16.dp)) {
                when (tab) {
                    0 -> UsageTab(s, onOpenUsageAccess)
                    1 -> BundlesTab(s, vm)
                    else -> SettingsTab(s.settings, vm::saveSettings)
                }
            }
        }
    }
}

@Composable
private fun UsageTab(s: UiState, onGrant: () -> Unit) {
    if (!s.hasAccess) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Usage access needed", style = MaterialTheme.typography.titleMedium)
            Text("Data Guard reads your phone's own mobile-data counters. Nothing is uploaded: the app has no internet permission.")
            Button(onClick = onGrant) { Text("Open usage access settings") }
        }
        return
    }
    val bin = s.settings.binaryGb
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("This month (mobile data)")
                    Text(CostEstimator.formatBytes(s.monthBytes, bin), style = MaterialTheme.typography.headlineMedium)
                    Text("Estimated cost: " + CostEstimator.formatNaira(CostEstimator.costKobo(s.monthBytes, s.settings)))
                    Text("Today: ${CostEstimator.formatBytes(s.todayBytes, bin)} (~${CostEstimator.formatNaira(CostEstimator.costKobo(s.todayBytes, s.settings))})")
                    if (s.settings.monthlyBudgetKobo > 0) {
                        val used = CostEstimator.costKobo(s.monthBytes, s.settings)
                        LinearProgressIndicator(
                            progress = { (used.toFloat() / s.settings.monthlyBudgetKobo).coerceIn(0f, 1f) },
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        )
                        Text("Budget ${CostEstimator.formatNaira(s.settings.monthlyBudgetKobo)}")
                    }
                    Text("Estimate uses your own price per GB in Settings.", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        items(s.apps.take(30), key = { it.uid }) { a ->
            ListItem(
                headlineContent = { Text(a.label) },
                supportingContent = { Text(CostEstimator.formatNaira(CostEstimator.costKobo(a.bytes, s.settings))) },
                trailingContent = { Text(CostEstimator.formatBytes(a.bytes, bin)) },
            )
        }
    }
}

@Composable
private fun BundlesTab(s: UiState, vm: MainViewModel) {
    var adding by remember { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(onClick = { adding = true }) { Text("Add bundle") }
        if (!s.hasAccess) Text("Grant usage access (Usage tab) to estimate how much of each bundle you have used.", style = MaterialTheme.typography.bodySmall)
        if (s.bundles.isEmpty()) Text("No bundles yet. Add the one you just bought to track expiry.")
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(s.bundles, key = { it.bundle.id }) { b ->
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text(b.bundle.name, style = MaterialTheme.typography.titleMedium)
                        Text("${b.state.name.replace('_', ' ')} · ${BundleTracker.formatLeft(b.millisLeft)}")
                        Text("Expires ${dateFmt.format(Date(b.bundle.expiresAtMillis))}")
                        if (s.hasAccess) {
                            LinearProgressIndicator(
                                progress = { b.usedBytes.toFloat() / b.bundle.sizeBytes },
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            )
                            Text("~${CostEstimator.formatBytes(b.remainingBytes)} left of ${CostEstimator.formatBytes(b.bundle.sizeBytes)} (estimate)")
                        }
                        Text("Paid ${CostEstimator.formatNaira(b.bundle.priceKobo)}")
                        TextButton(onClick = { vm.deleteBundle(b.bundle.id) }) { Text("Delete") }
                    }
                }
            }
        }
    }
    if (adding) AddBundleDialog(onDismiss = { adding = false }) { n, size, price, days ->
        val now = System.currentTimeMillis()
        vm.addBundle(n, size, price, now, now + days * 86_400_000L)
        adding = false
    }
}

@Composable
private fun AddBundleDialog(onDismiss: () -> Unit, onSave: (String, Long, Long, Int) -> Unit) {
    var name by remember { mutableStateOf("") }
    var gb by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var days by remember { mutableStateOf("30") }
    val sizeBytes = gb.toDoubleOrNull()?.let { (it * 1_000_000_000L).toLong() }
    val d = days.toIntOrNull()
    val err = BundleTracker.validate(name, sizeBytes, 0, ((d ?: 0) * 1L))
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add bundle") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(name, { name = it }, label = { Text("Name (e.g. MTN 5GB monthly)") })
                OutlinedTextField(gb, { gb = it }, label = { Text("Size (GB)") })
                OutlinedTextField(price, { price = it }, label = { Text("Price paid (\u20A6)") })
                OutlinedTextField(days, { days = it }, label = { Text("Valid for (days, from now)") })
            }
        },
        confirmButton = {
            TextButton(enabled = err == null && d != null, onClick = {
                onSave(name, sizeBytes!!, CostEstimator.parseNairaToKobo(price) ?: 0L, d!!)
            }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

@Composable
private fun SettingsTab(s: Settings, save: (Settings) -> Unit) {
    var price by remember(s.pricePerGbKobo) { mutableStateOf((s.pricePerGbKobo / 100.0).toString().removeSuffix(".0")) }
    var budget by remember(s.monthlyBudgetKobo) { mutableStateOf((s.monthlyBudgetKobo / 100.0).toString().removeSuffix(".0")) }
    val priceKobo = CostEstimator.parseNairaToKobo(price)
    val budgetKobo = CostEstimator.parseNairaToKobo(budget)
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Your numbers. Data Guard never looks up prices online.", style = MaterialTheme.typography.bodySmall)
        OutlinedTextField(price, { price = it }, label = { Text("Price per GB (\u20A6)") }, isError = priceKobo == null)
        OutlinedTextField(budget, { budget = it }, label = { Text("Monthly budget (\u20A6, 0 = none)") }, isError = budgetKobo == null)
        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            Switch(checked = s.binaryGb, onCheckedChange = { save(s.copy(binaryGb = it)) })
            Spacer(Modifier.width(8.dp)); Text("1 GB = 1024 MB (default is 1000 MB)")
        }
        Button(enabled = priceKobo != null && budgetKobo != null,
            onClick = { save(s.copy(pricePerGbKobo = priceKobo!!, monthlyBudgetKobo = budgetKobo!!)) }) { Text("Save") }
    }
}
