package com.github.k1rakishou.chan.features.image_saver

import android.content.Context
import android.net.Uri
import android.text.TextWatcher
import android.view.View
import android.widget.RadioGroup
import androidx.core.widget.doAfterTextChanged
import com.github.k1rakishou.chan.R
import com.github.k1rakishou.chan.core.base.Debouncer
import com.github.k1rakishou.chan.core.di.component.activity.ActivityComponent
import com.github.k1rakishou.chan.ui.controller.BaseFloatingController
import com.github.k1rakishou.chan.ui.theme.widget.ColorizableButton
import com.github.k1rakishou.chan.ui.theme.widget.ColorizableCheckBox
import com.github.k1rakishou.chan.ui.theme.widget.ColorizableEditText
import com.github.k1rakishou.chan.ui.theme.widget.ColorizableTextView
import com.github.k1rakishou.chan.utils.doIgnoringTextWatcher
import com.github.k1rakishou.chan.utils.setVisibilityFast
import com.github.k1rakishou.common.isNotNullNorBlank
import com.github.k1rakishou.fsaf.FileChooser
import com.github.k1rakishou.fsaf.FileManager
import com.github.k1rakishou.fsaf.callback.directory.PermanentDirectoryChooserCallback
import com.github.k1rakishou.model.data.post.ChanPostImage
import com.github.k1rakishou.persist_state.ImageSaverV2Options
import com.github.k1rakishou.persist_state.PersistableChanState
import javax.inject.Inject

