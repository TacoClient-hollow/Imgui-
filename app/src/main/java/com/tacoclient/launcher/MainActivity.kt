package com.tacoclient.launcher

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager.GET_META_DATA
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AppSettingsAlt
import androidx.compose.material.icons.filled.Monitor
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.tacoclient.launcher.ui.theme.TacoClientTheme

class MainActivity : ComponentActivity() {
    private val mcInfo = mutableStateOf<PackageInfo?>(null)

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val window = this.window
        val decorView = window.decorView
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val controller = WindowInsetsControllerCompat(window, decorView)
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        } else {
            @Suppress("DEPRECATION")
            decorView.systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_FULLSCREEN
                    )
        }
        updateMcInfo(this)
        setContent {
            Real()
        }
    }

    @Preview(device = Devices.AUTOMOTIVE_1024p)
    @Composable
    private fun Real() {
        val selected = remember { mutableIntStateOf(0) }
        TacoClientTheme {
            Scaffold(modifier = Modifier.fillMaxSize()) { paddingValues ->
                println(paddingValues)
                Row {
                    Navigation(selected)
                    when (selected.intValue) {
                        1 -> SystemScreen()
                        2 -> SettingScreen()
                        else -> LaunchScreen()
                    }
                }
            }
        }
    }

    private fun updateMcInfo(context: Context) {
        mcInfo.value = try { context.packageManager.getPackageInfo("com.mojang.minecraftpe", GET_META_DATA) } catch (e: Exception) { e.printStackTrace(); null}
    }

    @Composable
    private fun Navigation(selected: MutableIntState) {
        val tabs = listOf(
            Triple(Icons.Default.RocketLaunch, "Launch") { selected.intValue = 0 },
            Triple(Icons.Default.Monitor, "System") { selected.intValue = 1 },
            Triple(Icons.Default.AppSettingsAlt, "Setting") { selected.intValue = 2 }
        )

        Column(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(0.2722f)
                .verticalScroll(rememberScrollState())
                .background(Color(0xff1b1d2e))
                .padding(30.dp),
            verticalArrangement = Arrangement.spacedBy(15.dp)
        ) {
            NameCard()
            HorizontalDivider(thickness = 2.dp, color = Color.White)

            tabs.forEachIndexed { index, (icon, label, onClick) ->
                TabItem(selected.intValue == index, icon, label) { onClick() }
            }
        }
    }

    @Composable
    private fun TabItem(selected: Boolean, icon: ImageVector, text: String, onClick: () -> Unit) {
        Row(
            Modifier
                .fillMaxWidth()
                .height(80.dp)
                .clip(MaterialTheme.shapes.extraLarge)
                .background(Color(0xff0e121a))
                .clickable(onClick = onClick)
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            VerticalDivider(Modifier.clip(MaterialTheme.shapes.small), 5.dp, if (selected) Color.White else Color.Transparent)
            Spacer(Modifier.width(15.dp))
            Icon(icon, text, Modifier.requiredSize(40.dp))
            Spacer(Modifier.width(30.dp))
            Text(text, style = MaterialTheme.typography.titleLarge)
        }
    }

    @Composable
    private fun NameCard() {
        Row(
            Modifier
                .fillMaxWidth()
                .height(80.dp)
                .background(Color(0xff0e121a), MaterialTheme.shapes.extraLarge)
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Star,
                "Name",
                Modifier
                    .requiredSize(40.dp)
                    .background(Color(0xff1a2330), CircleShape)
                    .padding(3.dp)
            )
            Spacer(Modifier.width(40.dp))
            Text("Steve", style = MaterialTheme.typography.titleLarge)
        }
    }

    @Composable
    private fun LaunchScreen() {
        Box(Modifier
            .fillMaxSize()
            .paint(painterResource(R.drawable.background), contentScale = ContentScale.FillBounds), Alignment.BottomCenter) {
            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(30.dp)) {
                McInfoDisplay()
                Box(Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .background(Color(0xff13171c)))
            }
            TextButton({
                prepareLauncher {
                    startActivity(Intent(this@MainActivity, classLoader.loadClass("com.mojang.minecraftpe.Launcher")).apply {
                        putStringArrayListExtra("APKS", it)
                        flags = Intent.FLAG_ACTIVITY_NO_ANIMATION
                    })
                    finish()
                }
            }, Modifier
                .padding(20.dp)
                .width(400.dp)
                .height(60.dp)
                .background(Color(0xff0e121a), MaterialTheme.shapes.extraLarge)) {
                Text("Launch")
            }
        }
    }

    @Composable
    private fun McInfoDisplay() {
        val context = LocalContext.current
        val icon: BitmapDrawable = (mcInfo.value?.applicationInfo?.loadIcon(context.packageManager) ?: ContextCompat.getDrawable(context, R.drawable.gray_mc) ) as BitmapDrawable
        Row(Modifier
            .padding(15.dp)
            .background(Color(0xff1b1d2e))
            .padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
            Image(icon.bitmap.asImageBitmap(),"Minecraft Icon", Modifier.requiredSize(35.dp))
            Text(mcInfo.value?.versionName ?: "No Minecraft Installed", Modifier.padding(horizontal = 10.dp))
        }
    }

    @Composable
    private fun SystemScreen() {

    }

    @Composable
    private fun SettingScreen() {

    }
}