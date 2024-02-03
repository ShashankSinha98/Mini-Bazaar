package com.example.minibazaar.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.view.GravityCompat;

import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.denzcoskun.imageslider.ImageSlider;
import com.denzcoskun.imageslider.constants.ScaleTypes;
import com.denzcoskun.imageslider.models.SlideModel;
import com.example.minibazaar.NetworkSync.CheckInternetConnection;
import com.example.minibazaar.ProductsCategory.ShoesActivity;
import com.example.minibazaar.R;
import com.example.minibazaar.UserSession.UserSession;
import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetSequence;
import com.google.firebase.FirebaseApp;
import com.mikepenz.crossfadedrawerlayout.view.CrossfadeDrawerLayout;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.MiniDrawer;
import com.mikepenz.materialdrawer.interfaces.ICrossfader;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;
import com.mikepenz.materialdrawer.util.AbstractDrawerImageLoader;
import com.mikepenz.materialdrawer.util.DrawerImageLoader;
import com.mikepenz.materialdrawer.util.DrawerUIUtils;
import com.mikepenz.materialize.util.UIUtils;
import com.squareup.picasso.Picasso;
import com.webianks.easy_feedback.EasyFeedback;

import java.util.ArrayList;
import java.util.HashMap;

import es.dmoral.toasty.Toasty;

public class MainActivity extends AppCompatActivity {

    private UserSession session;
    private HashMap<String, String> user;
    private String name, email, photo, mobile;
    private ImageSlider imageSlider;
    private Drawer result;
    private CrossfadeDrawerLayout crossfadeDrawerLayout = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FirebaseApp.initializeApp(MainActivity.this);

        //check Internet Connection
        new CheckInternetConnection(this).checkConnection();

        session = new UserSession(getApplicationContext());
        Log.d("xlr8_wlv", String.valueOf(session.getWishlistValue()));

        Typeface typeface = ResourcesCompat.getFont(this, R.font.blacklist);
        TextView appname = findViewById(R.id.appname);
        appname.setTypeface(typeface);



        //retrieve session values and display on listviews
        getValues();

        inflateNavDrawer();

        //ImageSLider
        imageSlider = findViewById(R.id.slider);
        inflateImageSlider();


