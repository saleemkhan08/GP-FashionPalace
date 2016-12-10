package com.thnki.gp.fashion.palace.utils;

import android.net.Uri;
import android.util.Log;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.thnki.gp.fashion.palace.Brandfever;
import com.thnki.gp.fashion.palace.R;
import com.thnki.gp.fashion.palace.models.Accounts;
import com.thnki.gp.fashion.palace.models.Category;
import com.thnki.gp.fashion.palace.singletons.Otto;
import com.thnki.gp.fashion.palace.singletons.VolleyUtil;

import static com.thnki.gp.fashion.palace.interfaces.Const.AVAILABLE_;
import static com.thnki.gp.fashion.palace.interfaces.Const.AVAILABLE_FASHION_ACCESSORIES;
import static com.thnki.gp.fashion.palace.interfaces.Const.AVAILABLE_FIRST_LEVEL_CATEGORIES;
import static com.thnki.gp.fashion.palace.interfaces.Const.AVAILABLE_HOME_FURNISHING;
import static com.thnki.gp.fashion.palace.interfaces.Const.AVAILABLE_KIDS_WEAR;
import static com.thnki.gp.fashion.palace.interfaces.Const.AVAILABLE_MENS_WEAR;
import static com.thnki.gp.fashion.palace.interfaces.Const.AVAILABLE_WOMENS_WEAR;
import static com.thnki.gp.fashion.palace.interfaces.Const.FASHION_ACCESSORIES;
import static com.thnki.gp.fashion.palace.interfaces.Const.HOME_FURNISHING;
import static com.thnki.gp.fashion.palace.interfaces.Const.KIDS_WEAR;
import static com.thnki.gp.fashion.palace.interfaces.Const.MENS_WEAR;
import static com.thnki.gp.fashion.palace.interfaces.Const.WOMENS_WEAR;

public class InitialSetupUtil implements ValueEventListener
{
    public static final String APP_IMAGES = "appImages";
    private static final String FIRST_LEVEL_CATEGORIES = "firstLevelCategories";
    private static final String TAG = "InitialSetupUtil";
    private DatabaseReference mRootDbRef;
    private DatabaseReference mFirstLevelDbRef;
    public static final String INITIAL_SETUP_COMPLETE = "initialSetupComplete";

    public static void updateUi()
    {
        Log.d(TAG, "updateUi");
        new InitialSetupUtil();
    }

    private InitialSetupUtil()
    {
        mRootDbRef = FirebaseDatabase.getInstance().getReference();
        mFirstLevelDbRef = mRootDbRef.child(FIRST_LEVEL_CATEGORIES);
        mFirstLevelDbRef.addValueEventListener(this);
    }

