package info.papdt.express.helper.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import info.papdt.express.helper.R
import info.papdt.express.helper.dao.PackageDatabase
import info.papdt.express.helper.dao.SRDatabase
import info.papdt.express.helper.event.EventCallbacks
import info.papdt.express.helper.event.EventIntents
import info.papdt.express.helper.ui.adapter.CategoriesListAdapter
import info.papdt.express.helper.ui.common.AbsActivity
import info.papdt.express.helper.ui.dialog.EditCategoryDialog
import info.papdt.express.helper.ui.items.CategoryItemViewBinder
import moe.feng.kotlinyan.common.*

class ManageCategoriesActivity : AbsActivity() {

    companion object {

        fun launch(activity: Activity, requestCode: Int) {
            val intent = Intent(activity, ManageCategoriesActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
            activity.startActivityForResult(intent, requestCode)
        }

    }

    private val listView: RecyclerView by lazyFindNonNullView(android.R.id.list)
    private val emptyView: LinearLayout by lazyFindNonNullView(R.id.empty_view)

    private val adapter: CategoriesListAdapter = CategoriesListAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_categories)
    }

    override fun setUpViews() {
        listView.adapter = adapter
        adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onChanged() {
                emptyView.visibility = if (adapter.itemCount > 0) View.GONE else View.VISIBLE
            }
            override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
                onChanged()
            }
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                onChanged()
            }
        })

        ui {
            adapter.setCategories(asyncIO { SRDatabase.categoryDao.getAll() }.await())
        }
    }

    override fun onStart() {
        super.onStart()
        registerLocalBroadcastReceiver(EventCallbacks.onItemClick(CategoryItemViewBinder::class) {
            if (it != null) {
                EditCategoryDialog.newEditDialog(it).show(supportFragmentManager, "edit_dialog")
            }
        }, action = EventIntents.getItemOnClickActionName(CategoryItemViewBinder::class))
        registerLocalBroadcastReceiver(EventCallbacks.onSaveNewCategory {
            ui {
                val newList = asyncIO {
                    SRDatabase.categoryDao.add(it)
                    SRDatabase.categoryDao.getAll()
                }.await()
                adapter.setCategories(newList)
                setResult(RESULT_OK)
            }
        }, action = EventIntents.ACTION_SAVE_NEW_CATEGORY)
        registerLocalBroadcastReceiver(EventCallbacks.onSaveEditCategory { oldData, data -> ui {
            if (oldData.title == data.title) {
                // Its title hasn't been changed. Just update
                val newList = asyncIO {
                    SRDatabase.categoryDao.update(data)
                    SRDatabase.categoryDao.getAll()
                }.await()
                adapter.setCategories(newList)
                setResult(RESULT_OK)
            } else {
                // Title has been changed. Need update package list
                val newList = asyncIO {
                    val packDatabase = PackageDatabase.getInstance(this)
                    for (pack in packDatabase.data) {
                        if (pack.categoryTitle == oldData.title) {
                            pack.categoryTitle = data.title
                        }
                    }
                    packDatabase.save()
                    SRDatabase.categoryDao.delete(oldData)
                    SRDatabase.categoryDao.add(data)
                    SRDatabase.categoryDao.getAll()
                }.await()
                adapter.setCategories(newList)
                setResult(RESULT_OK)
            }
        } }, action = EventIntents.ACTION_SAVE_EDIT_CATEGORY)
        registerLocalBroadcastReceiver(EventCallbacks.onDeleteCategory {
            buildAlertDialog {
                titleRes = R.string.delete_category_dialog_title
                messageRes = R.string.delete_category_dialog_message
                okButton { _, _ ->
                    ui {
                        val newList = asyncIO {
                            SRDatabase.categoryDao.deleteWithUpdatingPackages(this, it)
                            SRDatabase.categoryDao.getAll()
                        }.await()
                        adapter.setCategories(newList)
                        setResult(RESULT_OK)
                    }
                }
                cancelButton()
            }.show()
        }, action = EventIntents.ACTION_REQUEST_DELETE_CATEGORY)
    }

    override fun onStop() {
        super.onStop()
        unregisterAllLocalBroadcastReceiver()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_manage_categories, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.action_new_category -> {
            EditCategoryDialog.newCreateDialog().show(supportFragmentManager, "create_dialog")
            true
        }
        R.id.action_reset_category -> {
            buildAlertDialog {
                titleRes = R.string.reset_default_category_dialog_title
                messageRes = R.string.reset_default_category_dialog_message
                okButton { _, _ ->
                    ui {
                        val newList = asyncIO {
                            SRDatabase.categoryDao.clearWithUpdatingPackages(this)
                            SRDatabase.categoryDao.addDefaultCategories(this)
                            SRDatabase.categoryDao.getAll()
                        }.await()
                        adapter.setCategories(newList)
                        setResult(RESULT_OK)
                    }
                }
                cancelButton()
            }.show()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

}