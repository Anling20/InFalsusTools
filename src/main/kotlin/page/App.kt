package cn.anling.inftool.page

import androidx.compose.runtime.Composable

object App {
    class ViewModel {

    }

    @Composable
    context(viewModel: ViewModel)
    fun Page(

    ) {
        Content(
            viewModel = viewModel,
        )
    }

    @Composable
    fun Content(
        viewModel: ViewModel,
    ) {

    }
}