package info.papdt.express.helper.ui

import android.animation.Animator
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.appcompat.widget.TooltipCompat
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.widget.*
import androidx.asynclayoutinflater.view.AsyncLayoutInflater
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.scwang.smartrefresh.layout.SmartRefreshLayout
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.constant.RefreshState
import com.scwang.smartrefresh.layout.listener.OnRefreshListener
import info.papdt.express.helper.*
import info.papdt.express.helper.api.Kuaidi100PackageApi
import info.papdt.express.helper.api.KtPackageApi
import info.papdt.express.helper.dao.PackageDatabase
import info.papdt.express.helper.model.BaseMessage
import info.papdt.express.helper.model.Kuaidi100Package
import info.papdt.express.helper.receiver.ConnectivityReceiver
import info.papdt.express.helper.support.PackageApiType
import info.papdt.express.helper.support.SettingsInstance
import info.papdt.express.helper.ui.adapter.HomeToolbarSpinnerAdapter
import info.papdt.express.helper.ui.adapter.HomePackageListAdapter
import info.papdt.express.helper.ui.common.AbsActivity
import moe.feng.common.stepperview.VerticalStepperItemView
import moe.feng.kotlinyan.common.*

import info.papdt.express.helper.R
import info.papdt.express.helper.dao.SRDatabase
import info.papdt.express.helper.event.EventCallbacks
import info.papdt.express.helper.model.MaterialIcon
import info.papdt.express.helper.support.SnackbarUtils
import info.papdt.express.helper.ui.dialog.ChooseCategoryDialog
import java.lang.Exception

class HomeActivity : AbsActivity(), OnRefreshListener {

    companion object {

        private const val TAG = "HomeActivity"

        const val STATE_ADD_PACKAGE_VIEWS = "state_add_package_views"
        const val STATE_ADD_PACKAGE_NUMBER = "$STATE_ADD_PACKAGE_VIEWS.number"
        const val STATE_ADD_PACKAGE_COMPANY = "$STATE_ADD_PACKAGE_VIEWS.company"
        const val STATE_ADD_PACKAGE_COMPANY_STATE = "$STATE_ADD_PACKAGE_VIEWS.company_state"
        const val STATE_ADD_PACKAGE_NAME = "$STATE_ADD_PACKAGE_VIEWS.name"

        const val STATE_FILTER_KEYWORD = "$TAG.filter_keyword"
        const val STATE_FILTER_COMPANY = "$TAG.filter_company"
        const val STATE_FILTER_CATEGORY = "$TAG.filter_category"
        const val STATE_TEMP_FILTER_COMPANY = "$TAG.temp_filter_company"
        const val STATE_TEMP_FILTER_CATEGORY = "$TAG.temp_filter_category"

        const val REQUEST_FILTER_COMPANY = 20001
        const val REQUEST_CODE_CHANGE = 20002

        fun search(context: Context, number: String) {
            val intent = Intent(context, HomeActivity::class.java)
            intent.action = ACTION_SEARCH
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.putExtra(EXTRA_DATA, number)
            context.startActivity(intent)
        }

        fun getSearchIntent(context: Context, number: String): Intent {
            val intent = Intent(context, HomeActivity::class.java)
            intent.action = ACTION_SEARCH
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.putExtra(EXTRA_DATA, number)
            return intent
        }

    }

    private val appBarLayout by lazy<AppBarLayout> { findViewById(R.id.app_bar_layout) }
    private val refreshLayout by lazy<SmartRefreshLayout> { findViewById(R.id.refresh_layout) }
    private val listView by lazy<RecyclerView> { findViewById(android.R.id.list) }
    private val spinner by lazy<Spinner> { mToolbar!!.findViewById(R.id.spinner) }
    private val addButton by lazy<View> { findViewById(R.id.add_button) }
    private val manageCategoriesButton by lazy<View> { findViewById(R.id.manage_category_button) }
    private val scanButton by lazy<View> { findViewById(R.id.scan_button) }
    private val moreButton by lazy<View> { findViewById(R.id.more_button) }
    private val bottomSheet by lazy<View> { findViewById(R.id.bottom_sheet_add_package) }
    private val bottomSheetBackground by lazy<View> {
        findViewById(R.id.bottom_sheet_background)
    }

