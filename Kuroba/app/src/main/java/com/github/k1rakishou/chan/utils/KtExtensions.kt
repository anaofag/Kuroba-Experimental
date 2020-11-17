package com.github.k1rakishou.chan.utils

import android.content.Context
import android.content.ContextWrapper
import android.graphics.drawable.ColorDrawable
import android.text.TextWatcher
import android.view.View
import androidx.appcompat.widget.AppCompatEditText
import androidx.lifecycle.Lifecycle
import com.airbnb.epoxy.AsyncEpoxyController
import com.airbnb.epoxy.DiffResult
import com.airbnb.epoxy.EpoxyController
import com.airbnb.epoxy.EpoxyRecyclerView
import com.airbnb.epoxy.OnModelBuildFinishedListener
import com.github.k1rakishou.chan.StartActivity
import com.github.k1rakishou.chan.controller.Controller
import com.github.k1rakishou.chan.utils.AppModuleAndroidUtils.waitForLayout
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

operator fun CompositeDisposable.plusAssign(disposable: Disposable) {
  this.add(disposable)
}


fun EpoxyRecyclerView.withModelsAsync(buildModels: EpoxyController.() -> Unit) {
  val controller = object : AsyncEpoxyController(true) {
    override fun buildModels() {
      buildModels(this)
    }
  }

  setController(controller)
  controller.requestModelBuild()
}

fun EpoxyController.addOneshotModelBuildListener(callback: () -> Unit) {
  addModelBuildListener(object : OnModelBuildFinishedListener {
    override fun onModelBuildFinished(result: DiffResult) {
      callback()

      removeModelBuildListener(this)
    }
  })
}

fun Context.getLifecycleFromContext(): Lifecycle? {
  return when (this) {
    is StartActivity -> this.lifecycle
    is ContextWrapper -> (this.baseContext as? StartActivity)?.lifecycle
    else -> null
  }
}

suspend fun View.awaitUntilLaidOut(continueRendering: Boolean = true) {
  suspendCancellableCoroutine<Unit> { cancellableContinuation ->
    waitForLayout(this) {
      cancellableContinuation.resume(Unit)
      return@waitForLayout continueRendering
    }
  }
}

fun Controller.findControllerOrNull(predicate: (Controller) -> Boolean): Controller? {
  if (predicate(this)) {
    return this
  }

  for (childController in childControllers) {
    val result = childController.findControllerOrNull(predicate)
    if (result != null) {
      return result
    }
  }

  return null
}

fun View.setAlphaFast(newAlpha: Float) {
  if (alpha != newAlpha) {
    alpha = newAlpha
  }
}

fun View.setVisibilityFast(newVisibility: Int) {
  if (visibility != newVisibility) {
    visibility = newVisibility
  }
}

fun View.setBackgroundColorFast(newBackgroundColor: Int) {
  val prevColor = (background as? ColorDrawable)?.color
  if (prevColor != newBackgroundColor) {
    setBackgroundColor(newBackgroundColor)
  }
}

fun AppCompatEditText.doIgnoringTextWatcher(textWatcher: TextWatcher, func: AppCompatEditText.() -> Unit) {
  removeTextChangedListener(textWatcher)
  func(this)
  addTextChangedListener(textWatcher)
}