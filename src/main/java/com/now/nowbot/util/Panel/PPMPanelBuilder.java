package com.now.nowbot.util.Panel;

import com.now.nowbot.config.NowbotConfig;
import com.now.nowbot.util.PanelUtil;
import com.now.nowbot.util.SkiaUtil;
import org.jetbrains.skija.Image;

import java.io.IOException;

public class PPMPanelBuilder extends PPPanelBuilder {
    /**banner*/
    public PPMPanelBuilder drowBanner(){
        try {
            drowTopBackground(SkiaUtil.fileToImage(NowbotConfig.BG_PATH+"ExportFileV3/Banner/b3.jpg"));
        } catch (IOException e) {
            log.error("PPMPanelBuilder->ppm banner素材加载失败", e);
        }
        return this;
    }
    /**叠加层*/
    public PPMPanelBuilder drowOverImage(){
        try {
            drowImage(SkiaUtil.fileToImage(NowbotConfig.BG_PATH+"ExportFileV3/panel-ppmodule.png"));
        } catch (IOException e) {
            log.error("PPMPanelBuilder->ppm 叠加层素材加载失败", e);
        }
        return this;
    }
    /**名字*/
    public PPMPanelBuilder drowValueName(){
        drowLeftNameN(0, "FAC");
        drowLeftNameN(1, "PTT");
        drowLeftNameN(2, "STA");
        drowLeftNameN(3, "STB");
        drowLeftNameN(4, "ENG");
        drowLeftNameN(5, "STH");
        return this;
    }
    public PPMPanelBuilder switchRank(int i, double date){
        if (date>0.95){
            drowLeftRankN(i, "SS", PanelUtil.COLOR_SS);
        }
        else if(date>0.90){
            drowLeftRankN(i, "S", PanelUtil.COLOR_S);
        }
        else if(date>0.85){
            drowLeftRankN(i, "A+", PanelUtil.COLOR_A_PLUS);
        }
        else if(date>0.80){
            drowLeftRankN(i, "A", PanelUtil.COLOR_A);
        }
        else if(date>0.70){
            drowLeftRankN(i, "B", PanelUtil.COLOR_B);
        }
        else if(date>0.60){
            drowLeftRankN(i, "C", PanelUtil.COLOR_C);
        }
        else if(date>0){
            drowLeftRankN(i, "D", PanelUtil.COLOR_D);
        }
        else {
            drowLeftRankN(i, "F", PanelUtil.COLOR_F);
        }
        return this;
    }

    /***
     * 左侧card
     * @param card
     * @return
     */
    @Override
    public PPMPanelBuilder drowLeftCard(Image card) {
        super.drowLeftCard(card);
        return this;
    }

    public PPMPanelBuilder drowLeftValueN(int n, String bigText, String simText){
        super.drowLeftValueN(n, bigText, simText);
        return this;
    }
}