package com.sentinel.a54

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.sentinel.a54.di.ServiceLocator
import com.sentinel.a54.ui.MainViewModel
import com.sentinel.a54.ui.theme.SentinelTheme
import com.sentinel.a54.ui.theme.AccentGreen
import com.sentinel.a54.ui.theme.CoreBlack
import com.sentinel.a54.contracts.PackageEntity
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val factory = object : ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return MainViewModel(ServiceLocator.provideRepository(this@MainActivity), ServiceLocator.provideOrchestrator(this@MainActivity)) as T
            }
        }
        setContent {
            SentinelTheme {
                val viewModel: MainViewModel = viewModel(factory = factory)
                SentinelApp(viewModel)
            }
        }
    }
}

@Composable
fun SentinelApp(viewModel: MainViewModel) {
    var currentTab by remember { mutableStateOf(0) }
    
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                NavigationBarItem(
                    selected = currentTab == 0,
                    onClick = { currentTab = 0 },
                    icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
                    label = { Text("Dashboard") },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = AccentGreen, selectedTextColor = AccentGreen)
                )
                NavigationBarItem(
                    selected = currentTab == 1,
                    onClick = { currentTab = 1 },
                    icon = { Icon(Icons.Default.Security, contentDescription = "Scan") },
                    label = { Text("Scan") },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = AccentGreen, selectedTextColor = AccentGreen)
                )
                NavigationBarItem(
                    selected = currentTab == 2,
                    onClick = { currentTab = 2 },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                    label = { Text("Settings") },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = AccentGreen, selectedTextColor = AccentGreen)
                )
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            when (currentTab) {
                0 -> DashboardScreen(viewModel)
                1 -> ScannerScreen(viewModel)
                2 -> SettingsScreen()
            }
        }
    }
}

@Composable
fun DashboardScreen(viewModel: MainViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        item { HeroHeader() }
        item {
            Spacer(modifier = Modifier.height(16.dp))
            StatusOverview(uiState.isScanning, uiState.progress, uiState.currentAction, uiState.securityScore)
        }
        item {
            MetricsRow(uiState.packages.size, uiState.systemApps, uiState.userApps)
        }
        if (uiState.aiAnalysis != null) {
            item { AiInsightCard(uiState.aiAnalysis!!) }
        }
    }
}

@Composable
fun ScannerScreen(viewModel: MainViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    var scanMode by remember { mutableStateOf("DEEP") }
    
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("SCAN ENGINE", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = Color.White)
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            ScanModeChip("QUICK", scanMode == "QUICK") { scanMode = "QUICK" }
            ScanModeChip("DEEP", scanMode == "DEEP") { scanMode = "DEEP" }
            ScanModeChip("PARANOID", scanMode == "PARANOID") { scanMode = "PARANOID" }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        if (!uiState.isScanning) {
            Button(
                onClick = { viewModel.startScan() },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentGreen, contentColor = CoreBlack),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("INITIATE $scanMode SCAN", fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            }
        } else {
            LinearProgressIndicator(
                progress = { uiState.progress },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                color = AccentGreen,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(uiState.currentAction, color = AccentGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        if (uiState.isScanning || uiState.logs.isNotEmpty()) {
            ConsoleLogCard(uiState.logs, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun ScanModeChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(24.dp),
        color = if (selected) AccentGreen else MaterialTheme.colorScheme.surfaceVariant,
        contentColor = if (selected) CoreBlack else Color.LightGray
    ) {
        Text(label, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), fontWeight = FontWeight.Bold, fontSize = 12.sp)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen() {
    var clamAvUrl by remember { mutableStateOf("https://database.clamav.net/main.cvd") }
    var yaraUrl by remember { mutableStateOf("https://github.com/Yara-Rules/rules/archive/master.zip") }
    
    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        item {
            Text("ENGINE CONFIGURATION", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = Color.White)
            Spacer(modifier = Modifier.height(24.dp))
        }
        
        item {
            Text("ClamAV Signature Source", color = AccentGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = clamAvUrl,
                onValueChange = { clamAvUrl = it },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AccentGreen,
                    unfocusedBorderColor = Color.DarkGray
                ),
                textStyle = LocalTextStyle.current.copy(color = Color.White)
            )
            Text("Default: database.clamav.net/main.cvd", color = Color.Gray, fontSize = 10.sp, modifier = Modifier.padding(top = 4.dp))
            Spacer(modifier = Modifier.height(24.dp))
        }
        
        item {
            Text("YARA Rule Repository", color = AccentGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = yaraUrl,
                onValueChange = { yaraUrl = it },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AccentGreen,
                    unfocusedBorderColor = Color.DarkGray
                ),
                textStyle = LocalTextStyle.current.copy(color = Color.White)
            )
            Text("Supports .yar and .yara remote bundles", color = Color.Gray, fontSize = 10.sp, modifier = Modifier.padding(top = 4.dp))
            Spacer(modifier = Modifier.height(32.dp))
        }
        
        item {
            Button(
                onClick = { /* Save action simulated */ },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = Color.White),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("SAVE CONFIGURATION", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun HeroHeader() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
    ) {
        AsyncImage(
            model = R.drawable.cyber_forensics_hero_1789273729690, // The generated image
            contentDescription = "Cyber Forensics Background",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(colors = listOf(Color.Transparent, CoreBlack)))
        )
        Row(
            modifier = Modifier.align(Alignment.BottomStart).padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Security, contentDescription = null, tint = AccentGreen, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text("Sentinel-A54", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                Text("Forensics Architecture", color = AccentGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun StatusOverview(isScanning: Boolean, progress: Float, currentAction: String, score: Int) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(80.dp)) {
                if (isScanning) {
                    CircularProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxSize(),
                        color = AccentGreen,
                        strokeWidth = 6.dp,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        strokeCap = StrokeCap.Round
                    )
                    Text("${(progress * 100).toInt()}%", fontWeight = FontWeight.Bold, color = Color.White)
                } else {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawArc(
                            color = AccentGreen,
                            startAngle = -90f,
                            sweepAngle = (score / 100f) * 360f,
                            useCenter = false,
                            style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("$score", fontWeight = FontWeight.Bold, fontSize = 24.sp, color = Color.White)
                        Text("SCORE", fontSize = 10.sp, color = AccentGreen)
                    }
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = if (isScanning) "SYSTEM SCANNING" else "SYSTEM SECURE",
                    color = if (isScanning) Color(0xFF00F0FF) else AccentGreen,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(currentAction, style = MaterialTheme.typography.bodyMedium, color = Color.LightGray)
            }
        }
    }
}

@Composable
fun MetricsRow(total: Int, sys: Int, user: Int) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        MetricCard("Total Artifacts", total.toString(), Modifier.weight(1f))
        MetricCard("System", sys.toString(), Modifier.weight(1f))
        MetricCard("User Apps", user.toString(), Modifier.weight(1f))
    }
}

@Composable
fun MetricCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(title, fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}

@Composable
fun ConsoleLogCard(logs: List<String>, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0A0A0A)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp).fillMaxSize()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Terminal, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("ENGINE LOGS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray, letterSpacing = 1.sp)
            }
            Spacer(modifier = Modifier.height(8.dp))
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(logs) { log ->
                    Text(
                        text = "> $log",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = Color(0xFF00F0FF),
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun AiInsightCard(report: String) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFFB388FF))
                Spacer(modifier = Modifier.width(8.dp))
                Text("GEMINI CRYPTO/THREAT INTELLIGENCE", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFB388FF))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(report, style = MaterialTheme.typography.bodyMedium, color = Color.LightGray, lineHeight = 20.sp)
        }
    }
}
