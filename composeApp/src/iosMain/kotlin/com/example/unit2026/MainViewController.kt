package com.example.unit2026

import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIRectEdgeAll
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController = ComposeUIViewController {
    App()
}.apply {
    edgesForExtendedLayout = UIRectEdgeAll
    extendedLayoutIncludesOpaqueBars = true
}
