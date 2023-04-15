package com.example.myapp

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.core.content.ContextCompat
import com.example.myapp.ui.theme.MyappTheme
import java.io.File
import java.io.FileOutputStream

class UploadPhoto : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyappTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    First()
                }
            }
        }
    }
}

val bitmap = mutableStateOf<Bitmap?>(null)
var direc = intent.value.getStringExtra("catalog")


@Composable
fun CameraScreen() {
    val context = LocalContext.current
    val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
    context.startActivity(intent)
    rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                Log.d("ttttttttt", "ttttttttt")
                val image = result.data?.extras?.get("data") as Bitmap
                bitmap.value = image
            }
        }
}


@SuppressLint("UnrememberedMutableState")
@Composable
fun ImagePicker() {
    var imageUri by remember {
        mutableStateOf<Uri?>(null)
    }
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract =
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri = uri
        Log.d("dddddd", imageUri.toString())
    }

    imageUri?.let {
        if (Build.VERSION.SDK_INT < 28) {
            bitmap.value = MediaStore.Images
                .Media.getBitmap(context.contentResolver, it)

        } else {
            val source = ImageDecoder
                .createSource(context.contentResolver, it)
            bitmap.value = ImageDecoder.decodeBitmap(source)
        }
    }
    var openCam = remember { mutableStateOf<Boolean>(false) }

    Column(
        modifier = Modifier.padding(16.dp),
    ) {
        Button(
            onClick = { launcher.launch("image/*") },
            modifier = Modifier.padding(vertical = 8.dp),
        ) {
            Text(text = "Pick Image")
        }
        Button(
            onClick = {

                Log.d("cammmmm", "done")
                openCam.value = true
            },
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            Text(text = "Take a Photo")
        }
        if (openCam.value) {
            CameraScreen()
            openCam.value = false
        }
    }
}

@Composable
fun DropDownL(): String {
    var expanded by remember { mutableStateOf(false) }
    var selectedItem by remember { mutableStateOf(direc) }
    Box(modifier = Modifier.width(200.dp)) {
        TextButton(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            selectedItem?.let { Text(it) }
            Icon(Icons.Filled.ArrowDropDown, contentDescription = null)
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            val tabs = listOf("Chest", "Biceps", "Triceps", "Shoulder", "Back", "Legs")
            for (tab in tabs) {
                DropdownMenuItem(onClick = {
                    selectedItem = tab
                    expanded = false
                }) {
                    Text(tab)
                }
            }
        }
    }
    return selectedItem.toString()
}

@Composable
fun First() {
    direc = intent.value.getStringExtra("catalog")
    var up = remember { mutableStateOf<Boolean>(false) }
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(250.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(8.dp),
                elevation = 8.dp
            ) {
                bitmap.value?.let {
                    Image(
                        bitmap = it.asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(6.dp)
                            .height(120.dp)
                    )
                }
            }
        }
        ImagePicker()
        val s: String = DropDownL()
        var ename by remember { mutableStateOf("") }
        TextField(
            value = ename,
            onValueChange = { ename = it },
            label = { Text("Exercise name") },
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Text
            )
        )
        Button(
            onClick = {
                up.value = true
            },
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            Text(text = "Add")
        }
        if (up.value) {
            if (bitmap.value != null && ename != "") {
                val context = LocalContext.current
                val imageDirectory = File(
                    ContextCompat.getExternalFilesDirs(
                        context,
                        Environment.DIRECTORY_PICTURES
                    )[0], s
                )
                if (!imageDirectory.exists()) {
                    imageDirectory.mkdir()
                }
                var imageFile = File(imageDirectory, "$ename.jpg")
                if (!imageFile.exists()) {
                    imageFile.createNewFile()
                } else {
                    var c = 1
                    while (imageFile.exists()) {
                        imageFile = File(imageDirectory, "$ename $c.jpg")
                        if (!imageFile.exists()) {
                            imageFile.createNewFile()
                            break
                        }
                        c++
                    }

                }
                val outputStream = FileOutputStream(imageFile)
                bitmap.value!!.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                outputStream.flush()
                outputStream.close()
                up.value = false
                context.startActivity(Intent(context, MainActivity::class.java))
            } else {
                if (bitmap.value == null)
                    Text(text = "No Photo")
                else
                    Text(text = "Empty Exercise name")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview2() {
    MyappTheme {
        First()
    }
}