package com.github.k1rakishou.chan.features.image_saver

import android.net.Uri
import com.github.k1rakishou.persist_state.ImageSaverV2Options
import okhttp3.HttpUrl

sealed class ResolveDuplicateImagesState {
  object Empty : ResolveDuplicateImagesState()
  object Loading : ResolveDuplicateImagesState()
  data class Error(val throwable: Throwable) : ResolveDuplicateImagesState()
  data class Data(val duplicateImages: MutableList<DuplicateImage>) : ResolveDuplicateImagesState()
}

data class DuplicateImage(
  val locked: Boolean,
  val serverImage: ServerImage?,
  val localImage: LocalImage?,
  val resolution: ImageSaverV2Options.DuplicatesResolution
)

interface IDuplicateImage

data class ServerImage(
  val url: HttpUrl,
  val fileName: String,
  val extension: String?,
  val size: Long
) : IDuplicateImage

data class LocalImage(
  val uri: Uri,
  val fileName: String,
  val extension: String?,
  val size: Long
) : IDuplicateImage