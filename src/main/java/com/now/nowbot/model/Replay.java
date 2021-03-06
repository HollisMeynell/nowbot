package com.now.nowbot.model;

import com.now.nowbot.util.lzma.LZMAInputStream;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class Replay {
    // 0 = osu!, 1 = osu!taiko, 2 = osu!catch, 3 = osu!mania
    byte mode;
    //创建该回放文件的游戏版本 例如：20131216
    int version;
    String MapHash;
    String username;
    // 回放文件的 MD5 hash
    String RepHash;
    short n_300;
    short n_100;
    short n_50;
    short n_geki;
    short n_katu;
    short n_miss;
    int score;
    short combo;
    // full combo（1 = 没有Miss和断滑条，并且没有提前完成的滑条）
    boolean perfect;
    int mods;
    Map<Integer, Float> HPList;
    //时间戳
    long date;
    int dataLength;
    List<hit> hitList;
    long scoreId;
    double tp;

    private Replay(ByteBuffer bf){
        if (bf.order() == ByteOrder.BIG_ENDIAN){
            bf.order(ByteOrder.LITTLE_ENDIAN);
        }
        mode = bf.get();
        version = bf.getInt();
        MapHash = readString(bf);
        username = readString(bf);
        RepHash = readString(bf);
        n_300 = bf.getShort();
        n_100 = bf.getShort();
        n_50 = bf.getShort();
        n_geki = bf.getShort();
        n_katu = bf.getShort();
        n_miss = bf.getShort();
        score = bf.getInt();
        combo = bf.getShort();
        perfect = bf.get() == 1;
        mods = bf.getInt();
        var Hp = readString(bf);
        date = readLong(bf);
        dataLength = bf.getInt();
        var data = new byte[dataLength];
        bf.get(data, 0, dataLength);
        hitList = hitList(data);
        scoreId = readLong(bf);
        if (bf.limit() >= 8 + bf.position()){
            tp = bf.getDouble();
        }

        HPList = readHp(Hp);
    }
    private static String readString(ByteBuffer bf){
//        int p = (0xFF & e[offset]) |
//                (0xFF & e[offset+1])<<8 |
//                (0xFF & e[offset+2])<<16 |
//                (0xFF & e[offset+3])<<24 ;
        if (bf.get() == 11){
            // 读取第二位 可变长int 值string byte长度
            int strLength = 0;
            int b = 0;
            byte temp;
            do {
                temp = bf.get();
                strLength += (0x7F & temp) << b;
                b += 7;
            }while ((temp & 0x80) != 0);
            //得到长度 读取string byte
            byte[] strData = new byte[strLength];
            bf.get(strData, 0, strLength);
            //转换string
            return new String(strData);
        }else {
            return "";
        }
    }
    private static long readLong(ByteBuffer bf){
        long value = 0;
        for (int i = 0; i < 8; i++) {
            int shift = (7-i) << 3;
            value |= ((long)0xff << i) & ((long)bf.get() << i);
        }
        return value;
    }
    private static Map<Integer, Float> readHp(String data){
        var p = Pattern.compile("(?<time>\\d+)\\|(?<hp>(\\d+)(\\.\\d+)?),");
        var m = p.matcher(data);
        var map = new LinkedHashMap<Integer, Float>();
        while (m.find()){
            int time = Integer.parseInt(m.group("time"));
            float hp = Float.parseFloat(m.group("hp"));
            map.put(time, hp);
        }
        return map;
    }
    private static List<hit> hitList(byte[] data){
        var hit_list = new LinkedList<hit>();
        try {
            String s = new String(new LZMAInputStream(new ByteArrayInputStream(data)).readAllBytes());
            var p = Pattern.compile("(?<time>\\d+)\\|(?<x>(\\d+)(\\.\\d+)?)\\|(?<y>(\\d+)(\\.\\d+)?)\\|(?<key>(\\d+)),");
            var m = p.matcher(s);
            while (m.find()){
                long time = Long.parseLong(m.group("time"));
                float x = Float.parseFloat(m.group("x"));
                float y = Float.parseFloat(m.group("y"));
                int key = Integer.parseInt(m.group("key"));
                hit_list.addLast(new hit(time, x, y, key));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return hit_list;
    }
    static class hit{
        long befTime;
//        鼠标的X坐标（从0到512）
        float x;
//        鼠标的Y坐标（从0到384）
        float y;
        //鼠标、键盘按键的组合（M1 = 1, M2 = 2, K1 = 4, K2 = 8, 烟雾 = 16）（K1 总是与 M1 一起使用，K2 总是与 M2 一起使用。所以 1+4=5 2+8=10。）
        int ket;

        public hit(long befTime, float x, float y, int ket) {
            this.befTime = befTime;
            this.x = x;
            this.y = y;
            this.ket = ket;
        }

        public long getBefTime() {
            return befTime;
        }

        public float getX() {
            return x;
        }

        public float getY() {
            return y;
        }

        public int getKet() {
            return ket;
        }
    }
    public static Replay readByteToRep(ByteBuffer buffer){
        return new Replay(buffer);
    }
    public static Replay readByteToRep(byte[] buffer){
        return new Replay(ByteBuffer.wrap(buffer));
    }

    public byte getMode() {
        return mode;
    }

    public int getVersion() {
        return version;
    }

    public String getMapHash() {
        return MapHash;
    }

    public String getUsername() {
        return username;
    }

    public String getRepHash() {
        return RepHash;
    }

    public short getN_300() {
        return n_300;
    }

    public short getN_100() {
        return n_100;
    }

    public short getN_50() {
        return n_50;
    }

    public short getN_geki() {
        return n_geki;
    }

    public short getN_katu() {
        return n_katu;
    }

    public short getN_miss() {
        return n_miss;
    }

    public int getScore() {
        return score;
    }

    public short getCombo() {
        return combo;
    }

    public boolean isPerfect() {
        return perfect;
    }

    public int getMods() {
        return mods;
    }

    public Map<Integer, Float> getHPList() {
        return HPList;
    }

    public long getDate() {
        return date;
    }

    public int getDataLength() {
        return dataLength;
    }

    public List<hit> getHitList() {
        return hitList;
    }

    public long getScoreId() {
        return scoreId;
    }

    public double getTp() {
        return tp;
    }
}
