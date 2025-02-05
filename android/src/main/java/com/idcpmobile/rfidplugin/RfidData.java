package com.idcpmobile.rfidplugin;

/**
 * @desp epc listview object
 * @author zhangdanli
 * @datetime 2024/11/8 13:55
 **/
public class RfidData {
    private String epc;
    private String tid;
    private int rssi;
    private int num;

    public String getEpc() {
        return epc;
    }

    public void setEpc(String epc) {
        this.epc = epc;
    }

    public String getTid() {
        return tid;
    }

    public void setTid(String tid) {
        this.tid = tid;
    }

    public int getRssi() {
        return rssi;
    }

    public void setRssi(int rssi) {
        this.rssi = rssi;
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }
}
