package com.example.keyboardtusk

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.Surface
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class MainActivity : ComponentActivity() {

    private val numberList = mutableListOf<String>()
    private val spaceFlagList = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        resetKeyboard()

        setContent {
            ContentView(
                numberList,
                onReset = {
                    resetKeyboard()
                }
            )
        }
    }


    private fun resetKeyboard() {
        numberList.clear()
        for (i in 1..9) {
            numberList.add(i.toString())
        }

        spaceFlagList.clear()
        for (i in 0 until 7) {
            spaceFlagList.add("")
        }

        val randomIconIndex = (spaceFlagList.indices).random()
        spaceFlagList[randomIconIndex] = "icon"

        for (i in 1..7) {
            val randomIndex = (0..numberList.size).random()
            numberList.add(randomIndex, spaceFlagList[i - 1])
        }
    }
}


@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun ContentView(
    numberList: MutableList<String>,
    onReset: () -> Unit
) {
    val state = rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden)
    val scope = rememberCoroutineScope()

    val shouldReset = remember {
        mutableStateOf(false)
    }

    val result = remember {
        mutableStateOf("")
    }

    ModalBottomSheetLayout(scrimColor = Color.Transparent,
        sheetBackgroundColor = Color.Black,
        sheetElevation = 5.dp,
        sheetShape = RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp),
        sheetState = state,
        sheetContent = {
            BottomActivitySheetView(
                numberList = numberList,
                onItemClick = { data ->
                    if (result.value.length == 6) {
                        return@BottomActivitySheetView
                    }
                    result.value += data

                    var index = 0
                    for (i in result.value.indices){
                        if (result.value[i] != '*'){
                            index = i
                            break
                        }
                    }

                    scope.launch {
                        delay(200)
                        StringBuilder(result.value).apply {
                             setCharAt(index, '*')
                            result.value = toString()
                        }
                    }
                }
            )
        }) {

        Surface(
            color = Color.White
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                
                Text(
                    text = result.value,
                    fontSize = 30.sp
                    )
                
                Spacer(modifier = Modifier.padding(vertical = 20.dp))

                Button(colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color.Black, contentColor = Color.White
                ), onClick = {

                    result.value = ""

                    scope.launch {
                        state.show()
                    }

                }) {
                    Text(
                        text = "Keyboard", fontSize = 20.sp
                    )
                }

            }
        }
    }

    BackHandler(
        enabled = (state.currentValue == ModalBottomSheetValue.HalfExpanded
                || state.currentValue == ModalBottomSheetValue.Expanded),
        onBack = {
            scope.launch {
                state.hide()
            }
        }
    )

    LaunchedEffect(key1 = Unit, block = {

        snapshotFlow {
            state.currentValue
        }.collect {

            if (it == ModalBottomSheetValue.Hidden && shouldReset.value) {
                onReset()
                shouldReset.value = false
            } else if (it == ModalBottomSheetValue.Expanded) {
                shouldReset.value = true
            }

        }

    })


}


@Composable
private fun BottomActivitySheetView(
    numberList: MutableList<String>,
    onItemClick: (data: String) -> Unit
) {

    LazyVerticalGrid(
        columns = GridCells.Fixed(4),
        contentPadding = PaddingValues(horizontal = 2.dp, vertical = 2.dp),
        state = rememberLazyGridState(),
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp)
    ) {

        itemsIndexed(items = numberList, key = { index: Int, _: String ->
            index
        }) { _: Int, item ->

            GridListItemView(
                data = item,
                onItemClick = onItemClick
            )

        }

    }
}


@Composable
private fun GridListItemView(
    data: String,
    onItemClick: (data: String) -> Unit
) {
    if (data == "icon") {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .height(60.dp)
                .background(Color.Gray),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.mipmap.dice6),
                contentDescription = "",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
            )

        }
    } else {
        TextButton(
            enabled = data != "",
            shape = RoundedCornerShape(0.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            onClick = {
                onItemClick(data)
            },
            colors = androidx.compose.material3.ButtonDefaults.textButtonColors(
                containerColor = if (data == "") {
                    Color.Gray
                } else {
                    Color.DarkGray
                }, contentColor = Color.White
            )
        ) {
            Text(
                text = data, fontSize = 15.sp, fontWeight = FontWeight.Bold
            )
        }
    }
}