class ImageSaverV2OptionsController(
  context: Context,
  private val onSaveClicked: (ImageSaverV2Options, String?) -> Unit,
  // Null when opening this controller when downloading multiple images at once
  private val chanPostImage: ChanPostImage? = null,
) : BaseFloatingController(context) {
  private lateinit var imageNameOptionsGroup: RadioGroup
  private lateinit var duplicatesResolutionOptionsGroup: RadioGroup
  private lateinit var appendSiteName: ColorizableCheckBox
  private lateinit var appendBoardCode: ColorizableCheckBox
  private lateinit var appendThreadId: ColorizableCheckBox
  private lateinit var rootDir: ColorizableTextView
  private lateinit var outputDir: ColorizableEditText
  private lateinit var fileName: ColorizableEditText
  private lateinit var additionalDirs: ColorizableEditText
  private lateinit var cancelButton: ColorizableButton
  private lateinit var saveButton: ColorizableButton

  private lateinit var fileNameTextWatcher: TextWatcher
  private lateinit var additionalDirsTextWatcher: TextWatcher

  private val currentSetting = PersistableChanState.imageSaverV2PersistedOptions.get().copy()

  private val fileNameDebouncer = Debouncer(false)
  private val additionalDirsDebouncer = Debouncer(false)

  private var overriddenFileName: String? = null

  @Inject
  lateinit var fileChooser: FileChooser
  @Inject
  lateinit var fileManager: FileManager

  override fun injectDependencies(component: ActivityComponent) {
    component.inject(this)
  }

  override fun getLayoutId(): Int = R.layout.controller_image_saver_options

  override fun onCreate() {
    super.onCreate()

    imageNameOptionsGroup = view.findViewById(R.id.image_name_options_group)
    duplicatesResolutionOptionsGroup = view.findViewById(R.id.duplicate_resolution_options_group)
    rootDir = view.findViewById(R.id.root_dir)
    appendSiteName = view.findViewById(R.id.append_site_name)
    appendBoardCode = view.findViewById(R.id.append_board_code)
    appendThreadId = view.findViewById(R.id.append_thread_id)
    additionalDirs = view.findViewById(R.id.additional_directories)
    outputDir = view.findViewById(R.id.output_dir)
    fileName = view.findViewById(R.id.custom_file_name)
    cancelButton = view.findViewById(R.id.cancel_button)
    saveButton = view.findViewById(R.id.save_button)

    if (chanPostImage == null) {
      fileName.setVisibilityFast(View.GONE)
    } else {
      fileName.setVisibilityFast(View.VISIBLE)
    }

    imageNameOptionsGroup.setOnCheckedChangeListener { _, itemId ->
      when (itemId) {
        R.id.image_name_options_use_server_name -> {
          currentSetting.imageNameOptions = ImageSaverV2Options.ImageNameOptions.UseServerFileName.rawValue
        }
        R.id.image_name_options_use_original_name -> {
          currentSetting.imageNameOptions = ImageSaverV2Options.ImageNameOptions.UseOriginalFileName.rawValue
        }
      }

      applyOptionsToView()
    }
    duplicatesResolutionOptionsGroup.setOnCheckedChangeListener { _, itemId ->
      when (itemId) {
        R.id.duplicate_resolution_options_ask -> {
          currentSetting.duplicatesResolution = ImageSaverV2Options.DuplicatesResolution.AskWhatToDo.rawValue
        }
        R.id.duplicate_resolution_options_overwrite -> {
          currentSetting.duplicatesResolution = ImageSaverV2Options.DuplicatesResolution.Overwrite.rawValue
        }
        R.id.duplicate_resolution_options_skip -> {
          currentSetting.duplicatesResolution = ImageSaverV2Options.DuplicatesResolution.Skip.rawValue
        }
      }

      applyOptionsToView()
    }

    rootDir.setOnClickListener {
      fileChooser.openChooseDirectoryDialog(object : PermanentDirectoryChooserCallback() {
        override fun onCancel(reason: String) {
          // TODO(KurobaEx v0.6.0): strings
          showToast("Canceled. Reason: $reason")
        }

        override fun onResult(uri: Uri) {
          // TODO(KurobaEx v0.6.0): strings
          val externalFile = fileManager.fromUri(uri)
          if (externalFile == null) {
            showToast("Failed to open result uri: '$uri'")
            return
          }

          if (!fileManager.isDirectory(externalFile)) {
            showToast("Uri '$uri' is not a directory")
            return
          }

          if (!fileManager.exists(externalFile)) {
            showToast("Directory '$uri' does not exist")
            return
          }

          currentSetting.rootDirectoryUri?.let { dirUriString ->
            if (uri.toString() == dirUriString) {
              return@let
            }

            val dirUri = Uri.parse(dirUriString)
            fileChooser.forgetSAFTree(dirUri)
          }

          currentSetting.rootDirectoryUri = uri.toString()
          applyOptionsToView()
        }
      })
    }

    fileNameTextWatcher = fileName.doAfterTextChanged { editable ->
      fileNameDebouncer.post({
        overriddenFileName = editable?.toString()
        applyOptionsToView()
      }, 100)
    }
    additionalDirsTextWatcher = additionalDirs.doAfterTextChanged { editable ->
      additionalDirsDebouncer.post({
        val input = editable?.toString()
        if (input.isNullOrEmpty()) {
          currentSetting.subDirs = null
          applyOptionsToView()
          return@post
        }

        currentSetting.subDirs = editable.toString()
        applyOptionsToView()
      }, 100)
    }

    appendSiteName.setOnCheckedChangeListener { _, isChecked ->
      currentSetting.appendSiteName = isChecked
      applyOptionsToView()
    }
    appendBoardCode.setOnCheckedChangeListener { _, isChecked ->
      currentSetting.appendBoardCode = isChecked
      applyOptionsToView()
    }
    appendThreadId.setOnCheckedChangeListener { _, isChecked ->
      currentSetting.appendThreadId = isChecked
      applyOptionsToView()
    }

    cancelButton.setOnClickListener {
      pop()
    }
    saveButton.setOnClickListener {
      val newFileName = if (chanPostImage != null) {
        fileName.text?.toString()
      } else {
        null
      }

      PersistableChanState.imageSaverV2PersistedOptions.set(currentSetting)
      onSaveClicked.invoke(currentSetting, newFileName)

      pop()
    }

    applyOptionsToView()
  }

  private fun checkSubDirsValid(input: String): Boolean {
    if (input.isBlank()) {
      return false
    }

    val subDirs = input
      .split("\\")

    for ((index, subDir) in subDirs.withIndex()) {
      if (pathContainsBadSymbols(subDir)) {
        return false
      }

      if (subDir.isBlank()) {
        return false
      }
    }

    return true
  }

  private fun pathContainsBadSymbols(subDir: String): Boolean {
    for (char in subDir) {
      if (char.isLetterOrDigit() || char == '\\' || char == '_') {
        continue
      }

      return true
    }

    return false
  }

  private fun fileNameContainsBadSymbols(fileName: String): Boolean {
    for (char in fileName) {
      if (char.isLetterOrDigit() || char == '_') {
        continue
      }

      return true
    }

    return false
  }

  private fun applyOptionsToView() {
    val options = currentSetting
    
    outputDir.error = null
    fileName.error = null
    additionalDirs.error = null

    when (options.imageNameOptions) {
      ImageSaverV2Options.ImageNameOptions.UseServerFileName.rawValue -> {
        imageNameOptionsGroup.check(R.id.image_name_options_use_server_name)
      }
      ImageSaverV2Options.ImageNameOptions.UseOriginalFileName.rawValue -> {
        imageNameOptionsGroup.check(R.id.image_name_options_use_original_name)
      }
    }

    when (options.duplicatesResolution) {
      ImageSaverV2Options.DuplicatesResolution.AskWhatToDo.rawValue -> {
        duplicatesResolutionOptionsGroup.check(R.id.duplicate_resolution_options_ask)
      }
      ImageSaverV2Options.DuplicatesResolution.Overwrite.rawValue -> {
        duplicatesResolutionOptionsGroup.check(R.id.duplicate_resolution_options_overwrite)
      }
      ImageSaverV2Options.DuplicatesResolution.Skip.rawValue -> {
        duplicatesResolutionOptionsGroup.check(R.id.duplicate_resolution_options_skip)
      }
    }

    val currentFileNameString = if (chanPostImage != null) {
      val currentFileNameString = if (overriddenFileName != null) {
        overriddenFileName
      } else {
        when (options.imageNameOptions) {
          ImageSaverV2Options.ImageNameOptions.UseServerFileName.rawValue -> {
            chanPostImage.serverFilename
          }
          ImageSaverV2Options.ImageNameOptions.UseOriginalFileName.rawValue -> {
            chanPostImage.filename
          }
          else -> throw IllegalArgumentException("Unknown imageNameOptions: ${options.imageNameOptions}")
        }
      }

      fileName.doIgnoringTextWatcher(fileNameTextWatcher) {
        text?.replace(0, text!!.length, currentFileNameString)
      }

      currentFileNameString
    } else {
      null
    }

    appendSiteName.isChecked = options.appendSiteName
    appendBoardCode.isChecked = options.appendBoardCode
    appendThreadId.isChecked = options.appendThreadId

    val rootDirectoryUriString = options.rootDirectoryUri
    if (rootDirectoryUriString.isNullOrBlank()) {
      rootDir.textSize = 20f
      rootDir.text = context.getString(R.string.controller_image_save_options_click_to_set_root_dir)
    } else {
      rootDir.text = rootDirectoryUriString
      rootDir.textSize = 14f
    }

    val subDirsString = options.subDirs

    if (subDirsString != null && !checkSubDirsValid(subDirsString)) {
      additionalDirs.error = context.getString(R.string.controller_image_save_options_sub_dirs_are_not_valid)
      enableDisableSaveButton(enable = false)
      return
    }

    additionalDirs.doIgnoringTextWatcher(additionalDirsTextWatcher) {
      text?.replace(0, text!!.length, subDirsString ?: "")
    }

    if (rootDirectoryUriString.isNullOrBlank()) {
      outputDir.error = context.getString(R.string.controller_image_save_options_root_dir_not_set)
      enableDisableSaveButton(enable = false)
      return
    }

    val outputDirText = buildString {
      append("<Root dir>")

      if (options.appendSiteName) {
        append("\\<Site name>")
      }
      if (options.appendBoardCode) {
        append("\\<Board code>")
      }
      if (options.appendThreadId) {
        append("\\<Thread id>")
      }
      if (subDirsString.isNotNullNorBlank()) {
        append("\\${subDirsString}")
      }

      if (currentFileNameString != null && chanPostImage != null) {
        append("\\${currentFileNameString}")
        append(".")
        append(chanPostImage.extension)
      }
    }

    outputDir.setText(outputDirText)

    if (chanPostImage != null) {
      if (currentFileNameString.isNullOrBlank()) {
        fileName.error = context.getString(R.string.controller_image_save_options_file_name_is_blank)
        enableDisableSaveButton(enable = false)
        return
      }

      if (fileNameContainsBadSymbols(currentFileNameString)) {
        fileName.error = context.getString(R.string.controller_image_save_options_file_name_bad_symbols)
        enableDisableSaveButton(enable = false)
        return
      }

      // fallthrough
    }

    enableDisableSaveButton(enable = true)
  }

  private fun enableDisableSaveButton(enable: Boolean) {
    saveButton.isEnabled = enable
    saveButton.isClickable = enable
    saveButton.isFocusable = enable
  }

}