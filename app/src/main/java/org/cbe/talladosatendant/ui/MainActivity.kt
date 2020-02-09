package org.cbe.talladosatendant.ui

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.google.android.material.navigation.NavigationView
import org.cbe.talladosatendant.R


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    lateinit var drawerLayout: DrawerLayout
    lateinit var navView: NavigationView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        drawerLayout = findViewById(R.id.drawer_layout)
        navView = findViewById(R.id.nav_view)
        val toolbar : Toolbar = findViewById(R.id.app_toolbar)
        setSupportActionBar(toolbar)

        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar, 0, 0
        )
        toggle.isDrawerIndicatorEnabled= true
        toggle.isDrawerSlideAnimationEnabled= true

        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        navView.setNavigationItemSelectedListener(this)
        //supportActionBar?.setDisplayHomeAsUpEnabled(true)
       // supportActionBar?.setHomeButtonEnabled(true)

    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val f: Fragment?
        when(item.itemId){
            R.id.take_attendace_menu -> f= TakeAttendanceFragment()
            R.id.report_attendace_menu -> f= ReviewAttendanceFragment()
            R.id.export_attendace_menu -> f= ExportAttendanceFragment()
            else -> f = null
        }
        f?.let { replaceFragment(f) }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean { // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.app_bar_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_action_home -> {
                replaceFragment(MainFragment())
                return true
            }

            else -> {
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item)
            }
        }
    }

    fun replaceFragment(f: Fragment){
        supportFragmentManager.beginTransaction().replace(R.id.fl_main_container,f).commit()
    }
}
