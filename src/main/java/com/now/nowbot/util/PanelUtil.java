package com.now.nowbot.util;

import org.jetbrains.skija.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PanelUtil {
    static final Logger log = LoggerFactory.getLogger(PanelUtil.class);
    /* **
     * SS-D #FEF668 #F09450 #3FBCEF #00B034 #3FBCEF #8E569B #EA6B48
     * 我方#00A8EC 对方 #FF0000
     */
    public static final int COLOR_SS = Color.makeRGB(254, 246, 104);
    public static final int COLOR_S = Color.makeRGB(240, 148, 80);
    public static final int COLOR_A_PLUS = Color.makeRGB(0, 176, 52);
    public static final int COLOR_A = Color.makeRGB(0, 176, 52);
    public static final int COLOR_B = Color.makeRGB(63, 188, 239);
    public static final int COLOR_C = Color.makeRGB(142, 86, 155);
    public static final int COLOR_D = Color.makeRGB(234, 107, 72);

    public static final int COLOR_HEX_ME = Color.makeRGB(0, 168, 236);
    public static final int COLOR_HEX_OTHER = Color.makeRGB(255, 0, 0);

    public static class PanelBuilder {
        final Paint p_white = new Paint().setARGB(255, 255, 255, 255).setStrokeWidth(10);
        int width;
        int hight;
        final Surface surface;
        Canvas canvas;
        boolean isClose = false;

        PanelBuilder(int w, int h) {
            width = w;
            hight = h;
            surface = Surface.makeRasterN32Premul(w, h);
            canvas = surface.getCanvas();
//            canvas.clear(Color.makeARGB(100, 0, 0, 0));
        }

        PanelBuilder(int w, int h, Image bg) {
            width = w;
            hight = h;
            surface = Surface.makeRasterN32Premul(w, h);
            canvas = surface.getCanvas();
            if (bg.getWidth() != w || bg.getHeight() != h) bg = SkiaUtil.getScaleCenterImage(bg, w, h);
            canvas.drawImage(bg, 0, 0);
        }

        /**
         * 绘制叠加层
         * @param add
         * @return
         */
        PanelBuilder drowImage(Image add){
            canvas.drawImage(add,0,0);
            return this;
        }

        /**
         * 指定位置
         * @param add
         * @param x
         * @param y
         * @return
         */
        PanelBuilder drowImage(Image add, int x, int y){
            canvas.save();
            canvas.translate(x, y);
            canvas.drawImage(add, 0, 0);
            canvas.restore();
            return this;
        }
        Image build(int r) {
            try (surface) {
                isClose = true;
                return RRectout(surface, r);
            }
        }

        static Image RRectout(Surface surface, int r) {
            var img = surface.makeImageSnapshot();
            var canvas = surface.getCanvas();
            canvas.clear(0);
            canvas.save();
            canvas.clipRRect(RRect.makeXYWH(0, 0, surface.getWidth(), surface.getHeight(), r));
            canvas.drawImage(img, 0, 0, new Paint().setAntiAlias(true).setMode(PaintMode.FILL));
            canvas.restore();
            var rImg = surface.makeImageSnapshot();
            canvas.clear(0);
            canvas.drawImage(img, 0, 0);
            return rImg;
        }

        public boolean isClose() {
            return isClose;
        }
    }

    //-----------卡片---------------
    public static class ACardBuilder extends PanelBuilder {
        ACardBuilder(Image bg) {
            super(430, 210, bg);
        }

        public ACardBuilder drowB1(String text) {
            return drowB(text, 177);
        }

        public ACardBuilder drowB2(String text) {
            return drowB(text, 151);
        }

        public ACardBuilder drowB3(String text) {
            return drowB(text, 125);
        }

        ACardBuilder drowB(String text, int y) {
            canvas.save();
            Typeface typeface = SkiaUtil.getTorusSemiBold();
            Font font = new Font(typeface, 24)
                    .setHinting(FontHinting.NONE)
                    .setBaselineSnapped(true);
            var line = TextLine.make(text, font);
            try (font; line) {
                canvas.translate(20, y);
                canvas.drawTextLine(line, 0, line.getCapHeight(), p_white);
            }
            canvas.restore();
            return this;
        }

        public ACardBuilder drowC1(String text) {
            return drowC(text, surface.getWidth() - 10, 153);
        }

        public ACardBuilder drowC2(String text) {
            return drowC(text, surface.getWidth() - 10, 126);
        }

        public ACardBuilder drowC3(String text) {
            return drowC(text, surface.getWidth() - 10, 99);
        }

        /***
         * 小字标识
         * @param text
         * @return
         */
        public ACardBuilder drowC4(String text) {
            canvas.save();
            Typeface typeface = SkiaUtil.getTorusSemiBold();
            Font font = new Font(typeface, 24)
                    .setHinting(FontHinting.NONE)
                    .setBaselineSnapped(true);
            var line = TextLine.make(text, font);
            try (font; line) {
                canvas.translate(215, 152);
                canvas.drawTextLine(line, 0, line.getCapHeight(), p_white);
            }
            canvas.restore();
            return this;
        }

        ACardBuilder drowC(String text, int x, int y) {
            canvas.save();
            Typeface typeface = SkiaUtil.getTorusSemiBold();
            Font font = new Font(typeface, 24)
                    .setHinting(FontHinting.NONE)
                    .setBaselineSnapped(true);
            var line = TextLine.make(text, font);
            try (font; line) {
                canvas.translate(x, y);
                canvas.drawTextLine(line, -line.getWidth(), line.getCapHeight(), p_white);
            }
            canvas.restore();
            return this;
        }

        public Image build() {
            return build(20);
        }
    }

    public static class A1CardBuilder extends ACardBuilder {
        A1CardBuilder(Image bg){
            super(bg);
        }
        /***
         * 头像
         * @param head_url
         * @return
         */
        public A1CardBuilder drowA1(String head_url) {
            canvas.save();
            final Image head = SkiaUtil.lodeNetWorkImage(head_url);
            try (head) {
                canvas.translate(20, 20);
                canvas.clipRRect(RRect.makeXYWH(0, 0, 100, 100, 10));
                canvas.drawImage(SkiaUtil.getScaleCenterImage(head, 100, 100), 0, 0);
            }
            canvas.restore();
            return this;
        }

        /***
         * 国旗,撒泼特,好友状态
         * @return
         */
        public A1CardBuilder drowA2(Object... loge) {
            //todo
            return this;
        }

        /***
         * 名字
         * @param text
         * @return
         */
        public A1CardBuilder drowA3(String text) {
            canvas.save();
            Typeface typeface = SkiaUtil.getTorusSemiBold();
            final Font font = new Font(typeface, 48)
                    .setHinting(FontHinting.NONE)
                    .setBaselineSnapped(true);
            final var line = TextLine.make(text, font);
            try (font; line) {
                canvas.translate(130, 23);
                canvas.drawTextLine(line, 0, line.getCapHeight(), p_white);
            }
            canvas.restore();
            return this;
        }
    }

    public static class A2CardBuilder extends ACardBuilder {
        A2CardBuilder(Image bg) {
            super(bg);
        }

        /***
         * 铺面状态
         * @param flag
         * @return
         */
        public A2CardBuilder drowD5(Object flag) {
            canvas.save();
            //todo 没有图标。。。
            canvas.restore();
            return this;
        }

        /***
         * 曲名
         * @param text
         * @return
         */
        public A2CardBuilder drowD4(String text) {
            canvas.save();
            Typeface typeface = SkiaUtil.getPuhuitiMedium();
            final Font font = new Font(typeface, 36)
                    .setHinting(FontHinting.NONE)
                    .setBaselineSnapped(true);
            final var line = TextLine.make(text, font);
            try (font; line) {
                canvas.translate(20, 24);
                canvas.drawTextLine(line, 0, line.getCapHeight(), p_white);
            }
            canvas.restore();
            return this;
        }

        public A2CardBuilder drowD3(String text) {
            canvas.save();
            Typeface typeface = SkiaUtil.getPuhuitiMedium();
            final Font font = new Font(typeface, 24)
                    .setHinting(FontHinting.NONE)
                    .setBaselineSnapped(true);
            final var line = TextLine.make(text, font);
            try (font; line) {
                canvas.translate(20, 62);
                canvas.drawTextLine(line, 0, line.getCapHeight(), p_white);
            }
            canvas.restore();
            return this;
        }

        /***
         *      到时候写个enum?
         * @param mode mod
         * @param color 颜色
         * @return
         */
        public A2CardBuilder drowD2(String mode, int color) {
            canvas.save();
            Typeface typeface = null;
            try {
                typeface = SkiaUtil.getEXTRA();
            } catch (Exception e) {
                log.error("字体加载异常", e);
                return this;
            }
            final Font font = new Font(typeface, 24)
                    .setHinting(FontHinting.NONE)
                    .setBaselineSnapped(true);
            final var line = TextLine.make(mode, font);
            try (font; line) {
                canvas.translate(20, 94);
                canvas.drawTextLine(line, 0, line.getCapHeight(), new Paint().setColor(color));
            }
            canvas.restore();
            return this;
        }

        public A2CardBuilder drowD1(String text) {
            canvas.save();
            Typeface typeface = SkiaUtil.getPuhuitiMedium();
            final Font font = new Font(typeface, 24)
                    .setHinting(FontHinting.NONE)
                    .setBaselineSnapped(true);
            final var line = TextLine.make(text, font);
            try (font; line) {
                canvas.translate(45, 94);
                canvas.drawTextLine(line, 0, line.getCapHeight(), p_white);
            }
            canvas.restore();
            return this;
        }
    }
    //-----------------------------

    public static class PPPanelBuilder extends PanelBuilder {
        PPPanelBuilder(Image bg) {
            super(1920, 1080, bg);
        }
        //l80 r430 m510 m790 m960 m1130 m1410 l1490 r1840
        /**
         * 左值名称 左对齐
         */
        private static final int X_L1 = 80;
        /**
         * 左数值 右对齐
         */
        private static final int X_L2 = 430;
        /**
         * 第一层文字高度
         */
        private static final int Y_T1 = 370 + 12;
        /**
         * 每层文字偏移
         */
        private static final int Y_T1_OFF = 115;
        /**
         * 大小字体高度差
         */
        private static final int Y_T1_SFONT = 16;
        /**
         * 左评价 居中对齐
         */
        private static final int X_L3 = 510;
        /**
         * 偏左中下 总和 居中对齐
         */
        private static final int X_L4 = 790;
        /**
         * 数字高度
         */
        private static final int Y_T2 = 945 + 12;

        /**
         * 偏右中下 总和
         */
        private static final int X_R4 = 1130;
        /**
         * 右评价
         */
        private static final int X_R3 = 1410;
        /**
         * 右值名称
         */
        private static final int X_R1 = 1490;
        /**
         * 右数值
         */
        private static final int X_R2 = 1840;

        /***
         * 绘制六边形
         * @param point 输入数值,范围[0-1];
         * @param color 颜色预设 true:蓝色 | false:红色
         * @return return
         */
        public PPPanelBuilder drowHexagon(float[] point, boolean color) {
            if (point.length != 6) {
                throw new RuntimeException("输入参数长度错误");
            }
            canvas.save();
            Path[] paths = SkiaUtil.creat6(180, 10, point);
            if (paths == null || paths.length != 2) {
                throw new RuntimeException("创建形状错误");
            }
            final var pt1 = paths[0];
            final var pt2 = paths[1];
            int c = color ? COLOR_HEX_ME : COLOR_HEX_OTHER;
            try (pt1; pt2) {
                canvas.translate(960, 600);
                //填充,半透
                canvas.drawPath(pt1, new Paint().setColor(c).setAlpha(102).setStroke(false));
                canvas.drawPath(pt1, new Paint().setColor(c).setStrokeWidth(5).setStroke(true));
                canvas.drawPath(pt2, new Paint().setColor(c).setStroke(false));
            }
            canvas.restore();
            return this;
        }

        /***
         * 绘制左边的value值  [name  value rank/sign]
         * @param n 行数下标,从零开始
         * @param text 文字
         * @return
         */
        public PPPanelBuilder drowLeftValueN(int n, String text) {
            if (n < 0 || n > 5) throw new RuntimeException("超出范围");
            return drowLeftValue(text, Y_T1 + Y_T1_OFF * n);
        }

        /***
         * 绘制左边的name值  [name  value rank/sign]
         * @param n 行数下标,从零开始
         * @param text 文字
         * @return
         */
        public PPPanelBuilder drowLeftNameN(int n, String text) {
            if (n < 0 || n > 5) throw new RuntimeException("超出范围");
            return drowLeftName(text, Y_T1 + Y_T1_OFF * n);
        }

        /***
         * 绘制左边的rank/sing值  [name  value rank/sign]
         * @param n 行数下标,从零开始
         * @param text 文字
         * @return
         */
        public PPPanelBuilder drowLeftRankN(int n, String text, int color) {
            if (n < 0 || n > 5) throw new RuntimeException("超出范围");
            return drowCenterText(text, X_L3, Y_T1 + Y_T1_OFF * n, new Paint().setColor(color));
        }

        /***
         * 绘制右边的value值  [name  value rank/sign]
         * @param n 行数下标,从零开始
         * @param text 文字
         * @return
         */
        public PPPanelBuilder drowRightValueN(int n, String text) {
            if (n < 0 || n > 5) throw new RuntimeException("超出范围");
            return drowRightValue(text, Y_T1 + Y_T1_OFF * n);
        }

        /***
         * 绘制右边的name值  [name  value rank/sign]
         * @param n 行数下标,从零开始
         * @param text 文字
         * @return
         */
        public PPPanelBuilder drowRightNameN(int n, String text) {
            if (n < 0 || n > 5) throw new RuntimeException("超出范围");
            return drowRightName(text, Y_T1 + Y_T1_OFF * n);
        }

        /***
         * 绘制右边的rank/sing值  [name  value rank/sign]
         * @param n 行数下标,从零开始
         * @param text 文字
         * @return
         */
        public PPPanelBuilder drowRightRankN(int n, String text, int color) {
            if (n < 0 || n > 5) throw new RuntimeException("超出范围");
            return drowCenterText(text, X_R3, Y_T1 + Y_T1_OFF * n, new Paint().setColor(color));
        }
        /**
         * 左侧value大小文字渲染
         * @param n 层数
         * @param bigText 大文字
         * @param simText 小文字
         * @return
         */
        public PPPanelBuilder drowLeftValueN(int n, String bigText, String simText){
            canvas.save();
            Typeface typeface = SkiaUtil.getTorusSemiBold();
            final Font fontB = new Font(typeface, 60);
            final Font fontS = new Font(typeface, 36);
            final var lineB = TextLine.make(bigText, fontB);
            final var lineS = TextLine.make(simText, fontS);
            try (typeface; fontB;lineB;lineS) {
                canvas.translate(X_L2, Y_T1 + Y_T1_OFF * n);
                canvas.drawTextLine(lineS, -lineS.getWidth(), lineS.getCapHeight() + Y_T1_SFONT, p_white);
                canvas.drawTextLine(lineB, -(lineS.getWidth() + lineB.getWidth()), lineB.getCapHeight(), p_white);
            }
            canvas.restore();
            return this;
        }

        /**
         * 右侧value大小文字渲染
         * @param n 层数
         * @param bigText 大文字
         * @param simText 小文字
         * @return
         */
        public PPPanelBuilder drowRightValueBS(int n, String bigText, String simText){
            canvas.save();
            Typeface typeface = SkiaUtil.getTorusSemiBold();
            final Font fontB = new Font(typeface, 60);
            final Font fontS = new Font(typeface, 36);
            final var lineB = TextLine.make(bigText, fontB);
            final var lineS = TextLine.make(simText, fontS);
            try (typeface; fontB;lineB;lineS) {
                canvas.translate(X_R2, Y_T1 + Y_T1_OFF * n );
                canvas.drawTextLine(lineS, -lineS.getWidth(), lineS.getCapHeight() + Y_T1_SFONT, p_white);
                canvas.drawTextLine(lineB, -(lineS.getWidth() + lineB.getWidth()), lineB.getCapHeight(), p_white);
            }
            canvas.restore();
            return this;
        }

        /**
         * 总值 左
         * @param text
         * @return
         */
        public PPPanelBuilder drowLeftTotal(String text) {
            return drowCenterText(text, X_L4, Y_T2);
        }
        /**
         * 总值 右
         * @param text
         * @return
         */
        public PPPanelBuilder drowRightTotal(String text) {
            return drowCenterText(text, X_R4, Y_T2);
        }

        /***
         * 左侧数值名称
         * @param text
         * @param top
         * @return
         */
        private PPPanelBuilder drowLeftName(String text, int top) {
            return drowLeftText(text, X_L1, top);
        }

        /***
         * 右侧数值名称
         * @param text
         * @param top
         * @return
         */
        private PPPanelBuilder drowRightName(String text, int top) {
            return drowLeftText(text, X_R1, top);
        }

        /***
         * 左侧数值
         * @param text
         * @param top
         * @return
         */
        private PPPanelBuilder drowLeftValue(String text, int top) {
            return drowRightText(text, X_L2, top);
        }

        /***
         * 右侧数值
         * @param text
         * @param top
         * @return
         */
        private PPPanelBuilder drowRightValue(String text, int top) {
            return drowRightText(text, X_R2, top);
        }
        //分别对应着 左 中 右 文字对齐方式

        /***
         * 左对齐渲染
         * @param text
         * @param right
         * @param top
         * @return
         */
        private PPPanelBuilder drowLeftText(String text, int right, int top) {
            canvas.save();
            Typeface typeface = SkiaUtil.getTorusSemiBold();
            final Font font = new Font(typeface, 60);
            final var line = TextLine.make(text, font);
            try (typeface; font;line) {
                canvas.translate(right, top);
                canvas.drawTextLine(line, 0, line.getCapHeight(), p_white);
            }
            canvas.restore();
            return this;
        }

        /***
         * 居中对齐渲染
         * @param text
         * @param X
         * @param top
         * @return
         */
        private PPPanelBuilder drowCenterText(String text, int X, int top) {
            return drowCenterText(text, X, top, p_white);
        }

        /***
         * 居中对齐渲染 带颜色
         * @param text
         * @param X
         * @param top
         * @return
         */
        private PPPanelBuilder drowCenterText(String text, int X, int top, Paint color) {
            canvas.save();
            Typeface typeface = SkiaUtil.getTorusSemiBold();
            final Font font = new Font(typeface, 60);
            final var line = TextLine.make(text, font);
            try (typeface; font;line) {
                canvas.translate(X, top);
                canvas.drawTextLine(line, -line.getWidth() * 0.5f, line.getCapHeight(), color);
            }
            canvas.restore();
            return this;
        }

        /***
         * 右对齐渲染
         * @param text
         * @param right
         * @param top
         * @return
         */
        private PPPanelBuilder drowRightText(String text, int right, int top) {
            canvas.save();
            Typeface typeface = SkiaUtil.getTorusSemiBold();
            final Font font = new Font(typeface, 60);
            final var line = TextLine.make(text, font);
            try (typeface; font;line) {
                canvas.translate(right, top);
                canvas.drawTextLine(line, -line.getWidth(), line.getCapHeight(), p_white);
            }
            canvas.restore();
            return this;
        }

        /***
         * 左侧card
         * @param card
         * @return
         */
        public PPPanelBuilder drowLeftCard(Image card) {
            canvas.save();
            canvas.translate(40, 40);
            canvas.drawImage(card,0,0);
            canvas.restore();
            return this;
        }

        /***
         * 右侧card
         * @param card
         * @return
         */
        public PPPanelBuilder drowRightCard(Image card) {
            canvas.save();
            canvas.translate(width - card.getWidth()- 40 , 40);
            canvas.drawImage(card,0,0);
            canvas.restore();
            return this;
        }

        @Override
        public PPPanelBuilder drowImage(Image add) {
            super.drowImage(add);
            return this;
        }

        public Image build() {
            return super.build(15);
        }
    }
    public static class PPMPanelBuilder extends PPPanelBuilder {
        PPMPanelBuilder(Image bg){
            super(bg);
        }
    }

    /***
     * 玩家卡片
     * @return
     */
    public static A1CardBuilder getA1Builder(Image bg) {
        return new A1CardBuilder(bg);
    }

    /***
     * 谱面/成绩卡片
     * @return
     */
    public static A2CardBuilder getA2Builder(Image bg) {
        return new A2CardBuilder(bg);
    }

    /***
     * PPA(P)面板
     * @return
     */
    public static PPMPanelBuilder getPPMBulider(Image bg) {
        return new PPMPanelBuilder(bg);
    }

    public static String cutDecimalPoint(Double m){
        if (m == null) return "";
        Double s = m - m.intValue();
        if (s < 0.01) return "";
        String r = s.toString();
        return r.substring(1,Math.min(r.length(),4));
    }
}