package com.github.k1rakishou.chan.features.media_viewer.media_view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.view.MotionEvent
import androidx.constraintlayout.widget.ConstraintLayout
import com.github.k1rakishou.chan.R
import com.github.k1rakishou.chan.features.media_viewer.ViewableMedia
import com.github.k1rakishou.chan.features.media_viewer.helper.CloseMediaActionHelper
import com.github.k1rakishou.chan.utils.AppModuleAndroidUtils
import com.github.k1rakishou.core_themes.ThemeEngine
import com.google.android.exoplayer2.upstream.DataSource
import javax.inject.Inject

@SuppressLint("ViewConstructor")
class UnsupportedMediaView(
  context: Context,
  initialMediaViewState: UnsupportedMediaViewState,
  mediaViewContract: MediaViewContract,
  private val cacheDataSourceFactory: DataSource.Factory,
  private val onThumbnailFullyLoadedFunc: () -> Unit,
  private val isSystemUiHidden: () -> Boolean,
  override val viewableMedia: ViewableMedia.Unsupported,
  override val pagerPosition: Int,
  override val totalPageItemsCount: Int
) : MediaView<ViewableMedia.Unsupported, UnsupportedMediaView.UnsupportedMediaViewState>(
  context = context,
  attributeSet = null,
  cacheDataSourceFactory = cacheDataSourceFactory,
  mediaViewContract = mediaViewContract,
  mediaViewState = initialMediaViewState
) {

  @Inject
  lateinit var themeEngine: ThemeEngine

  private val closeMediaActionHelper: CloseMediaActionHelper

  override val hasContent: Boolean
    get() = false

  init {
    AppModuleAndroidUtils.extractActivityComponent(context)
      .inject(this)

    inflate(context, R.layout.media_view_unsupported, this)
    setWillNotDraw(false)

    val movableContainer = findViewById<ConstraintLayout>(R.id.movable_container)

    closeMediaActionHelper = CloseMediaActionHelper(
      context = context,
      themeEngine = themeEngine,
      movableContainer = movableContainer,
      requestDisallowInterceptTouchEvent = { this.parent.requestDisallowInterceptTouchEvent(true) },
      onAlphaAnimationProgress = { alpha -> mediaViewContract.changeMediaViewerBackgroundAlpha(alpha) },
      invalidateFunc = { invalidate() },
      closeMediaViewer = { mediaViewContract.closeMediaViewer() },
      topGestureInfo = CloseMediaActionHelper.GestureInfo(
        gestureLabelText = AppModuleAndroidUtils.getString(R.string.download),
        onGestureTriggeredFunc = { mediaViewToolbar?.downloadMedia() },
        gestureCanBeExecuted = { mediaViewToolbar?.isDownloadAllowed() ?: false }
      ),
      bottomGestureInfo = CloseMediaActionHelper.GestureInfo(
        gestureLabelText = AppModuleAndroidUtils.getString(R.string.close),
        onGestureTriggeredFunc = { mediaViewContract.closeMediaViewer() },
        gestureCanBeExecuted = { true }
      )
    )
  }

  override fun preload() {
    // do not preload unsupported media
  }

  override fun bind() {
    onThumbnailFullyLoadedFunc()
    onThumbnailFullyLoaded()
  }

  override fun show() {
    onSystemUiVisibilityChanged(isSystemUiHidden())
    onMediaFullyLoaded()
  }

  override fun hide() {
    // no-op
  }

  override fun unbind() {
    // nothing to unbind
  }

  override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
    if (ev != null && closeMediaActionHelper.onInterceptTouchEvent(ev)) {
      return true
    }

    return super.onInterceptTouchEvent(ev)
  }

  override fun onTouchEvent(event: MotionEvent): Boolean {
    if (closeMediaActionHelper.onTouchEvent(event)) {
      return true
    }

    return super.onTouchEvent(event)
  }

  override fun draw(canvas: Canvas) {
    super.draw(canvas)
    closeMediaActionHelper.onDraw(canvas)
  }

  class UnsupportedMediaViewState : MediaViewState {
    override fun clone(): MediaViewState {
      return this
    }

    override fun updateFrom(other: MediaViewState?) {
    }
  }

  companion object {
    private const val TAG = "UnsupportedMediaView"
  }
}