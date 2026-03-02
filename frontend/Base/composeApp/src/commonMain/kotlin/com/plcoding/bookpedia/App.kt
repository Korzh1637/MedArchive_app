package com.plcoding.bookpedia


import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card

import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.ui.tooling.preview.Preview
import medarchive.composeapp.generated.resources.Res
import org.jetbrains.compose.resources.painterResource
import medarchive.composeapp.generated.resources.pic_0005
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

@Composable
@Preview
fun App() {
    LazyColumn(modifier = Modifier.fillMaxHeight().background(Color(0xFF00D09E))) {
        itemsIndexed(
            listOf("Zack Efron", "Michal Jackson", "Michal Jordan")
        ){ index, text ->
            ListItem(index,text, "Actor")
        }
    }
}

@Composable
private fun ListItem(index: Int, name: String, prof: String){
    val counter = remember {
        mutableStateOf(0)
    }
    val color = remember {
        mutableStateOf(Color.Red)
    }
    Card(

        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp),
        shape = RoundedCornerShape(15.dp),
        elevation = 10.dp,
    ) {
        Row(
            modifier = Modifier
                .background(color.value)
                .clickable{
                    ++counter.value
                when(counter.value % 2 ){
                    0 -> color.value = Color.Red
                    1 -> color.value = Color.Blue
                }
            },
            verticalAlignment = Alignment.CenterVertically,

        ) {
            Image(painter = painterResource(resource = Res.drawable.pic_0005),
                contentDescription = "image",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .padding(5.dp)
                    .size(128.dp)
                    .clip(CircleShape),
                )
            Column(
                modifier = Modifier.padding(start = 20.dp),
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                Text(text = "Id: $index", fontSize = 20.sp)
                Text(text = name, fontSize = 20.sp)
                Text(text = prof, fontSize = 15.sp)
                Text(text = "Clicked ${counter.value.toString()} times")
            }


        }

    }
}