        if (session.getFirstTime()) {
            //tap target view
            tapview();
            session.setFirstTime(false);
        }


    }

    private void inflateNavDrawer() {

        //set Custom toolbar to activity -----------------------------------------------------------
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Create the AccountHeader ----------------------------------------------------------------

        DrawerImageLoader.init(new AbstractDrawerImageLoader() {
            @Override
            public void set(ImageView imageView, Uri uri, Drawable placeholder) {
                Picasso.get().load(uri).placeholder(placeholder).into(imageView);
            }
            @Override
            public void cancel(ImageView imageView) {
                Picasso.get().cancelRequest(imageView);
            }
        });


        //Profile Making
        IProfile profile = new ProfileDrawerItem()
                .withName(name)
                .withEmail(email)
                .withIcon(photo);

        AccountHeader headerResult = new AccountHeaderBuilder()
                .withActivity(this)
                .withHeaderBackground(R.drawable.gradient_background)
                .addProfiles(profile)
                .withCompactStyle(true)
                .build();

        //Adding nav drawer items ------------------------------------------------------------------
        PrimaryDrawerItem item1 = new PrimaryDrawerItem().withIdentifier(1).withName(R.string.home).withIcon(R.drawable.home);
        PrimaryDrawerItem item2 = new PrimaryDrawerItem().withIdentifier(2).withName(R.string.myprofile).withIcon(R.drawable.profile);
        PrimaryDrawerItem item3 = new PrimaryDrawerItem().withIdentifier(3).withName(R.string.wishlist).withIcon(R.drawable.wishlist);
        PrimaryDrawerItem item4 = new PrimaryDrawerItem().withIdentifier(4).withName(R.string.cart).withIcon(R.drawable.cart);
        PrimaryDrawerItem item6 = new PrimaryDrawerItem().withIdentifier(5).withName(R.string.order_history).withIcon(R.drawable.order_hist_icon);
        PrimaryDrawerItem item5 = new PrimaryDrawerItem().withIdentifier(5).withName(R.string.logout).withIcon(R.drawable.logout);

        SecondaryDrawerItem item7 = new SecondaryDrawerItem().withIdentifier(7).withName("Offers").withIcon(R.drawable.tag);
        SecondaryDrawerItem item8 = new SecondaryDrawerItem().withIdentifier(8).withName(R.string.aboutapp).withIcon(R.drawable.credits);
        SecondaryDrawerItem item9 = new SecondaryDrawerItem().withIdentifier(9).withName(R.string.feedback).withIcon(R.drawable.feedback);
        SecondaryDrawerItem item10 = new SecondaryDrawerItem().withIdentifier(10).withName(R.string.helpcentre).withIcon(R.drawable.helpccenter);

        SecondaryDrawerItem item12 = new SecondaryDrawerItem().withIdentifier(12).withName("App Tour").withIcon(R.drawable.tour);
        SecondaryDrawerItem item13 = new SecondaryDrawerItem().withIdentifier(13).withName("Explore").withIcon(R.drawable.explore);


        //creating navbar and adding to the toolbar ------------------------------------------------
        result = new DrawerBuilder()
                .withActivity(this)
                .withToolbar(toolbar)
                .withHasStableIds(true)
                .withDrawerLayout(R.layout.crossfade_drawer)
                .withAccountHeader(headerResult)
                .withDrawerWidthDp(72)
                .withGenerateMiniDrawer(true)
                .withTranslucentStatusBar(true)
                .withActionBarDrawerToggleAnimated(true)
                .addDrawerItems(item1,item2,item3,item4,item6,item5, new DividerDrawerItem(), item7, item8, item9, item10,new DividerDrawerItem(),item12,item13).withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {

                        switch (position){

                            case 1:
                                if (result != null && result.isDrawerOpen()) {
                                    result.closeDrawer();
                                }
                                break;
                            case 2:
                                result.closeDrawer();
                                startActivity(new Intent(MainActivity.this, ProfileActivity.class));
                                break;
                            case 3:
                                Log.d("xlr8_wl","WLA Called");
                                result.closeDrawer();
                                startActivity(new Intent(MainActivity.this, WishlistActivity.class));
                                break;
                            case 4:
                                startActivity(new Intent(MainActivity.this, CartActivity.class));
                                result.closeDrawer();
                                break;

                            case 5:
                                startActivity(new Intent(MainActivity.this, OrderHistoryActivity.class));
                                result.closeDrawer();
                                break;

                            case 6:
                                session.logoutUser();
                                finish();
                                break;

                            case 8:
                                //startActivity(new Intent(MainActivity.this, NotificationActivity.class));
                                break;

                            case 10:
                                new EasyFeedback.Builder(MainActivity.this)
                                        .withEmail("sinhashashank.98@gmail.com")
                                        .withSystemInfo()
                                        .build()
                                        .start();
                                break;


                            case 13:
                                session.setFirstTimeLaunch(true);
                                result.closeDrawer();
                                startActivity(new Intent(MainActivity.this, WelcomeActivity.class));
                                finish();
                                break;

                            case 14:
                                if (result != null && result.isDrawerOpen()) {
                                    result.closeDrawer();
                                }
                                tapview();
                                break;
                            default:
                                Toast.makeText(MainActivity.this, "Default", Toast.LENGTH_LONG).show();
                        }

                        return true;
                    }
                }).build();

        //Setting crossfader drawer------------------------------------------------------------

        crossfadeDrawerLayout = (CrossfadeDrawerLayout) result.getDrawerLayout();

        //define maxDrawerWidth
        crossfadeDrawerLayout.setMaxWidthPx(DrawerUIUtils.getOptimalDrawerWidth(this));

        //add second view (which is the miniDrawer)
        final MiniDrawer miniResult = result.getMiniDrawer();

        //build the view for the MiniDrawer
        View view = miniResult.build(this);

        //set the background of the MiniDrawer as this would be transparent
        view.setBackgroundColor(UIUtils.getThemeColorFromAttrOrRes(this, com.mikepenz.materialdrawer.R.attr.material_drawer_background, com.mikepenz.materialdrawer.R.color.material_drawer_background));

        //we do not have the MiniDrawer view during CrossfadeDrawerLayout creation so we will add it here
        crossfadeDrawerLayout.getSmallView().addView(view, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        //define the crossfader to be used with the miniDrawer. This is required to be able to automatically toggle open / close
        miniResult.withCrossFader(new ICrossfader() {
            @Override
            public void crossfade() {
                boolean isFaded = isCrossfaded();
                crossfadeDrawerLayout.crossfade(400);

                //only close the drawer if we were already faded and want to close it now
                if (isFaded) {
                    result.getDrawerLayout().closeDrawer(GravityCompat.START);
                }
            }

            @Override
            public boolean isCrossfaded() {
                return crossfadeDrawerLayout.isCrossfaded();
            }
        });
    }








    private void inflateImageSlider() {

        ArrayList<SlideModel> slideModels = new ArrayList<>();
        slideModels.add(new SlideModel("https://m.media-amazon.com/images/G/31/img19/Wireless/Apple/iPhone11/RiverImages/11Pro/IN_iPhone11Pro_DESKTOP_01._CB437064827_.jpg", ScaleTypes.FIT));
        slideModels.add(new SlideModel("https://piunikaweb.com/wp-content/uploads/2019/08/oneplus_7_pro_5g_experience_the_power_of_5g_banner-750x354.jpg", ScaleTypes.FIT));
        slideModels.add(new SlideModel("https://lh3.googleusercontent.com/RSyeouwiFX4XVq6iw3H94al0VcXD693tBy2MxhBKCxAHCIfIpdt7wDV47_j2HanPSnTli7JgZ0fYHxESjz0uvVgeCBT3=w1000", ScaleTypes.FIT));
        slideModels.add(new SlideModel("https://cdn.metrobrands.com/mochi/media/images/content/Homepage/HOTTMARZZ-BANNER-MOCHI.webp", ScaleTypes.FIT));
        slideModels.add(new SlideModel("https://i.pinimg.com/originals/b2/78/7c/b2787cea792bff7d2c33e26ada6436bb.jpg", ScaleTypes.FIT));
        slideModels.add(new SlideModel("https://cdnb.artstation.com/p/assets/images/images/016/802/459/large/shuja-shuaib-banner.jpg?1553535424", ScaleTypes.FIT));

        imageSlider.setImageList(slideModels, ScaleTypes.FIT);

    }

    private void getValues() {

        //validating session
        session.isLoggedIn();

        //get User details if logged in
        user = session.getUserDetails();

        name = user.get(UserSession.KEY_NAME);
        email = user.get(UserSession.KEY_EMAIL);
        mobile = user.get(UserSession.KEY_MOBiLE);
        photo = user.get(UserSession.KEY_PHOTO);
    }


    private void tapview() {

        new TapTargetSequence(this)
                .targets(
                        TapTarget.forView(findViewById(R.id.notifintro), "Notifications", "Latest offers will be available here !")
                                .targetCircleColor(R.color.colorAccent)
                                .titleTextColor(R.color.colorAccent)
                                .titleTextSize(25)
                                .descriptionTextSize(15)
                                .descriptionTextColor(R.color.accent)
                                .drawShadow(true)                   // Whether to draw a drop shadow or not
                                .cancelable(false)                  // Whether tapping outside the outer circle dismisses the view
                                .tintTarget(true)
                                .transparentTarget(true)
                                .outerCircleColor(R.color.first),
                        TapTarget.forView(findViewById(R.id.view_profile), "Profile", "You can view and edit your profile here !")
                                .targetCircleColor(R.color.colorAccent)
                                .titleTextColor(R.color.colorAccent)
                                .titleTextSize(25)
                                .descriptionTextSize(15)
                                .descriptionTextColor(R.color.accent)
                                .drawShadow(true)                   // Whether to draw a drop shadow or not
                                .cancelable(false)                  // Whether tapping outside the outer circle dismisses the view
                                .tintTarget(true)
                                .transparentTarget(true)
                                .outerCircleColor(R.color.third),
                        TapTarget.forView(findViewById(R.id.cart), "Your Cart", "Here is Shortcut to your cart !")
                                .targetCircleColor(R.color.colorAccent)
                                .titleTextColor(R.color.colorAccent)
                                .titleTextSize(25)
                                .descriptionTextSize(15)
                                .descriptionTextColor(R.color.accent)
                                .drawShadow(true)
                                .cancelable(false)// Whether tapping outside the outer circle dismisses the view
                                .tintTarget(true)
                                .transparentTarget(true)
                                .outerCircleColor(R.color.second),
                        TapTarget.forView(findViewById(R.id.visitingcards), "Categories", "Product Categories have been listed here !")
                                .targetCircleColor(R.color.colorAccent)
                                .titleTextColor(R.color.colorAccent)
                                .titleTextSize(25)
                                .descriptionTextSize(15)
                                .descriptionTextColor(R.color.accent)
                                .drawShadow(true)
                                .cancelable(false)// Whether tapping outside the outer circle dismisses the view
                                .tintTarget(true)
                                .transparentTarget(true)
                                .outerCircleColor(R.color.fourth))
                .listener(new TapTargetSequence.Listener() {
                    // This listener will tell us when interesting(tm) events happen in regards
                    // to the sequence
                    @Override
                    public void onSequenceFinish() {
                        //session.setFirstTime(false);
                        Toasty.success(MainActivity.this, " You are ready to go !", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onSequenceStep(TapTarget lastTarget, boolean targetClicked) {

                    }

                    @Override
                    public void onSequenceCanceled(TapTarget lastTarget) {
                        // Boo
                    }
                }).start();

    }


    public void Notifications(View view) {
        //startActivity(new Intent(MainActivity.this, NotificationActivity.class));
    }

    public void viewProfile(View view) {
        startActivity(new Intent(MainActivity.this, ProfileActivity.class));
    }

    public void cardsActivity(View view) {
        startActivity(new Intent(MainActivity.this, ShoesActivity.class));
    }

    public void viewCart(View view) {
        startActivity(new Intent(MainActivity.this, CartActivity.class));
    }

    public void tshirtActivity(View view) {
        //startActivity(new Intent(MainActivity.this, Tshirts.class));
    }

    public void bagsActivity(View view) {

        //startActivity(new Intent(MainActivity.this, Bags.class));
    }

    public void stationaryAcitivity(View view) {

        //startActivity(new Intent(MainActivity.this, Stationary.class));
    }

    public void calendarsActivity(View view) {

        //startActivity(new Intent(MainActivity.this, Calendars.class));
    }

    public void keychainsActivity(View view) {

        //startActivity(new Intent(MainActivity.this, Keychains.class));
    }
}
