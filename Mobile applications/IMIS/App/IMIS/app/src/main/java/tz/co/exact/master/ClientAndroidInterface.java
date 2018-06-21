//Copyright (c) 2016-%CurrentYear% Swiss Agency for Development and Cooperation (SDC)
//
//The program users must agree to the following terms:
//
//Copyright notices
//This program is free software: you can redistribute it and/or modify it under the terms of the GNU AGPL v3 License as published by the 
//Free Software Foundation, version 3 of the License.
//This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of 
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU AGPL v3 License for more details www.gnu.org.
//
//Disclaimer of Warranty
//There is no warranty for the program, to the extent permitted by applicable law; except when otherwise stated in writing the copyright 
//holders and/or other parties provide the program "as is" without warranty of any kind, either expressed or implied, including, but not 
//limited to, the implied warranties of merchantability and fitness for a particular purpose. The entire risk as to the quality and 
//performance of the program is with you. Should the program prove defective, you assume the cost of all necessary servicing, repair or correction.
//
//Limitation of Liability 
//In no event unless required by applicable law or agreed to in writing will any copyright holder, or any other party who modifies and/or 
//conveys the program as permitted above, be liable to you for damages, including any general, special, incidental or consequential damages 
//arising out of the use or inability to use the program (including but not limited to loss of data or data being rendered inaccurate or losses 
//sustained by you or third parties or a failure of the program to operate with any other programs), even if such holder or other party has been 
//advised of the possibility of such damages.
//
//In case of dispute arising out or in relation to the use of the program, it is subject to the public law of Switzerland. The place of jurisdiction is Berne.

package tz.co.exact.master;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.icu.text.DecimalFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.widget.TextView;
import android.widget.Toast;

import com.exact.CallSoap.CallSoap;
import com.exact.InsureeImages;
import com.exact.general.General;
import com.exact.uploadfile.UploadFile;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.util.Date;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.Formatter;

import static android.database.sqlite.SQLiteDatabase.openOrCreateDatabase;
import static java.lang.Math.abs;

/**
 * Created by Hiren on 18/04/2017.
 */

public class ClientAndroidInterface {
    private Context mContext;

    private SQLHandler sqlHandler;
    private Global global;
    private int Uploaded;
    private HashMap<String, String> controls = new HashMap<>();
    private final String Path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/IMIS/";


    private File[] files;
    private int TotalFiles;
    private int UploadCounter; //
    private File XMLFile;
    private int result;
    private int UserId;
    private Date edate;
    public static boolean Activate = false;
    private int IsFamilyAvailable;
    private String payerId;
    private int DataDeleted;
    private int rtPolicyId = 0;
    private int rtPremiumId = 0;
    private int rtInsureeId = 0;
    private int rtEnrolledId = 0;
    private int enrol_result;
    Bitmap myBitmap;
    String resu;
    Bitmap theImage;

    SQLiteDatabase db;

    RecyclerView.LayoutManager myLayoutManger;
    EnrollmentReport enrollmentReport;

    StringBuffer buffer = new StringBuffer();
    Formatter formatter = new Formatter(buffer, Locale.US);


    private General general = new General(AppInformation.DomainInfo.getDomain());
    private ArrayList<String> mylist = new ArrayList<String>();


    ClientAndroidInterface(Context c) {
        mContext = c;
        sqlHandler = new SQLHandler(c);
        // activity = (Activity) c.getApplicationContext();
        getControls();
    }

    @JavascriptInterface
    public void SetUrl(String Url) {
        global = (Global) mContext.getApplicationContext();
        global.setCurrentUrl(Url);
    }

    private void getControls() {

        String tableName = "tblControls";
        String[] columns = {"FieldName", "Adjustibility"};
        String where = null;
        String OrderBy = null;

        JSONArray ctls = sqlHandler.getResult(tableName, columns, null, null);

        for (int i = 0; i < ctls.length(); i++) {
            try {
                JSONObject object = ctls.getJSONObject(i);
                String FieldName = object.getString("FieldName");
                String Adjustibility = object.getString("Adjustibility");
                controls.put(FieldName, Adjustibility);

            } catch (JSONException e) {
                e.printStackTrace();
            }


        }

    }

    @JavascriptInterface
    public String GetSystemImageFolder()
    {
        return global.getImageFolder();
    }

    @JavascriptInterface
    public String getControl(String ctl) {
        if(controls.get(ctl)==null){getControls();}
        return controls.get(ctl);
    }

    @JavascriptInterface
    public void ShowToast(String msg) {
        Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
    }

    @JavascriptInterface
    public AlertDialog ShowDialog(String msg) {
        return new AlertDialog.Builder(mContext)
                .setMessage(msg)
                .setCancelable(false)

                .setPositiveButton(R.string.Ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                }).show();
    }


    //    public int  getDialogResult(final String msg){
