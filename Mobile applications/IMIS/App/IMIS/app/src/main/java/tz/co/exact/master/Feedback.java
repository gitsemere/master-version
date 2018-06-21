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
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Xml;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.Toast;

import com.exact.CallSoap.CallSoap;
import com.exact.general.General;
import com.exact.uploadfile.UploadFile;

import org.xmlpull.v1.XmlSerializer;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by HP on 05/17/2017.
 */

public class Feedback extends AppCompatActivity {
    private General _General = new General(AppInformation.DomainInfo.getDomain());

    private EditText etOfficer;
    private EditText etClaimCode;
    private EditText etCHFID;
    private RadioGroup rg1;
    private RadioGroup rg2;
    private RadioGroup rg3;
    private RadioGroup rg4;
    private RadioButton rbYes1;
    private RadioButton rbYes2;
    private RadioButton rbYes3;
    private RadioButton rbYes4;
    private RadioButton rbNo1;
    private RadioButton rbNo2;
    private RadioButton rbNo3;
    private RadioButton rbNo4;
    private RatingBar rb1;
    private ProgressDialog pd;
    private Button btnSubmit;

    private int ClaimId;
    private File FeedbackXML;
    private String FileName;
     private String OfficerCode;
    private final String Path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/IMIS/";
    private int msgType;
    private ClientAndroidInterface ca;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.feedback);

        ca = new ClientAndroidInterface(this);
        etOfficer = (EditText) findViewById(R.id.etOfficer);
        etClaimCode = (EditText) findViewById(R.id.etClaimCode);
        etCHFID = (EditText) findViewById(R.id.etCHFID);
        rbYes1 = (RadioButton) findViewById(R.id.rYes1);
        rbYes2 = (RadioButton) findViewById(R.id.rYes2);
        rbYes3 = (RadioButton) findViewById(R.id.rYes3);
        rbYes4 = (RadioButton) findViewById(R.id.rYes4);
        rbNo1 = (RadioButton) findViewById(R.id.rNo1);
        rbNo2 = (RadioButton) findViewById(R.id.rNo2);
        rbNo3 = (RadioButton) findViewById(R.id.rNo3);
        rbNo4 = (RadioButton) findViewById(R.id.rNo4);
        btnSubmit = (Button) findViewById(R.id.btnSubmit);
        OfficerCode=getIntent().getStringExtra("OfficerCode");
        etOfficer.setText(OfficerCode);
         ClaimId = Integer.parseInt(getIntent().getStringExtra("ClaimId"));
        etClaimCode.setText(getIntent().getStringExtra("ClaimCode"));
        etCHFID.setText(getIntent().getStringExtra("CHFID"));

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!isValidate()) return;
                pd = ProgressDialog.show(Feedback.this, "", getResources().getString(R.string.UploadingFeedback));

                new Thread(){
                    public void run(){

                        String Answers = Answers();
                        try {
                            WriteXML(String.valueOf(etOfficer.getText()), String.valueOf(ClaimId),etCHFID.getText().toString(),Answers);
                        } catch (IllegalArgumentException | IllegalStateException e) {
                            e.printStackTrace();
                            return;
                        } catch (IOException e) {
                            e.printStackTrace();
                            return;
                        }

                        //Upload if internet is available

                        if(_General.isNetworkAvailable(Feedback.this)){

                            UploadFile uf = new UploadFile();
                            //String FileName = "feedback_" + etClaim.getText() + ".xml";
                            //File file = new File(Path + FileName);
                            if(uf.uploadFileToServer(Feedback.this,FeedbackXML,"feedback")){
                                if(ServerResponse()== 1){
                                    msgType = 1;
                                }else if (ServerResponse()==0){
                                    msgType = 2;
                                }
                                else
                                    msgType = -1;

                            }else{
                                msgType = 3;
                            }
                        }else{
                            msgType = 3;
                        }

                        File file = FeedbackXML;
                        MoveFile(file);

                        runOnUiThread(new Runnable() {
                            public void run() {
                                switch (msgType){
                                    case 1:
                                        DeleteRow(ClaimId);
                                        //ca.ShowDialog(getResources().getString(R.string.UploadedSuccessfully));
                                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.UploadedSuccessfully), Toast.LENGTH_LONG).show();
                                        break;
                                    case 2:
                                        DeleteRow(ClaimId);
                                       //ca. ShowDialog(getResources().getString(R.string.ServerRejected));
                                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.ServerRejected), Toast.LENGTH_LONG).show();
                                        break;
                                    case 3:
                                        UpdateRow(ClaimId);
                                       //ca. ShowDialog(getResources().getString(R.string.SavedOnSDCard));
                                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.SavedOnSDCard), Toast.LENGTH_LONG).show();
                                        break;
                                    case -1:
                                        //ca. ShowDialog(getResources().getString(R.string.FeedBackNotUploaded));
                                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.FeedBackNotUploaded), Toast.LENGTH_LONG).show();
                                        break;
                                }

                                finish();

                            }


                        });



                        pd.dismiss();
                    }

                }.start();


            }
        });
    }



    private void DeleteRow(int ClaimId){
       ca.CleanFeedBackTable(String.valueOf(ClaimId));
            }

    private void UpdateRow(int ClaimId){
       ca.UpdateFeedBack(ClaimId);
    }


    private void WriteXML(String Officer, String ClaimID, String CHFID, String Answers) throws IllegalArgumentException, IllegalStateException, IOException{
        //Here we are creating a directory
        File MyDir = new File(Path);
        MyDir.mkdir();

        File DirRejected = new File(Path + "RejectedFeedback");
        DirRejected.mkdir();

        File DirAccepted = new File(Path + "AcceptedFeedback");
        DirAccepted.mkdir();

        //Here we are giving name to the XML file
        FileName = "feedback_" + etClaimCode.getText() + ".xml";

        //Here we are creating file in that directory
        FeedbackXML = new File(MyDir,FileName);


        //Here we are creating outputstream
        FileOutputStream fos = new FileOutputStream(FeedbackXML);


        XmlSerializer serializer = Xml.newSerializer();

        serializer.setOutput(fos, "UTF-8");
        serializer.startDocument(null, Boolean.TRUE);
        serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
        serializer.startTag(null, "feedback");

        serializer.startTag(null, "Officer");
        serializer.text(Officer);
        serializer.endTag(null, "Officer");

        serializer.startTag(null, "ClaimID");
        serializer.text(ClaimID);
        serializer.endTag(null,"ClaimID");

        serializer.startTag(null, "CHFID");
        serializer.text(CHFID);
        serializer.endTag(null, "CHFID");

        serializer.startTag(null, "Answers");
        serializer.text(Answers);
        serializer.endTag(null, "Answers");

        serializer.startTag(null, "Date");
        @SuppressLint("SimpleDateFormat") SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
        Calendar cal = Calendar.getInstance();
        String d = formatter.format(cal.getTime());
        serializer.text(d);
        serializer.endTag(null, "Date");

        serializer.endTag(null, "feedback");
        serializer.endDocument();
        serializer.flush();
        fos.close();


    }
    private int ServerResponse(){

        CallSoap cs = new CallSoap();
        cs.setFunctionName("isValidFeedback");
        return  cs.isFeedbackAccepted(FeedbackXML.getName());

    }
    private void MoveFile(File file){
        switch(msgType){
            case 1:
                file.renameTo(new File(Path + "AcceptedFeedback/" + file.getName()));
                break;
            case 2:
                file.renameTo(new File(Path + "RejectedFeedback/" + file.getName()));
                break;
        }
    }
    private String Answers(){
        String Ans = "";
        rg1 = (RadioGroup)findViewById(R.id.radioGroup1);
        int Ans1 = rg1.getCheckedRadioButtonId();
        rg2 = (RadioGroup)findViewById(R.id.radioGroup2);
        int Ans2 = rg2.getCheckedRadioButtonId();
        rg3 = (RadioGroup)findViewById(R.id.radioGroup3);
        int Ans3 = rg3.getCheckedRadioButtonId();
        rg4 = (RadioGroup)findViewById(R.id.radioGroup4);
        int Ans4 = rg4.getCheckedRadioButtonId();

        if (Ans1 == R.id.rYes1)Ans = "1"; else Ans = "0";
        if (Ans2 == R.id.rYes2)Ans = Ans + "1"; else Ans = Ans + "0";
        if (Ans3 == R.id.rYes3)Ans = Ans + "1"; else Ans = Ans + "0";
        if (Ans4 == R.id.rYes4)Ans = Ans + "1"; else Ans = Ans + "0";

        //Read rating
        rb1 = (RatingBar)findViewById(R.id.ratingBar1);
        Ans = Ans + String.valueOf((int)rb1.getRating());
        return Ans;
    }
    private boolean isValidate(){

        if(etOfficer.getText().length() == 0){
            ca.ShowDialog(getResources().getString(R.string.MissingOfficer));
            etOfficer.requestFocus();
            return false;
        }
        if(etClaimCode.getText().length() == 0){
           ca. ShowDialog(getResources().getString(R.string.MissingClaimID));
            etClaimCode.requestFocus();
            return false;
        }
        if(etCHFID.getText().length() == 0){
            ca.ShowDialog(getResources().getString(R.string.MissingCHFID));
            etCHFID.requestFocus();
            return false;
        }
        if((!rbYes1.isChecked() && !rbNo1.isChecked()) || (!rbYes2.isChecked() && !rbNo2.isChecked()) || (!rbYes3.isChecked() && !rbNo3.isChecked()) || (!rbYes4.isChecked() && !rbNo4.isChecked())){
           ca. ShowDialog(getResources().getString(R.string.MissingAnswers));
            return false;
        }

        return true;
    }

}
