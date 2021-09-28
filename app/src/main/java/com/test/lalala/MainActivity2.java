package com.test.lalala;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class MainActivity2 extends AppCompatActivity {
    String ip;
    String msg1;
    String photo;
    EditText send;
    Button sendButton;
    Button disconnectButton;

    Socket socket;
    BufferedReader reader;
    PrintWriter writer;
    RecyclerView recyclerView;
    MyAdapter myAdapter;

    //圖片檔案物件
    ImageButton imgButton;
    ArrayList<Msg> msgList = new ArrayList<>();
    @SuppressLint("SimpleDateFormat")
    SimpleDateFormat format = new SimpleDateFormat("h:mm a");
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            send(msg1);
            Msg msg = new Msg(msg1, Msg.TYPE_SENT, String.valueOf(format.format(System.currentTimeMillis())));
            msgList.add(msg);
        }
    };
    Runnable runnable2 = new Runnable() {
        @Override
        public void run() {
            send(photo);
            Msg msg = new Msg(photo, Msg.TYPE_SENT_IMG, String.valueOf(format.format(System.currentTimeMillis())));
            msgList.add(msg);
        }
    };
    private ActivityResultLauncher<Intent> pickPicLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        Intent intent2 = getIntent();
        Intent intent1 = new Intent(this, MainActivity.class);
        ip = intent2.getStringExtra("ip");
        send = findViewById(R.id.send);
        sendButton = findViewById(R.id.sendButton);
        disconnectButton = findViewById(R.id.disconnectButton);
        imgButton = findViewById(R.id.imgButton);
        pickPicLauncher = getPickPicLauncher();


        //設置RecycleView
        recyclerView = findViewById(R.id.recycleView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        myAdapter = new MyAdapter();
        recyclerView.setAdapter(myAdapter);

        //連線
        connect(ip);

        //斷線
        disconnectButton.setOnClickListener(view -> {
            try {
                if (socket != null)
                    socket.close();
                startActivity(intent1);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        //傳送訊息
        sendButton.setOnClickListener(view -> {
            if (socket != null) {
                msg1 = send.getText().toString().trim();
                if (!msg1.equals("")) {
                    new Thread(runnable).start();
                } else {
                    Toast.makeText(this, "請輸入要傳送的訊息", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "請連線至伺服器", Toast.LENGTH_SHORT).show();
            }
            send.setText("");
        });

        //傳送圖片
        imgButton.setOnClickListener(view -> {
            try {
                // 5. 實例化Intent物件
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                // 6. 執行
                pickPicLauncher.launch(intent);
            } catch (ActivityNotFoundException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * 取得發射器物件
     * 1. 實例化 約定 物件
     * 2. 實例化 回呼 物件
     * 3. 實例化 發射器 物件
     *
     * @return 發射器物件
     */
    private ActivityResultLauncher<Intent> getPickPicLauncher() {
        return registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), activityResult -> {
                    /* 4. 取得圖像 **/
                    if (activityResult.getResultCode() != RESULT_OK) {
                        return;
                    }
                    try {
                        // 4.1 取得Uri物件
                        assert activityResult.getData() != null;
                        Uri uri = activityResult.getData().getData();
                        Bitmap bitmap;
                        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.P) {
                            // Android 9-
                            // 4.2 取得InputStream物件
                            InputStream is = getContentResolver().openInputStream(uri);
                            // 4.3 取得Bitmap物件
                            bitmap = BitmapFactory.decodeStream(is);

                            //將bitmap一位元組流輸出 Bitmap.CompressFormat.PNG 壓縮格式，100：壓縮率，byteArrayOutputStream：位元組流
                            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
                            byteArrayOutputStream.close();
                            byte[] buffer = byteArrayOutputStream.toByteArray();
                            System.out.println("圖片的大小：" + buffer.length);
                            //將圖片的位元組流資料加密成base64字元輸出
                            photo = Base64.encodeToString(buffer, 0, buffer.length, Base64.NO_WRAP);

                        } else {
                            // Android 9(+
                            // 	4.2 從Uri物件建立ImageDecoder.Source物件
                            ImageDecoder.Source source = ImageDecoder.createSource(
                                    getContentResolver(),
                                    uri);
                            // 4.3 取得Bitmap物件
                            bitmap = ImageDecoder.decodeBitmap(source);

                            //將bitmap一位元組流輸出 Bitmap.CompressFormat.PNG 壓縮格式，100：壓縮率，byteArrayOutputStream：位元組流
                            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
                            byteArrayOutputStream.close();
                            byte[] buffer = byteArrayOutputStream.toByteArray();
                            System.out.println("圖片的大小：" + buffer.length);
                            //將圖片的位元組流資料加密成base64字元輸出
                            photo = Base64.encodeToString(buffer, 0, buffer.length, Base64.NO_WRAP);
                        }
                        System.out.println(photo);
                        new Thread(runnable2).start();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
        );
    }

    public void send(String out) {
        if (writer != null) {
            writer.write(out + "\n");
            writer.flush();
        }
    }

    public void connect(String ip) {
        new Thread() {
            @Override
            public void run() {
                try {
                    socket = new Socket(ip, 5000);
                    writer = new PrintWriter(///將輸出流包裝成列印流
                            new OutputStreamWriter(
                                    socket.getOutputStream()));//獲取一個輸出流，向服務端傳送資訊
                    reader = new BufferedReader(
                            new InputStreamReader(
                                    socket.getInputStream()));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (line.length() > 100) {
                            Msg msg = new Msg(line, Msg.TYPE_RECEIVED_IMG, String.valueOf(format.format(System.currentTimeMillis())));
                            msgList.add(msg);
                        } else {
                            Msg msg = new Msg(line, Msg.TYPE_RECEIVED, String.valueOf(format.format(System.currentTimeMillis())));
                            msgList.add(msg);
                        }
                        runOnUiThread(() -> {
                            // 更新UI的操作
                            recyclerView.scrollToPosition(msgList.size() - 1);
                            myAdapter.refreshList();
                        });
                    }
                    writer.close();
                    reader.close();
                    writer = null;
                    reader = null;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    public Bitmap base64ToBitmap(String photo) {
        byte[] bytes = Base64.decode(photo, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    public String spliceImg(String photo) {
        int index = photo.indexOf(":");
        String info;
        if (index >= 0) {
            //獲取暱稱
            info = photo.substring(index + 1);
        } else {
            info = photo;
        }
        return info;
    }

    public String spliceName(String photo) {
        int index = photo.indexOf(":");
        String theName = null;

        if (index >= 0) {
            //獲取暱稱
            theName = photo.substring(0, index);
        }
        return theName;
    }

    private class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {

        @SuppressLint("NotifyDataSetChanged")
        public void refreshList() {
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        //連接layout檔案，return一個View
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.msg_item, parent, false);
            return new ViewHolder(view);
        }

        @Override
        //在這裡取得元件的控制(每個item內的控制)
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

            Msg msg = msgList.get(position);

            if (msg.getType() == Msg.TYPE_RECEIVED) {
                //如果是收到的訊息
                holder.left.setVisibility(View.VISIBLE);
                holder.right.setVisibility(View.GONE);
                holder.left2.setVisibility(View.GONE);
                holder.right2.setVisibility(View.GONE);
                holder.leftMsg.setText(spliceImg(msg.getContent()));
                holder.leftMsg3.setText(spliceName(msg.getContent()));
                holder.timeLeft.setText(msg.getTime());

            } else if (msg.getType() == Msg.TYPE_SENT) {
                //如果是發出的訊息
                holder.right.setVisibility(View.VISIBLE);
                holder.left.setVisibility(View.GONE);
                holder.left2.setVisibility(View.GONE);
                holder.right2.setVisibility(View.GONE);
                holder.rightMsg.setText(msg.getContent());
                holder.timeRight.setText(msg.getTime());
            } else if (msg.getType() == Msg.TYPE_RECEIVED_IMG) {
                //如果是收到的圖片
                holder.left2.setVisibility(View.VISIBLE);
                holder.right2.setVisibility(View.GONE);
                holder.left.setVisibility(View.GONE);
                holder.right.setVisibility(View.GONE);
                holder.leftMsg2.setText(spliceName(msg.getContent()));
                holder.imgLeft.setImageBitmap(base64ToBitmap(spliceImg(msg.getContent())));
                holder.timeLeft2.setText(msg.getTime());
            } else if (msg.getType() == Msg.TYPE_SENT_IMG) {
                //如果是發出的圖片
                holder.right2.setVisibility(View.VISIBLE);
                holder.left2.setVisibility(View.GONE);
                holder.left.setVisibility(View.GONE);
                holder.right.setVisibility(View.GONE);
                holder.imgRight.setImageBitmap(base64ToBitmap(msg.getContent()));
                holder.timeRight2.setText(msg.getTime());
            }

        }

        @Override
        //取得顯示數量，return一個int，通常都會return陣列長度(arrayList.size)
        public int getItemCount() {
            return msgList.size();
        }

        //取用方法並在內部連結所需要的控件
        class ViewHolder extends RecyclerView.ViewHolder {
            ConstraintLayout left;
            ConstraintLayout right;
            ConstraintLayout left2;
            ConstraintLayout right2;
            TextView leftMsg;
            TextView leftMsg2;
            TextView leftMsg3;
            TextView rightMsg;
            TextView timeLeft;
            TextView timeRight;
            TextView timeLeft2;
            TextView timeRight2;
            ImageView imgLeft;
            ImageView imgRight;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                left = itemView.findViewById(R.id.left);
                right = itemView.findViewById(R.id.right);
                left2 = itemView.findViewById(R.id.left2);
                right2 = itemView.findViewById(R.id.right2);
                leftMsg = itemView.findViewById(R.id.left_msg);
                leftMsg2 = itemView.findViewById(R.id.left_msg2);
                leftMsg3 = itemView.findViewById(R.id.left_msg3);
                rightMsg = itemView.findViewById(R.id.right_msg);
                timeLeft = itemView.findViewById(R.id.timeLeft);
                timeRight = itemView.findViewById(R.id.timeRight);
                timeLeft2 = itemView.findViewById(R.id.timeLeft2);
                timeRight2 = itemView.findViewById(R.id.timeRight2);
                imgLeft = itemView.findViewById(R.id.imgLeft);
                imgRight = itemView.findViewById(R.id.imgRight);
            }
        }

    }


}