    private void saveCategories()
    {
        Log.d(TAG, "saveCategories");
        //Child 1
        DatabaseReference appDataDbRef = mRootDbRef.child(UserUtil.APP_DATA);
        appDataDbRef.child(VolleyUtil.REQUEST_HANDLER_URL).setValue(Brandfever.getResString(R.string.defaultUrl));
        appDataDbRef.child(VolleyUtil.APP_ID).setValue(Brandfever.getResString(R.string.serverKey));

        //Child 2
        DatabaseReference ownersDbRef = mRootDbRef.child(Accounts.OWNERS).child("0");
        ownersDbRef.setValue("111955688396506807880");

        //Child 3 & 4
        String[] firstLevelCategories = Brandfever.getResStringArray(R.array.firstLevelCategories);
        String[] firstLevelCategoriesId = Brandfever.getResStringArray(R.array.firstLevelCategoriesId);
        addToDatabase(firstLevelCategories, FIRST_LEVEL_CATEGORIES, firstLevelCategoriesId);
        addToDatabase(firstLevelCategories, AVAILABLE_FIRST_LEVEL_CATEGORIES, firstLevelCategoriesId);

        //Child 5 & 6
        String[] mensWear = Brandfever.getResStringArray(R.array.mensWear);
        String[] mensWearId = Brandfever.getResStringArray(R.array.mensWearId);
        addToDatabase(mensWear, MENS_WEAR, mensWearId);
        Log.d(TAG, "Test : " + AVAILABLE_MENS_WEAR);

        addToDatabase(mensWear, AVAILABLE_MENS_WEAR, mensWearId);
        //addToDatabase(mensWear, MENS_WEAR, mensWearId, true);

        //Child 7 & 8
        String[] womensWear = Brandfever.getResStringArray(R.array.womensWear);
        String[] womensWearId = Brandfever.getResStringArray(R.array.womensWearId);
        addToDatabase(womensWear, WOMENS_WEAR, womensWearId);
        Log.d(TAG, "Test : " + AVAILABLE_WOMENS_WEAR);
        addToDatabase(womensWear, AVAILABLE_WOMENS_WEAR, womensWearId);
        //addToDatabase(womensWear, WOMENS_WEAR, womensWearId, true);

        //Child 9 & 10
        String[] kidsWear = Brandfever.getResStringArray(R.array.kidsWear);
        String[] kidsWearId = Brandfever.getResStringArray(R.array.kidsWearId);
        addToDatabase(kidsWear, KIDS_WEAR, kidsWearId);
        Log.d(TAG, "Test : " + AVAILABLE_KIDS_WEAR);
        addToDatabase(kidsWear, AVAILABLE_KIDS_WEAR, kidsWearId);
        //addToDatabase(kidsWear, KIDS_WEAR, kidsWearId, true);

        //Child 11 & 12
        String[] fashionAccessories = Brandfever.getResStringArray(R.array.fashionAccessories);
        String[] fashionAccessoriesId = Brandfever.getResStringArray(R.array.fashionAccessoriesId);
        addToDatabase(fashionAccessories, FASHION_ACCESSORIES, fashionAccessoriesId);
        Log.d(TAG, "Test : " + AVAILABLE_FASHION_ACCESSORIES);
        addToDatabase(fashionAccessories, AVAILABLE_FASHION_ACCESSORIES, fashionAccessoriesId);
        //addToDatabase(fashionAccessories, FASHION_ACCESSORIES, fashionAccessoriesId, true);

        //Child 13 & 14
        String[] homeFurnishing = Brandfever.getResStringArray(R.array.homeFurnishing);
        String[] homeFurnishingId = Brandfever.getResStringArray(R.array.homeFurnishingId);
        addToDatabase(homeFurnishing, HOME_FURNISHING, homeFurnishingId);
        Log.d(TAG, "Test : " + AVAILABLE_HOME_FURNISHING);
        addToDatabase(homeFurnishing, AVAILABLE_HOME_FURNISHING, homeFurnishingId);
        //addToDatabase(homeFurnishing, HOME_FURNISHING, homeFurnishingId, true);
        Otto.post(INITIAL_SETUP_COMPLETE);
        Log.d(TAG, "updateUi");
    }

    private void addToDatabase(final String[] categories, String name, final String[] childIds)
    {
        Log.d(TAG, "addToDatabase : " + name);
        DatabaseReference mCategoriesRef = mRootDbRef.child(name);
        for (int i = 0; i < categories.length; i++)
        {
            Log.d(TAG, "categories[" + i + "] : " + categories[i] + ", childIds[" + i + "] : " + childIds[i]);
            saveIndividualCategory(mCategoriesRef, i, name.replace(AVAILABLE_, ""), categories[i], childIds[i]);
        }
    }

    private void saveIndividualCategory(final DatabaseReference mCategoriesRef, final int index, final String parentCategory, final String categoryName, final String childId)
    {
        Log.d(TAG, "saveIndividualCategory : mCategoriesRef : " + mCategoriesRef
                + ", categoryName :" + categoryName + ", childId : " + childId);
        StorageReference storageReference = FirebaseStorage.getInstance()
                .getReference().child(APP_IMAGES).child(parentCategory).child(childId + ".jpg");

        Log.d(TAG, "storageReference : " + storageReference);

        storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>()
        {
            @Override
            public void onSuccess(Uri uri)
            {
                try
                {
                        Log.d(TAG, "saveIndividualCategory : mCategoriesRef : " + mCategoriesRef
                            + ", categoryName :" + categoryName + ", childId : " + childId);

                    DatabaseReference childRef = mCategoriesRef.child(index + "");
                    Category category = new Category(categoryName, index, parentCategory, childId, uri.toString());
                    category.setCategorySelected(true);
                    childRef.setValue(category);
                }
                catch (Exception e)
                {
                    Log.d("Exception", e.getMessage());
                }
            }
        });
    }

    @Override
    public void onDataChange(DataSnapshot dataSnapshot)
    {
        try
        {
            Log.d(TAG, "onDataChange : dataSnapshot.getValue() : " + dataSnapshot.getValue());
            if (dataSnapshot.getValue() == null)
            {
                saveCategories();
            }
            else
            {
                Otto.post(INITIAL_SETUP_COMPLETE);
            }
            mFirstLevelDbRef.removeEventListener(this);
        }
        catch (Exception e)
        {
            Log.d("Exception", e.getMessage());
        }
    }

    @Override
    public void onCancelled(DatabaseError databaseError)
    {
        Log.d(TAG, "onCancelled : databaseError : " + databaseError);
    }
}