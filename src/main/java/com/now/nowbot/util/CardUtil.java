package com.now.nowbot.util;

import org.jetbrains.skija.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CardUtil {
    static final Logger log = LoggerFactory.getLogger(CardUtil.class);
    public static class PanelBuilder {
        final Paint p_white = new Paint().setARGB(255,255,255,255).setStrokeWidth(10);
        int width;
        int hight;
        final Surface surface;
        Canvas canvas;
        boolean isClose = false;
        PanelBuilder(int w, int h){
            width = w;
            hight = h;
            surface = Surface.makeRasterN32Premul(w, h);
            canvas = surface.getCanvas();
            canvas.clear(Color.makeARGB(100,0,0,0));
        }
        PanelBuilder(int w, int h, Image bg){
            width = w;
            hight = h;
            surface = Surface.makeRasterN32Premul(w, h);
            canvas = surface.getCanvas();
            canvas.drawImage(SkiaUtil.getScaleCenterImage(bg,w,h),0,0);
        }

        public Image build(int r) {
            try(surface){
                isClose = true;
                return CardUtil.RRectout(surface,r);
            }
        }
        public boolean isClose(){ return isClose; }
    }
    //-----------卡片---------------
    public static class ACardBuilder extends PanelBuilder {
        ACardBuilder() {
            super(430, 210);
        }
        public ACardBuilder drowB1(String text){
            return drowB(text, 177);
        }
        public ACardBuilder drowB2(String text){
            return drowB(text, 151);
        }
        public ACardBuilder drowB3(String text){
            return drowB(text, 125);
        }
        ACardBuilder drowB(String text, int y){
            canvas.save();
            Typeface typeface = SkiaUtil.getTorusSemiBold();
            Font font = new Font(typeface,24)
                    .setHinting(FontHinting.NONE)
                    .setBaselineSnapped(true);
            var line = TextLine.make(text ,font);
            try(font; line) {
                canvas.translate(20, y);
                canvas.drawTextLine(line, 0, line.getCapHeight(), p_white);
            }
            canvas.restore();
            return this;
        }
        public ACardBuilder drowC1(String text){
            return drowC(text, surface.getWidth() - 10, 153);
        }
        public ACardBuilder drowC2(String text){
            return drowC(text, surface.getWidth() - 10, 126);
        }
        public ACardBuilder drowC3(String text){
            return drowC(text, surface.getWidth() - 10, 99);
        }

        /***
         * 小字标识
         * @param text
         * @return
         */
        public ACardBuilder drowC4(String text){
            canvas.save();
            Typeface typeface = SkiaUtil.getTorusSemiBold();
            Font font = new Font(typeface,24)
                    .setHinting(FontHinting.NONE)
                    .setBaselineSnapped(true);
            var line = TextLine.make(text ,font);
            try(font; line) {
                canvas.translate(215, 152);
                canvas.drawTextLine(line, 0, line.getCapHeight(), p_white);
            }
            canvas.restore();
            return this;
        }
        public ACardBuilder drowC(String text, int x, int y){
            canvas.save();
            Typeface typeface = SkiaUtil.getTorusSemiBold();
            Font font = new Font(typeface,24)
                    .setHinting(FontHinting.NONE)
                    .setBaselineSnapped(true);
            var line = TextLine.make(text ,font);
            try(font; line) {
                canvas.translate(x, y);
                canvas.drawTextLine(line, -line.getWidth(), line.getCapHeight(), p_white);
            }
            canvas.restore();
            return this;
        }
        public Image build(){
            return build(20);
        }
    }
    public static class A1CardBuilder extends ACardBuilder {
        /***
         * 头像
         * @param head_url
         * @return
         */
        public A1CardBuilder drowA1(String head_url){
            canvas.save();
            final Image head = SkiaUtil.lodeNetWorkImage(head_url);
            try (head){
                canvas.translate(20,20);
                canvas.clipRRect(RRect.makeXYWH(0,0,100,100,10));
                canvas.drawImage(SkiaUtil.getScaleCenterImage(head, 100, 100),0,0);
            }
            canvas.restore();
            return this;
        }

        /***
         * 国旗,撒泼特,好友状态
         * @return
         */
        public A1CardBuilder drowA2(Object ...loge){
            //todo
            return this;
        }

        /***
         * 名字
         * @param text
         * @return
         */
        public A1CardBuilder drowA3(String text){
            canvas.save();
            Typeface typeface = SkiaUtil.getTorusSemiBold();
            final Font font = new Font(typeface,48)
                    .setHinting(FontHinting.NONE)
                    .setBaselineSnapped(true);
            final var line = TextLine.make(text ,font);
            try(font; line){
                canvas.translate(130, 23);
                canvas.drawTextLine(line,0,line.getCapHeight(), p_white);
            }
            canvas.restore();
            return this;
        }
    }
    public static class A2CardBuilder extends ACardBuilder {
        /***
         * 铺面状态
         * @param flag
         * @return
         */
        public A2CardBuilder drowD5(Object flag){
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
        public A2CardBuilder drowD4(String text){
            canvas.save();
            Typeface typeface = SkiaUtil.getPuhuitiMedium();
            final Font font = new Font(typeface,36)
                    .setHinting(FontHinting.NONE)
                    .setBaselineSnapped(true);
            final var line = TextLine.make(text ,font);
            try(font; line){
                canvas.translate(20, 24);
                canvas.drawTextLine(line,0,line.getCapHeight(), p_white);
            }
            canvas.restore();
            return this;
        }
        public A2CardBuilder drowD3(String text){
            canvas.save();
            Typeface typeface = SkiaUtil.getPuhuitiMedium();
            final Font font = new Font(typeface,24)
                    .setHinting(FontHinting.NONE)
                    .setBaselineSnapped(true);
            final var line = TextLine.make(text ,font);
            try(font; line){
                canvas.translate(20, 62);
                canvas.drawTextLine(line,0,line.getCapHeight(), p_white);
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
        public A2CardBuilder drowD2(String mode, int color){
            canvas.save();
            Typeface typeface = null;
            try {
                typeface = SkiaUtil.getEXTRA();
            } catch (Exception e) {
                log.error("字体加载异常",e);
                return this;
            }
            final Font font = new Font(typeface,24)
                    .setHinting(FontHinting.NONE)
                    .setBaselineSnapped(true);
            final var line = TextLine.make(mode ,font);
            try(font; line){
                canvas.translate(20, 94);
                canvas.drawTextLine(line,0,line.getCapHeight(), new Paint().setColor(color));
            }
            canvas.restore();
            return this;
        }
        public A2CardBuilder drowD1(String text){
            canvas.save();
            Typeface typeface = SkiaUtil.getPuhuitiMedium();
            final Font font = new Font(typeface,24)
                    .setHinting(FontHinting.NONE)
                    .setBaselineSnapped(true);
            final var line = TextLine.make(text ,font);
            try(font; line){
                canvas.translate(45, 94);
                canvas.drawTextLine(line,0,line.getCapHeight(), p_white);
            }
            canvas.restore();
            return this;
        }
    }
    //-----------------------------

    public static class PPMBuilder extends PanelBuilder{
        PPMBuilder() {
            super(1920,1080);
        }
        //r430 m510 m790 m960 m1130 m1410 r1840

        /***
         * 绘制六边形
         * @param point 输入数值,范围[0-1];
         * @param color 颜色预设 true:蓝色 | false:红色
         * @return return
         */
        public PPMBuilder drowHexagon(float[] point,boolean color){
            if (point.length != 6){
                throw new RuntimeException("输入参数长度错误");
            }
            canvas.save();
            Path[] paths = SkiaUtil.creat6(180, 10,point);
            if (paths == null || paths.length != 2) {
                throw new RuntimeException("创建形状错误");
            }
            final var pt1 = paths[0];
            final var pt2 = paths[1];
            int c = color ? Color.makeRGB(0,0,0) : Color.makeRGB(0,0,1);
            try(pt1;pt2){
                canvas.translate(960, 600);
                //填充,半透
                canvas.drawPath(pt1, new Paint().setColor(c).setAlpha(102).setStroke(false));
                canvas.drawPath(pt1, new Paint().setColor(c).setStrokeWidth(5).setStroke(true));
                canvas.drawPath(pt2, new Paint().setColor(c).setStroke(false));
            }
            canvas.restore();
            return this;
        }
        public PPMBuilder drowLeftValue1(String text){
            return drowLeftValue(text, 370+12);
        }
        public PPMBuilder drowLeftName1(String text){
            return drowLeftName(text, 370+12);
        }
        public PPMBuilder drowLeftValue2(String text){
            return drowLeftValue(text, 485+12);
        }
        public PPMBuilder drowLeftName2(String text){
            return drowLeftName(text, 370+12);
        }
        public PPMBuilder drowLeftValue3(String text){
            return drowLeftValue(text, 600+12);
        }
        public PPMBuilder drowLeftName3(String text){
            return drowLeftName(text, 370+12);
        }
        public PPMBuilder drowLeftValue4(String text){
            return drowLeftValue(text, 715+12);
        }
        public PPMBuilder drowLeftName4(String text){
            return drowLeftName(text, 370+12);
        }
        public PPMBuilder drowLeftValue5(String text){
            return drowLeftValue(text, 830+12);
        }
        public PPMBuilder drowLeftName5(String text){
            return drowLeftName(text, 370+12);
        }
        public PPMBuilder drowLeftValue6(String text){
            return drowLeftValue(text, 945+12);
        }
        public PPMBuilder drowLeftName6(String text){
            return drowLeftName(text, 370+12);
        }
        public PPMBuilder drowRightValue1(String text){
            return drowRightValue(text, 370+12);
        }
        public PPMBuilder drowRightName1(String text){
            return drowRightName(text, 370+12);
        }
        public PPMBuilder drowRightValue2(String text){
            return drowRightValue(text, 485+12);
        }
        public PPMBuilder drowRightName2(String text){
            return drowRightName(text, 370+12);
        }
        public PPMBuilder drowRightValue3(String text){
            return drowRightValue(text, 600+12);
        }
        public PPMBuilder drowRightName3(String text){
            return drowRightName(text, 370+12);
        }
        public PPMBuilder drowRightValue4(String text){
            return drowRightValue(text, 715+12);
        }
        public PPMBuilder drowRightName4(String text){
            return drowRightName(text, 370+12);
        }
        public PPMBuilder drowRightValue5(String text){
            return drowRightValue(text, 830+12);
        }
        public PPMBuilder drowRightName5(String text){
            return drowRightName(text, 370+12);
        }
        public PPMBuilder drowRightValue6(String text){
            return drowRightValue(text, 945+12);
        }
        public PPMBuilder drowRightName6(String text){
            return drowRightName(text, 370+12);
        }
        public PPMBuilder drowLeftTotal(String text){
            return drowCenterText(text, 790,945+12);
        }
        public PPMBuilder drowRightTotal(String text){
            return drowCenterText(text, 1130,945+12);
        }

        /***
         * 左侧数值名称
         * @param text
         * @param top
         * @return
         */
        private PPMBuilder drowLeftName(String text, int top){
            return drowLeftText(text, 80, top);
        }

        /***
         * 右侧数值名称
         * @param text
         * @param top
         * @return
         */
        private PPMBuilder drowRightName(String text, int top){
            return drowLeftText(text, 1490, top);
        }

        /***
         * 左侧数值
         * @param text
         * @param top
         * @return
         */
        private PPMBuilder drowLeftValue(String text, int top){
            return drowRightText(text, 430, top);
        }

        /***
         * 右侧数值
         * @param text
         * @param top
         * @return
         */
        private PPMBuilder drowRightValue(String text, int top){
            return drowRightText(text, 1840, top);
        }
        //分别对应着 左 中 右 文字对齐方式
        /***
         * 左对齐渲染
         * @param text
         * @param right
         * @param top
         * @return
         */
        private PPMBuilder drowLeftText(String text, int right, int top){
            canvas.save();
            Typeface typeface = SkiaUtil.getTorusSemiBold();
            final Font font = new Font(typeface, 60);
            final var line = TextLine.make(text,font);
            try (typeface;font){
                canvas.translate(right, top);
                canvas.drawTextLine(line, -line.getWidth()*0.5f, line.getCapHeight(), p_white);
            }
            canvas.restore();
            return this;
        }

        /***
         * 居中对齐渲染
         * @param text
         * @param right
         * @param top
         * @return
         */
        private PPMBuilder drowCenterText(String text, int right, int top){
            canvas.save();
            Typeface typeface = SkiaUtil.getTorusSemiBold();
            final Font font = new Font(typeface, 60);
            final var line = TextLine.make(text,font);
            try (typeface;font){
                canvas.translate(right, top);
                canvas.drawTextLine(line, -line.getWidth()*0.5f, line.getCapHeight(), p_white);
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
        private PPMBuilder drowRightText(String text, int right, int top){
            canvas.save();
            Typeface typeface = SkiaUtil.getTorusSemiBold();
            final Font font = new Font(typeface, 60);
            final var line = TextLine.make(text,font);
            try (typeface;font){
                canvas.translate(right, top);
                canvas.drawTextLine(line, -line.getWidth(), line.getCapHeight(), p_white);
            }
            canvas.restore();
            return this;
        }
    }
    /***
     * 玩家面板
     * @return
     */
    public static A1CardBuilder getA1Builder(){
        return new A1CardBuilder();
    }

    /***
     * 谱面/成绩面板
     * @return
     */
    public static A2CardBuilder getA2Builder(){
        return new A2CardBuilder();
    }
    public static Image Card1(){
        Image img = null;
        try (Surface surface = Surface.makeRasterN32Premul(430,210);
        ){
            Canvas canvas = surface.getCanvas();

            img = RRectout(surface, 20);
        }
        return img;
    }
    public static Image Card2(){
        Image img = null;
        try (Surface surface = Surface.makeRasterN32Premul(430,210);
        ){
            Canvas canvas = surface.getCanvas();

            img = RRectout(surface, 20);
        }
        return img;
    }
    public static Image RRectout(Surface surface, int r){
        var img = surface.makeImageSnapshot();
        var canvas = surface.getCanvas();
        canvas.clear(0);
        canvas.save();
        canvas.clipRRect(RRect.makeXYWH(0,0, surface.getWidth(), surface.getHeight(),r));
        canvas.drawImage(img, 0, 0, new Paint().setAntiAlias(true).setMode(PaintMode.FILL));
        canvas.restore();
        var rImg = surface.makeImageSnapshot();
        canvas.clear(0);
        canvas.drawImage(img,0,0);
        return rImg;
    }
}