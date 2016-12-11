package com.example.zhangjia.cutkey.function;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.zhangjia.cutkey.R;
import com.example.zhangjia.cutkey.background.bean.DateTime;
import com.example.zhangjia.cutkey.background.bean.FSearchData;
import com.example.zhangjia.cutkey.background.bean.FSearchResult;
import com.example.zhangjia.cutkey.background.bean.MachiningHistory;
import com.example.zhangjia.cutkey.background.data.MachiningHistoryDatas;
import com.example.zhangjia.cutkey.background.model.ModelFuzzySearch;
import com.example.zhangjia.cutkey.background.model.ModelMachiningSerial;
import com.example.zhangjia.cutkey.background.model.ModelMachiningUnlockToolCode;
import com.example.zhangjia.cutkey.base.BaseActivity;
import com.example.zhangjia.cutkey.bean.Tooth;
import com.example.zhangjia.cutkey.view.EditTextWithDel;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AddMachiningActivity extends BaseActivity {
    private ListView sortview;
    private EditTextWithDel mEtSeachName;
    private List<Tooth> toothList = new ArrayList<>();
    private FuzzyAdapter adapter;
    private PopupWindow pw; //弹框
    private int selcton = -1;
    private ModelFuzzySearch modelFuzzySearch;
    private RelativeLayout rl_serch;//搜索选择框
    private List<String> choose;//搜索选择的内容
    private ListView chooselist;
    private ChooseAdapter chooseAdapter;
    private Button fuzzy;
    private TextView tv_serch;
    private boolean bechoose;
    private Button btn_loading;
    private FSearchResult fSearchResult;
    private List<FSearchData> fSearchDataList = new ArrayList<>();
    private int type;
    private MachiningHistory machiningHistory;
    private  String s;
    private TextView num;
    private ProgressDialog dialog;
    private boolean checks = false;
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 1:
                    dialog.dismiss();
                    setAdapter();
                    break;
            }
        }
    };

    @Override
    protected View initView() {
        View view = View.inflate(this,R.layout.activity_add_machining,null);
        mEtSeachName = (EditTextWithDel) view.findViewById(R.id.et_search);
        sortview = (ListView) view.findViewById(R.id.lv_contact);
        fuzzy = (Button) view.findViewById(R.id.btn_fuzzy);
        rl_serch = (RelativeLayout) view.findViewById(R.id.rl_serch);
        tv_serch = (TextView)view.findViewById(R.id.tv_serch);
        btn_loading = (Button)view.findViewById(R.id.btn_load);
        num = (TextView)view.findViewById(R.id.tv_result_num);
        return view;
    }

    @Override
    protected void initDate() {
        String t = "123";
        type = getIntent().getIntExtra("type",1);
        if (type == 1){
            tv_serch.setText("编码搜索");
        }else if (type == 2){
            tv_serch.setText("齿型码搜索");
        }
        showbackVisible(View.VISIBLE);
        setTitle("搜索");
        choose = new ArrayList<String>() {
            {
                add("齿型码搜索");
                add("编码搜索");
            }
        };
        chooselist = new ListView(this);
        chooselist.setBackgroundColor(Color.parseColor("#FFFFFF"));
        chooselist.setDivider(null);
        modelFuzzySearch = new ModelFuzzySearch();
        chooseAdapter = new ChooseAdapter();
        chooselist.setAdapter(chooseAdapter);
        fuzzy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "来米", Toast.LENGTH_SHORT).show();
                machiningHistory = new MachiningHistory();
                machiningHistory.setSerialIdEnd(fSearchDataList.get(selcton).getSerialIdEnd());
                machiningHistory.setSerial(s);
                machiningHistory.setSerialId(fSearchDataList.get(selcton).getSerialId());
                machiningHistory.setKeyWidth(fSearchDataList.get(selcton).getKeyWidth());
                machiningHistory.setNo(fSearchDataList.get(selcton).getNo());
                machiningHistory.setCuts(fSearchDataList.get(selcton).getCuts());
                machiningHistory.setJaw(fSearchDataList.get(selcton).getJaw());
                machiningHistory.setSearchDateTime(DateTime.now().toDateTimeString());
                MachiningHistoryDatas.addMachiningHistoryData(machiningHistory);
                if (type == 1){
                    Intent intent = new Intent(getApplicationContext(),CodePrcessActivity.class);
                    startActivity(intent);
                }else if (type == 2) {
                    Intent intent = new Intent(getApplicationContext(), ToothProcessActivity.class);
                    startActivity(intent);
                }

            }
        });
        btn_loading.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkinput();
            }
        });
        chooselist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                tv_serch.setText(choose.get(position));
                pw.dismiss();
            }
        });
        initEvents();
    }

    private void checkinput() {
        String result = mEtSeachName.getText().toString().toUpperCase();
        char[] type = result.toCharArray();
        for (int i = 0;i < type.length;i++){
            int check = Integer.valueOf(type[i]);
            if (check == 45 || (check >= 65 && check <= 97) || (check >= 49 && check <= 57)){
                checks = true;
            }else {
                checks = false;
                break;
            }
        }
        if (checks == true){
            loading();
            num.setText(fSearchDataList.size() + "");
        }else {
            Toast.makeText(getApplicationContext(),"有非法字符存在，请检查后重新输入",Toast.LENGTH_LONG).show();
        }
    }

    private class Adddata extends AsyncTask<Objects,Integer,List<FSearchData>>{
        @Override
        protected void onPreExecute() {

        }

        @Override
        protected List<FSearchData> doInBackground(Objects... params) {
            fSearchDataList = fSearchResult.getData();
            return fSearchDataList;
        }

        @Override
        protected void onPostExecute(List<FSearchData> fSearchDatas) {
            super.onPostExecute(fSearchDatas);
            dialog.dismiss();
            setAdapter();
        }
    }

    private void loading() {
        s = mEtSeachName.getText().toString();
        Log.d("ss",s);
        String type = tv_serch.getText().toString();
        Log.d("type",type);
        if (type.equals("编码搜索")){
            bechoose = true;
        }else if (type.equals("齿型码搜索")){
            bechoose = false;
        }
        modelFuzzySearch = new ModelFuzzySearch();
        fSearchResult = new FSearchResult();
        fSearchResult = modelFuzzySearch.fuzzySearch2(s,bechoose);
        boolean result = fSearchResult.isResult();
        if (result == true){
            dialog = new ProgressDialog(AddMachiningActivity.this);
            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            dialog.setMessage("正在加载数据.......");
            dialog.show();
            new Thread(){
                @Override
                public void run() {
                    fSearchDataList = fSearchResult.getData();
                    Message msg = handler.obtainMessage();
                    msg.what = 1;
                    handler.sendMessage(msg);
                }
            }.start();
           // Adddata adddata = new Adddata();
            //adddata.execute();
        }else {
            String tip = fSearchResult.getTips();
            Toast.makeText(getApplicationContext(),tip,Toast.LENGTH_SHORT).show();
        }

    }

    private void setAdapter() {
        adapter = new FuzzyAdapter(this, fSearchDataList);
        sortview.setAdapter(adapter);
    }

    private void initEvents() {
        //ListView点击事件
        sortview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
                // Toast.makeText(getContext(), ((UnlockTool) adapter.getItem(position)).getUnlockToolCodeName(), Toast.LENGTH_SHORT).show();
                selcton = position;
                adapter.notifyDataSetChanged();
            }
        });

    }
    private class FuzzyAdapter extends BaseAdapter {
        private List<FSearchData> list = null;
        private Context mContext;

        public FuzzyAdapter(Context mContext, List<FSearchData> list) {
            this.list = list;
            this.mContext = mContext;
        }

        public void updateListView(List<FSearchData> list) {
            this.list = list;
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return fSearchDataList.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ;
            ViewHolder viewHolder;
            if (convertView == null) {
                convertView = LayoutInflater.from(mContext).inflate(R.layout.fuzzyserch_item, null);
                viewHolder = new ViewHolder();
                viewHolder.No = (TextView) convertView.findViewById(R.id.tv_no);
                viewHolder.serialId = (TextView) convertView.findViewById(R.id.tv_serial_id);
                viewHolder.keyWidth = (TextView) convertView.findViewById(R.id.tv_key_width);
                viewHolder.cuts = (TextView) convertView.findViewById(R.id.tv_cuts);
                viewHolder.serialStart = (TextView) convertView.findViewById(R.id.tv_serial_start);
                viewHolder.serialEnd = (TextView) convertView.findViewById(R.id.tv_serial_end);
                viewHolder.serialCount = (TextView) convertView.findViewById(R.id.tv_serial_count);
                viewHolder.jaw = (TextView) convertView.findViewById(R.id.tv_jaw);
                viewHolder.bool = (TextView) convertView.findViewById(R.id.tv_bool);
                viewHolder.select = (RadioButton) convertView.findViewById(R.id.rb_select);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            viewHolder.No.setText(fSearchDataList.get(position).getNo() + "");
            viewHolder.serialId.setText(fSearchDataList.get(position).getSerialId() + "");
            viewHolder.keyWidth.setText(fSearchDataList.get(position).getKeyWidth() + "");
            viewHolder.cuts.setText(fSearchDataList.get(position).getCuts());
            viewHolder.serialStart.setText(fSearchDataList.get(position).getSerialStart());
            viewHolder.serialEnd.setText(fSearchDataList.get(position).getSerialIdEnd());
            viewHolder.serialCount.setText(fSearchDataList.get(position).getSerialCount() + "");
            viewHolder.jaw.setText(fSearchDataList.get(position).getJaw());
            if (fSearchDataList.get(position).isRelevant()) {
                viewHolder.bool.setText("YES");
            } else {
                viewHolder.bool.setText("NO");
            }
            if (selcton == position) {
                viewHolder.select.setChecked(true);
            } else {
                viewHolder.select.setChecked(false);
            }
            return convertView;
        }


        class ViewHolder {
            TextView No;
            TextView serialId;
            TextView keyWidth;
            TextView cuts;
            TextView serialStart;
            TextView serialEnd;
            TextView serialCount;
            TextView jaw;
            TextView bool;
            RadioButton select;
        }
    }

    private class ChooseAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return choose.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if (convertView == null) {
                convertView = View.inflate(getApplicationContext(), R.layout.choose_item, null);
                viewHolder = new ViewHolder();
                viewHolder.choose = (TextView) convertView.findViewById(R.id.tv_choose);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            viewHolder.choose.setText(choose.get(position));
            return convertView;
        }

        private class ViewHolder {
            TextView choose;
        }
    }
}