    private val moreMenu: PopupMenu by lazy {
        PopupMenu(this, moreButton).also {
            it.inflate(R.menu.bottom_menu_home)
            it.setOnMenuItemClickListener(this::onOptionsItemSelected)
        }
    }

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>
    private lateinit var addPackageViewHolder: AddPackageViewHolder

    private val listAdapter by lazy { HomePackageListAdapter() }
    private val homeListScrollListener = HomeListScrollListener()

    private var filterCompanyChoiceText: TextView? = null
    private var filterCategoryIconView: TextView? = null
    private var filterCategoryTitleView: TextView? = null
    private var tempFilterCompany: String? = null
    private var tempFilterCategory: String? = null

    private val packageDatabase by lazy { PackageDatabase.getInstance(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        if (savedInstanceState == null) {
            bottomSheet.makeGone()
            window.decorView.post {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
                bottomSheet.makeVisible()
            }

            listAdapter.filter = SettingsInstance.lastFilter
            listAdapter.sortType = SettingsInstance.lastSortBy
        } else {
            listAdapter.filterKeyword = savedInstanceState[STATE_FILTER_KEYWORD] as? String
            listAdapter.filterCompany = savedInstanceState[STATE_FILTER_COMPANY] as? String
            listAdapter.filterCategory = savedInstanceState[STATE_FILTER_CATEGORY] as? String
            tempFilterCompany = savedInstanceState[STATE_TEMP_FILTER_COMPANY] as? String
            tempFilterCategory = savedInstanceState[STATE_TEMP_FILTER_CATEGORY] as? String
        }

        spinner.setSelection(listAdapter.filter)

        addPackageViewHolder.onRestoreInstanceState(savedInstanceState)

        when (intent?.action) {
            ACTION_SEARCH -> {
                val number = intent[EXTRA_DATA]?.asString()
                if (number != null) {
                    addPackageViewHolder.setNumber(number)
                    addButton.performClick()
                }
            }
            ACTION_REQUEST_OPEN_DETAILS -> {
                DetailsActivity.launch(this, intent.getParcelableExtra(EXTRA_DATA))
            }
            ScannerActivity.ACTION_SCAN_TO_ADD -> {
                scanButton.performClick()
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        addPackageViewHolder.onSaveInstanceState(outState)

        outState.putString(STATE_FILTER_KEYWORD, listAdapter.filterKeyword)
        outState.putString(STATE_FILTER_COMPANY, listAdapter.filterCompany)
        outState.putString(STATE_FILTER_CATEGORY, listAdapter.filterCategory)
        outState.putString(STATE_TEMP_FILTER_COMPANY, tempFilterCompany)
        outState.putString(STATE_TEMP_FILTER_CATEGORY, tempFilterCategory)
    }

    override fun onStart() {
        super.onStart()
        registerLocalBroadcastReceiver(EventCallbacks.deletePackage {
            Log.i(TAG, "Requesting delete package: name=${it.name}")
            packageDatabase.remove(it)
            listAdapter.setPackages(packageDatabase.data)
            SnackbarUtils.makeInCoordinator(this,
                    getString(R.string.toast_item_removed, it.name),
                    Snackbar.LENGTH_LONG)
                    .setAction(R.string.toast_item_removed_action) {
                        packageDatabase.undoLastRemoval()
                        listAdapter.setPackages(packageDatabase.data)
                    }
                    .show()
        }, action = ACTION_REQUEST_DELETE_PACK)
        registerLocalBroadcastReceiver(ChooseCategoryDialog.onChooseCategory {
            tempFilterCategory = if (it.title.isEmpty()) null else it.title
            updateSearchDialogViewsValue()
        }, action = ChooseCategoryDialog.ACTION_CHOOSE_CATEGORY)
    }

    override fun onStop() {
        super.onStop()
        try {
            packageDatabase.save()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun showBottomSheetBackground() {
        bottomSheetBackground.makeVisible()
    }

    private fun hideBottomSheetBackground() {
        bottomSheetBackground.makeGone()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun setUpViews() {
        listView.addOnScrollListener(homeListScrollListener)

        listView.adapter = listAdapter
        listAdapter.lastUpdateTime = packageDatabase.lastUpdateTime
        listAdapter.setPackages(packageDatabase.data)

        refreshLayout.setOnRefreshListener(this)

        spinner.adapter = HomeToolbarSpinnerAdapter(this)
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?,
                                        position: Int,
                                        id: Long) {
                listAdapter.filter = position
                SettingsInstance.lastFilter = position
            }

        }

        addButton.setOnClickListener {
            if (refreshLayout.state == RefreshState.Refreshing) {
                SnackbarUtils.makeInCoordinator(this,
                        R.string.toast_please_wait_for_finishing_refreshing).show()
            } else {
                if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_HIDDEN) {
                    showBottomSheetBackground()
                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
                } else {
                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
                }
            }
        }

        TooltipCompat.setTooltipText(manageCategoriesButton, getString(R.string.action_manage_categories))
        manageCategoriesButton.setOnClickListener {
            ManageCategoriesActivity.launch(this, REQUEST_CODE_CHANGE)
        }

        TooltipCompat.setTooltipText(scanButton, getString(R.string.activity_scanner))
        scanButton.setOnClickListener { openScanner() }

        moreButton.setOnClickListener {
            moreMenu.show()
        }
        moreButton.setOnTouchListener(moreMenu.dragToOpenListener)

        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)
        bottomSheetBehavior.setBottomSheetCallback(AddPackageBottomSheetCallback())

        addPackageViewHolder = AddPackageViewHolder(bottomSheet)
    }

    override fun onResume() {
        super.onResume()

        bottomSheetBackground.setOnTouchListener { _, event ->
            if (event.actionMasked == KeyEvent.ACTION_DOWN) {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
                return@setOnTouchListener true
            }
            return@setOnTouchListener false
        }
    }

    private fun showSearchDialog() {
        val inflater = AsyncLayoutInflater(this)
        inflater.inflate(R.layout.dialog_home_search, null) { view, _, _ ->
            val keywordEdit = view.findViewById<EditText>(R.id.keyword_edit)
            filterCompanyChoiceText = view.findViewById(R.id.company_choice_text)
            filterCategoryIconView = view.findViewById<TextView>(R.id.category_icon_view)
                    .apply { typeface = MaterialIcon.iconTypeface }
            filterCategoryTitleView = view.findViewById(R.id.category_title_view)
            tempFilterCompany = listAdapter.filterCompany
            tempFilterCategory = listAdapter.filterCategory

            view.findViewById<Button>(R.id.company_choose_btn).setOnClickListener {
                val intent = Intent(this, CompanyChooserActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
                startActivityForResult(intent, REQUEST_FILTER_COMPANY)
            }
            view.findViewById<Button>(R.id.category_choose_button).setOnClickListener {
                ChooseCategoryDialog().show(supportFragmentManager, "choose_category_dialog")
            }

            updateSearchDialogViewsValue()

            buildAlertDialog {
                titleRes = R.string.search_dialog_title
                setView(view)
                okButton { _, _ ->
                    listAdapter.filterKeyword = if (keywordEdit.text?.toString().isNullOrBlank())
                        null else keywordEdit.text?.toString()
                    listAdapter.filterCompany = tempFilterCompany
                    listAdapter.filterCategory = tempFilterCategory
                    invalidateOptionsMenu()
                }
                setOnCancelListener {
                    clearSearchDialogViewRef()
                }
                setOnDismissListener {
                    clearSearchDialogViewRef()
                }
                cancelButton()
            }.show()
        }
    }

    private fun updateSearchDialogViewsValue() = ui {
        filterCompanyChoiceText?.text = if (tempFilterCompany == null)
            getString(R.string.unset)
        else
            Kuaidi100PackageApi.CompanyInfo.getNameByCode(tempFilterCompany)

        filterCategoryIconView?.text = tempFilterCategory?.let {
            SRDatabase.categoryDao.get(it)?.iconCode
        }
        filterCategoryTitleView?.text = tempFilterCategory
                ?: getString(R.string.choose_category_dialog_unclassified)
    }

    private fun clearSearchDialogViewRef() {
        filterCompanyChoiceText = null
        filterCategoryIconView = null
        filterCategoryTitleView = null
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.top_menu_home, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        when (SettingsInstance.lastSortBy) {
            HomePackageListAdapter.SORT_BY_UPDATE_TIME ->
                menu.findItem(R.id.action_sort_by_update_time).isChecked = true
            HomePackageListAdapter.SORT_BY_NAME ->
                menu.findItem(R.id.action_sort_by_name).isChecked = true
            HomePackageListAdapter.SORT_BY_CREATE_TIME ->
                menu.findItem(R.id.action_sort_by_create_time).isChecked = true
        }

        val searchItem = menu.findItem(R.id.action_search)
        if (listAdapter.hasFilter) {
            searchItem.setIcon(R.drawable.ic_reset_searched_text_primary_color_24dp)
            searchItem.setTitle(R.string.action_reset_search)
        } else {
            searchItem.setIcon(R.drawable.ic_search_text_primary_color_24dp)
            searchItem.setTitle(R.string.action_search)
        }

        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.action_read_all -> {
            if (bottomSheetBehavior.state != BottomSheetBehavior.STATE_HIDDEN) {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            }
            ui {
                val data = asyncIO {
                    val count = packageDatabase.readAll()
                    packageDatabase.save()
                    count
                }.await()

                listAdapter.setPackages(packageDatabase.data)
                SnackbarUtils.makeInCoordinator(this,
                        getString(R.string.toast_all_read, data),
                        Snackbar.LENGTH_LONG).show()
            }
            true
        }
        R.id.action_search -> {
            if (listAdapter.hasFilter) {
                listAdapter.clearFilters()
                invalidateOptionsMenu()
            } else {
                showSearchDialog()
            }
            true
        }
        R.id.action_refresh -> {
            if (bottomSheetBehavior.state != BottomSheetBehavior.STATE_HIDDEN) {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            }
            refreshLayout.autoRefresh()
            true
        }
        R.id.action_settings -> {
            SettingsActivity.launch(this)
            true
        }
        R.id.action_sort_by_create_time -> {
            item.isChecked = true
            listAdapter.sortType = HomePackageListAdapter.SORT_BY_CREATE_TIME
            SettingsInstance.lastSortBy = listAdapter.sortType
            true
        }
        R.id.action_sort_by_name -> {
            item.isChecked = true
            listAdapter.sortType = HomePackageListAdapter.SORT_BY_NAME
            SettingsInstance.lastSortBy = listAdapter.sortType
            true
        }
        R.id.action_sort_by_update_time -> {
            item.isChecked = true
            listAdapter.sortType = HomePackageListAdapter.SORT_BY_UPDATE_TIME
            SettingsInstance.lastSortBy = listAdapter.sortType
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            ScannerActivity.REQUEST_CODE_SCAN -> {
                if (RESULT_OK == resultCode) {
                    val result = data!![ScannerActivity.EXTRA_RESULT]?.asString()
                    if (result == null || result.isBlank()) {
                        // FIXME Show toast now
                    } else {
                        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
                        addPackageViewHolder.setNumber(result)
                    }
                }
            }
            REQUEST_CODE_CHOOSE_COMPANY -> {
                if (RESULT_OK == resultCode) {
                    val companyCode = data!![RESULT_EXTRA_COMPANY_CODE]!!.asString()
                    addPackageViewHolder.setCompany(companyCode)
                }
            }
            REQUEST_DETAILS -> {
                when (resultCode) {
                    RESULT_RENAMED -> {
                        listAdapter.refreshPackage(
                                PackageDatabase.getInstance(this),
                                data!!.getStringExtra("id")
                        )
                    }
                    RESULT_DELETED -> {
                        val deleting = data!!.getParcelableExtra<Kuaidi100Package>("data")
                        Log.i(TAG, "Requesting delete package: name=${deleting.name}")
                        packageDatabase.remove(deleting)
                        val index = listAdapter.items.indexOf(deleting)
                        listAdapter.setPackages(packageDatabase.data, notify = false)
                        if (index != -1) {
                            listAdapter.notifyItemRemoved(index)
                        } else {
                            listAdapter.notifyDataSetChanged()
                        }
                    }
                }
            }
            REQUEST_FILTER_COMPANY -> {
                if (RESULT_OK == resultCode) {
                    tempFilterCompany = data?.get(RESULT_EXTRA_COMPANY_CODE)?.asString()
                    updateSearchDialogViewsValue()
                }
            }
            REQUEST_CODE_CHANGE -> {
                if (RESULT_OK == resultCode) {
                    listAdapter.notifyDataSetChanged()
                }
            }
        }
    }

    override fun onBackPressed() {
        if (bottomSheetBehavior.state != BottomSheetBehavior.STATE_HIDDEN) {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            return
        }
        super.onBackPressed()
    }

    private fun openScanner() {
        val intent = Intent(this, ScannerActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        startActivityForResult(intent, ScannerActivity.REQUEST_CODE_SCAN)
    }

    private fun onPackageAdd(data: Kuaidi100Package) {
        // TODO Make animated
        listAdapter.setPackages(packageDatabase.data)
        listAdapter.notifyDataSetChanged()

        listView.post {
            listAdapter.items.indexOf(data).takeIf { it != -1 }?.let {
                listView.smoothScrollToPosition(it)
            }
        }
    }

    override fun onRefresh(refreshLayout: RefreshLayout) {
        if (refreshLayout.state != RefreshState.Refreshing) {
            refreshLayout.autoRefresh()
        }
        ui {
            val task = asyncIO {
                packageDatabase.pullDataFromNetwork(SettingsInstance.forceUpdateAllPackages)
                packageDatabase.data
            }
            listAdapter.setPackages(task.await())
            System.currentTimeMillis().let {
                listAdapter.lastUpdateTime = it
                packageDatabase.lastUpdateTime = it
            }
            packageDatabase.save()
            refreshLayout.finishRefresh()
        }
    }

    private inner class AddPackageBottomSheetCallback : BottomSheetBehavior.BottomSheetCallback() {

        override fun onSlide(v: View, slideOffset: Float) {
            val progress = if (slideOffset.isNaN())
                1f else 1f + Math.max(slideOffset, -1f)
            bottomSheetBackground.post {
                bottomSheetBackground.alpha = progress * 0.35f
            }
            bottomSheetBackground.postInvalidate()
        }

        @SuppressLint("SwitchIntDef")
        override fun onStateChanged(v: View, newState: Int) {
            when (newState) {
                BottomSheetBehavior.STATE_HIDDEN -> {
                    if (bottomSheetBackground.visibility != View.GONE) {
                        bottomSheetBackground.makeGone()
                    }
                    bottomSheet.hideKeyboard()
                }
                BottomSheetBehavior.STATE_COLLAPSED -> {
                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
                }
                else -> {
                    if (bottomSheetBackground.visibility != View.VISIBLE) {
                        bottomSheetBackground.makeVisible()
                    }
                }
            }
        }

    }

    private inner class HomeListScrollListener : RecyclerView.OnScrollListener() {

        private val statedElevation by lazy {
            mapOf(true to resources.getDimension(R.dimen.app_bar_elevation), false to 0f)
        }

        private val duration by lazy {
            resources.getInteger(android.R.integer.config_shortAnimTime).toLong()
        }

        private var elevationAnimator: Animator? = null
        private var animatorDirection: Boolean? = null

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            val layoutManager = recyclerView.layoutManager!! as LinearLayoutManager
            val shouldLift = layoutManager.findFirstCompletelyVisibleItemPosition() != 0

            if (animatorDirection != shouldLift) {
                if (elevationAnimator?.isRunning == true) {
                    elevationAnimator?.cancel()
                }
            }
            animatorDirection = shouldLift
            if (elevationAnimator?.isRunning != true
                    && appBarLayout.elevation != statedElevation[shouldLift]) {
                elevationAnimator = ObjectAnimator.ofFloat(
                        appBarLayout,
                        "elevation",
                        statedElevation[!shouldLift] as Float,
                        statedElevation[shouldLift] as Float
                )
                elevationAnimator!!.duration = duration
                elevationAnimator!!.start()
            }
        }

    }

    private inner class AddPackageViewHolder(val view: View) {

        private val step0: VerticalStepperItemView = view.findViewById(R.id.stepper_input_num)
        private val step1: VerticalStepperItemView = view.findViewById(R.id.stepper_choose_company)
        private val step2: VerticalStepperItemView = view.findViewById(R.id.stepper_find_package)

        private val numberEdit: TextInputEditText = view.findViewById(R.id.number_edit)
        private val step0NextButton: Button = view.findViewById(R.id.step_0_next_button)

        private val currentCompanyText: TextView = view.findViewById(R.id.tv_current_company)
        private val step1NextButton: Button = view.findViewById(R.id.choose_company_next_btn)
        private val detectingLayout: View = view.findViewById(R.id.detecting_layout)
        private val detectErrorView: View = view.findViewById(R.id.error_text)
        private val detectTryAgainButton: Button = view.findViewById(R.id.stepper_try_again)
        private val selectLayout: View = view.findViewById(R.id.select_layout)

        private val addErrorView: View = view.findViewById(R.id.error_layout_add)
        private val addErrorMsg: TextView = view.findViewById(R.id.add_error_message_text)
        private val addErrorDesc: TextView = view.findViewById(R.id.add_error_desc_text)
        private val addLoadingView: View = view.findViewById(R.id.loading_layout_add)
        private val addFinishLayout: View = view.findViewById(R.id.set_name_layout)
        private val nameEdit: TextInputEditText = view.findViewById(R.id.name_edit)

        private var currentStep = 0

        private var companyCode: String = ""

        private var result: Kuaidi100Package? = null

        private var number: String = ""

        private val activity: HomeActivity get() = this@HomeActivity

        init {
            VerticalStepperItemView.bindSteppers(step0, step1, step2)

            step0.isAnimationEnabled = false
            step1.isAnimationEnabled = false
            step2.isAnimationEnabled = false

            step0.summary = resources.string[R.string.stepper_detect_company_summary].format(number)
            step1.summary = when (SettingsInstance.packageApiTypeInt) {
                PackageApiType.BAIDU -> resources.string[R.string.summary_baidu_not_support_choose_company]
                else -> null
            }

            // Set up view in step0
            numberEdit.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {

                }

                override fun beforeTextChanged(s: CharSequence?,
                                               start: Int, count: Int, after: Int) {

                }

                override fun onTextChanged(s: CharSequence?,
                                           start: Int, before: Int, count: Int) {
                    step0NextButton.isEnabled = !s.isNullOrBlank()
                    number = s?.toString() ?: ""
                }
            })
            step0NextButton.isEnabled = !numberEdit.text.isNullOrBlank()
            step0NextButton.setOnClickListener {
                number = numberEdit.text.toString()
                currentStep = 1
                step0.nextStep()
                doStep()
            }

            // Set up views in step1
            step1NextButton.setOnClickListener { step1.nextStep() }
            view.findViewById<Button>(R.id.choose_company_change_btn).setOnClickListener {
                val intent = Intent(activity, CompanyChooserActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
                startActivityForResult(intent, REQUEST_CODE_CHOOSE_COMPANY)
            }
            detectTryAgainButton.setOnClickListener { doStep() }
            view.findViewById<Button>(R.id.choose_company_next_btn).setOnClickListener {
                currentStep = 2
                step1.nextStep()
                doStep()
            }
            view.findViewById<Button>(R.id.back_button_step_1).setOnClickListener {
                currentStep = 0
                step1.prevStep()
                doStep()
            }
            view.findViewById<Button>(R.id.back_button_step_1_2).setOnClickListener {
                currentStep = 0
                step1.prevStep()
                doStep()
            }

            // Set up views in step 2
            view.findViewById<Button>(R.id.try_again_btn_step_2).setOnClickListener { doStep() }
            view.findViewById<Button>(R.id.back_button_step_2).setOnClickListener {
                if (SettingsInstance.packageApiTypeInt == PackageApiType.BAIDU) {
                    currentStep = 0
                    step1.prevStep()
                } else {
                    currentStep = 1
                    step2.prevStep()
                }
                doStep()
            }
            view.findViewById<Button>(R.id.back_button_step_2_2).setOnClickListener {
                if (SettingsInstance.packageApiTypeInt == PackageApiType.BAIDU) {
                    currentStep = 0
                    step1.prevStep()
                } else {
                    currentStep = 1
                    step2.prevStep()
                }
                doStep()
            }
            view.findViewById<Button>(R.id.stepper_add_button).setOnClickListener {
                result?.name = if (nameEdit.text!!.isNotBlank())
                    nameEdit.text.toString()
                else
                    String.format(getString(R.string.package_name_unnamed), if (number.length >= 4)
                        number.reversed().substring(0, 4).reversed() else number)
                ui {
                    detectErrorView.makeGone()
                    detectingLayout.makeVisible()

                    asyncIO {
                        packageDatabase.add(result!!)
                        packageDatabase.save()
                    }.await()

                    onPackageAdd(result!!)
                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN

                    setNumber("")
                    companyCode = ""
                    nameEdit.text = null
                    currentStep = 0
                    step0.state = VerticalStepperItemView.STATE_SELECTED
                    step1.state = VerticalStepperItemView.STATE_NORMAL
                    step2.state = VerticalStepperItemView.STATE_NORMAL
                    doStep()
                }
            }

            doStep()
        }

        private fun doStep() {
            when (currentStep) {
                0 -> {
                    // Clear last company result
                    companyCode = ""
                }
                1 -> {
                    step0.summary = resources.string[R.string.stepper_detect_company_summary]
                            .format(number)
                    if (!companyCode.isEmpty()) {
                        setCompany(companyCode)
                        detectErrorView.makeGone()
                        detectTryAgainButton.makeGone()
                        detectingLayout.makeGone()
                        selectLayout.makeVisible()
                    } else {
                        // Request a detection
                        if (ConnectivityReceiver.readNetworkState(activity)) {
                            ui {
                                detectErrorView.makeGone()
                                detectTryAgainButton.makeGone()
                                detectingLayout.makeVisible()
                                selectLayout.makeGone()

                                try {
                                    val company = KtPackageApi.detectCompany(number)

                                    step1.setErrorText(0)
                                    when (SettingsInstance.packageApiTypeInt) {
                                        PackageApiType.BAIDU -> {
                                            currentStep = 2
                                            step0.state = VerticalStepperItemView.STATE_DONE
                                            step1.nextStep()
                                            doStep()
                                        }
                                        else -> {
                                            detectErrorView.makeGone()
                                            detectTryAgainButton.makeGone()
                                            detectingLayout.makeGone()
                                            selectLayout.makeVisible()
                                        }
                                    }
                                    setCompany(company)
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    detectErrorView.makeVisible()
                                    detectTryAgainButton.makeVisible()
                                    detectingLayout.makeGone()
                                    selectLayout.makeGone()
                                }
                            }
                        } else {
                            step1.setErrorText(R.string.message_no_internet_connection)
                            detectErrorView.makeVisible()
                            detectTryAgainButton.makeVisible()
                            detectingLayout.makeGone()
                            selectLayout.makeGone()
                        }
                    }
                }
                2 -> {
                    if (ConnectivityReceiver.readNetworkState(activity)) {
                        ui {
                            addLoadingView.makeVisible()
                            addErrorView.makeGone()
                            addFinishLayout.makeGone()
                            step2.setErrorText(0)

                            val newResult = KtPackageApi.getPackage(number, companyCode)

                            if (newResult.code == BaseMessage.CODE_OKAY &&
                                    newResult.data?.getState() != Kuaidi100Package.STATUS_FAILED) {
                                addErrorView.makeGone()
                                addLoadingView.makeGone()
                                addFinishLayout.makeVisible()
                                result = newResult.data
                                view.findViewById<TextView>(R.id.add_message_text).text =
                                        resources.string[R.string.message_successful_format]
                                                .format(number, currentCompanyText.text)
                            } else {
                                addErrorView.makeVisible()
                                addLoadingView.makeGone()
                                addFinishLayout.makeGone()
                                addErrorMsg.setText(R.string.message_no_found)
                                addErrorDesc.setText(R.string.description_no_found)
                                step2.setErrorText(R.string.message_no_found)
                            }
                        }
                    } else {
                        addLoadingView.makeGone()
                        addErrorView.makeVisible()
                        addFinishLayout.makeGone()
                        addErrorMsg.setText(R.string.message_no_internet_connection)
                        addErrorDesc.setText(R.string.description_no_internet_connection)
                        step2.setErrorText(R.string.message_no_internet_connection)
                    }
                }
            }
        }

        internal fun setNumber(number: String) {
            this.number = number
            numberEdit.setText(number)
            step0NextButton.isEnabled = !number.isBlank()
        }

        internal fun setCompany(company: String) {
            this.companyCode = company
            currentCompanyText.text = if (company.isNotBlank())
                Kuaidi100PackageApi.CompanyInfo.getNameByCode(company)
            else
                resources.string[R.string.stepper_company_cannot_detect]
            step1NextButton.isEnabled = company.isNotBlank()
            step1.setErrorText(if (company.isBlank()) R.string.stepper_company_cannot_detect else 0)
        }

        fun onSaveInstanceState(outState: Bundle) {
            outState[STATE_ADD_PACKAGE_NUMBER] = number
            outState[STATE_ADD_PACKAGE_COMPANY] = companyCode
            outState.putString(STATE_ADD_PACKAGE_NAME, nameEdit.text?.toString())
        }

        fun onRestoreInstanceState(savedInstanceState: Bundle?) {
            if (savedInstanceState != null) {
                setNumber(savedInstanceState.getString(STATE_ADD_PACKAGE_NUMBER) ?: "")
                setCompany(savedInstanceState.getString(STATE_ADD_PACKAGE_COMPANY) ?: "")
                nameEdit.setText(savedInstanceState.getString(STATE_ADD_PACKAGE_NAME))
            }
        }

    }

}