package top.fksoft.mediaSyncPlayer.activity;

import android.Manifest;
import android.app.WallpaperManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import android.widget.TextView;
import org.litepal.LitePal;
import top.fksoft.mediaSyncPlayer.R;
import top.fksoft.mediaSyncPlayer.fragment.PlayListFragment;
import top.fksoft.mediaSyncPlayer.fragment.SoftPrefFragment;
import top.fksoft.mediaSyncPlayer.utils.AndroidUtils;
import top.fksoft.mediaSyncPlayer.utils.BitmapUtils;
import top.fksoft.mediaSyncPlayer.utils.base.MainBaseFragment;
import top.fksoft.test.android.dao.BaseActivity;

public class MainActivity extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener {
    private static final String TAG = "MainActivity";
    private static final String[] PERM_NAME = new String[]
            {
                    "获取当前网络状态",
                    "获取WIFI信息",
                    "读取设备外部存储空间",
                    "写入设备外部存储空间",
            };

    private static final String[] PERM = new String[]
            {
                    Manifest.permission.ACCESS_NETWORK_STATE,
                    Manifest.permission.ACCESS_WIFI_STATE,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
            };
    private Toolbar toolbar;
    private DrawerLayout drawerLayout;
    private NavigationView navView;
    private LinearLayout drawMenuLayout;
    private SharedPreferences softSet;
    private MainBaseFragment[] fragments = new MainBaseFragment[]{new PlayListFragment()};
    private MainBaseFragment fragment  = null;
    private TextView statusBar;
    private TextView navigationBar;


    @Override
    public void initData() {
        LitePal.getDatabase();


    }

    @Override
    public void initView() {
        softSet = SoftPrefFragment.getSharedPreferences(getContext());
        AndroidUtils.immersive(getContext());
        //沉浸体验
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        drawerLayout = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        //bind drawLayout end
        navView = findViewById(R.id.nav_view);
        navView.setNavigationItemSelectedListener(this);
        //listen menu end
        View headerView = navView.getHeaderView(0);
        drawMenuLayout = headerView.findViewById(R.id.nav_layout);
        sendPermissions(PERM_NAME, PERM);
        setFragment(fragments[0]);
        configStatus2Nav();
    }

    private void configStatus2Nav() {
        statusBar = findViewById(R.id.logo);
        navigationBar = findViewById(R.id.navigationBar);
        LinearLayout.LayoutParams statusParams = (LinearLayout.LayoutParams) statusBar.getLayoutParams();
        LinearLayout.LayoutParams navigationParams = (LinearLayout.LayoutParams) navigationBar.getLayoutParams();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            statusParams.height = AndroidUtils.getStatusBarHeight(getContext());
            navigationParams.height = AndroidUtils.getNavigationBarHeight(getContext());
            statusBar.setLayoutParams(statusParams);
            navigationBar.setLayoutParams(navigationParams);
            if (!softSet.getBoolean("status",false)) {
                statusBar.setVisibility(View.VISIBLE);
            }
            if (!softSet.getBoolean("navigation",false)) {
                navigationBar.setVisibility(View.VISIBLE);
            }

        }


    }

    private void setFragment(MainBaseFragment fragment){
        if (fragment == null)
            return;
        this.fragment = fragment;
        getSupportFragmentManager().beginTransaction().replace(R.id.mainFragment, fragment).commit();
        setTitle(fragment.title());
    }

    @Override
    public int initLayout() {
        return R.layout.activity_main;
    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        drawerLayout.closeDrawer(GravityCompat.START);
        switch (menuItem.getItemId()) {
            case R.id.nav_playList:
                setFragment(fragments[0]);
                break;
            case R.id.nav_setting:
                startActivity(new Intent(getContext(), SettingsActivity.class));
                break;
        }
        return true;
    }


    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(Gravity.START)){
            drawerLayout.closeDrawer(GravityCompat.START);
        }else {
            super.onBackPressed();
        }
    }

    @Override
    public void permissionSuccessful(int i) {//授权成功
        drawMenuLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    drawMenuLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this::onGlobalLayout);
                }else {
                    drawMenuLayout.getViewTreeObserver().removeGlobalOnLayoutListener(this::onGlobalLayout);
                }
                double width = drawMenuLayout.getWidth();
                double height = drawMenuLayout.getHeight();
                updateNavWallpaper(width/height);
            }
        });
        //刷新显示

        //初始化文件位置
    }

    private void updateNavWallpaper(double prop) { //显示左侧UI
        WallpaperManager wallpaperManager = WallpaperManager.getInstance(getContext());
        Drawable wallpaperDrawable = wallpaperManager.getDrawable();
        Bitmap bm = ((BitmapDrawable) wallpaperDrawable).getBitmap();
        Bitmap formatWallpaper = BitmapUtils.cropBitmap(bm, prop);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            drawMenuLayout.setBackground(new BitmapDrawable(formatWallpaper));
        } else {
            drawMenuLayout.setBackgroundDrawable(new BitmapDrawable(formatWallpaper));
        }
    } //显示壁纸

}
