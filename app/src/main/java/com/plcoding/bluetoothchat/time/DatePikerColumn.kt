package com.plcoding.bluetoothchat.time


import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun DatePickerColumn(
    //	列表
    pairList: List<Pair<Int, String>>,
    itemHeight: Dp,
    itemWidth: Dp? = null,
    valueState: MutableState<Int>,
    focusColor: Color = MaterialTheme.colors.primary,
    unfocusColor: Color = Color(0xFFC5C7CF)
) {
    var isInit = false

    val dataPickerCoroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    var value by valueState
    LazyColumn(
        state = listState,
        modifier = Modifier
            .height(itemHeight * 6)
            .padding(top = itemHeight / 2, bottom = itemHeight / 2)
    ) {
        item {
            Surface(Modifier.height(itemHeight)) {}
        }
        item {
            Surface(Modifier.height(itemHeight)) {}
        }
        itemsIndexed(items = pairList, key = { index, pair -> pair.first }) { index, pair ->

            val widthModifier = itemWidth?.let { Modifier.width(itemWidth) } ?: Modifier
            Box(
                modifier = Modifier
                    .height(itemHeight)
                    .then(widthModifier)
                    .clickable {
                        dataPickerCoroutineScope.launch {
                            listState.animateScrollToItem(index = index)
                        }
                    }
                    .padding(start = 5.dp, end = 5.dp), Alignment.Center
            ) {
                Text(
                    text = pair.second, color =
                    if (listState.firstVisibleItemIndex == index) focusColor
                    else unfocusColor
                )
            }
        }
        item {
            Surface(Modifier.height(itemHeight)) {}
        }
        item {
            Surface(Modifier.height(itemHeight)) {}
        }
    }


    /**
     * Jetpack Compose LazyColumn的滑动开始、结束及进行中事件
     * 参考文章 https://blog.csdn.net/asd912756674/article/details/122544808
     */
    if (listState.isScrollInProgress) {

        LaunchedEffect(Unit) {
            //只会调用一次，相当于滚动开始
        }
        //当state处于滚动时，preScrollStartOffset会被初始化并记忆,不会再被更改
        val preScrollStartOffset by remember { mutableStateOf(listState.firstVisibleItemScrollOffset) }
        val preItemIndex by remember { mutableStateOf(listState.firstVisibleItemIndex) }
        val isScrollDown = if (listState.firstVisibleItemIndex > preItemIndex) {
            //第一个可见item的index大于开始滚动时第一个可见item的index，说明往下滚动了
            true
        } else if (listState.firstVisibleItemIndex < preItemIndex) {
            //第一个可见item的index小于开始滚动时第一个可见item的index，说明往上滚动了
            false
        } else {
            //第一个可见item的index等于开始滚动时第一个可见item的index,对比item offset
            listState.firstVisibleItemScrollOffset > preScrollStartOffset
        }

        DisposableEffect(Unit) {
            onDispose {
                //	滑动结束时给状态赋值，并自动对齐
                value = pairList[listState.firstVisibleItemIndex].first
                dataPickerCoroutineScope.launch {
                    listState.animateScrollToItem(listState.firstVisibleItemIndex)
                }
            }
        }
    }

    //  选择初始值
    LaunchedEffect(Unit) {

        var initIndex = 0

        for (index in pairList.indices) {
            if (value == pairList[index].first) {
                initIndex = index
                break
            }
        }
        dataPickerCoroutineScope.launch {
            listState.animateScrollToItem(initIndex)
        }
    }
}
