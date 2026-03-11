package com.example.appsentinel;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.appsentinel.api.ApiClient;
import com.example.appsentinel.api.ScanService;
import com.example.appsentinel.models.ScanResult;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    Button scanButton;
    ListView resultList;

    ArrayAdapter<String> adapter;
    List<String> results = new ArrayList<>();

    List<String> whitelist = Arrays.asList(
            "com.android.vending",
            "com.google.android.gms",
            "com.google.android.webview",
            "com.google.android.googlequicksearchbox",
            "com.amazon.mShop.android.shopping"
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        scanButton = findViewById(R.id.scanButton);
        resultList = findViewById(R.id.resultList);

        adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                results
        );

        resultList.setAdapter(adapter);

        scanButton.setOnClickListener(v -> {

            results.clear();
            adapter.notifyDataSetChanged();

            new Thread(this::scanApps).start();

        });
    }

    private void scanApps() {

        PackageManager pm = getPackageManager();

        List<PackageInfo> packages =
                pm.getInstalledPackages(PackageManager.GET_PERMISSIONS);

        ScanService service =
                ApiClient.getClient().create(ScanService.class);

        for (PackageInfo pkg : packages) {

            ApplicationInfo appInfo = pkg.applicationInfo;

            // skip system apps
            if ((appInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0)
                continue;

            // skip updated system apps
            if ((appInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0)
                continue;

            // skip framework apps
            if (whitelist.contains(pkg.packageName))
                continue;

            try {

                String apkPath = appInfo.sourceDir;

                File apkFile = new File(apkPath);

                RequestBody requestFile =
                        RequestBody.create(
                                MediaType.parse("application/vnd.android.package-archive"),
                                apkFile
                        );

                MultipartBody.Part apkPart =
                        MultipartBody.Part.createFormData(
                                "apk",
                                apkFile.getName(),
                                requestFile
                        );

                RequestBody packageName =
                        RequestBody.create(
                                MediaType.parse("text/plain"),
                                pkg.packageName
                        );

                Call<ScanResult> call =
                        service.scanApp(apkPart, packageName);

                call.enqueue(new Callback<ScanResult>() {

                    @Override
                    public void onResponse(
                            Call<ScanResult> call,
                            Response<ScanResult> response) {

                        if (response.isSuccessful() && response.body() != null) {

                            ScanResult result = response.body();

                            String riskLevel;

                            if (result.risk_score > 70)
                                riskLevel = "HIGH RISK";
                            else if (result.risk_score > 40)
                                riskLevel = "MEDIUM RISK";
                            else
                                riskLevel = "SAFE";

                            String line =
                                    pkg.packageName +
                                            " → " +
                                            riskLevel +
                                            " (" +
                                            result.risk_score +
                                            ")";

                            runOnUiThread(() -> {

                                results.add(line);
                                adapter.notifyDataSetChanged();

                            });

                        }

                    }

                    @Override
                    public void onFailure(
                            Call<ScanResult> call,
                            Throwable t) {

                        runOnUiThread(() -> {

                            results.add(pkg.packageName + " → Scan failed");
                            adapter.notifyDataSetChanged();

                        });

                    }

                });

            }

            catch (Exception ignored) {}

        }

    }

}