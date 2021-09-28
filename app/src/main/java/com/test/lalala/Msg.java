package com.test.lalala;

public class Msg {
    public static final int TYPE_RECEIVED = 0;
    public static final int TYPE_SENT = 1;
    public static final int TYPE_RECEIVED_IMG = 2;
    public static final int TYPE_SENT_IMG = 3;

    private String content;
    private String time;
    private int type;
    public Msg(String content, int type, String time){
        this.content = content;
        this.type = type;
        this.time = time;
    }
    public String getContent(){
        return content;
    }
    public int getType(){
        return type;
    }
    public String getTime() {
        return time;
    }
}