//        ShowDialogYesNo(msg);
//        while (inProgress){}
//        return  DialogResult;
//    }
    @JavascriptInterface
    public AlertDialog ShowDialogYesNo(final int InsureId, final int FamilyId, Boolean Activate, final int isOffline) {
        return new AlertDialog.Builder(mContext)
                .setMessage(R.string.ExceedThreshold)
                .setCancelable(false)
                .setNegativeButton(R.string.No, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SaveInsureePolicy(InsureId, FamilyId, false, isOffline);
                    }
                })
                .setPositiveButton(R.string.Yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        SaveInsureePolicy(InsureId, FamilyId, true, isOffline);
                    }
                }).show();
    }

    @JavascriptInterface
    public String getString(String str) {
        Resources resources = mContext.getResources();
        return resources.getString(resources.getIdentifier(str, "string", mContext.getPackageName()));
    }


    @JavascriptInterface
    public boolean isValidInsuranceNumber(String InsuranceNumber) {
        Escape escape = new Escape();
        int validInsuranceNumber = escape.CheckInsuranceNumber(InsuranceNumber);
        if (validInsuranceNumber != 0){
            ShowDialog(mContext.getResources().getString(validInsuranceNumber));
            return false;
        }
        return true;
    }

    //get Region Without Officer
    @JavascriptInterface
    public String getRegionsWO() {
        global = (Global) mContext.getApplicationContext();
        String Query = "SELECT LocationId,LocationName FROM tblLocations WHERE LocationType = 'R'";
        JSONArray Regions = sqlHandler.getResult(Query, null);

        return Regions.toString();
    }

    //get District Without Officer
    @JavascriptInterface
    public String getDistrictsWO(int RegionId) {

        String Query = "SELECT  LocationId,LocationName FROM tblLocations \n" +
                "WHERE LocationType = 'D' AND ParentLocationId = " + RegionId;
        JSONArray Districts = sqlHandler.getResult(Query, null);
        return Districts.toString();
    }


    @JavascriptInterface
    public String getRegions() {
        global = (Global) mContext.getApplicationContext();
        String Query = "SELECT LocationId, LocationName FROM tblLocations WHERE LocationId = (SELECT L.ParentLocationId LocationId FROM tblLocations L\n" +
                "INNER JOIN tblOfficer O ON L.LocationId = O.LocationId\n" +
                "WHERE OfficerId = " + global.getOfficerId() + ")";
        JSONArray Regions = sqlHandler.getResult(Query, null);

        return Regions.toString();
    }

    @JavascriptInterface
    public String getDistricts(int RegionId) {
        global = (Global) mContext.getApplicationContext();
//        String tableName = "tblLocations";
//        String[] columns = {"LocationId", "LocationName"};
//        String where = "LocationType = 'D' AND ParentLocationId = " + RegionId;
        String Query = "SELECT * FROM tblLocations L\n" +
                "INNER JOIN tblOfficer O ON L.LocationId = O.LocationId\n" +
                "WHERE OfficerId = " + global.getOfficerId() + " AND LocationType = 'D' AND ParentLocationId = " + RegionId;
        JSONArray Districts = sqlHandler.getResult(Query, null);
        return Districts.toString();
    }

    @JavascriptInterface
    public String getDistricts() {
        String tableName = "tblLocations";
        String[] columns = {"LocationId", "LocationName"};
        String where = "LocationType = 'D'";

        JSONArray Districts = sqlHandler.getResult(tableName, columns, where, null);

        return Districts.toString();
    }

    @JavascriptInterface
    public String getWards(int DistrictId) {
        String tableName = "tblLocations";
        String[] columns = {"LocationId", "LocationName"};
        String where = "LocationType = 'W' AND ParentLocationId = " + DistrictId;

        JSONArray Wards = sqlHandler.getResult(tableName, columns, where, null);

        return Wards.toString();
    }

    @JavascriptInterface
    public String getVillages(int WardId) {
        String tableName = "tblLocations";
        String[] columns = {"LocationId", "LocationName"};
        String where = "LocationType = 'V' AND ParentLocationId = " + WardId;

        JSONArray Villages = sqlHandler.getResult(tableName, columns, where, null);

        return Villages.toString();
    }

    @JavascriptInterface
    public String getYesNo() {
        JSONArray YesNo = new JSONArray();


        try {
            JSONObject object = new JSONObject();
            object.put("key", mContext.getResources().getString(R.string.Yes));
            object.put("value", 1);
            YesNo.put(object);

            object = new JSONObject();
            object.put("key", mContext.getResources().getString(R.string.No));
            object.put("value", 0);
            YesNo.put(object);


        } catch (JSONException e) {
            e.printStackTrace();
        }


        return YesNo.toString();
    }

    @JavascriptInterface
    public String getConfirmationTypes() {
        String tableName = "tblConfirmationTypes";
        String[] columns = {"ConfirmationTypeCode", "ConfirmationType", "AltLanguage"};
        String where = null;
        String OrderBy = "SortOrder";

        JSONArray ConfirmationTypes = sqlHandler.getResult(tableName, columns, null, OrderBy);

        return ConfirmationTypes.toString();
    }

    @JavascriptInterface
    public String getGroupTypes() {
        String tableName = "tblFamilyTypes";
        String[] columns = {"FamilyTypeCode", "FamilyType", "AltLanguage"};
        String where = null;
        String OrderBy = "SortOrder";

        JSONArray GroupTypes = sqlHandler.getResult(tableName, columns, null, OrderBy);

        return GroupTypes.toString();
    }

    @JavascriptInterface
    public String getGender() {
        JSONArray Gender = new JSONArray();

        JSONObject object = new JSONObject();
        try {
            object.put("Code", "");
            object.put("Gender", mContext.getResources().getString(R.string.SelectGender));
            Gender.put(object);

            object = new JSONObject();
            object.put("Code", "M");
            object.put("Gender", mContext.getResources().getString(R.string.Male));
            Gender.put(object);

            object = new JSONObject();
            object.put("Code", "F");
            object.put("Gender", mContext.getResources().getString(R.string.Female));
            Gender.put(object);

            object = new JSONObject();
            object.put("Code", "O");
            object.put("Gender", mContext.getResources().getString(R.string.Other));
            Gender.put(object);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return Gender.toString();
    }

    @JavascriptInterface
    public String getMaritalStatus() {
        JSONArray maritalStatus = new JSONArray();
        JSONObject object = new JSONObject();

        try {
            object.put("Code", "");
            object.put("Status", mContext.getResources().getString(R.string.SelectMaritalStatus));
            maritalStatus.put(object);

            object = new JSONObject();
            object.put("Code", "M");
            object.put("Status", mContext.getResources().getString(R.string.Married));
            maritalStatus.put(object);

            object = new JSONObject();
            object.put("Code", "S");
            object.put("Status", mContext.getResources().getString(R.string.Single));
            maritalStatus.put(object);

            object = new JSONObject();
            object.put("Code", "D");
            object.put("Status", mContext.getResources().getString(R.string.Divorced));
            maritalStatus.put(object);

            object = new JSONObject();
            object.put("Code", "W");
            object.put("Status", mContext.getResources().getString(R.string.Widowed));
            maritalStatus.put(object);

            object = new JSONObject();
            object.put("Code", "N");
            object.put("Status", mContext.getResources().getString(R.string.NotSpecified));
            maritalStatus.put(object);


        } catch (JSONException e) {
            e.printStackTrace();
        }

        return maritalStatus.toString();
    }

    @JavascriptInterface
    public String getProfessions() {
        String tableName = "tblProfessions";
        String[] columns = {"ProfessionId", "Profession", "AltLanguage"};
        String where = null;
        String OrderBy = "SortOrder";

        JSONArray Professions = sqlHandler.getResult(tableName, columns, null, OrderBy);

        return Professions.toString();
    }

    @JavascriptInterface
    public String getEducations() {
        String tableName = "tblEducations";
        String[] columns = {"EducationId", "Education", "AltLanguage"};
        String where = null;
        String OrderBy = "SortOrder";

        JSONArray Educations = sqlHandler.getResult(tableName, columns, null, OrderBy);

        return Educations.toString();
    }

    @JavascriptInterface
    public String getIdentificationTypes() {
        String tableName = "tblIdentificationTypes";
        String[] columns = {"IdentificationCode", "IdentificationTypes", "AltLanguage"};
        String where = null;
        String OrderBy = "SortOrder";

        JSONArray Educations = sqlHandler.getResult(tableName, columns, null, OrderBy);

        return Educations.toString();
    }

    @JavascriptInterface
    public String getRelationships() {
        String tableName = "tblRelations";
        String[] columns = {"RelationId", "Relation", "AltLanguage"};
        String where = null;
        String OrderBy = "SortOrder";

        JSONArray Relations = sqlHandler.getResult(tableName, columns, null, OrderBy);

        return Relations.toString();
    }

    @JavascriptInterface
    public String getHFLevels() {
        JSONArray HFLevels = new JSONArray();
        JSONObject object = new JSONObject();

        try {
            object.put("Code", "");
            object.put("HFLevel", mContext.getResources().getString(R.string.SelectHFLevel));
            HFLevels.put(object);

            object = new JSONObject();
            object.put("Code", "D");//Uploaded = 1;
            object.put("HFLevel", mContext.getResources().getString(R.string.Dispensary));
            HFLevels.put(object);

            object = new JSONObject();
            object.put("Code", "C");
            object.put("HFLevel", mContext.getResources().getString(R.string.HealthCentre));
            HFLevels.put(object);

            object = new JSONObject();
            object.put("Code", "H");
            object.put("HFLevel", mContext.getResources().getString(R.string.Hospital));
            HFLevels.put(object);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return HFLevels.toString();
    }

    @JavascriptInterface
    public String getHF(int DistrictId, String HFLevel) {
        String Query = "SELECT HFID,  HFCode ||\" : \"||  HFName HF FROM tblHF WHERE LocationId = ? AND HFLevel = ?";
        String[] args = {String.valueOf(DistrictId), HFLevel};

        JSONArray HFs = sqlHandler.getResult(Query, args);

        return HFs.toString();
    }

    private HashMap<String, String> jsonToTable(String jsonString) {
        HashMap<String, String> data = new HashMap<>();
        try {
            JSONArray array = new JSONArray(jsonString);
            for (int i = 0; i < array.length(); i++) {
                JSONObject object = array.getJSONObject(i);
                String ControlName = object.getString("id");
                String ControlValue;
                if (object.getString("value") != "null")
                    ControlValue = object.getString("value");
                else
                    ControlValue = null;
                data.put(ControlName, ControlValue);

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return data;

    }

    @JavascriptInterface
    public int SaveFamily(String FamilyData, String InsureeData) {

        int FamilyId = 0;
        int InsureeId = 0;
        int MaxFamilyId = 0;

        try {
            global = (Global) mContext.getApplicationContext();
            String MaxFamilyIdQuery = "SELECT  IFNULL(COUNT(FamilyId),0)+1  FamilyId  FROM tblFamilies";
            JSONArray JsonMaxFamily = sqlHandler.getResult(MaxFamilyIdQuery, null);
            try {
                JSONObject JmaxFamilyOb = JsonMaxFamily.getJSONObject(0);
                MaxFamilyId = JmaxFamilyOb.getInt("FamilyId");

            } catch (JSONException e) {
                e.printStackTrace();
            }

            if (InsureeData.length() > 0) {
                int validation = isValidInsureeData(jsonToTable(InsureeData));
                if (validation > 0) {
                    throw new UserException(mContext.getResources().getString(validation));
                }
            }

            //Insert Family
            //===============================================================================
            HashMap<String, String> data = jsonToTable(FamilyData);
            ContentValues values = new ContentValues();


            FamilyId = Integer.parseInt(data.get("hfFamilyId"));

            int LocationId = Integer.parseInt(data.get("ddlVillage"));

            Boolean Poverty = null;
            if (!TextUtils.isEmpty(data.get("ddlPovertyStatus"))){
                if(data.get("ddlPovertyStatus").equals("1")){
                    Poverty = true;
                }else {
                    Poverty = false;
                }
            }
//            if (Boolean.parseBoolean(data.get("ddlPovertyStatus")) || !Boolean.parseBoolean(data.get("ddlPovertyStatus")))
//                Poverty = Boolean.parseBoolean(data.get("ddlPovertyStatus"));

            String FamilyType = null;
            if (!TextUtils.isEmpty(data.get("ddlGroupType")) && !data.get("ddlGroupType").equals("0"))
                FamilyType = data.get("ddlGroupType");

            String PermanentAddress = data.get("txtPermanentAddress");

            String Ethnicity = data.get("ddlEthnicity");

            String ConfirmationNo = data.get("txtConfirmationNo");
            String ConfirmationType = data.get("ddlConfirmationType");
            int isOffline = getFamilyStatus(FamilyId);  // Integer.parseInt(data.get("hfisOffline"));
            values.put("LocationId", LocationId);
            values.put("Poverty", Poverty);
            // if (isOffline == 2) isOffline = 0;
            values.put("isOffline", isOffline);
            values.put("FamilyType", FamilyType);
            values.put("FamilyAddress", PermanentAddress);
            values.put("Ethnicity", Ethnicity);
            values.put("ConfirmationNo", ConfirmationNo);
            values.put("ConfirmationType", ConfirmationType);

            if (FamilyId == 0) {
                values.put("FamilyId", MaxFamilyId);
                sqlHandler.insertData("tblFamilies", values);
                FamilyId = MaxFamilyId;
            } else {
                int Online = 2;
                if (isOffline == 0 || isOffline == 2) {
                    isOffline = 0;
                    Online = 2;
                }
                sqlHandler.updateData("tblFamilies", values, "FamilyId = ? AND (isOffline = ? OR isOffline = ?) ", new String[]{String.valueOf(FamilyId), String.valueOf(isOffline), String.valueOf(Online)});
                if (isOffline == 0 && global.getUserId() > 0) {
                    pd = new ProgressDialog(mContext);
                    pd = ProgressDialog.show(mContext, "", mContext.getResources().getString(R.string.Uploading));
                    final int FinalFamilyId = FamilyId;
                    new Thread() {
                        public void run() {
                            try {
                                Enrol(FinalFamilyId, 0, 0, 0, 0);
                            } catch (UserException e) {
                                e.printStackTrace();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            pd.dismiss();
                        }
                    }.start();

                }
            }
            if (InsureeData.length() > 0) {
                //Insert Insuree
                //==========================================================================================
                InsureeId = SaveInsuree(InsureeData, FamilyId, 1, -1);//herman new

                //Update insureeId in tblFamilies
                //==========================================================================================
                ContentValues cvUpdate = new ContentValues();
                cvUpdate.put("InsureeId", InsureeId);

                String[] whereArgs = {String.valueOf(FamilyId)};

                sqlHandler.updateData("tblFamilies", cvUpdate, "FamilyId= ?", whereArgs);
            }
            return FamilyId;

        } catch (UserException e) {
            e.printStackTrace();
            if (InsureeId != 0)
                sqlHandler.deleteData("tblInsuree", "InsureeId = ?", new String[]{String.valueOf(InsureeId)});
            if (FamilyId > 0 && InsureeData.length() > 0)
                sqlHandler.deleteData("tblFamilies", "FamilyId", new String[]{String.valueOf(FamilyId)});
            FamilyId = 0;
            ShowDialog(mContext.getResources().getString(R.string.ErrorOccurred));

        } catch (Exception e) {
            e.printStackTrace();

            if (InsureeId != 0)
                sqlHandler.deleteData("tblInsuree", "InsureeId = ?", new String[]{String.valueOf(InsureeId)});

            if (FamilyId > 0 && InsureeData.length() > 0)
                sqlHandler.deleteData("tblFamilies", "FamilyId = ?", new String[]{String.valueOf(FamilyId)});

            FamilyId = 0;
            ShowDialog(e.getMessage());
        }

        return FamilyId;
    }

    private int isValidInsureeData(HashMap<String, String> data) {
        int Result;

        String InsuranceNumber = data.get("txtInsuranceNumber");
        String InsureeId = data.get("hfInsureeId");
        String Query = "SELECT InsureeId FROM tblInsuree WHERE Trim(CHFID) = ? AND InsureeId <> ?";
        String args[] = {InsuranceNumber, InsureeId};
        JSONArray returnData = sqlHandler.getResult(Query, args);
        if (returnData.length() > 0) {
            Result = R.string.InsuranceNumberExists;
        } else {
            Result = 0;
        }
        return Result;
    }


    @JavascriptInterface
    public int SaveInsuree(String InsureeData, int FamilyId, int isHead, int ExceedThreshold) throws Exception {

        inProgress = true;

        int InsureeId = 0;
        int IsHeadSet = 0;
        int isOffline = 1;
        int insureeIsOffline = 1;
        int MaxInsureeId = 0;
        Boolean res = false;
        int newInsureeId = 0;
        try {
            global = (Global) mContext.getApplicationContext();
            HashMap<String, String> data = jsonToTable(InsureeData);

            int validation = isValidInsureeData(data);
            if (validation > 0) {
                ShowDialog(mContext.getResources().getString(validation));
                return 7;
                //throw new UserException(mContext.getResources().getString(validation)); //commented by Rogers
            }


            String MaxInsureeIdQuery = "SELECT  IFNULL(COUNT(InsureeId),0)+1  InsureeId  FROM tblInsuree";
            JSONArray JsonMaxInsuree = sqlHandler.getResult(MaxInsureeIdQuery, null);
            try {
                JSONObject JmaxInsureeOb = JsonMaxInsuree.getJSONObject(0);
                MaxInsureeId = JmaxInsureeOb.getInt("InsureeId");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            ContentValues values = new ContentValues();
            ContentValues FamilyValues = new ContentValues();

            InsureeId = Integer.parseInt(data.get("hfInsureeId"));
            rtInsureeId = InsureeId;
            if(data.get("hfisHead").equals("1") || data.get("hfisHead").equals("0")){
                if(data.get("hfisHead").equals("1")){
                    IsHeadSet = 1;
                }else{
                    IsHeadSet = 0;
                }
            }else {
                IsHeadSet = Integer.parseInt(data.get("hfisHead"));
            }


            String Marital = null;
            if (data.get("ddlMaritalStatus") != "" && data.get("ddlMaritalStatus") != null)
                Marital = data.get("ddlMaritalStatus");

            Boolean CardIssued = null;
            if (!TextUtils.isEmpty(data.get("ddlBeneficiaryCard"))){
                if(data.get("ddlBeneficiaryCard").equals("1")){
                    CardIssued = true;
                }else {
                    CardIssued = false;
                }

            }


            Integer Relation = null;
            if (!TextUtils.isEmpty(data.get("ddlRelationship")) && !data.get("ddlRelationship").equals("0"))
                Relation = Integer.valueOf(data.get("ddlRelationship"));

            Integer Profession = null;
            if (!TextUtils.isEmpty(data.get("ddlProfession")) && !data.get("ddlProfession").equals("0"))
                Profession = Integer.valueOf(data.get("ddlProfession"));

            Integer Education = null;
            if (!TextUtils.isEmpty(data.get("ddlEducation")) && !data.get("ddlEducation").equals("0"))
                Education = Integer.valueOf(data.get("ddlEducation"));

            String IdentificationType = "null";
            if (!TextUtils.isEmpty(data.get("ddlIdentificationType")) && !data.get("ddlIdentificationType").equals(""))
                IdentificationType = (data.get("ddlIdentificationType")).toString();

            String PhotoPath = data.get("hfImagePath");
            String newPhotoPath = data.get("hfNewPhotoPath");
            if (GetListOfImagesContain(data.get("txtInsuranceNumber")).length() > 0) {
                File file = new File(newPhotoPath);
                if (file.exists()) {
                    copyImageFromGalleryToApplication(newPhotoPath, data.get("txtInsuranceNumber"));
                    PhotoPath = GetListOfImagesContain(data.get("txtInsuranceNumber"));
                }
            } else {
                if (!String.valueOf(PhotoPath).equals(newPhotoPath))
                    try{
                        PhotoPath = copyImageFromGalleryToApplication(newPhotoPath, data.get("txtInsuranceNumber"));
                    }catch(Exception e){
                        //e.printStackTrace();
                    }

            }
            values.put("FamilyId", FamilyId);
            values.put("CHFID", data.get("txtInsuranceNumber"));
            values.put("LastName", data.get("txtLastName"));
            values.put("OtherNames", data.get("txtOtherNames"));
            values.put("DOB", data.get("txtBirthDate"));
            values.put("Gender", data.get("ddlGender"));
            values.put("Marital", Marital);
            if (IsHeadSet == -1) {
                values.put("isHead", isHead);
            } else {
                values.put("isHead", IsHeadSet);
                isHead = IsHeadSet;
            }
            isOffline = getFamilyStatus(FamilyId);
            insureeIsOffline = getInsureeStatus(InsureeId);
            //Integer.parseInt(data.get("hfIsOffline"));
            //isOffline = getInsureeStatus(InsureeId);
            values.put("IdentificationNumber", data.get("txtIdentificationNumber"));
            values.put("Phone", data.get("txtPhoneNumber"));
            if (isOffline == 0 || isOffline == 2)
                PhotoPath = PhotoPath.toString().substring(PhotoPath.toString().lastIndexOf("/") + 1);
            values.put("PhotoPath", PhotoPath);
            values.put("CardIssued", CardIssued);

            //values.put("isOffline", isOffline);
            values.put("Relationship", Relation);
            values.put("Profession", Profession);
            values.put("Education", Education);
            values.put("Email", data.get("txtEmail"));
            values.put("TypeOfId", IdentificationType);

            if (data.get("ddlFSP") != null)
                values.put("HFID", Integer.valueOf(data.get("ddlFSP")));
            values.put("CurrentAddress", data.get("txtCurrentAddress"));
            values.put("GeoLocation", "");
            if (data.get("ddlCurrentVillage") != null)
                values.put("CurVillage", Integer.valueOf(data.get("ddlCurrentVillage")));
//            if(isOffline == 1 || isOffline)


            if (rtInsureeId == 0) {//New Insuaree
                values.put("isOffline", 1);
                if (general.isNetworkAvailable(mContext) && isOffline == 0) {//Existing family
                    CallSoap cs = new CallSoap();
                    cs.setFunctionName("InsureeNumberExist");
                    //check if insuree exist online
                    res = cs.InsureeNumberExist(String.valueOf(data.get("txtInsuranceNumber")));

                    if(res == false){
                        if (isOffline == 0){
                            MaxInsureeId = -MaxInsureeId;
                            newInsureeId = MaxInsureeId;
                        }
                        values.put("InsureeId", MaxInsureeId);

                        sqlHandler.insertData("tblInsuree", values);
                        inProgress = false;
                        rtInsureeId = MaxInsureeId;
                        if (ExceedThreshold == 1)
                            ShowDialogYesNo(rtInsureeId, FamilyId, Activate, isOffline);
                        else if (ExceedThreshold == 0)
                            SaveInsureePolicy(rtInsureeId, FamilyId, true, isOffline);
                    }else{
                        String ErrMsg = null;
                        ErrMsg = "[" + String.valueOf(data.get("txtInsuranceNumber")) + "] " + mContext.getString(R.string.DuplicateInsuranceNumber);
                        ShowDialog(ErrMsg);

                        return 6;

                    }
                }else{//New Family
                    if (isOffline == 0){
                        MaxInsureeId = -MaxInsureeId;
                    }
                    values.put("InsureeId", MaxInsureeId);
                    sqlHandler.insertData("tblInsuree", values);
                    rtInsureeId = MaxInsureeId;
                    if (ExceedThreshold == 1)
                        ShowDialogYesNo(rtInsureeId, FamilyId, Activate, isOffline);
                    else if (ExceedThreshold == 0)
                        SaveInsureePolicy(rtInsureeId, FamilyId, true, isOffline);
                }

            } else {//Existing Insuree
                values.put("isOffline", insureeIsOffline);
                if (insureeIsOffline == 0 || insureeIsOffline == 2) {
                    //if(InsureeId > 0){
                        newInsureeId = -InsureeId;
                        values.put("InsureeId", newInsureeId);
                        if(isHead == 1){
                            FamilyValues.put("InsureeId", newInsureeId);
                        }


                    //}
                }
                sqlHandler.updateData("tblInsuree", values, "InsureeId = ? AND (isOffline = ?)", new String[]{String.valueOf(InsureeId), String.valueOf(insureeIsOffline)});
                if (insureeIsOffline == 0 || insureeIsOffline == 2) {
                    if(isHead == 1){
                        sqlHandler.updateData("tblFamilies", FamilyValues, "FamilyId = ? AND (isOffline = ?)", new String[]{String.valueOf(FamilyId), String.valueOf(isOffline)});
                    }

                }
            }

            if (isOffline == 0 && global.getUserId() > 0) {
                final int FinalFamilyId = FamilyId;
                final int FinalInsureeId = rtInsureeId;
                if(rtInsureeId > 0 ){
                    newInsureeId = -rtInsureeId;
                }else{
                    newInsureeId = rtInsureeId;
                }
                if(rtInsureeId == 0 && res == true ){
                    inProgress = false;
                } else{
                    try {
                        int ReturnValue = Enrol(FinalFamilyId, newInsureeId, 0, 0, 0);//fetches from sqlite database
                        //Update insureeId to positive becouse its already online now.
                        if(ReturnValue == 0){
                            if(newInsureeId < 0){
                                values.put("InsureeId", -newInsureeId);
                                if(isHead == 1){
                                    FamilyValues.put("InsureeId", -newInsureeId);
                                }
                            }else{
                                values.put("InsureeId", newInsureeId);
                                if(isHead == 1){
                                    FamilyValues.put("InsureeId", newInsureeId);
                                }
                            }

                            int ins = 0;
                            if(newInsureeId < 0){
                                ins = newInsureeId;
                            }else{
                                ins = -newInsureeId;
                            }

                            values.put("isOffline", 0);
                            sqlHandler.updateData("tblInsuree", values, "InsureeId = ?", new String[]{String.valueOf(ins)});
                            if(isHead == 1){
                                sqlHandler.updateData("tblFamilies", FamilyValues, "FamilyId = ? AND (isOffline = ?)", new String[]{String.valueOf(FamilyId), String.valueOf(isOffline)});
                            }
                        }
                        inProgress = false;
                    } catch (UserException e) {
                        e.printStackTrace();
                    }
                }

/*                new Thread() {
                    public void run() {
                        try {
                            rtInsureeId = Enrol(FinalFamilyId, FinalInsureeId, 0, 0, 0);
                            inProgress = false;
                        } catch (UserException e) {
                            e.printStackTrace();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }.start();*/
            } else{
                inProgress = false;
            }

        } catch (NumberFormatException e) {
            e.printStackTrace();
            throw new Exception(e.getMessage());
        } catch (UserException e) {
            e.printStackTrace();
            throw new Exception(e.getMessage());
        }
        while (inProgress) {
        }
        inProgress = false;
        return rtInsureeId;
    }

    private String copyImageFromGalleryToApplication(String selectedPath, String InsuranceNumber) {
        try {
            //Get current date and format it in yyyyMMdd format
            global = (Global) mContext.getApplicationContext();

            @SuppressLint("SimpleDateFormat") SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
            Calendar cal = Calendar.getInstance();
            String d = format.format(cal.getTime());

            String Extension = selectedPath.substring(selectedPath.lastIndexOf("."));
            //Resize the image before saving
//            InputStream inputStream = new FileInputStream(selectedPath);
            String outputFileName = global.getImageFolder() + InsuranceNumber + "_" + global.getOfficerCode() + "_" + d + "_0_0" + Extension;
//            OutputStream outputStream = new FileOutputStream(outputFileName);
//
//            byte[] buffer = new byte[1024];
//            int length = 0;
//
//            while ((length = inputStream.read(buffer)) > 0) {
//                outputStream.write(buffer, 0, length);
//            }
//            outputStream.flush();
//            outputStream.close();

            OutputStream outputStream = ResizeImage(selectedPath, outputFileName, 400);
                assert outputStream != null;
                outputStream.flush();
                outputStream.close();
                return outputFileName;


        } catch (IOException e) {
            e.printStackTrace();
        }

        return "";
    }
    @JavascriptInterface
    public AlertDialog deleteDialog(String msg, final int FamilyId) {
        return new AlertDialog.Builder(mContext)
                .setTitle(R.string.TitleDelete)
                .setMessage(msg)
                .setIcon(R.drawable.ic_about)
                .setCancelable(false)
                .setPositiveButton(R.string.Yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if(DeleteFamily(FamilyId) == 1){
                            ShowDialog(mContext.getResources().getString(R.string.FamilyDeleted));
                        }
                    }
                })
                .setNegativeButton(R.string.No, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //some code when you click No
                    }
                }).show();
    }
    @JavascriptInterface
    public void shutDownProgress(){
        inProgress = false;
    }

    @JavascriptInterface
    public int DeleteOnlineDataF(final int FamilyId){
        int res = 0;
        if(DeleteFamily(FamilyId) == 1){
            res = 1;
        }
        return res;
    }

    @JavascriptInterface
    public void informUser(){
        ShowDialog(mContext.getResources().getString(R.string.DeleteFamilyOnlyOffline));
    }

    @JavascriptInterface
    public String getAllFamilies() {
        String Query = "SELECT F.FamilyId, I.CHFID, I.OtherNames ||\" \"||  I.LastName InsureeName, R.LocationName RegionName, D.LocationName DistrictName, W.LocationName WardName, V.LocationName VillageName, F.isOffline \n" +
                "FROM tblFamilies F\n" +
                "INNER JOIN tblInsuree I ON I.InsureeId = F.InsureeId\n" +
                "INNER JOIN tblLocations V ON V.LocationId = F.LocationId\n" +
                "INNER JOIN tblLocations W ON W.LocationId = V.ParentLocationId\n" +
                "INNER JOIN tblLocations D ON D.LocationId = W.ParentLocationId\n" +
                "INNER JOIN tblLocations R ON R.LocationId = D.ParentLocationId";

        JSONArray Families = sqlHandler.getResult(Query, null);

        return Families.toString();
    }

    @JavascriptInterface
    //Get last segment value from url
    public String queryString(String url, String name) {
        Uri uri = Uri.parse(url);
        return uri.getQueryParameter(name);
    }

    @JavascriptInterface
    public String getInsuree(int InsureeId) {
        String Query = "SELECT InsureeId, FamilyId, CHFID, LastName, OtherNames, DOB, Gender, Marital, isHead, IdentificationNumber, Phone, isOffline , PhotoPath, CardIssued, Relationship, Profession, Education, Email, TypeOfId, I.HFID, CurrentAddress,R.LocationId CurRegion, D.LocationId CurDistrict, W.LocationId CurWard,  I.CurVillage, HFR.LocationId FSPRegion, HFD.LocationId FSPDistrict, HF.HFLevel FSPCategory\n" +
                "FROM tblInsuree I\n" +
                "LEFT OUTER JOIN tblLocations V ON V.LocationId = I.CurVillage\n" +
                "LEFT OUTER JOIN tblLocations W ON W.LocationId = V.ParentLocationId\n" +
                "LEFT OUTER JOIN tblLocations D ON D.LocationId = W.ParentLocationId\n" +
                "LEFT OUTER JOIN tblLocations R ON R.LocationId = D.ParentLocationId\n" +
                "LEFT OUTER JOIN tblHF HF ON HF.HFID = I.HFID\n" +
                "LEFT OUTER JOIN tblLocations HFD ON HFD.LocationId = HF.LocationId\n" +
                "LEFT OUTER JOIN tblLocations HFR ON HFR.LocationId = HFD.ParentLocationId\n" +
                "WHERE I.InsureeId = ?";

        String[] args = {String.valueOf(InsureeId)};

        JSONArray Insuree = sqlHandler.getResult(Query, args);

        return Insuree.toString();

    }

    @JavascriptInterface
    public String getInsureesForFamily(int FamilyId) {
        String Query = "SELECT I.InsureeId, I.CHFID, I.Othernames ||\" \"|| I.LastName InsureeName, " +
                "CASE I.Gender WHEN 'M' THEN '" + mContext.getResources().getString(R.string.Male) + "' WHEN 'F' THEN '" + mContext.getResources().getString(R.string.Female) + "' ELSE '" + mContext.getResources().getString(R.string.Other) + "' END Gender, " +
                "I.DOB , I.isHead, isOffline FROM tblInsuree I WHERE FamilyId = ? ORDER BY I.isHead DESC, I.InsureeId ASC";
        String[] arg = {String.valueOf(FamilyId)};
        JSONArray Insurees = sqlHandler.getResult(Query, arg);
        return Insurees.toString();
    }

    @JavascriptInterface
    public String getFamilyHeader(int FamilyId) {
        String Query = "SELECT R.LocationName RegionName, R.LocationId RegionId,D.LocationId DistrictId,  D.LocationName DistrictName,  W.LocationName WardName,  V.LocationName VillageName, D.LocationId, isOffline FROM tblFamilies F \n" +
                "INNER JOIN tblLocations V ON V.LocationId = F.LocationId\n" +
                "INNER JOIN tblLocations W ON W.LocationId = V.ParentLocationId\n" +
                "INNER JOIN tblLocations D ON D.LocationId = W.ParentLocationId\n" +
                "INNER JOIN tblLocations R ON R.LocationId = D.ParentLocationId\n" +
                "WHERE F.FamilyID= ? ";
        String[] arg = {String.valueOf(FamilyId)};
        JSONArray HeadOfFamily = sqlHandler.getResult(Query, arg);
        return HeadOfFamily.toString();
    }

    @JavascriptInterface
    public String getFamily(int FamilyId) {
        String sSQL = "SELECT R.LocationId RegionId, D.LocationId DistrictId, W.LocationId WardId, V.LocationId VillageId, F.FamilyId, F.InsureeId, F.Poverty, F.isOffline, F.FamilyType, F.FamilyAddress, F.Ethnicity, F.ConfirmationNo, F.ConfirmationType, isOffline \n" +
                "FROM tblFamilies F\n" +
                "INNER JOIN tblLocations V ON V.LocationId= F.LocationId\n" +
                "INNER JOIN tblLocations W ON W.LocationId = V.ParentLocationId\n" +
                "INNER JOIN tblLocations D ON D.LocationId = W.ParentLocationId\n" +
                "INNER JOIN tblLocations R ON R.LocationId = D.ParentLocationId\n" +
                "WHERE F.FamilyId  = ?";


        String[] args = {String.valueOf(FamilyId)};
        JSONArray Family = sqlHandler.getResult(sSQL, args);
        return Family.toString();


    }

    private String getYear(Date date) {
        @SuppressLint("SimpleDateFormat") SimpleDateFormat format = new SimpleDateFormat("yyyy");
        return format.format(date);
    }

    private Date addYear(Date date, int number) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.YEAR, number);
        return calendar.getTime();
    }

    private Date addMonth(Date date, int number) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.MONTH, number);
        return calendar.getTime();
    }

    private Date addDay(Date date, int number) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DATE, number);
        return calendar.getTime();
    }

    @JavascriptInterface
    public String getPolicyPeriod(int ProdId, String EnrollDate) throws ParseException, JSONException {

        @SuppressLint("SimpleDateFormat") SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Date dEnrollDate = format.parse(EnrollDate);

        String sSQL = "SELECT IFNULL(AdministrationPeriod, 0) AdministrationPeriod, StartCycle1, StartCycle2, StartCycle3, StartCycle4, InsurancePeriod, IFNULL(GracePeriod, 0)GracePeriod\n" +
                "FROM tblProduct\n" +
                "WHERE ProdId = ?";

        String[] args = {String.valueOf(ProdId)};

        JSONArray productDetails = sqlHandler.getResult(sSQL, args);

        JSONObject object = productDetails.getJSONObject(0);

        String EnrollYear = getYear(dEnrollDate);

        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        Date StartCycle1 = null;
        Date StartCycle2 = null;
        Date StartCycle3 = null;
        Date StartCycle4 = null;
        int GracePeriod = Integer.parseInt(object.getString("GracePeriod"));
        boolean hasCycle = false;
        Date StartDate = null;
        Date ExpiryDate = null;
        int InsurancePeriod = Integer.parseInt(object.getString("InsurancePeriod"));

        Date dateWithGracePeriod1 = null;
        Date dateWithGracePeriod2 = null;
        Date dateWithGracePeriod3 = null;
        Date dateWithGracePeriod4 = null;

        if ((!TextUtils.isEmpty(object.getString("StartCycle1"))) && (!object.getString("StartCycle1").equals("null"))) {
            StartCycle1 = sdf.parse(object.getString("StartCycle1") + "-" + EnrollYear);
            dateWithGracePeriod1 = addMonth(StartCycle1, GracePeriod);
        }
        if ((!TextUtils.isEmpty(object.getString("StartCycle2"))) && (!object.getString("StartCycle2").equals("null"))) {
            StartCycle2 = sdf.parse(object.getString("StartCycle2") + "-" + EnrollYear);
            dateWithGracePeriod2 = addMonth(StartCycle2, GracePeriod);
        }
        if ((!TextUtils.isEmpty(object.getString("StartCycle3"))) && (!object.getString("StartCycle3").equals("null"))) {
            StartCycle3 = sdf.parse(object.getString("StartCycle3") + "-" + EnrollYear);
            dateWithGracePeriod3 = addMonth(StartCycle3, GracePeriod);
        }
        if ((!TextUtils.isEmpty(object.getString("StartCycle4"))) && (!object.getString("StartCycle4").equals("null"))) {
            StartCycle4 = sdf.parse(object.getString("StartCycle4") + "-" + EnrollYear);
            dateWithGracePeriod4 = addMonth(StartCycle4, GracePeriod);
        }

        if (StartCycle1 != null) {
            //They are using cycles
            hasCycle = true;
            if (dateWithGracePeriod1 != null && (dEnrollDate.compareTo(dateWithGracePeriod1) == 0 || dEnrollDate.before(dateWithGracePeriod1)))
                StartDate = StartCycle1;
            else if (dateWithGracePeriod2 != null && (dEnrollDate.compareTo(dateWithGracePeriod2) == 0 || dEnrollDate.before(dateWithGracePeriod2)))
                StartDate = StartCycle2;
            else if (dateWithGracePeriod3 != null && (dEnrollDate.compareTo(dateWithGracePeriod3) == 0 || dEnrollDate.before(dateWithGracePeriod3)))
                StartDate = StartCycle3;
            else if (dateWithGracePeriod4 != null && (dEnrollDate.compareTo(dateWithGracePeriod4) == 0 || dEnrollDate.before(dateWithGracePeriod4)))
                StartDate = StartCycle4;
            else
                StartDate = addYear(StartCycle1, 1);


        } else {
            //They are not using cycles
            hasCycle = false;
            StartDate = dEnrollDate;
        }

        ExpiryDate = addDay(addMonth(StartDate, InsurancePeriod), -1);

        SimpleDateFormat ymd = new SimpleDateFormat("yyyy-MM-dd");


        JSONArray period = new JSONArray();
        JSONObject o = new JSONObject();
        o.put("StartDate", ymd.format(StartDate));
        o.put("ExpiryDate", ymd.format(ExpiryDate));
        o.put("HasCycle", hasCycle);

        period.put(o);

        return period.toString();
    }


    public static int RESULT_LOAD_IMG = 1;
    public static int RESULT_SCAN = 100;
    public static String ImagePath;
    public static String InsuranceNo;
    public static boolean inProgress = true;

    @JavascriptInterface
    public String selectPicture() {
        try{
            inProgress = true;
            Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            ((Activity) mContext).startActivityForResult(galleryIntent, RESULT_LOAD_IMG);

            ((MainActivity) mContext).ImagePath = "";
            int count = 0;
            while (((MainActivity) mContext).ImagePath == "") {
                count++;
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        return ((MainActivity) mContext).ImagePath;
    }

    @JavascriptInterface
    public double getPolicyValue(String enrollDate, int ProductId, int FamilyId, String startDate, boolean HasCycle, int PolicyId, String PolicyStage, int IsOffline) throws JSONException {
        Date ExpiryDate = null;
        String expiryDate = null;
        int PreviousPolicyId = 0;
        Date PreviousExpiryDate;

        if (PolicyId > 0) {
            String PreviousPolicy = "SELECT  FamilyId,  ProdId,  PolicyStage,  EnrollDate, ExpiryDate, isOffline FROM tblPolicy WHERE PolicyId = " + PolicyId;
            JSONArray PrvPolicyArray = sqlHandler.getResult(PreviousPolicy, null);
            JSONObject object = PrvPolicyArray.getJSONObject(0);
            FamilyId = Integer.parseInt(object.getString("FamilyId"));
            ProductId = Integer.parseInt(object.getString("ProdId"));
            enrollDate = object.getString("EnrollDate");
            PolicyStage = object.getString("PolicyStage");
            expiryDate = object.getString("ExpiryDate");
            IsOffline = Integer.parseInt(object.getString("isOffline"));
        }


//        General general = new General();
//        if (general.isNetworkAvailable(mContext) && IsOffline == 0) {
//            CallSoap cs = new CallSoap();
//            cs.setFunctionName("getPolicyValue");
//            return cs.getPolicyValue(FamilyId, ProductId, PolicyId, PolicyStage, enrollDate, PolicyId);
//
//        }

//        SELECT TOP 1  @EnrollDate = EnrollDate, @ExpiryDate = ExpiryDate FROM tblPolicy WHERE PolicyID = @PolicyId


        String productDetailsQuery = "SELECT \n" +
                "CASE WHEN Lumpsum='null' OR Lumpsum = '' THEN 0 ELSE Lumpsum END Lumpsum,\n" +
                "CASE WHEN premiumAdult = 'null' OR premiumAdult = '' THEN 0 ELSE premiumAdult END premiumAdult,\n" +
                "CASE WHEN premiumchild='null' OR premiumchild = '' THEN 0 ELSE premiumchild END premiumchild ,\n" +
                "CASE WHEN RegistrationLumpsum='null' OR RegistrationLumpsum = '' THEN 0 ELSE RegistrationLumpsum END RegistrationLumpsum,\n" +
                "CASE WHEN RegistrationFee='null' OR RegistrationFee = '' THEN 0 ELSE RegistrationFee END RegistrationFee,\n" +
                "CASE WHEN GeneralAssemblyLumpSum= 'null' OR GeneralAssemblyLumpSum = '' THEN 0 ELSE  GeneralAssemblyLumpSum END GeneralAssemblyLumpSum ,\n" +
                "CASE WHEN GeneralAssemblyFee='null' OR GeneralAssemblyFee = '' THEN 0 ELSE  GeneralAssemblyFee END GeneralAssemblyFee,\n" +
                "CASE WHEN Threshold= 'null' OR Threshold = '' THEN 0 ELSE Threshold END Threshold, \n" +
                "CASE WHEN MemberCount =  'null' OR MemberCount = '' THEN 0 ELSE MemberCount END MemberCount,\n" +

                "CASE WHEN EnrolmentDiscountPeriod='null' OR EnrolmentDiscountPeriod = '' THEN 0 ELSE  EnrolmentDiscountPeriod END EnrolmentDiscountPeriod, \n" +
                "CASE WHEN EnrolmentDiscountPerc='null' OR EnrolmentDiscountPerc = '' THEN 0 ELSE  EnrolmentDiscountPerc END EnrolmentDiscountPerc, \n" +
                "CASE WHEN RenewalDiscountPeriod='null' OR RenewalDiscountPeriod = '' THEN 0 ELSE  RenewalDiscountPeriod END RenewalDiscountPeriod, \n" +
                "CASE WHEN RenewalDiscountPerc='null' OR RenewalDiscountPerc = '' THEN 0 ELSE  RenewalDiscountPerc END RenewalDiscountPerc \n" +

                "FROM tblProduct WHERE ProdId = ? ";
        String[] args = {String.valueOf(ProductId)};
        JSONArray productDetails = sqlHandler.getResult(productDetailsQuery, args);
        JSONObject object = productDetails.getJSONObject(0);
        double LumpSum = Double.parseDouble(object.getString("Lumpsum"));
        double PremiumAdult = Double.parseDouble(object.getString("premiumAdult"));
        double PremiumChild = Double.parseDouble(object.getString("premiumchild"));
        double RegistrationLumpSum = Double.parseDouble(object.getString("RegistrationLumpsum"));
        double RegistrationFee = Double.parseDouble(object.getString("RegistrationFee"));
        double GeneralAssemblyLumpSum = Double.parseDouble(object.getString("GeneralAssemblyLumpSum"));
        double Threshold = Double.parseDouble(object.getString("Threshold"));
        double GeneralAssemblyFee = Double.parseDouble(object.getString("GeneralAssemblyFee"));
        int MemberCount = Integer.parseInt((object.getString("MemberCount")));
        // int EnrolmentDiscountPeriod = Integer.parseInt(object.getString("EnrolmentDiscountPeriod"));

        //Added Values
        Date MinDiscountDateN;
        Date MinDiscountDateR;


        int DiscountPeriodR = Integer.parseInt(object.getString("RenewalDiscountPeriod"));
        double DiscountPercentR = Double.parseDouble(object.getString("RenewalDiscountPerc"));
        int DiscountPeriodN = Integer.parseInt(object.getString("EnrolmentDiscountPeriod"));
        double DiscountPercentN = Double.parseDouble(object.getString("EnrolmentDiscountPerc"));

        String AdultMembersQuery = "SELECT COUNT(InsureeId) count FROM tblInsuree WHERE (strftime('%Y', 'now') - strftime('%Y', DOB))  >= 18 AND IFNULL(Relationship,0) <> 7  AND FamilyID ='" + FamilyId + "' ORDER BY InsureeId ASC LIMIT " + MemberCount;

        JSONArray AdultMembersArray = sqlHandler.getResult(AdultMembersQuery, null);
        JSONObject AdultObject = AdultMembersArray.getJSONObject(0);
        int AdultMembers = Integer.parseInt(AdultObject.getString("count"));
        if (AdultMembers > MemberCount) AdultMembers = MemberCount;


        String ChildMembersQuery = "SELECT COUNT(InsureeId) count FROM tblInsuree WHERE (strftime('%Y', 'now') - strftime('%Y', DOB))  < 18 AND IFNULL(Relationship,0) <> 7  AND FamilyID = '" + FamilyId + "' ORDER BY InsureeId ASC LIMIT " + MemberCount;
        JSONArray ChildMembersArray = sqlHandler.getResult(ChildMembersQuery, null);
        JSONObject ChildObject = ChildMembersArray.getJSONObject(0);
        int ChildMembers = Integer.parseInt(ChildObject.getString("count"));

        if ((AdultMembers + ChildMembers) >= MemberCount) ChildMembers = MemberCount - AdultMembers;


        String OAdultMembersQuery = "SELECT COUNT(InsureeId) count FROM tblInsuree WHERE(strftime('%Y', 'now') - strftime('%Y', DOB))  >= 18 AND IFNULL(Relationship,0) = 7  AND FamilyID = '" + FamilyId + "' ORDER BY InsureeId ASC LIMIT " + MemberCount;
        JSONArray OAdultMembersArray = sqlHandler.getResult(OAdultMembersQuery, null);
        JSONObject OAdultObject = OAdultMembersArray.getJSONObject(0);
        int OAdultMembers = Integer.parseInt(OAdultObject.getString("count"));

        if ((AdultMembers + ChildMembers + OAdultMembers) >= MemberCount)
            OAdultMembers = MemberCount - (AdultMembers + ChildMembers);


        String OChildMembersQuery = "SELECT COUNT(InsureeId) count FROM tblInsuree WHERE(strftime('%Y', 'now') - strftime('%Y', DOB))  < 18 AND IFNULL(Relationship,0) = 7  AND FamilyID = '" + FamilyId + "'  ORDER BY InsureeId ASC LIMIT " + MemberCount;
        JSONArray OChildMembersArray = sqlHandler.getResult(OChildMembersQuery, null);
        JSONObject OChildObject = OChildMembersArray.getJSONObject(0);
        int OChildMembers = Integer.parseInt(OChildObject.getString("count"));
        if ((AdultMembers + ChildMembers + OAdultMembers + OChildMembers) >= MemberCount)
            OAdultMembers = MemberCount - (AdultMembers + ChildMembers + OAdultMembers);

        @SuppressLint("SimpleDateFormat") SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        //Get   Previous Expiry Date
        if (PreviousPolicyId > 0) {
            String PED_Query = "SELECT ExpiryDate FROM tblPolicy WHERE  PolicyId =" + PreviousPolicyId;
            JSONArray PED_Array = sqlHandler.getResult(PED_Query, null);
            JSONObject PEDObject = PED_Array.getJSONObject(0);
            Date prvDate = null;
            try {
                prvDate = format.parse(PEDObject.getString("ExpiryDate"));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            PreviousExpiryDate = addDay(prvDate, 1);
        }
//        Get extra members in family

        Date EnrollDate = null;
        Date StartDate = null;
        try {
            EnrollDate = format.parse(enrollDate);
            if (PolicyId > 0) ExpiryDate = format.parse(expiryDate);
            StartDate = format.parse(startDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        double PolicyValue = 0;
        int ExtraAdult = 0;
        int ExtraChild = 0;
        double Contribution = 0;
        double GeneralAssembly = 0;
        double Registration = 0;
        double AddonAdult = 0;
        double AddonChild = 0;
        Date MinDiscountDate;
        if (Threshold > 0 && AdultMembers > Threshold)
            ExtraAdult = (int) (AdultMembers - Threshold);

        if (Threshold > 0 && ChildMembers > (Threshold - AdultMembers + ExtraAdult))
            ExtraChild = (int) (ChildMembers - ((Threshold - AdultMembers + ExtraAdult)));


//        Get the Contribution
        if (LumpSum > 0)
            Contribution = LumpSum;
        else
            Contribution = (AdultMembers * PremiumAdult) + (ChildMembers * PremiumChild);

//        Get the Assembly
        if (GeneralAssemblyLumpSum > 0)
            GeneralAssembly = GeneralAssemblyLumpSum;
        else
            GeneralAssembly = (AdultMembers + ChildMembers + OAdultMembers + OChildMembers) * GeneralAssemblyFee;

        //Calculate If New
        if (PolicyStage.equalsIgnoreCase("N")) {
            if (RegistrationLumpSum > 0)
                Registration = RegistrationLumpSum;
            else
                Registration = (AdultMembers + ChildMembers + OAdultMembers + OChildMembers) * RegistrationFee;
        }


	/* Any member above the maximum member count  or with excluded relationship calculate the extra addon amount */

        AddonAdult = (ExtraAdult + OAdultMembers) * PremiumAdult;
        AddonChild = (ExtraChild + OChildMembers) * PremiumChild;
        Contribution += AddonAdult + AddonChild;
        PolicyValue = Contribution + GeneralAssembly + Registration;


        String PolicyPeriod = null;
        try {
            PolicyPeriod = getPolicyPeriod(ProductId, enrollDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        JSONArray jsonArray = new JSONArray(PolicyPeriod);
        JSONObject jsnobject = jsonArray.getJSONObject(0);
        try {
            StartDate = format.parse(jsnobject.getString("StartDate"));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (PolicyStage.equalsIgnoreCase("N")) {
            MinDiscountDateN = addMonth(StartDate, DiscountPeriodN);
            if (EnrollDate.before(MinDiscountDateN) && HasCycle == true) {
                PolicyValue -= (PolicyValue * 0.01 * DiscountPercentN);
            }
        } else if (PolicyStage.equalsIgnoreCase("R")) {


            if (PreviousPolicyId > 0) {

                PreviousExpiryDate = addDay(ExpiryDate, 1);

            } else {
                PreviousExpiryDate = StartDate;
            }
            MinDiscountDateR = addMonth(PreviousExpiryDate, DiscountPeriodR);///@),@);
            if (EnrollDate.before(MinDiscountDateR))
                PolicyValue -= (PolicyValue * 0.01 * DiscountPercentR);
        }


        return PolicyValue;
    }

    @JavascriptInterface
    public String getOfficers(int LocationId, String EnrolmentDate) {

        String OfficerQuery = " SELECT OfficerId, Code  ||\" - \"|| Othernames  ||\" \"||  LastName  Code, LocationId FROM tblOfficer \n" +
                " WHERE LocationId=" + LocationId + " AND ('" + EnrolmentDate + "' <= WorksTo OR  IFNULL(WorksTo,0)=0 OR  IFNULL('" + EnrolmentDate + "',0) = 0) \n" +
                " ORDER BY OfficerId";
        JSONArray Oficers = sqlHandler.getResult(OfficerQuery, null);

        return Oficers.toString();
    }

    @JavascriptInterface
    public String getProducts(int RegionId, int DistrictId, String EnrolmentDate) {

        String ProductQuery = "SELECT  ProdId, ProductCode ||\" - \"|| ProductName ProductCode, ProductName \n" +
                "FROM tblProduct P\n" +
                "INNER JOIN  uvwLocations L ON P.LocationId = L.LocationId\n" +
                "WHERE  ((L.RegionId = " + RegionId + " OR L.RegionId ='null') AND (L.DistrictId =  " + DistrictId + " OR L.DistrictId ='null') OR L.LocationId='null') AND " +
                "( '" + EnrolmentDate + "'  BETWEEN P.DateFrom AND P.Dateto OR IFNULL(" + EnrolmentDate + ",0) = 0 )  \n" +
                "ORDER BY  L.LocationId DESC";

        JSONArray Products = sqlHandler.getResult(ProductQuery, null);

        return Products.toString();
    }

    @JavascriptInterface
    public int SavePolicy(String PolicyData, int FamilyId, int PolicyId) throws Exception {
        inProgress = true;
        int MaxPolicyId = 0;
        rtPolicyId = PolicyId;
        int isOffline = 1;
        String nullify = null;
        global = (Global) mContext.getApplicationContext();
        try {

            String MaxPolicyIdQuery = "SELECT  IFNULL(COUNT(PolicyId),0)+1  PolicyId  FROM tblPolicy";
            JSONArray JsonMaxPolicy = sqlHandler.getResult(MaxPolicyIdQuery, null);
            try {
                JSONObject JmaxPolicyOb = JsonMaxPolicy.getJSONObject(0);
                MaxPolicyId = JmaxPolicyOb.getInt("PolicyId");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            HashMap<String, String> data = jsonToTable(PolicyData);
            ContentValues values = new ContentValues();
           // isOffline = getFamilyStatus(FamilyId);

            values.put("FamilyId", FamilyId);
            values.put("EnrollDate", data.get("txtEnrolmentDate"));
            values.put("StartDate", data.get("txtStartDate"));
            values.put("EffectiveDate", data.get("txtEffectiveDate"));
            values.put("ExpiryDate", data.get("txtExpiryDate"));
            values.put("PolicyStatus", data.get("hfPolicyStatus"));
            values.put("PolicyValue", data.get("hfPolicyValue"));
            values.put("ProdId", data.get("ddlProduct"));
            values.put("OfficerId", data.get("ddlOfficer"));
            if (isOffline == 2) isOffline = 0;
            values.put("isOffline", isOffline);

            values.put("PolicyStage", "N");
            if (rtPolicyId == 0) {
                if (isOffline == 0) MaxPolicyId = -MaxPolicyId;
                values.put("PolicyId", MaxPolicyId);
                sqlHandler.insertData("tblPolicy", values);
                rtPolicyId = MaxPolicyId;
                InsertPolicyInsuree(rtPolicyId, 1);

            } else {
                int Online = 2;
                if (isOffline == 0 || isOffline == 2) {
                    isOffline = 0;
                    Online = 2;
                }
                sqlHandler.updateData("tblPolicy", values, "PolicyId = ? AND (isOffline = ? OR isOffline = ?) ", new String[]{String.valueOf(PolicyId), String.valueOf(isOffline), String.valueOf(Online)});

            }
            if (isOffline == 0 && global.getUserId() > 0) {
                pd = new ProgressDialog(mContext);
                pd = ProgressDialog.show(mContext, "", mContext.getResources().getString(R.string.Uploading));
                final int FinalFamilyId = FamilyId;
                final int FinalPolicyId = rtPolicyId;
                new Thread() {
                    public void run() {
                        try {
                            rtPolicyId = Enrol(FinalFamilyId, 0, FinalPolicyId, 0, 0);
                            inProgress = false;
                        } catch (UserException e) {
                            e.printStackTrace();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        pd.dismiss();
                    }
                }.start();
            } else {
                inProgress = false;
            }

        } catch (NumberFormatException e) {
            e.printStackTrace();
        } catch (UserException e) {
            e.printStackTrace();
            throw new Exception(e.getMessage());
        }
        while (inProgress) {
        }
        return rtPolicyId;

    }

    @JavascriptInterface
    public String getFamilyPolicies(int FamilyId) throws ParseException {
        //mimi ni add
        //getPolicyValue(String enrollDate, int ProductId, int FamilyId, String startDate, boolean HasCycle, int PolicyId, String PolicyStage, int IsOffline) throws JSONException {
        boolean isValueChanged = false;
        String QueryPolicyValue = "SELECT P.PolicyId,Pro.ProdId , EffectiveDate, PolicyValue, StartDate, ExpiryDate, EnrollDate,FamilyId,PolicyStage,IsOffline FROM tblPolicy P\n" +
                "INNER JOIN tblProduct Pro ON Pro.ProdId = P.ProdId\n" +
                "WHERE FamilyId = " + FamilyId;
        JSONArray PolicyValueArray = sqlHandler.getResult(QueryPolicyValue, null);
        JSONObject ValueObject = null;
        String enrollDate = null;
        int ProductId;
        String startDate;
        boolean HasCycle = false;
        int PolicyId;
        String PolicyStage;
        int IsOffline;
        String getCycle;
        String PolicyValue = null;
        Double NewPolicyValue = null;

        for (int i = 0; i < PolicyValueArray.length(); i++) {
            try {
                ValueObject = PolicyValueArray.getJSONObject(i);
                enrollDate = ValueObject.getString("EnrollDate");
                ProductId = ValueObject.getInt("ProdId");

                PolicyId = ValueObject.getInt("PolicyId");
                PolicyStage = ValueObject.getString("StartDate");
                startDate = ValueObject.getString("PolicyStage");
                IsOffline = ValueObject.getInt("isOffline");
                PolicyValue = ValueObject.getString("PolicyValue");

                getCycle = getPolicyPeriod(ProductId, enrollDate);
                JSONArray CycleArray =  new JSONArray();
                //CycleArray.put(getCycle).getJSONArray(0);
                JSONArray newJArray = new JSONArray(getCycle);
                JSONObject o = null;
                o = newJArray.getJSONObject(0);
                startDate = o.getString("StartDate");
                HasCycle =  o.getBoolean("HasCycle");
                //Cycle affect start date
                 NewPolicyValue = getPolicyValue(enrollDate,ProductId,FamilyId,startDate,HasCycle,PolicyId,PolicyStage,IsOffline);
                Double doublePolicyValue = Double.valueOf(PolicyValue);
                Double doubleNewPolicyValue = Double.valueOf(NewPolicyValue);
                if(!doublePolicyValue.equals(doubleNewPolicyValue)){
                    if(!isValueChanged) isValueChanged = true;

                    ContentValues values = new ContentValues();
                    values.put("PolicyValue", NewPolicyValue);
                    try {//Update to new policy value
                        sqlHandler.updateData("tblPolicy", values, "PolicyId = ?", new String[]{String.valueOf(PolicyId)});
                    } catch (UserException e) {
                        e.printStackTrace();
                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }


        String Query = "SELECT  P.PolicyId, ProductCode, ProductName, EffectiveDate, PolicyValue, StartDate, EnrollDate, \n" +
                "   CASE    WHEN PolicyStatus = 1 THEN '" + mContext.getResources().getString(R.string.Idle) + "'   " +
                "   WHEN PolicyStatus = 2 THEN '" + mContext.getResources().getString(R.string.Active) + "'  " +
                "   WHEN PolicyStatus = 4 THEN '" + mContext.getResources().getString(R.string.Suspended) + "'  " +
                "   WHEN PolicyStatus = 8 THEN '" + mContext.getResources().getString(R.string.Expired) + "'  END  PolicyStatus, " +
                "   PolicyStatus PolicyStatusValue, P.ExpiryDate, isOffline FROM tblPolicy P \n" +
                "   INNER JOIN tblProduct Prod ON P.ProdId=Prod.ProdId  \n " +
                "   WHERE FamilyId = ?";

        String[] arg = {String.valueOf(FamilyId)};
        JSONArray Policies = sqlHandler.getResult(Query, arg);
        final boolean finalIsValueChanged = isValueChanged;
        final String finalEnrollDate = enrollDate;
        final Double finalNewPolicyValue = NewPolicyValue;
        final String finalPolicyValue = PolicyValue;
        if (finalIsValueChanged) {
            ((Activity) mContext).runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    ShowDialog(mContext.getResources().getString(R.string.PolicyValueChange) + finalEnrollDate + "," + "has been changed from " + finalPolicyValue + " to " + finalNewPolicyValue);


                }

            });
        }

        return Policies.toString();
    }

    @JavascriptInterface
    public String getPolicy(int PolicyId) {
        String Query = "SELECT  P.PolicyId, P.ProdId, OfficerId , ProductCode, ProductName, PolicyStage, EffectiveDate, IFNULL(PolicyValue,0) PolicyValue, StartDate, EnrollDate, \n" +
                "   CASE    WHEN PolicyStatus = 1 THEN '" + mContext.getResources().getString(R.string.Idle) + "'   " +
                "   WHEN PolicyStatus = 2 THEN '" + mContext.getResources().getString(R.string.Active) + "'  " +
                "   WHEN PolicyStatus = 4 THEN '" + mContext.getResources().getString(R.string.Suspended) + "'  " +
                "   WHEN PolicyStatus = 8 THEN '" + mContext.getResources().getString(R.string.Expired) + "'  END  PolicyStatus, " +
                "   PolicyStatus  PolicyStatusValue, P.ExpiryDate, (IFNULL(PolicyValue,0) - IFNULL(Contribution,0)) Balance ,  IFNULL(Contribution,0) Contribution, P.isOffline  FROM tblPolicy P \n" +
                "   INNER JOIN tblProduct Prod ON P.ProdId=Prod.ProdId  \n " +
                "   LEFT JOIN (SELECT MAX(PolicyId) PolicyId, IFNULL(Sum(Amount),0) Contribution ,PremiumId " +
                "   FROM  tblPremium WHERE PolicyId = " + PolicyId + " AND isPhotoFee = 'false' ) " +
                "   Pre ON Pre.PolicyId=P.PolicyId \n " +
                "   WHERE P.PolicyId = ?";

        String[] arg = {String.valueOf(PolicyId)};
        JSONArray Policies = sqlHandler.getResult(Query, arg);
        return Policies.toString();
    }


    @JavascriptInterface
    public int getPolicyVal(String PolicyId) {
        int PolicId = Integer.parseInt(PolicyId);
        int policyval = 0;
        String Query = "SELECT PolicyValue FROM tblPolicy WHERE PolicyId = "+ PolicId + "";
        JSONArray Policies = sqlHandler.getResult(Query, null);
        try {
            JSONObject JmaxPolicyOb = Policies.getJSONObject(0);
            policyval = JmaxPolicyOb.getInt("PolicyValue");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return policyval;
    }
    @JavascriptInterface
    public int getSumPrem(String PolicyId) {
        int PolicId = Integer.parseInt(PolicyId);
        int totalPremium = 0;
        String Query = "SELECT SUM(Amount) FROM tblPremium WHERE PolicyId = "+ PolicId + "";
        JSONArray Policies = sqlHandler.getResult(Query, null);
        try {
            JSONObject JmaxPolicyOb = Policies.getJSONObject(0);
            totalPremium = JmaxPolicyOb.getInt("SUM(Amount)");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return totalPremium;
    }

    @JavascriptInterface
    public int SavePremiums(String PremiumData, int PolicyId, int PremiumId, int FamilyId) throws Exception {
        inProgress = true;
        int MaxPremiumId = 0;
        int isOffline = 1;
        rtPremiumId = PremiumId;
        //  String CHFID = "";
        String ReceiptNo = "";
        try {
            global = (Global) mContext.getApplicationContext();

            //getPremium Max Id
            String MaxPremiumIdQuery = "SELECT  IFNULL(COUNT(PremiumId),0)+1  PremiumId  FROM tblPremium";
            // String CHFIDQUERY = "SELECT CHFID FROM tblInsuree WHERE isHead = 'true' OR isHead = '1' AND FamilyId = "+ FamilyId;
            JSONArray JsonMaxPremium = sqlHandler.getResult(MaxPremiumIdQuery, null);
            // JSONArray JsonCHFID = sqlHandler.getResult(CHFIDQUERY, null);
            try {
                JSONObject JmaxPremiumOb = JsonMaxPremium.getJSONObject(0);
                //  JSONObject JsonCHFIDJSONObject = JsonCHFID.getJSONObject(0);
                MaxPremiumId = JmaxPremiumOb.getInt("PremiumId");
                //  CHFID = JsonCHFIDJSONObject.getString("CHFID");
            } catch (JSONException e) {
                e.printStackTrace();
            }


          //  isOffline = getFamilyStatus(FamilyId);
            if (isOffline == 2) isOffline = 0;

            HashMap<String, String> data = jsonToTable(PremiumData);
            ReceiptNo = data.get("txtReceipt");

            ContentValues values = new ContentValues();
            values.put("PolicyId", PolicyId);
            values.put("Amount", data.get("txtAmount"));
            values.put("payerId", data.get("ddlPayer"));
            values.put("Receipt", ReceiptNo);
            values.put("PayDate", data.get("txtPayDate"));
            values.put("PayType", data.get("ddlPayType"));
            values.put("isOffline", isOffline);
            values.put("IsPhotoFee", data.get("ddlPhotoFee"));

            if (rtPremiumId == 0) {
                if (isOffline == 0) MaxPremiumId = -MaxPremiumId;
                values.put("PremiumId", MaxPremiumId);
                sqlHandler.insertData("tblPremium", values);
                rtPremiumId = MaxPremiumId;
            } else {
                int Online = 2;
                if (isOffline == 0 || isOffline == 2) {
                    isOffline = 0;
                    Online = 2;
                }
                sqlHandler.updateData("tblPremium", values, "PremiumId = ? AND (isOffline = ? OR isOffline = ?) ", new String[]{String.valueOf(PremiumId), String.valueOf(isOffline), String.valueOf(Online)});

            }
            if (isOffline == 0 && global.getUserId() >= 0) {

                pd = new ProgressDialog(mContext);
                pd = ProgressDialog.show(mContext, "", mContext.getResources().getString(R.string.Uploading));
                final int FinalFamilyId = FamilyId;
                final int FinalPremium = rtPremiumId;
                //new Thread() {
                    //public void run() {
                        try {

                            rtPremiumId = Enrol(FinalFamilyId, 0, 0, FinalPremium, 0);
                            inProgress = false;
                        } catch (UserException e) {
                            e.printStackTrace();
                        }

                        pd.dismiss();
                   // }
               // }.start();
            } else {
                inProgress = false;
            }

        } catch (NumberFormatException e) {
            e.printStackTrace();
        } catch (UserException e) {
            e.printStackTrace();
            throw new Exception(e.getMessage());
        }
        while (inProgress) {
        }
        return rtPremiumId;
    }
    @JavascriptInterface
    public String EnforceDialog(){
        inProgress = true;
        final String[] enforceRes = {""};
        AlertDialog.Builder alertDialog2 = new AlertDialog.Builder(
                mContext);

// Setting Dialog Title
        alertDialog2.setTitle("NO INTERNET CONNECTION");
        alertDialog2.setMessage("Do you want to get Data from your local text file?");

// Setting Icon to Dialog
        // alertDialog2.setIcon(R.drawable.delete);

// Setting Positive "Yes" Btn
        alertDialog2.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        enforceRes[0] = "ok";
                        // Write your code here to execute after dialog
                    }
                }).setNegativeButton("ENFORCE",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        enforceRes[0] = "enforce";
                    }
                }).setNeutralButton("NO",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        enforceRes[0] = "no";
                    }
                });

// Showing Alert Dialog
        alertDialog2.show();
        return enforceRes[0];
    }
    @JavascriptInterface
    public String getPayers(int RegionId, int DistrictId) {
        String Query = "SELECT PayerId, PayerName,P.LocationId FROM tblPayer P \n" +
                "INNER JOIN  uvwLocations L ON P.LocationId = L.LocationId\n" +
                "WHERE  (L.RegionId = " + RegionId + " OR L.RegionId ='null') AND (L.DistrictId = " + DistrictId + " OR L.DistrictId ='null')  " +
                " ORDER BY L.LocationId";
        JSONArray Payers = sqlHandler.getResult(Query, null);

        return Payers.toString();
    }

    @JavascriptInterface
    public String getPremiums(int PolicyId) {
        String Query = "SELECT PremiumId, PayerId, Amount, Receipt , PayDate, " +
                "CASE PayType WHEN 'C' THEN '" + mContext.getResources().getString(R.string.Cash) + "' WHEN 'B' THEN '" + mContext.getResources().getString(R.string.BankTransfer) + "' WHEN 'M' THEN '" + mContext.getResources().getString(R.string.MobilePhone) + "' END PayType, " +
                "isOffline,IsPhotoFee \n" +
                "FROM tblPremium WHERE PolicyId=?";
        String arg[] = {String.valueOf(PolicyId)};
        JSONArray Premiums = sqlHandler.getResult(Query, arg);
        return Premiums.toString();
    }

    @JavascriptInterface
    public String getPremium(int PremiumId) {
        String Query = "SELECT PremiumId, PayerId, Amount, Receipt , PayDate, PayType,isOffline,IsPhotoFee \n" +
                "FROM tblPremium WHERE PremiumId=?";
        String arg[] = {String.valueOf(PremiumId)};
        JSONArray Premiums = sqlHandler.getResult(Query, arg);
        return Premiums.toString();
    }
    @JavascriptInterface
    public boolean IsReceiptNumberUnique(String ReceiptNo, int FamilyId) {
        String CHFID = "";
        int isOffline = 0;
        int isHead = 1;
        Boolean res = true;
        String CHFIDQUERY = "SELECT CHFID,isOffline FROM tblInsuree WHERE isHead = " + isHead + " AND FamilyId = " + FamilyId;

        JSONArray JsonCHFID = sqlHandler.getResult(CHFIDQUERY, null);
        try {
            JSONObject JsonCHFIDJSONObject = JsonCHFID.getJSONObject(0);
            CHFID = JsonCHFIDJSONObject.getString("CHFID");
            isOffline = JsonCHFIDJSONObject.getInt("isOffline");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        //General general = new General();
        if (general.isNetworkAvailable(mContext) && isOffline == 0) {
            CallSoap cs = new CallSoap();
            cs.setFunctionName("isUniqueReceiptNo");
            if (!cs.isUniqueReceiptNo(ReceiptNo.trim(), CHFID)){
                res = false;
            }
        }

/*        String Query = "SELECT PremiumId, PayerId, Amount, Receipt , PayDate, PayType,IsOffline,isPhotoFee \n" +
                "FROM tblPremium WHERE Receipt=?";*/

        String Query = "SELECT * FROM tblPremium WHERE LOWER(Receipt)=?";
        String arg[] = {(ReceiptNo.toLowerCase()).trim()};
        JSONArray Premiums = sqlHandler.getResult(Query, arg);
        Premiums.toString();
        int Count = Premiums.length();

        if(Count > 0){
            res = false;
        }
        return res;
    }

    @JavascriptInterface
    public String checkNet(){
        if(general.isNetworkAvailable(mContext)){
            return "true";
        }else{
            return "false";
        }
    }
    @JavascriptInterface
    public void getLocalData(){

        ((Activity) mContext).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((MainActivity) mContext).openDialogFromPage();
                ((MainActivity) mContext).calledFrom = "htmlpage";
            }
        });
    }

    @JavascriptInterface
    public int DeletePremium(int PremiumId, int PolicyId) {
        String Query = "DELETE FROM tblPremium WHERE PremiumId=?";
        String arg[] = {String.valueOf(PremiumId)};
        JSONArray Premiums = sqlHandler.getResult(Query, arg);
        //Premiums.toString();
        //calculated by herman
        int sumpremiums = getSumPremium(PolicyId);
        int policyvalue = getPolicyValue(PolicyId);
        if(sumpremiums < policyvalue){
            updatePolicystatus(PolicyId,1);
        }
        return 1;
    }
    @JavascriptInterface
    public int DeletePremium(int PremiumId) {
        String Query = "DELETE FROM tblPremium WHERE PremiumId=?";
        String arg[] = {String.valueOf(PremiumId)};
        JSONArray Premiums = sqlHandler.getResult(Query, arg);
        //Premiums.toString();
        return 1;
    }
    //get sum of premiums of policy id
    public int getSumPremium(int PolicyId) {
        int sumpremiums = 0;
        String Query = "SELECT SUM(Amount) FROM tblPremium WHERE PolicyId=?";
        String arg[] = {String.valueOf(PolicyId)};
        JSONArray Premiums = sqlHandler.getResult(Query, arg);
        try {
            JSONObject JmaxPremiumOb = Premiums.getJSONObject(0);
            sumpremiums = JmaxPremiumOb.getInt("SUM(Amount)");
            //  CHFID = JsonCHFIDJSONObject.getString("CHFID");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return sumpremiums;
    }
    public int getPolicyValue(int PolicyId){
        int polv = 0;
        String Query = "SELECT PolicyValue FROM tblPolicy WHERE PolicyId=?";
        String arg[] = {String.valueOf(PolicyId)};
        JSONArray policyvalue = sqlHandler.getResult(Query, arg);
        try {
            JSONObject JmaxPremiumOb = policyvalue.getJSONObject(0);
            polv = JmaxPremiumOb.getInt("PolicyValue");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return polv;
    }
    public void updatePolicystatus(int PolicyId, int status){
        ContentValues values = new ContentValues();
        values.put("PolicyStatus", String.valueOf(status));
        try {//Update to new policy value
            sqlHandler.updateData("tblPolicy", values, "PolicyId = ?", new String[]{String.valueOf(PolicyId)});
        } catch (UserException e) {
            e.printStackTrace();
        }
    }




    @JavascriptInterface
    public int DeletePolicy(int PolicyId) {
        String PremiumQuery = "DELETE FROM tblPremium WHERE PolicyId=?";
        String arg[] = {String.valueOf(PolicyId)};
        JSONArray Premiums = sqlHandler.getResult(PremiumQuery, arg);
        //Premiums.toString();
        String PolicyQuery = "DELETE FROM tblPolicy WHERE PolicyId=?";
        String PolicyArg[] = {String.valueOf(PolicyId)};
        JSONArray Policy = sqlHandler.getResult(PolicyQuery, PolicyArg);
        //Policy.toString();
        //Added by salum 12.12.2017
        DeleteInsureePolicy(PolicyId,0);
        return 1;

    }

    @JavascriptInterface
    public int DeleteInsuree(int InsureeId) {
        int res = 0;
        try{
            String IsHeadQuery = "SELECT InsureeId FROM tblInsuree WHERE InsureeId=? AND ishead =?";
            String IsHeadarg[] = {String.valueOf(InsureeId), "1"};
            JSONArray IsHead = sqlHandler.getResult(IsHeadQuery, IsHeadarg);
            int Count = IsHead.length();
            if (Count > 0)
                res = 2;
            else if (Count == 0) {
                String InsureeQuery = "DELETE FROM tblInsuree WHERE InsureeId=?";
                String arg[] = {String.valueOf(InsureeId)};
                JSONArray result = sqlHandler.getResult(InsureeQuery, arg);
                //Added by Salumu on 12/12/2017 to delete InsureePolicy
                DeleteInsureePolicy(0,InsureeId);
                res = 1;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return res;
    }

    @JavascriptInterface
    public int DeleteFamily(int FamilyId) {

        String PremiumQuery = "DELETE FROM tblPremium   \n" +
                "WHERE PolicyId IN \n" +
                "(SELECT PolicyId FROM tblPolicy WHERE FamilyId = " + FamilyId + " )";
        JSONArray Premium = sqlHandler.getResult(PremiumQuery, null);
        //Premium.toString();

        String PolicyQuery = "DELETE FROM tblPolicy WHERE FamilyId=?";
        String PolicyArg[] = {String.valueOf(FamilyId)};
        JSONArray Policy = sqlHandler.getResult(PolicyQuery, PolicyArg);
        //Policy.toString();


        String InsureeQuery = "DELETE FROM  tblInsuree WHERE FamilyId=?";
        String arg[] = {String.valueOf(FamilyId)};
        JSONArray Insuree = sqlHandler.getResult(InsureeQuery, arg);
        //Insuree.toString();

        String FamilyQuery = "DELETE FROM  tblfamilies WHERE FamilyId=?";
        String Familyarg[] = {String.valueOf(FamilyId)};
        JSONArray Families = sqlHandler.getResult(FamilyQuery, arg);
        //Families.toString();
        return 1;
    }
    public String getDataFromDb2(String chfid){

        try{

            resu = "[{";

            db = openOrCreateDatabase(Path +"ImisData.db3",null);

            String[] columns = {"CHFID" ,"Photo" , "InsureeName", "DOB", "Gender","ProductCode", "ProductName", "ExpiryDate", "Status", "DedType", "Ded1", "Ded2", "Ceiling1", "Ceiling2"};

            Cursor c = db.query("tblPolicyInquiry", columns, "Trim(CHFID)=" + "\'"+ chfid +"\'" , null, null, null, null);

            int i = 0;
            boolean _isHeadingDone = false;

            for(c.moveToFirst();!c.isAfterLast();c.moveToNext()){
                for(i=0;i<5;i++){
                    if (!_isHeadingDone){
                        if (c.getColumnName(i).equalsIgnoreCase("photo")){
                            byte[] photo = c.getBlob(i);
                            if (photo != null){
                                ByteArrayInputStream is = new ByteArrayInputStream(photo);
                                theImage = BitmapFactory.decodeStream(is);
                            }
                            continue;
                        }
                        resu = resu + "\"" + c.getColumnName(i) + "\":" + "\"" + c.getString(i) + "\",";
                    }else{

                    }
                }
                _isHeadingDone = true;

                if (c.isFirst())
                    resu = resu + "\"" + "Details" + "\":[{" ;
                else
                    resu = resu + "{";

                for(i=5;i<c.getColumnCount();i++){

                    resu = resu + "\"" + c.getColumnName(i) + "\":" + "\"" + c.getString(i) + "\"";
                    if(i < c.getColumnCount() - 1)
                        resu = resu + ",";
                    else{
                        resu = resu + "}";
                        if (!c.isLast())resu = resu + ",";
                    }

                }
                //result = result + "]}";
            }

            resu = resu + "]}]";

        }catch(Exception e){
            resu = e.toString();
        }

        return resu;

    }

    public String OfflineEnquire(String CHFID) {
        sqlHandler.isPrivate = false;
        String Query = "SELECT CHFID ,Photo ,InsureeName,DOB,Gender,ProductCode,ProductName,ExpiryDate,Status,DedType,Ded1,Ded2,Ceiling1,Ceiling2 FROM tblPolicyInquiry WHERE  Trim(CHFID) = ?";
        String arg[] = {CHFID};
        JSONArray Insuree = sqlHandler.getResult(Query, arg);
        return Insuree.toString();
    }

    public String OfflineRenewals(String OfficerCode) {
        String Query = "SELECT RenewalId, PolicyId, OfficerId, OfficerCode, CHFID, LastName, OtherNames, ProductCode, ProductName, VillageName, RenewalPromptDate, IMEI, Phone,LocationId,PolicyValue " +
                " FROM tblRenewals WHERE LOWER(OfficerCode)=? AND isDone = ? ";
        String arg[] = {OfficerCode.toLowerCase(), "N"};
        JSONArray Renews = sqlHandler.getResult(Query, arg);
        return Renews.toString();
    }


    public String InsertRenewals(String Result) {
        String TableName = "tblRenewals";
        String[] Columns = {"RenewalId", "PolicyId", "OfficerId", "OfficerCode", "CHFID", "LastName", "OtherNames", "ProductCode", "ProductName", "VillageName", "RenewalPromptDate", "IMEI", "Phone", "LocationId", "PolicyValue"};
        String Where = "isDone = ?";
        String[] WhereArg = {"N"};
        sqlHandler.deleteData(TableName, Where, WhereArg);
        try {
            sqlHandler.insertData(TableName, Columns, Result, "");
        } catch (JSONException e) {
            e.printStackTrace();
            return String.valueOf(0);
        }
        return String.valueOf(1);
    }

    public int DeleteRenewalOfflineRow(Integer RenewalId) {
        String Query = "DELETE FROM tblRenewals WHERE RenewalId=?";
        String arg[] = {String.valueOf(RenewalId)};
        JSONArray Renewal = sqlHandler.getResult(Query, arg);
        Renewal.toString();
        return 1; //Delete Success
    }

    public int UpdateRenewTable(int RenewalId) {
        ContentValues values = new ContentValues();
        values.put("isDone", "Y");
        try {
            sqlHandler.updateData("tblRenewals", values, "RenewalId = ?", new String[]{String.valueOf(RenewalId)});
        } catch (UserException e) {
            e.printStackTrace();
        }
        return 1;//Update Success
    }

    public String getOfflineFeedBack(String OfficerCode) {
        String Query = "SELECT ClaimId,OfficerId,OfficerCode,CHFID,LastName,OtherNames,HFCode,HFName,ClaimCode,DateFrom,DateTo,IMEI, Phone,FeedbackPromptDate " +
                "FROM  tblFeedbacks WHERE LOWER(OfficerCode) = ?  AND  isDone = ?";
        String arg[] = {OfficerCode.toLowerCase(), "N"};
        JSONArray FeedBacks = sqlHandler.getResult(Query, arg);
        return FeedBacks.toString();
    }

    public Boolean InsertFeedbacks(String Result) {
        String TableName = "tblFeedbacks";
        String[] Columns = {"ClaimId", "OfficerId", "OfficerCode", "CHFID", "LastName", "OtherNames", "HFCode", "HFName", "ClaimCode", "DateFrom", "DateTo", "IMEI", "Phone", "FeedbackPromptDate"};
        String Where = "isDone = ?";
        String[] WhereArg = {"N"};
        sqlHandler.deleteData(TableName, Where, WhereArg);
        try {
            sqlHandler.insertData(TableName, Columns, Result, "");
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }


    public Cursor SearchPayer(String InputText, String LocationId) {
        String Query = "WITH RECURSIVE AllLocations(LocationId, ParentLocationId) AS\n" +
                "(\n" +
                " SELECT 0 LocationId, NULL ParentLocationId FROM tbllocations\n" +
                " UNION \n" +
                " SELECT   LocationId, ParentLocationId FROM tblLocations WHERE LocationId =" + LocationId + "\n" +
                " UNION \n" +
                " SELECT L.LocationId, L.ParentLocationId\n" +
                " FROM tblLocations L, AllLocations\n" +
                " WHERE L.LocationId = AllLocations.ParentLocationId\n" +
                ")\n" +
                "SELECT PayerId, PayerName,P.LocationId FROM tblPayer P \n" +
                "INNER JOIN ALLLocations AL ON IFNULL(P.LocationId,0) =IFNULL(AL.LocationId,0) \n" +
                "WHERE PayerName LIKE '%" + InputText + "%' \n" +
                "ORDER BY AL.ParentLocationId ,AL.LocationId";
        Cursor c = (Cursor) sqlHandler.getResult(Query, null);
        if (c != null) {
            c.moveToFirst();
        }
        return c;
    }

    public String CleanFeedBackTable(String ClaimId) {
        String Query = "DELETE FROM tblFeedbacks WHERE ClaimId = ?";
        String arg[] = {ClaimId};
        JSONArray Feedback = sqlHandler.getResult(Query, arg);
        return Feedback.toString();
    }

    public boolean UpdateFeedBack(int ClaimId) {
        ContentValues values = new ContentValues();
        values.put("isDone", "Y");
        try {
            sqlHandler.updateData("tblFeedbacks", values, "ClaimId = ?", new String[]{String.valueOf(ClaimId)});
        } catch (UserException e) {
            e.printStackTrace();
        }
        return true;//Update Success
    }

    private void DeleteFeedBackRenewal() {
        String Query = "DELETE FROM tblFeedbacks WHERE isDone = 'Y'; DELETE FROM tblRenewals WHERE isDone = 'Y' ";
        sqlHandler.getResult(Query, null);
    }

    @JavascriptInterface
    public int UpdatePolicy(int PolicyId, String PayDate, int policystatus) throws ParseException {
        ContentValues values = new ContentValues();
        @SuppressLint("SimpleDateFormat") SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        String PolicyQuery = "SELECT StartDate FROM tblPolicy WHERE PolicyId = " + PolicyId;
        JSONArray Policy = sqlHandler.getResult(PolicyQuery, null);
        String StartDate = null;
        JSONObject O = null;
        try {
            O = Policy.getJSONObject(0);
            StartDate = O.getString("StartDate");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Date Paydate = format.parse(PayDate);
        Date Startdate = format.parse(StartDate);
        String Effectivedate = null;

        if (Paydate.after(Startdate))
            Effectivedate = PayDate;
        else
            Effectivedate = StartDate;


        values.put("PolicyStatus", String.valueOf(policystatus));
        values.put("EffectiveDate", Effectivedate);

        try {
            sqlHandler.updateData("tblPolicy", values, "PolicyId = ?", new String[]{String.valueOf(PolicyId)});
        } catch (UserException e) {
            e.printStackTrace();
        }
        return 1;//Update Success
    }


    private ProgressDialog pd = null;
    private ArrayList<String> enrolMessages = new ArrayList<>();

    @JavascriptInterface
    public void uploadEnrolment() throws Exception {

        pd = new ProgressDialog(mContext);
        pd = ProgressDialog.show(mContext, "", mContext.getResources().getString(R.string.Uploading));

        try {
            new Thread() {
                public void run() {
                    try {
                         enrol_result = Enrol(0, 0, 0, 0, 1);
                    } catch (UserException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    if(mylist.size() == 0){
                        ((Activity) mContext).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if(enrol_result != 999){
                                    //if error is encountered
                                    if (enrolMessages.size() > 0 && enrolMessages != null) {
                                        CharSequence[] charSequence = enrolMessages.toArray(new CharSequence[(enrolMessages.size())]);
                                        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                                        builder.setTitle(mContext.getResources().getString(R.string.UploadFailureReport));
                                        builder.setCancelable(false);
                                        builder.setItems(charSequence, null);
                                        builder.setPositiveButton(mContext.getResources().getString(R.string.Ok), new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                dialogInterface.dismiss();
                                            }
                                        });
                                        AlertDialog dialog = builder.create();
                                        dialog.show();
                                        enrolMessages.clear();

                                    }else{
                                        //deleteImage();
                                        ShowDialog(mContext.getResources().getString(R.string.FamilyUploaded));
                                    }
                                }else{
                                    ShowDialog(mContext.getResources().getString(R.string.NoFamilyUploaded));
                                }

                                //ShowDialog(mContext.getResources().getString(R.string.FamilyUploaded));
                            }

                        });
                    }


                    pd.dismiss();
                }
            }.start();
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception(e.getMessage());
        }


    }

/*    private void deleteImage() {
        File fdelete = new File(PhotoPath.trim());
        if (fdelete.exists()) {
            if (fdelete.delete()) {
                System.out.println("file Deleted :" + file_dj_path);
            } else {
                System.out.println("file not Deleted :" + file_dj_path);
            }
        }
    }*/

    private int Enrol(int oFamilyId, int oInsureeId, int oPolicyId, int oPremiumId, int CallerId) throws UserException, JSONException {
        mylist.clear();
        String Query;
        String[] args;
        int EnrolResult = oInsureeId;
        int IsOffline = 1;
        //Get all the families which are in offline state

        Query = "SELECT FamilyId,isOffline  FROM tblFamilies WHERE InsureeId != ''" +
                " ORDER BY FamilyId";
//        }
        JSONArray familiesToUpload = sqlHandler.getResult(Query, null);

        //Loop through each familyId and get Header, Insuree, Policy and Premium details
        for (int i = 0; i < familiesToUpload.length(); i++) {
            String FamilyId = null;
            String CHFNumber = null;
            JSONObject object = null;
            try {
                object = familiesToUpload.getJSONObject(i);
            } catch (JSONException e) {
                e.printStackTrace();
            }
           // try {
                FamilyId = object != null ? object.getString("FamilyId") : null;
                IsOffline = Integer.parseInt(object.getString("isOffline"));
        /*    } catch (JSONException e) {
                e.printStackTrace();
            }*/
            args = new String[]{FamilyId};
            Query = "SELECT F.FamilyId, F.InsureeId, F.LocationId, I.CHFID AS HOFCHFID, NULLIF(F.Poverty,'null') Poverty, NULLIF(F.FamilyType,'null') FamilyType, NULLIF(F.FamilyAddress,'null') FamilyAddress, NULLIF(F.Ethnicity,'null') Ethnicity, NULLIF(F.ConfirmationNo,'null') ConfirmationNo, F.ConfirmationType ConfirmationType,F.isOffline FROM tblFamilies F\n" +
                    "INNER JOIN tblInsuree I ON I.InsureeId = F.InsureeId WHERE F.FamilyId = ? AND F.InsureeId != ''";
            JSONArray familyArray = sqlHandler.getResult(Query, args);

            JSONArray newFamilyArray = new JSONArray();

            for(int j = 0;j < familyArray.length();j++){
                JSONObject ob = familyArray.getJSONObject(j);
                String typeofId = ob.getString("FamilyType");
                String ConfirmationType = ob.getString("ConfirmationType");
                if(typeofId == "0"){
                    ob.put("FamilyType", "");
                    newFamilyArray.put(ob);
                }
                if(ConfirmationType == "0"){
                    ob.put("ConfirmationType", "");
                    newFamilyArray.put(ob);
                }
                else{
                    newFamilyArray.put(ob);
                }
            }

            familyArray = newFamilyArray;


            //get Insureesf
            Query = "SELECT I.InsureeId, I.FamilyId, I.CHFID, I.LastName, I.OtherNames, I.DOB, I.Gender, NULLIF(I.Marital,'null') Marital, I.isHead, NULLIF(I.IdentificationNumber,'null') IdentificationNumber, NULLIF(I.Phone,'null') Phone, REPLACE(I.PhotoPath, RTRIM(PhotoPath, REPLACE(PhotoPath, '/', '')), '') PhotoPath, NULLIF(I.CardIssued,'null') CardIssued, NULLIF(I.Relationship,'null') Relationship, NULLIF(I.Profession,'null') Profession, NULLIF(I.Education,'null') Education, NULLIF(I.Email,'null') Email, CASE WHEN I.TypeOfId='null' THEN null ELSE I.TypeOfId END TypeOfId, NULLIF(I.HFID,'null') HFID, NULLIF(I.CurrentAddress,'null') CurrentAddress, NULLIF(I.GeoLocation,'null') GeoLocation, NULLIF(I.CurVillage,'null') CurVillage,I.isOffline \n" +
                    "FROM tblInsuree I \n" +
                    "WHERE I.InsureeId != ''\n" +
                    "AND I.FamilyId = " + FamilyId + " \n";

            if(IsOffline == 0 && CallerId == 1){
                Query += " AND  I.InsureeId < 0" + "";
            }
            if (CallerId == 0) Query += " AND  I.InsureeId = " + oInsureeId;
            JSONArray insureesArray = sqlHandler.getResult(Query, null);

            JSONObject O = null;


                //try {
            if(insureesArray.length() > 0){
                JSONObject o = insureesArray.getJSONObject(0);
                CHFNumber = o.getString("CHFID");


                JSONArray newInsureesArray = new JSONArray();

                for(int j = 0;j < insureesArray.length();j++){
                    JSONObject ob = insureesArray.getJSONObject(j);
                    String typeofId = ob.getString("TypeOfId");
                    if(typeofId == "0"){
                        ob.put("TypeOfId", "");
                        newInsureesArray.put(ob);
                    }else{
                        newInsureesArray.put(ob);
                    }
                }

                insureesArray = newInsureesArray;
            /*    } catch (JSONException e) {
                    e.printStackTrace();
                }*/
            }

                //get Policies
                Query = "SELECT PolicyId, FamilyId, EnrollDate, StartDate, NULLIF(EffectiveDate,'null') EffectiveDate, ExpiryDate, Policystatus, PolicyValue, ProdId, OfficerId, PolicyStage, isOffline\n" +
                        "FROM tblPolicy\n" +
                        "WHERE FamilyId =  " + FamilyId;
                if (CallerId == 0) Query += " AND PolicyId = " + oPolicyId;
                JSONArray policiesArray = sqlHandler.getResult(Query, null);
/*            if(policiesArray.length() == 0){
                if(!getRule("AllowFamilyWithoutPolicy")){
                    String chfid = null;
                    String lastname = null;
                    String othername = null;
                    try {
                        JSONObject insuree = insureesArray.getJSONObject(0);
                        chfid = insuree.getString("CHFID");
                        lastname = insuree.getString("LastName");
                        othername = insuree.getString("OtherNames");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    mylist.add("Family " + chfid + " " + " " + lastname + " " + " " + othername + " " + mContext.getResources().getString(R.string.WithoutPolicy));
                }
            }*/
            if (insureesArray.length() > 0 || policiesArray.length() != 0 || familyArray.length() > 0) {

                //get Premiums
                Query = "SELECT PR.PremiumId, PR.PolicyId, NULLIF(PR.PayerId,'null') PayerId, PR.Amount, PR.Receipt, PR.PayDate, PR.PayType, PR.isPhotoFee,PR.isOffline\n" +
                        "FROM tblPremium PR\n" +
                        "INNER JOIN tblPolicy PL ON PL.PolicyId = PR.PolicyId \n" +
                        "WHERE PL.FamilyId =  " + FamilyId;

                if (CallerId == 0){
                    Query += " AND PR.PremiumId  = " + oPremiumId;
                }
                JSONArray premiumsArray = sqlHandler.getResult(Query, null);

                if(CallerId == 1){
                    if(IsOffline == 1){
                        if(premiumsArray.length() == 0){
                            if(!getRule("AllowPolicyWithoutPremium")){
                                String chfid = null;
                                String lastname = null;
                                String othername = null;
                                try {
                                    JSONObject insuree = insureesArray.getJSONObject(0);
                                    chfid = insuree.getString("CHFID");
                                    lastname = insuree.getString("LastName");
                                    othername = insuree.getString("OtherNames");
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                mylist.add("Family " + chfid + " " + " " + lastname + " " + " " + othername + " " + mContext.getResources().getString(R.string.WithoutPolicyPremium));
                            }
                        }
                    }else{
                        if(policiesArray.length() != 0){
                            if(premiumsArray.length() == 0){
                                if(!getRule("AllowPolicyWithoutPremium")){
                                    String chfid = null;
                                    String lastname = null;
                                    String othername = null;
                                    try {
                                        JSONObject insuree = insureesArray.getJSONObject(0);
                                        chfid = insuree.getString("CHFID");
                                        lastname = insuree.getString("LastName");
                                        othername = insuree.getString("OtherNames");
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    mylist.add("Family " + chfid + " " + " " + lastname + " " + " " + othername + " " + mContext.getResources().getString(R.string.WithoutPolicyPremium));

                                }
                            }
                        }
                    }
                }




                JSONObject objEnrol = new JSONObject();
                //try {
                objEnrol.put("Family", familyArray);
       /*     } catch (JSONException e) {
                e.printStackTrace();
            }*/
                String Family = objEnrol.toString();

                objEnrol = new JSONObject();

                try {
                    objEnrol.put("Insuree", insureesArray);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                String Insuree = String.valueOf(objEnrol);

                objEnrol = new JSONObject();
                //try {
                objEnrol.put("Policy", policiesArray);
/*            } catch (JSONException e) {
                e.printStackTrace();
            }*/
                String Policy = objEnrol.toString();

                objEnrol = new JSONObject();
                //try {
                objEnrol.put("Premium", premiumsArray);
 /*           } catch (JSONException e) {
                e.printStackTrace();
            }*/
                String Premium = objEnrol.toString();

                /// String enrol = "test";

                CallSoap cs = new CallSoap();
                cs.setFunctionName("EnrollFamily");
                global = (Global) mContext.getApplicationContext();
                InsureeImages[] InsureeImages = FamilyPictures(insureesArray);

                //if(CallerId ==  0 && IsOffline == 1)
                if(mylist.size() == 0){
                    EnrolResult = cs.EnrollFamily(Family, Insuree, Policy, Premium, global.getUserId(), global.getOfficerId(),InsureeImages);
                }else {
                    addCategoryBox();
                }
//             EnrolResult=1001;
                if (EnrolResult >= 0) {
                    if (IsOffline == 0 && EnrolResult > 0) {
                        ContentValues values = new ContentValues();
                        String UpdateQuery = "";
                        values.put("isOffline", 2);
                        if (oPremiumId != 0 && premiumsArray.length() > 0) {
                            values.put("PremiumId", EnrolResult);
                            sqlHandler.updateData("tblPremium", values, "PremiumId = ?", new String[]{String.valueOf(oPremiumId)});
                            rtEnrolledId = EnrolResult;
                        } else if (oPolicyId != 0 && policiesArray.length() > 0) {
//                        values.put("PolicyId", EnrolResult);
//                        sqlHandler.updateData("tblPolicy", values, "PolicyId = ?", new String[]{String.valueOf(oPolicyId)});
                            UpdateQuery = "UPDATE tblPolicy SET PolicyId = " + EnrolResult + ", isOffline = 2 WHERE PolicyId = " + oPolicyId + " AND isOffline = 0";
                            sqlHandler.getResult(UpdateQuery, null);
                            rtEnrolledId = EnrolResult;

                        } else if (oInsureeId != 0 && insureesArray.length() > 0) {
                            values.put("InsureeId", EnrolResult);
                            sqlHandler.updateData("tblInsuree", values, "InsureeId = ?", new String[]{String.valueOf(oInsureeId)});
                            rtEnrolledId = EnrolResult;
                        } else if (oFamilyId > 0 && oInsureeId == 0 && oPolicyId == 0 && oPremiumId == 0) {
                            sqlHandler.updateData("tblFamilies", values, "FamilyId = ?", new String[]{String.valueOf(oFamilyId)});
                        }
                    }

                    if(mylist.size() == 0){
                        if (CallerId == 1) {
                            DeleteImages(insureesArray);
                            DeleteUploadedData(Integer.parseInt(FamilyId));
                            if (IsOffline == 0){
                                DeleteFamily(Integer.parseInt(FamilyId));
                            }
                        }
                    }


                } else {
                    String ErrMsg = null;
                    switch (EnrolResult) {

                        case -1:
                            ErrMsg = "[" + CHFNumber + "] " + mContext.getString(R.string.MissingHOF);
                            break;
                        case -2:
                            ErrMsg = "[" + CHFNumber + "] " + mContext.getString(R.string.DuplicateHOF);
                            break;
                        case -3:
                            ErrMsg = "[" + CHFNumber + "] " + mContext.getString(R.string.DuplicateInsuranceNumber);
                            break;
                        case -4:
                            ErrMsg = "[" + CHFNumber + "] " + mContext.getString(R.string.DuplicateReceiptNumber);
                            break;
                        case -6:
                            ErrMsg = "[" + CHFNumber + "] " + mContext.getString(R.string.Interuption);
                            break;
                        case -400:
                            ErrMsg = "[" + CHFNumber + "] " + mContext.getString(R.string.ServerError);
                            break;
                        default:
                            ErrMsg = "[" + CHFNumber + "] " + mContext.getString(R.string.UncaughtException);
                    }
                    enrolMessages.add(ErrMsg);
                }

            }else{
                EnrolResult = 0;

                if (CallerId == 1) {
                    DeleteImages(insureesArray);
                    DeleteUploadedData(Integer.parseInt(FamilyId));
                    if (IsOffline == 0){
                        DeleteFamily(Integer.parseInt(FamilyId));
                    }
                }
            }

        }
        if (rtEnrolledId > 0) return rtEnrolledId;
        return EnrolResult;
    }

    public InsureeImages[] FamilyPictures(JSONArray insurees){

        InsureeImages[] images = new InsureeImages[insurees.length()];

        String PhotoPath = null;
        String FileName = "";
        String chfid = null;
        String lastname = null;
        String othername = null;
        int IsOffline = 1;
        JSONObject Insureeobject = null;
        for (int j = 0; j < insurees.length(); j++) {
            try {
                Insureeobject = insurees.getJSONObject(j);
                PhotoPath = (Insureeobject.getString("PhotoPath"));
                chfid = (Insureeobject.getString("CHFID"));
                lastname = (Insureeobject.getString("LastName"));
                othername = (Insureeobject.getString("OtherNames"));
                IsOffline = Integer.parseInt(Insureeobject.getString("isOffline"));

                if (PhotoPath.length() > 0 && !PhotoPath.equals("null") && PhotoPath != null) {
                    FileName = PhotoPath;
                    final String newfile = FileName;

                    files = GetListOfImages(global.getImageFolder(), newfile);

                    if(files.length > 0) {
                        int size = (int) files[0].length();
                        byte[] imgcontent = new byte[size];
                        try{
                            BufferedInputStream buf = new BufferedInputStream(new FileInputStream(files[0]));
                            buf.read(imgcontent,0,imgcontent.length);
                            buf.close();

                            InsureeImages img = new InsureeImages(files[0].getName(),imgcontent);
                            images[j] = img;
                        }
                        catch (FileNotFoundException e){
                            e.printStackTrace();
                        }
                        catch(IOException e){
                            e.printStackTrace();
                        }
                    }
                    else{
                        byte[] imgcontent = new byte[0];
                        InsureeImages img = new InsureeImages("",imgcontent);
                        images[j] = img;
                    }

                }else{
                    if(IsOffline == 1){
                        if(getRule("AllowInsureeWithoutPhoto")){
                            byte[] empty = new byte[0];
                            InsureeImages img = new InsureeImages("",empty);
                            images[j] = img;
                        }else {
                            mylist.add("Insuree " + chfid + " " + " " + lastname + " " + " " + othername + " " + mContext.getResources().getString(R.string.WithoutPhoto));
                        }
                    }else {
                        byte[] empty = new byte[0];
                        InsureeImages img = new InsureeImages("",empty);
                        images[j] = img;
                    }

                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

        return images;
    }

/*    public byte[] extractBytes (String ImageName) throws IOException {
        // open image
        File imgPath = new File(ImageName);
        BufferedImage bufferedImage = ImageIO.read(imgPath);

        // get DataBufferBytes from Raster
        WritableRaster raster = bufferedImage .getRaster();
        DataBufferByte data   = (DataBufferByte) raster.getDataBuffer();

        return ( data.getData() );
    }*/

    @JavascriptInterface
    public void UploadImages(final String Filename) {

        files = GetListOfImages(global.getImageFolder(), Filename);
        UploadFile uf = new UploadFile();
        if(files.length > 0){
            if (uf.isValidFTPCredentials()) {
                for (int i = 0; i < files.length; i++) {
                    UploadCounter = i + 1;
                    if (uf.uploadFileToServer(mContext, files[i], "tz.co.exact.imis")) {
                        files[i].delete();
                    }
                }
            }
        }
/*        new Thread() {
            public void run() {
                files = GetListOfImages(global.getImageFolder(), Filename);
                UploadFile uf = new UploadFile();
                if(files.length > 0){
                    if (uf.isValidFTPCredentials()) {

                        for (int i = 0; i < files.length; i++) {
                            UploadCounter = i + 1;
                            if (uf.uploadFileToServer(mContext, files[i], "tz.co.exact.imis")) {
                                files[i].delete();
                            }
                        }
                    }
                }

            }
        }.start();*/

    }
    public void DeleteImages(JSONArray insurees) {

        String PhotoPath = null;
        String FileName = "";
        JSONObject Insureeobject = null;
        for (int j = 0; j < insurees.length(); j++) {
            try {
                Insureeobject = insurees.getJSONObject(j);
                PhotoPath = (Insureeobject.getString("PhotoPath"));
                if (PhotoPath.length() > 0 && !PhotoPath.equals("null") && PhotoPath != null) {
                    FileName = PhotoPath;
                    final String newfile = FileName;

                    files = GetListOfImages(global.getImageFolder(), newfile);
                    if(files.length > 0){
                        for (int i = 0; i < files.length; i++) {
                            files[i].delete();
                        }
                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

    }

    public void addCategoryBox(){

        ((Activity) mContext).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // get prompts.xml view
                LayoutInflater li = LayoutInflater.from(mContext);
                View promptsView = li.inflate(R.layout.error_message, null);

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mContext);

                // set prompts.xml to alertdialog builder
                alertDialogBuilder.setView(promptsView);

                final TextView textView1 = (TextView) promptsView.findViewById(R.id.textView1);
                final RecyclerView error_message = (RecyclerView) promptsView.findViewById(R.id.error_message);

                enrollmentReport = new EnrollmentReport(mContext,mylist);

                error_message.setLayoutManager(new LinearLayoutManager(mContext));
                error_message.addItemDecoration(new DividerItemDecoration(mContext,DividerItemDecoration.VERTICAL));
                error_message.setAdapter(enrollmentReport);

                String title = mContext.getString(R.string.failedToUpload);
                textView1.setText(title.toUpperCase());
                // set dialog message
                alertDialogBuilder
                        .setCancelable(false)
                        .setPositiveButton("Ok",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,int id) {
                                        dialog.cancel();
                                    }
                                });

                // create alert dialog
                AlertDialog alertDialog = alertDialogBuilder.create();

                // show it
                alertDialog.show();
            }
        });

    }


    public Boolean isOfficerCodeValid(String OfficerCode) throws JSONException {
        String Query = "SELECT OfficerId , OtherNames || ' ' || LastName AS OfficerName\n" +
                "FROM tblOfficer WHERE LOWER(Code)= LOWER(?)";
        String arg[] = {OfficerCode};
        JSONArray Officer = sqlHandler.getResult(Query, arg);
        //  Officer.toString();
        int Count = Officer.length();
        String OfficerName = null;
        JSONObject object;
        if (Count > 0) {
            object = Officer.getJSONObject(0);
            global = (Global) mContext.getApplicationContext();
            int OfficerId = Integer.parseInt(object.getString("OfficerId"));
            OfficerName = object.getString("OfficerName");
            global.setOfficerId(OfficerId);
            global.setOfficerName(OfficerName);
            Officer.toString();
            return true;
        } else {
            return false;
        }
    }


    @JavascriptInterface
    public int isValidLogin(final String Username, final String Password) throws InterruptedException {
        global = (Global) mContext.getApplicationContext();
        CallSoap cs = new CallSoap();
        cs.setFunctionName("isValidLogin");
        UserId = cs.isUserLoggedIn(Username, Password);
        global.setUserId(UserId);
        ((Activity) mContext).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                MainActivity.SetLogedIn(mContext.getResources().getString(R.string.Login), mContext.getResources().getString(R.string.Logout));
            }
        });
        return UserId;
    }

    private void DeleteUploadedData(int FamilyId) {
        String DeletePolicyPayment = "DELETE FROM tblPremium\n" +
                "WHERE PolicyId IN (SELECT PolicyId FROM tblPolicy WHERE FamilyId = " + FamilyId + " AND isOffline != 0 ) ";
        String DeleteFamilyPolicy = "DELETE FROM tblPolicy WHERE FamilyId=" + FamilyId + "  AND isOffline != 0";
        String DeleteFamilyInsuree = "DELETE FROM tblInsuree WHERE FamilyId=" + FamilyId + "  AND isOffline != 0";
        String DeleteFamilyQuery = "DELETE FROM tblFamilies WHERE FamilyId = " + FamilyId + "  AND isOffline != 0";
        sqlHandler.getResult(DeletePolicyPayment, null);
        sqlHandler.getResult(DeleteFamilyPolicy, null);
        sqlHandler.getResult(DeleteFamilyInsuree, null);
        sqlHandler.getResult(DeleteFamilyQuery, null);

    }


    @JavascriptInterface
    public Boolean UploadOfflineFeedbackRenewal(final String ActivityName) {
        //General _General = new General();
        files = GetListOfFiles(Path, "feedbackRenewal", null);
        TotalFiles = files.length;
        if (TotalFiles == 0) {
            ShowDialog(mContext.getResources().getString(R.string.NoFiles));
            //Clean tables in database as well
            DeleteFeedBackRenewal();
            return false;
        }
        if (!general.isNetworkAvailable(mContext)) {
            ShowDialog(mContext.getResources().getString(R.string.NoInternet));
            return false;
        }
        final ProgressDialog pd;
        pd = ProgressDialog.show(mContext, "", mContext.getResources().getString(R.string.Uploading));

        new Thread() {
            public void run() {
                UploadFile uf = new UploadFile();
                //Check if valid ftp credentials are available
                if (uf.isValidFTPCredentials()) {

                    for (int i = 0; i < files.length; i++) {
                        UploadCounter = i + 1;
                        //runOnUiThread(ChangeMessage);
                        if (uf.uploadFileToServer(mContext, files[i], ActivityName)) {
                            XMLFile = files[i];
                            CallSoap cs = new CallSoap();
                            if (XMLFile.getName().contains("RenPol_")) {
                                cs.setFunctionName("isValidRenewal");
                                int response = cs.isPolicyAccepted(XMLFile.getName());
                                if (response == 1)
                                    MoveFile(XMLFile,1);
                                else
                                    MoveFile(XMLFile, 2);
                                result = 1;
                            } else if (XMLFile.getName().contains("feedback_")) {
                                cs.setFunctionName("isValidFeedback");
                                int response = cs.isFeedbackAccepted(XMLFile.getName());
                                if (response == 1)
                                    MoveFile(XMLFile,1);
                                else
                                    MoveFile(XMLFile, 2);
                                result = 1;
                            } else
                                result = 2;
                        }
                    }
                    ((Activity) mContext).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ShowDialog(mContext.getResources().getString(R.string.RenewalUploaded));
                        }
                    });
                } else {
                    result = -1;
                }

                new Runnable() {
                    @Override
                    public void run() {
                        switch (result) {
                            case -1:
                                ShowDialog(mContext.getResources().getString(R.string.FTPConnectionFailed));
                                break;
                            default:
                                ShowDialog(mContext.getResources().getString(R.string.BulkUpload));
                        }
                    }
                };

                pd.dismiss();
            }

        }.start();
        return true;
    }

    private File[] GetListOfFiles(String DirectoryPath, final String FileType, final String FileName) {
        File Directory = new File(DirectoryPath);
        final Pattern p = Pattern.compile("(RenPol_)");
        FilenameFilter filter = new FilenameFilter() {

            @Override
            public boolean accept(File dir, String filename) {
                if (FileType == "Image") {
                    //  return filename.equalsIgnoreCase(FileName);
                    return filename.endsWith(".jpg");
                } else {
                    return filename.startsWith("RenPol_") || filename.startsWith("feedback_");
                }
            }
        };
        return Directory.listFiles(filter);
    }

    private File[] GetListOfImages(String DirectoryPath, final String FileName) {
        File Directory = new File(DirectoryPath);
        FilenameFilter filter = new FilenameFilter() {

            @Override
            public boolean accept(File dir, String filename) {
                return filename.equalsIgnoreCase(FileName);
            }
        };
        return Directory.listFiles(filter);
    }

    @JavascriptInterface
    public String GetListOfImagesContain(final String FileName) {
        File[] Photos = null;
        global = (Global) mContext.getApplicationContext();
        File Directory = new File(global.getImageFolder());

        FilenameFilter filter = new FilenameFilter() {

            @Override
            public boolean accept(File dir, String filename) {
                return filename.startsWith(FileName + "_");
            }
        };
        Photos = Directory.listFiles(filter);
        String newFileName = "";
        if (Photos.length > 0){
            newFileName = Photos[Photos.length - 1].toString();
        }
        return newFileName;
    }

    private void MoveFile(File file, int res) {
        String Accepted = "", Rejected = "";
        if (file.getName().contains("RenPol_")) {
            Accepted = "AcceptedRenewal/";
            Rejected = "RejectedRenewal/";
        } else if (file.getName().contains("feedback_")) {
            Accepted = "AcceptedFeedback/";
            Rejected = "RejectedFeedback/";
        }

        switch (res) {
            case 1:
                file.renameTo(new File(Path + Accepted + file.getName()));
                break;
            case 2:
                file.renameTo(new File(Path + Rejected + file.getName()));
                break;
        }
    }


    private void RegisterUploadDetails(String ImageName) {
        String[] FileName = ImageName.split("_");
        String CHFID, OfficerCode;
        if (FileName.length > 0) {
            CHFID = FileName[0];
            OfficerCode = FileName[1];
            CallSoap cs = new CallSoap();
            cs.setFunctionName("InsertPhotoEntry");
            cs.InsertPhotoEntry(CHFID, OfficerCode, ImageName);
        }
    }
    @JavascriptInterface
    public void popUpOfficerDialog(){
        ((MainActivity) mContext).ShowDialogTex();
    }
    @JavascriptInterface
    public boolean downloadMasterData() {
        ProgressDialog pd = null;
        pd = ProgressDialog.show(mContext, mContext.getResources().getString(R.string.Sync), mContext.getResources().getString(R.string.DownloadingMasterData));
        final ProgressDialog finalPd = pd;
        new Thread() {
            public void run() {
                try {
                    startDownloading();
                    finalPd.dismiss();

                    ((Activity) mContext).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            //ShowDialog(mContext.getResources().getString(R.string.DataDownloadedSuccess));
                        }
                    });


                } catch (JSONException e) {
                    e.printStackTrace();
                    finalPd.dismiss();
                } catch (UserException e) {
                    e.printStackTrace();
                    finalPd.dismiss();
                }
            }
        }.start();


        global.setOfficerCode("");
        Intent refresh = new Intent(mContext, MainActivity.class);
        mContext.startActivity(refresh);
        ((MainActivity)mContext).finish();
        return true;
        //        finally {
//            pd.dismiss();
//        }


    }
    public void importMasterData(String data) throws JSONException, UserException {

        String MD = data;
        JSONArray masterData = new JSONArray(MD);

        if (masterData.length() == 0)
            throw new UserException(mContext.getResources().getString(R.string.DownloadMasterDataFailed));

        //Sequence of table
        /*
            1   :   ConfirmationTypes
            2   :   Controls
            3   :   Education
            4   :   FamilyTypes
            5   :   HF
            6   :   IdentificationTypes
            7   :   Languages
            8   :   Locations
            9   :   Officers
            10  :   Payers
            11  :   Products
            12  :   Professions
            13  :   Relations
            14  :   PhoneDefaults
         */

        JSONArray ConfirmationTypes = new JSONArray();
        JSONArray Controls = new JSONArray();
        JSONArray Education = new JSONArray();
        JSONArray FamilyTypes = new JSONArray();
        JSONArray HF = new JSONArray();
        JSONArray IdentificationTypes = new JSONArray();
        JSONArray Languages = new JSONArray();
        JSONArray Locations = new JSONArray();
        JSONArray Officers = new JSONArray();
        JSONArray Payers = new JSONArray();
        JSONArray Products = new JSONArray();
        JSONArray Professions = new JSONArray();
        JSONArray Relations = new JSONArray();
        JSONArray PhoneDefaults = new JSONArray();

        for (int i = 0; i < masterData.length(); i++) {
            String keyName = masterData.getJSONObject(i).keys().next();
            switch (keyName.toLowerCase()) {
                case "confirmationtypes":
                    ConfirmationTypes = (JSONArray) masterData.getJSONObject(i).get(keyName);
                    break;
                case "controls":
                    Controls = (JSONArray) masterData.getJSONObject(i).get(keyName);
                    break;
                case "education":
                    Education = (JSONArray) masterData.getJSONObject(i).get(keyName);
                    break;
                case "familytypes":
                    FamilyTypes = (JSONArray) masterData.getJSONObject(i).get(keyName);
                    break;
                case "hf":
                    HF = (JSONArray) masterData.getJSONObject(i).get(keyName);
                    break;
                case "identificationtypes":
                    IdentificationTypes = (JSONArray) masterData.getJSONObject(i).get(keyName);
                    break;
                case "languages":
                    Languages = (JSONArray) masterData.getJSONObject(i).get(keyName);
                    break;
                case "locations":
                    Locations = (JSONArray) masterData.getJSONObject(i).get(keyName);
                    break;
                case "officers":
                    Officers = (JSONArray) masterData.getJSONObject(i).get(keyName);
                    break;
                case "payers":
                    Payers = (JSONArray) masterData.getJSONObject(i).get(keyName);
                    break;
                case "products":
                    Products = (JSONArray) masterData.getJSONObject(i).get(keyName);
                    break;
                case "professions":
                    Professions = (JSONArray) masterData.getJSONObject(i).get(keyName);
                    break;
                case "relations":
                    Relations = (JSONArray) masterData.getJSONObject(i).get(keyName);
                    break;
                case "phonedefaults":
                    PhoneDefaults = (JSONArray) masterData.getJSONObject(i).get(keyName);
                    break;
            }
        }

        insertConfirmationTypes(ConfirmationTypes);
        insertControls(Controls);
        insertEducation(Education);
        insertFamilyTypes(FamilyTypes);
        insertHF(HF);
        insertIdentificationTypes(IdentificationTypes);
        insertLanguages(Languages);
        insertLocations(Locations);
        insertOfficers(Officers);
        insertPayers(Payers);
        insertProducts(Products);
        insertProfessions(Professions);
        insertRelations(Relations);
        insertPhoneDefaults(PhoneDefaults);

    }

    @SuppressWarnings("ConstantConditions")
    public void startDownloading() throws JSONException, UserException {

        CallSoap cs = new CallSoap();
        cs.setFunctionName("downloadMasterData");
        String MD = cs.downloadMasterData();
        JSONArray masterData = new JSONArray(MD);

        if (masterData.length() == 0)
            throw new UserException(mContext.getResources().getString(R.string.DownloadMasterDataFailed));

        //Sequence of table
        /*
            1   :   ConfirmationTypes
            2   :   Controls
            3   :   Education
            4   :   FamilyTypes
            5   :   HF
            6   :   IdentificationTypes
            7   :   Languages
            8   :   Locations
            9   :   Officers
            10  :   Payers
            11  :   Products
            12  :   Professions
            13  :   Relations
            14  :   PhoneDefaults
         */

        JSONArray ConfirmationTypes = new JSONArray();
        JSONArray Controls = new JSONArray();
        JSONArray Education = new JSONArray();
        JSONArray FamilyTypes = new JSONArray();
        JSONArray HF = new JSONArray();
        JSONArray IdentificationTypes = new JSONArray();
        JSONArray Languages = new JSONArray();
        JSONArray Locations = new JSONArray();
        JSONArray Officers = new JSONArray();
        JSONArray Payers = new JSONArray();
        JSONArray Products = new JSONArray();
        JSONArray Professions = new JSONArray();
        JSONArray Relations = new JSONArray();
        JSONArray PhoneDefaults = new JSONArray();

        for (int i = 0; i < masterData.length(); i++) {
            String keyName = masterData.getJSONObject(i).keys().next();
            switch (keyName.toLowerCase()) {
                case "confirmationtypes":
                    ConfirmationTypes = (JSONArray) masterData.getJSONObject(i).get(keyName);
                    break;
                case "controls":
                    Controls = (JSONArray) masterData.getJSONObject(i).get(keyName);
                    break;
                case "education":
                    Education = (JSONArray) masterData.getJSONObject(i).get(keyName);
                    break;
                case "familytypes":
                    FamilyTypes = (JSONArray) masterData.getJSONObject(i).get(keyName);
                    break;
                case "hf":
                    HF = (JSONArray) masterData.getJSONObject(i).get(keyName);
                    break;
                case "identificationtypes":
                    IdentificationTypes = (JSONArray) masterData.getJSONObject(i).get(keyName);
                    break;
                case "languages":
                    Languages = (JSONArray) masterData.getJSONObject(i).get(keyName);
                    break;
                case "locations":
                    Locations = (JSONArray) masterData.getJSONObject(i).get(keyName);
                    break;
                case "officers":
                    Officers = (JSONArray) masterData.getJSONObject(i).get(keyName);
                    break;
                case "payers":
                    Payers = (JSONArray) masterData.getJSONObject(i).get(keyName);
                    break;
                case "products":
                    Products = (JSONArray) masterData.getJSONObject(i).get(keyName);
                    break;
                case "professions":
                    Professions = (JSONArray) masterData.getJSONObject(i).get(keyName);
                    break;
                case "relations":
                    Relations = (JSONArray) masterData.getJSONObject(i).get(keyName);
                    break;
                case "phonedefaults":
                    PhoneDefaults = (JSONArray) masterData.getJSONObject(i).get(keyName);
                    break;
            }
        }

        insertConfirmationTypes(ConfirmationTypes);
        insertControls(Controls);
        insertEducation(Education);
        insertFamilyTypes(FamilyTypes);
        insertHF(HF);
        insertIdentificationTypes(IdentificationTypes);
        insertLanguages(Languages);
        insertLocations(Locations);
        insertOfficers(Officers);
        insertPayers(Payers);
        insertProducts(Products);
        insertProfessions(Professions);
        insertRelations(Relations);
        insertPhoneDefaults(PhoneDefaults);

    }

    private boolean insertConfirmationTypes(JSONArray jsonArray) throws JSONException {
        String Columns[] = {"ConfirmationTypeCode", "ConfirmationType", "SortOrder", "AltLanguage"};
        sqlHandler.insertData("tblConfirmationTypes", Columns, jsonArray.toString(), "DELETE FROM tblConfirmationTypes");
        return true;
    }

    private boolean insertControls(JSONArray jsonArray) throws JSONException {
        String Columns[] = {"FieldName", "Adjustibility"};
        sqlHandler.insertData("tblControls", Columns, jsonArray.toString(), "DELETE FROM tblControls;");
        return true;
    }

    private boolean insertEducation(JSONArray jsonArray) throws JSONException {
        String Columns[] = {"EducationId", "Education", "SortOrder", "AltLanguage"};
        sqlHandler.insertData("tblEducations", Columns, jsonArray.toString(), "DELETE FROM tblEducations;");
        return true;
    }

    private boolean insertFamilyTypes(JSONArray jsonArray) throws JSONException {
        String Columns[] = {"FamilyTypeCode", "FamilyType", "SortOrder", "AltLanguage"};
        sqlHandler.insertData("tblFamilyTypes", Columns, jsonArray.toString(), "DELETE FROM tblFamilyTypes;");
        return true;
    }

    private boolean insertHF(JSONArray jsonArray) throws JSONException {
        String Columns[] = {"HFID", "HFCode", "HFName", "LocationId", "HFLevel"};
        sqlHandler.insertData("tblHF", Columns, jsonArray.toString(), "DELETE FROM tblHF;");
        return true;
    }

    private boolean insertIdentificationTypes(JSONArray jsonArray) throws JSONException {
        String Columns[] = {"IdentificationCode", "IdentificationTypes", "AltLanguage", "SortOrder"};
        sqlHandler.insertData("tblIdentificationTypes", Columns, jsonArray.toString(), "DELETE FROM tblIdentificationTypes;");
        return true;
    }

    private boolean insertLanguages(JSONArray jsonArray) throws JSONException {
        String Columns[] = {"LanguageCode", "LanguageName", "SortOrder"};
        sqlHandler.insertData("tblLanguages", Columns, jsonArray.toString(), "DELETE FROM tblLanguages;");
        return true;
    }

    private boolean insertLocations(JSONArray jsonArray) throws JSONException {
        String Columns[] = {"LocationId", "LocationCode", "LocationName", "ParentLocationId", "LocationType"};
        sqlHandler.insertData("tblLocations", Columns, jsonArray.toString(), "DELETE FROM tblLocations;");

        return true;
    }

    private boolean insertOfficers(JSONArray jsonArray) throws JSONException {
        String Columns[] = {"OfficerId", "Code", "LastName", "OtherNames", "Phone", "LocationId", "OfficerIDSubst", "WorksTo"};
        sqlHandler.insertData("tblOfficer", Columns, jsonArray.toString(), "DELETE FROM tblOfficer;");
        return true;
    }

    private boolean insertPayers(JSONArray jsonArray) throws JSONException {
        String Columns[] = {"payerId", "PayerName", "LocationId"};
        sqlHandler.insertData("tblPayer", Columns, jsonArray.toString(), "DELETE FROM tblPayer;");
        return true;
    }

    private boolean insertProducts(JSONArray jsonArray) throws JSONException {
        String Columns[] = {"ProdId", "ProductCode", "ProductName", "LocationId", "InsurancePeriod", "DateFrom", "DateTo", "ConversionProdId", "Lumpsum", "MemberCount", "PremiumAdult", "PremiumChild", "RegistrationLumpsum", "RegistrationFee", "GeneralAssemblyLumpSum", "GeneralAssemblyFee", "StartCycle1", "StartCycle2", "StartCycle3", "StartCycle4", "GracePeriodRenewal", "MaxInstallments", "WaitingPeriod", "Threshold", "RenewalDiscountPerc", "RenewalDiscountPeriod", "AdministrationPeriod", "EnrolmentDiscountPerc", "EnrolmentDiscountPeriod", "GracePeriod"};
        sqlHandler.insertData("tblProduct", Columns, jsonArray.toString(), "DELETE FROM tblProduct;");
        return true;
    }

    private boolean insertProfessions(JSONArray jsonArray) throws JSONException {
        String Columns[] = {"ProfessionId", "Profession", "SortOrder", "AltLanguage"};
        sqlHandler.insertData("tblProfessions", Columns, jsonArray.toString(), "DELETE FROM tblProfessions;");
        return true;
    }

    private boolean insertRelations(JSONArray jsonArray) throws JSONException {
        String Columns[] = {"Relationid", "Relation", "SortOrder", "AltLanguage"};
        sqlHandler.insertData("tblRelations", Columns, jsonArray.toString(), "DELETE FROM tblRelations;");
        return true;
    }
    private boolean insertPhoneDefaults(JSONArray jsonArray) throws JSONException {
        String Columns[] = {"RuleName", "RuleValue"};
        sqlHandler.insertData("tblIMISDefaultsPhone", Columns, jsonArray.toString(), "DELETE FROM tblIMISDefaultsPhone;");
        return true;
    }


    public int isMasterDataAvailable() {
        String Query = "SELECT * FROM tblLanguages";
        JSONArray Languages = sqlHandler.getResult(Query, null);
        return Languages.length();

    }

    public JSONArray getLanguage() {
        String Query = "SELECT * FROM tblLanguages";

        return sqlHandler.getResult(Query, null);
    }


    @JavascriptInterface
    public int getTotalFamily() {

        String FamilyQuery = "SELECT count(1) Families  FROM  tblfamilies WHERE isoffline = 1 OR isoffline = 0"; // WHERE isoffline = 1 OR isoffline = 0
        JSONArray Families = sqlHandler.getResult(FamilyQuery, null);
        JSONObject object = null;
        int TotalFamilies = 0;
        try {
            object = Families.getJSONObject(0);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            TotalFamilies = Integer.parseInt(object != null ? object.getString("Families") : null);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return TotalFamilies;
    }

    @JavascriptInterface
    public int getTotalInsuree() {
        String InsureeQuery = "SELECT count(1) Insuree FROM tblInsuree WHERE isoffline !=''"; //WHERE isoffline = 1 OR isoffline = 0
        JSONArray Insuree = sqlHandler.getResult(InsureeQuery, null);
        JSONObject object = null;
        int TotalInsuree = 0;
        try {
            object = Insuree.getJSONObject(0);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            TotalInsuree = Integer.parseInt(object != null ? object.getString("Insuree") : null);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return TotalInsuree;
    }

    @JavascriptInterface
    public int getTotalPolicy() {
        String PolicyQuery = "SELECT count(1) Policies  FROM  tblPolicy WHERE isoffline = 1 OR isoffline = 0"; //WHERE isoffline = 1 OR isoffline = 0
        JSONArray Policy = sqlHandler.getResult(PolicyQuery, null);
        JSONObject object = null;
        int TotalPolicies = 0;
        try {
            object = Policy.getJSONObject(0);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            TotalPolicies = Integer.parseInt(object != null ? object.getString("Policies") : null);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return TotalPolicies;
    }

    @JavascriptInterface
    public int getTotalPremium() {
        String PremiumQuery = "SELECT count(1) Premiums  FROM  tblPremium WHERE isoffline = 1 OR isoffline = 0"; // WHERE isoffline = 1 OR isoffline = 0
        JSONArray Premium = sqlHandler.getResult(PremiumQuery, null);
        JSONObject object = null;
        int TotalPremiums = 0;
        try {
            object = Premium.getJSONObject(0);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            TotalPremiums = Integer.parseInt(object != null ? object.getString("Premiums") : null);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return TotalPremiums;
    }

//    @JavascriptInterface
//    public Bitmap ResizeImage(String ImagePath, int newSize) {
//        try {
//            File file = new File(ImagePath);
//            //Decode image size
//            BitmapFactory.Options options = new BitmapFactory.Options();
//            options.inJustDecodeBounds = true;
//
//            BitmapFactory.decodeStream(new FileInputStream(file), null, options);
//
//
//            //Docode with new size
//            BitmapFactory.Options options1 = new BitmapFactory.Options();
//          //  options1.inSampleSize = scale;
//
//            Bitmap newImage = BitmapFactory.decodeStream(new FileInputStream(file), null, options1);
//            String outputFileName = global.getImageFolder() + "TestImageSize" + "_" + "OfficerCode_"  + "_0_0.jpeg";
//           Bitmap lastBit= Bitmap.createScaledBitmap(newImage,(int)(newImage.getWidth()*0.8), (int)(newImage.getHeight()*0.8), true);
//            OutputStream outputStream = new FileOutputStream(outputFileName);
//          //  lastBit.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
//
//            outputStream.close();
//
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return  null;
//    }


    public String getPayer(int LocationId) {
        String Query = "SELECT D.LocationId DistrictId , R.LocationId RegionId  FROM tblLocations V\n" +
                "\tINNER JOIN tblLocations W ON W.LocationId = V.ParentLocationId \n" +
                "\tINNER JOIN tblLocations D ON D.LocationId = W.ParentLocationId \n" +
                "\tINNER JOIN tblLocations R ON R.LocationId = D.ParentLocationId \n" +
                "\tWHERE V.locationId =" + LocationId;
        int RegionId = 0;
        int DistrictId = 0;

        JSONArray RD = sqlHandler.getResult(Query, null);
        JSONObject object = null;
        try {
            object = RD.getJSONObject(0);
            RegionId = Integer.parseInt(object.getString("RegionId"));
            DistrictId = Integer.parseInt(object.getString("DistrictId"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
/*String PayerQuery = "SELECT PayerId, PayerName,P.LocationId FROM tblPayer P\n" +
        "INNER JOIN uvwLocations L ON P.LocationId = L.LocationId\n" +
        "WHERE \n" +
        "(\n" +
        "L.RegionId = CASE WHEN "+ RegionId +" IS NULL THEN NULL ELSE "+ RegionId +" END  \n" +
        "OR L.RegionId is null\n" +
        "OR L.DistrictId = CASE WHEN "+ DistrictId +" IS NULL THEN "+ RegionId +" ELSE "+ DistrictId +" END  \n" +
        ") \n" +
        "ORDER BY L.LocationId";*/
       String PayerQuery = "SELECT PayerId, PayerName,P.LocationId FROM tblPayer P \n" +
                "INNER JOIN uvwLocations L ON P.LocationId = L.LocationId\n" +
                "WHERE (L.RegionId = " + RegionId + " OR L.RegionId ='null') AND (L.DistrictId = " + DistrictId + " OR L.DistrictId ='null')  " +
                "ORDER BY L.LocationId";
        JSONArray Payers = sqlHandler.getResult(PayerQuery, null);
        return Payers.toString();
    }

    private OutputStream ResizeImage(String ImagePath, String outputFileName, int newSize) {
        try {
            File file = new File(ImagePath);


            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(new FileInputStream(file), null, options);

            int scale = 1;
            while (options.outWidth / scale / 2 >= newSize && options.outHeight / scale / 2 >= newSize) {
                scale *= 2;
            }

            BitmapFactory.Options options1 = new BitmapFactory.Options();
            options1.inJustDecodeBounds = false;
            options1.inSampleSize = scale;

            //String outputFileName = IMAGE_FOLDER + "TestImageSize" + "_" + "OfficerCode_"  + "_0_0.jpeg";
            OutputStream outputStream = new FileOutputStream(outputFileName);


            Bitmap bitmap = BitmapFactory.decodeStream(new FileInputStream(file), null, options1);

            bitmap.compress(Bitmap.CompressFormat.JPEG, 30, outputStream);

            return outputStream;


        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static void setInsuranceNo(String insuranceNo){
        InsuranceNo = insuranceNo;

    }
    ArrayList<String> scanned = new ArrayList<String>();
    String aBuffer = "";

    @JavascriptInterface
    public void clearBuffer(){
// empty the current content

    try {
        String dir = Environment.getExternalStorageDirectory() + File.separator + "scanned";
        FileOutputStream fOut = new FileOutputStream(dir+"/values.txt");
        OutputStreamWriter myOutWriter =new OutputStreamWriter(fOut);
        myOutWriter.append("");
        myOutWriter.close();
        fOut.close();
    } catch (IOException e) {
        e.printStackTrace();
    }
    }
    @JavascriptInterface
    public String getInsuranceNo(){
        try {
            String dir = Environment.getExternalStorageDirectory() + File.separator + "scanned";
            File myFile = new File("/"+dir+"/values.txt");
            FileInputStream fIn = new FileInputStream(myFile);
            BufferedReader myReader = new BufferedReader(new InputStreamReader(fIn));
                aBuffer = myReader.readLine();
                myReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return aBuffer;
    }
    @JavascriptInterface
    public String getScannedNumber() {
        //clearInsuranceNo();
        //clearXml();
        Intent intent = new Intent("com.google.zxing.client.android.SCAN");
        intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
        try{
            ((Activity) mContext).startActivityForResult(intent, 100);
            ((MainActivity) mContext).InsureeNumber = "";
            while (((MainActivity) mContext).InsureeNumber == "") {

            }
        }catch (Exception e){
            e.printStackTrace();
        }


        return ((MainActivity) mContext).InsureeNumber;
    }

    public void clearXml(){
        String dir = Environment.getExternalStorageDirectory() + File.separator + "scanned";
        //create folder
        File folder = new File(dir); //folder name
        folder.mkdirs();

        //create file
        File file = new File(dir, "values.txt");
        try {
            file.createNewFile();
            FileOutputStream fOut = new FileOutputStream(file);
            OutputStreamWriter myOutWriter =new OutputStreamWriter(fOut);
            myOutWriter.append("");
            myOutWriter.close();
            fOut.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @JavascriptInterface
    public void clearInsuranceNo(){
        InsuranceNo = "";
    }

    @JavascriptInterface
    public String getOfficerLocation() {
        global = (Global) mContext.getApplicationContext();
        String Query = " SELECT RegionId, DistrictId FROM uvwLocations UL\n" +
                " INNER JOIN tblOfficer O ON O.LocationId = UL.DistrictId \n" +
                " WHERE OfficerId = " + global.getOfficerId();
        JSONArray jsonArray = sqlHandler.getResult(Query, null);
        return jsonArray.toString();
    }

    @JavascriptInterface
    public String getFamilyPolicy(int FamilyId) {
        String Query = "SELECT count(1) Ins, IFNULL(Threshold,0) Threshold,IFNULL(MemberCount,0) MemberCount, IFNULL(PolicyId,0) PolicyId  FROM tblInsuree I\n" +
                "LEFT JOIN(\n" +
                "SELECT Threshold,MemberCount, PolicyId FROM tblPolicy  PL\n" +
                "INNER JOIN tblProduct PR ON PL.ProdId = PR.ProdId\n" +
                "WHERE PL.FamilyId =" + FamilyId + "\n" +
                "LIMIT 1) Policy ON 1 =1\n" +
                "WHERE I.FamilyId=" + FamilyId + " \n" +
                "GROUP BY PolicyId ";
        JSONArray FamilyPolicy = sqlHandler.getResult(Query, null);
        return FamilyPolicy.toString();
    }
    @JavascriptInterface
    public int getMaxInstallments(String id) {
        int PolicyId = Integer.parseInt(id);
        int MaxInstallments = 0;
        JSONArray MaxInstallArray = null;

        int ProdId = getProdId(PolicyId);
        try{
            String Query = "SELECT MaxInstallments from tblProduct where ProdId = " + ProdId + "";
            MaxInstallArray = sqlHandler.getResult(Query, null);
        }catch (Exception e){
            e.printStackTrace();
        }

        JSONObject MaxObject = null;
        for (int i = 0; i < MaxInstallArray.length(); i++) {
            try {
                MaxObject = MaxInstallArray.getJSONObject(i);
                MaxInstallments = MaxObject.getInt("MaxInstallments");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return MaxInstallments;
    }
    @JavascriptInterface
    public int getGracePeriods(String id) {
        int PolicyId = Integer.parseInt(id);
        int gracePeriod = 0;
        JSONArray GracePeriodArray = null;

        int ProdId = getProdId(PolicyId);
        try{
            String Query = "SELECT GracePeriod from tblProduct where ProdId = " + ProdId + "";
            GracePeriodArray = sqlHandler.getResult(Query, null);
        }catch (Exception e){
            e.printStackTrace();
        }

        JSONObject MaxObject = null;
        for (int i = 0; i < GracePeriodArray.length(); i++) {
            try {
                MaxObject = GracePeriodArray.getJSONObject(i);
                gracePeriod = MaxObject.getInt("GracePeriod");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return gracePeriod;
    }

    public int getProdId(int PolicyId) {
        int ProdId = 0;
        String Query = "SELECT ProdId from tblPolicy where PolicyId = " + PolicyId + "";
        JSONArray MaxInstallArray = sqlHandler.getResult(Query, null);
        JSONObject MaxObject = null;
        for (int i = 0; i < MaxInstallArray.length(); i++) {
            try {
                MaxObject = MaxInstallArray.getJSONObject(i);
                ProdId = MaxObject.getInt("ProdId");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return ProdId;
    }

    @JavascriptInterface
    public void SaveInsureePolicy(int InsureId, int FamilyId, Boolean Activate, int isOffline) {
        String InsQuery = "SELECT count(1) TotalIns FROM tblInsuree I WHERE FamilyId =" + FamilyId;
        JSONArray InsArray = sqlHandler.getResult(InsQuery, null);
        JSONObject InsObject = null;
        int TotalIns = 0;
        int MaxInsureePolicyId = 0;
        int PolicyId = 0;
        double PolicyValue = 0;
        double NewPolicyValue = 0;
        String EffectiveDate = null;
        String StartDate = null;
        String PolicyStage = null;
        String EnrollmentDate = null;
        String EnrollDate = null;
        String ExpiryDate = null;
        int IsOffline = 1;
        int MaxMember = 0;
        int ProdID = 0;
        boolean HasCycle = false;
        try {
            InsObject = InsArray.getJSONObject(0);
            TotalIns = Integer.parseInt(InsObject.getString("TotalIns"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String PolicyQuery = " SELECT PolicyId,PolicyValue,EffectiveDate,PolicyStage,ProdID,StartDate, EnrollDate,isOffline FROM tblPolicy WHERE FamilyID  =  " + FamilyId;
        JSONArray PolicyArray = sqlHandler.getResult(PolicyQuery, null);
        JSONObject PolicyObject = null;
        for (int i = 0; i < PolicyArray.length(); i++) {
            try {
                PolicyObject = PolicyArray.getJSONObject(i);
                PolicyId = Integer.parseInt(PolicyObject.getString("PolicyId"));
                PolicyValue = Double.parseDouble(PolicyObject.getString("PolicyValue"));
                EffectiveDate = (PolicyObject.getString("EffectiveDate"));
                PolicyStage = PolicyObject.getString("PolicyStage");
                StartDate = PolicyObject.getString("StartDate");
                ProdID = Integer.parseInt(PolicyObject.getString("ProdId"));
                EnrollmentDate = PolicyObject.getString("EnrollDate");
                IsOffline = Integer.parseInt(PolicyObject.getString("isOffline"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            String MemberCount = "SELECT MemberCount,StartCycle1 FROM tblProduct WHERE ProdId =" + ProdID;
            JSONArray MCArray = sqlHandler.getResult(MemberCount, null);
            JSONObject MCObject = null;
            try {
                MCObject = MCArray.getJSONObject(0);
                MaxMember = Integer.parseInt(MCObject.getString("MemberCount"));
                if ((!TextUtils.isEmpty(MCObject.getString("StartCycle1"))) && (!MCObject.getString("StartCycle1").equals("null"))) {
                    HasCycle = true;
                }
            } catch (JSONException e) {
                e.printStackTrace();//sio error
            }
               if (MaxMember >= TotalIns){
                   try {
//                General general = new General();
//                if(general.isNetworkAvailable(mContext) && IsOffline == 0) {
//                    CallSoap cs = new CallSoap();
//                    cs.setFunctionName("getPolicyValue");
//                    NewPolicyValue =     cs.getPolicyValue(FamilyId, ProdID,0,PolicyStage,EnrollmentDate,PolicyId);
//
//                }
//                else
                       NewPolicyValue = getPolicyValue(EnrollmentDate, ProdID, FamilyId, StartDate, HasCycle, PolicyId, PolicyStage, isOffline);
                   } catch (JSONException e) {
                       e.printStackTrace();
                   }

/*                   if (NewPolicyValue != PolicyValue) {

                   }*/
                   if (!Activate) EffectiveDate = null;
 /*                String MaxIdQuery = "SELECT  IFNULL(COUNT(InsureePolicyId),0)+1  InsureePolicyId  FROM tblInsureePolicy";
               JSONArray JsonA = sqlHandler.getResult(MaxIdQuery, null);
                try {
                    //Integer.parseInt(PolicyObject.getString("ProdId"));
                    JSONObject JmaxOb = JsonA.getJSONObject(0);
                    MaxInsureePolicyId = JmaxOb.getString("InsureePolicyId ");
                } catch (JSONException e) {
                    e.printStackTrace();
                }*/
                   String MaxIdQuery = "SELECT  Count(InsureePolicyId)+1  InsureePolicyId  FROM tblInsureePolicy";
                   JSONArray JsonA = sqlHandler.getResult(MaxIdQuery, null);
                   try {
                       JSONObject JmaxOb = JsonA.getJSONObject(0);
                       MaxInsureePolicyId = JmaxOb.getInt("InsureePolicyId");
                   } catch (JSONException e) {
                       e.printStackTrace();
                   }


                ContentValues values = new ContentValues();

                   String PolicyQuery2 = " SELECT StartDate,EnrollDate,ExpiryDate FROM tblPolicy WHERE PolicyID =" + PolicyId;
                   JSONArray PolicyArray2 = sqlHandler.getResult(PolicyQuery2, null);
                   JSONObject PolicyObject2 = null;
                       try {
                           PolicyObject2 = PolicyArray2.getJSONObject(0);
                           StartDate = PolicyObject2.getString("StartDate");
                           ExpiryDate = PolicyObject2.getString("ExpiryDate");
                           EnrollDate = PolicyObject2.getString("EnrollDate");
                       } catch (JSONException e) {
                           e.printStackTrace();
                       }
                   values.put("InsureePolicyId", MaxInsureePolicyId);
                   values.put("InsureeId", InsureId);
                   values.put("PolicyId", PolicyId);
                   values.put("EnrollmentDate", EnrollDate);
                   values.put("StartDate", StartDate);
                   values.put("EffectiveDate", EffectiveDate);
                   values.put("ExpiryDate", ExpiryDate);
                   values.put("isOffline", isOffline);


/*                   String SavePolicyInsuree = "INSERT INTO tblInsureePolicy(InsureePolicyId,InsureeId,PolicyId,EnrollmentDate,StartDate,EffectiveDate,ExpiryDate,isOffline)\n" +
                           "SELECT " + MaxInsureePolicyId + "," + InsureId + "," + PolicyId + ",EnrollDate,StartDate," + EffectiveDate + ",ExpiryDate," + isOffline + "\n" +
                           "FROM tblPolicy WHERE PolicyID =" + PolicyId;*/

                   try {
                       sqlHandler.insertData("tblInsureePolicy", values);
                   } catch (UserException e) {
                       e.printStackTrace();
                   }
               }

        }

    }

    @JavascriptInterface
    public void InsertPolicyInsuree(int PolicyId, int IsOffline) {
        int MaxMember = 0;
        int MaxInsureePolicyId = 0;
        String MaxIdQuery = "SELECT  Count(InsureePolicyId)+1  InsureePolicyId  FROM tblInsureePolicy";
        JSONArray JsonA = sqlHandler.getResult(MaxIdQuery, null);
        try {
            JSONObject JmaxOb = JsonA.getJSONObject(0);
            MaxInsureePolicyId = JmaxOb.getInt("InsureePolicyId");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String MemberCount = "SELECT MemberCount FROM tblProduct Prod  \n" +
                " INNER JOIN tblPolicy P ON P.ProdId =Prod.ProdId \n" +
                " WHERE PolicyId =" + PolicyId + " LIMIT 1";
        JSONArray MCArray = sqlHandler.getResult(MemberCount, null);
        JSONObject MCObject = null;
        try {
            MCObject = MCArray.getJSONObject(0);
            MaxMember = Integer.parseInt(MCObject.getString("MemberCount"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String SavePolicyInsuree = "INSERT INTO tblInsureePolicy(InsureePolicyId,InsureeId,PolicyId,EnrollmentDate,StartDate,EffectiveDate,ExpiryDate,isOffline)\n" +
                "SELECT " + MaxInsureePolicyId + ",  InsureeId ,PolicyId ,EnrollDate,StartDate, EffectiveDate ,ExpiryDate,I.isOffline FROM tblPolicy P\n" +
                "INNER JOIN tblInsuree I ON  I.FamilyId = P.FamilyId\n" +
                "WHERE PolicyID = " + PolicyId + "\n" +
                "LIMIT " + MaxMember;
        sqlHandler.getResult(SavePolicyInsuree, null);
    }
    @JavascriptInterface
    public void UpdateInsureePolicy(int PolicyId){//herman new
        ContentValues values = new ContentValues();
        @SuppressLint("SimpleDateFormat") SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        String PolicyQuery = "SELECT EffectiveDate FROM tblPolicy WHERE PolicyId = " + PolicyId;
        JSONArray Policy = sqlHandler.getResult(PolicyQuery, null);
        String EffectiveDate = null;
        JSONObject O = null;
        try {
            O = Policy.getJSONObject(0);
            EffectiveDate = O.getString("EffectiveDate");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        values.put("EffectiveDate", EffectiveDate);

        try {
            sqlHandler.updateData("tblInsureePolicy", values, "PolicyId = ?", new String[]{String.valueOf(PolicyId)});
        } catch (UserException e) {
            e.printStackTrace();
        }
    }

    public File[] getPhotos() {
        String path = mContext.getApplicationInfo().dataDir + "/Images/";
        File Directory = new File(path);
        FilenameFilter filter = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                return filename.contains("0");
            }
        };
        File[] newFiles = Directory.listFiles(filter);
        return Directory.listFiles(filter);
    }


    @JavascriptInterface
    public void UploadPhotos() throws JSONException {
        pd = new ProgressDialog(mContext);
        pd = ProgressDialog.show(mContext, "", mContext.getResources().getString(R.string.Uploading));

        new Thread() {
            public void run() {
//            try {
//                Thread.sleep(10000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
                File[] Photo = getPhotos();

                if(Photo.length > 0) {
                    UploadFile uf = new UploadFile();
                    if (uf.isValidFTPCredentials()) {
                        for (int i = 0; i < Photo.length; i++) {
                            UploadCounter = i + 1;
                            String FileName = Photo[i].toString().substring(Photo[i].toString().lastIndexOf("/") + 1);
                            String PhotoQuery = "SELECT PhotoPath FROM tblInsuree WHERE isOffline = 1 AND REPLACE(PhotoPath, RTRIM(PhotoPath, REPLACE(PhotoPath, '/', '')), '') = '" + FileName + "'";
                            JSONArray jsonArray = sqlHandler.getResult(PhotoQuery, null);
                            JSONObject jsonObject = null;
                            String PhotoPath = "";
                            if (jsonArray.length() > 0) {
                                try {
                                    jsonObject = jsonArray.getJSONObject(0);
                                    PhotoPath = jsonObject.getString("PhotoPath");
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                            if (PhotoPath.trim().length() == 0) {
                                if (uf.uploadFileToServer(mContext, Photo[i], "com.exact.imis.enrollment")) {
                                    RegisterUploadDetails(FileName);
                                    Uploaded = 1;
                                    Photo[i].delete();
                                }
                            }
                        }
                    }

                }else{
                    Uploaded = 0;
                }
                ((Activity) mContext).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (Uploaded == 1){
                            ShowDialog(mContext.getResources().getString(R.string.PhotosUploaded));
                        } else{
                            ShowDialog(mContext.getResources().getString(R.string.NoPhoto));
                        }
                    }

                });

                pd.dismiss();


            }
        }.start();
    }

    @JavascriptInterface
    public int ModifyFamily(final String InsuranceNumber) {//herman change
        IsFamilyAvailable = 0;
        inProgress = true;

        String Query = "SELECT * FROM tblInsuree WHERE Trim(CHFID) = '" + InsuranceNumber + "'";
        JSONArray JsonInsNo = sqlHandler.getResult(Query, null);

        if (JsonInsNo.length() > 0) {
            IsFamilyAvailable = 2;
            inProgress = false;
        }else {

            try {
                int LocationId = getLocationId();

                CallSoap cs = new CallSoap();
                cs.setFunctionName("DownloadFamilyData");
                String MD = cs.DownloadFamilyData(InsuranceNumber, LocationId);
                JSONArray FamilyData = new JSONArray(MD);

                if (FamilyData.length() == 0) {
                    IsFamilyAvailable = 0;
                    inProgress = false;
                }else {
                    DownloadFamilyData(FamilyData);
                    IsFamilyAvailable = 1;
                    inProgress = false;
                }
                inProgress = false;

            } catch (JSONException e) {
                inProgress = false;
                e.printStackTrace();
            } catch (UserException e) {
                inProgress = false;
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }


/*            new Thread() {
                public void run() {
                    try {
                        int LocationId = getLocationId();

                        CallSoap cs = new CallSoap();
                        cs.setFunctionName("DownloadFamilyData");
                        String MD = cs.DownloadFamilyData(InsuranceNumber, LocationId);
                        JSONArray FamilyData = new JSONArray(MD);

                        if (FamilyData.length() == 0) {
                            IsFamilyAvailable = 0;
                            inProgress = false;
                        }else {
                            DownloadFamilyData(FamilyData);
                            IsFamilyAvailable = 1;
                            inProgress = false;
                        }
                        inProgress = false;

                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (UserException e) {
                        e.printStackTrace();
                    }
                }
            }.start();*/
            while (inProgress) {
            }
        }

        inProgress = false;
        return IsFamilyAvailable;
    }


    private void DownloadFamilyData(JSONArray FamilyData) throws JSONException, UserException, IOException {


        //Sequence of table
        /*
            1   :   Family
            2   :   Insuree
            3   :   Policy
            4   :   InsureePolicy
            5   :   Premiums
         */
        JSONArray newFamilyArr = new JSONArray();
         JSONArray Family = new JSONArray();
        JSONArray Insuree = new JSONArray();
        //  JSONArray Policy = new JSONArray();
        //  JSONArray PolicyInsuree = new JSONArray();
        // JSONArray Premium = new JSONArray();



        for (int i = 0; i < FamilyData.length(); i++) {
            String keyName = FamilyData.getJSONObject(i).keys().next();
            switch (keyName.toLowerCase()) {
                case "families":
                    Family = (JSONArray) FamilyData.getJSONObject(i).get(keyName);
                    for(int j = 0;j < Family.length();j++){
                        JSONObject ob = Family.getJSONObject(j);
                        String Poverty = ob.getString("Poverty");
                        if(Poverty == "true"){
                            ob.put("Poverty", 1);
                            newFamilyArr.put(ob);
                        }else if(Poverty == "false"){
                            ob.put("Poverty", 0);
                            newFamilyArr.put(ob);
                        }else{
                            newFamilyArr.put(ob);
                        }
                    }

                    break;
                case "insurees":
                    Insuree = (JSONArray) FamilyData.getJSONObject(i).get(keyName);
                    break;
//                case "policies":
//                    Policy = (JSONArray) FamilyData.getJSONObject(i).get(keyName);
//                    break;
//                case "insureepolicies":
//                    PolicyInsuree = (JSONArray) FamilyData.getJSONObject(i).get(keyName);
//                    break;
//                case "premiums":
//                    Premium = (JSONArray) FamilyData.getJSONObject(i).get(keyName);
//                    break;

            }

        }
        InsertFamilyDataFromOnline(newFamilyArr);
        InsertInsureeDataFromOnline(Insuree);

        //Iterate insuree array and download image for each Insuree
        for(int i = 0; i < Insuree.length(); i++ ){
            JSONObject ins = Insuree.getJSONObject(i);
            String PhotoPath = ins.getString("PhotoPath");
            if (PhotoPath.length() > 0)
            {
                Bitmap insureeImage = GetImageFromUrl(AppInformation.DomainInfo.getDomain() + "/Images/Updated/" +PhotoPath);
                if (insureeImage != null)
                {
                    FileOutputStream stream = null;
                    try {
                        stream = new FileOutputStream(global.getImageFolder() + PhotoPath);
                        insureeImage.compress(Bitmap.CompressFormat.JPEG, 100, stream);


                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }finally {
                        if(stream != null)
                            stream.close();
                    }



                }
            }
        }

//        InsertPolicyDataFromOnline(Policy);
//        InsertPolicyInsureeDataFromOnline(PolicyInsuree);
//        InsertPremiumDataFromOnline(Premium);

    }

    private Bitmap GetImageFromUrl(String ImagePath)
    {
        try {
            java.net.URL url = new java.net.URL( ImagePath);
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap bitmap = BitmapFactory.decodeStream(input);
            return bitmap;

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private boolean InsertFamilyDataFromOnline(JSONArray jsonArray) throws JSONException {
        JSONObject object = jsonArray.getJSONObject(0);
        int FamilyId = object.getInt("FamilyId");

        String QueryCheck = "SELECT FamilyId FROM tblFamilies WHERE FamilyId = " + FamilyId + " AND (isOffline = 0 OR isOffline = 2)";
        JSONArray CheckedArrey = sqlHandler.getResult(QueryCheck, null);
        if (CheckedArrey.length() == 0) {
            String Columns[] = {"FamilyId", "InsureeId", "LocationId", "Poverty", "isOffline", "FamilyType",
                    "FamilyAddress", "Ethnicity", "ConfirmationNo", "ConfirmationType"};
            sqlHandler.insertData("tblFamilies", Columns, jsonArray.toString(), "");
        }
        return true;

    }


    private boolean InsertInsureeDataFromOnline(JSONArray jsonArray) throws JSONException {
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONArray TempJsonArray = new JSONArray();
            JSONObject object = jsonArray.getJSONObject(i);
            String CHFID = object.getString("CHFID");
            String QueryCheck = "SELECT InsureeId FROM tblInsuree WHERE Trim(CHFID) = " + CHFID + " AND (isOffline = 0 OR isOffline = 2)";
            JSONArray CheckedArrey = sqlHandler.getResult(QueryCheck, null);
            if (CheckedArrey.length() == 0) {
                TempJsonArray.put(jsonArray.getJSONObject(i));
                String Columns[] = {"IdentificationNumber","InsureeId" ,"FamilyId", "CHFID", "LastName", "OtherNames", "DOB", "Gender", "Marital", "IsHead", "Phone", "PhotoPath", "CardIssued",
                        "isOffline", "Relationship", "Profession", "Education", "Email", "TypeOfId", "HFID", "CurrentAddress", "GeoLocation", "CurVillage"};
                sqlHandler.insertData("tblInsuree", Columns, TempJsonArray.toString(), "");
            }
        }
        return true;
    }

    private boolean InsertPolicyDataFromOnline(JSONArray jsonArray) throws JSONException {

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONArray TempJsonArray = new JSONArray();
            JSONObject object = jsonArray.getJSONObject(i);
            int PolicyId = object.getInt("PolicyId");
            String QueryCheck = "SELECT PolicyId FROM tblPolicy WHERE PolicyId = " + PolicyId + " AND (isOffline = 0 OR isOffline = 2)";
            JSONArray CheckedArrey = sqlHandler.getResult(QueryCheck, null);
            if (CheckedArrey.length() == 0) {
                TempJsonArray.put(jsonArray.getJSONObject(i));
                String Columns[] = {"PolicyId", "FamilyId", "EnrollDate", "StartDate", "EffectiveDate", "ExpiryDate", "PolicyStatus", "PolicyValue",
                        "ProdId", "OfficerId", "isOffline", "PolicyStage"};
                sqlHandler.insertData("tblPolicy", Columns, TempJsonArray.toString(), "");

            }
        }

        return true;
    }

    private boolean InsertPolicyInsureeDataFromOnline(JSONArray jsonArray) throws JSONException {
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONArray TempJsonArray = new JSONArray();
            JSONObject object = jsonArray.getJSONObject(i);
            int InsureePolicyId = object.getInt("InsureePolicyId");
            String QueryCheck = "SELECT PolicyId FROM tblInsureePolicy WHERE InsureePolicyId = " + InsureePolicyId + " AND (isOffline = 0 OR isOffline = 2)";
            JSONArray CheckedArrey = sqlHandler.getResult(QueryCheck, null);
            if (CheckedArrey.length() == 0) {
                TempJsonArray.put(jsonArray.getJSONObject(i));
                String Columns[] = {"InsureePolicyId", "InsureeId", "PolicyId", "EnrollmentDate", "StartDate", "EffectiveDate", "ExpiryDate", "isOffline"};
                sqlHandler.insertData("tblInsureePolicy", Columns, TempJsonArray.toString(), "");

            }
        }
        return true;
    }

    private boolean InsertPremiumDataFromOnline(JSONArray jsonArray) throws JSONException {
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONArray TempJsonArray = new JSONArray();
            JSONObject object = jsonArray.getJSONObject(i);
            int PremiumId = object.getInt("PremiumId");
            String QueryCheck = "SELECT PolicyId FROM tblPremium WHERE PremiumId = " + PremiumId + " AND (isOffline = 0 OR isOffline = 2)";
            JSONArray CheckedArrey = sqlHandler.getResult(QueryCheck, null);
            if (CheckedArrey.length() == 0) {
                TempJsonArray.put(jsonArray.getJSONObject(i));
                String Columns[] = {"PremiumId", "PolicyId", "PayerId", "Amount", "Receipt", "PayDate", "PayType", "isOffline", "isPhotoFee"};
                sqlHandler.insertData("tblPremium", Columns, TempJsonArray.toString(), "");
            }
        }

        return true;
    }

    //****************************Online Statistics ******************************//
    @JavascriptInterface
    public int getTotalFamilyOnline() {

        String FamilyQuery = "SELECT count(1) Families  FROM  tblfamilies WHERE isOffline = 0 ";
        JSONArray Families = sqlHandler.getResult(FamilyQuery, null);
        JSONObject object = null;
        int TotalFamilies = 0;
        try {
            object = Families.getJSONObject(0);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            TotalFamilies = Integer.parseInt(object != null ? object.getString("Families") : null);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return TotalFamilies;
    }

    @JavascriptInterface
    public int getTotalInsureeOnline() {
        String InsureeQuery = "SELECT count(1) Insuree  FROM tblInsuree WHERE isOffline = 0 ";
        JSONArray Insuree = sqlHandler.getResult(InsureeQuery, null);
        JSONObject object = null;
        int TotalInsuree = 0;
        try {
            object = Insuree.getJSONObject(0);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            TotalInsuree = Integer.parseInt(object != null ? object.getString("Insuree") : null);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return TotalInsuree;
    }

    @JavascriptInterface
    public int getTotalPolicyOnline() {
        String PolicyQuery = "SELECT count(1) Policies  FROM  tblPolicy WHERE isOffline = 0 ";
        JSONArray Policy = sqlHandler.getResult(PolicyQuery, null);
        JSONObject object = null;
        int TotalPolicies = 0;
        try {
            object = Policy.getJSONObject(0);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            TotalPolicies = Integer.parseInt(object != null ? object.getString("Policies") : null);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return TotalPolicies;
    }

    @JavascriptInterface
    public int getTotalPremiumOnline() {
        String PremiumQuery = "SELECT count(1) Premiums  FROM  tblPremium  WHERE isOffline = 0 ";
        JSONArray Premium = sqlHandler.getResult(PremiumQuery, null);
        JSONObject object = null;
        int TotalPremiums = 0;
        try {
            object = Premium.getJSONObject(0);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            TotalPremiums = Integer.parseInt(object != null ? object.getString("Premiums") : null);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return TotalPremiums;
    }
    @JavascriptInterface
    public int getCountPremiums(String id) {
        int PolicyId = Integer.parseInt(id);
        String PremiumQuery = "SELECT count(1) Premiums  FROM  tblPremium  WHERE isPhotoFee = 'false' AND PolicyId = " + PolicyId + "";
        JSONArray Premium = sqlHandler.getResult(PremiumQuery, null);
        JSONObject object = null;
        int TotalPremiums = 0;
        try {
            object = Premium.getJSONObject(0);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            TotalPremiums = Integer.parseInt(object != null ? object.getString("Premiums") : null);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return TotalPremiums;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @JavascriptInterface
    public String getSumPremium() {
        String PremiumQuery = "SELECT SUM(Amount) FROM  tblPremium";//WHERE isOffline = 1
        JSONArray Premium = sqlHandler.getResult(PremiumQuery, null);
        JSONObject object = null;
        String TotalPremiums = "0";
        try {
            object = Premium.getJSONObject(0);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            if(object != null){
                String amt = object.getString("SUM(Amount)");
                if(amt != ""){
                    int Amount= Integer.parseInt(amt);
                    String number = String.valueOf(Amount);
                    double amount = Double.parseDouble(number);
                    DecimalFormat formatter = null;
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                        formatter = new DecimalFormat("#,###.00");
                        TotalPremiums = formatter.format(amount);
                    }
                }else {
                    return TotalPremiums;
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return TotalPremiums;
    }

    private int getLocationId() throws JSONException {
        global = (Global) mContext.getApplicationContext();
        String Query = "SELECT LocationId FROM tblOfficer WHERE OfficerId = " + global.getOfficerId();
        JSONArray jsonArray = sqlHandler.getResult(Query, null);
        JSONObject object = jsonArray.getJSONObject(0);
        int LocationId = object.getInt("LocationId");
        return LocationId;
    }

    @JavascriptInterface
    public int getFamilyStat(int FamilyId){
        int status = 0;
        try {
            status = getFamilyStatus(FamilyId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return status;
    }

    private int getFamilyStatus(int FamilyId) throws JSONException {
        if (FamilyId == 0) return 1;
        String Query = "SELECT isOffline FROM tblFamilies WHERE FamilyId = " + FamilyId;
        JSONArray jsonArray = sqlHandler.getResult(Query, null);
        if (jsonArray.length() == 0) return 1;
        JSONObject object = jsonArray.getJSONObject(0);
        if (object.getInt("isOffline") == 1) return 1;
        else return 0;
    }

    private int getInsureeStatus(int InsureeId) throws JSONException {//herman
        if (InsureeId == 0) return 1;
        String Query = "SELECT isOffline FROM tblInsuree WHERE InsureeId = " + InsureeId;
        JSONArray jsonArray = sqlHandler.getResult(Query, null);
        if (jsonArray.length() == 0){
            return 1;
        } else{
            JSONObject object = jsonArray.getJSONObject(0);
            if (object.getInt("isOffline") == 1){
                return 1;
            }else{
                return 0;
            }
        }

    }

    @JavascriptInterface
    public int DeleteOnlineData(final int Id, final String DeleteInfo) {
        try{
            DataDeleted = 0;
            //global = (Global) mContext.getApplicationContext();
            //final int userId = global.getUserId();
            //if (userId > 0) {
                inProgress = true;
                //pd = new ProgressDialog(mContext);
                //pd = ProgressDialog.show(mContext, "", mContext.getResources().getString(R.string.Deleting));
                Toast.makeText(mContext,"Please wait...",Toast.LENGTH_LONG).show();

                //Uncoment this if you want to delete online data too.
                //CallSoap cs = new CallSoap();
                //cs.setFunctionName("DeleteFromPhone");
                DataDeleted = 1;//cs.DeleteFromPhone(Id, userId, DeleteInfo);

                inProgress = false;

                if (DataDeleted == 1) {
                    if (DeleteInfo.equalsIgnoreCase("F")) DeleteFamily(Id);//Enrollment page
                    if (DeleteInfo.equalsIgnoreCase("I")) DeleteInsuree(Id);//family and insuree page
                    if (DeleteInfo.equalsIgnoreCase("PO")) DeletePolicy(Id);//Family and policy page
                    if (DeleteInfo.equalsIgnoreCase("PR")) DeletePremium(Id);//PolicyPremium page

                    Toast.makeText(mContext,mContext.getResources().getString(R.string.dataDeleted),Toast.LENGTH_LONG).show();

                    inProgress = false;
                }

               // pd.dismiss();

/*                new Thread() {
                    public void run() {
                        CallSoap cs = new CallSoap();
                        cs.setFunctionName("DeleteFromPhone");
                        DataDeleted = cs.DeleteFromPhone(Id, userId, DeleteInfo);
                        inProgress = false;
                        if (DataDeleted == 1) {
                            if (DeleteInfo.equalsIgnoreCase("F")) DeleteFamily(Id);
                            if (DeleteInfo.equalsIgnoreCase("I")) DeleteInsuree(Id);
                            if (DeleteInfo.equalsIgnoreCase("PO")) DeletePolicy(Id);
                            if (DeleteInfo.equalsIgnoreCase("PR")) DeletePremium(Id);
                        }
                        //pd.dismiss();

                    }
                }.start();*/
/*            } else {
                DataDeleted = -1;
                inProgress = false;
            }*/

            while (inProgress) {}

        }catch (Exception e){
            e.printStackTrace();
        }

        return DataDeleted;
    }

    @JavascriptInterface
    public  int getOfficerId(){
        global = (Global) mContext.getApplicationContext();
        return  global.getOfficerId();
    }
    //Added by Salumu 12/12/2017 to delete insuree policy
    public void DeleteInsureePolicy(int  PolicyId, int InsureeId) {
        try {
            String TableName = "tblInsureePolicy";
            String WhereClause = "PolicyId=" + PolicyId + " OR InsureeId=" + InsureeId + "";
            sqlHandler.deleteData(TableName, WhereClause, null);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public boolean getRule(String rulename){
        boolean rule = false;
        String Query = "SELECT RuleValue FROM tblIMISDefaultsPhone WHERE RuleName=?";
        String arg[] = {rulename};
        JSONArray rulevalue = sqlHandler.getResult(Query, arg);

        try {
            JSONObject RuleObject = rulevalue.getJSONObject(0);

            rule = RuleObject.getBoolean("RuleValue");

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return rule;
    }



}


