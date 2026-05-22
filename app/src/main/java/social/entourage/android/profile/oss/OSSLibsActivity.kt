package social.entourage.android.profile.oss

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mikepenz.aboutlibraries.Libs
import com.mikepenz.aboutlibraries.entity.Library
import com.mikepenz.aboutlibraries.ui.compose.LibrariesContainer
import com.mikepenz.aboutlibraries.ui.compose.util.htmlReadyLicenseContent
import com.mikepenz.aboutlibraries.ui.compose.variant.LibraryBadges
import com.mikepenz.aboutlibraries.util.withContext
import social.entourage.android.R

class OSSLibsActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        val libs = Libs.Builder().withContext(this).build()
        setContent {
            OSSLibsScreen(libs = libs, onBack = { finish() })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OSSLib(lib: Library, onDismiss: () -> Unit) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        contentColor = Color.Black
    ) {
        OSSLibContent(lib = lib)
    }
}

@Composable
fun OSSLibContent(lib: Library) {
    val uriHandler = LocalUriHandler.current
    val quicksandBold = FontFamily(Font(R.font.quicksand_bold, FontWeight.Bold))

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 32.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = lib.name,
            fontFamily = quicksandBold,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = colorResource(id = R.color.orange)
        )

        if (lib.developers.isNotEmpty()) {
            Text(
                text = lib.developers.joinToString { it.name ?: "" },
                fontSize = 14.sp,
                color = Color.Black,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        lib.artifactVersion?.let {
            Text(
                text = "Version $it",
                fontSize = 12.sp,
                color = Color.Gray,
                modifier = Modifier.padding(top = 2.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = lib.description ?: "",
            fontSize = 14.sp,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(24.dp))

        lib.website?.let { url ->
            Button(
                onClick = { uriHandler.openUri(url) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.orange))
            ) {
                Text(text = "Website", color = Color.White, fontFamily = quicksandBold)
            }
        }
        val htmlLicense = remember(lib) {
            lib.htmlReadyLicenseContent.takeIf { it.isNotEmpty() }?.let { AnnotatedString.fromHtml(it) }
        }

        if (htmlLicense != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = htmlLicense,
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun OSSLibsScreen(libs: Libs, onBack: () -> Unit) {
    var selectedLibrary by remember { mutableStateOf<Library?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        OSSLibsHeader(onBack = onBack)
        LibrariesContainer(
            libraries = libs,
            modifier = Modifier.fillMaxSize(),
            contentPadding = WindowInsets.navigationBars.asPaddingValues(),
            badges = LibraryBadges(version=true, author=true, description=false, license=true, funding=false),
            onLibraryClick = { library : Library -> selectedLibrary = library; return@LibrariesContainer true },
        )
    }

    selectedLibrary?.let {
        OSSLib(lib = it, onDismiss = { selectedLibrary = null })
    }
}

@Composable
fun OSSLibsHeader(onBack: () -> Unit) {
    val quicksandBold = FontFamily(Font(R.font.quicksand_bold, FontWeight.Bold))

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp)
            .padding(top = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(61.dp) // 20dp top + 25dp icon + 16dp bottom
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .size(25.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.new_back),
                    contentDescription = null,
                    tint = Color.Unspecified
                )
            }

            Text(
                text = stringResource(id = R.string.about_oss_licenses),
                fontFamily = quicksandBold,
                fontSize = 15.sp,
                color = Color.Black,
                modifier = Modifier.align(Alignment.Center)
            )
        }
        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(),
            thickness = 1.dp,
            color = colorResource(id = R.color.light_orange_opacity_50)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun OSSLibsScreenPreview() {
    val mockLibs = Libs.Builder().withJson(
        """
        {
          "libraries": [
            {
              "uniqueId": "social.entourage:android",
              "artifactVersion": "1.0.0",
              "name": "Entourage Android",
              "description": "Cette application est un réseau social de solidarité.",
              "website": "https://www.entourage.social",
              "licenses": ["Apache-2.0"]
            },
            {
              "uniqueId": "com.mikepenz:aboutlibraries",
              "artifactVersion": "14.2.0",
              "name": "AboutLibraries",
              "description": "Library to show OSS licenses.",
              "website": "https://github.com/mikepenz/AboutLibraries",
              "licenses": ["Apache-2.0"]
            }
          ],
          "licenses": {
            "Apache-2.0": {
              "name": "Apache License 2.0",
              "url": "https://www.apache.org/licenses/LICENSE-2.0.txt"
            }
          }
        }
    """.trimIndent()
    ).build()

    OSSLibsScreen(libs = mockLibs, onBack = {})
}

@Preview(showBackground = true)
@Composable
fun OSSLibPreview() {
    val mockLibs = Libs.Builder().withJson(
        """
        {
          "libraries": [
            {
              "uniqueId": "social.entourage:android",
              "artifactVersion": "1.0.0",
              "name": "Entourage Android",
              "description": "Cette application est un réseau social de solidarité.",
              "website": "https://www.entourage.social",
              "developers": [{"name": "Entourage Team"}],
              "licenses": ["Apache-2.0"]
            }
          ],
          "licenses": {
            "Apache-2.0": {
              "name": "Apache License 2.0",
              "url": "https://www.apache.org/licenses/LICENSE-2.0.txt",
              "content": "C'est une Apache License 2.0"
            }
          }
        }
    """.trimIndent()
    ).build()

    val library = mockLibs.libraries.first()

    OSSLibContent(lib = library)
}
